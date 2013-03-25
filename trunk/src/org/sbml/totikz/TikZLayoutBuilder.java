/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2008-2013 by the University of Tuebingen, Germany.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */

package org.sbml.totikz;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.tolatex.util.LaTeX;

import de.zbit.sbml.layout.AbstractLayoutBuilder;
import de.zbit.sbml.layout.AssociationNode;
import de.zbit.sbml.layout.Catalysis;
import de.zbit.sbml.layout.Compartment;
import de.zbit.sbml.layout.Consumption;
import de.zbit.sbml.layout.DissociationNode;
import de.zbit.sbml.layout.Inhibition;
import de.zbit.sbml.layout.LayoutBuilder;
import de.zbit.sbml.layout.Macromolecule;
import de.zbit.sbml.layout.Modulation;
import de.zbit.sbml.layout.NecessaryStimulation;
import de.zbit.sbml.layout.NucleicAcidFeature;
import de.zbit.sbml.layout.OmittedProcessNode;
import de.zbit.sbml.layout.PerturbingAgent;
import de.zbit.sbml.layout.ProcessNode;
import de.zbit.sbml.layout.Production;
import de.zbit.sbml.layout.ReversibleConsumption;
import de.zbit.sbml.layout.SBGNArc;
import de.zbit.sbml.layout.SBGNNode;
import de.zbit.sbml.layout.SBGNReactionNode;
import de.zbit.sbml.layout.SimpleChemical;
import de.zbit.sbml.layout.SimpleLayoutAlgorithm;
import de.zbit.sbml.layout.SourceSink;
import de.zbit.sbml.layout.Stimulation;
import de.zbit.sbml.layout.UncertainProcessNode;
import de.zbit.sbml.layout.UnspecifiedNode;
import de.zbit.util.ResourceManager;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * This class writes the TikZ-Commands for drawing the different components of a
 * reaction glyph
 * 
 * @author Mirjam Gutekunst
 * @version $Rev$
 */
public class TikZLayoutBuilder<W extends Writer> extends AbstractLayoutBuilder<W, String, String> {

	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(SimpleLayoutAlgorithm.class.toString());
	
	/**
	 * Localization support.
	 */
	private ResourceBundle bundle = ResourceManager.getBundle("org.sbml.totikz.locales.UI");
	
	private W writer;
	/**
	 * Switch to decide whether or not the document head and foot of the LaTeX document should be written to the {@link Writer}.
	 */
	private boolean footAndHeadIncluded;
	
	/**
	 * @return the footAndHeadIncluded
	 */
	public boolean isFootAndHeadIncluded() {
		return footAndHeadIncluded;
	}
	
	public static double DEFAULT_LINE_WIDTH = 0.25d;

	/**
	 * 
	 * @param writer for the TikZ commands
	 */
	public TikZLayoutBuilder(W writer) {
		this(writer, true);
	}
	
	/**
	 * 
	 * @param writer
	 * @param isFootAndHeadIncluded
	 *        Switch to decide whether or not the document head and foot of the
	 *        LaTeX document should be written to the {@link Writer}. If this is
	 *        {@code true}, this class will create a stand-alone LaTeX document,
	 *        which can directly be compiled without any further editing. If, in
	 *        contrast, the Ti<i>k</i>Z image is to be included in a larger LaTeX
	 *        document, this switch can be set to {@code false}, and only the part
	 *        that actually creates the image will be created by this
	 *        {@link LayoutBuilder}. Furthermore, in this case also the
	 *        {@link Writer} will not be closed by the {@link #builderEnd()}
	 *        method. This is important if a writer for a larger document is
	 *        re-used elsewhere.
	 */
	public TikZLayoutBuilder(W writer, boolean isFootAndHeadIncluded) {
		super();
		this.writer = writer;
		this.footAndHeadIncluded = isFootAndHeadIncluded;
	}

	/**
	 * method for opening the BufferedWriter and writing the beginning of the
	 * LaTeX file
	 * 
	 * @param layout
	 */
	public void builderStart(Layout layout) {
		try {
			if (footAndHeadIncluded) {
				writer.write(LaTeX.dcoumentClass("scrartcl", 14));
				writeRequiredPackageDeclarationAndDefinitions(writer, layout);
				writer.write(LaTeX.pageStyle("empty"));
				writer.write(LaTeX.beginDocument());
				writer.write(LaTeX.beginCenter());
			}
			Dimensions dimension = layout.getDimensions();
			double width, height, defaultVal = 1000d;
			if (dimension != null) {
				width = dimension.isSetWidth() ? dimension.getWidth() : defaultVal;
				height = dimension.isSetHeight() ? dimension.getHeight() : defaultVal;
			} else {
				width = height = defaultVal;
				logger.warning(MessageFormat.format(
					bundle.getString("NO_DIMENSIONS_FOR_LAYOUT"),
					defaultVal, defaultVal));
			}
			writer.write(TikZ.beginTikZPicture(width, height));
			//TODO: Change scaling for scaling the arrows too.
			writer.write(LaTeX.scaleFont(.3d)); // field for changing the text size
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	/**
	 * 
	 * @param writer
	 * @param layouts
	 * @throws IOException
	 */
	public static void writeRequiredPackageDeclarationAndDefinitions(Writer writer, Iterable<Layout> layouts) throws IOException {
		for (String pckg : new String[] {"scalefnt", "tikz", "amssymb"}) {
			writer.write(LaTeX.usepackage(pckg).toString());
		}
		writer.write(TikZ.useTikZLibrary("arrows", "decorations.pathmorphing", "backgrounds", "positioning", "fit", "petri"));
		boolean compartments = false, species = false;
		if (layouts != null) {
			for (Layout layout : layouts) {
				if (!compartments && (layout.getCompartmentGlyphCount() > 0)) {
					writer.write(LaTeX.defineColor("compartment", 204d, 204d, 0d));
					compartments = true;
				}
				if (!species && (layout.getSpeciesGlyphCount() > 0)) {
					writer.write(LaTeX.defineColor("unspecifiedEntity", 204d, 204d, 204d));
					writer.write(LaTeX.defineColor("simpleChemical", 153d, 153d, 255d));
					writer.write(LaTeX.defineColor("NucleicAcidFeature", 153d, 153d, 255d));
					writer.write(LaTeX.defineColor("PerturbingAgent", 255d, 0d, 255d));
					writer.write(LaTeX.defineColor("macromolecule", 204d, 255d, 204d));
					writer.write(LaTeX.defineColor("sourceSink", 255d, 204d, 204d));
					species = true;
				}
			}
		}
	}

	/**
	 * 
	 * @param writer
	 * @param layout
	 * @throws IOException
	 */
	public static void writeRequiredPackageDeclarationAndDefinitions(Writer writer, Layout layout) throws IOException {
		writeRequiredPackageDeclarationAndDefinitions(writer, Arrays.asList(layout));
	}

	/**
	 * method for writing the TikZ commands to draw a {@link CompartmentGlyph}.
	 * 
	 * @param compartmentGlyp to be drawn
	 */
	public void buildCompartment(CompartmentGlyph compartmentGlyph) {
		try {

			BoundingBox boundingBox = compartmentGlyph.getBoundingBox();
			Point point = boundingBox.getPosition();
			Dimensions dimension = boundingBox.getDimensions();

			double x = point.getX();
			double y = point.getY();
			double z = point.getZ();
			double width = dimension.getWidth();
			double height = dimension.getHeight();
			double depth = dimension.getDepth();

			Compartment<String> node = createCompartment();
			writer.write(node.draw(x, y, z, width, height, depth));

		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildEntityPoolNode(org.sbml.jsbml.ext.layout.SpeciesGlyph, boolean)
	 */
	public void buildEntityPoolNode(SpeciesGlyph speciesGlyph, boolean cloneMarker) {
		try {
			SBGNNode<String> node = getSBGNNode(speciesGlyph.getSBOTerm());

			if (cloneMarker) {
				node.setCloneMarker();
			}

			BoundingBox boundingBox = speciesGlyph.getBoundingBox();
			Point point = boundingBox.getPosition();
			Dimensions dimension = boundingBox.getDimensions();

			writer.write(node.draw(point.getX(), point.getY(), point.getZ(),
					dimension.getWidth(), dimension.getHeight(), dimension.getDepth()));

		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	/* (non-Javadoc)
	 * @see org.sbml.totikz.LayoutBuilder#buildCubicBezier(CubicBezier cubicBezier)
	 */
	public void buildCubicBezier(CubicBezier cubicBezier, double lineWidth) {
		try {
			TikZCubicBezier node = new TikZCubicBezier();
			if (lineWidth >= 0) {
				writer.write(node.draw(cubicBezier, lineWidth* DEFAULT_LINE_WIDTH));
			} else {
				writer.write(node.draw(cubicBezier));
			}
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}

	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildConnectingArc(org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph, org.sbml.jsbml.ext.layout.ReactionGlyph)
	 */
	public void buildConnectingArc(SpeciesReferenceGlyph speciesReferenceGlyph, ReactionGlyph rg, double curveWidth) {
		try {
			SBGNArc<String> arc = createArc(speciesReferenceGlyph, rg);
			Curve curve;
			if (speciesReferenceGlyph.isSetCurve()) {
				curve = speciesReferenceGlyph.getCurve();
			} else {
				curve = rg.getCurve();
			}

			if (curveWidth >= 0) {
				writer.write(arc.draw(curve, curveWidth * DEFAULT_LINE_WIDTH));
			}
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildProcessNode(org.sbml.jsbml.ext.layout.ReactionGlyph, double)
	 */
	public void buildProcessNode(ReactionGlyph reactionGlyph, double rotationAngle, double curveWidth) {
		try {
			SBGNReactionNode<String> node;
			if (reactionGlyph.isSetReaction()) {
				node = getSBGNReactionNode(reactionGlyph.getReactionInstance().getSBOTerm());
			} else {
				node = getSBGNReactionNode(reactionGlyph.getSBOTerm());
			}
			node.setLineWidth(curveWidth * DEFAULT_LINE_WIDTH);

			BoundingBox boundingBox = reactionGlyph.getBoundingBox();
			Point point = boundingBox.getPosition();
			Dimensions dimension = boundingBox.getDimensions();

			double x = point.getX();
			double y = point.getY();
			double z = point.getZ();
			double half_of_width = dimension.getWidth() / 2d;
			double half_of_height = dimension.getHeight() / 2d;
			double half_of_depth = dimension.getDepth() / 2d;

			// the position is left-above...
			x += half_of_width;
			y += half_of_height;
			z += half_of_depth;

			// two short lines from the reaction (10pt long)
			LineSegment line1 = new LineSegment();
			line1.createStart((x + (half_of_width/2d)) , y, 0);
			line1.createEnd((x + half_of_width), y, 0);
			LineSegment line2 = new LineSegment();
			line2.createStart((x - (half_of_width/2d)), y, 0);
			line2.createEnd((x - half_of_width), y, 0);
			Point rotationPoint = new Point(x, y, 0);

			if (curveWidth >= 0) {
				// draws the box of the reaction, 10pt x 10pt
				if ((rotationAngle % 180) == 0) {
					writer.write(node.draw(x, y, z, half_of_width, half_of_height, half_of_depth));
				} else {
					// this call rotates the whole node
					writer.write(node.draw(x, y, z, half_of_width, half_of_height, half_of_depth, rotationAngle, rotationPoint));
				}
				writer.write(node.drawLineSegment(line1, rotationAngle, rotationPoint));
				writer.write(node.drawLineSegment(line2, rotationAngle, rotationPoint));
			}

		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	/* (non-Javadoc)
	 * @see org.sbml.totikz.LayoutBuilder#buildTextGlyph(TextGlyph textGlyph)
	 */
	public void buildTextGlyph(TextGlyph textGlyph) {
		try {
			BoundingBox boundingBox = textGlyph.getBoundingBox();
			Point point = boundingBox.getPosition();
			double x = point.getX();
			double y = point.getY();
			Dimensions dimension = boundingBox.getDimensions();
			double height = dimension.getHeight() / 2d;
			double width = dimension.getWidth() / 2d;
			NamedSBase nsb = textGlyph.getOriginOfTextInstance();

			String text = ""; // No text set!
			if (textGlyph.isSetText()) {
				// if this text glyph corresponds to a source-sink species, then
				// the name is not printed in the species.
				if (!SBO.isChildOf(nsb.getSBOTerm(), SBO.getEmptySet())) {
					text = textGlyph.getText();
				}
			} else {
				if (textGlyph.isSetOriginOfText()) {
					if ((nsb != null) && nsb.isSetName()) {
						if (!SBO.isChildOf(nsb.getSBOTerm(), SBO.getEmptySet())) {
							text = nsb.getName();
						}
					}
				}
			}
			text = LaTeX.maskSpecialChars(text);

			if (nsb instanceof CompartmentGlyph) {
				writer.write(TikZ.drawText(x + width, y + height, "below left", "phv", text));
			} else {
				writer.write(TikZ.drawText(x + width, y + height, "anchor = center", "phv", text));
			}

		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	/**
	 * Method for writing the commands necessary at the end of a LaTeX file and
	 * closing the {@link FileWriter}.
	 */
	public void builderEnd() {
		try {
			writer.write(TikZ.endTikZPicture());
			if (footAndHeadIncluded) {
				writer.write(LaTeX.endCenter());
				writer.write(LaTeX.endDocument());
				writer.close();
			}
			terminated = true;
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	/* (non-Javadoc)
	 * @see org.sbml.totikz.LayoutBuilder#getProduct()
	 */
	public W getProduct() {
		return writer;
	}

	/* (non-Javadoc)
	 * @see org.sbml.totikz.LayoutBuilder#createMacromolecule()
	 */
	public Macromolecule<String> createMacromolecule() {
		return new TikZMacromolecule();
	}

	/* (non-Javadoc)
	 * @see org.sbml.totikz.LayoutBuilder#createSourceSink()
	 */
	public SourceSink<String> createSourceSink() {
		return new TikZSourceSink();
	}

	/* (non-Javadoc)
	 * @see org.sbml.totikz.LayoutBuilder#createUnspecifiedNode()
	 */
	public UnspecifiedNode<String> createUnspecifiedNode() {
		return new TikZUnspecifiedNode();
	}

	/* (non-Javadoc)
	 * @see org.sbml.totikz.LayoutBuilder#createSimpleChemical()
	 */
	public SimpleChemical<String> createSimpleChemical() {
		return new TikZSimpleChemical();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createCompartment()
	 */
	public Compartment<String> createCompartment() {
		return new TikZCompartment();
	}

	/* (non-Javadoc)
	 * @see org.sbml.totikz.LayoutBuilder#createProduction()
	 */
	public Production<String> createProduction() {
		return new TikZProduction();
	}

	/* (non-Javadoc)
	 * @see org.sbml.totikz.LayoutBuilder#createConsumption()
	 */
	public Consumption<String> createConsumption() {
		return new TikZConsumption();
	}

	/* (non-Javadoc)
	 * @see org.sbml.totikz.LayoutBuilder#createCatalysis()
	 */
	public Catalysis<String> createCatalysis() {
		return new TikZCatalysis();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createProcessNode()
	 */
	public ProcessNode<String> createProcessNode() {
		return new TikZProcessNode();
	}

	/* (non-Javadoc)
	 * @see org.sbml.totikz.LayoutBuilder#createInhibition()
	 */
	public Inhibition<String> createInhibition() {
		return new TikZInhibition();
	}

	/* (non-Javadoc)
	 * @see org.sbml.totikz.LayoutBuilder#addProgressListener(AbstractProgressBar progress)
	 */
	public void addProgressListener(AbstractProgressBar progress) {
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createModulation()
	 */
	//@Override
	public Modulation<String> createModulation() {
		return new TikZModulation();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createNecessaryStimulation()
	 */
	//@Override
	public NecessaryStimulation<String> createNecessaryStimulation() {
		return new TikZNecessaryStimulation();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createPerturbingAgent()
	 */
	//@Override
	public PerturbingAgent<String> createPerturbingAgent() {
		return new TikZPerturbingAgent();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createNucleicAcidFeature()
	 */
	//@Override
	public NucleicAcidFeature<String> createNucleicAcidFeature() {
		return new TikZNucleicAcidFeature();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createStimulation()
	 */
	//@Override
	public Stimulation<String> createStimulation() {
		return new TikZStimulation();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createOmittedProcessNode()
	 */
	//@Override
	public OmittedProcessNode<String> createOmittedProcessNode() {
		return new TikZOmittedProcessNode();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createAssociationNode()
	 */
	//@Override
	public AssociationNode<String> createAssociationNode() {
		return new TikZAssociationNode();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createDissociationNode()
	 */
	//@Override
	public DissociationNode<String> createDissociationNode() {
		return new TikZDissociationNode();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createUncertainProcessNode()
	 */
	//@Override
	public UncertainProcessNode<String> createUncertainProcessNode() {
		return new TikZUncertainProcessNode();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createReversibleConsumption()
	 */
	//@Override
	public ReversibleConsumption<String> createReversibleConsumption() {
		return new TikZReversibleConsumption();
	}

}
