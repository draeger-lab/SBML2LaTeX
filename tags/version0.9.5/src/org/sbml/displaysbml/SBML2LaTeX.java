/*
    SBML2LaTeX converts SBML files (http://sbml.org) into LaTeX files.
    Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.displaysbml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.jdom.JDOMException;
import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.AlgebraicRule;
import org.sbml.libsbml.CVTerm;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentType;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.Date;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.ListOfCompartments;
import org.sbml.libsbml.ListOfEvents;
import org.sbml.libsbml.ListOfFunctionDefinitions;
import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfReactions;
import org.sbml.libsbml.ListOfRules;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.ListOfUnitDefinitions;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModelCreator;
import org.sbml.libsbml.ModelHistory;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLError;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.SpeciesType;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.XMLAttributes;
import org.sbml.libsbml.libsbmlConstants;

import cz.kebrt.html2latex.FatalErrorException;
import cz.kebrt.html2latex.HTML2LaTeX;

/**
 * This class displays the whole information content of an SBML model in a LaTeX
 * file which can then be further processed, i.e., to a PDF file.
 * 
 * @since 2.0
 * @version 1.0
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:dwouamba@yahoo.fr">Dieudonne Motsou Wouamba</a>
 * @date December 4, 2007
 */
public class SBML2LaTeX extends LaTeX implements libsbmlConstants, DisplaySBML {

	/**
	 * The location of the SBML2LaTeX logo file.
	 */
	private static String logo;

	/**
	 * This is the link to the MIRIAM resources.
	 */
	private static final MIRIAMparser miriam = new MIRIAMparser();

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
	 * This variable is needed to decide whether a method should write a
	 * document head and tail for the LaTeX output.
	 */
	private boolean headTail;

	/**
	 * If true species (reactants, modifiers and products) in reaction equations
	 * will be displayed with their name if they have one. By default the ids of
	 * the species are used in these equations.
	 */
	private boolean printNameIfAvailable;

	/**
	 * If true this will produce LaTeX files for for entirely landscape
	 * documents
	 */
	private boolean landscape;

	/**
	 * If true ids are set in typewriter font (default).
	 */
	private boolean typewriter;

	/**
	 * If true predefined SBML units will be made explicitly if not overridden
	 * in the model.
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
	 * If true MIRIAM annotations are included into the model report. This
	 * process takes a bit time due to the necessary connection to EBI's
	 * web-service.
	 */
	private boolean includeMIRIAM = false;

	/**
	 * This switch allows to change the way how the reactants, modifiers and
	 * products are presented in each reaction. If true a table is created
	 * containing the identifiers of each reactant, modifier and product
	 * together with the respective name. If false (default), a subsection for
	 * each one of the three groups of participants is created giving all
	 * details of each participant.
	 */
	private boolean arrangeReactionParticipantsInOneTable = false;

	/* MiriamLink miriam = new MiriamLink(); */
	static {
		/*
		 * // Sets the address to access the Web Services miriam.setAddress(
		 * "http://www.ebi.ac.uk/compneur-srv/miriamws-main/MiriamWebServices");
		 */
		try {
			miriam.setMIRIAMfile(System.getProperty("user.dir") + fileSeparator
					+ "resources" + fileSeparator + "MIRIAM.xml");
		} catch (JDOMException exc) {
			// exc.printStackTrace();
		} catch (IOException exc) {
			// exc.printStackTrace();
		}
		logo = (new File(System.getProperty("user.dir") + fileSeparator
				+ "resources" + fileSeparator + "SBML2LaTeX.eps"))
				.getAbsolutePath();
		logo = logo.substring(0, logo.lastIndexOf('.'));
	}

	/**
	 * Constructs a new instance of LaTeX export. For each document to be
	 * translated a new instance has to be created. Here default values are used
	 * (A4 paper, 11pt, portrait, fancy headings, no titlepage).
	 */
	public SBML2LaTeX() {
		this(false, true, (short) 11, "a4", true, false, false);
	}

	/**
	 * Constructs a new instance of LaTeX export. For each document to be
	 * translated a new instance has to be created. This constructor allows you
	 * to set many properties of the resulting LaTeX file.
	 * 
	 * @param landscape
	 *            If <code>true</code> the whole document will be set to
	 *            landscape format, otherwise portrait.
	 * @param typeWriter
	 *            If <code>true</code> ids are set in typewriter font (default).
	 *            Otherwise the regular font is used.
	 * @param fontSize
	 *            The size of the font to be used here. The default is 11.
	 *            Allowed values are 8, 9, 10, 11, 12, 14, 16 and 17.
	 * @param paperSize
	 *            Allowed are
	 *            <ul>
	 *            <li>letter</li>
	 *            <li>legal</li>
	 *            <li>executive</li>
	 *            <li>a* where * stands for values from 0 thru 9</li>
	 *            <li>b*</li>
	 *            <li>c*</li>
	 *            <li>d*</li>
	 *            </ul>
	 * @param addPredefinedUnits
	 *            If true predefined SBML units will be made explicitly if not
	 *            overridden in the model.
	 * @param titlepage
	 *            if true a title page will be created for the model report.
	 *            Default is false (just a caption).
	 */
	public SBML2LaTeX(boolean landscape, boolean typeWriter, short fontSize,
			String paperSize, boolean addPredefinedUnits, boolean titlepage,
			boolean printNameIfAvailable) {
		this.headTail = true;
		setLandscape(landscape);
		setTypewriter(typeWriter);
		setFontSize(fontSize);
		setPaperSize(paperSize);
		setShowImplicitUnitDeclarations(addPredefinedUnits);
		setTitlepage(titlepage);
		setPrintNameIfAvailable(printNameIfAvailable);
	}

	/**
	 * Method that transforms any libSBML abstract syntax tree (mathematical
	 * formula) of into LaTeX code.
	 * 
	 * @param model
	 *            The model, which contains this abstract syntax tree
	 * @param astnode
	 *            The node of the abstract syntax tree to be transformed
	 *            recursively, i.e., the subtree rooted at this node will be
	 *            translated into LaTeX.
	 * @return String An expression in LaTeX format describing the subtree
	 *         rooted at the given astnode.
	 * @throws IOException
	 */
	public StringBuffer toLaTeX(Model model, ASTNode astnode)
			throws IOException {
		if (astnode == null)
			return mathrm("undefined");

		ASTNode ast;
		StringBuffer value;
		if (astnode.isUMinus()) {
			value = new StringBuffer('-');
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;
		} else if (astnode.isSqrt())
			return sqrt(toLaTeX(model, astnode.getLeftChild()));
		else if (astnode.isInfinity())
			return POSITIVE_INFINITY;
		else if (astnode.isNegInfinity())
			return NEGATIVE_ININITY;

		switch (astnode.getType()) {
		/*
		 * Numbers
		 */
		case AST_REAL:
			return format(astnode.getReal());

		case AST_INTEGER:
			return value = new StringBuffer(Integer.toString(astnode
					.getInteger()));
			/*
			 * Basic Functions
			 */
		case AST_FUNCTION_LOG: {
			value = new StringBuffer("\\log");
			if (astnode.getNumChildren() == 2) {
				value.append("_{");
				value.append(toLaTeX(model, astnode.getLeftChild()));
				value.append('}');
			}
			value.append('{');
			if (astnode.getChild(astnode.getNumChildren() - 1).getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getChild(astnode
						.getNumChildren() - 1))));
			else
				value.append(toLaTeX(model, astnode.getChild(astnode
						.getNumChildren() - 1)));
			value.append('}');
			return value;
		}
			/*
			 * Operators
			 */
		case AST_POWER:
			value = toLaTeX(model, astnode.getLeftChild());
			if (astnode.getLeftChild().getNumChildren() > 0)
				value = brackets(value);
			value.append("^{");
			value.append(toLaTeX(model, astnode.getRightChild()));
			value.append("}");
			return value;

		case AST_PLUS:
			value = toLaTeX(model, astnode.getLeftChild());
			for (int i = 1; i < astnode.getNumChildren(); i++) {
				ast = astnode.getChild(i);
				value.append(" + ");
				if (ast.getType() == AST_MINUS)
					value.append(brackets(toLaTeX(model, ast)));
				else
					value.append(toLaTeX(model, ast));
			}
			return value;

		case AST_MINUS:
			value = toLaTeX(model, astnode.getLeftChild());
			for (int i = 1; i < astnode.getNumChildren(); i++) {
				ast = astnode.getChild(i);
				value.append(" - ");
				if (ast.getType() == AST_PLUS)
					value.append(brackets(toLaTeX(model, ast)));
				else
					value.append(toLaTeX(model, ast));
			}
			return value;

		case AST_TIMES:
			value = toLaTeX(model, astnode.getLeftChild());
			if (astnode.getLeftChild().getNumChildren() > 1
					&& (astnode.getLeftChild().getType() == AST_MINUS || astnode
							.getLeftChild().getType() == AST_PLUS))
				value = brackets(value);
			for (int i = 1; i < astnode.getNumChildren(); i++) {
				ast = astnode.getChild(i);
				value.append("\\cdot");
				if ((ast.getType() == AST_MINUS) || (ast.getType() == AST_PLUS))
					value.append(brackets(toLaTeX(model, ast)));
				else {
					value.append(' ');
					value.append(toLaTeX(model, ast));
				}
			}
			return value;

		case AST_DIVIDE:
			return frac(toLaTeX(model, astnode.getLeftChild()), toLaTeX(model,
					astnode.getRightChild()));

		case AST_RATIONAL:
			return frac(Double.toString(astnode.getNumerator()), Double
					.toString(astnode.getDenominator()));

		case AST_NAME_TIME:
			return mathrm(astnode.getName());

		case AST_FUNCTION_DELAY:
			return mathrm(astnode.getName());

			/*
			 * Names of identifiers: parameters, functions, species etc.
			 */
		case AST_NAME:
			if (model.getSpecies(astnode.getName()) != null) {
				// Species.
				Species species = model.getSpecies(astnode.getName());
				Compartment c = model.getCompartment(species.getCompartment());
				boolean concentration = !species.getHasOnlySubstanceUnits()
						&& (0 < c.getSpatialDimensions());
				value = new StringBuffer();
				if (concentration)
					value.append('[');
				value.append(getNameOrID(species, true));
				if (concentration)
					value.append(']');
				return value;

			} else if (model.getCompartment(astnode.getName()) != null) {
				// Compartment
				Compartment c = model.getCompartment(astnode.getName());
				return getSize(c);
			}
			// TODO: weitere spezialfälle von Namen!!!
			return value = new StringBuffer(mathtt(maskSpecialChars(astnode
					.getName())));
			/*
			 * Constants: pi, e, true, false
			 */
		case AST_CONSTANT_PI:
			return CONSTANT_PI;
		case AST_CONSTANT_E:
			return CONSTANT_E;
		case AST_CONSTANT_TRUE:
			return CONSTANT_TRUE;
		case AST_CONSTANT_FALSE:
			return CONSTANT_FALSE;
		case AST_REAL_E:
			return new StringBuffer(Double.toString(astnode.getReal()));
			/*
			 * More complicated functions
			 */
		case AST_FUNCTION_ABS:
			return abs(toLaTeX(model, astnode
					.getChild(astnode.getNumChildren() - 1)));

		case AST_FUNCTION_ARCCOS:
			if (astnode.getLeftChild().getNumChildren() > 0)
				return arccos(brackets(toLaTeX(model, astnode.getLeftChild())));
			return arccos(toLaTeX(model, astnode.getLeftChild()));

		case AST_FUNCTION_ARCCOSH:
			if (astnode.getLeftChild().getNumChildren() > 0)
				return arccosh(brackets(toLaTeX(model, astnode.getLeftChild())));
			return arccosh(toLaTeX(model, astnode.getLeftChild()));

		case AST_FUNCTION_ARCCOT:
			value = new StringBuffer("\\arcot{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_ARCCOTH:
			value = mathrm("arccoth");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;

		case AST_FUNCTION_ARCCSC:
			value = new StringBuffer("\\arccsc{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_ARCCSCH:
			value = mathrm("arccsh");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;

		case AST_FUNCTION_ARCSEC:
			value = new StringBuffer("\\arcsec{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_ARCSECH:
			value = mathrm("arcsech");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;

		case AST_FUNCTION_ARCSIN:
			value = new StringBuffer("\\arcsin{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_ARCSINH:
			value = mathrm("arcsinh");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;

		case AST_FUNCTION_ARCTAN:
			value = new StringBuffer("\\arctan{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_ARCTANH:
			value = new StringBuffer("\\arctanh{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_CEILING:
			return ceiling(toLaTeX(model, astnode.getLeftChild()));

		case AST_FUNCTION_COS:
			value = new StringBuffer("\\cos{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_COSH:
			value = new StringBuffer("\\cosh{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_COT:
			value = new StringBuffer("\\cot{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_COTH:
			value = new StringBuffer("\\coth{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_CSC:
			value = new StringBuffer("\\csc{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_CSCH:
			value = mathrm("csch");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;

		case AST_FUNCTION_EXP:
			value = new StringBuffer("\\exp{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_FACTORIAL:
			if (astnode.getLeftChild().getNumChildren() > 0)
				value = brackets(toLaTeX(model, astnode.getLeftChild()));
			else
				value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append('!');
			return value;

		case AST_FUNCTION_FLOOR:
			return floor(toLaTeX(model, astnode.getLeftChild()));

		case AST_FUNCTION_LN:
			value = new StringBuffer("\\ln{");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value = brackets(toLaTeX(model, astnode.getLeftChild()));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_POWER:
			if (astnode.getLeftChild().getNumChildren() > 0)
				value = brackets(toLaTeX(model, astnode.getLeftChild()));
			else
				value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append("^{");
			value.append(toLaTeX(model, astnode.getChild(astnode
					.getNumChildren() - 1)));
			value.append('}');
			return value;

		case AST_FUNCTION_ROOT:
			ASTNode left = astnode.getLeftChild();
			if ((astnode.getNumChildren() > 1)
					&& ((left.isInteger() && (left.getInteger() != 2)) || (left
							.isReal() && (left.getReal() != 2d))))
				return root(toLaTeX(model, astnode.getLeftChild()), toLaTeX(
						model, astnode.getRightChild()));
			return sqrt(toLaTeX(model, astnode.getChild(astnode
					.getNumChildren() - 1)));

		case AST_FUNCTION_SEC:
			value = new StringBuffer("\\sec{");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_SECH:
			value = mathrm("sech");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_SIN:
			value = new StringBuffer("\\sin{");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_SINH:
			value = new StringBuffer("\\sinh{");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_TAN:
			value = new StringBuffer("\\tan{");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_TANH:
			value = new StringBuffer("\\tanh{");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION:
			value = new StringBuffer(
					mathtt(maskSpecialChars(astnode.getName())));
			StringBuffer args = new StringBuffer(toLaTeX(model, astnode
					.getLeftChild()));
			for (int i = 1; i < astnode.getNumChildren(); i++) {
				args.append(", ");
				args.append(toLaTeX(model, astnode.getChild(i)));
			}
			args = brackets(args);
			value.append(args);
			return value;

		case AST_LAMBDA:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			// mathtt(maskLaTeXspecialSymbols(astnode.getName())) == LAMBDA!!!
			// value.append('(');
			for (int i = 1; i < astnode.getNumChildren() - 1; i++) {
				value.append(", ");
				value.append(toLaTeX(model, astnode.getChild(i)));
			}
			value = brackets(value);
			value.append(" = ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_LOGICAL_AND:
			return mathematicalOperation(astnode, model, "\\wedge ");
		case AST_LOGICAL_XOR:
			return mathematicalOperation(astnode, model, "\\oplus ");
		case AST_LOGICAL_OR:
			return mathematicalOperation(astnode, model, "\\lor ");
		case AST_LOGICAL_NOT:
			value = new StringBuffer("\\neg ");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;

		case AST_FUNCTION_PIECEWISE:
			value = new StringBuffer("\\begin{dcases}");
			value.append(newLine);
			for (int i = 0; i < astnode.getNumChildren() - 1; i++) {
				value.append(toLaTeX(model, astnode.getChild(i)));
				value.append(((i % 2) == 0) ? " & \\text{if\\ } " : lineBreak);
			}
			value.append(toLaTeX(model, astnode.getChild(astnode
					.getNumChildren() - 1)));
			if ((astnode.getNumChildren() % 2) == 1) {
				value.append(" & \\text{otherwise}");
				value.append(newLine);
			}
			value.append("\\end{dcases}");
			return value;

		case AST_RELATIONAL_EQ:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append(" = ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_RELATIONAL_GEQ:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append(" \\geq ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_RELATIONAL_GT:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append(" > ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_RELATIONAL_NEQ:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append(" \\neq ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_RELATIONAL_LEQ:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append(" \\leq ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_RELATIONAL_LT:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append(" < ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_UNKNOWN:
			return mathtext(" unknown ");

		default:
			return value = new StringBuffer();
		}
	}

	/**
	 * For a given empty list of function identifiers this method performs dfs
	 * to detect all functions called by the expression hidden in the given
	 * ASTNode and returns the filled list of function identifiers.
	 * 
	 * @param ast
	 * @param funcIDs
	 * @return
	 */
	public List<String> callsFunctions(ASTNode ast, List<String> funcIDs) {
		if (ast.getType() == AST_FUNCTION)
			funcIDs.add(ast.getName());
		else
			for (int i = 0; i < ast.getNumChildren(); i++)
				funcIDs.addAll(callsFunctions(ast.getChild(i), funcIDs));
		return funcIDs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.displaysbml.DisplaySBML#subsubsection(java.lang.String,
	 * boolean)
	 */
	public StringBuffer subsubsection(String title, boolean numbering) {
		return heading("subsubsection", title, numbering);
	}

	public StringBuffer subsection(String title, boolean numbering) {
		return heading("subsection", title, numbering);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.displaysbml.DisplaySBML#section(java.lang.String, boolean)
	 */
	public StringBuffer section(String title, boolean numbering) {
		return heading("section", title, numbering);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jcell.client.io.DisplaySBML#format(org.sbml.libsbml.SBMLDocument,
	 * java.io.BufferedWriter)
	 */
	public void format(SBMLDocument doc, BufferedWriter buffer)
			throws IOException {
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
		buffer.append(Long.toString(doc.getLevel()));
		buffer.append(" Version ");
		buffer.append(Long.toString(doc.getVersion()));
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
		if (doc.getModel() != null)
			format(doc.getModel(), buffer);

		documentFoot(doc, buffer);
		headTail = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jcell.client.io.DisplaySBML#format(org.sbml.libsbml.Model,
	 * java.io.BufferedWriter)
	 */
	public void format(Model model, BufferedWriter buffer) throws IOException {
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
					SBOParser.getSBOTermName(model.getSBOTerm()),
					leftQuotationMark, rightQuotationMark));
			buffer.append(isVocal(sboModelName.charAt(0)) ? "n " : " ");
			buffer.append(sboModelName);
			buffer.append(". Its SBO term is ");
			buffer.append(getSBOnumber(model.getSBOTerm()));
			buffer
					.append(". See Section~\\ref{sec:glossary} for the definition.");
			buffer.newLine();
		}

		if (model.isSetModelHistory()) {
			// buffer.append(subsection("Model History", false));
			ModelHistory history = model.getModelHistory();
			if ((history.getNumCreators() > 0) || (history.isSetCreatedDate())) {
				buffer.append("This model was ");
				if (history.getNumCreators() > 0) {
					buffer.append("created by ");
					if (history.getNumCreators() > 1) {
						buffer.append("the following ");
						buffer
								.append(getWordForNumber(history
										.getNumCreators()));
						buffer.append(" authors: ");
					}
					for (long i = 0; i < history.getNumCreators(); i++) {
						if (history.getNumCreators() > 1
								&& (i == history.getNumCreators() - 1)) {
							if (1 < i)
								buffer.append(',');
							buffer.append(" and ");
						} else if (i > 0)
							buffer.append(", ");
						format(history.getCreator(i), buffer);
					}
					buffer.newLine();
				}
				if (history.isSetCreatedDate()) {
					buffer.append("at ");
					format(history.getCreatedDate(), buffer);
					if (history.isSetModifiedDate())
						buffer.append(" and ");
				}
			}
			if (history.isSetModifiedDate()) {
				buffer.append("last modified at ");
				format(history.getModifiedDate(), buffer);
			}
			if ((history.getNumCreators() > 0)
					&& !(history.isSetCreatedDate() || history
							.isSetModifiedDate()))
				buffer.append('.');
			buffer.newLine();
		}

		buffer.append("Table~\\ref{tab:components} ");
		double random = Math.random();
		if (random <= 0.33)
			buffer.append("provides");
		else if (random <= 0.66)
			buffer.append("shows");
		else
			buffer.append("gives");
		buffer
				.append(" an overview of the quantities of all components of this model.");
		buffer.newLine();
		buffer.append("\\begin{table}[h!]");
		buffer.newLine();
		buffer.append("\\centering");
		buffer.newLine();
		buffer.append("\\caption{The SBML components in this model.}");
		buffer.append("\\label{tab:components}");
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
		buffer.append(Long.toString(model.getNumCompartmentTypes()));
		buffer.append("&compartments&");
		buffer.append(Long.toString(model.getNumCompartments()));
		buffer.append(lineBreak);
		buffer.append("species types&");
		buffer.append(Long.toString(model.getNumSpeciesTypes()));
		buffer.append("&species&");
		buffer.append(Long.toString(model.getNumSpecies()));
		buffer.append(lineBreak);
		buffer.append("events&");
		buffer.append(Long.toString(model.getNumEvents()));
		buffer.append("&constraints&");
		buffer.append(Long.toString(model.getNumConstraints()));
		buffer.append(lineBreak);
		buffer.append("reactions&");
		buffer.append(Long.toString(model.getNumReactions()));
		buffer.append("&function definitions&");
		buffer.append(Long.toString(model.getNumFunctionDefinitions()));
		buffer.append(lineBreak);
		buffer.append("global parameters&");
		buffer.append(Long.toString(model.getNumParameters()));
		buffer.append("&unit definitions&");
		buffer.append(Long.toString(model.getNumUnitDefinitions()));
		buffer.append(lineBreak);
		buffer.append("rules&");
		buffer.append(Long.toString(model.getNumRules()));
		buffer.append("&initial assignments&");
		buffer.append(Long.toString(model.getNumInitialAssignments()));
		buffer.append(lineBreak);
		buffer.append("\\bottomrule\\end{tabular}");
		buffer.newLine();
		buffer.append("\\end{table}");
		buffer.newLine();

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
			for (long i = 0; i < model.getNumCVTerms(); i++)
				format(model.getCVTerm(i), buffer);
		}

		format(model.getListOfUnitDefinitions(), buffer);
		format(model.getListOfCompartmentTypes(), buffer, true);
		format(model.getListOfCompartments(), buffer, true);
		format(model.getListOfSpeciesTypes(), buffer, true);
		format(model.getListOfSpecies(), buffer, true);
		format(model.getListOfParameters(), buffer, true);
		format(model.getListOfInitialAssignments(), buffer, true);
		format(model.getListOfFunctionDefinitions(), buffer, true);
		format(model.getListOfRules(), buffer, true);
		format(model.getListOfEvents(), buffer);
		format(model.getListOfConstraints(), buffer, true);
		if (model.getNumReactions() == 0)
			model.getListOfReactions().setSBMLDocument(model.getSBMLDocument());
		format(model.getListOfReactions(), buffer, true);
		if (headTail)
			documentFoot(model.getSBMLDocument(), buffer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jcell2.client.io.DisplaySBML#format(org.sbml.libsbml.ListOf,
	 * java.lang.String, java.io.BufferedWriter, boolean)
	 */
	public void format(ListOf list, BufferedWriter buffer, boolean section)
			throws IOException {
		long i;
		boolean compartments = list instanceof ListOfCompartments;
		boolean species = list instanceof ListOfSpecies;
		boolean reactions = list instanceof ListOfReactions;
		boolean parameters = list instanceof ListOfParameters;
		if (headTail)
			documentHead(list.getSBMLDocument(), buffer);
		if (list.size() > 0) {
			String name = list.get(0).getElementName().toLowerCase();
			boolean setLandscape = false;
			if (paperSize.equals("executive") || paperSize.equals("legal")
					|| paperSize.equals("letter"))
				setLandscape = true;
			else {
				short size = Short.parseShort(Character.toString(paperSize
						.charAt(1)));
				char variant = paperSize.charAt(0);
				if ((size >= 4)
						&& ((variant == 'a') || (variant == 'b')
								|| (variant == 'c') || (variant == 'd')))
					setLandscape = true;
			}
			setLandscape = setLandscape && !landscape;
			if (species || reactions) {
				if (setLandscape) {
					buffer.newLine();
					buffer.append("\\begin{landscape}");
				}
				buffer.newLine();
			} else if (list instanceof ListOfRules)
				name = "rule";
			else if (list instanceof ListOfFunctionDefinitions)
				name = "function definition";
			else if (name.endsWith("type"))
				name = name.substring(0, name.length() - 4) + " type";

			if (section) {
				buffer.append(section(firstLetterUpperCase(name)
						+ ((list.size() > 1) && (!name.endsWith("s")) ? "s"
								: ""), true));
				if (compartments || species || reactions || parameters)
					buffer.append("This model contains ");
				else
					buffer.append("This is an overview of ");
				buffer.append(getWordForNumber(list.size()));
				if (parameters)
					buffer.append(" global");
				buffer.append(name.charAt(0) == ' ' ? name : " " + name);
				buffer.append((list.size() > 1) && (!name.endsWith("s")) ? "s."
						: ".");
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
								.append(list.getModel()
										.getNumSpeciesWithBoundaryCondition() > 1 ? "ese"
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

			if (compartments)
				buffer
						.append(longtableHead("@{}lllC{2cm}llcl@{}",
								"Properties of all compartments.",
								"Id&Name&SBO&Spatial Dimensions&Size&Unit&Constant&Outside"));
			else if (species)
				buffer
						.append(longtableHead(
								paperSize.equals("letter")
										|| paperSize.equals("executive") ? "@{}p{3.5cm}p{6cm}p{4.5cm}p{2.5cm}C{1.5cm}C{1.5cm}@{}"
										: "@{}p{3.5cm}p{6.5cm}p{5cm}p{3cm}C{1.5cm}C{1.5cm}@{}",
								"Properties of each species.",
								"Id&Name&Compartment&Derived Unit&Constant&Boundary Condition"));
			else if (reactions) {
				buffer.append(" All reactions are listed in the following");
				buffer.append(" table and are subsequently described in");
				buffer.append(" detail. If a reaction is affected by one");
				buffer.append(" or more modifiers, the ");
				if (printNameIfAvailable)
					buffer.append("names or---if not specified---the");
				buffer.append(" identifiers of the modifier species are");
				buffer.append(" written above the reaction arrow.");
				buffer.newLine();
				buffer.append(longtableHead("rp{3cm}p{7cm}p{8cm}p{1.5cm}",
						"Overview of all reactions",
						"\\numero&Id&Name&Reaction Equation&SBO"));
			} else if (parameters) {
				long preDecimal = 1, postDecimal = 1;
				for (i = 0; i < list.size(); i++) {
					String[] value = Double.toString(
							((Parameter) list.get(i)).getValue()).split("\\.");
					if (value[0].length() > preDecimal)
						preDecimal = value[0].length();
					if (value[1].length() > postDecimal) {
						postDecimal = value[1].length();
						if (value[1].contains("E"))
							postDecimal += 2;
					}
				}
				String head;
				if (paperSize.equals("executive"))
					head = "p{2cm}p{3cm}cR{";
				else
					head = "p{2.5cm}p{3cm}cR{";
				head += Long.toString(preDecimal) + "}{"
						+ Long.toString(Math.min(postDecimal, 3));
				head += (paperSize.equals("executive")) ? "}p{2.8cm}c"
						: "}p{3cm}c";
				buffer
						.append(longtableHead(
								head,
								"Properties of each parameter.",
								"\\multicolumn{1}{l}{Id}&Name&SBO&\\multicolumn{1}{c}{Value}&Unit&\\multicolumn{1}{c}{Constant}"));
			}

			/*
			 * Iterate over all elements in the list and format them
			 * appropriately
			 */
			for (i = 0; i < list.size(); i++) {
				SBase s = list.get(i);
				if (section
						&& !(compartments || species || reactions || parameters)) {
					buffer.append(subsection(firstLetterUpperCase(name) + ' '
							+ texttt(maskSpecialChars(s.getId())), true));
					if (s instanceof Rule) {
						buffer.newLine();
						buffer.append("\\label{rule" + s.getId() + "}");
					}
				}
				if (parameters) {
					buffer.append(texttt(maskSpecialChars(s.getId())));
					buffer.append('&');
					if (s.isSetName())
						buffer.append(maskSpecialChars(s.getName()));
					buffer.append('&');
					if (s.isSetSBOTerm()) {
						buffer.append(getSBOnumber(s.getSBOTerm()));
						sboTerms.add(Integer.valueOf(s.getSBOTerm()));
					}
					buffer.append('&');
					Parameter p = (Parameter) s;
					String value = Double.toString(p.getValue());
					buffer.append(value.contains("E") ? "\\multicolumn{1}{r}{"
							+ format(p.getValue()) + "}" : value);
					buffer.append('&');
					UnitDefinition ud = p.getDerivedUnitDefinition();
					if ((ud == null) || (ud.getNumUnits() == 0)) {
						if (p.isSetUnits()) {
							if ((ud = p.getModel().getUnitDefinition(
									p.getUnits())) != null)
								buffer.append(math(format(ud)));
							else if (Unit.isBuiltIn(p.getUnits(), p.getLevel()))
								buffer
										.append(math(format(new Unit(p
												.getUnits()))));
							else
								buffer.append(texttt(maskSpecialChars(p
										.getUnits())));
						} else
							buffer.append(' ');
					} else
						buffer.append(math(format(ud)));
					buffer.append('&');
					buffer.append(p.getConstant() ? yes : no);
					buffer.append(lineBreak);
				} else if (compartments) {
					Compartment c = (Compartment) s;
					if (c.isSetId())
						buffer.append(texttt(maskSpecialChars(c.getId())));
					buffer.append('&');
					buffer.append(maskSpecialChars(c.getName()));
					buffer.append('&');
					if (c.isSetSBOTerm()) {
						buffer.append(getSBOnumber(c.getSBOTerm()));
						sboTerms.add(Integer.valueOf(c.getSBOTerm()));
					}
					buffer.append('&');
					buffer.append(Long.toString(c.getSpatialDimensions()));
					buffer.append('&');
					buffer.append(format(c.getSize()));
					buffer.append('&');
					UnitDefinition ud;
					if (c.isSetUnits())
						ud = c.getModel().getUnitDefinition(c.getUnits());
					else
						ud = c.getDerivedUnitDefinition();
					if ((ud == null) || (ud.getNumUnits() == 0)) {
						buffer.append(' ');
					} else if (ud.isVariantOfVolume()
							&& (ud.getNumUnits() == 1) && (c.getSize() == 1.0)
							&& (ud.getUnit(0).isLitre()))
						buffer.append("litre");
					else
						buffer.append(math(format(ud)));
					buffer.append('&');
					buffer.append(c.getConstant() ? yes : no);
					buffer.append('&');
					buffer.append(texttt(maskSpecialChars(c.getOutside())));
					buffer.append(lineBreak);
				} else if (species) {
					String mask = maskSpecialChars(s.getId());
					buffer.append(texttt(mask));
					buffer.append('&');
					buffer.append(maskSpecialChars(s.getName()));
					buffer.append('&');
					buffer.append(texttt(maskSpecialChars(((Species) s)
							.getCompartment())));
					buffer.append('&');
					buffer.append(math(format(((Species) s)
							.getDerivedUnitDefinition())));
					buffer.append('&');
					buffer.append(((Species) s).getConstant() ? yes : no);
					buffer.append('&');
					buffer.append(((Species) s).getBoundaryCondition() ? yes
							: no);
					buffer.append(lineBreak);
				} else if (reactions) {
					buffer.append(Long.toString(i + 1));
					buffer.append('&');
					Reaction r = (Reaction) list.get(i);
					// buffer.append("\\hyperref[v");
					// buffer.append(Long.toString(i + 1));
					// buffer.append("]{");
					buffer.append(texttt(maskSpecialChars(r.getId())));
					// buffer.append("}&");
					buffer.append('&');
					buffer.append(r.isSetName() ? maskSpecialChars(r.getName())
							: " ");
					buffer.append("&\\ce{");
					buffer.append(reactionEquation(r));
					buffer.append("}&");
					if (r.isSetSBOTerm()) {
						buffer.append(getSBOnumber(r.getSBOTerm()));
						sboTerms.add(Integer.valueOf(r.getSBOTerm()));
					}
					buffer.append(lineBreak);
				} else if (s instanceof Constraint) {
					Constraint c = (Constraint) s;
					buffer.append(descriptionBegin);
					format(c, buffer, true);
					buffer.append(descriptionItem("Message", formatHTML(
							c.getMessageString()).toString()));
					buffer.append(descriptionItem("Equation", equation(toLaTeX(
							c.getModel(), c.getMath()))));
					buffer.append(descriptionEnd);
				} else if (s instanceof FunctionDefinition)
					format((FunctionDefinition) s, buffer);
				else if (s instanceof InitialAssignment)
					format((InitialAssignment) s, buffer);
				else if (s instanceof Rule)
					format((Rule) s, buffer);
				else if ((s instanceof SpeciesType)
						|| (s instanceof CompartmentType)) {
					boolean isSpecType = s instanceof SpeciesType;
					StringBuffer sb = new StringBuffer();
					long j, counter = 0;
					if (isSpecType)
						for (j = 0; j < s.getModel().getNumSpecies(); j++) {
							Species spec = s.getModel().getSpecies(j);
							if (spec.isSetSpeciesType()
									&& spec.getSpeciesType().equals(s.getId())) {
								sb
										.append(texttt(maskSpecialChars(spec
												.getId())));
								sb.append('&');
								if (spec.isSetName())
									sb.append(maskSpecialChars(spec.getName()));
								sb.append(lineBreak);
								counter++;
							}
						}
					else
						for (j = 0; j < s.getModel().getNumCompartments(); j++) {
							Compartment c = s.getModel().getCompartment(j);
							if (c.isSetCompartmentType()
									&& c.getCompartmentType().equals(s.getId())) {
								sb.append(texttt(maskSpecialChars(c.getId())));
								sb.append('&');
								if (c.isSetName())
									sb.append(maskSpecialChars(c.getName()));
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
						buffer.append(longtableHead("@{}ll@{}",
								(isSpecType ? "Species" : "Compartments")
										+ " of this type", "Id&Name"));
						buffer.append(sb);
						buffer.append(bottomrule);
					}
				} else
					format(s, buffer, false);
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
		if (reactions)
			format((ListOfReactions) list, buffer);
		if (compartments)
			format((ListOfCompartments) list, buffer);
		if (headTail)
			documentFoot(list, buffer);
	}

	/**
	 * 
	 */
	public void format(ListOfEvents eventList, BufferedWriter buffer)
			throws IOException {
		if (headTail)
			documentHead(eventList.getSBMLDocument(), buffer);
		if (eventList.size() > 0) {
			LinkedList<StringBuffer>[] events = new LinkedList[(int) eventList
					.size()];
			long i;
			buffer.append(section((eventList.size() > 1) ? "Events" : "Event",
					true));
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
			for (i = 0; i < eventList.size(); i++) {
				ev = (Event) eventList.get(i);
				events[(int) i] = new LinkedList<StringBuffer>();
				events[(int) i].add(toLaTeX(ev.getModel(), ev.getTrigger()
						.getMath()));
				for (int j = 0; j < ev.getNumEventAssignments(); j++)
					events[(int) i].add(toLaTeX(ev.getModel(), ev
							.getEventAssignment(j).getMath()));
			}
			String var;
			for (i = 0; i < events.length; i++) {
				ev = (Event) eventList.get(i);
				buffer.append(subsection("Event "
						+ texttt(maskSpecialChars(ev.getId())), true));
				buffer.append("\\label{event" + ev.getId() + "}");
				buffer.newLine();
				format(ev, buffer, false);
				buffer.append("\\textbf{\\sffamily Trigger condition}");
				buffer.append(equation(events[(int) i].get(0)));
				if (ev.isSetDelay()) {
					buffer.append("\\textbf{\\sffamily Delay}");
					buffer.append(equation(toLaTeX(ev.getModel(), ev.getDelay()
							.getMath())));
					UnitDefinition ud = ev.getDelay()
							.getDerivedUnitDefinition();
					if ((ud != null) && (ud.getNumUnits() > 0)) {
						buffer
								.append("\\textbf{\\sffamily Time unit of the delay} ");
						buffer.append(math(format(ud)));
					}
				}
				buffer.append("\\textbf{\\sffamily Assignment");
				if (ev.getNumEventAssignments() > 1) {
					buffer.append("s}");
					buffer.newLine();
					buffer.append("\\begin{align}");
				} else {
					buffer.append('}');
					buffer.append(eqBegin);
				}
				for (int j = 0; j < events[(int) i].size() - 1; j++) {
					var = ev.getEventAssignment(j).getVariable();
					Model model = ev.getModel();
					if (model.getSpecies(var) != null) {
						Species species = model.getSpecies(var);
						if (species.getHasOnlySubstanceUnits())
							buffer.append('[');
						buffer.append(mathtt(maskSpecialChars(model.getSpecies(
								var).getId())));
						if (species.getHasOnlySubstanceUnits())
							buffer.append(']');
					} else if (model.getCompartment(var) != null)
						buffer.append(getSize(model.getCompartment(var)));
					else
						buffer.append(mathtt(maskSpecialChars(var)));
					buffer.append((ev.getNumEventAssignments() > 1) ? " =& "
							: " = ");
					buffer.append(events[(int) i].get(j + 1));
					if (j < events[(int) i].size() - 2)
						buffer.append(lineBreak);
				}
				if (ev.getNumEventAssignments() == 1)
					buffer.append(eqEnd);
				else {
					buffer.append("\\end{align}");
					buffer.newLine();
				}
			}
		}
		buffer.newLine();
		if (headTail)
			documentFoot(eventList, buffer);
	}

	/**
	 * Creates a readable format of all unit definitions within the given list.
	 * 
	 * @param listOfUnits
	 * @param buffer
	 * @throws IOException
	 */
	public void format(ListOfUnitDefinitions listOfUnits, BufferedWriter buffer)
			throws IOException {
		if (headTail)
			documentHead(listOfUnits.getSBMLDocument(), buffer);
		List<String> defaults = new Vector<String>();
		UnitDefinition def;
		if (showPredefinedUnitDeclarations) {
			Unit u;
			String notes = " is the predefined SBML unit for <tt>";
			if (listOfUnits.get("substance") == null) {
				def = new UnitDefinition("substance");
				def.addUnit(new Unit("mole"));
				def.setNotes("Mole" + notes + def.getId() + "</tt>.");
				listOfUnits.append(def);
				defaults.add(def.getId());
			}
			if (listOfUnits.get("volume") == null) {
				def = new UnitDefinition("volume");
				def.addUnit(new Unit("litre"));
				def.setNotes("Litre" + notes + def.getId() + "</tt>.");
				listOfUnits.append(def);
				defaults.add(def.getId());
			}
			if ((listOfUnits.get("area") == null)
					&& (listOfUnits.getLevel() > 1)) {
				def = new UnitDefinition("area");
				u = new Unit("metre");
				u.setExponent(2);
				def.addUnit(u);
				def.setNotes("Square metre" + notes + def.getId()
						+ "</tt> since SBML Level 2 Version 1.");
				listOfUnits.append(def);
				defaults.add(def.getId());
			}
			if ((listOfUnits.get("length") == null)
					&& (listOfUnits.getLevel() > 1)) {
				def = new UnitDefinition("length");
				def.addUnit(new Unit("metre"));
				def.setNotes("Metre" + notes + def.getId()
						+ "</tt> since SBML Level 2 Version 1.");
				listOfUnits.append(def);
				defaults.add(def.getId());
			}
			if (listOfUnits.get("time") == null) {
				def = new UnitDefinition("time");
				def.addUnit(new Unit("second"));
				def.setNotes("Second" + notes + def.getId() + "</tt>.");
				listOfUnits.append(def);
				defaults.add(def.getId());
			}
		}
		if (0 < listOfUnits.size()) {
			buffer.append(section(listOfUnits.size() > 1 ? "Unit Definitions"
					: "Unit Definition", true));
			buffer.append("This is an overview of ");
			buffer.append(getWordForNumber(listOfUnits.size()));
			buffer.append(" unit definition");
			if (listOfUnits.size() > 1)
				buffer.append('s');
			buffer.append('.');
			buffer.newLine();
			if (0 < defaults.size()) {
				if (defaults.size() < listOfUnits.size()) {
					buffer.append("The unit");
					if (defaults.size() > 1)
						buffer.append('s');
					buffer.append(' ');
					for (int i = 0; i < defaults.size(); i++) {
						if ((0 < i) && (i < defaults.size() - 1))
							buffer.append(", ");
						else if (i == defaults.size() - 1) {
							if (2 < defaults.size())
								buffer.append(',');
							buffer.append(" and ");
						}
						buffer.append(texttt(defaults.get(i)));
					}
					buffer.append(defaults.size() > 1 ? " are " : " is ");
				} else
					buffer.append("All units are ");
				buffer.append("predefined by SBML and not");
				buffer.append(" mentioned in the model.");
			}
			for (long i = 0; i < listOfUnits.size(); i++) {
				def = (UnitDefinition) listOfUnits.get(i);
				buffer.append(subsection("Unit "
						+ texttt(maskSpecialChars(def.getId())), true));
				buffer.append(descriptionBegin);
				format(def, buffer, true);
				if (def.getNumUnits() > 0)
					buffer.append(descriptionItem("Definition",
							math(format(def))));
				buffer.append(descriptionEnd);
			}
		}
		if (headTail)
			documentFoot(listOfUnits, buffer);
	}

	/**
	 * Returns a properly readable unit definition.
	 * 
	 * @param def
	 * @return
	 */
	public StringBuffer format(UnitDefinition def) {
		StringBuffer buffer = new StringBuffer();
		for (long j = 0; j < def.getNumUnits(); j++) {
			buffer.append(format((Unit) def.getListOfUnits().get(j)));
			if (j < def.getListOfUnits().size() - 1)
				buffer.append("\\cdot ");
		}
		return buffer;
	}

	/**
	 * Returns a unit.
	 * 
	 * @param u
	 * @return
	 */
	public StringBuffer format(Unit u) {
		StringBuffer buffer = new StringBuffer();
		boolean standardScale = (u.getScale() == 18) || (u.getScale() == 12)
				|| (u.getScale() == 9) || (u.getScale() == 6)
				|| (u.getScale() == 3) || (u.getScale() == 2)
				|| (u.getScale() == 1) || (u.getScale() == 0)
				|| (u.getScale() == -1) || (u.getScale() == -2)
				|| (u.getScale() == -3) || (u.getScale() == -6)
				|| (u.getScale() == -9) || (u.getScale() == -12)
				|| (u.getScale() == -15) || (u.getScale() == -18);
		if (u.getOffset() != 0d) {
			buffer.append(format(u.getOffset()).toString()
					.replaceAll("\\$", ""));
			if ((u.getMultiplier() != 0) || (!standardScale))
				buffer.append('+');
		}
		if (u.getMultiplier() != 1d) {
			if (u.getMultiplier() == -1d)
				buffer.append('-');
			else {
				buffer.append(format(u.getMultiplier()).toString().replaceAll(
						"\\$", ""));
				buffer.append(!standardScale ? "\\cdot " : "\\;");
			}
		}
		if (u.isKilogram()) {
			u.setScale(u.getScale() + 3);
			u.setKind(UNIT_KIND_GRAM);
		}
		if (!u.isDimensionless()) {
			switch (u.getScale()) {
			case 18:
				buffer.append(mathrm('E'));
				break;
			case 15:
				buffer.append(mathrm('P'));
				break;
			case 12:
				buffer.append(mathrm('T'));
				break;
			case 9:
				buffer.append(mathrm('G'));
				break;
			case 6:
				buffer.append(mathrm('M'));
				break;
			case 3:
				buffer.append(mathrm('k'));
				break;
			case 2:
				buffer.append(mathrm('h'));
				break;
			case 1:
				buffer.append(mathrm("da"));
				break;
			case 0:
				break;
			case -1:
				buffer.append(mathrm('d'));
				break;
			case -2:
				buffer.append(mathrm('c'));
				break;
			case -3:
				buffer.append(mathrm('m'));
				break;
			case -6:
				buffer.append("\\upmu");
				break;
			case -9:
				buffer.append(mathrm('n'));
				break;
			case -12:
				buffer.append(mathrm('p'));
				break;
			case -15:
				buffer.append(mathrm('f'));
				break;
			case -18:
				buffer.append(mathrm('a'));
				break;
			default:
				buffer.append("10^{");
				buffer.append(Integer.toString(u.getScale()));
				buffer.append("}\\cdot ");
				break;
			}
			switch (u.getKind()) {
			case UNIT_KIND_AMPERE:
				buffer.append(mathrm('A'));
				break;
			case UNIT_KIND_BECQUEREL:
				buffer.append(mathrm("Bq"));
				break;
			case UNIT_KIND_CANDELA:
				buffer.append(mathrm("cd"));
				break;
			case UNIT_KIND_CELSIUS:
				buffer.append("\\text{\\textcelsius}");
				break;
			case UNIT_KIND_COULOMB:
				buffer.append(mathrm('C'));
				break;
			case UNIT_KIND_DIMENSIONLESS:
				break;
			case UNIT_KIND_FARAD:
				buffer.append(mathrm('F'));
				break;
			case UNIT_KIND_GRAM:
				buffer.append(mathrm('g'));
				break;
			case UNIT_KIND_GRAY:
				buffer.append(mathrm("Gy"));
				break;
			case UNIT_KIND_HENRY:
				buffer.append(mathrm('H'));
				break;
			case UNIT_KIND_HERTZ:
				buffer.append(mathrm("Hz"));
				break;
			case UNIT_KIND_INVALID:
				buffer.append(mathrm("invalid"));
				break;
			case UNIT_KIND_ITEM:
				buffer.append(mathrm("item"));
				break;
			case UNIT_KIND_JOULE:
				buffer.append(mathrm('J'));
				break;
			case UNIT_KIND_KATAL:
				buffer.append(mathrm("kat"));
				break;
			case UNIT_KIND_KELVIN:
				buffer.append(mathrm('K'));
				break;
			// case UNIT_KIND_KILOGRAM:
			// buffer.append("\\mathrm{kg}");
			// break;
			case UNIT_KIND_LITER:
				buffer.append(mathrm('l'));
				break;
			case UNIT_KIND_LITRE:
				buffer.append(mathrm('l'));
				break;
			case UNIT_KIND_LUMEN:
				buffer.append(mathrm("lm"));
				break;
			case UNIT_KIND_LUX:
				buffer.append(mathrm("lx"));
				break;
			case UNIT_KIND_METER:
				buffer.append(mathrm('m'));
				break;
			case UNIT_KIND_METRE:
				buffer.append(mathrm('m'));
				break;
			case UNIT_KIND_MOLE:
				buffer.append(mathrm("mol"));
				break;
			case UNIT_KIND_NEWTON:
				buffer.append(mathrm('N'));
				break;
			case UNIT_KIND_OHM:
				buffer.append("\\upOmega");
				break;
			case UNIT_KIND_PASCAL:
				buffer.append(mathrm("Pa"));
				break;
			case UNIT_KIND_RADIAN:
				buffer.append(mathrm("rad"));
				break;
			case UNIT_KIND_SECOND:
				buffer.append(mathrm('s'));
				break;
			case UNIT_KIND_SIEMENS:
				buffer.append(mathrm('S'));
				break;
			case UNIT_KIND_SIEVERT:
				buffer.append(mathrm("Sv"));
				break;
			case UNIT_KIND_STERADIAN:
				buffer.append(mathrm("sr"));
				break;
			case UNIT_KIND_TESLA:
				buffer.append(mathrm('T'));
				break;
			case UNIT_KIND_VOLT:
				buffer.append(mathrm('V'));
				break;
			case UNIT_KIND_WATT:
				buffer.append(mathrm('W'));
				break;
			case UNIT_KIND_WEBER:
				buffer.append(mathrm("Wb"));
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
				&& (u.getExponent() != 1d))
			buffer = brackets(buffer);
		if (u.getExponent() != 1) {
			buffer.append("^{");
			buffer.append(Integer.toString(u.getExponent()));
			buffer.append('}');
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
	 * @return the size of the paper to be used.
	 */
	public String getPaperSize() {
		return paperSize;
	}

	/**
	 * Returns true if the abstract syntax tree contains a node with the given
	 * name or id. To this end, the AST is traversed recursively.
	 * 
	 * @param id
	 * @param math
	 * @return
	 */
	public boolean contains(String id, ASTNode math) {
		if ((math.getType() == AST_NAME) && (math.getName().equals(id)))
			return true;
		for (int i = 0; i < math.getNumChildren(); i++)
			if (contains(id, math.getChild(i)))
				return true;
		return false;
	}

	/**
	 * @return true if names instead of ids are displayed in formulas and
	 *         reaction equations if available, i.e., the respective SBase has a
	 *         name attribute.
	 */
	public boolean isPrintNameIfAvailable() {
		return printNameIfAvailable;
	}

	/**
	 * Returns true if the given <code>UnitDefinition</code> is a variant of
	 * substance per time.
	 * 
	 * @param ud
	 * @return
	 */
	public boolean isVariantOfSubstancePerTime(UnitDefinition ud) {
		boolean perTime = false;
		UnitDefinition newUnit = new UnitDefinition(), testUnit, unitDefinition = (UnitDefinition) ud
				.cloneObject();
		UnitDefinition.simplify(unitDefinition);
		for (long i = 0; i < unitDefinition.getNumUnits(); i++) {
			Unit unit = unitDefinition.getUnit(i);
			testUnit = new UnitDefinition();
			testUnit.addUnit((Unit) unit.cloneObject());
			testUnit.getUnit(0).setExponent(1);
			if (testUnit.isVariantOfTime()) {
				if (unit.getExponent() == -1)
					perTime = true;
			} else
				newUnit.addUnit((Unit) unit.cloneObject());
		}
		return newUnit.isVariantOfSubstance() && perTime;
	}

	/**
	 * @return true if landscape format for the whole document is to be used.
	 */
	public boolean isLandscape() {
		return landscape;
	}

	/**
	 * @return true if implicitly declared units should be made explicit.
	 */
	public boolean isAddPredefinedUnitDeclarations() {
		return showPredefinedUnitDeclarations;
	}

	/**
	 * @return true if ids are written in type writer font.
	 */
	public boolean isTypeWriter() {
		return typewriter;
	}

	/**
	 * @return true if an extra title page is created false otherwise.
	 */
	public boolean isTitlepage() {
		return titlepage;
	}

	/**
	 * If this method returns true, this exporter performs a consistency check
	 * of the given SBML file and writes all errors and warnings found to at the
	 * end of the document.
	 * 
	 * @return
	 */
	public boolean isCheckConsistency() {
		return checkConsistency;
	}

	/**
	 * Lets you decide weather or not MIRIAM annotations should be included into
	 * the model report
	 * 
	 * @return
	 */
	public boolean isSetIncludeMiriam() {
		return includeMIRIAM;
	}

	/**
	 * This switch allows to change the way how the reactants, modifiers and
	 * products are presented in each reaction. If true, one table is created
	 * containing the identifiers of each reactant, modifier and product
	 * together with the respective name. If false (default), a subsection for
	 * each one of the three groups of participants is created giving all
	 * details of each participant.
	 * 
	 * @return The state of this switch.
	 */
	public boolean isArrangeReactionParticipantsInOneTable() {
		return arrangeReactionParticipantsInOneTable;
	}

	/**
	 * This switch allows to change the way how the reactants, modifiers and
	 * products are presented in each reaction. If true, one table is created
	 * containing the identifiers of each reactant, modifier and product
	 * together with the respective name. If false (default), a subsection for
	 * each one of the three groups of participants is created giving all
	 * details of each participant.
	 * 
	 * @param arrangeReactionParticipantsInOneTable
	 *            True if the participants of the reactions should be arranged
	 *            in small tables, false if a subsection should be created for
	 *            the three groups.
	 */
	public void setArrangeReactionParticipantsInOneTable(
			boolean arrangeReactionParticipantsInOneTable) {
		this.arrangeReactionParticipantsInOneTable = arrangeReactionParticipantsInOneTable;
	}

	/**
	 * Tells you if MIRIAM annotations will be included when generating a model
	 * report
	 * 
	 * @param includeMiriam
	 */
	public void setIncludeMiriam(boolean includeMiriam) {
		this.includeMIRIAM = includeMiriam;
	}

	/**
	 * This method allows you to set the MIRIAM XML file to be parsed for MIRIAM
	 * identifiers.
	 * 
	 * @param path
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static void setMIRIAMfile(String path) throws JDOMException,
			IOException {
		miriam.setMIRIAMfile(path);
	}

	/**
	 * If set to true, an SBML consistency check of the document is performed
	 * and all errors found will be written at the end of the document.
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
	 *            values are:
	 *            <ul>
	 *            <li>8</li>
	 *            <li>9</li>
	 *            <li>10</li>
	 *            <li>11</li>
	 *            <li>12</li>
	 *            <li>14</li>
	 *            <li>16</li>
	 *            <li>17</li>
	 *            </ul>
	 *            Other values are set to the default of 11.
	 * 
	 */
	public void setFontSize(short fontSize) {
		if ((fontSize < 8) || (fontSize == 13) || (17 < fontSize))
			this.fontSize = 11;
		this.fontSize = fontSize;
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
					|| ((paperSize.charAt(0) != 'a')
							&& (paperSize.charAt(0) != 'b')
							&& (paperSize.charAt(0) != 'c') && (paperSize
							.charAt(0) != 'd')))
				this.paperSize = "a4";
			else {
				short size = Short.parseShort(Character.toString(paperSize
						.charAt(1)));
				if ((0 <= size) && (size < 10))
					this.paperSize = paperSize;
				else
					this.paperSize = "a4";
			}
		} else
			this.paperSize = "a4";
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
	 * If true an extra title page is created. Default false.
	 * 
	 * @param titlepage
	 */
	public void setTitlepage(boolean titlepage) {
		this.titlepage = titlepage;
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
	 * If true predefined SBML units will be made explicitly if not overridden
	 * in the model.
	 * 
	 * @param showImplicitUnitDeclarations
	 */
	public void setShowImplicitUnitDeclarations(
			boolean showImplicitUnitDeclarations) {
		this.showPredefinedUnitDeclarations = showImplicitUnitDeclarations;
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
	 * 
	 * @return
	 */
	public String getHeadingsFont() {
		return fontHeadings;
	}

	/**
	 * Allows you to change the font in headlines. Default: Helvetica.
	 * 
	 * @param fontHeadings
	 *            possible values are: cmss, avant, helvetica = helvet, times,
	 *            palatino
	 */
	public void setHeadingsFont(String fontHeadings) {
		fontHeadings = fontHeadings.toLowerCase();
		if (fontHeadings.equals("helvetica"))
			fontHeadings = "helvet";
		if (fontHeadings.equals("cmss") || fontHeadings.equals("avant")
				|| fontHeadings.equals("helvet")
				|| fontHeadings.equals("times")
				|| fontHeadings.equals("palatino"))
			this.fontHeadings = fontHeadings;
		else
			System.err.println("Unsupported font " + fontHeadings + ". Using "
					+ this.fontHeadings + ".");
	}

	/**
	 * 
	 * @return The font to be used for standard text.
	 */
	public String getTextFont() {
		return fontText;
	}

	/**
	 * Sets the font of the standard text to the given value. Default: Times.
	 * 
	 * @param fontText
	 *            possible values: "Computer Modern Roman" = cmr, times =
	 *            mathptmx, palatino = mathpazo, zapf = chancery, bookman,
	 *            charter, newcent = "New Century Schoolbook" and utopia
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
		else if (fontText.equals("new century schoolbook"))
			fontText = "newcent";
		if (fontText.equals("bookman") || fontText.equals("chancery")
				|| fontText.equals("charter") || fontText.equals("cmr")
				|| fontText.equals("mathpazo") || fontText.equals("mathptmx")
				|| fontText.equals("newcent") || fontText.equals("utopia"))
			this.fontText = fontText;
		else
			System.err.println("Unsupported font " + fontText + ". Using "
					+ this.fontText + ".");
	}

	/**
	 * 
	 * @return The typewriter font to be used in the document.
	 */
	public String getTypewriterFont() {
		return fontTypewriter;
	}

	/**
	 * Allows to change the typewriter font to be used. Default: CMT.
	 * 
	 * @param fontTypewriter
	 *            Possible values are: cmt, courier
	 */
	public void setTypewriterFont(String fontTypewriter) {
		fontTypewriter = fontTypewriter.toLowerCase();
		if (fontTypewriter.equals("courier") || fontTypewriter.equals("cmt"))
			this.fontTypewriter = fontTypewriter;
		else
			System.err.println("Unsupported font " + fontTypewriter
					+ ". Using " + this.fontTypewriter + ".");
	}

	/**
	 * This allows you to set the path of the logo file. It is more convenient
	 * to omit the file extension here so that LaTeX or PDFLaTeX can choose the
	 * desired file from the directory.
	 * 
	 * @param logoFilePath
	 *            Example: /home/user/logos/mylogo
	 */
	public static void setLogoFile(String logoFilePath) {
		logo = logoFilePath;
	}

	public static String getLogoFile() {
		return logo;
	}

	/**
	 * Returns the location of the SBO definition file.
	 * 
	 * @return
	 */
	public static String getSBOFile() {
		return SBOParser.getSBOOboFile();
	}

	@Override
	public StringBuffer texttt(String id) {
		return !typewriter ? new StringBuffer(id) : super.texttt(id);
	}

	@Override
	public StringBuffer mathtt(String id) {
		return !typewriter ? mathrm(id) : super.mathtt(id);
	}

	/**
	 * Checks whether a unit with the given kind is one of the base units (given
	 * SBML Level and Version from the model) and returns the corresponding
	 * LaTeX formated unit string or "Unknown unit" if the given kind string
	 * cannot be mapped to any known unit.
	 * 
	 * @param kind
	 * @param model
	 * @return
	 */
	private StringBuffer unitTest(String kind, Model model) {
		if (Unit.isUnitKind(kind, model.getLevel(), model.getVersion()))
			return format(new Unit(kind));
		else if (Unit.isBuiltIn(kind, model.getLevel()))
			return format(Unit.convertToSI(new Unit(kind)));
		return new StringBuffer("Unknown unit " + kind);
	}

	/**
	 * 
	 * @param list
	 * @param buffer
	 * @throws IOException
	 */
	private void format(ListOfCompartments list, BufferedWriter buffer)
			throws IOException {
		StringBuffer description;
		for (long i = 0; i < list.size(); i++) {
			Compartment c = (Compartment) list.get(i);
			buffer.append(subsection("Compartment "
					+ texttt(maskSpecialChars(c.getId())), true));
			buffer.append("This is a");
			String dimension = getWordForNumber(c.getSpatialDimensions());
			if (isVocal(dimension.charAt(0)))
				buffer.append('n');
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
			if (!c.getConstant())
				buffer.append("not ");
			buffer.append("constant ");
			if (c.isSetSize()) {
				buffer.append("size of ");
				if (c.getSize() - ((long) c.getSize()) == 0)
					buffer.append(getWordForNumber((long) c.getSize()));
				else
					buffer.append(format(c.getSize()));
			} else
				buffer.append("size given in");
			String unitDef = format(c.getDerivedUnitDefinition()).toString();
			if (unitDef.equals("\\mathrm{l}"))
				unitDef = "litre";
			else
				unitDef = math(unitDef).toString();
			buffer.append("\\,");
			buffer.append(unitDef);
			if (c.isSetOutside()) {
				buffer.append(", which is surrounded by ");
				description = texttt(maskSpecialChars(c.getOutside()));
				Compartment outside = c.getModel().getCompartment(
						c.getOutside());
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
	 * This method formats MIRIAM annotations or other annotations stored in CV
	 * terms (controlled vocabulary).
	 * 
	 * @param cv
	 * @param buffer
	 * @throws IOException
	 */
	private void format(CVTerm cv, BufferedWriter buffer) throws IOException {
		XMLAttributes resources = cv.getResources();
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
				buffer.append(resources.getLength() == 1 ? "a part" : "parts");
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
			/*
			 * case 9: buffer.append("occurs in"); break;
			 */
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
		if (resources.getLength() > 1) {
			buffer.append(":\\begin{itemize}");
			buffer.newLine();
			item = "\\item ";
		} else
			buffer.append(' ');
		for (int i = 0; i < resources.getLength(); i++) {
			String identifier = resources.getValue(i);
			if (!identifier.startsWith("urn"))
				identifier = miriam.getMiriamURI(identifier);
			String urls[] = miriam.getLocations(identifier);
			identifier = maskSpecialChars(identifier, false);
			if (urls != null) {
				buffer.append(item);
				if (urls.length >= 1) {
					buffer.append(href(urls[0].replaceAll("\\%", "\\\\%"),
							texttt(identifier).toString()));
					buffer.append('.');
				} else
					buffer.append(" no URL available for resource identifier "
							+ texttt(identifier));
			}
			buffer.newLine();
		}
		if (resources.getLength() > 1)
			buffer.append("\\end{itemize}");
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
		buffer.append(getMonthName((short) date.getMonth()));
		buffer.append(' ');
		buffer.append(getNumbering(date.getDay()));
		buffer.append(' ');
		buffer.append(Long.toString(date.getYear()));
		buffer.append(" at ");
		if ((date.getMinute() == 0) || (date.getMinute() == 60)) {
			if (date.getHour() == 12)
				buffer.append("noon");
			else if (date.getHour() == 24)
				buffer.append("midnight");
			else {
				buffer.append(getWordForNumber((date.getHour() > 12) ? date
						.getHour() - 12 : date.getHour()));
				buffer.append(" o' clock in the ");
				buffer.append(date.getHour() > 12 ? "afternoon" : "morning");
			}
			buffer.append('.');
		} else {
			buffer.append(Long
					.toString((date.getHour() > 12) ? date.getHour() - 12
							: date.getHour()));
			buffer.append(':');
			if (date.getMinute() < 10)
				buffer.append('0');
			buffer.append(Long.toString(date.getMinute()));
			buffer.append(date.getHour() > 12 ? "~p.\\,m." : "~a.\\,m.");
		}
		buffer.append(' ');
	}

	/**
	 * 
	 * @param creator
	 * @param buffer
	 * @throws IOException
	 */
	private void format(ModelCreator creator, BufferedWriter buffer)
			throws IOException {
		if (creator.isSetGivenName())
			buffer.append(creator.getGivenName());
		if (creator.isSetFamilyName()) {
			if (creator.isSetGivenName())
				buffer.append(' ');
			buffer.append(creator.getFamilyName());
		}
		if ((creator.isSetGivenName() || creator.isSetFamilyName())
				&& (creator.isSetOrganisation() || creator.isSetEmail()))
			buffer.append("\\footnote{");
		if (creator.isSetOrganisation())
			buffer.append(creator.getOrganisation());
		if (creator.isSetEmail()) {
			if (creator.isSetOrganisation())
				buffer.append(", ");
			buffer.append(href("mailto:" + creator.getEmail(), "\\nolinkurl{"
					+ creator.getEmail() + '}'));
		}
		if ((creator.isSetGivenName() || creator.isSetFamilyName())
				&& (creator.isSetOrganisation() || creator.isSetEmail()))
			buffer.append('}');
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
	private void formatReactionsInvolved(Model model, long speciesIndex,
			List<Long>[] reactantsReaction, List<Long>[] productsReaction,
			List<Long>[] modifierReaction, BufferedWriter buffer)
			throws IOException {
		buffer.append("This species takes part in ");
		int numReactants = reactantsReaction[(int) speciesIndex].size();
		int numProducts = productsReaction[(int) speciesIndex].size();
		int numModifiers = modifierReaction[(int) speciesIndex].size();
		final long numReactionsInvolved = numReactants + numProducts
				+ numModifiers;
		buffer.append(getWordForNumber(numReactionsInvolved));
		buffer.append(" reaction");
		if (numReactionsInvolved > 1)
			buffer.append('s');
		buffer.append(" (");
		Reaction reaction;
		boolean noComma = false;
		for (long i = 0, reactionIndex; i < numReactionsInvolved; i++) {
			if (i < numReactants) {
				if (i == 0) {
					buffer.append("as a reactant in ");
					noComma = true;
				} else
					noComma = false;
				reactionIndex = reactantsReaction[(int) speciesIndex].get(
						(int) i).longValue();
			} else if (i < numReactants + numProducts) {
				if (i == numReactants) {
					if (0 < i)
						buffer.append(" and ");
					buffer.append("as a product in ");
					noComma = true;
				} else
					noComma = false;
				reactionIndex = productsReaction[(int) speciesIndex].get(
						(int) i - numReactants).longValue();
			} else {
				if (i == numReactants + numProducts) {
					if (0 < i)
						buffer.append(" and ");
					buffer.append("as a modifier in ");
					noComma = true;
				} else
					noComma = false;
				reactionIndex = modifierReaction[(int) speciesIndex].get(
						(int) i - numReactants - numProducts).longValue();
			}
			reaction = model.getReaction(reactionIndex - 1);
			if ((0 < i) && (!noComma)) {
				buffer.append(", ");
			} else
				buffer.append(' ');
			buffer.append("\\hyperref[v");
			buffer.append(Long.toString(reactionIndex));
			buffer.append("]{");
			buffer.append(texttt(maskSpecialChars(reaction.getId())));
			buffer.append('}');
		}
		buffer.append(')');
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
		try {
			StringWriter st = new StringWriter();
			BufferedWriter bw = new BufferedWriter(st);
			BufferedReader br = new BufferedReader(new StringReader(note));
			HTML2LaTeX.convert(br, bw);
			br.close();
			bw.close();
			StringBuffer sb = st.getBuffer();
			int index = sb.indexOf("\\begin{document}");
			if (index > -1)
				sb.delete(0, index + 16);
			index = sb.indexOf("\\end{document}");
			if (index > -1)
				sb.delete(index, sb.length());
			return sb;
		} catch (FatalErrorException exc) {
			exc.printStackTrace();
		}
		return new StringBuffer();
	}

	/**
	 * Colores a velocity if necessary.
	 * 
	 * @param k
	 * @param notSubstancePerTimeUnit
	 * @param notExistingKineticLaw
	 * @param equationBW
	 * @throws IOException
	 */
	private void formatVelocity(Long k, boolean notSubstancePerTimeUnit,
			boolean notExistingKineticLaw, BufferedWriter equationBW)
			throws IOException {
		// if (notSubstancePerTimeUnit || notExistingKineticLaw)
		equationBW.append("\\hyperref[v" + Long.toString(k) + "]{");
		if (notSubstancePerTimeUnit)
			equationBW.append("\\colorbox{lightgray}{$");
		else if (notExistingKineticLaw)
			equationBW.append("\\textcolor{red}{");
		equationBW.append("v_{");
		equationBW.append(Long.toString(k));
		equationBW.append('}');
		if (notExistingKineticLaw)
			equationBW.append("}}");
		else if (notSubstancePerTimeUnit)
			equationBW.append("$}}");
		else
			equationBW.append('}');
	}

	/**
	 * Formats name, SBO term and notes as items of a description environment.
	 * 
	 * @param sBase
	 * @param buffer
	 * @param onlyItems
	 *            If true items will be written otherwise this will be
	 *            surrounded by a description environment.
	 * @throws IOException
	 */
	private void format(SBase sBase, BufferedWriter buffer, boolean onlyItems)
			throws IOException {
		if (sBase.isSetName() || sBase.isSetNotes() || sBase.isSetSBOTerm()
				|| ((sBase.getNumCVTerms() > 0 && includeMIRIAM))) {
			if (!onlyItems)
				buffer.append(descriptionBegin);
			if (sBase.isSetName())
				buffer.append(descriptionItem("Name", maskSpecialChars(sBase
						.getName())));
			if (sBase.isSetSBOTerm()) {
				buffer.append(descriptionItem("SBO:"
						+ getSBOnumber(sBase.getSBOTerm()),
						correctQuotationMarks(SBOParser.getSBOTermName(sBase
								.getSBOTerm()), leftQuotationMark,
								rightQuotationMark)));
				sboTerms.add(Integer.valueOf(sBase.getSBOTerm()));
			}
			if (sBase.isSetNotes())
				buffer.append(descriptionItem("Notes", formatHTML(
						sBase.getNotesString()).toString()));
			if ((sBase.getNumCVTerms() > 0) && includeMIRIAM) {
				StringWriter description = new StringWriter();
				BufferedWriter bw = new BufferedWriter(description);
				for (long i = 0; i < sBase.getNumCVTerms(); i++) {
					format(sBase.getCVTerm(i), bw);
					bw.newLine();
				}
				bw.close();
				buffer
						.append(descriptionItem("MIRIAM Annotation",
								description));
			}
			if (!onlyItems)
				buffer.append(descriptionEnd);
		}
	}

	/**
	 * 
	 * @param reactionList
	 * @param buffer
	 * @throws IOException
	 */
	private void format(ListOfReactions reactionList, BufferedWriter buffer)
			throws IOException {
		long reactionIndex, speciesIndex, sReferenceIndex;
		Species species;
		HashMap<String, Long> speciesIDandIndex = new HashMap<String, Long>();
		Model model = reactionList.getModel();
		if (model.getNumSpecies() > 0) {
			List<Long>[] reactantsReaction = new List[(int) model
					.getNumSpecies()];
			List<Long>[] productsReaction = new List[(int) model
					.getNumSpecies()];
			List<Long>[] modifiersReaction = new List[(int) model
					.getNumSpecies()];
			boolean notSubstancePerTimeUnit = false, notExistingKineticLaw = false;

			for (speciesIndex = 0; speciesIndex < model.getNumSpecies(); speciesIndex++) {
				speciesIDandIndex.put(model.getSpecies(speciesIndex).getId(),
						Long.valueOf(speciesIndex));
				reactantsReaction[(int) speciesIndex] = new Vector<Long>();
				productsReaction[(int) speciesIndex] = new Vector<Long>();
				modifiersReaction[(int) speciesIndex] = new Vector<Long>();
			}
			for (reactionIndex = 0; reactionIndex < reactionList.size(); reactionIndex++) {
				Reaction r = (Reaction) reactionList.get(reactionIndex);
				buffer.append(format(r, reactionIndex));
				if (!r.isSetKineticLaw())
					notExistingKineticLaw = true;
				else if (!isVariantOfSubstancePerTime(r.getKineticLaw()
						.getDerivedUnitDefinition()))
					notSubstancePerTimeUnit = true;
				for (sReferenceIndex = 0; sReferenceIndex < r.getNumReactants(); sReferenceIndex++) {
					speciesIndex = speciesIDandIndex.get(
							r.getReactant(sReferenceIndex).getSpecies())
							.longValue();
					reactantsReaction[(int) speciesIndex].add(Long
							.valueOf(reactionIndex + 1));
				}
				for (sReferenceIndex = 0; sReferenceIndex < r.getNumProducts(); sReferenceIndex++) {
					speciesIndex = speciesIDandIndex.get(
							r.getProduct(sReferenceIndex).getSpecies())
							.longValue();
					productsReaction[(int) speciesIndex].add(Long
							.valueOf(reactionIndex + 1));
				}
				for (sReferenceIndex = 0; sReferenceIndex < r.getNumModifiers(); sReferenceIndex++) {
					speciesIndex = speciesIDandIndex.get(
							r.getModifier(sReferenceIndex).getSpecies())
							.longValue();
					modifiersReaction[(int) speciesIndex].add(Long
							.valueOf(reactionIndex + 1));
				}
			}

			// writing Equations
			buffer.append(section(
					(model.getNumSpecies() > 1) ? "Derived Rate Equations"
							: "Derived Rate Equation", true));
			buffer.append("\\label{sec:DerivedRateEquations}");
			buffer.newLine();
			buffer.append("When interpreted as an ordinary differential ");
			buffer.append("equation framework, this model implies ");
			buffer.append("the following ");
			if (reactionList.size() == 1)
				buffer.append("equation");
			else
				buffer.append("set of equations");
			buffer.append(" for the rate");
			if (model.getNumSpecies() > 1)
				buffer.append("s of change of each ");
			else
				buffer.append(" of change of the following ");
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
				buffer.append("\\colorbox{lightgray}{gray} ");
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
				buffer.append(subsection("Species "
						+ texttt(maskSpecialChars(species.getId())), true));
				buffer.append(descriptionBegin);
				format(species, buffer, true);
				if (species.isSetInitialConcentration()) {
					String text = format(species.getInitialConcentration())
							.toString().replaceAll("\\$", "");
					if ((model.getUnitDefinition("substance") != null)
							|| species.isSetSubstanceUnits()) {
						text += "\\;";
						UnitDefinition ud;
						if (species.isSetSubstanceUnits()
								&& (model.getUnitDefinition(species
										.getSubstanceUnits()) == null)) {
							ud = new UnitDefinition();
							if (Unit.isUnitKind(species.getSubstanceUnits(),
									species.getLevel(), species.getVersion()))
								ud
										.addUnit(new Unit(species
												.getSubstanceUnits()));
							// else: something's wrong.
						} else
							ud = new UnitDefinition(species
									.isSetSubstanceUnits() ? model
									.getUnitDefinition(species
											.getSubstanceUnits()) : model
									.getUnitDefinition("substance"));
						Compartment compartment = model.getCompartment(species
								.getCompartment());
						for (long i = 0; i < compartment
								.getDerivedUnitDefinition().getNumUnits(); i++) {
							Unit unit = new Unit(compartment
									.getDerivedUnitDefinition().getUnit(i));
							unit.setExponent(-unit.getExponent());
							ud.addUnit(unit);
						}
						text += format(ud);
					}
					buffer.append(descriptionItem("Initial concentration",
							math(text)));
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
					buffer
							.append(descriptionItem("Initial amount",
									math(text)));
				}
				if (species.isSetCharge())
					buffer.append(descriptionItem("Charge", Integer
							.toString(species.getCharge())));
				if (species.isSetSpeciesType()) {
					SpeciesType type = model.getSpeciesType(species
							.getSpeciesType());
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
					hasInitialAssignment = model.getInitialAssignment(i)
							.getSymbol().equals(species.getId());
					if (hasInitialAssignment)
						break;
				}
				if (hasInitialAssignment)
					buffer.append(descriptionItem("Initial assignment",
							texttt(maskSpecialChars(model.getInitialAssignment(
									i - 1).getId()))));

				// =========== R U L E S and E V E N T S =================

				// Events, in which this species is involved in
				Vector<String> eventsInvolved = new Vector<String>();
				for (i = 0; i < model.getNumEvents(); i++) {
					Event event = model.getEvent(i);
					for (j = 0; j < event.getNumEventAssignments(); j++)
						if (event.getEventAssignment(j).getVariable().equals(
								species.getId()))
							eventsInvolved.add(event.getId());
				}
				if (eventsInvolved.size() > 0) {
					buffer.append("\\item[Involved in event");
					if (eventsInvolved.size() > 1)
						buffer.append('s');
					buffer.append("] ");
					for (i = 0; i < eventsInvolved.size(); i++) {
						String id = eventsInvolved.get(i);
						buffer.append("\\hyperref[event" + id + "]{");
						buffer.append(texttt(maskSpecialChars(id)));
						buffer.append('}');
						if (i < eventsInvolved.size() - 1)
							buffer.append(", ");
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

				Vector<String> rulesInvolved = new Vector<String>();
				for (i = 0; i < model.getNumRules(); i++) {
					Rule rule = model.getRule(i);
					if (rule instanceof AlgebraicRule) {
						if (contains(species.getId(), rule.getMath()))
							rulesInvolved.add(rule.getId());
					} else if (rule.getVariable().equals(species.getId()))
						rulesInvolved.add(rule.getId());
				}
				if (rulesInvolved.size() > 0) {
					buffer.append("\\item[Involved in rule");
					if (rulesInvolved.size() > 1)
						buffer.append('s');
					buffer.append("] ");
					for (i = 0; i < rulesInvolved.size(); i++) {
						String id = rulesInvolved.get(i);
						buffer.append("\\hyperref[rule" + id + "]{");
						buffer.append(texttt(maskSpecialChars(id)));
						buffer.append('}');
						if (i < rulesInvolved.size() - 1)
							buffer.append(", ");
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
						KineticLaw kl = r.getKineticLaw();
						if (kl != null)
							notSubstancePerTimeUnit = !isVariantOfSubstancePerTime(kl
									.getDerivedUnitDefinition());
						else
							notExistingKineticLaw = true;
					} else
						notExistingKineticLaw = true;
					equationBW.flush();
					if (equation.getBuffer().length() > 0)
						equationBW.append(" + ");
					SpeciesReference product = r.getProduct(species.getId());
					if (product.isSetStoichiometryMath()) {
						ast = product.getStoichiometryMath().getMath();
						if (ast.getType() == AST_PLUS
								|| ast.getType() == AST_MINUS)
							equationBW.append(brackets(toLaTeX(model, ast)));
						else
							equationBW.append(toLaTeX(model, ast));
					} else {
						double doubleStoch = product.getStoichiometry();
						if (doubleStoch != 1d)
							equationBW.append(format(doubleStoch).toString()
									.replaceAll("\\$", ""));
					}
					formatVelocity(reactionIndex, notSubstancePerTimeUnit,
							notExistingKineticLaw, equationBW);
				}
				for (i = 0; i < reactantsReaction[(int) speciesIndex].size(); i++) {
					reactionIndex = reactantsReaction[(int) speciesIndex]
							.get(i) - 1;
					Reaction r = model.getReaction(reactionIndex);
					notSubstancePerTimeUnit = notExistingKineticLaw = false;
					if (r != null) {
						KineticLaw kl = r.getKineticLaw();
						if (kl != null) {
							notSubstancePerTimeUnit = !isVariantOfSubstancePerTime(kl
									.getDerivedUnitDefinition());
						} else
							notExistingKineticLaw = true;
					} else
						notExistingKineticLaw = true;

					SpeciesReference reactant = r.getReactant(species.getId());
					equationBW.append('-');
					if (reactant.isSetStoichiometryMath()) {
						ast = reactant.getStoichiometryMath().getMath();
						if (ast.getType() == AST_PLUS
								|| ast.getType() == AST_MINUS)
							equationBW.append(brackets(toLaTeX(model, ast)));
						else
							equationBW.append(toLaTeX(model, ast));
					} else {
						double doubleStoch = reactant.getStoichiometry();
						if (doubleStoch != 1.0)
							equationBW.append(format(doubleStoch).toString()
									.replaceAll("\\$", ""));
					}
					formatVelocity(reactionIndex + 1, notSubstancePerTimeUnit,
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
							formatReactionsInvolved(model, speciesIndex,
									reactantsReaction, productsReaction,
									modifiersReaction, buffer);
							buffer.append(", which do");
							if (numReactionsInvolved == 1)
								buffer.append("es");
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
						if ((rulesInvolved.size() > 0)
								|| (eventsInvolved.size() > 0)) {
							buffer
									.append("This species' quantity is affected by ");
							if (rulesInvolved.size() > 0) {
								buffer.append(getWordForNumber(rulesInvolved
										.size()));
								buffer.append(" rule");
								if (rulesInvolved.size() > 1)
									buffer.append('s');
								if (eventsInvolved.size() > 0)
									buffer.append(" and");
							}
							if (eventsInvolved.size() > 0) {
								buffer.append(getWordForNumber(eventsInvolved
										.size()));
								buffer.append(" event");
								if (eventsInvolved.size() > 1)
									buffer.append('s');
							}
							buffer
									.append(". Please verify this SBML document.");
							buffer.newLine();
						}
					} else {
						// changes only due to rules and events
						if (numReactionsInvolved > 0)
							formatReactionsInvolved(model, speciesIndex,
									reactantsReaction, productsReaction,
									modifiersReaction, buffer);
						if ((rulesInvolved.size() > 0)
								|| (eventsInvolved.size() > 0)) {
							if (numReactionsInvolved == 1)
								buffer.append(". Not this but ");
							else if (numReactionsInvolved > 1)
								buffer.append(". Not these but ");
							if (rulesInvolved.size() > 0) {
								String number = getWordForNumber(rulesInvolved
										.size());
								if (numReactionsInvolved == 0)
									number = firstLetterUpperCase(number);
								buffer.append(number);
								buffer.append(" rule");
								if (rulesInvolved.size() > 1)
									buffer.append('s');
								if (eventsInvolved.size() > 0)
									buffer.append(" together with ");
							}
							if (eventsInvolved.size() > 0) {
								String number = getWordForNumber(eventsInvolved
										.size());
								if (numReactionsInvolved == 0)
									number = firstLetterLowerCase(number);
								buffer.append(number);
								buffer.append(" event");
								if (eventsInvolved.size() > 1)
									buffer.append('s');
							}
							if (rulesInvolved.size() == 0)
								buffer.append(" influence");
							else
								buffer.append(" determine");
							if (eventsInvolved.size() + rulesInvolved.size() == 1)
								buffer.append('s');
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
								if (numReactionsInvolved == 1)
									buffer.append("es");
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
					int numModification = modifiersReaction[(int) speciesIndex]
							.size();
					if (species.getConstant()) {
						// never changes, cannot be reactant or product and no
						// rules; but can be a modifier of reactions
						if ((rulesInvolved.size() == eventsInvolved.size())
								&& (numReactionsInvolved - numModification == 0)
								&& (numReactionsInvolved - numModification == rulesInvolved
										.size())) {
							if (0 < numModification) {
								formatReactionsInvolved(model, speciesIndex,
										reactantsReaction, productsReaction,
										modifiersReaction, buffer);
								buffer.append('.');
								buffer.newLine();
							}
							buffer.append(eqBegin);
							buffer.append("\\frac{\\mathrm d}{\\mathrm dt} ");
							buffer.append(getNameOrID(species, true));
							buffer.append(" = 0");
							buffer.append(eqEnd);
						} else {
							buffer
									.append("As this species is constant and its");
							buffer.append(" boundary condition is ");
							buffer.append(texttt("false"));
							buffer.append(" it cannot be involved in");
							boolean comma = false;
							if (rulesInvolved.size() > 0) {
								buffer.append(" any rules");
								comma = true;
							}
							if (eventsInvolved.size() > 0) {
								if (comma)
									buffer.append(", ");
								else
									comma = true;
								buffer.append(" any events");
							}
							if (numReactionsInvolved - numModification > 0) {
								if (comma)
									buffer.append(" or");
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
							for (Iterator<String> iterator = rulesInvolved
									.iterator(); iterator.hasNext();)
								if (!(model.getRule(iterator.next()) instanceof AlgebraicRule))
									allAlgebraic = false;
							String number = getWordForNumber(rulesInvolved
									.size());
							if (0 < numReactionsInvolved) {
								formatReactionsInvolved(model, speciesIndex,
										reactantsReaction, productsReaction,
										modifiersReaction, buffer);
								buffer.append(" and is also involved in ");
							} else
								number = firstLetterUpperCase(number);
							buffer.append(number);
							if (allAlgebraic)
								buffer.append(" algebraic");
							buffer.append(" rule");
							if (rulesInvolved.size() > 1)
								buffer.append('s');
							if (0 < numReactionsInvolved)
								buffer.append(" that");
							if (numReactionsInvolved - numModification == 0) {
								buffer.append(" determine");
								if (rulesInvolved.size() == 1)
									buffer.append('s');
								buffer.append(" this species' quantity.");
							} else if (!allAlgebraic) {
								buffer.append(". Please verify this SBML ");
								buffer.append("document.");
							} else {
								buffer.append('.');
								buffer.append(eqBegin);
								buffer
										.append("\\frac{\\mathrm d}{\\mathrm dt} ");
								buffer.append(getNameOrID(species, true));
								buffer.append(" = ");
								if (equation.getBuffer().length() > 0)
									buffer.append(equation.getBuffer());
								else
									buffer.append('0');
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
								formatReactionsInvolved(model, speciesIndex,
										reactantsReaction, productsReaction,
										modifiersReaction, buffer);
								buffer.append('.');
								buffer.newLine();
							}
							buffer.append(eqBegin);
							buffer.append("\\frac{\\mathrm d}{\\mathrm dt} ");
							buffer.append(getNameOrID(species, true));
							buffer.append(" = ");
							buffer
									.append((equation.getBuffer().length() > 0) ? equation
											.getBuffer()
											: "0");
							buffer.append(eqEnd);
						}
						if (eventsInvolved.size() > 0) {
							buffer.append("Furthermore, ");
							buffer.append(getWordForNumber(eventsInvolved
									.size()));
							buffer.append(" event");
							if (eventsInvolved.size() > 1)
								buffer.append('s');
							buffer.append(" influence");
							if (eventsInvolved.size() == 1)
								buffer.append('s');
							buffer.append(" this species' rate of change.");
						}
					}
				}
			}
			buffer.newLine();
		}
	}

	/**
	 * 
	 * @param a
	 * @param buffer
	 * @throws IOException
	 */
	private void format(InitialAssignment a, BufferedWriter buffer)
			throws IOException {
		buffer.append(descriptionBegin);
		format(a, buffer, true);
		buffer.append(descriptionItem("Derived unit", a
				.containsUndeclaredUnits() ? "contains undeclared units"
				: math(format(a.getDerivedUnitDefinition()))));
		buffer.append(descriptionItem("Math", math(toLaTeX(a.getModel(), a
				.getMath()))));
		buffer.append(descriptionEnd);
	}

	/**
	 * Creates a subsection with all necessary information about one reaction.
	 * 
	 * @param r
	 * @param reactionIndex
	 * @return
	 * @throws IOException
	 */
	private StringBuffer format(Reaction r, long reactionIndex)
			throws IOException {
		long i;
		StringWriter reactString = new StringWriter();
		reactString.append(subsection("Reaction "
				+ texttt(maskSpecialChars(r.getId())), true));
		reactString.append("This is a");
		if (!r.getReversible())
			reactString.append(r.getFast() ? " fast ir" : "n ir");
		else
			reactString.append(r.getFast() ? " fast " : " ");
		reactString.append("reversible reaction of ");
		reactString.append(getWordForNumber(r.getNumReactants()));
		reactString.append(" reactant");
		if (r.getNumReactants() > 1)
			reactString.append('s');
		reactString.append(" forming ");
		reactString.append(getWordForNumber(r.getNumProducts()));
		reactString.append(" product");
		if (r.getNumProducts() > 1)
			reactString.append('s');
		if (r.getNumModifiers() > 0) {
			reactString.append(" influenced by ");
			reactString.append(getWordForNumber(r.getNumModifiers()));
			reactString.append(" modifier");
			if (r.getNumModifiers() > 1)
				reactString.append('s');
		}
		reactString.append('.');

		int hasSBOReactants = 0, hasSBOProducts = 0, hasSBOModifiers = 0;
		boolean onlyItems = false;
		if (arrangeReactionParticipantsInOneTable) {
			for (i = 0; i < r.getNumReactants(); i++)
				if (r.getReactant(i).isSetSBOTerm())
					hasSBOReactants++;
			for (i = 0, hasSBOProducts = 0; i < r.getNumProducts(); i++)
				if (r.getProduct(i).isSetSBOTerm())
					hasSBOProducts++;
			for (i = 0, hasSBOModifiers = 0; i < r.getNumModifiers(); i++)
				if (r.getModifier(i).isSetSBOTerm())
					hasSBOModifiers++;
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
				if (hasSBOReactants > 1)
					reactString.append('s');
				reactString.append(" with SBO annotation] ");
				for (i = 0; i < r.getNumReactants(); i++) {
					SpeciesReference reactant = r.getReactant(i);
					if (r.getReactant(i).isSetSBOTerm()) {
						reactString.append(texttt(maskSpecialChars(reactant
								.getSpecies())));
						reactString.append(" (");
						reactString.append(getSBOnumber(reactant.getSBOTerm()));
						sboTerms.add(Integer.valueOf(reactant.getSBOTerm()));
						reactString.append(')');
						if (--hasSBOReactants > 0)
							reactString.append(", ");
					}
				}
			}
			if (hasSBOProducts > 0) {
				reactString.append("\\item[Product");
				if (hasSBOProducts > 1)
					reactString.append('s');
				reactString.append(" with SBO annotation] ");
				for (i = 0; i < r.getNumProducts(); i++) {
					SpeciesReference product = r.getProduct(i);
					if (r.getProduct(i).isSetSBOTerm()) {
						reactString.append(texttt(maskSpecialChars(product
								.getSpecies())));
						reactString.append(" (");
						reactString.append(getSBOnumber(product.getSBOTerm()));
						sboTerms.add(Integer.valueOf(product.getSBOTerm()));
						reactString.append(')');
						if (--hasSBOProducts > 0)
							reactString.append(", ");
					}
				}
			}
			if (r.getListOfModifiers().size() > 0) {
				if (hasSBOModifiers > 0) {
					reactString.append("\\item[Modifier");
					if (hasSBOModifiers > 1)
						reactString.append('s');
					reactString.append(" with SBO annotation] ");
					for (i = 0; i < r.getNumModifiers(); i++) {
						ModifierSpeciesReference m = r.getModifier(i);
						if (m.isSetSBOTerm()) {
							reactString.append(" (");
							reactString.append(getSBOnumber(m.getSBOTerm()));
							reactString.append(" ");
							reactString
									.append(maskSpecialChars(correctQuotationMarks(
											SBOParser.getSBOTermName(m
													.getSBOTerm()),
											leftQuotationMark,
											rightQuotationMark)));
							sboTerms.add(Integer.valueOf(m.getSBOTerm()));
							reactString.append(')');
							if (--hasSBOModifiers > 0)
								reactString.append(", ");
						}
					}
				}
			}
			if (onlyItems)
				reactString.append(descriptionEnd);
		}

		reactString.append(subsubsection("Reaction equation", false));
		reactString.append("\\reaction{");
		reactString.append(reactionEquation(r));
		reactString.append('}');
		reactString.append(newLine);

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
				if (r.getNumReactants() > 0)
					columns += 2;
				if (r.getNumModifiers() > 0)
					columns += 2;
				if (r.getNumProducts() > 0)
					columns += 2;
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
				} else
					headLine += "}{Reactants}";
				cols++;
			}
			if (r.getNumModifiers() > 0) {
				headLine += "\\multicolumn{2}{c";
				head += idAndNameColumn;
				if (r.getNumProducts() > 0) {
					headLine += "|}{Modifiers}&";
					head += '|';
				} else
					headLine += "}{Modifiers}";
				cols++;
			}
			if (r.getNumProducts() > 0) {
				headLine += "\\multicolumn{2}{c}{Products}";
				head += idAndNameColumn;
				cols++;
			}
			headLine += lineBreak;
			headLine += "Id&Name";
			for (i = 1; i < cols; i++)
				headLine += "&Id&Name";
			reactString.append(longtableHead(head + "@{}",
					"Overview of participating species.", headLine));
			for (i = 0; i < Math.max(r.getNumReactants(), Math.max(r
					.getNumProducts(), r.getNumModifiers())); i++) {
				Species s;
				if (r.getNumReactants() > 0) {
					if (i < r.getNumReactants()) {
						s = r.getModel().getSpecies(
								r.getReactant(i).getSpecies());
						reactString.append(texttt(maskSpecialChars(s.getId())));
						reactString.append('&');
						reactString.append(maskSpecialChars(s.getName()));
						/*
						 * reactString.append('&');
						 * reactString.append(s.isSetSBOTerm() ? getSBOnumber(s
						 * .getSBOTerm()) : " ");
						 */
					} else
						reactString.append('&');
					if ((r.getNumModifiers() > 0) || (r.getNumProducts() > 0))
						reactString.append('&');
				}
				if (r.getNumModifiers() > 0) {
					if (i < r.getNumModifiers()) {
						s = r.getModel().getSpecies(
								r.getModifier(i).getSpecies());
						reactString.append(texttt(maskSpecialChars(s.getId())));
						reactString.append('&');
						reactString.append(maskSpecialChars(s.getName()));
						/*
						 * reactString.append('&');
						 * reactString.append(s.isSetSBOTerm() ? getSBOnumber(s
						 * .getSBOTerm()) : " ");
						 */
					}
					if (r.getNumProducts() > 0)
						reactString.append('&');
				}
				if (r.getNumProducts() > 0) {
					if (i < r.getNumProducts()) {
						s = r.getModel().getSpecies(
								r.getProduct(i).getSpecies());
						reactString.append(texttt(maskSpecialChars(s.getId())));
						reactString.append('&');
						reactString.append(maskSpecialChars(s.getName()));
						/*
						 * reactString.append('&');
						 * reactString.append(s.isSetSBOTerm() ? getSBOnumber(s
						 * .getSBOTerm()) : " ");
						 */
					} else
						reactString.append('&');
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
				reactString.append(subsubsection(
						r.getNumReactants() > 1 ? "Reactants" : "Reactant",
						false));
				reactString.append(longtableHead(columnDef, caption
						+ "reactant.", headLine));
				for (i = 0; i < r.getListOfReactants().size(); i++) {
					specRef = r.getReactant(i);
					species = r.getModel().getSpecies(specRef.getSpecies());
					reactString.append(texttt(maskSpecialChars(specRef
							.getSpecies())));
					reactString.append('&');
					reactString.append(maskSpecialChars(specRef.getName()
							.length() == 0 ? species.getName() : specRef
							.getName()));
					reactString.append('&');
					if (specRef.isSetSBOTerm()) {
						reactString.append(getSBOnumber(specRef.getSBOTerm()));
						sboTerms.add(Integer.valueOf(specRef.getSBOTerm()));
					}
					reactString.append(lineBreak);
				}
				reactString.append(bottomrule);
			}
			if (r.getNumModifiers() > 0) {
				reactString.append(subsubsection(
						r.getNumModifiers() > 1 ? "Modifiers" : "Modifier",
						false));
				reactString.append(longtableHead(columnDef, caption
						+ "modifier.", headLine));
				for (i = 0; i < r.getListOfModifiers().size(); i++) {
					modRef = r.getModifier(i);
					species = r.getModel().getSpecies(modRef.getSpecies());
					reactString.append(texttt(maskSpecialChars(modRef
							.getSpecies())));
					reactString.append('&');
					reactString.append(maskSpecialChars(modRef.getName()
							.length() == 0 ? species.getName() : modRef
							.getName()));
					reactString.append('&');
					if (modRef.isSetSBOTerm()) {
						reactString.append(getSBOnumber(modRef.getSBOTerm()));
						sboTerms.add(Integer.valueOf(modRef.getSBOTerm()));
					}
					reactString.append(lineBreak);
				}
				reactString.append(bottomrule);
			}
			if (r.getNumProducts() > 0) {
				reactString
						.append(subsubsection(
								r.getNumProducts() > 1 ? "Products" : "Product",
								false));
				reactString.append(longtableHead(columnDef, caption
						+ "product.", headLine));
				for (i = 0; i < r.getListOfProducts().size(); i++) {
					specRef = r.getProduct(i);
					species = r.getModel().getSpecies(specRef.getSpecies());
					reactString.append(texttt(maskSpecialChars(specRef
							.getSpecies())));
					reactString.append('&');
					reactString.append(maskSpecialChars(specRef.getName()
							.length() == 0 ? species.getName() : specRef
							.getName()));
					reactString.append('&');
					if (specRef.isSetSBOTerm()) {
						reactString.append(getSBOnumber(specRef.getSBOTerm()));
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
			if (ud.getListOfUnits().size() == 0)
				reactString.append("not available");
			else if (kin.containsUndeclaredUnits())
				reactString.append("contains undeclared units");
			else {
				UnitDefinition.simplify(ud);
				reactString.append(math(format(ud)));
			}
			reactString.append(newLine);
			reactString.append(descriptionEnd);
			reactString.append(eqBegin);
			reactString.append("v_{" + (reactionIndex + 1) + "}=");
			if (kin.getMath() != null) {
				reactString.append(toLaTeX(r.getModel(), kin.getMath()));
				if (0 < r.getModel().getNumFunctionDefinitions())
					functionCalls = callsFunctions(kin.getMath(),
							new Vector<String>());
			} else
				reactString.append("\\text{no mathematics specified}");
			pBuffer = new BufferedWriter(localParameters);
			if (r.getKineticLaw().getNumParameters() > 0)
				format(r.getKineticLaw().getListOfParameters(), pBuffer, false);
			pBuffer.close();
		} else {
			reactString.append(eqBegin);
			reactString.append("v_{" + (reactionIndex + 1) + "}=");
			reactString.append("\\text{not specified}");
		}
		reactString.append(newLine);
		reactString.append("\\label{v");
		reactString.append(Long.toString(reactionIndex + 1));
		reactString.append('}');
		reactString.append(eqEnd);
		if (functionCalls != null)
			// If a kinetic law calls functions we add their definitions
			// redundantly right after the kinetic law.
			for (String id : functionCalls)
				reactString.append(equation(mathtt(maskSpecialChars(id)),
						toLaTeX(r.getModel(), r.getModel()
								.getFunctionDefinition(id).getMath())));
		reactString.append(localParameters.getBuffer());

		return reactString.getBuffer();
	}

	/**
	 * 
	 * @param def
	 * @param buffer
	 * @throws IOException
	 */
	private void format(FunctionDefinition def, BufferedWriter buffer)
			throws IOException {
		buffer.append(descriptionBegin);
		format(def, buffer, true);
		if ((def.getNumArguments() > 0) || (def.getBody() != null)
				|| def.isSetMath()) {
			if (def.getNumArguments() > 0) {
				buffer.append("\\item[Argument");
				buffer.append(def.getNumArguments() > 1 ? "s] " : "] ");
				for (long j = 0; j < def.getNumArguments(); j++) {
					buffer.append(math(toLaTeX(def.getModel(), def
							.getArgument(j))));
					if (j < def.getNumArguments() - 1)
						buffer.append(", ");
				}
				buffer.newLine();
				if (def.getBody() != null)
					buffer.append(descriptionItem("Mathematical Expression",
							equation(toLaTeX(def.getModel(), def.getBody()))));
			} else if (def.isSetMath()) {
				buffer.append(descriptionItem("Mathematical Formula", equation(
						getNameOrID(def, true), toLaTeX(def.getModel(), def
								.getMath()))));
			}
		}
		buffer.append(descriptionEnd);
	}

	/**
	 * Creates a mathematical equation in a math environment (not in-line).
	 * 
	 * @param formula
	 *            A formula to be displayed as an equation.
	 * @param formulae
	 *            Additional terms, for instance the following two: "=",
	 *            "some expression"
	 * @return
	 */
	private StringBuffer equation(StringBuffer formula,
			StringBuffer... formulae) {
		StringBuffer equation = new StringBuffer();
		if (formula.length() == 0)
			return equation;
		if (formula.charAt(0) != '$')
			equation.append(eqBegin);
		equation.append(formula);
		for (StringBuffer f : formulae)
			equation.append(f);
		if ((formula.charAt(formula.length() - 1) != '$')
				&& (equation.charAt(equation.length() - 1) != '$'))
			equation.append(eqEnd);
		return equation;
	}

	/**
	 * 
	 * @param rl
	 * @param buffer
	 * @throws IOException
	 */
	private void format(Rule rl, BufferedWriter buffer) throws IOException {
		buffer.append("Rule ");
		boolean hasId = false;
		if ((rl.isSetId()) && (rl.getId().length() > 0)) {
			hasId = true;
			buffer.append(texttt(maskSpecialChars(rl.getId())));
		}
		if ((rl.isSetName()) && (rl.getName().length() > 0)) {
			if (hasId)
				buffer.append(" (");
			buffer.append(maskSpecialChars(rl.getName()));
			if (hasId)
				buffer.append(')');
		}
		if (rl.isSetSBOTerm()) {
			buffer.append(" has the SBO reference ");
			buffer.append(getSBOnumber(rl.getSBOTerm()));
			sboTerms.add(Integer.valueOf(rl.getSBOTerm()));
			buffer.append(" and");
		}
		buffer.append(" is a");
		if (rl.isAlgebraic()) {
			buffer.append("n algebraic rule");
			buffer.append(equation(toLaTeX(rl.getModel(), rl.getMath()),
					new StringBuffer("\\equiv 0")));
		} else if (rl.isAssignment()) {
			buffer.append("n assignment rule for ");
			Model model = rl.getModel();
			String id = rl.getVariable();
			if (model.getSpecies(id) != null) {
				buffer.append("species ");
				buffer.append(texttt(maskSpecialChars(id)));
				buffer.append(':');
				buffer.append(eqBegin);
				Species species = model.getSpecies(id);
				if (species.getHasOnlySubstanceUnits())
					buffer.append('[');
				buffer.append(mathtt(maskSpecialChars(id)));
				if (species.getHasOnlySubstanceUnits())
					buffer.append(']');
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
			buffer.append(toLaTeX(model, rl.getMath()));
			buffer.append(eqEnd);
		} else {
			buffer.append(" rate rule for ");
			boolean hasOnlySubstanceUnits = false;
			if (rl.getModel().getSpecies(rl.getVariable()) != null) {
				buffer.append("species ");
				hasOnlySubstanceUnits = rl.getModel().getSpecies(
						rl.getVariable()).getHasOnlySubstanceUnits();
			} else if (rl.getModel().getCompartment(rl.getVariable()) != null)
				buffer.append("compartment ");
			else
				buffer.append("parameter ");
			buffer.append(texttt(maskSpecialChars(rl.getVariable())));
			buffer.append(':');
			buffer.append(eqBegin);
			buffer.append("\\frac{\\mathrm d}{\\mathrm dt} ");
			if (hasOnlySubstanceUnits)
				buffer.append('[');
			if (rl.getModel().getCompartment(rl.getVariable()) != null)
				buffer.append(getSize(rl.getModel().getCompartment(
						rl.getVariable())));
			else
				buffer.append(mathtt(maskSpecialChars(rl.getVariable())));
			if (hasOnlySubstanceUnits)
				buffer.append(']');
			buffer.append(" = ");
			buffer.append(toLaTeX(rl.getModel(), rl.getMath()));
			buffer.append(eqEnd);
		}
		if (rl.isSetNotes()
				|| ((rl.getDerivedUnitDefinition().getNumUnits() > 0) && !rl
						.containsUndeclaredUnits())) {
			buffer.append(descriptionBegin);
			if ((rl.getDerivedUnitDefinition().getNumUnits() > 0)
					&& !rl.containsUndeclaredUnits())
				buffer.append(descriptionItem("Derived unit", math(format(rl
						.getDerivedUnitDefinition()))));
			if (rl.isSetNotes())
				buffer.append(descriptionItem("Notes", formatHTML(rl
						.getNotesString())));
			if ((rl.getNumCVTerms() > 0) && includeMIRIAM) {
				buffer.append("\\item[Annotation] ");
				for (long i = 0; i < rl.getNumCVTerms(); i++)
					format(rl.getCVTerm(i), buffer);
			}
			buffer.append(descriptionEnd);
		}
	}

	/**
	 * Returns a mathematical formula if stoichiometric math is used or the
	 * formated stoichiometric coefficient of the given SpeciesReference. Be
	 * aware that dollar symbols may be set at the beginning and the end of the
	 * stoichiometric coefficient/mathematical formula if necessary. If already
	 * using math mode please remove these dollar symbols.
	 * 
	 * @param spec
	 * @return
	 * @throws IOException
	 */
	private StringBuffer formatStoichiometry(SpeciesReference spec)
			throws IOException {
		StringWriter sw = new StringWriter();
		if (spec.isSetStoichiometryMath())
			sw.append(math(toLaTeX(spec.getModel(), spec.getStoichiometryMath()
					.getMath())));
		else if (spec.getStoichiometry() != 1d)
			sw.append(format(spec.getStoichiometry()));
		sw.close();
		return sw.getBuffer();
	}

	/**
	 * This method decides if brakets are to be set. The symbol is a
	 * mathematical operator, e. g., plus, minus, multiplication etc. in LaTeX
	 * syntax (for instance
	 * 
	 * <pre>
	 * \cdot
	 * </pre>
	 * 
	 * ). It simply counts the number of descendants on the left and the right
	 * hand side of the symbol.
	 * 
	 * @param astnode
	 * @param model
	 * @param symbol
	 * @return
	 * @throws IOException
	 */
	private StringBuffer mathematicalOperation(ASTNode astnode, Model model,
			String symbol) throws IOException {
		StringBuffer value = new StringBuffer();
		if (1 < astnode.getLeftChild().getNumChildren())
			value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
		else
			value.append(toLaTeX(model, astnode.getLeftChild()));
		value.append(symbol);
		if (1 < astnode.getChild(astnode.getNumChildren() - 1).getNumChildren())
			value.append(brackets(toLaTeX(model, astnode.getChild(astnode
					.getNumChildren() - 1))));
		else
			value.append(toLaTeX(model, astnode.getChild(astnode
					.getNumChildren() - 1)));
		return value;
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
			doc.checkConsistency();
			if (doc.getNumErrors() > 0) {
				long i;
				SBMLError error;
				Vector<Long> infos = new Vector<Long>();
				Vector<Long> warnings = new Vector<Long>();
				Vector<Long> fatal = new Vector<Long>();
				Vector<Long> system = new Vector<Long>();
				Vector<Long> xml = new Vector<Long>();
				Vector<Long> internal = new Vector<Long>();
				Vector<Long> errors = new Vector<Long>();
				for (i = 0; i < doc.getNumErrors(); i++) {
					error = doc.getError(i);
					if (error.isInfo())
						infos.add(Long.valueOf(i));
					else if (error.isWarning())
						warnings.add(Long.valueOf(i));
					else if (error.isFatal())
						fatal.add(Long.valueOf(i));
					else if (error.isSystem())
						system.add(Long.valueOf(i));
					else if (error.isXML())
						xml.add(Long.valueOf(i));
					else if (error.isInternal())
						internal.add(Long.valueOf(i));
					else
						// error.isError())
						errors.add(Long.valueOf(i));
				}
				buffer.append(section("Model Consistency Report", true));
				buffer.append("The given SBML document contains ");
				buffer.append(getWordForNumber(doc.getNumErrors()));
				buffer.append(" issue");
				if (doc.getNumErrors() > 1)
					buffer.append('s');
				buffer.append(", which are listed in ");
				buffer.append("the remainder of this model report.");
				buffer.newLine();
				buffer.append("The messages and identification codes shown ");
				buffer.append("here are those reported by the ");
				buffer.append(href("http://sbml.org/Facilities/Validator",
						"SBML.org online validator"));
				buffer.append('.');
				buffer.newLine();
				if (xml.size() > 0)
					problemMessage(xml, doc, xml.size() > 1 ? "XML errors"
							: "XML error", buffer, "Error");
				if (fatal.size() > 0)
					problemMessage(fatal, doc,
							fatal.size() > 1 ? "Fatal errors" : "Fatal error",
							buffer, "Error");
				if (system.size() > 0)
					problemMessage(system, doc,
							system.size() > 1 ? "System messages"
									: "System message", buffer, "Error");
				if (internal.size() > 0)
					problemMessage(internal, doc,
							internal.size() > 1 ? "Internal problems"
									: "Internal problem", buffer, "Error");
				if (errors.size() > 0)
					problemMessage(errors, doc,
							errors.size() > 1 ? "Error messages"
									: "Error message", buffer, "Error");
				if (infos.size() > 0)
					problemMessage(infos, doc,
							infos.size() > 1 ? "Information messages"
									: "Information message", buffer,
							"Information");
				if (warnings.size() > 0)
					problemMessage(warnings, doc,
							warnings.size() > 1 ? "Warnings" : "Warning",
							buffer, "Warning");
			}
		}

		if (sboTerms.size() > 0) {
			buffer.append(section("Glossary of Systems Biology Ontology Terms",
					true));
			buffer.append("\\label{sec:glossary}");
			buffer.append(descriptionBegin);
			int sbo[] = new int[sboTerms.size()], i = 0;
			for (Integer it : sboTerms)
				sbo[i++] = it.intValue();
			Arrays.sort(sbo);
			for (int id : sbo)
				buffer
						.append(descriptionItem(
								"SBO:" + getSBOnumber(id),
								"\\textbf{"
										+ maskSpecialChars(correctQuotationMarks(
												SBOParser.getSBOTermName(id),
												leftQuotationMark,
												rightQuotationMark))
										+ ":} "
										+ maskSpecialChars(correctQuotationMarks(
												SBOParser.getSBOTermDef(id),
												leftQuotationMark,
												rightQuotationMark))));
			buffer.append(descriptionEnd);
		}
		buffer.append(imprint());
		buffer.append("\\end{document}");
		buffer.newLine();
	}

	private StringBuffer imprint() {
		StringBuffer imprint = new StringBuffer("\\begin{figure}[b!]");
		imprint.append(newLine);
		imprint.append("\\setlength{\\fboxrule}{.1cm}");
		imprint.append(newLine);
		imprint.append("\\setlength{\\fboxsep}{.3cm}");
		imprint.append(newLine);
		imprint.append("\\fcolorbox{lightgray}{white}{");
		imprint.append("\\begin{minipage}{.945\\textwidth}");
		imprint.append(newLine);
		imprint.append("% \\renewcommand{\\footnoterule}{}");
		imprint.append("% \\renewcommand{\\thempfootnote}");
		imprint.append("{\\fnsymbol{mpfootnote}}");
		imprint.append(newLine);
		imprint.append("\\footnotesize\\SBMLLaTeX{} was developed ");
		imprint.append("by Andreas Dr\\\"ager\\footnote{Center for ");
		imprint.append("Bioinformatics T\\\"ubingen (ZBIT), Germany}, ");
		imprint.append("Hannes Planatscher\\mpfootnotemark[1], ");
		imprint.append("Dieudonn\\'e M Wouamba\\mpfootnotemark[1], ");
		imprint.append("Adrian Schr\\\"oder\\mpfootnotemark[1], Michael Hucka");
		imprint.append("\\footnote{California Institute of Technology, ");
		imprint.append("Beckman Institute BNMC, Pasadena, United States}, ");
		imprint.append("Lukas Endler\\footnote{European Bioinformatics ");
		imprint.append("Institute, Wellcome Trust Genome Campus, ");
		imprint.append("Hinxton, United Kingdom}, Martin Golebiewski");
		imprint.append("\\footnote{EML Research gGmbH, Heidelberg, Germany}");
		imprint.append(", Wolfgang M{\\\"u}ller\\mpfootnotemark[4], ");
		imprint.append("and Andreas Zell\\mpfootnotemark[1]. Please see ");
		imprint.append("\\url{http://www.ra.cs.uni-tuebingen.de/software/");
		imprint.append("SBML2LaTeX} for more information.");
		imprint.append(newLine);
		imprint.append("\\end{minipage}}");
		imprint.append(newLine);
		imprint.append("\\end{figure}");
		imprint.append(newLine);
		return imprint;
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
		if (doc.isSetName()) {
			title = maskSpecialChars(doc.getName());
			titlePrefix = "Document name:";
		} else if (model != null) {
			if (model.isSetName()) {
				title = maskSpecialChars(model.getName());
				titlePrefix = "Model name:";
			}
			if ((title.length() == 0) && (model.isSetId())
					&& (model.getId().length() > 0)) {
				title = maskSpecialChars(model.getId());
				titlePrefix = "Model identifier:";
			}
		} else if (doc.isSetId() && (doc.getId().length() > 0)) {
			title = maskSpecialChars(doc.getId());
			titlePrefix = "Document identifier:";
		}
		if (title.length() == 0) {
			title = "Untitled";
			titlePrefix = "";
		} else
			titlePrefix += " ";
		buffer.append("\\documentclass[");
		buffer.append(Short.toString(fontSize));
		buffer.append("pt,twoside");
		if (titlepage)
			buffer.append(",titlepage");
		if (landscape)
			buffer.append(",landscape");
		buffer.append(',');
		buffer.append(paperSize);
		buffer.append("paper");
		if (!paperSize.equals("a4") || (fontSize < 10) || (12 < fontSize))
			buffer.append(",DIVcalc");
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
		buffer.append(space + "pdftitle={" + titlePrefix + "\"" + title
				+ "\"},");
		buffer.newLine();
		buffer.append(space
				+ "pdfauthor={Produced by SBML2LaTeX version 1.0beta},");
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
				|| fontText.equals("utopia"))
			buffer.append(usepackage(fontText));
		if (fontHeadings.equals("helvet"))
			buffer.append(usepackage(fontHeadings, "scaled=.95"));
		else if (fontHeadings.equals("avant"))
			buffer.append(usepackage(fontHeadings));
		if (fontTypewriter.equals("courier"))
			buffer.append(usepackage(fontTypewriter));
		String[] packages = new String[] { "[english]{babel}",
				"[english]{rccol}", "[version=3]{mhchem}", "{relsize}",
				"{pifont}", "{textcomp}", "{longtable}", "{tabularx}",
				"{booktabs}", "{amsmath}", "{amsfonts}", "{amssymb}",
				"{mathtools}", "{ulem}", "{wasysym}", "{eurosym}",
				"{rotating}", "{upgreek}", "{flexisym}", "{breqn}" };
		for (i = 0; i < packages.length; i++) {
			buffer.append("\\usepackage");
			buffer.append(packages[i]);
			buffer.newLine();
		}
		buffer.newLine();
		if (0 < model.getNumFunctionDefinitions()) {
			buffer
					.append("% Introduce automatic line breaks in function calls");
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
			buffer
					.append("  \\medmuskip\\Dmedmuskip \\thickmuskip\\Dthickmuskip");
			buffer.newLine();
			buffer.append("  \\let\\m@Bin\\d@@Bin \\let\\m@Rel\\d@@Rel");
			buffer.newLine();
			buffer.append("  \\let\\m@Pun\\d@@Pun %% new for punctuation");
			buffer.newLine();
			buffer
					.append("  \\let\\@symRel\\d@@symRel \\let\\@symBin\\d@@symBin");
			buffer.newLine();
			buffer
					.append("  \\let\\m@DeL\\d@@DeL \\let\\m@DeR\\d@@DeR \\let\\m@DeB\\d@@DeB");
			buffer.newLine();
			buffer.append("  \\let\\m@DeA\\d@@DeA");
			buffer.newLine();
			buffer
					.append("  \\let\\@symDeL\\d@@symDeL \\let\\@symDeR\\d@@symDeR");
			buffer.newLine();
			buffer
					.append("  \\let\\@symDeB\\d@@symDeB \\let\\@symDeA\\d@@symDeA");
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
		buffer
				.append("\\newcolumntype{C}[1]{>{\\centering\\arraybackslash}p{#1}}");
		buffer.newLine();
		buffer.append("\\newcommand{\\SBMLLaTeX}{{\\sffamily\\upshape");
		buffer.append("\\raisebox{-.35ex}{S\\hspace{-.425ex}BML}");
		buffer
				.append("\\hspace{-0.5ex}\\begin{rotate}{-17.5}\\raisebox{-.1ex}{2}");
		buffer.append("\\end{rotate}\\hspace{1ex}\\LaTeX}}");
		buffer.newLine();
		buffer.append("\\cfoot{\\textcolor{gray}{Produced by \\SBMLLaTeX}}");
		buffer.newLine();
		buffer.newLine();
		buffer.append("\\subject{SBML Model Report}");
		buffer.newLine();
		buffer.append("\\title{");
		buffer.append(titlePrefix);
		if (titlePrefix.contains("identified"))
			title = texttt(title).toString();
		else
			title = "``" + title + "\"";
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
	 * Writes a document foot for the LaTeX Document for all objects derived
	 * from {@see ListOf}.
	 * 
	 * @param listOf
	 * @param buffer
	 * @throws IOException
	 */
	private void documentFoot(ListOf listOf, BufferedWriter buffer)
			throws IOException {
		if (listOf.size() == 0) {
			buffer.append("This list of ");
			buffer.append(listOf.getElementName());
			buffer.append(' ');
			boolean hasId = false;
			if (listOf.isSetId())
				buffer.append(texttt(maskSpecialChars(listOf.getId())));
			if (listOf.isSetName()) {
				if (hasId)
					buffer.append(" (");
				buffer.append(maskSpecialChars(listOf.getName()));
				if (hasId)
					buffer.append(") ");
				else
					buffer.append(' ');
			}
			buffer.append("does not contain any entries.");
		}
		documentFoot(listOf.getSBMLDocument(), buffer);
	}

	/**
	 * Creates a subsection for the given problem class.
	 * 
	 * @param listOfErrorIndices
	 *            A list containing indices of document errors.
	 * @param doc
	 *            The SBML document containing the problems
	 * @param title
	 *            The title of a subsection for the problem class.
	 * @param buffer
	 *            the writer
	 * @param messageType
	 *            An identifier, e. g., "Error" or "Problem" or "Information"
	 *            etc.
	 * @throws IOException
	 */
	private void problemMessage(Vector<Long> listOfErrorIndices,
			SBMLDocument doc, String title, BufferedWriter buffer,
			String messageType) throws IOException {
		buffer.append(subsection(title, true));
		if (doc.isSetName())
			buffer.append("The SBML document "
					+ maskSpecialChars(doc.getName()));
		else
			buffer.append("This SBML document");
		buffer.append(" contains ");
		buffer.append(getWordForNumber(listOfErrorIndices.size()));
		buffer.append(' ');
		buffer.append(title.startsWith("XML") ? title
				: firstLetterLowerCase(title));
		buffer.append('.');
		buffer.newLine();
		buffer.append(descriptionBegin);
		Long[] errors = listOfErrorIndices.toArray(new Long[0]);
		Arrays.sort(errors);
		SBMLError error = null;
		StringBuffer message = new StringBuffer();
		for (Long e : errors) {
			SBMLError curr = doc.getError(e.longValue());
			if (!curr.equals(error)) {
				error = curr;
				message = formatErrorMessage(error.getMessage());
			}
			buffer.append(descriptionItem(messageType + ' '
					+ Long.toString(error.getErrorId()), message.toString()));
		}
		buffer.append(descriptionEnd);
		buffer.newLine();
	}

	/**
	 * 
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
				} else
					m.append("$<$");
				break;
			case '>':
				m.append("$>$");
				break;
			default:
				if ((c == '_') || (c == '\\') || (c == '$') || (c == '&')
						|| (c == '#') || (c == '{') || (c == '}') || (c == '~')
						|| (c == '%'))
					m.append('\\');
				m.append(c);
				break;
			}
		}
		return m;
	}

	/**
	 * This method returns a StringBuffer containing the reaction equation for
	 * the given reaction. Note that this equation has to be surrounded by a
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
	 */
	private StringBuffer reactionEquation(Reaction r) throws IOException {
		long i;
		StringBuffer reactString = new StringBuffer();
		if (r.getNumReactants() == 0)
			reactString.append("$\\emptyset$");
		else
			for (i = 0; i < r.getNumReactants(); i++) {
				if (r.getReactant(i) == null) {
					reactString
							.append(math("\\text{invalid species reference for reactant "
									+ getWordForNumber(i + 1) + '}'));
				} else {
					reactString.append(formatStoichiometry(r.getReactant(i)));
					reactString.append(' ');
					reactString.append(math(getNameOrID(r.getModel()
							.getSpecies(r.getReactant(i).getSpecies()), true)));
				}
				if (i < r.getNumReactants() - 1)
					reactString.append(" + ");
			}
		reactString.append(r.getReversible() ? " <=>" : " ->");
		if (r.getNumModifiers() > 0) {
			reactString.append("[\\text{");
			reactString.append(math(getNameOrID(r.getModel().getSpecies(
					r.getModifier(0).getSpecies()), true)));
			for (i = 1; i < r.getNumModifiers(); i++) {
				reactString.append(",\\;");
				reactString.append(math(getNameOrID(r.getModel().getSpecies(
						r.getModifier(i).getSpecies()), true)));
			}
			reactString.append("}] ");
		} else
			reactString.append(' ');
		if (r.getNumProducts() == 0)
			reactString.append("$\\emptyset$");
		else
			for (i = 0; i < r.getNumProducts(); i++) {
				if (r.getProduct(i) == null) {
					reactString
							.append(math("\\text{invalid species reference for product "
									+ getWordForNumber(i + 1) + '}'));
				} else {
					reactString.append(formatStoichiometry(r.getProduct(i)));
					reactString.append(' ');
					reactString.append(math(getNameOrID(r.getModel()
							.getSpecies(r.getProduct(i).getSpecies()), true)));
				}
				if (i < r.getNumProducts() - 1)
					reactString.append(" + ");
			}
		return reactString;
	}

	/**
	 * If the field printNameIfAvailable is false this method returns a the id
	 * of the given SBase. If printNameIfAvailable is true this method looks for
	 * the name of the given SBase and will return it.
	 * 
	 * @param sbase
	 *            the SBase, whose name or id is to be returned.
	 * @param mathMode
	 *            if true this method returns the name typesetted in mathmode,
	 *            i.e., mathrm for names and mathtt for ids, otherwise texttt
	 *            will be used for ids and normalfont (nothing) will be used for
	 *            names.
	 * @return The name or the ID of the SBase (according to the field
	 *         printNameIfAvailable), whose LaTeX special symbols are masked and
	 *         which is type set in typewriter font if it is an id. The mathmode
	 *         argument decides if mathtt or mathrm has to be used.
	 */
	private StringBuffer getNameOrID(SBase sbase, boolean mathMode) {
		boolean isName = printNameIfAvailable && sbase.isSetName();
		String name = maskSpecialChars(isName ? sbase.getName() : sbase.getId());
		if (!isName)
			return mathMode ? mathtt(name) : texttt(name);
		return mathMode ? mathrm(name) : new StringBuffer(name);
	}

	/**
	 * This method returns the correct LaTeX expression for a function which
	 * returns the size of a compartment. This can be a volume, an area, a
	 * length or a point.
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

}
