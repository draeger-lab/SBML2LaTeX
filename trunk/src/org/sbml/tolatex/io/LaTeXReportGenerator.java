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
package org.sbml.tolatex.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.Assignment;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Creator;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.History;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Priority;
import org.sbml.jsbml.QuantityWithUnit;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBO.Term;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.StoichiometryMath;
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.Trigger;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.Variable;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.util.NotImplementedException;
import org.sbml.jsbml.util.StringTools;
import org.sbml.jsbml.util.filters.NameFilter;
import org.sbml.jsbml.validator.OverdeterminationValidator;
import org.sbml.tolatex.LaTeXOptions.PaperSize;
import org.sbml.tolatex.SBML2LaTeX;
import org.sbml.tolatex.util.LaTeX;
import org.sbml.totikz.TikZLayoutAlgorithm;
import org.sbml.totikz.TikZLayoutBuilder;

import cz.kebrt.html2latex.HTML2LaTeX;
import de.zbit.io.OpenFile;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.sbml.io.SBOTermFormatter;
import de.zbit.sbml.layout.LayoutAlgorithm;
import de.zbit.sbml.layout.LayoutDirector;
import de.zbit.text.LaTeXFormatter;
import de.zbit.text.TableColumn.Align;
import de.zbit.util.ResourceManager;

/**
 * This class generates LaTeX reports for given SBML files.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 2010-10-22
 * @version $Rev$
 */
@SuppressWarnings("deprecation")
public class LaTeXReportGenerator extends LaTeX implements SBMLReportGenerator {
	
	/**
	 * The location of the SBML2LaTeX logo file.
	 */
	private static String logo;
	
	/**
	 * A {@link Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(LaTeXReportGenerator.class.getName());
	
	/**
	 * Localization support.
	 */
	private static final ResourceBundle bundleContent = ResourceManager.getBundle("org.sbml.tolatex.locales.SBMLreport");
	
	/**
	 * At the moment only in English!
	 */
	private static final ResourceBundle bundleElements = ResourceManager.getBundle("de.zbit.sbml.locales.ElementNames", Locale.ENGLISH);
	/**
	 * 
	 */
	private static final ResourceBundle bundleUI = ResourceManager.getBundle("org.sbml.tolatex.locales.UI");
	
	/**
	 * A fancy symbol for saying yes. Requires the Zapf fonts in LaTeX.
	 */
	private static final String yes = "\\yes";
	
	/**
	 * A fancy symbol for saying no. Requires the Zapf fonts in LaTeX.
	 */
	private static final String no = "\\no";
	// \\ding{53}
	
	/**
	 * To parse notes in JSBML, complete HTML-code is requires. Thus, this string
	 * is used to start notes.
	 */
	private final static String notesStartString = "<notes><body xmlns=\"http://www.w3.org/1999/xhtml\">";
	
	/**
	 * To parse notes in JSBML, complete HTML-code is requires. Thus, this string
	 * is used to end notes.
	 */
	private final static String notesEndString = "</body></notes>";
	
	/* MiriamLink miriam = new MiriamLink(); */
	static {
		logo = (new File(System.getProperty("user.dir")
				+ "/resources/SBML2LaTeX.eps")).getAbsolutePath();
		logo = logo.substring(0, logo.lastIndexOf('.'));
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getLogoFile() {
		return logo;
	}
	
	/**
	 * This allows you to set the path of the logo file. It is more convenient to
	 * omit the file extension here so that LaTeX or PDFLaTeX can choose the
	 * desired file from the directory.
	 * 
	 * @param logoFilePath
	 *        Example: /home/user/logos/mylogo
	 */
	public static void setLogoFile(String logoFilePath) {
		logo = logoFilePath;
	}
	
	/**
	 * This converts HTML formation tags into associated LaTeX format
	 * assignments.
	 * 
	 * @param notes
	 * @return
	 * @throws IOException
	 */
	private static StringBuffer formatHTML(String notes) throws IOException {
		StringWriter st = new StringWriter();
		int start = notes.indexOf('>') + 1, end = notes.indexOf("</notes") - 1;
		notes = notes.substring(start, end);
		BufferedWriter bw = new BufferedWriter(st);
		BufferedReader br = new BufferedReader(new StringReader(notes));
		new HTML2LaTeX(br, bw);
		br.close();
		bw.close();
		StringBuffer sb = st.getBuffer();
		int index = sb.indexOf("\\begin{document}");
		if (index > -1) {
			sb.delete(0, index + 16);
		}
		index = sb.indexOf("\\end{document}");
		if (index > -1) {
			sb.delete(index, sb.length());
		}
		return sb;
	}
	
	/**
	 * Set of SBO Term used in the current SBML document to be translated. This
	 * set stores the SBO ids.
	 */
	private Set<Integer> sboTerms = new HashSet<Integer>();
	
	/**
	 * This is the font size to be used in this document. Allowed values are:
	 * <ul>
	 * <li>8</li>
	 * <li>9</li>
	 * <li>10</li>
	 * <li>11</li>
	 * <li>12</li>
	 * <li>14</li>
	 * <li>16</li>
	 * <li>17</li>
	 * </ul>
	 * Other values are set to the default of 11.
	 */
	private short fontSize;
	
	/**
	 * The font to be used for captions.
	 */
	private String fontHeadings = "helvet";
	
	/**
	 * The font to be used in the regular text and in the math mode.
	 */
	private String fontText = "mathptmx";
	
	/**
	 * The font to used for typewriter text.
	 */
	private String fontTypewriter = "courier";
	
	/**
	 * Allowed are
	 * <ul>
	 * <li>letter</li>
	 * <li>legal</li>
	 * <li>executive</li>
	 * <li>a* where * stands for values from 0 thru 9</li>
	 * <li>b*</li>
	 * <li>c*</li>
	 * <li>d*</li>
	 * </ul>
	 * The default is letter.
	 */
	private PaperSize paperSize;
	
	/**
	 * This variable is needed to decide whether a method should write a document
	 * head and tail for the LaTeX output.
	 */
	private boolean headTail;
	
	/**
	 * If {@code true} species (reactants, modifiers and products) in reaction equations
	 * will be displayed with their name if they have one. By default the ids of
	 * the species are used in these equations.
	 */
	private boolean printNameIfAvailable;
	
	/**
	 * If {@code true} this will produce LaTeX files for for entirely landscape documents
	 */
	private boolean landscape;
	
	/**
	 * If {@code true} ids are set in typewriter font (default).
	 */
	private boolean typewriter;
	
	/**
	 * If {@code true} predefined SBML units will be made explicitly if not overridden in
	 * the model.
	 */
	private boolean showPredefinedUnitDeclarations;
	
	/**
	 * If {@code true} a title page will be created by LaTeX for the resulting document.
	 * Otherwise there will only be a title on top of the first page.
	 */
	private boolean titlepage;
	
	/**
	 * If {@code true} a section of all errors found in the SBML file are printed at the
	 * end of the document
	 */
	private boolean checkConsistency = false;
	
	/**
	 * If {@code true} MIRIAM annotations are included into the model report. This process
	 * takes a bit time due to the necessary connection to EBI's web-service.
	 */
	private boolean includeMIRIAM = false;
  
  /**
   * These options determine which sections should be included when writing a
   * report. By default, these are all {@code true}.
   */
	private boolean includeUnitDefinitionsSection,
			includeCompartmentTypesSection, includeCompartmentsSection,
			includeSpeciesTypesSection, includeSpeciesSection,
			includeParametersSection, includeInitialAssignmentsSection,
			includeFunctionDefinitionsSection, includeRulesSection,
			includeEventsSection, includeConstraintsSection, includeReactionsSection,
			includeLayoutSection;
	
	/**
	 * @return the includeLayoutSection
	 */
	public boolean isIncludeLayoutSection() {
		return includeLayoutSection;
	}

	/**
	 * @param includeLayoutSection the includeLayoutSection to set
	 */
	public void setIncludeLayoutSection(boolean includeLayoutSection) {
		this.includeLayoutSection = includeLayoutSection;
	}

	/**
   * @return the includeSpeciesTypesSection
   */
  public boolean isIncludeSpeciesTypesSection() {
    return includeSpeciesTypesSection;
  }

  /**
   * @param includeSpeciesTypesSection the includeSpeciesTypesSection to set
   */
  public void setIncludeSpeciesTypesSection(boolean includeSpeciesTypesSection) {
    this.includeSpeciesTypesSection = includeSpeciesTypesSection;
  }

  /**
   * @return the includeUnitDefinitionsSection
   */
  public boolean isIncludeUnitDefinitionsSection() {
    return includeUnitDefinitionsSection;
  }

  /**
   * @param includeUnitDefinitionsSection the includeUnitDefinitionsSection to set
   */
  public void setIncludeUnitDefinitionsSection(
    boolean includeUnitDefinitionsSection) {
    this.includeUnitDefinitionsSection = includeUnitDefinitionsSection;
  }

  /**
   * @return the includeCompartmentTypesSection
   */
  public boolean isIncludeCompartmentTypesSection() {
    return includeCompartmentTypesSection;
  }

  /**
   * @param includeCompartmentTypesSection the includeCompartmentTypesSection to set
   */
  public void setIncludeCompartmentTypesSection(
    boolean includeCompartmentTypesSection) {
    this.includeCompartmentTypesSection = includeCompartmentTypesSection;
  }

  /**
   * @return the includeCompartmentsSection
   */
  public boolean isIncludeCompartmentsSection() {
    return includeCompartmentsSection;
  }

  /**
   * @param includeCompartmentsSection the includeCompartmentsSection to set
   */
  public void setIncludeCompartmentsSection(boolean includeCompartmentsSection) {
    this.includeCompartmentsSection = includeCompartmentsSection;
  }

  /**
   * @return the includeSpeciesSection
   */
  public boolean isIncludeSpeciesSection() {
    return includeSpeciesSection;
  }

  /**
   * @param includeSpeciesSection the includeSpeciesSection to set
   */
  public void setIncludeSpeciesSection(boolean includeSpeciesSection) {
    this.includeSpeciesSection = includeSpeciesSection;
  }

  /**
   * @return the includeParametersSection
   */
  public boolean isIncludeParametersSection() {
    return includeParametersSection;
  }

  /**
   * @param includeParametersSection the includeParametersSection to set
   */
  public void setIncludeParametersSection(boolean includeParametersSection) {
    this.includeParametersSection = includeParametersSection;
  }

  /**
   * @return the includeInitialAssignmentsSection
   */
  public boolean isIncludeInitialAssignmentsSection() {
    return includeInitialAssignmentsSection;
  }

  /**
   * @param includeInitialAssignmentsSection the includeInitialAssignmentsSection to set
   */
  public void setIncludeInitialAssignmentsSection(
    boolean includeInitialAssignmentsSection) {
    this.includeInitialAssignmentsSection = includeInitialAssignmentsSection;
  }

  /**
   * @return the includeFunctionDefinitionsSection
   */
  public boolean isIncludeFunctionDefinitionsSection() {
    return includeFunctionDefinitionsSection;
  }

  /**
   * @param includeFunctionDefinitionsSection the includeFunctionDefinitionsSection to set
   */
  public void setIncludeFunctionDefinitionsSection(
    boolean includeFunctionDefinitionsSection) {
    this.includeFunctionDefinitionsSection = includeFunctionDefinitionsSection;
  }

  /**
   * @return the includeRulesSection
   */
  public boolean isIncludeRulesSection() {
    return includeRulesSection;
  }

  /**
   * @param includeRulesSection the includeRulesSection to set
   */
  public void setIncludeRulesSection(boolean includeRulesSection) {
    this.includeRulesSection = includeRulesSection;
  }

  /**
   * @return the includeEventsSection
   */
  public boolean isIncludeEventsSection() {
    return includeEventsSection;
  }

  /**
   * @param includeEventsSection the includeEventsSection to set
   */
  public void setIncludeEventsSection(boolean includeEventsSection) {
    this.includeEventsSection = includeEventsSection;
  }

  /**
   * @return the includeConstraintsSection
   */
  public boolean isIncludeConstraintsSection() {
    return includeConstraintsSection;
  }

  /**
   * @param includeConstraintsSection the includeConstraintsSection to set
   */
  public void setIncludeConstraintsSection(boolean includeConstraintsSection) {
    this.includeConstraintsSection = includeConstraintsSection;
  }

  /**
   * @return the includeReactionsSection
   */
  public boolean isIncludeReactionsSection() {
    return includeReactionsSection;
  }

  /**
   * @param includeReactionsSection the includeReactionsSection to set
   */
  public void setIncludeReactionsSection(boolean includeReactionsSection) {
    this.includeReactionsSection = includeReactionsSection;
  }

  /**
	 * This switch allows to change the way how the reactants, modifiers and
	 * products are presented in each reaction. If {@code true} a table is created
	 * containing the identifiers of each reactant, modifier and product together
	 * with the respective name. If {@code false} (default), a subsection for each one of
	 * the three groups of participants is created giving all details of each
	 * participant.
	 */
	private boolean arrangeReactionParticipantsInOneTable = false,
			printFullODEsystem = false;
	
	private LaTeXFormatter formatter;
	
	/**
	 * 
	 */
	private OverdeterminationValidator validator;
	
	/**
	 * Constructs a new instance of LaTeX export. For each document to be
	 * translated a new instance has to be created. Here default values are used
	 * (A4 paper, 11pt, portrait, fancy headings, no titlepage).
	 */
	public LaTeXReportGenerator() {
		this(false, true, (short) 11, PaperSize.letter, true, false, false);
	}
	
	/**
	 * Constructs a new instance of LaTeX export. For each document to be
	 * translated a new instance has to be created. This constructor allows you to
	 * set many properties of the resulting LaTeX file.
	 * 
	 * @param landscape
	 *        If {@code true} the whole document will be set to landscape
	 *        format, otherwise portrait.
	 * @param typeWriter
	 *        If {@code true} ids are set in typewriter font (default).
	 *        Otherwise the regular font is used.
	 * @param fontSize
	 *        The size of the font to be used here. The default is 11. Allowed
	 *        values are 8, 9, 10, 11, 12, 14, 16 and 17.
	 * @param paperSize
	 *        Allowed are
	 *        <ul>
	 *        <li>letter</li>
	 *        <li>legal</li>
	 *        <li>executive</li>
	 *        <li>a* where * stands for values from 0 thru 9</li>
	 *        <li>b*</li>
	 *        <li>c*</li>
	 *        <li>d*</li>
	 *        </ul>
	 * @param addPredefinedUnits
	 *        If {@code true} predefined SBML units will be made explicitly if not
	 *        overridden in the model.
	 * @param titlepage
	 *        if {@code true} a title page will be created for the model report. Default
	 *        is {@code false} (just a caption).
	 */
	public LaTeXReportGenerator(boolean landscape, boolean typeWriter,
		short fontSize, PaperSize paperSize, boolean addPredefinedUnits,
		boolean titlepage, boolean printNameIfAvailable) {
		this.headTail = true;
		setLandscape(landscape);
		setTypewriter(typeWriter);
		setFontSize(fontSize);
		setPaperSize(paperSize);
		setShowPredefinedUnitDeclarations(addPredefinedUnits);
		setTitlepage(titlepage);
		setPrintNameIfAvailable(printNameIfAvailable);
		setIncludeCompartmentsSection(true);
		setIncludeCompartmentTypesSection(true);
		setIncludeConstraintsSection(true);
		setIncludeEventsSection(true);
		setIncludeFunctionDefinitionsSection(true);
		setIncludeInitialAssignmentsSection(true);
		setIncludeParametersSection(true);
		setIncludeReactionsSection(true);
		setIncludeRulesSection(true);
		setIncludeSpeciesSection(true);
		setIncludeSpeciesTypesSection(true);
		setIncludeUnitDefinitionsSection(true);
		setIncludeLayoutSection(true);
		this.formatter = new LaTeXFormatter();
		this.formatter.setUsingTypewriterFont(typeWriter);
	}
	
	/**
	 * For a given empty list of function identifiers this method performs DFS to
	 * detect all functions called by the expression hidden in the given ASTNode
	 * and returns the filled list of function identifiers. Every function called
	 * in this tree is added only one time to the list (in the order the functions
	 * are called).
	 * 
	 * @param ast
	 * @param funcIDs
	 * @return
	 */
	public List<String> callsFunctions(ASTNode ast) {
		List<String> funcIDs = new Vector<String>();
		if (ast.getType() == ASTNode.Type.FUNCTION) {
			if (!funcIDs.contains(ast.getName())) {
				funcIDs.add(ast.getName());
			}
		} else {
			int i, j;
			String elem;
			List<String> calls;
			for (i = 0; i < ast.getChildCount(); i++) {
				calls = callsFunctions(ast.getChild(i));
				for (j = 0; j < calls.size(); j++) {
					elem = calls.get(j);
					if (!funcIDs.contains(elem)) {
						funcIDs.add(elem);
					}
				}
			}
		}
		return funcIDs;
	}
	
	/**
	 * Returns {@code true} if the abstract syntax tree contains a node with the given
	 * name or id. To this end, the AST is traversed recursively.
	 * 
	 * @param math
	 * @param id
	 * @return
	 */
	public boolean contains(ASTNode math, String id) {
		if ((math.getType() == ASTNode.Type.NAME) && (math.getName().equals(id))) {
			return true;
		}
		for (int i = 0; i < math.getChildCount(); i++) {
			if (contains(math.getChild(i), id)) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.jcell2.client.io.DisplaySBML#format(org.sbml.libsbml.ListOf, java.lang.String, java.io.BufferedWriter, boolean)
	 */
	public void format(ListOf<? extends SBase> list, BufferedWriter buffer,
		boolean section) throws IOException, SBMLException {
		if (list.isEmpty()) { 
			return; 
		}
		int i;
		Model model = list.getModel();
		SBase first = list.getFirst();
		boolean compartments = first instanceof Compartment;
		boolean species = first instanceof Species;
		boolean reactions = first instanceof Reaction;
		boolean parameters = (first instanceof Parameter)
				|| (first instanceof LocalParameter);
		if (headTail) {
			documentHead(list.getSBMLDocument(), buffer);
		}
		if (list.size() > 0) {
			boolean setLandscape = false;
			if ((paperSize == PaperSize.letter) || (paperSize == PaperSize.legal)
					|| (paperSize == PaperSize.letter)) {
				setLandscape = true;
			} else {
				short size = Short.parseShort(Character.toString(paperSize.toString().charAt(1)));
				char variant = paperSize.toString().charAt(0);
				if ((size >= 4)
						&& ((variant == 'a') || (variant == 'b') || (variant == 'c') || (variant == 'd'))) {
					setLandscape = true;
				}
			}
			setLandscape = setLandscape && !landscape;
			String name = bundleElements.getString(list.getElementName());
			if (species || reactions) {
				if (setLandscape) {
					buffer.newLine();
					buffer.append(formatter.beginLandscape());
				}
				buffer.newLine();
			}
			
			if (section) {
				buffer.append(section(name, true));
				String text;
				if (compartments || species || reactions || parameters) {
					text = bundleContent.getString("INTRODUCTION_MODEL_COMPONENTS");
				} else {
					text = bundleContent.getString("INTRODUCTION_SUBCOMPONENTS");
				}
				buffer.append(
					MessageFormat.format(
						text,
						MessageFormat.format(bundleContent.getString("NUMERALS"), list.size()),
						bundleContent.getString(parameters ? "GLOBAL" : "WHITE_SPACE"),
						(list.size() > 1) ? name : bundleElements.getString(list.getFirst().getElementName())
				  )
				);
				buffer.newLine();
				if (species) {
					int specWithBound = model.getSpeciesWithBoundaryConditionCount();
					if (specWithBound > 0) {
						Integer speciesCount = Integer.valueOf(list.size());
						buffer.append(MessageFormat.format(
							bundleContent.getString("SPECIES_WITH_BOUNDARY_CONDITION_COUNT"),
							MessageFormat.format(bundleContent.getString("NUMERALS"), specWithBound),
							formatter.texttt(bundleContent.getString("TRUE")),
							speciesCount, speciesCount, speciesCount));
						buffer.newLine();
					}
					buffer.append(MessageFormat.format(
						bundleContent.getString("SPECIES_SECTION_ODE_REFERENCE"),
						formatter.protectedBlank(),
						formatter.ref("sec:DerivedRateEquations")));
					buffer.newLine();
				}
			}
			
			if (compartments) {
				buffer.append(longtableHead("lllC{2cm}llcl", 
					MessageFormat.format(
						bundleContent.getString("PROPERTIES_TABLE_CAPTION"),
						bundleElements.getString(first.getElementName())),
					bundleElements.getString("id"), bundleElements.getString("name"),
					"SBO",
					bundleElements.getString("spatialDimensions"),
					bundleElements.getString("size"),
					bundleElements.getString("unit"),
					bundleElements.getString("constant"),
					bundleElements.getString("outside")));
			} else if (species) {
				buffer.append(longtableHead(
					paperSize.equals("letter") || paperSize.equals("executive") ? "p{3.5cm}p{6cm}p{4.5cm}p{2.5cm}C{1.5cm}C{1.5cm}" : "p{3.5cm}p{6.5cm}p{5cm}p{3cm}C{1.5cm}C{1.5cm}",
					MessageFormat.format(
						bundleContent.getString("PROPERTIES_TABLE_CAPTION"),
						bundleElements.getString(first.getElementName())),
				  bundleElements.getString("id"),
				  bundleElements.getString("name"),
				  bundleElements.getString("compartment"),
				  bundleElements.getString("derivedUnit"),
				  bundleElements.getString("constant"),
				  bundleElements.getString("boundaryCondition")));
			} else if (reactions) {
				buffer.append(MessageFormat.format(
					bundleContent.getString("INTRODUCTION_SECTION_REACTIONS"),
					printNameIfAvailable ? MessageFormat.format(bundleContent.getString("NAMES_IF_AVAILABLE"), formatter.emdash()) : ""
				));
				buffer.newLine();
				buffer.append(longtableHead("rp{3cm}p{7cm}p{8cm}p{1.5cm}",
					MessageFormat.format(
						bundleContent.getString("OVERVIEW_TABLE_CAPTION"),
						bundleElements.getString(list.getElementName())),
					formatter.numero(),
					bundleElements.getString("id"),
					bundleElements.getString("name"),
					bundleContent.getString("REACTION_EQUATION"),
					"SBO"));
			} else if (parameters) {
				// TODO
				int preDecimal = 1, postDecimal = 1;
				double v;
				for (i = 0; i < list.size(); i++) {
					v = ((QuantityWithUnit) list.get(i)).getValue();
					if (!Double.isNaN(v)) {
						String[] value = Double.toString(v).split("\\.");
						if (value[0].length() > preDecimal) {
							preDecimal = value[0].length();
						}
						if (value[1].length() > postDecimal) {
							postDecimal = value[1].length();
							if (value[1].contains("E")) {
								postDecimal += 2;
							}
						}
					}
				}
				String head;
				if (paperSize.equals("executive")) {
					head = "p{2cm}p{3cm}cR{";
				} else {
					head = "p{2.5cm}p{3cm}cR{";
				}
				head += Integer.toString(preDecimal) + "}{"
						+ Integer.toString(Math.min(postDecimal, 3));
				head += (paperSize.equals("executive")) ? "}p{2.8cm}c" : "}p{3cm}c";
				buffer.append(longtableHead(
							head,
							MessageFormat.format(
								bundleContent.getString("PROPERTIES_TABLE_CAPTION"),
								bundleElements.getString(first.getElementName())),
							formatter.multicolumn(1, Align.left, bundleElements.getString("id")),
							bundleElements.getString("name"),
							"SBO",
							formatter.multicolumn(1, Align.center, bundleElements.getString("value")),
							bundleElements.getString("unit"),
							formatter.multicolumn(1, Align.center, bundleElements.getString("constant"))));
			}
			
			/*
			 * Iterate over all elements in the list and format them appropriately
			 */
			for (i = 0; i < list.size(); i++) {
				SBase s = list.get(i);
				if (section && !(compartments || species || reactions || parameters)) {
					buffer.append(subsection(firstLetterUpperCase(name) + ' ' + (i + 1),
						true));
					if (s instanceof Rule) {
						buffer.newLine();
						buffer.append(label("rule" + i));
					}
				}
				if (parameters) {
					appendIdOrDefault(buffer, s, " ");
					buffer.append('&');
					appendNameOrDefault(buffer, s, " ");
					buffer.append('&');
					if (s.isSetSBOTerm()) {
						buffer.append(SBO.sboNumberString(s.getSBOTerm()));
						sboTerms.add(Integer.valueOf(s.getSBOTerm()));
					}
					buffer.append('&');
					QuantityWithUnit p = (QuantityWithUnit) s;
					double v = p.getValue();
					if (Double.isNaN(v)) {
						buffer.append(formatter.multicolumn(1, Align.right, "NaN"));
					} else {
						String value = Double.toString(v);
						buffer.append(value.contains("E") ? formatter.multicolumn(1, Align.right, format(p.getValue())) : value);
					}
					buffer.append('&');
					UnitDefinition ud = p.getDerivedUnitDefinition();
					if ((ud == null) || (ud.getUnitCount() == 0)) {
						if (p.isSetUnits()) {
							if ((ud = p.getModel().getUnitDefinition(p.getUnits())) != null) {
								buffer.append(math(format(ud)));
							} else if (Unit.isPredefined(p.getUnits(), p.getLevel())) {
								Unit u = new Unit(p.getLevel(), p.getVersion());
								u.setKind(Unit.Kind.valueOf(p.getUnits()));
								buffer.append(math(format(u)));
							} else {
								buffer.append(texttt(maskSpecialChars(p.getUnits())));
							}
						} else {
							buffer.append(' ');
						}
					} else {
						buffer.append(math(format(ud)));
					}
					buffer.append('&');
					if (p instanceof LocalParameter) {
						buffer.append(yes);
					} else {
						buffer.append(((Symbol) p).getConstant() ? yes : no);
					}
					buffer.append(lineBreak);
				} else if (compartments) {
					Compartment c = (Compartment) s;
					if (c.isSetId()) {
						buffer.append(texttt(maskSpecialChars(c.getId())));
					}
					buffer.append('&');
					buffer.append(maskSpecialChars(c.getName()));
					buffer.append('&');
					if (c.isSetSBOTerm()) {
						buffer.append(SBO.sboNumberString(c.getSBOTerm()));
						sboTerms.add(Integer.valueOf(c.getSBOTerm()));
					}
					buffer.append('&');
					buffer.append(StringTools.toString(Locale.ENGLISH,
						c.getSpatialDimensions()));
					buffer.append('&');
					buffer.append(format(c.getSize()));
					buffer.append('&');
					UnitDefinition ud;
					if (c.isSetUnits())
						ud = c.getModel().getUnitDefinition(c.getUnits());
					else ud = c.getDerivedUnitDefinition();
					if ((ud == null) || (ud.getUnitCount() == 0)) {
						buffer.append(' ');
					} else if (ud.isVariantOfVolume() && (ud.getUnitCount() == 1)
							&& (c.getSize() == 1.0) && (ud.getUnit(0).isLitre()))
						buffer.append("litre");
					else buffer.append(math(format(ud)));
					buffer.append('&');
					buffer.append(c.getConstant() ? yes : no);
					buffer.append('&');
					buffer.append(texttt(maskSpecialChars(c.getOutside())));
					buffer.append(lineBreak);
				} else if (species) {
					Species spec = (Species) s;
					String mask = maskSpecialChars(spec.getId());
					buffer.append(texttt(mask));
					buffer.append('&');
					buffer.append(maskSpecialChars(spec.getName()));
					buffer.append('&');
					buffer.append(texttt(maskSpecialChars(spec.getCompartment())));
					buffer.append('&');
					buffer.append(math(format(spec.getDerivedUnitDefinition())));
					buffer.append('&');
					buffer.append(spec.getConstant() ? yes : no);
					buffer.append('&');
					buffer.append(spec.getBoundaryCondition() ? yes : no);
					buffer.append(lineBreak);
				} else if (reactions) {
					buffer.append(Integer.toString(i + 1));
					buffer.append('&');
					Reaction r = (Reaction) list.get(i);
					// buffer.append("\\hyperref[v");
					// buffer.append(Integer.toString(i + 1));
					// buffer.append("]{");
					buffer.append(texttt(maskSpecialChars(r.getId())));
					// buffer.append("}&");
					buffer.append('&');
					buffer.append(r.isSetName() ? maskSpecialChars(r.getName()) : " ");
					buffer.append("&\\ce{");
					buffer.append(reactionEquation(r));
					buffer.append("}&");
					if (r.isSetSBOTerm()) {
						buffer.append(SBO.sboNumberString(r.getSBOTerm()));
						sboTerms.add(Integer.valueOf(r.getSBOTerm()));
					}
					buffer.append(lineBreak);
				} else if (s instanceof Constraint) {
					Constraint c = (Constraint) s;
					buffer.append(descriptionBegin);
					format(c, buffer, true);
					buffer.append(descriptionItem(bundleElements.getString("message"),
						formatHTML(c.getMessageString()).toString()));
					buffer.append(descriptionItem(bundleContent.getString("EQUATION"),
							equation(new StringBuffer(c.getMath().toLaTeX()))));
					buffer.append(descriptionEnd);
				} else if (s instanceof FunctionDefinition) {
					format((FunctionDefinition) s, buffer);
				} else if (s instanceof InitialAssignment) {
					format((InitialAssignment) s, buffer);
				} else if (s instanceof Rule) {
					format((Rule) s, buffer);
				} else if ((s instanceof SpeciesType) || (s instanceof CompartmentType)) {
					NamedSBase nsb = (NamedSBase) s;
					boolean isSpecType = s instanceof SpeciesType;
					StringBuffer sb = new StringBuffer();
					int j, counter = 0;
					if (isSpecType) {
						for (j = 0; j < s.getModel().getSpeciesCount(); j++) {
							Species spec = s.getModel().getSpecies(j);
							if (spec.isSetSpeciesType()
									&& spec.getSpeciesType().equals(nsb.getId())) {
								sb.append(texttt(maskSpecialChars(spec.getId())));
								sb.append('&');
								if (spec.isSetName()) {
									sb.append(maskSpecialChars(spec.getName()));
								}
								sb.append(lineBreak);
								counter++;
							}
						}
					} else for (j = 0; j < s.getModel().getCompartmentCount(); j++) {
						Compartment c = s.getModel().getCompartment(j);
						if (c.isSetCompartmentType()
								&& c.getCompartmentType().equals(nsb.getId())) {
							sb.append(texttt(maskSpecialChars(c.getId())));
							sb.append('&');
							if (c.isSetName()) {
								sb.append(maskSpecialChars(c.getName()));
							}
							sb.append(lineBreak);
							counter++;
						}
					}
					format(s, buffer, false);
					if (counter == 0) {
						buffer.append(MessageFormat.format(
								bundleContent.getString("MODEL_DOES_NOT_CONTAIN_ELEMENTS"),
								MessageFormat.format(isSpecType ? "GRAMMATICAL_NUMBER_SPECIES" : "GRAMMATICAL_NUMBER_COMPARTMENTS", counter)));
						buffer.append(bundleContent.getString("WHITE_SPACE"));
					} else {
						buffer.append(longtableHead("ll",
							MessageFormat.format(
									bundleContent.getString("ELEMENTS_OF_THIS_TYPE"),
									MessageFormat.format(isSpecType ? 
											"GRAMMATICAL_NUMBER_SPECIES" : 
											"GRAMMATICAL_NUMBER_COMPARTMENTS", counter)
							), 
							bundleElements.getString("id"), bundleElements.getString("name")));
						buffer.append(sb);
						buffer.append(bottomrule);
					}
				} else {
					format(s, buffer, false);
				}
			}
			if (parameters || compartments || species || reactions) {
				buffer.append(bottomrule);
				if ((species || reactions) && setLandscape) {
					buffer.append("\\end{landscape}");
					buffer.newLine();
				}
			}
			buffer.newLine();
		}
		if (reactions) {
			formatReactions(list, buffer);
		}
		if (compartments) {
			formatCompartments(list, buffer);
		}
		if (headTail) {
			documentFoot(list, buffer);
		}
	}
	
	/**
	 * @param buffer
	 * @param s
	 * @param def
	 * @throws IOException
	 */
	private void appendNameOrDefault(BufferedWriter buffer, SBase s, String def)
		throws IOException {
		if (s instanceof NamedSBase) {
			NamedSBase nsb = (NamedSBase) s;
			buffer.append(maskSpecialChars(nsb.isSetName() ? nsb.getName() : def));
		}
		buffer.append(def);
	}
	
	/**
	 * @param buffer
	 * @param s
	 * @param def
	 * @throws IOException
	 */
	private void appendIdOrDefault(BufferedWriter buffer, SBase s, String def)
		throws IOException {
		if (s instanceof NamedSBase) {
			NamedSBase nsb = (NamedSBase) s;
			buffer.append(texttt(maskSpecialChars(nsb.getId())));
		} else {
			buffer.append(def);
		}
	}
	
	/**
	 * @throws SBMLException
	 */
	private void formatEvents(ListOf<? extends Event> eventList,
		BufferedWriter buffer) throws IOException, SBMLException {
		if (headTail) {
			documentHead(eventList.getSBMLDocument(), buffer);
		}
		if (eventList.size() > 0) {
			int i, j;
			buffer.append(section(MessageFormat.format(
					bundleContent.getString("GRAMMATICAL_NUMBER_EVENT"), 
					eventList.size()), true));
			buffer.append(
				MessageFormat.format(bundleContent.getString("INTRODUCTION_SUBCOMPONENTS"),
					MessageFormat.format(bundleContent.getString("NUMERALS"), eventList.size()),
					bundleContent.getString("WHITE_SPACE"),
					bundleElements.getString(eventList.size() == 1 ? eventList.getFirst().getElementName() : eventList.getElementName())
				)
			);
			buffer.append(bundleContent.getString("WHITE_SPACE"));
			buffer.append(MessageFormat.format(
					bundleContent.getString("EVENT_INTRODUCTION"),
					texttt(bundleContent.getString("FALSE")),
					texttt(bundleContent.getString("TRUE"))));
			buffer.append(bundleContent.getString("WHITE_SPACE"));
			buffer.append(bundleContent.getString("DELAY_FUNCTION_DESCRIPTION"));
			buffer.newLine();
			
			Event ev;
			String var;
			for (i = 0; i < eventList.size(); i++) {
				ev = (Event) eventList.get(i);
				subsection(ev, i, buffer);
				buffer.append(descriptionBegin);
				format(ev, buffer, true);
				Trigger trigger = ev.getTrigger();
				buffer.append(descriptionItem(bundleElements.getString(trigger.getElementName()), format(trigger)));
				if (ev.isSetPriority()) {
					Priority priority = ev.getPriority();
					buffer.append(descriptionItem(bundleElements.getString(priority.getElementName()), format(priority)));
				}
				if (ev.isSetDelay()) {
					Delay delay = ev.getDelay();
					buffer.append(descriptionItem(bundleElements.getString(delay.getElementName()), equation(delay.getMath().toLaTeX())));
					UnitDefinition ud = ev.getDelay().getDerivedUnitDefinition();
					if ((ud != null) && (ud.getUnitCount() > 0)) {
						buffer.append(descriptionItem(
							bundleContent.getString("DELAY_FUNCTION_TIME_UNITS"),
							math(format(ud))));
					}
				}
				StringBuffer description = new StringBuffer();
				description.append(MessageFormat.format(
						bundleContent.getString(ev.getUseValuesFromTriggerTime() ? 
								"EVENT_DOES_USE_VALUES_FROM_TRIGGER_TIME" : 
								"EVENT_DOES_NOT_USE_VALUES_FROM_TRIGGER_TIME"),
						ev.getEventAssignmentCount(), ev.isSetDelay() ? 1 : 0));
				if (ev.getEventAssignmentCount() > 1) {
					description.append(newLine());
					description.append("\\begin{align}");
				} else {
					description.append(eqBegin);
				}
				for (j = 0; j < ev.getEventAssignmentCount(); j++) {
					var = ev.getEventAssignment(j).getVariable();
					Model model = ev.getModel();
					if (model.getSpecies(var) != null) {
						Species species = model.getSpecies(var);
						if (species.getHasOnlySubstanceUnits()) {
							description.append('[');
						}
						description.append(mathtt(maskSpecialChars(model.getSpecies(var)
								.getId())));
						if (species.getHasOnlySubstanceUnits()) {
							description.append(']');
						}
					} else if (model.getCompartment(var) != null) {
						description.append(getSize(model.getCompartment(var)));
					} else {
						description.append(mathtt(maskSpecialChars(var)));
					}
					description.append((ev.getEventAssignmentCount() > 1) ? " =& " : " = ");
					description.append(ev.getEventAssignment(j).getMath().toLaTeX());
					if (j < ev.getEventAssignmentCount() - 1) {
						description.append(lineBreak);
					}
				}
				if (ev.getEventAssignmentCount() == 1) {
					description.append(eqEnd);
				} else {
					description.append("\\end{align}");
					description.append(newLine());
				}
				buffer.append(descriptionItem(
					MessageFormat.format(
						bundleContent.getString("GRAMMATICAL_NUMBER_ASSIGNMENT"),
						ev.getEventAssignmentCount()),
					description));
				buffer.append(descriptionEnd);
			}
		}
		buffer.newLine();
		if (headTail) {
			documentFoot(eventList, buffer);
		}
	}
	
	/**
	 * 
	 * @param nsb
	 * @param buffer
	 * @throws IOException 
	 */
	private void subsection(NamedSBase nsb, int index, Writer buffer) throws IOException {
		String elementName = nsb.getElementName();
		if (nsb.isSetId()) {
			buffer.append(subsection(MessageFormat.format(
				bundleContent.getString("LABELED_ELEMENT"),
				bundleElements.getString(elementName),
				texttt(maskSpecialChars(nsb.getId()))), true));
			buffer.append(label(elementName.toLowerCase() + nsb.getId()));
		} else {
			buffer.append(subsection(MessageFormat.format(
				bundleContent.getString("ELEMENT_WITHOUT_IDENTIFIER"),
				bundleElements.getString(elementName)), true));
			buffer.append(label(elementName.toLowerCase() + index));
		}
		buffer.append(newLine());
	}

	/**
	 * 
	 * @param priority
	 * @return
	 * @throws SBMLException
	 */
	private String format(Priority priority) throws SBMLException {
		StringBuilder sb = new StringBuilder();
		sb.append(bundleContent.getString("PRIORITY_INTRODUCTION"));
		sb.append(newLine());
		sb.append(equation(priority.getMath().toLaTeX()));
		sb.append(newLine());
		return sb.toString();
	}
	
	/**
	 * 
	 * @param trigger
	 * @return
	 * @throws SBMLException
	 */
	private String format(Trigger trigger) throws SBMLException {
		StringBuilder sb = new StringBuilder();
		if (trigger.isSetInitialValue()) {
			sb.append(MessageFormat.format(
				bundleContent.getString("TRIGGER_DESCRIPTION"),
				trigger.isInitialValue() ? 1 : 0,
				texttt(bundleContent.getString("TRUE")),
				math("t = 0")));
			sb.append(newLine());
		}
		if (trigger.isSetPersistent()) {
			sb.append(MessageFormat.format(
				bundleContent.getString("TRIGGER_PERSISTENT_DESCRIPTION"),
				texttt(bundleContent.getString("FALSE")),
				trigger.isPersistent() ? 1 : 0));
			sb.append(newLine());
		}
		sb.append(bundleContent.getString("TRIGGER_CONDITION"));
		sb.append(equation(trigger.getMath().toLaTeX()));
		sb.append(newLine());
		return sb.toString();
	}
	
	/**
	 * Creates a readable format of all unit definitions within the given list.
	 * 
	 * @param listOfUnits
	 * @param buffer
	 * @throws IOException
	 */
	private void formatUnitDefinitions(ListOf<? extends SBase> listOfUnits,
		BufferedWriter buffer) throws IOException {
		if (headTail) {
			documentHead(listOfUnits.getSBMLDocument(), buffer);
		}
		List<String> defaults = new Vector<String>();
		UnitDefinition def;
		Model m = listOfUnits.getModel();
		ListOf<UnitDefinition> lud = new ListOf<UnitDefinition>(m.getLevel(), m.getVersion());
		lud.setSBaseListType(UnitDefinition.class);
		for (int i = 0; i < listOfUnits.size(); i++) {
			lud.add((UnitDefinition) listOfUnits.get(i).clone());
		}
		if (showPredefinedUnitDeclarations && (m.getLevel() < 3)) {
			String notes = " is the predefined SBML unit for <tt>";
			if (lud.firstHit(new NameFilter("substance")) == null) {
				def = m.getPredefinedUnitDefinition("substance").clone();
				def.setNotes(notesStartString + "Mole" + notes + def.getId() + "</tt>."
						+ notesEndString);
				lud.append(def);
				defaults.add(def.getId());
			}
			if (lud.firstHit(new NameFilter("volume")) == null) {
				def = m.getPredefinedUnitDefinition("volume");
				def.setNotes(notesStartString + "Litre" + notes + def.getId()
						+ "</tt>." + notesEndString);
				lud.append(def);
				defaults.add(def.getId());
			}
			if ((lud.firstHit(new NameFilter("area")) == null)
					&& (lud.getLevel() > 1)) {
				def = m.getPredefinedUnitDefinition("area");
				def.setNotes(notesStartString + "Square metre" + notes + def.getId()
						+ "</tt> since SBML Level 2 Version 1." + notesEndString);
				lud.append(def);
				defaults.add(def.getId());
			}
			if ((lud.firstHit(new NameFilter("length")) == null)
					&& (lud.getLevel() > 1)) {
				def = m.getPredefinedUnitDefinition("length");
				def.setNotes(notesStartString + "Metre" + notes + def.getId()
						+ "</tt> since SBML Level 2 Version 1." + notesEndString);
				lud.append(def);
				defaults.add(def.getId());
			}
			if (lud.firstHit(new NameFilter("time")) == null) {
				def = m.getPredefinedUnitDefinition("time");
				def.setNotes(notesStartString + "Second" + notes + def.getId()
						+ "</tt>." + notesEndString);
				lud.append(def);
				defaults.add(def.getId());
			}
		}
		if (0 < lud.size()) {
			buffer.append(section(bundleElements.getString(lud.getElementName()), true));
			buffer.append(MessageFormat.format(
				bundleContent.getString("INTRODUCTION_SUBCOMPONENTS"),
				MessageFormat.format(bundleContent.getString("NUMERALS"), lud.size()),
				bundleContent.getString("WHITE_SPACE"),
				bundleElements.getString((lud.size() > 1) ? lud.getElementName() : lud.getFirst().getElementName())
			));
			buffer.newLine();
			if (0 < defaults.size()) {
				if (defaults.size() < lud.size()) {
					List<String> defaultsList = new ArrayList<String>(defaults.size());
					for (String t : defaults) {
						defaultsList.add(texttt(t).toString());
					}
					buffer.append(MessageFormat.format(bundleContent.getString("THE_ELEMENT"),
							MessageFormat.format(bundleContent.getString("GRAMMATICAL_NUMBER_UNITS"), defaults.size())));
					buffer.append(bundleContent.getString("WHITE_SPACE"));
					buffer.append(format(defaultsList));
					buffer.append(MessageFormat.format(bundleContent.getString("CONJUGATION_PRESENT_INDICATIVE_3RD_PERSON_OF_BE"), defaults.size()));
				} else {
					buffer.append("PREDEFINED_UNITS_ALL");
				}
				buffer.append(bundleContent.getString("PREDEFINED_UNITS"));
			}
			for (int i = 0; i < lud.size(); i++) {
				def = (UnitDefinition) lud.get(i);
				subsection(def, i, buffer);
				buffer.append(descriptionBegin);
				format(def, buffer, true);
				if (def.getUnitCount() > 0) {
					buffer.append(descriptionItem(bundleContent.getString("DEFINITION"), math(format(def))));
				}
				buffer.append(descriptionEnd);
			}
		}
		if (headTail) {
			documentFoot(lud, buffer);
		}
	}
	
	/**
	 * 
	 * @param list
	 * @param itemFormatPattern
	 * @return
	 */
	private <T> String format(List<T> list) {
		StringBuilder sb = new StringBuilder();
		assert list != null;
		int i = 0;
		for (T item : list) {
			if ((0 < i) && (i < list.size() - 1)) {
				sb.append(bundleContent.getString("SERIES_SEPARATOR"));
			} else if (i == list.size() - 1) {
				sb.append(MessageFormat.format(bundleContent.getString("SERIAL_AND"), list.size()));
			}
			sb.append(item.toString());
			i++;
		}
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.jcell.client.io.DisplaySBML#format(org.sbml.libsbml.Model, java.io.BufferedWriter)
	 */
	public void format(Model model, BufferedWriter buffer) throws IOException,
		SBMLException {
		if (headTail) {
			documentHead(model.getSBMLDocument(), buffer);
			// buffer.append("\\tableofcontents");
			buffer.newLine();
			buffer.append(section(bundleContent.getString("GENERAL_OVERVIEW"), true));
		}
		if (model.isSetSBOTerm()) {
			sboTerms.add(Integer.valueOf(model.getSBOTerm()));
			String sboModelName = maskSpecialChars(correctQuotationMarks(
				SBO.getTerm(model.getSBOTerm()).getName(), leftQuotationMark, rightQuotationMark));
			buffer.append(MessageFormat.format(
				bundleContent.getString("SBO_CONCEPT_OF_MODEL"),
				indefiniteArticle(sboModelName.charAt(0)),
				sboModelName,
				SBO.sboNumberString(model.getSBOTerm()),
				formatter.protectedBlank(),
				formatter.ref("sec:glossary")
			));
			buffer.newLine();
		}
		
		validator = new OverdeterminationValidator(model);
		
		// buffer.append(subsection("Model History", false));
		formatHistory(model, buffer);
		
		buffer.append(MessageFormat.format(
			bundleContent.getString("INTRODUCTION_MODEL_OVERVIEW"),
			formatter.protectedBlank(),
			formatter.ref("tab:components"),
			bundleContent.getString("WHITE_SPACE"))
		);
		buffer.newLine();
		buffer.append("\\begin{table}[h!]");
		buffer.newLine();
		buffer.append("\\centering");
		buffer.newLine();
		buffer.append(formatter.caption(bundleContent.getString("SBML_COMPONENTS_OF_THIS_MODEL")));
		buffer.append(label("tab:components"));
		buffer.newLine();
		buffer.append(bundleContent.getString("SBML_COMPONENT_TABLE_DESCRIPTION"));
		buffer.newLine();
		// buffer.append("\\begin{tabular}{C{2cm}ccC{2cm}ccccC{2cm}}");
		buffer.append("\\begin{tabular}{l|r||l|r}");
		buffer.append(toprule);
		buffer.append(formatter.multicolumn(1, Align.center, "Element") + "&\\multicolumn{1}{|c||}{Quantity}&");
		buffer.append(formatter.multicolumn(1, Align.center, "Element", false, true) + '&' + formatter.multicolumn(1, Align.center, "Quantity"));
		buffer.append(lineBreak);
		buffer.append(midrule);
		buffer.append("compartment types&");
		buffer.append(Integer.toString(model.getCompartmentTypeCount()));
		buffer.append("&compartments&");
		buffer.append(Integer.toString(model.getCompartmentCount()));
		buffer.append(lineBreak);
		buffer.append("species types&");
		buffer.append(Integer.toString(model.getSpeciesTypeCount()));
		buffer.append("&species&");
		buffer.append(Integer.toString(model.getSpeciesCount()));
		buffer.append(lineBreak);
		buffer.append("events&");
		buffer.append(Integer.toString(model.getEventCount()));
		buffer.append("&constraints&");
		buffer.append(Integer.toString(model.getConstraintCount()));
		buffer.append(lineBreak);
		buffer.append("reactions&");
		buffer.append(Integer.toString(model.getReactionCount()));
		buffer.append("&function definitions&");
		buffer.append(Integer.toString(model.getFunctionDefinitionCount()));
		buffer.append(lineBreak);
		buffer.append("global parameters&");
		buffer.append(Integer.toString(model.getParameterCount()));
		buffer.append("&unit definitions&");
		buffer.append(Integer.toString(model.getUnitDefinitionCount()));
		buffer.append(lineBreak);
		buffer.append("rules&");
		buffer.append(Integer.toString(model.getRuleCount()));
		buffer.append("&initial assignments&");
		buffer.append(Integer.toString(model.getInitialAssignmentCount()));
		buffer.append(lineBreak);
		buffer.append("\\bottomrule\\end{tabular}");
		buffer.append(lineBreak);
		buffer.append("\\end{table}");
		buffer.append(lineBreak);
		
		if (model.isSetNotes()) {
			buffer.append(subsection(MessageFormat.format(
				bundleContent.getString("ELEMENT_NOTES"), 
				bundleElements.getString(model.getElementName())), false));
			buffer.append(formatHTML(model.getNotesString()));
			buffer.newLine();
		}
		
		if ((model.getCVTermCount() > 0) && (includeMIRIAM)) {
			buffer.append(subsection(MessageFormat.format(
				bundleContent.getString("ELEMENT_ANNOTATION"), 
				bundleElements.getString(model.getElementName())), false));
			buffer.append(bundleContent.getString("MODEL_RESOURCES"));
			buffer.newLine();
			buffer.newLine();
			for (int i = 0; i < model.getCVTermCount(); i++) {
				format(model.getCVTerm(i), buffer);
			}
		}
		
		/*
		 * Create content of the report.
		 */
    if (includeUnitDefinitionsSection) {
      formatUnitDefinitions(model.getListOfUnitDefinitions(), buffer);
    }
    if (includeCompartmentTypesSection) {
      format(model.getListOfCompartmentTypes(), buffer, true);
    }
    if (includeCompartmentsSection) {
      format(model.getListOfCompartments(), buffer, true);
    }
    if (includeSpeciesTypesSection) {
      format(model.getListOfSpeciesTypes(), buffer, true);
    }
    if (includeSpeciesSection) {
      format(model.getListOfSpecies(), buffer, true);
    }
    if (includeParametersSection) {
      format(model.getListOfParameters(), buffer, true);
    }
    if (includeInitialAssignmentsSection) {
      format(model.getListOfInitialAssignments(), buffer, true);
    }
    if (includeFunctionDefinitionsSection) {
      format(model.getListOfFunctionDefinitions(), buffer, true);
    }
    if (includeRulesSection) {
      format(model.getListOfRules(), buffer, true);
    }
    if (includeEventsSection) {
      formatEvents(model.getListOfEvents(), buffer);
    }
    if (includeConstraintsSection) {
      format(model.getListOfConstraints(), buffer, true);
    }
    if (includeReactionsSection) {
      format(model.getListOfReactions(), buffer, true);
    }
    
    /*
     * Extension packages
     */
    if (includeLayoutSection) {
    	LayoutModelPlugin layoutPlugin = (LayoutModelPlugin) model.getExtension(
    		LayoutConstants.getNamespaceURI(model.getLevel(), model.getVersion()));
    	if (layoutPlugin != null) {
    		LayoutDirector<BufferedWriter> director;
    		LayoutAlgorithm layoutAlgorithm = new TikZLayoutAlgorithm();
    		for (int i = 0; i < layoutPlugin.getLayoutCount(); i++) {
    			buffer.append("\\begin{figure}\n\\centering\n");
    			Layout layout = layoutPlugin.getLayout(i); 
    			director = new LayoutDirector<BufferedWriter>(
    					layout,
    					new TikZLayoutBuilder<BufferedWriter>(buffer, false),
    					layoutAlgorithm);
    			director.run();
    			buffer.newLine();
    			buffer.append(formatter.caption(getNameOrID(layout, false).toString()));
    			buffer.append(formatter.label(layout.isSetId() ? layout.getId() : "layout" + i));
    			buffer.append("\\end{figure}\n");
    		}
    	}
    }
    
		if (headTail) {
			documentFoot(model.getSBMLDocument(), buffer);
		}
	}
	
	/**
	 * 
	 * @param charAtStart
	 * @return
	 */
	private String indefiniteArticle(char charAtStart) {
		return bundleContent.getString(isVowel(charAtStart) ? "INDEFINITE_ARTICLE" : "INDEFINITE_ARTICLE_FOLLOWED_BY_VOWEL");
	}

	/**
	 * @param sbase
	 * @param buffer
	 * @throws IOException
	 */
	private void formatHistory(SBase sbase, BufferedWriter buffer)
		throws IOException {
		if (!sbase.isSetHistory()) { 
			return; 
		}
		History history = sbase.getHistory();
		if ((history.getCreatorCount() > 0) || (history.isSetCreatedDate())) {
			buffer.append(MessageFormat.format("This {0} was ", bundleElements.getString(sbase.getElementName())));
			if (history.getCreatorCount() > 0) {
				buffer.append("created by ");
				if (history.getCreatorCount() > 1) {
					buffer.append("the following ");
					buffer.append(MessageFormat.format(bundleContent.getString("NUMERALS"), history.getCreatorCount()));
					buffer.append(" authors: ");
				}
				List<String> creatorList = new ArrayList<String>(history.getCreatorCount());
				for (int i = 0; i < history.getCreatorCount(); i++) {
					creatorList.add(format(history.getCreator(i)));
				}
				buffer.append(format(creatorList));
				buffer.newLine();
			}
			if (history.isSetCreatedDate()) {
				buffer.append("at ");
				format(history.getCreatedDate(), buffer);
				if (history.isSetModifiedDate()) {
					buffer.append(bundleContent.getString("CONJUNCTION_AND"));
				}
			}
		}
		if (history.isSetModifiedDate()) {
			buffer.append(" last modified at ");
			format(history.getModifiedDate(), buffer);
		}
		if ((history.getCreatorCount() > 0)
				|| history.isSetCreatedDate() || history.isSetModifiedDate()) {
			buffer.append('.');
		}
		buffer.newLine();
		buffer.newLine();
	}
	
	/* (non-Javadoc)
	 * @see org.jcell.client.io.DisplaySBML#format(org.sbml.libsbml.SBMLDocument, java.io.BufferedWriter)
	 */
	public void format(SBMLDocument doc, BufferedWriter buffer)
		throws IOException, SBMLException {
		/*
		 * writing latex head
		 */
		headTail = false;
		documentHead(doc, buffer);
		// buffer.append("\\tableofcontents");
		buffer.newLine();
		
		/*
		 * Overview
		 */
		buffer.append(section(bundleContent.getString("GENERAL_OVERVIEW"), true));
		buffer.append(MessageFormat.format(
			bundleContent.getString("SBML_DOCUMENT_INTRODUCTION"),
			Integer.valueOf(doc.getLevel()),
			Integer.valueOf(doc.getVersion())));
		buffer.newLine();
		format(doc, buffer, false);
				
		/*
		 * The model: append model description
		 */
		if (doc.getModel() != null) {
			format(doc.getModel(), buffer);
		}
		
		documentFoot(doc, buffer);
		headTail = true;
	}
	
	/**
	 * Returns a unit.
	 * 
	 * @param u
	 * @return
	 */
	public StringBuffer format(Unit u) {
		StringBuffer buffer = new StringBuffer();
		boolean standardScale = (u.getScale() == 24) || (u.getScale() == 21)
				|| (u.getScale() == 18) || (u.getScale() == 12) || (u.getScale() == 9)
				|| (u.getScale() == 6) || (u.getScale() == 3) || (u.getScale() == 2)
				|| (u.getScale() == 1) || (u.getScale() == 0) || (u.getScale() == -1)
				|| (u.getScale() == -2) || (u.getScale() == -3) || (u.getScale() == -6)
				|| (u.getScale() == -9) || (u.getScale() == -12)
				|| (u.getScale() == -15) || (u.getScale() == -18)
				|| (u.getScale() == -21) || (u.getScale() == -24);
		if (u.getOffset() != 0d) {
			buffer.append(format(u.getOffset()).toString().replaceAll("\\$", ""));
			if ((u.getMultiplier() != 0) || (!standardScale)) {
				buffer.append('+');
			}
		}
		if (u.getMultiplier() != 1d) {
			if (u.getMultiplier() == -1d) {
				buffer.append('-');
			} else {
				buffer.append(format(u.getMultiplier()).toString()
						.replaceAll("\\$", ""));
				buffer.append(!standardScale ? "\\cdot " : "\\;");
			}
		}
		if (u.isKilogram()) {
			u.setScale(u.getScale() + 3);
			u.setKind(Unit.Kind.GRAM);
		}
		if (!u.isDimensionless()) {
			String prefix = u.getPrefix();
			if (prefix.startsWith("10")) {
				prefix = u.getScale() != 0 ? String.format("10^{%s}",
					Integer.toString(u.getScale())) : "";
			} else if (prefix.equals("\u03BC")) {
				prefix = "\\upmu";
			} else {
				prefix = mathrm(prefix).toString();
			}
			buffer.append(prefix);
			switch (u.getKind()) {
				case CELSIUS:
					buffer.append("\\text{\\textcelsius}");
					break;
				case OHM:
					buffer.append("\\upOmega");
					break;
				default:
					buffer.append(mathrm(u.getKind().getSymbol()));
					break;
			}
		} else {
			if (u.getScale() != 0) {
				buffer.append("10^{");
				buffer.append(Integer.toString(u.getScale()));
				buffer.append("}\\;");
			}
			buffer.append(mathrm("dimensionless"));
		}
		if (((u.getOffset() != 0d) || (u.getMultiplier() != 1d) || !standardScale)
				&& (u.getExponent() != 1d)) {
			buffer = brackets(buffer);
		}
		if (u.getExponent() != 1) {
			buffer.append("^{");
			buffer.append(StringTools.toString(Locale.ENGLISH, u.getExponent()));
			buffer.append('}');
		}
		return buffer;
	}
	
	/**
	 * Returns a properly readable unit definition.
	 * 
	 * @param def
	 * @return
	 */
	public StringBuffer format(UnitDefinition def) {
		StringBuffer buffer = new StringBuffer();
		for (int j = 0; j < def.getUnitCount(); j++) {
			buffer.append(format(def.getUnit(j)));
			if (j < def.getListOfUnits().size() - 1) {
				buffer.append("\\cdot ");
			}
		}
		return buffer;
	}
	
	/**
	 * Returns the size of font to be used for regular text in point.
	 * 
	 * @return
	 */
	public short getFontSize() {
		return fontSize;
	}
	
	/**
	 * @return
	 */
	public String getHeadingsFont() {
		return fontHeadings;
	}
	
	/**
	 * @return the size of the paper to be used.
	 */
	public PaperSize getPaperSize() {
		return paperSize;
	}
	
	/**
	 * @return The font to be used for standard text.
	 */
	public String getTextFont() {
		return fontText;
	}
	
	/**
	 * @return The typewriter font to be used in the document.
	 */
	public String getTypewriterFont() {
		return fontTypewriter;
	}
	
	/**
	 * @return {@code true} if implicitly declared units should be made explicit.
	 */
	public boolean isAddPredefinedUnitDeclarations() {
		return showPredefinedUnitDeclarations;
	}
	
	/**
	 * This switch allows to change the way how the reactants, modifiers and
	 * products are presented in each reaction. If {@code true}, one table is created
	 * containing the identifiers of each reactant, modifier and product together
	 * with the respective name. If {@code false} (default), a subsection for each one of
	 * the three groups of participants is created giving all details of each
	 * participant.
	 * 
	 * @return The state of this switch.
	 */
	public boolean isArrangeReactionParticipantsInOneTable() {
		return arrangeReactionParticipantsInOneTable;
	}
	
	/**
	 * If this method returns {@code true}, this exporter performs a consistency check of
	 * the given SBML file and writes all errors and warnings found to at the end
	 * of the document.
	 * 
	 * @return
	 */
	public boolean isCheckConsistency() {
		return checkConsistency;
	}
	
	/**
	 * @return {@code true} if landscape format for the whole document is to be used.
	 */
	public boolean isLandscape() {
		return landscape;
	}
	
	/**
	 * @return {@code true} if names instead of ids are displayed in formulas and reaction
	 *         equations if available, i.e., the respective SBase has a name
	 *         attribute.
	 */
	public boolean isPrintNameIfAvailable() {
		return printNameIfAvailable;
	}
	
	/**
	 * Lets you decide weather or not MIRIAM annotations should be included into
	 * the model report
	 * 
	 * @return
	 */
	public boolean isSetIncludeMIRIAM() {
		return includeMIRIAM;
	}
	
	/**
	 * @return {@code true} if an extra title page is created {@code false} otherwise.
	 */
	public boolean isSetTitlepage() {
		return titlepage;
	}
	
	/**
	 * @return {@code true} if ids are written in type writer font.
	 */
	public boolean isSetTypewriter() {
		return typewriter;
	}
	
	@Override
	public StringBuffer mathtt(String id) {
		return !typewriter ? mathrm(id) : super.mathtt(id);
	}
	
	/**
	 * This switch allows to change the way how the reactants, modifiers and
	 * products are presented in each reaction. If {@code true}, one table is created
	 * containing the identifiers of each reactant, modifier and product together
	 * with the respective name. If {@code false} (default), a subsection for each one of
	 * the three groups of participants is created giving all details of each
	 * participant.
	 * 
	 * @param arrangeReactionParticipantsInOneTable
	 *        {@code true} if the participants of the reactions should be arranged in
	 *        small tables, {@code false} if a subsection should be created for the three
	 *        groups.
	 */
	public void setArrangeReactionParticipantsInOneTable(
		boolean arrangeReactionParticipantsInOneTable) {
		this.arrangeReactionParticipantsInOneTable = arrangeReactionParticipantsInOneTable;
	}
	
	/**
	 * If set to {@code true}, an SBML consistency check of the document is performed and
	 * all errors found will be written at the end of the document.
	 * 
	 * @param checkConsistency
	 */
	public void setCheckConsistency(boolean checkConsistency) {
		this.checkConsistency = checkConsistency;
	}
	
	/**
	 * This is the font size to be used in this document.
	 * 
	 * @param Allowed
	 *        values are:
	 *        <ul>
	 *        <li>8</li>
	 *        <li>9</li>
	 *        <li>10</li>
	 *        <li>11</li>
	 *        <li>12</li>
	 *        <li>14</li>
	 *        <li>16</li>
	 *        <li>17</li>
	 *        </ul>
	 *        Other values are set to the default of 11.
	 */
	public void setFontSize(short fontSize) {
		if ((fontSize < 8) || (fontSize == 13) || (17 < fontSize))
			this.fontSize = 11;
		this.fontSize = fontSize;
	}
	
	/**
	 * Allows you to change the font in headlines. Default: Helvetica.
	 * 
	 * @param fontHeadings
	 *        possible values are: cmss, avant, helvetica = helvet, times,
	 *        palatino
	 */
	public void setHeadingsFont(String fontHeadings) {
		fontHeadings = fontHeadings.toLowerCase();
		if (fontHeadings.equals("helvetica")) {
			fontHeadings = "helvet";
		}
		if (fontHeadings.equals("cmss") || fontHeadings.equals("avant")
				|| fontHeadings.equals("helvet") || fontHeadings.equals("times")
				|| fontHeadings.equals("mathptmx") || fontHeadings.equals("palatino")) {
			this.fontHeadings = fontHeadings;
		} else {
			logger.warning("Unsupported font " + fontHeadings + ". Using "
					+ this.fontHeadings + ".");
		}
	}
	
	/**
	 * Tells you if MIRIAM annotations will be included when generating a model
	 * report
	 * 
	 * @param includeMIRIAM
	 */
	public void setIncludeMIRIAM(boolean includeMIRIAM) {
		this.includeMIRIAM = includeMIRIAM;
	}
	
	/**
	 * If {@code true} is given the whole document will be created in landscape mode.
	 * Default is portrait.
	 * 
	 * @param landscape
	 */
	public void setLandscape(boolean landscape) {
		this.landscape = landscape;
	}
	
	/**
	 * Allowed are
	 * <ul>
	 * <li>letter</li>
	 * <li>legal</li>
	 * <li>executive</li>
	 * <li>a* where * stands for values from 0 thru 9</li>
	 * <li>b*</li>
	 * <li>c*</li>
	 * <li>d*</li>
	 * </ul>
	 * The default is {@link PaperSize#letter}.
	 */
	public void setPaperSize(PaperSize paperSize) {
		this.paperSize = paperSize;
	}
	
	/**
	 * If {@code true} species (reactants, modifiers and products) in reaction equations
	 * will be displayed with their name if they have one. By default the ids of
	 * the species are used in these equations.
	 */
	public void setPrintNameIfAvailable(boolean printNameIfAvailable) {
		this.printNameIfAvailable = printNameIfAvailable;
	}
	
	/**
	 * If {@code true} predefined SBML units will be made explicitly if not overridden in
	 * the model.
	 * 
	 * @param showPredefinedUnitDeclarations
	 */
	public void setShowPredefinedUnitDeclarations(
		boolean showPredefinedUnitDeclarations) {
		this.showPredefinedUnitDeclarations = showPredefinedUnitDeclarations;
	}
	
	/**
	 * Sets the font of the standard text to the given value. Default: Times.
	 * 
	 * @param fontText
	 *        possible values: "Computer Modern Roman" = cmr, times = mathptmx,
	 *        palatino = mathpazo, zapf = chancery, bookman, charter, newcent =
	 *        "New Century Schoolbook" and utopia
	 */
	public void setTextFont(String fontText) {
		fontText = fontText.toLowerCase();
		if (fontText.equals("times")) {
			fontText = "mathptmx";
		} else if (fontText.equals("computer modern roman")) {
			fontText = "cmr";
		} else if (fontText.equals("palatino")) {
			fontText = "mathpazo";
		} else if (fontText.equals("zapf")) {
			fontText = "chancery";
		} else if (fontText.equals("new century schoolbook")) {
			fontText = "newcent";
		}
		if (fontText.equals("bookman") || fontText.equals("chancery")
				|| fontText.equals("charter") || fontText.equals("cmr")
				|| fontText.equals("mathpazo") || fontText.equals("mathptmx")
				|| fontText.equals("newcent") || fontText.equals("utopia")) {
			this.fontText = fontText;
		} else {
			logger.warning("Unsupported font " + fontText + ". Using "
				+ this.fontText + ".");
		}
	}
	
	/**
	 * If {@code true} an extra title page is created. Default {@code false}.
	 * 
	 * @param titlepage
	 */
	public void setTitlepage(boolean titlepage) {
		this.titlepage = titlepage;
	}
	
	/**
	 * If {@code true} identifiers are set in typewriter font (default).
	 * 
	 * @param typewriter
	 */
	public void setTypewriter(boolean typewriter) {
		this.typewriter = typewriter;
	}
	
	/**
	 * Allows to change the typewriter font to be used. Default: CMT.
	 * 
	 * @param fontTypewriter
	 *        Possible values are: cmt, courier
	 */
	public void setTypewriterFont(String fontTypewriter) {
		fontTypewriter = fontTypewriter.toLowerCase();
		if (fontTypewriter.equals("courier") || fontTypewriter.equals("cmt")) {
			this.fontTypewriter = fontTypewriter;
		} else {
			logger.warning(MessageFormat.format(
				bundleUI.getString("UNSUPPORTED_FONT"),
				fontTypewriter, this.fontTypewriter));
		}
	}
	
	@Override
	public StringBuffer texttt(String id) {
		return !typewriter ? new StringBuffer(id) : super.texttt(id);
	}
	
	/**
	 * Writes a document foot for the LaTeX Document for all objects derived from
	 * {@see ListOf}.
	 * 
	 * @param listOf
	 * @param buffer
	 * @throws IOException
	 */
	private void documentFoot(ListOf<?> listOf, BufferedWriter buffer)
		throws IOException {
		if (listOf.size() == 0) {
			buffer.append(MessageFormat.format(
				bundleContent.getString("EMPTY_LIST_OF"),
				bundleElements.getString(listOf.getElementName())));
		}
		documentFoot(listOf.getSBMLDocument(), buffer);
	}
	
	/**
	 * This method writes the foot of a LaTeX document.
	 * 
	 * @param doc
	 * @param buffer
	 * @throws IOException
	 */
	private void documentFoot(SBMLDocument doc, BufferedWriter buffer)
		throws IOException {
		buffer.append(formatter.appendix());
		buffer.newLine();
		if (checkConsistency) {
			boolean notImplemented = false;
			try {
				doc.checkConsistency();
			} catch (NotImplementedException exc) {
				notImplemented = true;
			}
			if ((doc.getErrorCount() > 0) || notImplemented) {
				int i;
				SBMLError error;
				Vector<Integer> infos = new Vector<Integer>();
				Vector<Integer> warnings = new Vector<Integer>();
				Vector<Integer> fatal = new Vector<Integer>();
				Vector<Integer> system = new Vector<Integer>();
				Vector<Integer> xml = new Vector<Integer>();
				Vector<Integer> internal = new Vector<Integer>();
				Vector<Integer> errors = new Vector<Integer>();
				for (i = 0; i < doc.getErrorCount(); i++) {
					error = doc.getError(i);
					if (error.isInfo()) {
						infos.add(Integer.valueOf(i));
					} else if (error.isWarning()) {
						warnings.add(Integer.valueOf(i));
					} else if (error.isFatal()) {
						fatal.add(Integer.valueOf(i));
					} else if (error.isSystem()) {
						system.add(Integer.valueOf(i));
					} else if (error.isXML()) {
						xml.add(Integer.valueOf(i));
					} else if (error.isInternal()) {
						internal.add(Integer.valueOf(i));
					} else {
						// error.isError())
						errors.add(Integer.valueOf(i));
					}
				}
				buffer.append(section(bundleContent.getString("DOCUMENT_CONSISTENCY_REPORT"), true));
				
				if (notImplemented) {
					buffer.append(MessageFormat.format(
						bundleContent.getString("FULL_SBML_VALIDATION_NOT_YET_SUPPORTED"),
						formatter.trademark(),
						formatter.link("http://sbml.org/Software/JSBML", "JSBML"),
						formatter.sbml2latex(),
						formatter.link("http://sbml.org", "sbml.org")
					));
				} else {
					buffer.append(MessageFormat.format(
						bundleContent.getString("SBML_DOCUMENT_ERROR_INTRODUCTION"), 
						MessageFormat.format(bundleContent.getString("NUMERALS"), doc.getErrorCount()),
						doc.getErrorCount(),
						href("http://sbml.org/Facilities/Validator", bundleContent.getString("SBML_ONLINE_VALIDATOR"))));
					buffer.newLine();
					if (xml.size() > 0) {
						problemMessage(xml, doc, MessageFormat.format(bundleContent.getString("LABELED_ELEMENT"), bundleContent
							.getString("XML"), MessageFormat.format(
								bundleContent.getString("GRAMMATICAL_NUMBER_ERROR"), xml.size())), buffer, bundleContent.getString("ERROR"));
					}
					if (fatal.size() > 0) {
						problemMessage(fatal, doc, MessageFormat.format(bundleContent.getString("LABELED_ELEMENT"), bundleContent
							.getString("FATAL"), MessageFormat.format(
								bundleContent.getString("GRAMMATICAL_NUMBER_ERROR"), fatal.size())), buffer, bundleContent.getString("ERROR"));
					}
					if (system.size() > 0) {
						problemMessage(system, doc, MessageFormat.format(bundleContent.getString("LABELED_ELEMENT"), bundleContent
							.getString("SYSTEM"), MessageFormat.format(
								bundleContent.getString("GRAMMATICAL_NUMBER_MESSAGE"), system.size())), buffer, bundleContent.getString("ERROR"));
					}
					if (internal.size() > 0) {
						problemMessage(internal, doc,
							MessageFormat.format(bundleContent.getString("LABELED_ELEMENT"), bundleContent
								.getString("INTERNAL"), MessageFormat.format(
									bundleContent.getString("GRAMMATICAL_NUMBER_PROBLEM"), internal.size())),
							buffer, bundleContent.getString("ERROR"));
					}
					if (errors.size() > 0) {
						problemMessage(
							errors,
							doc,
							MessageFormat.format(bundleContent.getString("LABELED_ELEMENT"), bundleContent
									.getString("ERROR"), MessageFormat.format(
								bundleContent.getString("GRAMMATICAL_NUMBER_MESSAGE"), errors.size())),
							buffer, bundleContent.getString("ERROR"));
					}
					if (infos.size() > 0) {
						problemMessage(
							infos,
							doc,
							MessageFormat.format(
								bundleContent.getString("LABELED_ELEMENT"),
								bundleContent.getString("INFORMATION"),
								MessageFormat.format(
									bundleContent.getString("GRAMMATICAL_NUMBER_MESSAGE"), infos.size())),
							buffer, bundleContent.getString("INFORMATION"));
					}
					if (warnings.size() > 0) {
						problemMessage(warnings, doc, MessageFormat.format(
							bundleContent.getString("GRAMMATICAL_NUMBER_WARNING"), warnings.size()),
							buffer, bundleContent.getString("WARNING"));
					}
				}
			}
		}
		
		if (sboTerms.size() > 0) {
			buffer.append(section(bundleContent.getString("GLOSSARY_OF_ONTOLOGY"), true));
			buffer.append(label("sec:glossary"));
			buffer.append(descriptionBegin);
			int sbo[] = new int[sboTerms.size()], i = 0;
			for (Integer it : sboTerms) {
				sbo[i++] = it.intValue();
			}
			Arrays.sort(sbo);
			Term term;
			String name, def;
			for (int id : sbo) {
				term = SBO.getTerm(id);
				if (term != null) {
					def = SBOTermFormatter.getShortDefinition(term);
					def = maskSpecialChars(correctQuotationMarks(def, leftQuotationMark,
						rightQuotationMark));
					name = maskSpecialChars(correctQuotationMarks(term.getName(),
						leftQuotationMark, rightQuotationMark));
					buffer.append(descriptionItem(term.getId(),
						String.format("\\textbf{%s:} %s%s", name, def, newLine())));
				}
			}
			buffer.append(descriptionEnd);
		}
		buffer.append(OpenFile.readFile("../locales/literature.bbl"));
		buffer.append(LaTeX.endDocument());
		buffer.newLine();
	}
	
	/**
	 * This method writes the head of a LaTeX file.
	 * 
	 * @param doc
	 * @param buffer
	 * @throws IOException
	 */
	private void documentHead(SBMLDocument doc, BufferedWriter buffer)
		throws IOException {
		int i;
		Model model = doc.getModel();
		String title = "", titlePrefix = "";
		if (model != null) {
			if (model.isSetName()) {
				title = maskSpecialChars(model.getName());
				titlePrefix = "Model name:";
			}
			if ((title.length() == 0) && (model.isSetId())
					&& (model.getId().length() > 0)) {
				title = maskSpecialChars(model.getId());
				titlePrefix = "Model identifier:";
			}
		}
		if (title.length() == 0) {
			title = "Untitled";
			titlePrefix = "";
		} else {
			titlePrefix += " ";
		}
		buffer.append("\\documentclass[");
		buffer.append(Short.toString(fontSize));
		buffer.append("pt,twoside,bibtotoc");
		if (titlepage) {
			buffer.append(",titlepage");
		}
		if (landscape) {
			buffer.append(",landscape");
		}
		buffer.append(',');
		buffer.append(paperSize.toString());
		buffer.append("paper");
		if (!paperSize.equals("a4") || (fontSize < 10) || (12 < fontSize)) {
			buffer.append(",DIVcalc");
		}
		buffer.append("]{scrartcl}");
		buffer.newLine();
		buffer.append(usepackage("xcolor", "dvipsnames", "svgnames"));
		buffer.append(usepackage("ifpdf"));
		buffer.append(usepackage("scrpage2"));
		buffer.append(usepackage("footmisc"));
		// Allows multiple referencens to the same footnote within minipages.
		buffer.append("\\ifpdf");
		buffer.newLine();
		buffer.append("  \\usepackage[pdfpagemode={UseOutlines},");
		buffer.newLine();
		String space = "              ";
		buffer.append(space + "pdftitle={" + titlePrefix + "\"" + title + "\"},");
		buffer.newLine();
		buffer.append(space + "pdfauthor={" + MessageFormat.format(bundleContent.getString("PRODUCED_BY_SBML2LATEX"), "SBML2LaTeX", SBML2LaTeX.VERSION_NUMBER) + "},");
		buffer.newLine();
		buffer.append(space + "pdfsubject={SBML model summary},");
		buffer.newLine();
		buffer.append(space + "pdfkeywords={},");
		buffer.newLine();
		buffer.append(space + "pdfview={FitBH},");
		buffer.newLine();
		buffer.append(space + "plainpages={false},");
		buffer.newLine();
		buffer.append(space + "pdftex,");
		buffer.newLine();
		buffer.append(space + "colorlinks=true,");
		buffer.newLine();
		buffer.append(space + "pdfdisplaydoctitle=true,");
		buffer.newLine();
		buffer.append(space + "linkcolor=royalblue,");
		buffer.newLine();
		buffer.append(space + "bookmarks,");
		buffer.newLine();
		buffer.append(space + "bookmarksopen,");
		buffer.newLine();
		buffer.append(space + "bookmarksnumbered,");
		buffer.newLine();
		buffer.append(space + "pdfhighlight={/P},");
		buffer.newLine();
		buffer.append(space + "urlcolor={blue}]{hyperref}");
		buffer.newLine();
		buffer.append("  " + usepackage("pdflscape"));
		buffer.append("  \\pdfcompresslevel=9");
		buffer.newLine();
		buffer.append("  " + usepackage("graphicx", "pdftex"));
		buffer.append("\\else");
		buffer.newLine();
		buffer.append("  " + usepackage("hyperref", "plainpages={false}"));
		buffer.append("  " + usepackage("lscape"));
		buffer.append("  " + usepackage("graphicx"));
		buffer.append("  " + usepackage("breakurl"));
		buffer.append("\\fi");
		buffer.newLine();
		buffer.append(usepackage("calc"));
		buffer.append(usepackage("geometry", "paper=" + paperSize + "paper",
			"landscape=" + Boolean.toString(landscape), "centering"));
		// Font packages
		if (fontText.equals("bookman") || fontText.equals("chancery")
				|| fontText.equals("charter") || fontText.equals("mathpazo")
				|| fontText.equals("mathptmx") || fontText.equals("newcent")
				|| fontText.equals("utopia")) {
			buffer.append(usepackage(fontText));
		}
		if (fontHeadings.equals("helvet")) {
			buffer.append(usepackage(fontHeadings, "scaled=.95"));
		} else if (fontHeadings.equals("avant")) {
			buffer.append(usepackage(fontHeadings));
		}
		if (fontTypewriter.equals("courier")) {
			buffer.append(usepackage(fontTypewriter));
		}
		String[] packages = new String[] { "[english]{babel}", "[english]{rccol}",
				"[version=3]{mhchem}", "{relsize}", "{pifont}", "{textcomp}",
				"{longtable}", "{tabularx}", "{booktabs}", "{amsmath}", "{amsfonts}",
				"{amssymb}", "{mathtools}", "{ulem}", "{wasysym}", "{eurosym}",
				"{rotating}", "{upgreek}", "{flexisym}", "{breqn}", "{natbib}", "{varioref}" };
		for (i = 0; i < packages.length; i++) {
			buffer.append("\\usepackage");
			buffer.append(packages[i]);
			buffer.newLine();
		}
		
		if (includeLayoutSection) {
			// TODO: More general way to include extension package declarations required!
			LayoutModelPlugin layoutPlugin = (LayoutModelPlugin) model.getExtension(
				LayoutConstants.getNamespaceURI(model.getLevel(), model.getVersion()));
			if ((layoutPlugin != null) && (layoutPlugin.getLayoutCount() > 0)) {
				TikZLayoutBuilder.writeRequiredPackageDeclarationAndDefinitions(buffer, layoutPlugin.getListOfLayouts());
			}
		}
		
		buffer.newLine();
		buffer.append("\\selectlanguage{english}");
		buffer.newLine();
		if (0 < model.getFunctionDefinitionCount()) {
			buffer.append(OpenFile.readFile("../locales/linebreakdef.sty"));
		}
		
		buffer.append("\\definecolor{royalblue}{cmyk}{.93, .79, 0, 0}");
		buffer.newLine();
		// buffer.append("\\definecolor{grau}{gray}{0.7}");
		// buffer.newLine();
		buffer.append("\\definecolor{lightgray}{gray}{0.95}");
		buffer.newLine();
		buffer.append("\\addtokomafont{sectioning}{\\color{royalblue}}");
		// \definecolor{blue}{cmyk}{.93, .59, 0, 0}
		buffer.newLine();
		buffer.append("\\pagestyle{scrheadings}");
		buffer.newLine();
		buffer.append("\\newcommand{\\yes}{\\parbox[c]{1.3em}{\\Large\\Square\\hspace{-.65em}\\ding{51}}}");
		buffer.newLine();
		buffer.append("\\newcommand{\\no}{\\parbox[c]{1.3em}{\\Large\\Square\\hspace{-.62em}--}}");
		buffer.newLine();
		buffer.append("\\newcommand{\\numero}{N\\hspace{-0.075em}\\raisebox{0.25em}{\\relsize{-2}\\b{o}}}");
		buffer.newLine();
		buffer.append("\\newcommand{\\reaction}[1]{");
		buffer.append("\\begin{equation}\\ce{#1}\\end{equation}}");
		buffer.newLine();
		buffer.append("\\newcolumntype{C}[1]{>{\\centering\\arraybackslash}p{#1}}");
		buffer.newLine();
		buffer.append("\\newcommand{\\SBMLLaTeX}{{\\sffamily\\upshape");
		buffer.append("\\raisebox{-.35ex}{S\\hspace{-.425ex}BML}");
		buffer.append("\\hspace{-0.5ex}\\begin{rotate}{-17.5}\\raisebox{-.1ex}{2}");
		buffer.append("\\end{rotate}\\hspace{1ex}\\LaTeX}}");
		buffer.newLine();
		buffer.append("\\cfoot{");
		buffer.append(formatter.textcolor("gray",
			MessageFormat.format(
				bundleContent.getString("PRODUCED_BY_SBML2LATEX"),
				formatter.sbml2latex(), SBML2LaTeX.VERSION_NUMBER)));
		buffer.append('}');
		buffer.newLine();
		buffer.newLine();
		buffer.append(formatter.documentSubject(bundleContent.getString("SUBJECT")));
		buffer.newLine();
		buffer.append("\\title{");
		buffer.append(titlePrefix);
		if (titlePrefix.contains("identified")) {
			title = texttt(title).toString();
		} else {
			title = formatter.quote(title);
		}
		buffer.append(title);
		buffer.append('}');
		// buffer.append("}}}}");
		buffer.newLine();
		buffer.append(formatter.date(formatter.today()));
		buffer.newLine();
		buffer.append("\\author{");
		buffer.append("\\includegraphics[height=3.5ex]{" + logo + "}}");
		buffer.newLine();
		if (!typewriter) {
			buffer.append("\\urlstyle{same}");
			buffer.newLine();
		}
		
		buffer.newLine();
		buffer.append(LaTeX.beginDocument());
		buffer.append(LaTeX.makeTitle());
		buffer.newLine();
		buffer.append(formatter.thisPageStyle("scrheadings"));
		buffer.newLine();
	}
	
	/**
	 * Creates a mathematical equation in a math environment (not in-line).
	 * 
	 * @param formula
	 *        A formula to be displayed as an equation.
	 * @param formulae
	 *        Additional terms, for instance the following two: "=",
	 *        "some expression"
	 * @return
	 */
	private StringBuffer equation(StringBuffer formula, StringBuffer... formulae) {
		StringBuffer equation = new StringBuffer();
		if (formula.length() == 0) return equation;
		if (formula.charAt(0) != '$') equation.append(eqBegin);
		equation.append(formula);
		for (StringBuffer f : formulae)
			equation.append(f);
		if ((formula.charAt(formula.length() - 1) != '$')
				&& (equation.charAt(equation.length() - 1) != '$'))
			equation.append(eqEnd);
		return equation;
	}
	
	/**
	 * @param formula
	 * @return
	 */
	private StringBuffer equation(String formula) {
		return equation(new StringBuffer(formula));
	}
	
	/**
	 * This method formats MIRIAM annotations or other annotations stored in CV
	 * terms (controlled vocabulary).
	 * 
	 * @param cv
	 * @param buffer
	 * @throws IOException
	 */
	private void format(CVTerm cv, BufferedWriter buffer) throws IOException {
		List<String> resources = cv.getResources();
		switch (cv.getQualifierType()) {
			case MODEL_QUALIFIER:
				buffer.append(MessageFormat.format(
					bundleContent.getString(cv.getQualifierType().name()),
					bundleContent.getString(cv.getModelQualifierType().name())));
				break;
			case BIOLOGICAL_QUALIFIER:
				buffer.append(MessageFormat.format(
					bundleContent.getString(cv.getQualifierType().name()),
					MessageFormat.format(
						bundleContent.getString(cv.getBiologicalQualifierType().name()),
						resources.size())));
				break;
			default: // UNKNOWN_QUALIFIER
				buffer.append(bundleContent.getString(CVTerm.Type.UNKNOWN_QUALIFIER.name()));
				break;
		}
		String item = "";
		if (resources.size() > 1) {
			buffer.append(":\\begin{itemize}");
			buffer.newLine();
			item = "\\item ";
		} else {
			buffer.append(' ');
		}
		for (int i = 0; i < resources.size(); i++) {
			String identifier = resources.get(i);
			String url = null;
			if (!identifier.startsWith("urn")) {
				url = identifier;
			} else {
				url = "http://identifiers.org/"
						+ identifier.substring(11).replace(':', '/');
			}
			identifier = maskSpecialChars(identifier, false);
			buffer.append(item);
			buffer.append(href(url, texttt(identifier).toString()));
			buffer.append('.');
			buffer.newLine();
		}
		if (resources.size() > 1) {
			buffer.append("\\end{itemize}");
		}
		buffer.newLine();
	}
	
	/**
	 * Formats the date properly.
	 * 
	 * @param createdDate
	 * @param buffer
	 * @throws IOException
	 */
	private void format(Date date, BufferedWriter buffer) throws IOException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMMM '%s' yyyy, 'at' h:m '\\textsc{'a'}'", Locale.ENGLISH);
		DateFormatSymbols symbols = dateFormat.getDateFormatSymbols();
		symbols.setAmPmStrings(new String[] {"am", "pm"});
		dateFormat.setDateFormatSymbols(symbols);
		buffer.append(String.format(dateFormat.format(date), getNumbering(calendar.get(Calendar.DAY_OF_MONTH))));
	}
	
	/**
	 * @param def
	 * @param buffer
	 * @throws IOException
	 * @throws SBMLException
	 */
	private void format(FunctionDefinition def, BufferedWriter buffer)
		throws IOException, SBMLException {
		buffer.append(descriptionBegin);
		format(def, buffer, true);
		if ((def.getArgumentCount() > 0) || (def.getBody() != null)
				|| def.isSetMath()) {
			if (def.getArgumentCount() > 0) {
				List<String> eqnList = new ArrayList<String>(def.getArgumentCount());
				for (int j = 0; j < def.getArgumentCount(); j++) {
					eqnList.add(math(def.getArgument(j).toLaTeX()).toString());
				}
				buffer.append(descriptionItem(
					MessageFormat.format(bundleContent.getString("GRAMMATICAL_NUMBER_ARGUMENTS"),
					def.getArgumentCount()), format(eqnList)));
				if (def.getBody() != null) {
					buffer.append(descriptionItem(bundleContent.getString("MATHEMATICAL_EXPRESSION"),
						equation(new StringBuffer(def.getBody().toLaTeX()))));
				}
			} else if (def.isSetMath()) {
				buffer.append(descriptionItem(
					bundleContent.getString("MATHEMATICAL_FORMULA"),
					equation(getNameOrID(def, true), new StringBuffer(def.getMath()
							.toLaTeX()))));
			}
		}
		buffer.append(descriptionEnd);
	}

	/**
	 * @param a
	 * @param buffer
	 * @throws IOException
	 * @throws SBMLException
	 */
	private void format(InitialAssignment a, BufferedWriter buffer)
		throws IOException, SBMLException {
		buffer.append(descriptionBegin);
		format(a, buffer, true);
		buffer.append(descriptionItem(
			bundleElements.getString("derivedUnit"),
			a.containsUndeclaredUnits() ? bundleContent.getString("CONTAINS_UNDECLARED_UNITS") : math(format(a
					.getDerivedUnitDefinition()))));
		buffer.append(descriptionItem("Math", math(a.getMath().toLaTeX())));
		buffer.append(descriptionEnd);
	}
	
	/**
	 * @param list
	 * @param buffer
	 * @throws IOException
	 */
	private void formatCompartments(ListOf<? extends SBase> list,
		BufferedWriter buffer) throws IOException {
		StringBuffer description;
		for (int i = 0; i < list.size(); i++) {
			Compartment c = (Compartment) list.get(i);
			buffer.append(
				subsection(MessageFormat.format(
					bundleContent.getString("LABELED_ELEMENT"),
					bundleElements.getString(c.getElementName()),
					getNameOrID(c, false)),
				true));
			buffer.append("This is ");
			int spatialDim = (int) c.getSpatialDimensions();
			String dimension = (spatialDim - c.getSpatialDimensions() == 0d) ? MessageFormat.format(bundleContent.getString("NUMERALS"), spatialDim)
					: StringTools.toString(Locale.ENGLISH, c.getSpatialDimensions());
			buffer.append(indefiniteArticle(dimension.charAt(0)));
			buffer.append(' ');
			buffer.append(dimension);
			buffer.append("-dimensional compartment ");
			if (c.isSetCompartmentType()) {
				buffer.append("of type ");
				description = texttt(maskSpecialChars(c.getCompartmentType()));
				CompartmentType type = c.getModel().getCompartmentType(
					c.getCompartmentType());
				if (type.isSetName()) {
					description.append(" (");
					description.append(maskSpecialChars(type.getName()));
					description.append(')');
				}
				buffer.append(description);
				buffer.append(' ');
			}
			buffer.append("with a ");
			if (!c.getConstant()) {
				buffer.append("not ");
			}
			buffer.append("constant ");
			if (c.isSetSize()) {
				buffer.append("size of ");
				if (c.getSize() - (c.getSize()) == 0) {
					buffer.append(MessageFormat.format(bundleContent.getString("NUMERALS"), (int) c.getSize()));
				} else {
					buffer.append(format(c.getSize()));
				}
			} else buffer.append("size given in");
			String unitDef = format(c.getDerivedUnitDefinition()).toString();
			if (unitDef.equals("\\mathrm{l}")) {
				unitDef = "litre";
			} else {
				unitDef = math(unitDef).toString();
			}
			buffer.append("\\,");
			buffer.append(unitDef);
			if (c.isSetOutside()) {
				buffer.append(" that is surrounded by ");
				description = texttt(maskSpecialChars(c.getOutside()));
				Compartment outside = c.getModel().getCompartment(c.getOutside());
				if (outside.isSetName()) {
					description.append(" (");
					description.append(maskSpecialChars(outside.getName()));
					description.append(')');
				}
				buffer.append(description);
			}
			buffer.append('.');
			buffer.newLine();
			format((SBase) c, buffer, false);
		}
	}
	
	/**
	 * @param reactionList
	 * @param buffer
	 * @throws IOException
	 * @throws SBMLException
	 */
	@SuppressWarnings("unchecked")
	private void formatReactions(ListOf<? extends SBase> reactionList,
		BufferedWriter buffer) throws IOException, SBMLException {
		int reactionIndex, speciesIndex, sReferenceIndex;
		Species species;
		HashMap<String, Integer> speciesIDandIndex = new HashMap<String, Integer>();
		Model model = reactionList.getModel();
		if (model.getSpeciesCount() > 0) {
			List<Integer>[] reactantsReaction = new List[model.getSpeciesCount()];
			List<Integer>[] productsReaction = new List[model.getSpeciesCount()];
			List<Integer>[] modifiersReaction = new List[model.getSpeciesCount()];
			boolean notSubstancePerTimeUnit = false, notExistingKineticLaw = false;
			
			for (speciesIndex = 0; speciesIndex < model.getSpeciesCount(); speciesIndex++) {
				speciesIDandIndex.put(model.getSpecies(speciesIndex).getId(),
					Integer.valueOf(speciesIndex));
				reactantsReaction[(int) speciesIndex] = new Vector<Integer>();
				productsReaction[(int) speciesIndex] = new Vector<Integer>();
				modifiersReaction[(int) speciesIndex] = new Vector<Integer>();
			}
			for (reactionIndex = 0; reactionIndex < reactionList.size(); reactionIndex++) {
				Reaction r = (Reaction) reactionList.get(reactionIndex);
				buffer.append(format(r, reactionIndex));
				if (!r.isSetKineticLaw()) {
					notExistingKineticLaw = true;
				} else if (!r.getKineticLaw().getDerivedUnitDefinition()
						.isVariantOfSubstancePerTime()) {
					notSubstancePerTimeUnit = true;
				}
				for (sReferenceIndex = 0; sReferenceIndex < r.getReactantCount(); sReferenceIndex++) {
					speciesIndex = speciesIDandIndex.get(
						r.getReactant(sReferenceIndex).getSpecies()).intValue();
					reactantsReaction[(int) speciesIndex].add(Integer
							.valueOf(reactionIndex + 1));
				}
				for (sReferenceIndex = 0; sReferenceIndex < r.getProductCount(); sReferenceIndex++) {
					speciesIndex = speciesIDandIndex.get(
						r.getProduct(sReferenceIndex).getSpecies()).intValue();
					productsReaction[(int) speciesIndex].add(Integer
							.valueOf(reactionIndex + 1));
				}
				for (sReferenceIndex = 0; sReferenceIndex < r.getModifierCount(); sReferenceIndex++) {
					speciesIndex = speciesIDandIndex.get(
						r.getModifier(sReferenceIndex).getSpecies()).intValue();
					modifiersReaction[(int) speciesIndex].add(Integer
							.valueOf(reactionIndex + 1));
				}
			}
			
			// writing Equations
			buffer.append(section(MessageFormat.format(
				bundleContent.getString("DERIVED_RATE_EQUATIONS"),
				model.getSpeciesCount()), true));
			buffer.append(label("sec:DerivedRateEquations"));
			buffer.newLine();
			buffer.append("When interpreted as an ordinary differential equation framework, this model implies the following ");
			if (reactionList.size() == 1) {
				buffer.append("equation");
			} else {
				buffer.append("set of equations");
			}
			buffer.append(" for the rate");
			if (model.getSpeciesCount() > 1) {
				buffer.append("s of change of each ");
			} else {
				buffer.append(" of change of the following ");
			}
			buffer.append("species. ");
			buffer.newLine();
			
			if (notExistingKineticLaw) {
				buffer.newLine();
				buffer.append(MessageFormat.format(
					bundleContent.getString("REACTIONS_WITHOUT_OR_WITH_INCORRECT_KINETICS"),
					formatter.textcolor("red", "red")));
				buffer.newLine();
			}
			if (notSubstancePerTimeUnit) {
				buffer.newLine();
				buffer.append(MessageFormat.format(
					bundleContent.getString("REACTION_UNITS_CANNOT_BE_VERIFIED"),
					colorbox("lightgray", "gray"),
					texttt("substance"),
					texttt("time")));
				buffer.append("Please check if ");
				buffer.newLine();
				buffer.append("\\begin{itemize}");
				buffer.newLine();
				buffer.append("\\item parameters without a unit definition are involved or");
				buffer.newLine();
				buffer.append("\\item volume correction is necessary because the ");
				buffer.append(texttt("has\\-Only\\-Substance\\-Units"));
				buffer.append(" flag may be set to ");
				buffer.append(texttt(bundleContent.getString("FALSE")));
				buffer.append(bundleContent.getString("CONJUNCTION_AND"));
				buffer.append(texttt("spacial\\-Di\\-men\\-si\\-ons"));
				buffer.append("$> 0$ for certain species.");
				buffer.newLine();
				buffer.append("\\end{itemize}");
				buffer.newLine();
			}
			
			for (speciesIndex = 0; speciesIndex < model.getSpeciesCount(); speciesIndex++) {
				species = model.getSpecies(speciesIndex);
				subsection(species, speciesIndex, buffer);
				buffer.append(descriptionBegin);
				format(species, buffer, true);
				if (species.isSetInitialConcentration()) {
					String text = format(species.getInitialConcentration()).toString()
							.replaceAll("\\$", "");
					if ((model.getUnitDefinition("substance") != null)
							|| species.isSetSubstanceUnits()) {
						text += "\\;";
						UnitDefinition ud;
						if (species.isSetSubstanceUnits()
								&& (model.getUnitDefinition(species.getSubstanceUnits()) == null)) {
							ud = new UnitDefinition(species.getLevel(), species.getVersion());
							if (Unit.isUnitKind(species.getSubstanceUnits(),
								species.getLevel(), species.getVersion())) {
								Unit u = new Unit(species.getLevel(), species.getVersion());
								u.setKind(Unit.Kind.valueOf(species.getSubstanceUnits()));
								ud.addUnit(u);
							}
							// else: something's wrong.
						} else {
							ud = new UnitDefinition(
								species.isSetSubstanceUnits() ? model.getUnitDefinition(species
										.getSubstanceUnits())
										: model.getUnitDefinition("substance"));
						}
						Compartment compartment = model.getCompartment(species
								.getCompartment());
						for (int i = 0; i < compartment.getDerivedUnitDefinition().getUnitCount(); i++) {
							Unit unit = new Unit(compartment.getDerivedUnitDefinition().getUnit(i));
							unit.setExponent(-unit.getExponent());
							ud.addUnit(unit);
						}
						text += format(ud);
					}
					buffer.append(descriptionItem("Initial concentration", math(text)));
				} else if (species.isSetInitialAmount()) {
					String text = format(species.getInitialAmount()).toString()
							.replaceAll("\\$", "");
					if (species.isSetSubstanceUnits()) {
						text += "\\;";
						text += unitTest(species.getSubstanceUnits(), model);
					} else if (model.getUnitDefinition("substance") != null) {
						text += "\\;";
						text += format(model.getUnitDefinition("substance"));
					}
					buffer.append(descriptionItem("Initial amount", math(text)));
				}
				if (species.isSetCharge()) {
					buffer.append(descriptionItem("Charge",
						Integer.toString(species.getCharge())));
				}
				if (species.isSetSpeciesType()) {
					SpeciesType type = model.getSpeciesType(species.getSpeciesType());
					StringBuffer text = new StringBuffer(
						texttt(maskSpecialChars(type.getId())));
					if (type.isSetName()) {
						text.append(" (");
						text.append(maskSpecialChars(type.getName()));
						text.append(")");
					}
					buffer.append(descriptionItem("Species type", text));
				}
				// if (species.getBoundaryCondition()) {
				// buffer.append("\\item[Boundary condition] ");
				// buffer.append(yes);
				// buffer.newLine();
				// }
				// if (species.getConstant()) {
				// buffer.append("\\item[Constant] ");
				// buffer.append(yes);
				// buffer.newLine();
				// }
				
				int i, j;
				
				// ======= I N I T I A L A S S I G N M E N T S===========
				
				boolean hasInitialAssignment = false;
				for (i = 0; (i < model.getInitialAssignmentCount()); i++) {
					hasInitialAssignment = model.getInitialAssignment(i).getVariable().equals(species.getId());
					if (hasInitialAssignment) {
						break;
					}
				}
				if (hasInitialAssignment) {
					buffer.append(descriptionItem(
						bundleElements.getString("initialAssignment") + bundleContent.getString("WHITE_SPACE"),
						Integer.toString(i)));
				}
				
				// =========== R U L E S and E V E N T S =================
				
				// Events, in which this species is involved in
				Vector<String> eventsInvolved = new Vector<String>();
				Event event = null;
				for (i = 0; i < model.getEventCount(); i++) {
					event = model.getEvent(i);
					for (j = 0; j < event.getEventAssignmentCount(); j++) {
						if (event.getEventAssignment(j).getVariable()
								.equals(species.getId())) {
							eventsInvolved.add(event.isSetId() ? event.getId() : Integer
									.toString(i));
						}
					}
				}
				if (eventsInvolved.size() > 0) {
					List<String> evtList = new ArrayList<String>(eventsInvolved.size());
					for (String id : eventsInvolved) {
						evtList.add(hyperref(event.getElementName().toLowerCase() + id, texttt(maskSpecialChars(id))).toString());
					}
					buffer.append(descriptionItem(MessageFormat.format(
						bundleContent.getString("INVOLVED_IN_EVENTS"), evtList.size()),
						format(evtList)));
					
					// buffer.append(" influence");
					// if (eventsInvolved.size() == 1)
					// buffer.append('s');
					// buffer.append(" the rate of change of this species.");
					buffer.newLine();
				}
				
				/*
				 * Rules
				 */

				List<Integer> rulesInvolved = new Vector<Integer>();
				for (i = 0; i < model.getRuleCount(); i++) {
					Rule rule = model.getRule(i);
					if (rule instanceof AlgebraicRule) {
						if (contains(rule.getMath(), species.getId())) {
							rulesInvolved.add(Integer.valueOf(i));
						}
					} else {
						Assignment a = (Assignment) rule;
						if (a.getVariable().equals(species.getId())) {
							rulesInvolved.add(Integer.valueOf(i));
						}
					}
				}
				if (rulesInvolved.size() > 0) {
					buffer.append("\\item[Involved in rule");
					if (rulesInvolved.size() > 1) {
						buffer.append('s');
					}
					buffer.append("] ");
					List<String> ruleList = new ArrayList<String>(rulesInvolved.size());
					for (i = 0; i < rulesInvolved.size(); i++) {
						int index = rulesInvolved.get(i);
						ruleList.add(hyperref("rule" + index, "Rule " + index).toString());
					}
					buffer.append(format(ruleList));
					// buffer.append(" determine");
					// if (rulesInvolved.size() == 1)
					// buffer.append('s');
					// buffer.append(" the rate of change of this species.");
					buffer.newLine();
				}
				buffer.append(descriptionEnd);
				
				/*
				 * Derived Rate of Change
				 */

				StringWriter equation = new StringWriter();
				BufferedWriter equationBW = new BufferedWriter(equation);
				ASTNode ast;
				for (i = 0; i < productsReaction[(int) speciesIndex].size(); i++) {
					reactionIndex = productsReaction[(int) speciesIndex].get(i);
					Reaction r = model.getReaction(reactionIndex - 1);
					notSubstancePerTimeUnit = notExistingKineticLaw = false;
					if (r != null) {
						if (r.isSetKineticLaw()) {
							notSubstancePerTimeUnit = !r.getKineticLaw()
									.getDerivedUnitDefinition().isVariantOfSubstancePerTime();
						} else {
							notExistingKineticLaw = true;
						}
					} else {
						notExistingKineticLaw = true;
					}
					equationBW.flush();
					if (equation.getBuffer().length() > 0) {
						equationBW.append(" + ");
					}
					SpeciesReference product = r.getProductForSpecies(species.getId());
					if (product.isSetStoichiometryMath()) {
						ast = product.getStoichiometryMath().getMath();
						if ((ast.getType() == ASTNode.Type.PLUS)
								|| (ast.getType() == ASTNode.Type.MINUS)) {
							equationBW.append(brackets(ast.toLaTeX()));
						} else {
							equationBW.append(ast.toLaTeX());
						}
					} else {
						double doubleStoch = product.getStoichiometry();
						if (doubleStoch != 1d) {
							equationBW.append(format(doubleStoch).toString().replaceAll(
								"\\$", ""));
						}
					}
					formatVelocity(r, reactionIndex, notSubstancePerTimeUnit,
						notExistingKineticLaw, equationBW);
				}
				for (i = 0; i < reactantsReaction[(int) speciesIndex].size(); i++) {
					reactionIndex = reactantsReaction[(int) speciesIndex].get(i) - 1;
					Reaction r = model.getReaction(reactionIndex);
					notSubstancePerTimeUnit = notExistingKineticLaw = false;
					if (r != null) {
						KineticLaw kl = r.getKineticLaw();
						if (kl != null) {
							notSubstancePerTimeUnit = !kl.getDerivedUnitDefinition()
									.isVariantOfSubstancePerTime();
						} else {
							notExistingKineticLaw = true;
						}
					} else {
						notExistingKineticLaw = true;
					}
					
					SpeciesReference reactant = r.getReactantForSpecies(species.getId());
					equationBW.append('-');
					if (reactant.isSetStoichiometryMath()) {
						ast = reactant.getStoichiometryMath().getMath();
						if (ast.getType() == ASTNode.Type.PLUS
								|| ast.getType() == ASTNode.Type.MINUS) {
							equationBW.append(brackets(ast.toLaTeX()));
						} else {
							equationBW.append(ast.toLaTeX());
						}
					} else {
						double doubleStoch = reactant.getStoichiometry();
						if (doubleStoch != 1d) {
							equationBW.append(format(doubleStoch).toString().replaceAll(
								"\\$", ""));
						}
					}
					formatVelocity(r, reactionIndex + 1, notSubstancePerTimeUnit,
						notExistingKineticLaw, equationBW);
				}
				equationBW.close();
				
				final int numReactionsInvolved = productsReaction[(int) speciesIndex]
						.size()
						+ modifiersReaction[(int) speciesIndex].size()
						+ reactantsReaction[(int) speciesIndex].size();
				
				if (species.getBoundaryCondition()) {
					if (species.getConstant()) {
						// never changes
						if (0 < numReactionsInvolved) {
							formatReactionsInvolved(model, speciesIndex, reactantsReaction,
								productsReaction, modifiersReaction, buffer);
							buffer.append(", which do");
							if (numReactionsInvolved == 1) {
								buffer.append("es");
							}
							buffer.append(" not influence its rate of change because this constant species is on the boundary of the reaction system:");
						}
						buffer.append(eqBegin);
						buffer.append("\\frac{\\mathrm d}{\\mathrm dt} ");
						buffer.append(getNameOrID(species, true));
						buffer.append(" = 0");
						buffer.append(eqEnd);
						if ((rulesInvolved.size() > 0) || (eventsInvolved.size() > 0)) {
							buffer.append("This species' quantity is affected by ");
							if (rulesInvolved.size() > 0) {
								buffer.append(MessageFormat.format(bundleContent.getString("NUMERALS"), rulesInvolved.size()));
								buffer.append(" rule");
								if (rulesInvolved.size() > 1) {
									buffer.append('s');
								}
								if (eventsInvolved.size() > 0) {
									buffer.append(" and");
								}
							}
							if (eventsInvolved.size() > 0) {
								buffer.append(MessageFormat.format(bundleContent.getString("NUMERALS"), eventsInvolved.size()));
								buffer.append(" event");
								if (eventsInvolved.size() > 1) {
									buffer.append('s');
								}
							}
							buffer.append(". Please verify this SBML document.");
							buffer.newLine();
						}
					} else {
						// changes only due to rules and events
						if (numReactionsInvolved > 0) {
							formatReactionsInvolved(model, speciesIndex, reactantsReaction,
								productsReaction, modifiersReaction, buffer);
						}
						if ((rulesInvolved.size() > 0) || (eventsInvolved.size() > 0)) {
							if (numReactionsInvolved == 1) {
								buffer.append(". Not this but ");
							} else if (numReactionsInvolved > 1) {
								buffer.append(". Not these but ");
							}
							if (rulesInvolved.size() > 0) {
								String number = MessageFormat.format(bundleContent.getString("NUMERALS"), rulesInvolved.size());
								if (numReactionsInvolved == 0) {
									number = firstLetterUpperCase(number);
								}
								buffer.append(number);
								buffer.append(" rule");
								if (rulesInvolved.size() > 1) {
									buffer.append('s');
								}
								if (eventsInvolved.size() > 0) {
									buffer.append(" together with ");
								}
							}
							if (eventsInvolved.size() > 0) {
								String number = MessageFormat.format(bundleContent.getString("NUMERALS"), eventsInvolved.size());
								if (numReactionsInvolved == 0) {
									number = firstLetterLowerCase(number);
								}
								buffer.append(number);
								buffer.append(" event");
								if (eventsInvolved.size() > 1) {
									buffer.append('s');
								}
							}
							if (rulesInvolved.size() == 0) {
								buffer.append(" influence");
							} else {
								buffer.append(" determine");
							}
							if (eventsInvolved.size() + rulesInvolved.size() == 1) {
								buffer.append('s');
							}
							buffer.append(" the species' quantity");
							if (numReactionsInvolved > 0) {
								buffer.append(" because this species is on the boundary of the reaction system");
							}
							buffer.append('.');
						} else {
							if (numReactionsInvolved > 0) {
								buffer.append(", which do");
								if (numReactionsInvolved == 1) {
									buffer.append("es");
								}
								buffer.append(" not influence its rate of change because this species is on the boundary of the reaction system:");
							}
							buffer.append(eqBegin);
							buffer.append("\\frac{\\mathrm d}{\\mathrm dt} ");
							buffer.append(getNameOrID(species, true));
							buffer.append(" = 0");
							buffer.append(eqEnd);
						}
					}
				} else { // not boundary condition.
					int numModification = modifiersReaction[(int) speciesIndex].size();
					if (species.getConstant()) {
						// never changes, cannot be reactant or product and no
						// rules; but can be a modifier of reactions
						if ((rulesInvolved.size() == eventsInvolved.size())
								&& (numReactionsInvolved - numModification == 0)
								&& (numReactionsInvolved - numModification == rulesInvolved
										.size())) {
							if (0 < numModification) {
								formatReactionsInvolved(model, speciesIndex, reactantsReaction,
									productsReaction, modifiersReaction, buffer);
								buffer.append('.');
								buffer.newLine();
							}
							buffer.append(eqBegin);
							buffer.append("\\frac{\\mathrm d}{\\mathrm dt} ");
							buffer.append(getNameOrID(species, true));
							buffer.append(" = 0");
							buffer.append(eqEnd);
						} else {
							buffer.append("As this species is constant and its boundary condition is ");
							buffer.append(texttt(bundleContent.getString("FALSE")));
							buffer.append(" it cannot be involved in");
							boolean comma = false;
							if (rulesInvolved.size() > 0) {
								buffer.append(" any rules");
								comma = true;
							}
							if (eventsInvolved.size() > 0) {
								if (comma) {
									buffer.append(bundleContent.getString("SERIES_SEPARATOR"));
								} else {
									comma = true;
								}
								buffer.append(" any events");
							}
							if (numReactionsInvolved - numModification > 0) {
								if (comma) {
									buffer.append(" or");
								}
								buffer.append(" any reactions except it acts as as a modifier");
							}
							buffer.append(". Please verify this SBML document.");
						}
					} else { // not constant
						// changes by reactions xor rules; and events
						if (rulesInvolved.size() > 0) {
							boolean allAlgebraic = true;
							for (Iterator<Integer> iterator = rulesInvolved.iterator(); iterator
									.hasNext();) {
								if (!(model.getRule(iterator.next()) instanceof AlgebraicRule)) {
									allAlgebraic = false;
								}
							}
							String number = MessageFormat.format(bundleContent.getString("NUMERALS"), rulesInvolved.size());
							if (0 < numReactionsInvolved) {
								formatReactionsInvolved(model, speciesIndex, reactantsReaction,
									productsReaction, modifiersReaction, buffer);
								buffer.append(" and is also involved in ");
							} else {
								number = firstLetterUpperCase(number);
							}
							buffer.append(number);
							if (allAlgebraic) {
								buffer.append(" algebraic");
							}
							buffer.append(" rule");
							if (rulesInvolved.size() > 1) {
								buffer.append('s');
							}
							if (0 < numReactionsInvolved) {
								buffer.append(" that");
							}
							if (numReactionsInvolved - numModification == 0) {
								buffer.append(" determine");
								if (rulesInvolved.size() == 1) {
									buffer.append('s');
								}
								buffer.append(" this species' quantity.");
							} else if (!allAlgebraic) {
								buffer.append(". Please verify this SBML document.");
							} else {
								buffer.append('.');
								buffer.append(eqBegin);
								buffer.append("\\frac{\\mathrm d}{\\mathrm dt} ");
								buffer.append(getNameOrID(species, true));
								buffer.append(" = ");
								if (equation.getBuffer().length() > 0) {
									buffer.append(equation.getBuffer());
								} else {
									buffer.append('0');
								}
								buffer.append(eqEnd);
							}
							buffer.newLine();
						} else { // not involved in any rules.
							if (numReactionsInvolved == 0) {
								buffer.append("This species does not take part in any reactions. Its quantity does hence not change over time:");
							} else {
								formatReactionsInvolved(model, speciesIndex, reactantsReaction,
									productsReaction, modifiersReaction, buffer);
								buffer.append('.');
								buffer.newLine();
							}
							buffer.append(eqBegin);
							buffer.append("\\frac{\\mathrm d}{\\mathrm dt} ");
							buffer.append(getNameOrID(species, true));
							buffer.append(" = ");
							buffer.append((equation.getBuffer().length() > 0) ? equation
									.getBuffer() : "0");
							buffer.append(eqEnd);
						}
						if (eventsInvolved.size() > 0) {
							buffer.append("Furthermore, ");
							buffer.append(MessageFormat.format(bundleContent.getString("NUMERALS"), eventsInvolved.size()));
							buffer.append(" event");
							if (eventsInvolved.size() > 1) {
								buffer.append('s');
							}
							buffer.append(" influence");
							if (eventsInvolved.size() == 1) {
								buffer.append('s');
							}
							buffer.append(" this species' rate of change.");
						}
					}
				}
			}
			buffer.newLine();
		}
	}
	
	/**
	 * @param creator
	 */
	private String format(Creator creator) {
		StringBuilder sb = new StringBuilder();
		if (creator.isSetGivenName()) {
			sb.append(creator.getGivenName());
		}
		if (creator.isSetFamilyName()) {
			if (creator.isSetGivenName()) {
				sb.append(' ');
			}
			sb.append(creator.getFamilyName());
		}
		if ((creator.isSetGivenName() || creator.isSetFamilyName())
				&& (creator.isSetOrganisation() || creator.isSetEmail())) {
			sb.append("\\footnote{");
		}
		if (creator.isSetOrganisation()) {
			sb.append(creator.getOrganisation());
		}
		if (creator.isSetEmail()) {
			if (creator.isSetOrganisation()) {
				sb.append(bundleContent.getString("SERIES_SEPARATOR"));
			}
			sb.append(href("mailto:" + creator.getEmail(), "\\protect\\nolinkurl{"
					+ creator.getEmail() + '}'));
		}
		if ((creator.isSetGivenName() || creator.isSetFamilyName())
				&& (creator.isSetOrganisation() || creator.isSetEmail())) {
			sb.append('}');
		}
		return sb.toString();
	}

	/**
	 * Creates a subsection with all necessary information about one reaction.
	 * 
	 * @param r
	 * @param reactionIndex
	 * @return
	 * @throws IOException
	 * @throws SBMLException
	 */
	private StringBuffer format(Reaction r, int reactionIndex)
		throws IOException, SBMLException {
		int i;
		StringWriter reactString = new StringWriter();
		subsection(r, reactionIndex, reactString);
		reactString.append("This is a");
		if (!r.getReversible()) {
			reactString.append(r.getFast() ? " fast ir" : "n ir");
		} else {
			reactString.append(r.getFast() ? " fast " : " ");
		}
		reactString.append("reversible reaction of ");
		
		reactString.append(MessageFormat.format(bundleContent.getString("NUMERALS"), r.getReactantCount()));
		reactString.append(" reactant");
		if (r.getReactantCount() > 1) {
			reactString.append('s');
		}
		reactString.append(" forming ");
		reactString.append(MessageFormat.format(bundleContent.getString("NUMERALS"), r.getProductCount()));
		reactString.append(" product");
		if (r.getProductCount() > 1) {
			reactString.append('s');
		}
		if (r.getModifierCount() > 0) {
			reactString.append(" influenced by ");
			reactString.append(MessageFormat.format(bundleContent.getString("NUMERALS"), r.getModifierCount()));
			reactString.append(" modifier");
			if (r.getModifierCount() > 1) {
				reactString.append('s');
			}
		}
		reactString.append('.');
		
		int hasSBOReactants = 0, hasSBOProducts = 0, hasSBOModifiers = 0;
		boolean onlyItems = false;
		if (arrangeReactionParticipantsInOneTable) {
			for (i = 0; i < r.getReactantCount(); i++) {
				if (r.getReactant(i).isSetSBOTerm()) {
					hasSBOReactants++;
				}
			}
			for (i = 0, hasSBOProducts = 0; i < r.getProductCount(); i++) {
				if (r.getProduct(i).isSetSBOTerm()) {
					hasSBOProducts++;
				}
			}
			for (i = 0, hasSBOModifiers = 0; i < r.getModifierCount(); i++) {
				if (r.getModifier(i).isSetSBOTerm()) {
					hasSBOModifiers++;
				}
			}
			if (r.isSetName() || r.isSetNotes() || r.isSetSBOTerm()
					|| ((r.getCVTermCount() > 0) && includeMIRIAM)
					|| (hasSBOReactants + hasSBOProducts + hasSBOModifiers > 0)) {
				reactString.append(descriptionBegin);
				onlyItems = true;
			}
		}
		BufferedWriter bw = new BufferedWriter(reactString);
		format(r, bw, onlyItems);
		bw.close();
		if (arrangeReactionParticipantsInOneTable) {
			if (hasSBOReactants > 0) {
				reactString.append("\\item[");
				reactString.append(
				  MessageFormat.format(bundleContent.getString("ELEMENT_WITH_SBO"),
				  MessageFormat.format(bundleContent.getString("GRAMMATICAL_NUMBER_REACTANT"),
				    hasSBOReactants)));
				reactString.append("] ");
				for (i = 0; i < r.getReactantCount(); i++) {
					SpeciesReference reactant = r.getReactant(i);
					if (r.getReactant(i).isSetSBOTerm()) {
						reactString.append(texttt(maskSpecialChars(reactant.getSpecies())));
						reactString.append(" (");
						reactString.append(SBO.sboNumberString(reactant.getSBOTerm()));
						sboTerms.add(Integer.valueOf(reactant.getSBOTerm()));
						reactString.append(')');
						if (--hasSBOReactants > 0) {
							reactString.append(bundleContent.getString("SERIES_SEPARATOR"));
						}
					}
				}
			}
			if (hasSBOProducts > 0) {
				reactString.append("\\item[");
				reactString.append(
				  MessageFormat.format(bundleContent.getString("ELEMENT_WITH_SBO"),
				  MessageFormat.format(bundleContent.getString("GRAMMATICAL_NUMBER_PRODUCT"),
				    hasSBOProducts)));
				reactString.append("] ");
				for (i = 0; i < r.getProductCount(); i++) {
					SpeciesReference product = r.getProduct(i);
					if (r.getProduct(i).isSetSBOTerm()) {
						reactString.append(texttt(maskSpecialChars(product.getSpecies())));
						reactString.append(" (");
						reactString.append(SBO.sboNumberString(product.getSBOTerm()));
						sboTerms.add(Integer.valueOf(product.getSBOTerm()));
						reactString.append(')');
						if (--hasSBOProducts > 0) {
							reactString.append(bundleContent.getString("SERIES_SEPARATOR"));
						}
					}
				}
			}
			if (r.getListOfModifiers().size() > 0) {
				if (hasSBOModifiers > 0) {
					reactString.append("\\item[");
					reactString.append(
					  MessageFormat.format(bundleContent.getString("ELEMENT_WITH_SBO"),
					  MessageFormat.format(bundleContent.getString("GRAMMATICAL_NUMBER_MODIFIER"),
					    hasSBOModifiers)));
					reactString.append("] ");
					for (i = 0; i < r.getModifierCount(); i++) {
						ModifierSpeciesReference m = r.getModifier(i);
						if (m.isSetSBOTerm()) {
							reactString.append(" (");
							reactString.append(SBO.sboNumberString(m.getSBOTerm()));
							reactString.append(" ");
							reactString.append(maskSpecialChars(correctQuotationMarks(SBO
									.getTerm(m.getSBOTerm()).getName(), leftQuotationMark,
								rightQuotationMark)));
							sboTerms.add(Integer.valueOf(m.getSBOTerm()));
							reactString.append(')');
							if (--hasSBOModifiers > 0) {
								reactString.append(bundleContent.getString("SERIES_SEPARATOR"));
							}
						}
					}
				}
			}
			if (onlyItems) {
				reactString.append(descriptionEnd);
			}
		}
		
		reactString.append(subsubsection(bundleContent.getString("REACTION_EQUATION"), false));
		reactString.append("\\reaction{");
		reactString.append(reactionEquation(r));
		reactString.append('}');
		reactString.append(newLine());
		
		if (arrangeReactionParticipantsInOneTable) {
			/*
			 * One table for the reactants and products
			 */
			String headLine = "", head = "", idAndNameColumn;
			double nameWidth = 3d;
			double idWidth = nameWidth / 2d;
			if ((paperSize == PaperSize.letter) || (paperSize == PaperSize.a4)) { 
				idAndNameColumn = "p{" + idWidth + "cm}p{" + nameWidth + "cm}";
			} else {
				int columns = 0;
				if (r.getReactantCount() > 0) {
					columns += 2;
				}
				if (r.getModifierCount() > 0) {
					columns += 2;
				}
				if (r.getProductCount() > 0) {
					columns += 2;
				}
				switch (columns) {
					case 2:
						idWidth = 0.3;
						break;
					case 4:
						idWidth = 0.15;
						break;
					case 6:
						idWidth = 0.1;
						break;
					default:
						idWidth = 0;
						break;
				}
				nameWidth = idWidth * 2;
				idAndNameColumn = "p{" + idWidth + "\\textwidth}p{" + nameWidth + "\\textwidth}";
			}
			int cols = 0;
			if (r.getReactantCount() > 0) {
				headLine = "\\multicolumn{2}{c";
				head += idAndNameColumn;
				if ((r.getProductCount() > 0) || (r.getModifierCount() > 0)) {
					headLine += "|}{" + bundleElements.getString(r.getListOfReactants().getElementName()) + "}&";
					head += '|';
				} else {
					headLine += "}{" + bundleElements.getString(r.getListOfReactants().getElementName()) + "}";
				}
				cols++;
			}
			if (r.getModifierCount() > 0) {
				headLine += "\\multicolumn{2}{c";
				head += idAndNameColumn;
				if (r.getProductCount() > 0) {
					headLine += "|}{" + bundleElements.getString(r.getListOfModifiers().getElementName()) + "}&";
					head += '|';
				} else {
					headLine += "}{" + bundleElements.getString(r.getListOfModifiers().getElementName()) + "}";
				}
				cols++;
			}
			if (r.getProductCount() > 0) {
				headLine += formatter.multicolumn(2, Align.center, bundleElements.getString(r.getListOfProducts().getElementName()));
				head += idAndNameColumn;
				cols++;
			}
			headLine += lineBreak;
			String idAndNameColumnDef = bundleElements.getString("id") + '&' + bundleElements.getString("name");
			headLine += idAndNameColumnDef;
			for (i = 1; i < cols; i++) {
				headLine += '&' + idAndNameColumnDef;
			}
			reactString.append(longtableHead(head, MessageFormat.format(
				bundleContent.getString("OVERVIEW_TABLE_CAPTION"),
				bundleContent.getString("PARTICIPATING_SPECIES")),
				headLine));
			for (i = 0; i < Math.max(r.getReactantCount(),
				Math.max(r.getProductCount(), r.getModifierCount())); i++) {
				Species s;
				if (r.getReactantCount() > 0) {
					if (i < r.getReactantCount()) {
						s = r.getModel().getSpecies(r.getReactant(i).getSpecies());
						reactString.append(texttt(maskSpecialChars(s.getId())));
						reactString.append('&');
						reactString.append(maskSpecialChars(s.getName()));
						/*
						 * reactString.append('&'); reactString.append(s.isSetSBOTerm() ?
						 * SBO.sboNumberString(s .getSBOTerm()) : " ");
						 */
					} else {
						reactString.append('&');
					}
					if ((r.getModifierCount() > 0) || (r.getProductCount() > 0)) {
						reactString.append('&');
					}
				}
				if (r.getModifierCount() > 0) {
					if (i < r.getModifierCount()) {
						s = r.getModel().getSpecies(r.getModifier(i).getSpecies());
						reactString.append(texttt(maskSpecialChars(s.getId())));
						reactString.append('&');
						reactString.append(maskSpecialChars(s.getName()));
						/*
						 * reactString.append('&'); reactString.append(s.isSetSBOTerm() ?
						 * SBO.sboNumberString(s .getSBOTerm()) : " ");
						 */
					}
					if (r.getProductCount() > 0) {
						reactString.append('&');
					}
				}
				if (r.getProductCount() > 0) {
					if (i < r.getProductCount()) {
						s = r.getModel().getSpecies(r.getProduct(i).getSpecies());
						reactString.append(texttt(maskSpecialChars(s.getId())));
						reactString.append('&');
						reactString.append(maskSpecialChars(s.getName()));
						/*
						 * reactString.append('&'); reactString.append(s.isSetSBOTerm() ?
						 * SBO.sboNumberString(s .getSBOTerm()) : " ");
						 */
					} else {
						reactString.append('&');
					}
				}
				reactString.append(lineBreak);
			}
			reactString.append(bottomrule);
			
		} else {
			/*
			 * We want to arrange all participants in a separate table.
			 */
			SpeciesReference specRef;
			ModifierSpeciesReference modRef;
			Species species;
			String caption = "Properties of each ";
			String columnDef = "llc";
			String headLine = "Id & Name & SBO";
			
			if (r.getReactantCount() > 0) {
				reactString.append(subsubsection(bundleElements.getString(r.getListOfReactants().getElementName()), false));
				reactString.append(longtableHead(columnDef, caption + "reactant.", headLine));
				for (i = 0; i < r.getListOfReactants().size(); i++) {
					specRef = r.getReactant(i);
					species = r.getModel().getSpecies(specRef.getSpecies());
					reactString.append(texttt(maskSpecialChars(specRef.getSpecies())));
					reactString.append('&');
					reactString.append(
						maskSpecialChars(specRef.getName().length() == 0 ? species.getName() : specRef.getName()));
					reactString.append('&');
					if (specRef.isSetSBOTerm()) {
						reactString.append(SBO.sboNumberString(specRef.getSBOTerm()));
						sboTerms.add(Integer.valueOf(specRef.getSBOTerm()));
					}
					reactString.append(lineBreak);
				}
				reactString.append(bottomrule);
			}
			if (r.getModifierCount() > 0) {
				reactString.append(subsubsection(bundleElements.getString(r.getListOfModifiers().getElementName()), false));
				reactString.append(longtableHead(columnDef, caption + "modifier.", headLine));
				for (i = 0; i < r.getListOfModifiers().size(); i++) {
					modRef = r.getModifier(i);
					species = r.getModel().getSpecies(modRef.getSpecies());
					reactString.append(texttt(maskSpecialChars(modRef.getSpecies())));
					reactString.append('&');
					reactString.append(maskSpecialChars(modRef.getName().length() == 0 ? species.getName() : modRef.getName()));
					reactString.append('&');
					if (modRef.isSetSBOTerm()) {
						reactString.append(SBO.sboNumberString(modRef.getSBOTerm()));
						sboTerms.add(Integer.valueOf(modRef.getSBOTerm()));
					}
					reactString.append(lineBreak);
				}
				reactString.append(bottomrule);
			}
			if (r.getProductCount() > 0) {
				reactString.append(subsubsection(bundleElements.getString(r.getListOfProducts().getElementName()), false));
				reactString.append(longtableHead(columnDef, caption + "product.", headLine));
				for (i = 0; i < r.getListOfProducts().size(); i++) {
					specRef = r.getProduct(i);
					species = r.getModel().getSpecies(specRef.getSpecies());
					reactString.append(texttt(maskSpecialChars(specRef.getSpecies())));
					reactString.append('&');
					reactString.append(maskSpecialChars(specRef.getName().length() == 0 ? species
									.getName() : specRef.getName()));
					reactString.append('&');
					if (specRef.isSetSBOTerm()) {
						reactString.append(SBO.sboNumberString(specRef.getSBOTerm()));
						sboTerms.add(Integer.valueOf(specRef.getSBOTerm()));
					}
					reactString.append(lineBreak);
				}
				reactString.append(bottomrule);
			}
		}
		
		reactString.append(subsubsection(bundleElements.getString("kineticLaw"), false));
		StringWriter localParameters = new StringWriter();
		List<String> functionCalls = null;
		if (r.isSetKineticLaw()) {
			KineticLaw kin = r.getKineticLaw();
			reactString.append(descriptionBegin);
			BufferedWriter pBuffer = new BufferedWriter(reactString);
			format(kin, pBuffer, true);
			pBuffer.close();
			UnitDefinition ud = kin.getDerivedUnitDefinition();
			reactString.append(formatter.labeledItem(bundleElements.getString("derivedUnit")));
			if (ud.getUnitCount() == 0) {
				reactString.append(bundleContent.getString("NOT_AVAILABLE"));
			} else if (kin.containsUndeclaredUnits()) {
				reactString.append(bundleContent.getString("CONTAINS_UNDECLARED_UNITS"));
			} else {
				UnitDefinition.simplify(ud);
				reactString.append(math(format(ud)));
			}
			reactString.append(newLine());
			reactString.append(descriptionEnd);
			reactString.append(eqBegin);
			reactString.append("v_{" + (reactionIndex + 1) + "}=");
			if (kin.getMath() != null) {
				reactString.append(kin.getMath().toLaTeX());
				if (0 < r.getModel().getFunctionDefinitionCount()) {
					functionCalls = callsFunctions(kin.getMath());
				}
			} else {
				reactString.append(formatter.mathText(bundleContent.getString("NO_MATH_SPECIFIED")));
			}
			pBuffer = new BufferedWriter(localParameters);
			if (r.getKineticLaw().getLocalParameterCount() > 0) {
				format(r.getKineticLaw().getListOfLocalParameters(), pBuffer, false);
			}
			pBuffer.close();
		} else {
			reactString.append(eqBegin);
			reactString.append("v_{" + (reactionIndex + 1) + "}=");
			reactString.append(formatter.mathText(bundleContent.getString("NOT_SPECIFIED")));
		}
		reactString.append(newLine());
		reactString.append(label("v" + Integer.toString(reactionIndex + 1)));
		reactString.append(eqEnd);
		if (functionCalls != null) {
			// If a kinetic law calls functions we add their definitions
			// redundantly right after the kinetic law.
			for (String id : functionCalls) {
				reactString.append(equation(mathtt(maskSpecialChars(id)),
					new StringBuffer(r.getModel().getFunctionDefinition(id).getMath()
							.toLaTeX())));
			}
		}
		reactString.append(localParameters.getBuffer());
		
		return reactString.getBuffer();
	}

	/**
	 * @param rl
	 * @param buffer
	 * @throws IOException
	 * @throws SBMLException
	 */
	private void format(Rule rl, BufferedWriter buffer) throws IOException,
		SBMLException {
		buffer.append("Rule ");
		if (rl.isSetSBOTerm()) {
			buffer.append(" has the SBO reference ");
			buffer.append(SBO.sboNumberString(rl.getSBOTerm()));
			sboTerms.add(Integer.valueOf(rl.getSBOTerm()));
			buffer.append(" and");
		}
		buffer.append(" is a");
		if (rl.isAlgebraic()) {
			buffer.append("n algebraic rule");
			buffer.append(equation(new StringBuffer(rl.getMath().toLaTeX()), new StringBuffer("\\equiv 0")));
			buffer.newLine();
			Variable variable;
			if (!validator.isOverdetermined()) {
				variable = (Variable) validator.getMatching().get(rl);
				buffer.append(MessageFormat.format(
					bundleContent.getString("ALGEBRAIC_RULE_VARIABLE"),
					variable.getElementName(),
					getNameOrID(variable, false)));
				buffer.newLine();
			} else {
				buffer.append(bundleContent.getString("ALGEBRAIC_RULE_OVERDETERMINED_MESSAGE"));
			}
		} else if (rl.isAssignment()) {
			buffer.append("n assignment rule for ");
			Model model = rl.getModel();
			String id = ((Assignment) rl).getVariable();
			if (model.getSpecies(id) != null) {
				buffer.append("species ");
				buffer.append(texttt(maskSpecialChars(id)));
				buffer.append(':');
				buffer.append(eqBegin);
				Species species = model.getSpecies(id);
				if (species.getHasOnlySubstanceUnits()) {
					buffer.append('[');
				}
				buffer.append(mathtt(maskSpecialChars(id)));
				if (species.getHasOnlySubstanceUnits()) {
					buffer.append(']');
				}
			} else if (model.getCompartment(id) != null) {
				buffer.append("compartment ");
				buffer.append(texttt(maskSpecialChars(id)));
				buffer.append(':');
				buffer.append(eqBegin);
				buffer.append(getSize(model.getCompartment(id)));
			} else {
				buffer.append("parameter ");
				buffer.append(texttt(maskSpecialChars(id)));
				buffer.append(':');
				buffer.append(eqBegin);
				buffer.append(mathtt(maskSpecialChars(id)));
			}
			buffer.append(" = ");
			buffer.append(rl.getMath().toLaTeX());
			buffer.append(eqEnd);
		} else {
			buffer.append(" rate rule for ");
			boolean hasOnlySubstanceUnits = false;
			if (rl.getModel().getSpecies(((Assignment) rl).getVariable()) != null) {
				buffer.append("species ");
				hasOnlySubstanceUnits = rl.getModel()
						.getSpecies(((Assignment) rl).getVariable())
						.getHasOnlySubstanceUnits();
			} else if (rl.getModel().getCompartment(((Assignment) rl).getVariable()) != null) {
				buffer.append("compartment ");
			} else {
				buffer.append("parameter ");
			}
			buffer.append(texttt(maskSpecialChars(((Assignment) rl).getVariable())));
			buffer.append(':');
			buffer.append(eqBegin);
			buffer.append("\\frac{\\mathrm d}{\\mathrm dt} ");
			if (hasOnlySubstanceUnits) {
				buffer.append('[');
			}
			if (rl.getModel().getCompartment(((Assignment) rl).getVariable()) != null) {
				buffer.append(getSize(rl.getModel().getCompartment(
					((Assignment) rl).getVariable())));
			} else {
				buffer.append(mathtt(maskSpecialChars(((Assignment) rl).getVariable())));
			}
			if (hasOnlySubstanceUnits) {
				buffer.append(']');
			}
			buffer.append(" = ");
			buffer.append(rl.getMath().toLaTeX());
			buffer.append(eqEnd);
		}
		boolean containsUndeclaredUnits = rl.containsUndeclaredUnits();
		UnitDefinition derivedUnit = rl.getDerivedUnitDefinition();
		if (rl.isSetNotes() || 
			((derivedUnit.getUnitCount() > 0) && !containsUndeclaredUnits) ||
			((rl.getCVTermCount() > 0) && includeMIRIAM)) {
			buffer.append(descriptionBegin);
			if ((derivedUnit.getUnitCount() > 0) && !containsUndeclaredUnits) {
				buffer.append(descriptionItem(
					bundleElements.getString("derivedUnit"),
					math(format(derivedUnit))));
			}
			if (rl.isSetNotes()) {
				buffer.append(descriptionItem(
					bundleElements.getString("notes"),
					formatHTML(rl.getNotesString())));
			}
			if ((rl.getCVTermCount() > 0) && includeMIRIAM) {
				buffer.append(formatter.labeledItem("Annotation"));
				for (int i = 0; i < rl.getCVTermCount(); i++) {
					format(rl.getCVTerm(i), buffer);
				}
			}
			buffer.append(descriptionEnd);
		}
	}

	/**
	 * Formats name, {@link SBO} term and notes as items of a description environment.
	 * 
	 * @param sBase
	 * @param buffer
	 * @param onlyItems
	 *        If {@code true} items will be written otherwise this will be
	 *        surrounded by a description environment.
	 * @throws IOException
	 */
	private void format(SBase sBase, BufferedWriter buffer, boolean onlyItems)
		throws IOException {
		
		formatHistory(sBase, buffer);
		
		if (((sBase instanceof NamedSBase) && ((NamedSBase) sBase).isSetName())
				|| sBase.isSetNotes() || sBase.isSetSBOTerm()
				|| ((sBase.getCVTermCount() > 0 && includeMIRIAM))) {
			if (!onlyItems) {
				buffer.append(descriptionBegin);
			}
			if (((sBase instanceof NamedSBase) && ((NamedSBase) sBase).isSetName())) {
				buffer.append(descriptionItem(bundleElements.getString("name"),
					maskSpecialChars(((NamedSBase) sBase).getName())));
			}
			if (sBase.isSetSBOTerm()) {
				buffer.append(descriptionItem(
					"SBO:" + SBO.sboNumberString(sBase.getSBOTerm()),
					correctQuotationMarks(SBO.getTerm(sBase.getSBOTerm()).getName(),
						leftQuotationMark, rightQuotationMark)));
				sboTerms.add(Integer.valueOf(sBase.getSBOTerm()));
			}
			if (sBase.isSetNotes()) {
				buffer.append(descriptionItem(bundleElements.getString("notes"),
					formatHTML(sBase.getNotesString()).toString()));
			}
			if ((sBase.getCVTermCount() > 0) && includeMIRIAM) {
				StringWriter description = new StringWriter();
				BufferedWriter bw = new BufferedWriter(description);
				for (int i = 0; i < sBase.getCVTermCount(); i++) {
					format(sBase.getCVTerm(i), bw);
					bw.newLine();
				}
				bw.close();
				buffer.append(descriptionItem("MIRIAM Annotation", description));
			}
			if (!onlyItems) {
				buffer.append(descriptionEnd);
			}
		}
	}

	/**
	 * @param message
	 * @return
	 */
	private StringBuffer formatErrorMessage(String message) {
		StringBuffer m = new StringBuffer();
		for (int i = 0; i < message.length(); i++) {
			char c = message.charAt(i);
			switch (c) {
				case '<':
					int closing = message.indexOf('>', i);
					if ((typewriter) && (i < closing)) {
						m.append(texttt(message.substring(i, closing + 1)));
						i = closing;
					} else m.append("$<$");
					break;
				case '>':
					m.append("$>$");
					break;
				default:
					if ((c == '_') || (c == '\\') || (c == '$') || (c == '&')
							|| (c == '#') || (c == '{') || (c == '}') || (c == '~')
							|| (c == '%')) m.append('\\');
					m.append(c);
					break;
			}
		}
		return m;
	}

	/**
	 * Creates a list of reactions including the correct hyper references to the
	 * respective kinetic laws, in which the given species is involved in. This
	 * list is enclosed in parenthesis.
	 * 
	 * @param model
	 * @param speciesIndex
	 * @param reactantsReaction
	 * @param productsReaction
	 * @param buffer
	 * @param modifierReaction
	 * @throws IOException
	 */
	private void formatReactionsInvolved(Model model, int speciesIndex,
		List<Integer>[] reactantsReaction, List<Integer>[] productsReaction,
		List<Integer>[] modifierReaction, BufferedWriter buffer) throws IOException {
		int numReactants = reactantsReaction[speciesIndex].size();
		int numProducts = productsReaction[speciesIndex].size();
		int numModifiers = modifierReaction[speciesIndex].size();
		final int numReactionsInvolved = numReactants + numProducts + numModifiers;
		buffer.append(MessageFormat.format(
			bundleContent.getString("REACTION_PARTICIPATION_OF_SPECIES"),
			MessageFormat.format(bundleContent.getString("NUMERALS"), numReactionsInvolved),
			numReactionsInvolved));
		buffer.append(" (");
		Reaction reaction;
		boolean noComma = false;
		for (int i = 0, reactionIndex; i < numReactionsInvolved; i++) {
			if (i < numReactants) {
				if (i == 0) {
					buffer.append("as a reactant in ");
					noComma = true;
				} else {
					noComma = false;
				}
				reactionIndex = reactantsReaction[speciesIndex].get(i).intValue();
			} else if (i < numReactants + numProducts) {
				if (i == numReactants) {
					if (0 < i) {
						buffer.append(bundleContent.getString("CONJUNCTION_AND"));
					}
					buffer.append("as a product in ");
					noComma = true;
				} else {
					noComma = false;
				}
				reactionIndex = productsReaction[speciesIndex].get(i - numReactants)
						.intValue();
			} else {
				if (i == numReactants + numProducts) {
					if (0 < i) {
						buffer.append(bundleContent.getString("CONJUNCTION_AND"));
					}
					buffer.append("as a modifier in ");
					noComma = true;
				} else {
					noComma = false;
				}
				reactionIndex = modifierReaction[(int) speciesIndex].get(
					(int) i - numReactants - numProducts).intValue();
			}
			reaction = model.getReaction(reactionIndex - 1);
			if ((0 < i) && (!noComma)) {
				buffer.append(bundleContent.getString("SERIES_SEPARATOR"));
			} else {
				buffer.append(' ');
			}
			buffer.append(hyperref("v" + Integer.toString(reactionIndex),
				texttt(maskSpecialChars(reaction.getId()))));
		}
		buffer.append(')');
	}

	/**
	 * Returns a mathematical formula if {@link StoichiometryMath} is used or the
	 * formated stoichiometric coefficient of the given {@link SpeciesReference}. Be aware
	 * that dollar symbols may be set at the beginning and the end of the
	 * stoichiometric coefficient/mathematical formula if necessary. If already
	 * using math mode please remove these dollar symbols.
	 * 
	 * @param spec
	 * @return
	 * @throws SBMLException
	 * @throws IOException
	 */
	private StringBuffer formatStoichiometry(SpeciesReference spec)
		throws SBMLException, IOException {
		StringWriter sw = new StringWriter();
		if (spec.isSetStoichiometryMath()) {
			sw.append(math(spec.getStoichiometryMath().getMath().toLaTeX()));
		} else if (spec.getStoichiometry() != 1d) {
			sw.append(format(spec.getStoichiometry()));
		}
		sw.close();
		return sw.getBuffer();
	}

	/**
	 * Colores a velocity if necessary.
	 * 
	 * @param reaction
	 * @param k
	 * @param notSubstancePerTimeUnit
	 * @param notExistingKineticLaw
	 * @param equationBW
	 * @throws IOException
	 * @throws SBMLException
	 */
	private void formatVelocity(Reaction reaction, int k,
		boolean notSubstancePerTimeUnit, boolean notExistingKineticLaw,
		BufferedWriter equationBW) throws IOException, SBMLException {
		StringBuffer v = new StringBuffer();
		if (printFullODEsystem) {
			v.append("\\underbrace{");
			if ((reaction == null) || !reaction.isSetKineticLaw()) {
				v.append(formatter.mathText(bundleContent.getString("NO_MATH_SPECIFIED")));
			} else {
				v.append(reaction.getKineticLaw().getMath().toLaTeX());
			}
			v.append("}_{");
		}
		v.append("v_{");
		v.append(Integer.toString(k));
		v.append('}');
		if (notSubstancePerTimeUnit) {
			v = colorbox("lightgray", math(v));
		} else if (notExistingKineticLaw) {
			v = textcolor("red", v);
		}
		if (printFullODEsystem) {
			v.append('}');
		}
		equationBW.append(hyperref("v" + Integer.toString(k), v));
	}

	/**
	 * If the field printNameIfAvailable is {@code false} this method returns a the id of
	 * the given SBase. If printNameIfAvailable is {@code true} this method looks for the
	 * name of the given SBase and will return it.
	 * 
	 * @param sbase
	 *        the {@link SBase}, whose name or id is to be returned.
	 * @param mathMode
	 *        if {@code true} this method returns the name typesetted in mathmode, i.e.,
	 *        mathrm for names and mathtt for ids, otherwise texttt will be used
	 *        for ids and normalfont (nothing) will be used for names.
	 * @return The name or the ID of the SBase (according to the field
	 *         printNameIfAvailable), whose LaTeX special symbols are masked and
	 *         which is type set in typewriter font if it is an id. The mathmode
	 *         argument decides if mathtt or mathrm has to be used.
	 */
	private StringBuffer getNameOrID(NamedSBase sbase, boolean mathMode) {
		boolean isName = printNameIfAvailable && sbase.isSetName();
		String name = maskSpecialChars(isName ? sbase.getName() : sbase.getId());
		if (!isName) { 
			return mathMode ? mathtt(name) : texttt(name); 
		}
		return mathMode ? mathrm(name) : new StringBuffer(name);
	}

	/**
	 * This method returns the correct LaTeX expression for a function which
	 * returns the size of a compartment. This can be a volume, an area, a length
	 * or a point.
	 */
	private StringBuffer getSize(Compartment c) {
		StringBuffer value;
		int spatialDim = (int) c.getSpatialDimensions();
		if (spatialDim != c.getSpatialDimensions()) {
			logger.warning(MessageFormat.format(
			  bundleUI.getString("NON_INTEGER_SPATIAL_DIMENSIONS"),
			  c.isSetName() ? c.getName() : c.getId(),
			  c.getSpatialDimensions()));
		}
		switch (spatialDim) {
			case 3:
				value = mathrm("vol");
				break;
			case 2:
				value = mathrm("area");
				break;
			case 1:
				value = mathrm("length");
				break;
			default:
				value = mathrm("point");
				break;
		}
		value.append(brackets(getNameOrID(c, true)));
		return value;
	}

	/**
	 * Creates a subsection for the given problem class.
	 * 
	 * @param listOfErrorIndices
	 *        A list containing indices of document errors.
	 * @param doc
	 *        The SBML document containing the problems
	 * @param title
	 *        The title of a subsection for the problem class.
	 * @param buffer
	 *        the writer
	 * @param messageType
	 *        An identifier, e. g., "Error" or "Problem" or "Information" etc.
	 * @throws IOException
	 */
	private void problemMessage(List<Integer> listOfErrorIndices,
		SBMLDocument doc, String title, BufferedWriter buffer, String messageType)
		throws IOException {
		buffer.append(subsection(title, true));
		buffer.append(MessageFormat.format(bundleContent
				.getString("SBML_DOCUMENT_PROBLEM_DESCRIPTION"), MessageFormat.format(
			bundleContent.getString("NUMERALS"), listOfErrorIndices.size()), title
				.startsWith("XML") ? title : firstLetterLowerCase(title)));
		buffer.newLine();
		buffer.append(descriptionBegin);
		Integer[] errors = listOfErrorIndices.toArray(new Integer[0]);
		Arrays.sort(errors);
		SBMLError error = null;
		StringBuffer message = new StringBuffer();
		for (Integer e : errors) {
			SBMLError curr = doc.getError(e.intValue());
			if (!curr.equals(error)) {
				error = curr;
				message = formatErrorMessage(error.getMessage());
			}
			buffer.append(descriptionItem(
				messageType + ' ' + Integer.toString(error.getErrorId()),
				message.toString()));
		}
		buffer.append(descriptionEnd);
		buffer.newLine();
	}

	/**
	 * This method returns a {@link StringBuffer} containing the reaction equation for the
	 * given reaction. Note that this equation has to be surrounded by a
	 * 
	 * <pre>
	 * \ce{...}
	 * </pre>
	 * 
	 * tag to be displayed in LaTeX.
	 * 
	 * @param r
	 * @return
	 * @throws IOException
	 * @throws SBMLException
	 */
	public StringBuffer reactionEquation(Reaction r) throws IOException,
		SBMLException {
		int i;
		StringBuffer reactString = new StringBuffer();
		if (r.getReactantCount() == 0) {
			reactString.append(math(formatter.emptySet()));
		} else {
			for (i = 0; i < r.getReactantCount(); i++) {
				if (r.getReactant(i) == null) {
					reactString.append(math(formatter.mathText(
						MessageFormat.format(bundleContent.getString("INVALID_SPECIES_REFERENCE_FOR_REACTION_PARTICIPANT"),
							bundleContent.getString("REACTANT"),
							MessageFormat.format(bundleContent.getString("NUMERALS"), i + 1)))));
				} else {
					reactString.append(formatStoichiometry(r.getReactant(i)));
					reactString.append(' ');
					reactString.append(math(getNameOrID(
						r.getModel().getSpecies(r.getReactant(i).getSpecies()), true)));
				}
				if (i < r.getReactantCount() - 1) {
					reactString.append(" + ");
				}
			}
		}
		reactString.append(r.getReversible() ? " <=>" : " ->");
		if (r.getModifierCount() > 0) {
			reactString.append("[\\text{");
			reactString.append(math(getNameOrID(r.getModifier(0).getSpeciesInstance(), true)));
			for (i = 1; i < r.getModifierCount(); i++) {
				reactString.append(",");
				reactString.append(formatter.smallSpace());
				reactString.append(math(getNameOrID(r.getModifier(i).getSpeciesInstance(), true)));
			}
			reactString.append("}] ");
		} else {
			reactString.append(' ');
		}
		if (r.getProductCount() == 0) {
			reactString.append(math(formatter.emptySet()));
		} else {
			for (i = 0; i < r.getProductCount(); i++) {
				if (r.getProduct(i) == null) {
					reactString.append(math(formatter.mathText(
						MessageFormat.format(bundleContent.getString("INVALID_SPECIES_REFERENCE_FOR_REACTION_PARTICIPANT"),
							bundleContent.getString("PRODUCT"),
							MessageFormat.format(bundleContent.getString("NUMERALS"), i + 1)))));
				} else {
					reactString.append(formatStoichiometry(r.getProduct(i)));
					reactString.append(' ');
					reactString.append(math(getNameOrID(
						r.getModel().getSpecies(r.getProduct(i).getSpecies()), true)));
				}
				if (i < r.getProductCount() - 1) reactString.append(" + ");
			}
		}
		return reactString;
	}

	/**
	 * Checks whether a unit with the given kind is one of the base units (given
	 * SBML Level and Version from the model) and returns the corresponding LaTeX
	 * formated unit string or "Unknown unit" if the given kind string cannot be
	 * mapped to any known unit.
	 * 
	 * @param kind
	 * @param model
	 * @return
	 */
	private StringBuffer unitTest(String kind, Model model) {
		if (Unit.isUnitKind(kind, model.getLevel(), model.getVersion())) {
			Unit u = new Unit(model.getLevel(), model.getVersion());
			u.setKind(Unit.Kind.valueOf(kind));
			return format(u);
		} else if (Unit.isPredefined(kind.toLowerCase(), model.getLevel())) {
			return format(UnitDefinition.getPredefinedUnit(kind.toLowerCase(),
				model.getLevel(), model.getVersion()));
		} else {
			UnitDefinition ud = model.getUnitDefinition(kind);
			if (ud != null) {
				return format(ud);
			}
		}
		return new StringBuffer(MessageFormat.format(bundleContent.getString("UNKNOWN_UNIT"), kind));
	}
	
	/**
	 * @param sbase
	 * @return
	 * @throws SBMLException
	 * @throws IOException
	 */
	public StringBuffer toLaTeX(Reaction reaction) throws IOException,
		SBMLException {
		return format(reaction, reaction.getParent().getIndex(reaction));
	}

	/**
	 * Writes a report for the given {@link Model} to the given {@link File} if
	 * this is a TeX file.
	 * 
	 * @param model
	 * @param file
	 * @throws IOException
	 * @throws SBMLException
	 */
	public void toLaTeX(Model model, File file) throws IOException, SBMLException {
		if (!SBFileFilter.isTeXFile(file)) { 
		  throw new IOException(MessageFormat.format(bundleUI.getString("INVALID_LATEX_FILE"), file)); 
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		format(model, bw);
		bw.close();
	}

	/**
	 * 
	 * @param printFullODEsystem
	 */
	public void setPrintFullODEsystem(boolean printFullODEsystem) {
		this.printFullODEsystem = printFullODEsystem;
	}

}
