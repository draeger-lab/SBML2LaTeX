/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2008-2014 by the University of Tuebingen, Germany.
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

import java.util.HashSet;
import java.util.Set;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;

import de.zbit.sbml.layout.LayoutDirector;
import de.zbit.sbml.layout.SimpleLayoutAlgorithm;

/**
 * @composed - - - RelativePosition
 * 
 * @author Mirjam Gutekunst
 * @since 1.0
 * @version $Rev$
 */
public class TikZLayoutAlgorithm extends SimpleLayoutAlgorithm {

	/**
	 * A counter for compartment glyphs
	 */
	private int compartmentGlyphCounter = 0;

	/**
	 * The default text glyphs position
	 */
	private double defaultTextGlyphYPosition = 0d;

	/**
	 * The y-position of species glyphs
	 */
	private double speciesGlyphYPosition = 0d;

	/**
	 * Set to hold all glyphs
	 */
	protected Set<GraphicalObject> setOfAllGlyphs;

	private int layoutLevel = level;
	private int layoutVersion = version;


	/**
	 * Constructor that creates a new set of all glyphs.
	 */
	public TikZLayoutAlgorithm() {
		super();
		setOfAllGlyphs = new HashSet<GraphicalObject>();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#addLayoutedGlyph(org.sbml.jsbml.ext.layout.GraphicalObject)
	 */
	@Override
	public void addLayoutedGlyph(GraphicalObject glyph) {
		// look if the dimensions fit to the glyph
		correctDimensions(glyph);
		setOfLayoutedGlyphs.add(glyph);
		setOfAllGlyphs.add(glyph);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#addUnlayoutedGlyph(org.sbml.jsbml.ext.layout.GraphicalObject)
	 */
	@Override
	public void addUnlayoutedGlyph(GraphicalObject glyph) {
		//return the set of unlayouted glyphs
		setOfUnlayoutedGlyphs.add(glyph);
		setOfAllGlyphs.add(glyph);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#addLayoutedEdge(org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph, org.sbml.jsbml.ext.layout.ReactionGlyph)
	 */
	@Override
	public void addLayoutedEdge(SpeciesReferenceGlyph srg, ReactionGlyph rg) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#addUnlayoutedEdge(org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph, org.sbml.jsbml.ext.layout.ReactionGlyph)
	 */
	@Override
	public void addUnlayoutedEdge(SpeciesReferenceGlyph srg, ReactionGlyph rg) {
		//create curve
		if (!LayoutDirector.glyphIsLayouted(rg)
				|| !LayoutDirector.glyphIsLayouted(srg)) {
			completeLayoutInformation(rg);
			completeLayoutInformation(srg);
		} else {
			correctDimensions(rg);
		}
		Curve curve = createCurve(rg, srg);
		srg.setCurve(curve);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#completeGlyphs()
	 */
	@Override
	public Set<GraphicalObject> completeGlyphs() {
		// go through all unlayouted glyphs and layout them
		for (GraphicalObject currentGlyph : setOfUnlayoutedGlyphs) {
			completeLayoutInformation(currentGlyph);
			/*
			 * there is only this case, because the other glyphs are layouted
			 * with the call of completeLayoutInformation(currentGlyph)
			 */
			if (currentGlyph instanceof ReactionGlyph) {
				ReactionGlyph rg = (ReactionGlyph) currentGlyph;
				for (SpeciesReferenceGlyph spg : rg.getListOfSpeciesReferenceGlyphs()) {
					if (!spg.isSetCurve()) {
						spg.setCurve(createCurve(rg, spg));
					}
				}
			}
			setOfLayoutedGlyphs.add(currentGlyph);
		}
		return setOfLayoutedGlyphs;
	}

	/**
	 * Methods completes the layout informations of the incoming glyph.
	 * @param currentGlyph
	 */
	private void completeLayoutInformation(GraphicalObject currentGlyph) {
		// if the current glyph is already layouted there is nothing more to complete
		if (!LayoutDirector.glyphIsLayouted(currentGlyph)) {
			if (!currentGlyph.isSetBoundingBox()) {
				createGlyphBoundingBox(currentGlyph, null);
			} else {
				if (!LayoutDirector.glyphHasPosition(currentGlyph)) {
					Point positionPoint = null;
					if (currentGlyph instanceof CompartmentGlyph) {
						positionPoint = createCompartmentGlyphPosition((CompartmentGlyph) currentGlyph);
					}  else if (currentGlyph instanceof ReactionGlyph) {
						positionPoint = createReactionGlyphPosition((ReactionGlyph) currentGlyph);
					} else if (currentGlyph instanceof SpeciesReferenceGlyph) {
						//this case does not exist, because thats the task of addLayoutedEdge or addUnlayoutedEdge
					} else if (currentGlyph instanceof SpeciesGlyph) {
						positionPoint = createSpeciesGlyphPosition((SpeciesGlyph) currentGlyph);
					} else if (currentGlyph instanceof TextGlyph) {
						positionPoint = createTextGlyphPosition((TextGlyph) currentGlyph);
					}
					currentGlyph.getBoundingBox().setPosition(positionPoint);
					correctDimensions(currentGlyph);
				} else if (!LayoutDirector.glyphHasDimensions(currentGlyph)) {
					Dimensions dimensions = null;
					if (currentGlyph instanceof CompartmentGlyph) {
						dimensions = createCompartmentGlyphDimension((CompartmentGlyph) currentGlyph);
					} else if (currentGlyph instanceof ReactionGlyph) {
						dimensions = createReactionGlyphDimension((ReactionGlyph) currentGlyph);
					} else if (currentGlyph instanceof SpeciesReferenceGlyph) {
						//this case does not exist, because thats the task of addLayoutedEdge or addUnlayoutedEdge
					} else if (currentGlyph instanceof SpeciesGlyph) {
						dimensions = createSpeciesGlyphDimension();
					} else if (currentGlyph instanceof TextGlyph) {
						dimensions = createTextGlyphDimension((TextGlyph) currentGlyph);
					}
					currentGlyph.getBoundingBox().setDimensions(dimensions);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createCompartmentGlyphDimension(CompartmentGlyph previousCompartmentGlyph)
	 */
	@Override
	public Dimensions createCompartmentGlyphDimension(CompartmentGlyph previousCompartmentGlyph) {
		if (previousCompartmentGlyph == null) {
			return new Dimensions(300, 300, 0, layoutLevel, layoutVersion);
		} else {
			Dimensions previousDimension = previousCompartmentGlyph.getBoundingBox().getDimensions();
			// new CompartmentGlyph is drawn 20pt smaller than the previous
			double width = previousDimension.getWidth() - 20;
			double height = previousDimension.getHeight() - 20;
			double depth = previousDimension.getDepth() - 20;
			return new Dimensions(width, depth, height, layoutLevel, layoutVersion);
		}
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createCompartmentGlyphPosition(CompartmentGlyph previousCompartmentGlyph)
	 */
	@Override
	public Point createCompartmentGlyphPosition(CompartmentGlyph previousCompartmentGlyph) {
		if (previousCompartmentGlyph == null) {
			double x = 0;
			double y = compartmentGlyphCounter * 300;
			double z = 0;
			return new Point(x, y, z, layoutLevel, layoutVersion);
		} else {
			Point previousPosition = previousCompartmentGlyph.getBoundingBox().getPosition();
			// the position of the new CompartmentGlyph is moved 10pt from the position of the previous
			double x = previousPosition.getX() + 10;
			double y = previousPosition.getY() + 10;
			double z = previousPosition.getZ() + 10;
			return new Point(x, y, z, layoutLevel, layoutVersion);
		}
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createCurve(org.sbml.jsbml.ext.layout.ReactionGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public Curve createCurve(ReactionGlyph reactionGlyph, SpeciesReferenceGlyph specRefGlyph) {
		Curve curve = new Curve();
		layoutLevel = layout.getLevel();
		layoutVersion = layout.getVersion();

		curve.setLevel(layoutLevel);
		curve.setVersion(layoutVersion);

		ListOf<CurveSegment> curveSegmentsList = new ListOf<CurveSegment>(layoutLevel, layoutVersion);

		LineSegment curveSegment = new LineSegment();
		curveSegment.setLevel(layoutLevel);
		curveSegment.setVersion(layoutVersion);

		if (specRefGlyph.isSetSpeciesGlyph()) {
			SpeciesGlyph speciesGlyph = specRefGlyph.getSpeciesGlyphInstance();

			// curve point at reaction glyph
			Point rgCurvePoint = new Point(layoutLevel, layoutVersion);
			// curve point at species glyph
			Point sgCurvePoint = new Point(layoutLevel, layoutVersion);

			SpeciesReferenceRole specRefRole = specRefGlyph.getSpeciesReferenceRole();

			// Set the SpeciesGlyph
			Point middleOfSpecies = calculateCenter(speciesGlyph);
			sgCurvePoint = calculateSpeciesGlyphDockingPosition(middleOfSpecies, reactionGlyph, specRefRole, speciesGlyph);
			double rotationAngle = calculateReactionGlyphRotationAngle(reactionGlyph);
			rgCurvePoint = calculateReactionGlyphDockingPoint(reactionGlyph, rotationAngle, specRefGlyph);

			if (specRefRole.equals(SpeciesReferenceRole.PRODUCT) || specRefRole.equals(SpeciesReferenceRole.SIDEPRODUCT)) {
				//curve goes from reaction glyph to species glyph
				curveSegment.setStart(rgCurvePoint);
				curveSegment.setEnd(sgCurvePoint);
			} else {
				//curve goes from species glyph to reaction glyph
				curveSegment.setStart(sgCurvePoint);
				curveSegment.setEnd(rgCurvePoint);
			}
		}
		curveSegmentsList.add(curveSegment);
		curve.setListOfCurveSegments(curveSegmentsList);
		/*
		 * base points do not have to be considered, only if the curve is set.
		 */
		return curve;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createGlyphBoundingBox(org.sbml.jsbml.ext.layout.NamedSBaseGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public BoundingBox createGlyphBoundingBox(GraphicalObject glyph, SpeciesReferenceGlyph specRefGlyph) {
		BoundingBox boundingBox = createBoundingBoxWithLevelAndVersion();
		if (glyph instanceof TextGlyph) {
			TextGlyph textGlyph = (TextGlyph) glyph;
			boundingBox.setDimensions(createTextGlyphDimension(textGlyph));
			boundingBox.setPosition(createTextGlyphPosition(textGlyph));
		} else if (glyph instanceof ReactionGlyph) {
			ReactionGlyph reactionGlyph = (ReactionGlyph) glyph;
			if (specRefGlyph != null) {
				boundingBox.setDimensions(createSpeciesReferenceGlyphDimension(reactionGlyph, specRefGlyph));
				boundingBox.setPosition(createSpeciesReferenceGlyphPosition(reactionGlyph, specRefGlyph));
			} else {
				boundingBox.setDimensions(createReactionGlyphDimension(reactionGlyph));
				boundingBox.setPosition(createReactionGlyphPosition(reactionGlyph));
			}
		} else if (glyph instanceof SpeciesGlyph) {
			SpeciesGlyph speciesGlyph = (SpeciesGlyph) glyph;
			if (specRefGlyph != null) {
				boundingBox.setDimensions(createSpeciesGlyphDimension());
				boundingBox.setPosition(createSpeciesGlyphPosition(speciesGlyph,specRefGlyph));
			} else {
				boundingBox.setDimensions(createSpeciesGlyphDimension());
				boundingBox.setPosition(createSpeciesGlyphPosition(speciesGlyph));
			}
		} else if (glyph instanceof CompartmentGlyph) {
			CompartmentGlyph compartmentGlyph = (CompartmentGlyph) glyph;
			boundingBox.setDimensions(createCompartmentGlyphDimension(compartmentGlyph));
			boundingBox.setPosition(createCompartmentGlyphPosition(compartmentGlyph));
		}
		glyph.setBoundingBox(boundingBox);
		return boundingBox;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createLayoutDimension()
	 */
	@Override
	public Dimensions createLayoutDimension() {
		layoutLevel = layout.getLevel();
		layoutVersion = layout.getVersion();
		ListOf<SpeciesGlyph> speciesGlyphList = layout.getListOfSpeciesGlyphs();

		SpeciesGlyph leftMostGlyph = new SpeciesGlyph(layoutLevel, layoutVersion);
		leftMostGlyph.createBoundingBox(0, 0, 0, 0, 0, 0);

		SpeciesGlyph rightMostGlyph = new SpeciesGlyph(layoutLevel, layoutVersion);
		rightMostGlyph.createBoundingBox(0, 0, 0, 300, 300, 0);

		SpeciesGlyph topMostGlyph = new SpeciesGlyph(layoutLevel, layoutVersion);
		topMostGlyph.createBoundingBox(0, 0, 0, 0, 0, 0);

		SpeciesGlyph bottomMostGlyph = new SpeciesGlyph(layoutLevel, layoutVersion);
		bottomMostGlyph.createBoundingBox(0, 0, 0, 300, 300, 0);

		for (SpeciesGlyph speciesGlyph : speciesGlyphList) {
			if (speciesGlyph.isSetBoundingBox()) {
				BoundingBox sgBoundingBox = speciesGlyph.getBoundingBox();
				if (sgBoundingBox.isSetPosition() && sgBoundingBox.isSetDimensions()) {
					Point sgPosition = sgBoundingBox.getPosition();
					if (leftMostGlyph.getBoundingBox().getPosition().getX() > sgPosition.getX()) {
						leftMostGlyph = speciesGlyph;
					}
					if (rightMostGlyph.getBoundingBox().getPosition().getX() < sgPosition.getX()) {
						rightMostGlyph = speciesGlyph;
					}
					if (topMostGlyph.getBoundingBox().getPosition().getY() > sgPosition.getY()) {
						topMostGlyph = speciesGlyph;
					}
					if (bottomMostGlyph.getBoundingBox().getPosition().getY() < sgPosition.getY()) {
						bottomMostGlyph = speciesGlyph;
					}
				}
			}
		}

		double width = (rightMostGlyph.getBoundingBox().getPosition().getX()
				- leftMostGlyph.getBoundingBox().getPosition().getX())
				+ rightMostGlyph.getBoundingBox().getDimensions().getWidth();
		double height = (bottomMostGlyph.getBoundingBox().getPosition().getY()
				- topMostGlyph.getBoundingBox().getPosition().getY())
				+ bottomMostGlyph.getBoundingBox().getDimensions().getHeight();

		if (width == 0) {
			width = 300;
		}
		if (height == 0) {
			height = 300;
		}

		return new Dimensions(width, height, 0, layoutLevel, layoutVersion);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesGlyphDimension()
	 */
	@Override
	public Dimensions createSpeciesGlyphDimension() {
		// sets width and height to 60pt and depth to 0
		return new Dimensions(60, 60, 0, layoutLevel, layoutVersion);
	}

	/**
	 * Method to create the position of a {@link SpeciesGlyph}.
	 */
	private Point createSpeciesGlyphPosition(SpeciesGlyph speciesGlyph) {
		Dimensions dimension;
		if (speciesGlyph.isSetBoundingBox() && speciesGlyph.getBoundingBox().isSetDimensions()) {
			dimension = speciesGlyph.getBoundingBox().getDimensions();
		} else {
			dimension = createSpeciesGlyphDimension();
		}

		double x = 0;
		double y = speciesGlyphYPosition;
		double z = 0;
		speciesGlyphYPosition = speciesGlyphYPosition + dimension.getHeight();

		return new Point(x, y, z, layoutLevel, layoutVersion);

	}

	/**
	 * Method to create the position of a {@link SpeciesGlyph} in dependence of the
	 * corresponding {@link SpeciesReferenceGlyph}.
	 */
	private Point createSpeciesGlyphPosition(SpeciesGlyph speciesGlyph,
			SpeciesReferenceGlyph specRefGlyph) {
		double x = 0;
		double y = 0;
		double z = 0;

		SpeciesReferenceRole role = specRefGlyph.getSpeciesReferenceRole();
		Dimensions specGlyphDimension;
		if (speciesGlyph.isSetBoundingBox() && speciesGlyph.getBoundingBox().isSetDimensions()) {
			specGlyphDimension = speciesGlyph.getBoundingBox().getDimensions();
		} else {
			specGlyphDimension = createSpeciesGlyphDimension();
		}

		if (specRefGlyph.isSetCurve() || specRefGlyph.getCurve() != null) {
			Curve curve = specRefGlyph.getCurve();
			if (curve.isSetListOfCurveSegments()) {
				for (CurveSegment curveSegment : curve.getListOfCurveSegments()) {
					LineSegment ls = (LineSegment) curveSegment;
					Point startPoint;
					Point endPoint;

					if (role.equals(SpeciesReferenceRole.PRODUCT)) {
						// point at reaction glyph
						startPoint = ls.getStart();
						// point at species glyph
						endPoint = ls.getEnd();
					} else {
						// point at reaction glyph
						startPoint = ls.getEnd();
						// point at species glyph
						endPoint = ls.getStart();
					}

					RelativePosition relativePosition = getRelativePosition(startPoint, endPoint);
					if (relativePosition.equals(RelativePosition.ABOVE)) {
						x = endPoint.getX() - (specGlyphDimension.getWidth() / 2d);
						y = endPoint.getY() - specGlyphDimension.getHeight();
					}
					if (relativePosition.equals(RelativePosition.BELOW)) {
						x = endPoint.getX() - (specGlyphDimension.getWidth() / 2d);
						y = endPoint.getY();
					}
					if (relativePosition.equals(RelativePosition.LEFT)) {
						x = endPoint.getX() - specGlyphDimension.getWidth();//
						y = endPoint.getY() - (specGlyphDimension.getHeight() / 2d);
					}
					if (relativePosition.equals(RelativePosition.RIGHT)) {
						x = endPoint.getX();
						y = endPoint.getY() - (specGlyphDimension.getHeight() / 2d);
					}
				}
			}
		}

		return new Point(x, y, z, layoutLevel, layoutVersion);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesReferenceGlyphDimension(ReactionGlyph reactionGlyph, SpeciesReferenceGlyph speciesReferenceGlyph)
	 */
	@Override
	public Dimensions createSpeciesReferenceGlyphDimension(ReactionGlyph reactionGlyph, SpeciesReferenceGlyph specRefGlyph) {

		double width = 0;
		double height = 0;
		double depth = 0;

		Curve curve = specRefGlyph.getCurve();
		if (curve == null) {
			curve = createCurve(reactionGlyph, specRefGlyph);
		}

		for (CurveSegment curveSegment : curve.getListOfCurveSegments()) {
			LineSegment ls = (LineSegment) curveSegment;
			Point startPoint = ls.getStart();
			Point endPoint = ls.getEnd();
			double startX = startPoint.getX();
			double startY = startPoint.getY();
			double startZ = startPoint.getZ();
			double endX = endPoint.getX();
			double endY = endPoint.getY();
			double endZ = endPoint.getZ();

			if (curveSegment instanceof CubicBezier) {
				CubicBezier cb = (CubicBezier) curveSegment;
				Point basePoint1 = cb.getBasePoint1();
				Point basePoint2 = cb.getBasePoint2();

				double maxX = Math.max(startX, endX);
				maxX = Math.max(maxX, basePoint1.getX());
				maxX = Math.max(maxX, basePoint2.getX());
				double minX = Math.min(startX, endX);
				minX = Math.min(minX, basePoint1.getX());
				minX = Math.min(minX, basePoint2.getX());
				width = maxX - minX;

				double maxY = Math.max(startY, endY);
				maxY = Math.max(maxY, basePoint1.getY());
				maxY = Math.max(maxY, basePoint2.getY());
				double minY = Math.min(startY, endY);
				minY = Math.min(minY, basePoint1.getY());
				minY = Math.min(minY, basePoint2.getY());
				height = maxY - minY;

				double maxZ = Math.max(startZ, endZ);
				maxZ = Math.max(maxZ, basePoint1.getZ());
				maxZ = Math.max(maxZ, basePoint2.getZ());
				double minZ = Math.min(startZ, endZ);
				minZ = Math.min(minZ, basePoint1.getZ());
				minZ = Math.min(minZ, basePoint2.getZ());
				depth = maxZ - minZ;

			} else {

				if (startX == endX) {
					// line is vertical, with the width needed for the line
					// ending (10pt)
					width = 10;
				} else {
					double maxX = Math.max(startX, endX);
					double minX = Math.min(startX, endX);
					width = maxX - minX;
				}

				if (startY == endY) {
					// line is horizontal, with the height needed for the line ending (10pt)
					height = 10;
				} else {
					double maxY = Math.max(startY, endY);
					double minY = Math.min(startY, endY);
					height = maxY - minY;
				}

				if (startZ == endZ) {
					// line is horizontal, with the height needed for the line ending (10pt)
					depth = 10;
				} else {
					double maxZ = Math.max(startZ, endZ);
					double minZ = Math.min(startZ, endZ);
					depth = maxZ - minZ;
				}

			}
		}

		return new Dimensions(width, height, depth, layoutLevel, layoutVersion);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createTextGlyphDimension(TextGlyph textGlyph)
	 */
	@Override
	public Dimensions createTextGlyphDimension(TextGlyph textGlyph) {
		// width 0pt, height 0pt and depth 0pt
		// changes in width and height causes that the text will not be placed in the species field
		double textWidth = 0;
		double textHeight = 0;

		return new Dimensions(textWidth, textHeight, 0, layout.getLevel(), layout.getVersion());
	}


	/**
	 * Method to create the {@link Position} of a {@link TextGlyph}.
	 * @param textGlyph
	 * @return position
	 */
	private Point createTextGlyphPosition(TextGlyph textGlyph) {
		double textGlyphHeight = 10d;
		double textGlyphWidth = 30d;
		if (textGlyph.isSetBoundingBox() && textGlyph.getBoundingBox().isSetDimensions()) {
			Dimensions textGlyphDimension = textGlyph.getBoundingBox().getDimensions();
			textGlyphHeight = textGlyphDimension.getHeight();
			textGlyphWidth = textGlyphDimension.getWidth();
		}
		double x = 300d;
		double y = defaultTextGlyphYPosition;
		double z = 0d;


		if (textGlyph.isSetOriginOfText()) {

			NamedSBase originInstance = textGlyph.getOriginOfTextInstance();

			if ((originInstance instanceof SpeciesGlyph) || (originInstance instanceof Species)) {
				SpeciesGlyph speciesGlyph = null;
				if (originInstance instanceof Species) {
					if (textGlyph.isSetGraphicalObject()) {
						String glyphId = textGlyph.getGraphicalObject();
						ListOf<SpeciesGlyph> speciesGlyphList = layout.getListOfSpeciesGlyphs();
						speciesGlyph = speciesGlyphList.get(glyphId);
					}
				} else {
					speciesGlyph = (SpeciesGlyph)originInstance;
				}

				if ((speciesGlyph != null) && speciesGlyph.isSetBoundingBox() && speciesGlyph.getBoundingBox().isSetPosition()) {
					Point speciesGlyphPosition = calculateCenter(speciesGlyph);
					x = speciesGlyphPosition.getX();
					y = speciesGlyphPosition.getY();
					z = speciesGlyphPosition.getZ();
				}
			}

			if ((originInstance instanceof CompartmentGlyph) || (originInstance instanceof Compartment)) {
				CompartmentGlyph compartmentGlyph = null;
				if (originInstance instanceof Compartment) {
					if (textGlyph.isSetGraphicalObject()) {
						String glyphId = textGlyph.getGraphicalObject();
						ListOf<CompartmentGlyph> compartmentGlyphList = layout.getListOfCompartmentGlyphs();
						compartmentGlyph = compartmentGlyphList.get(glyphId);
					}
				} else {
					compartmentGlyph = (CompartmentGlyph)originInstance;
				}

				if ((compartmentGlyph != null) && compartmentGlyph.isSetBoundingBox()
						&& compartmentGlyph.getBoundingBox().isSetPosition()
						&& compartmentGlyph.getBoundingBox().isSetDimensions()) {
					// text glyph of compartment is set at the right top
					Point compGlyphPosition = compartmentGlyph.getBoundingBox().getPosition();
					double compGlyphWidth = compartmentGlyph.getBoundingBox().getDimensions().getWidth();
					x = compGlyphPosition.getX() + compGlyphWidth - (textGlyphWidth*3d);
					y = compGlyphPosition.getY() + (textGlyphHeight *2d);
					z = compGlyphPosition.getZ();
				}
			}

			if ((originInstance instanceof ReactionGlyph) || (originInstance instanceof Reaction)) {
				// there is no text for a reaction Glyph
			}
		}

		if ((x == 300d) && (y == defaultTextGlyphYPosition) && (z == 0d)) {
			defaultTextGlyphYPosition = defaultTextGlyphYPosition + textGlyphHeight;
		}

		return new Point(x, y, z, layoutLevel, layoutVersion);
	}

}
