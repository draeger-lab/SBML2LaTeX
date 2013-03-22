/*
 * $Id: LaTeXReportGenerator.java 82 2011-12-13 11:43:28Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/tags/version0.9.9/src/org/sbml/tolatex/io/LaTeXReportGenerator.java $
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates 
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2008-2011 by the University of Tuebingen, Germany.
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.Assignment;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Creator;
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
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.Trigger;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.Variable;
import org.sbml.jsbml.util.NotImplementedException;
import org.sbml.jsbml.util.StringTools;
import org.sbml.jsbml.util.filters.NameFilter;
import org.sbml.jsbml.validator.OverdeterminationValidator;
import org.sbml.tolatex.util.LaTeX;

import cz.kebrt.html2latex.HTML2LaTeX;
import de.zbit.io.SBFileFilter;
import de.zbit.sbml.io.SBOTermFormatter;

/**
 * This class generates LaTeX reports for given SBML files.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 2010-10-22
 * @version $Rev: 82 $
 */
@SuppressWarnings("deprecation")
public class LaTeXReportGenerator extends LaTeX implements SBMLReportGenerator {
	
	/**
	 * The location of the SBML2LaTeX logo file.
	 */
	private static String logo;
	
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
	 * This converts HTML formatation tags into associated LaTeX format
	 * assignments.
	 * 
	 * @param note
	 * @return
	 * @throws IOException
	 */
	private static StringBuffer formatHTML(String note) throws IOException {
		StringWriter st = new StringWriter();
		BufferedWriter bw = new BufferedWriter(st);
		BufferedReader br = new BufferedReader(new StringReader(note));
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
	 * The default is a4.
	 */
	private String paperSize;
	
	/**
	 * This variable is needed to decide whether a method should write a document
	 * head and tail for the LaTeX output.
	 */
	private boolean headTail;
	
	/**
	 * If true species (reactants, modifiers and products) in reaction equations
	 * will be displayed with their name if they have one. By default the ids of
	 * the species are used in these equations.
	 */
	private boolean printNameIfAvailable;
	
	/**
	 * If true this will produce LaTeX files for for entirely landscape documents
	 */
	private boolean landscape;
	
	/**
	 * If true ids are set in typewriter font (default).
	 */
	private boolean typewriter;
	
	/**
	 * If true predefined SBML units will be made explicitly if not overridden in
	 * the model.
	 */
	private boolean showPredefinedUnitDeclarations;
	
	/**
	 * If true a title page will be created by LaTeX for the resulting document.
	 * Otherwise there will only be a title on top of the first page.
	 */
	private boolean titlepage;
	
	/**
	 * If true a section of all errors found in the SBML file are printed at the
	 * end of the document
	 */
	private boolean checkConsistency = false;
	
	/**
	 * If true MIRIAM annotations are included into the model report. This process
	 * takes a bit time due to the necessary connection to EBI's web-service.
	 */
	private boolean includeMIRIAM = false;
  
  /**
   * These options determine which sections should be included when writing a
   * report. By default, these are all <code>true</code>.
   */
  private boolean includeUnitDefinitionsSection,
      includeCompartmentTypesSection, includeCompartmentsSection,
      includeSpeciesTypesSection, includeSpeciesSection,
      includeParametersSection, includeInitialAssignmentsSection,
      includeFunctionDefinitionsSection, includeRulesSection,
      includeEventsSection, includeConstraintsSection, includeReactionsSection;
	
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
	 * products are presented in each reaction. If true a table is created
	 * containing the identifiers of each reactant, modifier and product together
	 * with the respective name. If false (default), a subsection for each one of
	 * the three groups of participants is created giving all details of each
	 * participant.
	 */
	private boolean arrangeReactionParticipantsInOneTable = false,
			printFullODEsystem = false;
	
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
		this(false, true, (short) 11, "a4", true, false, false);
	}
	
	/**
	 * Constructs a new instance of LaTeX export. For each document to be
	 * translated a new instance has to be created. This constructor allows you to
	 * set many properties of the resulting LaTeX file.
	 * 
	 * @param landscape
	 *        If <code>true</code> the whole document will be set to landscape
	 *        format, otherwise portrait.
	 * @param typeWriter
	 *        If <code>true</code> ids are set in typewriter font (default).
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
	 *        If true predefined SBML units will be made explicitly if not
	 *        overridden in the model.
	 * @param titlepage
	 *        if true a title page will be created for the model report. Default
	 *        is false (just a caption).
	 */
	public LaTeXReportGenerator(boolean landscape, boolean typeWriter,
		short fontSize, String paperSize, boolean addPredefinedUnits,
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
		setIncludeUnitDefinitionsSection(true);
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
	 * Returns true if the abstract syntax tree contains a node with the given
	 * name or id. To this end, the AST is traversed recursively.
	 * 
	 * @param math
	 * @param id
	 * @return
	 */
	public boolean contains(ASTNode math, String id) {
		if ((math.getType() == ASTNode.Type.NAME) && (math.getName().equals(id))) { return true; }
		for (int i = 0; i < math.getChildCount(); i++) {
			if (contains(math.getChild(i), id)) { return true; }
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jcell2.client.io.DisplaySBML#format(org.sbml.libsbml.ListOf,
	 * java.lang.String, java.io.BufferedWriter, boolean)
	 */
	public void format(ListOf<? extends SBase> list, BufferedWriter buffer,
		boolean section) throws IOException, SBMLException {
		if (list.isEmpty()) { return; }
		int i;
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
			String name = list.get(0).getElementName().toLowerCase();
			boolean setLandscape = false;
			if (paperSize.equals("executive") || paperSize.equals("legal")
					|| paperSize.equals("letter")) {
				setLandscape = true;
			} else {
				short size = Short.parseShort(Character.toString(paperSize.charAt(1)));
				char variant = paperSize.charAt(0);
				if ((size >= 4)
						&& ((variant == 'a') || (variant == 'b') || (variant == 'c') || (variant == 'd'))) {
					setLandscape = true;
				}
			}
			setLandscape = setLandscape && !landscape;
			if (species || reactions) {
				if (setLandscape) {
					buffer.newLine();
					buffer.append("\\begin{landscape}");
				}
				buffer.newLine();
			} else if (list.getSBaseListType().equals(ListOf.Type.listOfRules)) {
				name = "rule";
			} else if (list.getSBaseListType().equals(
				ListOf.Type.listOfFunctionDefinitions)) {
				name = "function definition";
			} else if (name.endsWith("type")) {
				name = name.substring(0, name.length() - 4) + " type";
			}
			
			if (section) {
				buffer.append(section(firstLetterUpperCase(name)
						+ ((list.size() > 1) && (!name.endsWith("s")) ? "s" : ""), true));
				if (compartments || species || reactions || parameters) {
					buffer.append("This model contains ");
				} else {
					buffer.append("This is an overview of ");
				}
				buffer.append(getWordForNumber(list.size()));
				if (parameters) {
					buffer.append(" global");
				}
				buffer.append(name.charAt(0) == ' ' ? name : " " + name);
				buffer.append((list.size() > 1) && (!name.endsWith("s")) ? "s." : ".");
				buffer.newLine();
				if (species) {
					if (list.getModel().getNumSpeciesWithBoundaryCondition() > 0) {
						buffer.append("The boundary condition of ");
						buffer.append(getWordForNumber(list.getModel()
								.getNumSpeciesWithBoundaryCondition()));
						buffer.append(" of these species is set to ");
						buffer.append(texttt("true"));
						buffer.append(" so that th");
						buffer
								.append(list.getModel().getNumSpeciesWithBoundaryCondition() > 1 ? "ese"
										: "is");
						buffer
								.append(" species' amount cannot be changed by any reaction. ");
					}
					buffer.append("Section~\\ref{sec:DerivedRateEquations} ");
					buffer.append("provides further details and the derived ");
					buffer.append("rates of change of each species.");
					buffer.newLine();
				}
			}
			
			if (compartments) {
				buffer.append(longtableHead("@{}lllC{2cm}llcl@{}",
					"Properties of all compartments.",
					"Id&Name&SBO&Spatial Dimensions&Size&Unit&Constant&Outside"));
			} else if (species) {
				buffer
						.append(longtableHead(
							paperSize.equals("letter") || paperSize.equals("executive") ? "@{}p{3.5cm}p{6cm}p{4.5cm}p{2.5cm}C{1.5cm}C{1.5cm}@{}"
									: "@{}p{3.5cm}p{6.5cm}p{5cm}p{3cm}C{1.5cm}C{1.5cm}@{}",
							"Properties of each species.",
							"Id&Name&Compartment&Derived Unit&Constant&Boundary Condition"));
			} else if (reactions) {
				buffer.append(" All reactions are listed in the following");
				buffer.append(" table and are subsequently described in");
				buffer.append(" detail. If a reaction is affected by one");
				buffer.append(" or more modifiers, the ");
				if (printNameIfAvailable) {
					buffer.append("names or---if not specified---the");
				}
				buffer.append(" identifiers of the modifier species are");
				buffer.append(" written above the reaction arrow.");
				buffer.newLine();
				buffer
						.append(longtableHead("rp{3cm}p{7cm}p{8cm}p{1.5cm}",
							"Overview of all reactions",
							"\\numero&Id&Name&Reaction Equation&SBO"));
			} else if (parameters) {
				int preDecimal = 1, postDecimal = 1;
				for (i = 0; i < list.size(); i++) {
					String[] value = Double.toString(
						((QuantityWithUnit) list.get(i)).getValue()).split("\\.");
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
				String head;
				if (paperSize.equals("executive")) {
					head = "p{2cm}p{3cm}cR{";
				} else {
					head = "p{2.5cm}p{3cm}cR{";
				}
				head += Integer.toString(preDecimal) + "}{"
						+ Integer.toString(Math.min(postDecimal, 3));
				head += (paperSize.equals("executive")) ? "}p{2.8cm}c" : "}p{3cm}c";
				buffer
						.append(longtableHead(
							head,
							"Properties of each parameter.",
							"\\multicolumn{1}{l}{Id}&Name&SBO&\\multicolumn{1}{c}{Value}&Unit&\\multicolumn{1}{c}{Constant}"));
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
					String value = Double.toString(p.getValue());
					buffer.append(value.contains("E") ? "\\multicolumn{1}{r}{"
							+ format(p.getValue()) + "}" : value);
					buffer.append('&');
					UnitDefinition ud = p.getDerivedUnitDefinition();
					if ((ud == null) || (ud.getNumUnits() == 0)) {
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
					if ((ud == null) || (ud.getNumUnits() == 0)) {
						buffer.append(' ');
					} else if (ud.isVariantOfVolume() && (ud.getNumUnits() == 1)
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
					buffer.append(descriptionItem("Message",
						formatHTML(c.getMessageString()).toString()));
					buffer.append(descriptionItem("Equation", equation(new StringBuffer(c
							.getMath().toLaTeX()))));
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
						for (j = 0; j < s.getModel().getNumSpecies(); j++) {
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
					} else for (j = 0; j < s.getModel().getNumCompartments(); j++) {
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
						buffer.append("This model does not contain any ");
						buffer.append(isSpecType ? "species" : "compartments");
						buffer.append(" of this type.");
					} else {
						buffer.append(longtableHead("@{}ll@{}", (isSpecType ? "Species"
								: "Compartments") + " of this type", "Id&Name"));
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
	private void formatEvents(ListOf<? extends SBase> eventList,
		BufferedWriter buffer) throws IOException, SBMLException {
		if (headTail) {
			documentHead(eventList.getSBMLDocument(), buffer);
		}
		if (eventList.size() > 0) {
			int i, j;
			buffer.append(section((eventList.size() > 1) ? "Events" : "Event", true));
			buffer.append("This is an overview of ");
			buffer.append(getWordForNumber(eventList.size()));
			buffer.append(" event");
			buffer.append(eventList.size() == 1 ? "." : "s.");
			buffer.append(" Each event is initiated whenever its");
			buffer.append(" trigger condition switches from ");
			buffer.append(texttt("false"));
			buffer.append(" to ");
			buffer.append(texttt("true"));
			buffer.append(". A delay function postpones the effects");
			buffer.append(" of an event to a later time point.");
			buffer.append(" At the time of execution, an event");
			buffer.append(" can assign values to species, ");
			buffer.append(" parameters or compartments if these");
			buffer.append(" are not set to constant.");
			buffer.newLine();
			
			Event ev;
			String var;
			for (i = 0; i < eventList.size(); i++) {
				ev = (Event) eventList.get(i);
				if (ev.isSetId()) {
					buffer.append(subsection(
						"Event " + texttt(maskSpecialChars(ev.getId())), true));
					buffer.append(label("event" + ev.getId()));
				} else {
					buffer.append(subsection("Event without an identifier", true));
					buffer.append(label("event" + i));
				}
				buffer.newLine();
				buffer.append(descriptionBegin);
				format(ev, buffer, true);
				buffer.append(descriptionItem("Trigger", format(ev.getTrigger())));
				if (ev.isSetPriority()) {
					buffer.append(descriptionItem("Priority", format(ev.getPriority())));
				}
				if (ev.isSetDelay()) {
					buffer.append(descriptionItem("Delay", equation(ev.getDelay()
							.getMath().toLaTeX())));
					UnitDefinition ud = ev.getDelay().getDerivedUnitDefinition();
					if ((ud != null) && (ud.getNumUnits() > 0)) {
						buffer.append(descriptionItem("Time unit of the delay",
							math(format(ud))));
					}
				}
				String item = "Assignment";
				if (1 < ev.getNumEventAssignments()) item += 's';
				StringBuffer description = new StringBuffer();
				if (ev.getUseValuesFromTriggerTime()) {
					description.append("The values of the assinment formula");
					if (1 < ev.getNumEventAssignments()) {
						description.append("s are");
					} else {
						description.append(" is");
					}
					description.append(" computed at the moment this event ");
					description.append("fires");
					if (ev.isSetDelay()) {
						description.append(", not after the delay");
					}
					description.append('.');
				} else {
					description.append("The formula");
					if (1 < ev.getNumEventAssignments()) {
						description.append('s');
					}
					description.append(" in this event's assignment ");
					description.append(1 < ev.getNumEventAssignments() ? "are" : "is");
					description.append(" to be computed");
					if (ev.isSetDelay()) {
						description.append(" after the delay,");
					}
					description.append(" at the time the event is executed.");
				}
				if (ev.getNumEventAssignments() > 1) {
					description.append(newLine());
					description.append("\\begin{align}");
				} else {
					description.append(eqBegin);
				}
				for (j = 0; j < ev.getNumEventAssignments(); j++) {
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
					description
							.append((ev.getNumEventAssignments() > 1) ? " =& " : " = ");
					description.append(ev.getEventAssignment(j).getMath().toLaTeX());
					if (j < ev.getNumEventAssignments() - 1) {
						description.append(lineBreak);
					}
				}
				if (ev.getNumEventAssignments() == 1) {
					description.append(eqEnd);
				} else {
					description.append("\\end{align}");
					description.append(newLine());
				}
				buffer.append(descriptionItem(item, description));
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
	 * @param priority
	 * @return
	 * @throws SBMLException
	 */
	private String format(Priority priority) throws SBMLException {
		StringBuilder sb = new StringBuilder();
		sb.append("This event is ranked with the following weight:");
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
			sb.append("This trigger can");
			if (trigger.isInitialValue()) {
				sb.append("not");
			}
			sb.append(" fire at the beginning of a simulation");
			if (trigger.isInitialValue()) {
				sb.append(", even ");
			}
			sb.append("if its condition evaluates to true at time ");
			sb.append(math("t = 0"));
			sb.append('.');
			sb.append(newLine());
		}
		if (trigger.isSetPersistent()) {
			sb.append("If in the time between triggering and ");
			sb.append("finally executing the event, the trigger  ");
			sb.append("condition switches back to ");
			sb.append(texttt("false"));
			sb.append(" this trigger is ");
			if (trigger.isPersistent()) {
				sb.append("not affected and the event is regularly");
			} else {
				sb.append("switched off again and the event is not");
			}
			sb.append(" executed.");
			sb.append(newLine());
		}
		sb.append("The following condition decides whether this ");
		sb.append("trigger may fire:");
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
		ListOf<UnitDefinition> lud = new ListOf<UnitDefinition>(m.getLevel(),
			m.getVersion());
		for (int i = 0; i < listOfUnits.size(); i++) {
			lud.add((UnitDefinition) listOfUnits.get(i));
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
			buffer.append(section(lud.size() > 1 ? "Unit Definitions"
					: "Unit Definition", true));
			buffer.append("This is an overview of ");
			buffer.append(getWordForNumber(lud.size()));
			buffer.append(" unit definition");
			if (lud.size() > 1) {
				buffer.append('s');
			}
			buffer.append('.');
			buffer.newLine();
			if (0 < defaults.size()) {
				if (defaults.size() < lud.size()) {
					buffer.append("The unit");
					if (defaults.size() > 1) {
						buffer.append('s');
					}
					buffer.append(' ');
					for (int i = 0; i < defaults.size(); i++) {
						if ((0 < i) && (i < defaults.size() - 1)) {
							buffer.append(", ");
						} else if (i == defaults.size() - 1) {
							if (2 < defaults.size()) {
								buffer.append(',');
							}
							buffer.append(" and ");
						}
						buffer.append(texttt(defaults.get(i)));
					}
					buffer.append(defaults.size() > 1 ? " are " : " is ");
				} else {
					buffer.append("All units are ");
				}
				buffer.append("predefined by SBML and not");
				buffer.append(" mentioned in the model.");
			}
			for (int i = 0; i < lud.size(); i++) {
				def = (UnitDefinition) lud.get(i);
				buffer.append(subsection("Unit "
						+ texttt(maskSpecialChars(def.getId())), true));
				buffer.append(descriptionBegin);
				format(def, buffer, true);
				if (def.getNumUnits() > 0) {
					buffer.append(descriptionItem("Definition", math(format(def))));
				}
				buffer.append(descriptionEnd);
			}
		}
		if (headTail) {
			documentFoot(lud, buffer);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jcell.client.io.DisplaySBML#format(org.sbml.libsbml.Model,
	 * java.io.BufferedWriter)
	 */
	public void format(Model model, BufferedWriter buffer) throws IOException,
		SBMLException {
		if (headTail) {
			documentHead(model.getSBMLDocument(), buffer);
			// buffer.append("\\tableofcontents");
			buffer.newLine();
			buffer.append(section("General Overview", true));
		}
		if (model.isSetSBOTerm()) {
			sboTerms.add(Integer.valueOf(model.getSBOTerm()));
			buffer.append("The SBO concept of this model is a");
			String sboModelName = maskSpecialChars(correctQuotationMarks(
				SBO.getTerm(model.getSBOTerm()).getName(), leftQuotationMark,
				rightQuotationMark));
			buffer.append(isVocal(sboModelName.charAt(0)) ? "n " : " ");
			buffer.append(sboModelName);
			buffer.append(". Its SBO term is ");
			buffer.append(SBO.sboNumberString(model.getSBOTerm()));
			buffer.append(". See Section~\\ref{sec:glossary} for the definition.");
			buffer.newLine();
		}
		
		validator = new OverdeterminationValidator(model);
		
		// buffer.append(subsection("Model History", false));
		formatHistory(model, buffer);
		
		buffer.append("Table~\\ref{tab:components} ");
		double random = Math.random();
		if (random <= 0.33) {
			buffer.append("provides");
		} else if (random <= 0.66) {
			buffer.append("shows");
		} else {
			buffer.append("gives");
		}
		buffer
				.append(" an overview of the quantities of all components of this model.");
		buffer.newLine();
		buffer.append("\\begin{table}[h!]");
		buffer.newLine();
		buffer.append("\\centering");
		buffer.newLine();
		buffer.append("\\caption{The SBML components in this model.}");
		buffer.append(label("tab:components"));
		buffer.newLine();
		buffer.append("All components are described in more detail ");
		buffer.append("in the following sections.");
		buffer.newLine();
		// buffer.append("\\begin{tabular}{C{2cm}ccC{2cm}ccccC{2cm}}");
		buffer.append("\\begin{tabular}{l|r||l|r}");
		buffer.append(toprule);
		buffer
				.append("\\multicolumn{1}{c}{Element}&\\multicolumn{1}{|c||}{Quantity}&");
		buffer
				.append("\\multicolumn{1}{c|}{Element}&\\multicolumn{1}{c}{Quantity}");
		buffer.append(lineBreak);
		buffer.append(midrule);
		buffer.append("compartment types&");
		buffer.append(Integer.toString(model.getNumCompartmentTypes()));
		buffer.append("&compartments&");
		buffer.append(Integer.toString(model.getNumCompartments()));
		buffer.append(lineBreak);
		buffer.append("species types&");
		buffer.append(Integer.toString(model.getNumSpeciesTypes()));
		buffer.append("&species&");
		buffer.append(Integer.toString(model.getNumSpecies()));
		buffer.append(lineBreak);
		buffer.append("events&");
		buffer.append(Integer.toString(model.getNumEvents()));
		buffer.append("&constraints&");
		buffer.append(Integer.toString(model.getNumConstraints()));
		buffer.append(lineBreak);
		buffer.append("reactions&");
		buffer.append(Integer.toString(model.getNumReactions()));
		buffer.append("&function definitions&");
		buffer.append(Integer.toString(model.getNumFunctionDefinitions()));
		buffer.append(lineBreak);
		buffer.append("global parameters&");
		buffer.append(Integer.toString(model.getNumParameters()));
		buffer.append("&unit definitions&");
		buffer.append(Integer.toString(model.getNumUnitDefinitions()));
		buffer.append(lineBreak);
		buffer.append("rules&");
		buffer.append(Integer.toString(model.getNumRules()));
		buffer.append("&initial assignments&");
		buffer.append(Integer.toString(model.getNumInitialAssignments()));
		buffer.append(lineBreak);
		buffer.append("\\bottomrule\\end{tabular}");
		buffer.append(lineBreak);
		buffer.append("\\end{table}");
		buffer.append(lineBreak);
		
		if (model.isSetNotes()) {
			buffer.append(subsection("Model Notes", false));
			buffer.append(formatHTML(model.getNotesString()));
			buffer.newLine();
		}
		
		if ((model.getNumCVTerms() > 0) && (includeMIRIAM)) {
			buffer.append(subsection("Model Annotation", false));
			buffer.append("The following resources provide further ");
			buffer.append("information about this model:");
			buffer.newLine();
			buffer.newLine();
			for (int i = 0; i < model.getNumCVTerms(); i++) {
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
    
		if (headTail) {
			documentFoot(model.getSBMLDocument(), buffer);
		}
	}
	
	/**
	 * @param sbase
	 * @param buffer
	 * @throws IOException
	 */
	private void formatHistory(SBase sbase, BufferedWriter buffer)
		throws IOException {
		if (!sbase.isSetHistory()) { return; }
		History history = sbase.getHistory();
		if ((history.getNumCreators() > 0) || (history.isSetCreatedDate())) {
			buffer.append(String.format("This %s was ", sbase.getElementName()));
			if (history.getNumCreators() > 0) {
				buffer.append("created by ");
				if (history.getNumCreators() > 1) {
					buffer.append("the following ");
					buffer.append(getWordForNumber(history.getNumCreators()));
					buffer.append(" authors: ");
				}
				for (int i = 0; i < history.getNumCreators(); i++) {
					if (history.getNumCreators() > 1
							&& (i == history.getNumCreators() - 1)) {
						if (1 < i) buffer.append(',');
						buffer.append(" and ");
					} else if (i > 0) buffer.append(", ");
					format(history.getCreator(i), buffer);
				}
				buffer.newLine();
			}
			if (history.isSetCreatedDate()) {
				buffer.append("at ");
				format(history.getCreatedDate(), buffer);
				if (history.isSetModifiedDate()) {
					buffer.append(" and ");
				}
			}
		}
		if (history.isSetModifiedDate()) {
			buffer.append("last modified at ");
			format(history.getModifiedDate(), buffer);
		}
		if ((history.getNumCreators() > 0)
				&& !(history.isSetCreatedDate() || history.isSetModifiedDate())) {
			buffer.append('.');
		}
		buffer.newLine();
		buffer.newLine();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jcell.client.io.DisplaySBML#format(org.sbml.libsbml.SBMLDocument,
	 * java.io.BufferedWriter)
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
		buffer.append(section("General Overview", true));
		buffer.append("This is a document in SBML Level ");
		buffer.append(Integer.toString(doc.getLevel()));
		buffer.append(" Version ");
		buffer.append(Integer.toString(doc.getVersion()));
		buffer.append(" format. ");
		format(doc, buffer, false);
		if (doc.isSetNotes()) {
			buffer.append(subsection("Document Notes", false));
			buffer.append(formatHTML(doc.getNotesString()));
			buffer.newLine();
		}
		
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
		for (int j = 0; j < def.getNumUnits(); j++) {
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
	public String getPaperSize() {
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
	 * @return true if implicitly declared units should be made explicit.
	 */
	public boolean isAddPredefinedUnitDeclarations() {
		return showPredefinedUnitDeclarations;
	}
	
	/**
	 * This switch allows to change the way how the reactants, modifiers and
	 * products are presented in each reaction. If true, one table is created
	 * containing the identifiers of each reactant, modifier and product together
	 * with the respective name. If false (default), a subsection for each one of
	 * the three groups of participants is created giving all details of each
	 * participant.
	 * 
	 * @return The state of this switch.
	 */
	public boolean isArrangeReactionParticipantsInOneTable() {
		return arrangeReactionParticipantsInOneTable;
	}
	
	/**
	 * If this method returns true, this exporter performs a consistency check of
	 * the given SBML file and writes all errors and warnings found to at the end
	 * of the document.
	 * 
	 * @return
	 */
	public boolean isCheckConsistency() {
		return checkConsistency;
	}
	
	/**
	 * @return true if landscape format for the whole document is to be used.
	 */
	public boolean isLandscape() {
		return landscape;
	}
	
	/**
	 * @return true if names instead of ids are displayed in formulas and reaction
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
	 * @return true if an extra title page is created false otherwise.
	 */
	public boolean isSetTitlepage() {
		return titlepage;
	}
	
	/**
	 * @return true if ids are written in type writer font.
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
	 * products are presented in each reaction. If true, one table is created
	 * containing the identifiers of each reactant, modifier and product together
	 * with the respective name. If false (default), a subsection for each one of
	 * the three groups of participants is created giving all details of each
	 * participant.
	 * 
	 * @param arrangeReactionParticipantsInOneTable
	 *        True if the participants of the reactions should be arranged in
	 *        small tables, false if a subsection should be created for the three
	 *        groups.
	 */
	public void setArrangeReactionParticipantsInOneTable(
		boolean arrangeReactionParticipantsInOneTable) {
		this.arrangeReactionParticipantsInOneTable = arrangeReactionParticipantsInOneTable;
	}
	
	/**
	 * If set to true, an SBML consistency check of the document is performed and
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
			System.err.println("Unsupported font " + fontHeadings + ". Using "
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
	 * If true is given the whole document will be created in landscape mode.
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
	 * The default is a4.
	 */
	public void setPaperSize(String paperSize) {
		paperSize = paperSize.toLowerCase();
		if (paperSize.equals("letter") || paperSize.equals("legal")
				|| paperSize.equals("executive"))
			this.paperSize = paperSize;
		else if (paperSize.length() == 2) {
			if (!Character.isDigit(paperSize.charAt(1))
					|| ((paperSize.charAt(0) != 'a') && (paperSize.charAt(0) != 'b')
							&& (paperSize.charAt(0) != 'c') && (paperSize.charAt(0) != 'd')))
				this.paperSize = "a4";
			else {
				short size = Short.parseShort(Character.toString(paperSize.charAt(1)));
				if ((0 <= size) && (size < 10))
					this.paperSize = paperSize;
				else this.paperSize = "a4";
			}
		} else this.paperSize = "a4";
		this.paperSize = paperSize;
	}
	
	/**
	 * If true species (reactants, modifiers and products) in reaction equations
	 * will be displayed with their name if they have one. By default the ids of
	 * the species are used in these equations.
	 */
	public void setPrintNameIfAvailable(boolean printNameIfAvailable) {
		this.printNameIfAvailable = printNameIfAvailable;
	}
	
	/**
	 * If true predefined SBML units will be made explicitly if not overridden in
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
		if (fontText.equals("times"))
			fontText = "mathptmx";
		else if (fontText.equals("computer modern roman"))
			fontText = "cmr";
		else if (fontText.equals("palatino"))
			fontText = "mathpazo";
		else if (fontText.equals("zapf"))
			fontText = "chancery";
		else if (fontText.equals("new century schoolbook")) fontText = "newcent";
		if (fontText.equals("bookman") || fontText.equals("chancery")
				|| fontText.equals("charter") || fontText.equals("cmr")
				|| fontText.equals("mathpazo") || fontText.equals("mathptmx")
				|| fontText.equals("newcent") || fontText.equals("utopia"))
			this.fontText = fontText;
		else System.err.println("Unsupported font " + fontText + ". Using "
				+ this.fontText + ".");
	}
	
	/**
	 * If true an extra title page is created. Default false.
	 * 
	 * @param titlepage
	 */
	public void setTitlepage(boolean titlepage) {
		this.titlepage = titlepage;
	}
	
	/**
	 * If true identifiers are set in typewriter font (default).
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
		if (fontTypewriter.equals("courier") || fontTypewriter.equals("cmt"))
			this.fontTypewriter = fontTypewriter;
		else System.err.println("Unsupported font " + fontTypewriter + ". Using "
				+ this.fontTypewriter + ".");
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
			buffer.append("This list of ");
			buffer.append(listOf.getElementName());
			buffer.append(' ');
			buffer.append("does not contain any entries.");
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
		buffer.append("\\appendix");
		buffer.newLine();
		if (checkConsistency) {
			boolean notImplemented = false;
			try {
				doc.checkConsistency();
			} catch (NotImplementedException exc) {
				notImplemented = true;
			}
			if ((doc.getNumErrors() > 0) || notImplemented) {
				int i;
				SBMLError error;
				Vector<Integer> infos = new Vector<Integer>();
				Vector<Integer> warnings = new Vector<Integer>();
				Vector<Integer> fatal = new Vector<Integer>();
				Vector<Integer> system = new Vector<Integer>();
				Vector<Integer> xml = new Vector<Integer>();
				Vector<Integer> internal = new Vector<Integer>();
				Vector<Integer> errors = new Vector<Integer>();
				for (i = 0; i < doc.getNumErrors(); i++) {
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
				buffer.append(section("Model Consistency Report", true));
				
				if (notImplemented) {
					buffer.append("Currently, the Java\\texttrademark{} ");
					buffer.append("library \\href{http://jsbml.sourceforge.net}");
					buffer.append("{JSBML}, which is used ");
					buffer.append("by the documentation tool \\SBMLLaTeX, does ");
					buffer.append("not provide an implementation for full SBML ");
					buffer.append("consistency checks. Hence, at this position ");
					buffer.append("this report cannot highlight any problems ");
					buffer.append("in the SBML document, which does not mean ");
					buffer.append("that it is entirely correct. Please visit ");
					buffer.append("the website \\href{http://sbml.org}");
					buffer.append("{sbml.org} and use the SBML validator there.");
				} else {
					
					buffer.append("The given SBML document contains ");
					buffer.append(getWordForNumber(doc.getNumErrors()));
					buffer.append(" issue");
					if (doc.getNumErrors() > 1) buffer.append('s');
					buffer.append(", which ");
					buffer.append(doc.getNumErrors() > 1 ? "are" : "is");
					buffer.append(" listed in the remainder of this model report.");
					buffer.newLine();
					buffer.append("The messages and identification codes shown ");
					buffer.append("here are those reported by the ");
					buffer.append(href("http://sbml.org/Facilities/Validator",
						"SBML.org online validator"));
					buffer.append('.');
					buffer.newLine();
					if (xml.size() > 0) {
						problemMessage(xml, doc, xml.size() > 1 ? "XML errors"
								: "XML error", buffer, "Error");
					}
					if (fatal.size() > 0) {
						problemMessage(fatal, doc, fatal.size() > 1 ? "Fatal errors"
								: "Fatal error", buffer, "Error");
					}
					if (system.size() > 0) {
						problemMessage(system, doc, system.size() > 1 ? "System messages"
								: "System message", buffer, "Error");
					}
					if (internal.size() > 0) {
						problemMessage(internal, doc,
							internal.size() > 1 ? "Internal problems" : "Internal problem",
							buffer, "Error");
					}
					if (errors.size() > 0) {
						problemMessage(errors, doc, errors.size() > 1 ? "Error messages"
								: "Error message", buffer, "Error");
					}
					if (infos.size() > 0) {
						problemMessage(
							infos,
							doc,
							infos.size() > 1 ? "Information messages" : "Information message",
							buffer, "Information");
					}
					if (warnings.size() > 0) {
						problemMessage(warnings, doc, warnings.size() > 1 ? "Warnings"
								: "Warning", buffer, "Warning");
					}
				}
			}
		}
		
		if (sboTerms.size() > 0) {
			buffer
					.append(section("Glossary of Systems Biology Ontology Terms", true));
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
					buffer.append(descriptionItem(String.format("%s", term.getId()),
						String.format("\\textbf{%s:} %s%s", name, def, newLine())));
				}
			}
			buffer.append(descriptionEnd);
		}
		buffer.append(imprint());
		buffer.append("\\end{document}");
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
		} else titlePrefix += " ";
		buffer.append("\\documentclass[");
		buffer.append(Short.toString(fontSize));
		buffer.append("pt,twoside,bibtotoc");
		if (titlepage) buffer.append(",titlepage");
		if (landscape) buffer.append(",landscape");
		buffer.append(',');
		buffer.append(paperSize);
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
		buffer.append(space
				+ "pdfauthor={Produced by SBML2LaTeX for JSBML version 1.0beta},");
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
				"{rotating}", "{upgreek}", "{flexisym}", "{breqn}", "{natbib}" };
		for (i = 0; i < packages.length; i++) {
			buffer.append("\\usepackage");
			buffer.append(packages[i]);
			buffer.newLine();
		}
		buffer.newLine();
		buffer.append("\\selectlanguage{english}");
		buffer.newLine();
		if (0 < model.getNumFunctionDefinitions()) {
			buffer.append("% Introduce automatic line breaks in function calls");
			buffer.newLine();
			buffer.append("\\makeatletter");
			buffer.newLine();
			buffer
					.append("\\edef\\breqn@identify@comma{\\number\\symletters3B}% fingers crossed!");
			buffer.newLine();
			buffer.append("\\let\\m@@Pun\\m@Pun");
			buffer.newLine();
			buffer
					.append("\\def\\d@@Pun#1#2#3{\\edef\\breqn@stored@args{\\number#1#2#3}");
			buffer.newLine();
			buffer.append("\\futurelet\\@let@token\\d@@Punaux}");
			buffer.newLine();
			buffer.append("\\def\\d@@Punaux{%");
			buffer.newLine();
			buffer.append("  \\expandafter\\m@@Pun\\breqn@stored@args");
			buffer.newLine();
			buffer.append("  \\ifx\\@let@token\\@sptoken");
			buffer.newLine();
			buffer.append("    \\ifx\\breqn@stored@args\\breqn@identify@comma");
			buffer.newLine();
			// buffer.append("      \\penalty\\breqn@comma@penalty\\relax");
			buffer.append("      \\penalty\\breqn@comma@penalty\\relax");
			buffer.newLine();
			buffer.append("      \\EQ@prebin@space");
			buffer.newLine();
			buffer.append("    \\fi");
			buffer.newLine();
			buffer.append("  \\fi");
			buffer.newLine();
			buffer.append("}");
			buffer.newLine();
			buffer.append("\\def\\display@setup{%");
			buffer.newLine();
			buffer.append("  \\medmuskip\\Dmedmuskip \\thickmuskip\\Dthickmuskip");
			buffer.newLine();
			buffer.append("  \\let\\m@Bin\\d@@Bin \\let\\m@Rel\\d@@Rel");
			buffer.newLine();
			buffer.append("  \\let\\m@Pun\\d@@Pun %% new for punctuation");
			buffer.newLine();
			buffer.append("  \\let\\@symRel\\d@@symRel \\let\\@symBin\\d@@symBin");
			buffer.newLine();
			buffer
					.append("  \\let\\m@DeL\\d@@DeL \\let\\m@DeR\\d@@DeR \\let\\m@DeB\\d@@DeB");
			buffer.newLine();
			buffer.append("  \\let\\m@DeA\\d@@DeA");
			buffer.newLine();
			buffer.append("  \\let\\@symDeL\\d@@symDeL \\let\\@symDeR\\d@@symDeR");
			buffer.newLine();
			buffer.append("  \\let\\@symDeB\\d@@symDeB \\let\\@symDeA\\d@@symDeA");
			buffer.newLine();
			buffer
					.append("  \\let\\left\\eq@left \\let\\right\\eq@right \\global\\lr@level\\z@");
			buffer.newLine();
			buffer.append("  \\global\\eq@wdCond\\z@          %BRM: new");
			buffer.newLine();
			buffer.append("  \\everyhbox{\\everyhbox\\@emptytoks");
			buffer.newLine();
			buffer
					.append("    \\let\\display@setup\\relax \\textmath@setup \\let\\textmath@setup\\relax");
			buffer.newLine();
			buffer.append("  }%");
			buffer.newLine();
			buffer.append("  \\everyvbox{\\everyvbox\\@emptytoks");
			buffer.newLine();
			buffer
					.append("    \\let\\display@setup\\relax \\textmath@setup \\let\\textmath@setup\\relax");
			buffer.newLine();
			buffer.append("  }%");
			buffer.newLine();
			buffer.append("}");
			buffer.newLine();
			buffer
					.append("\\define@key{breqn}{comma-penalty}{\\def\\breqn@comma@penalty{#1}}");
			buffer.newLine();
			buffer
					.append("\\setkeys{breqn}{comma-penalty=5000}% break is the default");
			buffer.newLine();
			buffer.append("\\makeatother");
			buffer.newLine();
			buffer.append("% End line break definition");
			buffer.newLine();
		}
		buffer.newLine();
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
		buffer
				.append("\\newcommand{\\yes}{\\parbox[c]{1.3em}{\\Large\\Square\\hspace{-.65em}\\ding{51}}}");
		buffer.newLine();
		buffer
				.append("\\newcommand{\\no}{\\parbox[c]{1.3em}{\\Large\\Square\\hspace{-.62em}--}}");
		buffer.newLine();
		buffer
				.append("\\newcommand{\\numero}{N\\hspace{-0.075em}\\raisebox{0.25em}{\\relsize{-2}\\b{o}}}");
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
		buffer.append("\\cfoot{\\textcolor{gray}{Produced by \\SBMLLaTeX}}");
		buffer.newLine();
		buffer.newLine();
		buffer.append("\\subject{SBML Model Report}");
		buffer.newLine();
		buffer.append("\\title{");
		buffer.append(titlePrefix);
		if (titlePrefix.contains("identified")) {
			title = texttt(title).toString();
		} else {
			title = "``" + title + "\"";
		}
		buffer.append(title);
		buffer.append('}');
		// buffer.append("}}}}");
		buffer.newLine();
		buffer.append("\\date{\\today}");
		buffer.newLine();
		buffer.append("\\author{");
		buffer.append("\\includegraphics[height=3.5ex]{" + logo + "}}");
		buffer.newLine();
		if (!typewriter) {
			buffer.append("\\urlstyle{same}");
			buffer.newLine();
		}
		buffer.newLine();
		buffer.append("\\begin{document}");
		buffer.newLine();
		buffer.append("\\maketitle");
		buffer.newLine();
		buffer.append("\\thispagestyle{scrheadings}");
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
		buffer.append("This ");
		switch (cv.getQualifierType()) {
			case MODEL_QUALIFIER:
				buffer.append("model ");
				switch (cv.getModelQualifierType()) {
					case BQM_IS:
						buffer.append("is");
						break;
					case BQM_IS_DESCRIBED_BY:
						buffer.append("is described by");
						break;
					default: // unknown
						buffer.append("has something to do with");
						break;
				}
				break;
			case BIOLOGICAL_QUALIFIER:
				buffer.append("biological entity ");
				switch (cv.getBiologicalQualifierType()) {
					case BQB_ENCODES:
						buffer.append("encodes");
						break;
					case BQB_HAS_PART:
						buffer.append("has ");
						buffer.append(resources.size() == 1 ? "a part" : "parts");
						break;
					case BQB_HAS_VERSION:
						buffer.append("has the version");
						break;
					case BQB_IS:
						buffer.append("is");
						break;
					case BQB_IS_DESCRIBED_BY:
						buffer.append("is described by");
						break;
					case BQB_IS_ENCODED_BY:
						buffer.append("is encoded by");
						break;
					case BQB_IS_HOMOLOG_TO:
						buffer.append("is homolog to");
						break;
					case BQB_IS_PART_OF:
						buffer.append("is a part of");
						break;
					case BQB_IS_VERSION_OF:
						buffer.append("is a version of");
						break;
					case BQB_OCCURS_IN:
						buffer.append("occurs in");
						break;
					default: // unknown
						buffer.append("has something to do with");
						break;
				}
				break;
			default: // UNKNOWN_QUALIFIER
				buffer.append("element has something to do with");
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
		int minute = calendar.get(Calendar.MINUTE);
		int hour = calendar.get(Calendar.HOUR);
		int day = calendar.get(Calendar.DATE);
		// short month = (short) calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		
		// TODO! Change everything to use the DateFormat!
		SimpleDateFormat dateFormat = new SimpleDateFormat("'MMMM'");
		
		buffer.append(dateFormat.format(date));
		buffer.append(' ');
		buffer.append(getNumbering(day));
		buffer.append(' ');
		buffer.append(Integer.toString(year));
		buffer.append(" at ");
		if ((minute == 0) || (minute == 60)) {
			if (hour == 12) {
				buffer.append("noon");
			} else if (hour == 24) {
				buffer.append("midnight");
			} else {
				buffer.append(getWordForNumber((hour > 12) ? hour - 12 : hour));
				buffer.append(" o' clock in the ");
				buffer.append(hour > 12 ? "afternoon" : "morning");
			}
			buffer.append('.');
		} else {
			buffer.append(Long.toString((hour > 12) ? hour - 12 : hour));
			buffer.append(':');
			if (minute < 10) {
				buffer.append('0');
			}
			buffer.append(Integer.toString(minute));
			buffer.append(hour > 12 ? "~p.\\,m." : "~a.\\,m.");
		}
		buffer.append(' ');
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
		if ((def.getNumArguments() > 0) || (def.getBody() != null)
				|| def.isSetMath()) {
			if (def.getNumArguments() > 0) {
				buffer.append("\\item[Argument");
				buffer.append(def.getNumArguments() > 1 ? "s] " : "] ");
				for (int j = 0; j < def.getNumArguments(); j++) {
					buffer.append(math(def.getArgument(j).toLaTeX()));
					if (j < def.getNumArguments() - 1) {
						buffer.append(", ");
					}
				}
				buffer.newLine();
				if (def.getBody() != null) {
					buffer.append(descriptionItem("Mathematical Expression",
						equation(new StringBuffer(def.getBody().toLaTeX()))));
				}
			} else if (def.isSetMath()) {
				buffer.append(descriptionItem(
					"Mathematical Formula",
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
			"Derived unit",
			a.containsUndeclaredUnits() ? "contains undeclared units" : math(format(a
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
			buffer.append(subsection(
				"Compartment " + texttt(maskSpecialChars(c.getId())), true));
			buffer.append("This is a");
			int spatialDim = (int) c.getSpatialDimensions();
			String dimension = (spatialDim - c.getSpatialDimensions() == 0d) ? getWordForNumber(spatialDim)
					: StringTools.toString(Locale.ENGLISH, c.getSpatialDimensions());
			if (isVocal(dimension.charAt(0))) buffer.append('n');
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
			if (!c.getConstant()) buffer.append("not ");
			buffer.append("constant ");
			if (c.isSetSize()) {
				buffer.append("size of ");
				if (c.getSize() - (c.getSize()) == 0) {
					buffer.append(getWordForNumber((int) c.getSize()));
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
		if (model.getNumSpecies() > 0) {
			List<Integer>[] reactantsReaction = new List[model.getNumSpecies()];
			List<Integer>[] productsReaction = new List[model.getNumSpecies()];
			List<Integer>[] modifiersReaction = new List[model.getNumSpecies()];
			boolean notSubstancePerTimeUnit = false, notExistingKineticLaw = false;
			
			for (speciesIndex = 0; speciesIndex < model.getNumSpecies(); speciesIndex++) {
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
				for (sReferenceIndex = 0; sReferenceIndex < r.getNumReactants(); sReferenceIndex++) {
					speciesIndex = speciesIDandIndex.get(
						r.getReactant(sReferenceIndex).getSpecies()).intValue();
					reactantsReaction[(int) speciesIndex].add(Integer
							.valueOf(reactionIndex + 1));
				}
				for (sReferenceIndex = 0; sReferenceIndex < r.getNumProducts(); sReferenceIndex++) {
					speciesIndex = speciesIDandIndex.get(
						r.getProduct(sReferenceIndex).getSpecies()).intValue();
					productsReaction[(int) speciesIndex].add(Integer
							.valueOf(reactionIndex + 1));
				}
				for (sReferenceIndex = 0; sReferenceIndex < r.getNumModifiers(); sReferenceIndex++) {
					speciesIndex = speciesIDandIndex.get(
						r.getModifier(sReferenceIndex).getSpecies()).intValue();
					modifiersReaction[(int) speciesIndex].add(Integer
							.valueOf(reactionIndex + 1));
				}
			}
			
			// writing Equations
			buffer.append(section(
				(model.getNumSpecies() > 1) ? "Derived Rate Equations"
						: "Derived Rate Equation", true));
			buffer.append(label("sec:DerivedRateEquations"));
			buffer.newLine();
			buffer.append("When interpreted as an ordinary differential ");
			buffer.append("equation framework, this model implies ");
			buffer.append("the following ");
			if (reactionList.size() == 1) {
				buffer.append("equation");
			} else {
				buffer.append("set of equations");
			}
			buffer.append(" for the rate");
			if (model.getNumSpecies() > 1) {
				buffer.append("s of change of each ");
			} else {
				buffer.append(" of change of the following ");
			}
			buffer.append("species. ");
			buffer.newLine();
			
			if (notExistingKineticLaw) {
				buffer.newLine();
				buffer.append("The identifiers for reactions, ");
				buffer.append("which are not defined properly or ");
				buffer.append("which are lacking a kinetic equation, ");
				buffer.append("are highlighted in \\textcolor{red}{red}. ");
				buffer.newLine();
			}
			if (notSubstancePerTimeUnit) {
				buffer.newLine();
				buffer.append("Identifiers for kinetic laws highlighted in ");
				buffer.append(colorbox("lightgray", "gray"));
				buffer.append(" cannot be verified  to evaluate to ");
				buffer.append("units of SBML ");
				buffer.append(texttt("substance"));
				buffer.append(" per ");
				buffer.append(texttt("time"));
				buffer.append(". As a result, some SBML interpreters may ");
				buffer.append("not be able to verify the consistency ");
				buffer.append("of the units on ");
				buffer.append("quantities in the model. Please check if ");
				buffer.newLine();
				buffer.append("\\begin{itemize}");
				buffer.newLine();
				buffer.append("\\item parameters without a unit definition");
				buffer.append(" are involved or");
				buffer.newLine();
				buffer.append("\\item volume correction is necessary");
				buffer.append(" because the ");
				buffer.append(texttt("has\\-Only\\-Substance\\-Units"));
				buffer.append(" flag may be set to ");
				buffer.append(texttt("false"));
				buffer.append(" and ");
				buffer.append(texttt("spacial\\-Di\\-men\\-si\\-ons"));
				buffer.append("$> 0$ for certain species.");
				buffer.newLine();
				buffer.append("\\end{itemize}");
				buffer.newLine();
			}
			
			for (speciesIndex = 0; speciesIndex < model.getNumSpecies(); speciesIndex++) {
				species = model.getSpecies(speciesIndex);
				buffer.append(subsection(
					"Species " + texttt(maskSpecialChars(species.getId())), true));
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
						for (int i = 0; i < compartment.getDerivedUnitDefinition()
								.getNumUnits(); i++) {
							Unit unit = new Unit(compartment.getDerivedUnitDefinition()
									.getUnit(i));
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
				for (i = 0; (i < model.getNumInitialAssignments()); i++) {
					hasInitialAssignment = model.getInitialAssignment(i).getVariable()
							.equals(species.getId());
					if (hasInitialAssignment) {
						break;
					}
				}
				if (hasInitialAssignment) {
					buffer.append(descriptionItem("Initial assignment ",
						Integer.toString(i)));
				}
				
				// =========== R U L E S and E V E N T S =================
				
				// Events, in which this species is involved in
				Vector<String> eventsInvolved = new Vector<String>();
				for (i = 0; i < model.getNumEvents(); i++) {
					Event event = model.getEvent(i);
					for (j = 0; j < event.getNumEventAssignments(); j++) {
						if (event.getEventAssignment(j).getVariable()
								.equals(species.getId())) {
							eventsInvolved.add(event.isSetId() ? event.getId() : Integer
									.toString(i));
						}
					}
				}
				if (eventsInvolved.size() > 0) {
					buffer.append("\\item[Involved in event");
					if (eventsInvolved.size() > 1) {
						buffer.append('s');
					}
					buffer.append("] ");
					for (i = 0; i < eventsInvolved.size(); i++) {
						String id = eventsInvolved.get(i);
						buffer.append(hyperref("event" + id, texttt(maskSpecialChars(id))));
						if (i < eventsInvolved.size() - 1) {
							buffer.append(", ");
						}
					}
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
				for (i = 0; i < model.getNumRules(); i++) {
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
					for (i = 0; i < rulesInvolved.size(); i++) {
						int index = rulesInvolved.get(i);
						buffer.append(hyperref("rule" + index, "Rule " + index));
						if (i < rulesInvolved.size() - 1) {
							buffer.append(", ");
						}
					}
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
							buffer.append(" not influence its rate of change ");
							buffer.append("because this constant species is ");
							buffer.append("on the boundary of the reaction");
							buffer.append(" system:");
						}
						buffer.append(eqBegin);
						buffer.append("\\frac{\\mathrm d}{\\mathrm dt} ");
						buffer.append(getNameOrID(species, true));
						buffer.append(" = 0");
						buffer.append(eqEnd);
						if ((rulesInvolved.size() > 0) || (eventsInvolved.size() > 0)) {
							buffer.append("This species' quantity is affected by ");
							if (rulesInvolved.size() > 0) {
								buffer.append(getWordForNumber(rulesInvolved.size()));
								buffer.append(" rule");
								if (rulesInvolved.size() > 1) {
									buffer.append('s');
								}
								if (eventsInvolved.size() > 0) {
									buffer.append(" and");
								}
							}
							if (eventsInvolved.size() > 0) {
								buffer.append(getWordForNumber(eventsInvolved.size()));
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
								String number = getWordForNumber(rulesInvolved.size());
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
								String number = getWordForNumber(eventsInvolved.size());
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
								buffer.append(" because this species is ");
								buffer.append("on the boundary of the ");
								buffer.append("reaction system");
							}
							buffer.append('.');
						} else {
							if (numReactionsInvolved > 0) {
								buffer.append(", which do");
								if (numReactionsInvolved == 1) {
									buffer.append("es");
								}
								buffer.append(" not influence its rate ");
								buffer.append("of change because this ");
								buffer.append("species is on the boundary");
								buffer.append(" of the reaction system:");
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
							buffer.append("As this species is constant and its");
							buffer.append(" boundary condition is ");
							buffer.append(texttt("false"));
							buffer.append(" it cannot be involved in");
							boolean comma = false;
							if (rulesInvolved.size() > 0) {
								buffer.append(" any rules");
								comma = true;
							}
							if (eventsInvolved.size() > 0) {
								if (comma) {
									buffer.append(", ");
								} else {
									comma = true;
								}
								buffer.append(" any events");
							}
							if (numReactionsInvolved - numModification > 0) {
								if (comma) {
									buffer.append(" or");
								}
								buffer.append(" any reactions except it ");
								buffer.append("acts as as a modifier");
							}
							buffer.append(". Please verify this SBML");
							buffer.append(" document.");
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
							String number = getWordForNumber(rulesInvolved.size());
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
								buffer.append(". Please verify this SBML ");
								buffer.append("document.");
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
								buffer.append("This species does not take ");
								buffer.append("part in any reactions. ");
								buffer.append("Its quantity does hence not ");
								buffer.append("change over time:");
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
							buffer.append(getWordForNumber(eventsInvolved.size()));
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
	 * @param buffer
	 * @throws IOException
	 */
	private void format(Creator creator, BufferedWriter buffer)
		throws IOException {
		if (creator.isSetGivenName()) {
			buffer.append(creator.getGivenName());
		}
		if (creator.isSetFamilyName()) {
			if (creator.isSetGivenName()) {
				buffer.append(' ');
			}
			buffer.append(creator.getFamilyName());
		}
		if ((creator.isSetGivenName() || creator.isSetFamilyName())
				&& (creator.isSetOrganisation() || creator.isSetEmail())) {
			buffer.append("\\footnote{");
		}
		if (creator.isSetOrganisation()) {
			buffer.append(creator.getOrganisation());
		}
		if (creator.isSetEmail()) {
			if (creator.isSetOrganisation()) {
				buffer.append(", ");
			}
			buffer.append(href("mailto:" + creator.getEmail(), "\\nolinkurl{"
					+ creator.getEmail() + '}'));
		}
		if ((creator.isSetGivenName() || creator.isSetFamilyName())
				&& (creator.isSetOrganisation() || creator.isSetEmail())) {
			buffer.append('}');
		}
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
		reactString.append(subsection(
			"Reaction " + texttt(maskSpecialChars(r.getId())), true));
		reactString.append("This is a");
		if (!r.getReversible()) {
			reactString.append(r.getFast() ? " fast ir" : "n ir");
		} else {
			reactString.append(r.getFast() ? " fast " : " ");
		}
		reactString.append("reversible reaction of ");
		reactString.append(getWordForNumber(r.getNumReactants()));
		reactString.append(" reactant");
		if (r.getNumReactants() > 1) {
			reactString.append('s');
		}
		reactString.append(" forming ");
		reactString.append(getWordForNumber(r.getNumProducts()));
		reactString.append(" product");
		if (r.getNumProducts() > 1) {
			reactString.append('s');
		}
		if (r.getNumModifiers() > 0) {
			reactString.append(" influenced by ");
			reactString.append(getWordForNumber(r.getNumModifiers()));
			reactString.append(" modifier");
			if (r.getNumModifiers() > 1) {
				reactString.append('s');
			}
		}
		reactString.append('.');
		
		int hasSBOReactants = 0, hasSBOProducts = 0, hasSBOModifiers = 0;
		boolean onlyItems = false;
		if (arrangeReactionParticipantsInOneTable) {
			for (i = 0; i < r.getNumReactants(); i++) {
				if (r.getReactant(i).isSetSBOTerm()) {
					hasSBOReactants++;
				}
			}
			for (i = 0, hasSBOProducts = 0; i < r.getNumProducts(); i++) {
				if (r.getProduct(i).isSetSBOTerm()) {
					hasSBOProducts++;
				}
			}
			for (i = 0, hasSBOModifiers = 0; i < r.getNumModifiers(); i++) {
				if (r.getModifier(i).isSetSBOTerm()) {
					hasSBOModifiers++;
				}
			}
			if (r.isSetName() || r.isSetNotes() || r.isSetSBOTerm()
					|| ((r.getNumCVTerms() > 0) && includeMIRIAM)
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
				reactString.append("\\item[Reactant");
				if (hasSBOReactants > 1) {
					reactString.append('s');
				}
				reactString.append(" with SBO annotation] ");
				for (i = 0; i < r.getNumReactants(); i++) {
					SpeciesReference reactant = r.getReactant(i);
					if (r.getReactant(i).isSetSBOTerm()) {
						reactString.append(texttt(maskSpecialChars(reactant.getSpecies())));
						reactString.append(" (");
						reactString.append(SBO.sboNumberString(reactant.getSBOTerm()));
						sboTerms.add(Integer.valueOf(reactant.getSBOTerm()));
						reactString.append(')');
						if (--hasSBOReactants > 0) {
							reactString.append(", ");
						}
					}
				}
			}
			if (hasSBOProducts > 0) {
				reactString.append("\\item[Product");
				if (hasSBOProducts > 1) {
					reactString.append('s');
				}
				reactString.append(" with SBO annotation] ");
				for (i = 0; i < r.getNumProducts(); i++) {
					SpeciesReference product = r.getProduct(i);
					if (r.getProduct(i).isSetSBOTerm()) {
						reactString.append(texttt(maskSpecialChars(product.getSpecies())));
						reactString.append(" (");
						reactString.append(SBO.sboNumberString(product.getSBOTerm()));
						sboTerms.add(Integer.valueOf(product.getSBOTerm()));
						reactString.append(')');
						if (--hasSBOProducts > 0) {
							reactString.append(", ");
						}
					}
				}
			}
			if (r.getListOfModifiers().size() > 0) {
				if (hasSBOModifiers > 0) {
					reactString.append("\\item[Modifier");
					if (hasSBOModifiers > 1) {
						reactString.append('s');
					}
					reactString.append(" with SBO annotation] ");
					for (i = 0; i < r.getNumModifiers(); i++) {
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
								reactString.append(", ");
							}
						}
					}
				}
			}
			if (onlyItems) {
				reactString.append(descriptionEnd);
			}
		}
		
		reactString.append(subsubsection("Reaction equation", false));
		reactString.append("\\reaction{");
		reactString.append(reactionEquation(r));
		reactString.append('}');
		reactString.append(newLine());
		
		if (arrangeReactionParticipantsInOneTable) {
			/*
			 * One table for the reactants and products
			 */
			String headLine = "", head = "@{}", idAndNameColumn;
			double nameWidth = 3;
			double idWidth = nameWidth / 2;
			if (paperSize.equals("letter") || paperSize.equals("a4"))
				idAndNameColumn = "p{" + idWidth + "cm}p{" + nameWidth + "cm}";
			else {
				int columns = 0;
				if (r.getNumReactants() > 0) {
					columns += 2;
				}
				if (r.getNumModifiers() > 0) {
					columns += 2;
				}
				if (r.getNumProducts() > 0) {
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
				idAndNameColumn = "p{" + idWidth + "\\textwidth}p{" + nameWidth
						+ "\\textwidth}";
			}
			int cols = 0;
			if (r.getNumReactants() > 0) {
				headLine = "\\multicolumn{2}{c";
				head += idAndNameColumn;
				if ((r.getNumProducts() > 0) || (r.getNumModifiers() > 0)) {
					headLine += "|}{Reactants}&";
					head += '|';
				} else {
					headLine += "}{Reactants}";
				}
				cols++;
			}
			if (r.getNumModifiers() > 0) {
				headLine += "\\multicolumn{2}{c";
				head += idAndNameColumn;
				if (r.getNumProducts() > 0) {
					headLine += "|}{Modifiers}&";
					head += '|';
				} else {
					headLine += "}{Modifiers}";
				}
				cols++;
			}
			if (r.getNumProducts() > 0) {
				headLine += "\\multicolumn{2}{c}{Products}";
				head += idAndNameColumn;
				cols++;
			}
			headLine += lineBreak;
			headLine += "Id&Name";
			for (i = 1; i < cols; i++) {
				headLine += "&Id&Name";
			}
			reactString.append(longtableHead(head + "@{}",
				"Overview of participating species.", headLine));
			for (i = 0; i < Math.max(r.getNumReactants(),
				Math.max(r.getNumProducts(), r.getNumModifiers())); i++) {
				Species s;
				if (r.getNumReactants() > 0) {
					if (i < r.getNumReactants()) {
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
					if ((r.getNumModifiers() > 0) || (r.getNumProducts() > 0)) {
						reactString.append('&');
					}
				}
				if (r.getNumModifiers() > 0) {
					if (i < r.getNumModifiers()) {
						s = r.getModel().getSpecies(r.getModifier(i).getSpecies());
						reactString.append(texttt(maskSpecialChars(s.getId())));
						reactString.append('&');
						reactString.append(maskSpecialChars(s.getName()));
						/*
						 * reactString.append('&'); reactString.append(s.isSetSBOTerm() ?
						 * SBO.sboNumberString(s .getSBOTerm()) : " ");
						 */
					}
					if (r.getNumProducts() > 0) {
						reactString.append('&');
					}
				}
				if (r.getNumProducts() > 0) {
					if (i < r.getNumProducts()) {
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
			
			if (r.getNumReactants() > 0) {
				reactString.append(subsubsection(r.getNumReactants() > 1 ? "Reactants"
						: "Reactant", false));
				reactString.append(longtableHead(columnDef, caption + "reactant.",
					headLine));
				for (i = 0; i < r.getListOfReactants().size(); i++) {
					specRef = r.getReactant(i);
					species = r.getModel().getSpecies(specRef.getSpecies());
					reactString.append(texttt(maskSpecialChars(specRef.getSpecies())));
					reactString.append('&');
					reactString
							.append(maskSpecialChars(specRef.getName().length() == 0 ? species
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
			if (r.getNumModifiers() > 0) {
				reactString.append(subsubsection(r.getNumModifiers() > 1 ? "Modifiers"
						: "Modifier", false));
				reactString.append(longtableHead(columnDef, caption + "modifier.",
					headLine));
				for (i = 0; i < r.getListOfModifiers().size(); i++) {
					modRef = r.getModifier(i);
					species = r.getModel().getSpecies(modRef.getSpecies());
					reactString.append(texttt(maskSpecialChars(modRef.getSpecies())));
					reactString.append('&');
					reactString
							.append(maskSpecialChars(modRef.getName().length() == 0 ? species
									.getName() : modRef.getName()));
					reactString.append('&');
					if (modRef.isSetSBOTerm()) {
						reactString.append(SBO.sboNumberString(modRef.getSBOTerm()));
						sboTerms.add(Integer.valueOf(modRef.getSBOTerm()));
					}
					reactString.append(lineBreak);
				}
				reactString.append(bottomrule);
			}
			if (r.getNumProducts() > 0) {
				reactString.append(subsubsection(r.getNumProducts() > 1 ? "Products"
						: "Product", false));
				reactString.append(longtableHead(columnDef, caption + "product.",
					headLine));
				for (i = 0; i < r.getListOfProducts().size(); i++) {
					specRef = r.getProduct(i);
					species = r.getModel().getSpecies(specRef.getSpecies());
					reactString.append(texttt(maskSpecialChars(specRef.getSpecies())));
					reactString.append('&');
					reactString
							.append(maskSpecialChars(specRef.getName().length() == 0 ? species
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
		
		reactString.append(subsubsection("Kinetic Law", false));
		StringWriter localParameters = new StringWriter();
		List<String> functionCalls = null;
		if (r.getKineticLaw() != null) {
			KineticLaw kin = r.getKineticLaw();
			reactString.append(descriptionBegin);
			BufferedWriter pBuffer = new BufferedWriter(reactString);
			format(kin, pBuffer, true);
			pBuffer.close();
			UnitDefinition ud = kin.getDerivedUnitDefinition();
			reactString.append("\\item[Derived unit] ");
			if (ud.getNumUnits() == 0) {
				reactString.append("not available");
			} else if (kin.containsUndeclaredUnits()) {
				reactString.append("contains undeclared units");
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
				if (0 < r.getModel().getNumFunctionDefinitions()) {
					functionCalls = callsFunctions(kin.getMath());
				}
			} else {
				reactString.append("\\text{no mathematics specified}");
			}
			pBuffer = new BufferedWriter(localParameters);
			if (r.getKineticLaw().getLocalParameterCount() > 0) {
				format(r.getKineticLaw().getListOfLocalParameters(), pBuffer, false);
			}
			pBuffer.close();
		} else {
			reactString.append(eqBegin);
			reactString.append("v_{" + (reactionIndex + 1) + "}=");
			reactString.append("\\text{not specified}");
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
			buffer.append(equation(new StringBuffer(rl.getMath().toLaTeX()),
				new StringBuffer("\\equiv 0")));
			Variable variable;
			if (!validator.isOverdetermined()) {
				variable = (Variable) validator.getMatching().get(rl);
				buffer.append(" This rule determines the value of ");
				buffer.append(variable.getElementName());
				buffer.append(' ');
				buffer.append(getNameOrID(variable, false));
				buffer.append('.');
				buffer.newLine();
			} else {
				buffer
						.append(" As this model is overdetermined, it is not possible to state, which variable is determined by this rule.");
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
				buffer
						.append(mathtt(maskSpecialChars(((Assignment) rl).getVariable())));
			}
			if (hasOnlySubstanceUnits) {
				buffer.append(']');
			}
			buffer.append(" = ");
			buffer.append(rl.getMath().toLaTeX());
			buffer.append(eqEnd);
		}
		if (rl.isSetNotes()
				|| ((rl.getDerivedUnitDefinition().getNumUnits() > 0) && !rl
						.containsUndeclaredUnits())) {
			buffer.append(descriptionBegin);
			if ((rl.getDerivedUnitDefinition().getNumUnits() > 0)
					&& !rl.containsUndeclaredUnits()) {
				buffer.append(descriptionItem("Derived unit",
					math(format(rl.getDerivedUnitDefinition()))));
			}
			if (rl.isSetNotes()) {
				buffer
						.append(descriptionItem("Notes", formatHTML(rl.getNotesString())));
			}
			if ((rl.getNumCVTerms() > 0) && includeMIRIAM) {
				buffer.append("\\item[Annotation] ");
				for (int i = 0; i < rl.getNumCVTerms(); i++) {
					format(rl.getCVTerm(i), buffer);
				}
			}
			buffer.append(descriptionEnd);
		}
	}

	/**
	 * Formats name, SBO term and notes as items of a description environment.
	 * 
	 * @param sBase
	 * @param buffer
	 * @param onlyItems
	 *        If true items will be written otherwise this will be surrounded by a
	 *        description environment.
	 * @throws IOException
	 */
	private void format(SBase sBase, BufferedWriter buffer, boolean onlyItems)
		throws IOException {
		
		formatHistory(sBase, buffer);
		
		if (((sBase instanceof NamedSBase) && ((NamedSBase) sBase).isSetName())
				|| sBase.isSetNotes() || sBase.isSetSBOTerm()
				|| ((sBase.getNumCVTerms() > 0 && includeMIRIAM))) {
			if (!onlyItems) {
				buffer.append(descriptionBegin);
			}
			if (((sBase instanceof NamedSBase) && ((NamedSBase) sBase).isSetName())) {
				buffer.append(descriptionItem("Name",
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
				buffer.append(descriptionItem("Notes",
					formatHTML(sBase.getNotesString()).toString()));
			}
			if ((sBase.getNumCVTerms() > 0) && includeMIRIAM) {
				StringWriter description = new StringWriter();
				BufferedWriter bw = new BufferedWriter(description);
				for (int i = 0; i < sBase.getNumCVTerms(); i++) {
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
		buffer.append("This species takes part in ");
		int numReactants = reactantsReaction[speciesIndex].size();
		int numProducts = productsReaction[speciesIndex].size();
		int numModifiers = modifierReaction[speciesIndex].size();
		final int numReactionsInvolved = numReactants + numProducts + numModifiers;
		buffer.append(getWordForNumber(numReactionsInvolved));
		buffer.append(" reaction");
		if (numReactionsInvolved > 1) {
			buffer.append('s');
		}
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
						buffer.append(" and ");
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
						buffer.append(" and ");
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
				buffer.append(", ");
			} else {
				buffer.append(' ');
			}
			buffer.append(hyperref("v" + Integer.toString(reactionIndex),
				texttt(maskSpecialChars(reaction.getId()))));
		}
		buffer.append(')');
	}

	/**
	 * Returns a mathematical formula if stoichiometric math is used or the
	 * formated stoichiometric coefficient of the given SpeciesReference. Be aware
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
				v.append("\\text{no mathematics specified}");
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
	 * If the field printNameIfAvailable is false this method returns a the id of
	 * the given SBase. If printNameIfAvailable is true this method looks for the
	 * name of the given SBase and will return it.
	 * 
	 * @param sbase
	 *        the SBase, whose name or id is to be returned.
	 * @param mathMode
	 *        if true this method returns the name typesetted in mathmode, i.e.,
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
		if (!isName) { return mathMode ? mathtt(name) : texttt(name); }
		return mathMode ? mathrm(name) : new StringBuffer(name);
	}

	/**
	 * This method returns the correct LaTeX expression for a function which
	 * returns the size of a compartment. This can be a volume, an area, a length
	 * or a point.
	 */
	private StringBuffer getSize(Compartment c) {
		StringBuffer value;
		switch ((int) c.getSpatialDimensions()) {
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
	 * 
	 * @return
	 */
	private StringBuffer imprint() {
		StringBuffer imprint = new StringBuffer();
		imprint.append(newLine());
		imprint.append(newLine());
		imprint.append("\\begin{thebibliography}{}");
		imprint.append(newLine());
		imprint
				.append("\\bibitem[Dr\\\"ager {\\em et~al.}(2009)Dr\\\"ager, Planatscher, Wouamba, Schr\\\"oder,");
		imprint.append(newLine());
		imprint
				.append("  Hucka, Endler, Golebiewski, M{\\\"u}ller, and Zell]{Draeger2009b}");
		imprint.append(newLine());
		imprint
				.append("Dr\\\"ager, A., Planatscher, H., Wouamba, D.~M., Schr\\\"oder, A., Hucka, M.,");
		imprint.append(newLine());
		imprint
				.append("  Endler, L., Golebiewski, M., M{\\\"u}ller, W., and Zell, A. (2009).");
		imprint.append(newLine());
		imprint
				.append("\\newblock {SBML2\\LaTeX: Conversion of SBML files into human-readable reports}.");
		imprint.append(newLine());
		// imprint.append("\\newblock {\\em Bioinformatics\\/}, {\\bf 25}(11), 1455--1456.");
		imprint.append("\\newblock {Bioinformatics}, {\\bf 25}(11), 1455--1456. ");
		imprint
				.append("\\href{http://dx.doi.org/10.1093/bioinformatics/btp170}{10.1093/bioinformatics/btp170}.");
		imprint.append(newLine());
		imprint.append("\\end{thebibliography}");
		imprint.append(newLine());
		
		return imprint;
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
	private void problemMessage(Vector<Integer> listOfErrorIndices,
		SBMLDocument doc, String title, BufferedWriter buffer, String messageType)
		throws IOException {
		buffer.append(subsection(title, true));
		buffer.append("This SBML document");
		buffer.append(" contains ");
		buffer.append(getWordForNumber(listOfErrorIndices.size()));
		buffer.append(' ');
		buffer
				.append(title.startsWith("XML") ? title : firstLetterLowerCase(title));
		buffer.append('.');
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
	 * This method returns a StringBuffer containing the reaction equation for the
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
		if (r.getNumReactants() == 0) {
			reactString.append("$\\emptyset$");
		} else {
			for (i = 0; i < r.getNumReactants(); i++) {
				if (r.getReactant(i) == null) {
					reactString
							.append(math("\\text{invalid species reference for reactant "
									+ getWordForNumber(i + 1) + '}'));
				} else {
					reactString.append(formatStoichiometry(r.getReactant(i)));
					reactString.append(' ');
					reactString.append(math(getNameOrID(
						r.getModel().getSpecies(r.getReactant(i).getSpecies()), true)));
				}
				if (i < r.getNumReactants() - 1) {
					reactString.append(" + ");
				}
			}
		}
		reactString.append(r.getReversible() ? " <=>" : " ->");
		if (r.getNumModifiers() > 0) {
			reactString.append("[\\text{");
			reactString.append(math(getNameOrID(
				r.getModel().getSpecies(r.getModifier(0).getSpecies()), true)));
			for (i = 1; i < r.getNumModifiers(); i++) {
				reactString.append(",\\;");
				reactString.append(math(getNameOrID(
					r.getModel().getSpecies(r.getModifier(i).getSpecies()), true)));
			}
			reactString.append("}] ");
		} else {
			reactString.append(' ');
		}
		if (r.getNumProducts() == 0) {
			reactString.append("$\\emptyset$");
		} else {
			for (i = 0; i < r.getNumProducts(); i++) {
				if (r.getProduct(i) == null) {
					reactString
							.append(math("\\text{invalid species reference for product "
									+ getWordForNumber(i + 1) + '}'));
				} else {
					reactString.append(formatStoichiometry(r.getProduct(i)));
					reactString.append(' ');
					reactString.append(math(getNameOrID(
						r.getModel().getSpecies(r.getProduct(i).getSpecies()), true)));
				}
				if (i < r.getNumProducts() - 1) reactString.append(" + ");
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
			if (ud != null) { return format(ud); }
		}
		return new StringBuffer("Unknown unit " + kind);
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
		  throw new IOException(String.format("%s is not a valid TeX file.", file)); 
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
