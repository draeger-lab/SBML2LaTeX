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

/**
 * Created at 2009-01-03.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 */
public class LaTeX extends StringOperations {

	/**
	 * An opening quotation mark.
	 */
	protected String leftQuotationMark = "``";

	/**
	 * An closing quotation mark.
	 */
	protected String rightQuotationMark = "\"";

	/**
	 * This is a LaTeX line break. The line break symbol double backslash
	 * followed by a new line symbol of the operating system.
	 */
	protected static final String lineBreak = "\\\\" + newLine;

	/**
	 * Surrounded by new line symbols. Begin equation. This type of equation
	 * requires the LaTeX package breqn. It will produce equations with
	 * automatic line breaks (LaTeX will compute the optimal place for line
	 * breaks). Unfortunately, this does not work for very long denominators.
	 */
	protected static final String eqBegin = newLine + "\\begin{dmath}"
			+ newLine; // equation

	/**
	 * End equation; cf. eqBegin. Surrounded by new line symbols.
	 */
	protected static final String eqEnd = newLine + "\\end{dmath}" + newLine; // equation

	/**
	 * Needed for the beginning of a table. Requires LaTeX package booktabs.
	 * Surounded by new line symbols.
	 */
	protected static final String toprule = newLine + "\\toprule" + newLine;

	/**
	 * Produces a fancy line in tables. Requires LaTeX package booktabs. Starts
	 * and ends with a new line.
	 */
	protected static final String midrule = newLine + "\\midrule" + newLine;

	/**
	 * Requires LaTeX package booktabs. Produces a fancy line at the bottom of a
	 * table. This variable also includes the <code>end{longtable}</code>
	 * command and a new line.
	 */
	protected static final String bottomrule = "\\bottomrule\\end{longtable}"
			+ newLine;

	/**
	 * Surrounded by new line symbols. The begin of a description environment in
	 * LaTeX.
	 */
	protected static final String descriptionBegin = "\\begin{description}"
			+ newLine;

	/**
	 * Surrounded by new line symbols. The end of a description environment.
	 */
	protected static final String descriptionEnd = "\\end{description}"
			+ newLine;

	/**
	 * The constant pi
	 */
	protected static final StringBuffer CONSTANT_PI = new StringBuffer("\\pi");

	protected static final StringBuffer CONSTANT_E = mathrm("e");

	protected static final StringBuffer CONSTANT_TRUE = mathrm("true");

	protected static final StringBuffer CONSTANT_FALSE = mathrm("false");

	protected static final StringBuffer POSITIVE_INFINITY = new StringBuffer(
			"\\infty");

	protected static final StringBuffer NEGATIVE_ININITY = new StringBuffer(
			"-\\infty");

	/**
	 * Creates head lines.
	 * 
	 * @param kind
	 *            E.g., section, subsection, subsubsection, paragraph etc.
	 * @param title
	 *            The title of the heading.
	 * @param numbering
	 *            If true a number will be placed in front of the title.
	 * @return
	 */
	protected static StringBuffer heading(String kind, String title,
			boolean numbering) {
		StringBuffer heading = new StringBuffer(newLine);
		heading.append("\\");
		heading.append(kind);
		if (!numbering)
			heading.append('*');
		heading.append('{');
		heading.append(title);
		heading.append('}');
		heading.append(newLine);
		return heading;
	}

	/**
	 * 
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	public static StringBuffer frac(Object numerator, Object denominator) {
		StringBuffer frac = new StringBuffer("\\frac{");
		frac.append(numerator);
		frac.append("}{");
		frac.append(denominator);
		frac.append('}');
		return frac;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static StringBuffer sqrt(Object value) {
		StringBuffer sqrt = new StringBuffer("\\sqrt{");
		sqrt.append(value);
		sqrt.append('}');
		return sqrt;
	}

	/**
	 * 
	 * @param root
	 * @param value
	 * @return
	 */
	public static StringBuffer root(Object root, Object value) {
		if (root.toString().equals("2"))
			return sqrt(value);
		StringBuffer sqrt = new StringBuffer("\\sqrt");
		sqrt.append('[');
		sqrt.append(root);
		sqrt.append("]{");
		sqrt.append(value);
		sqrt.append('}');
		return sqrt;
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	public static StringBuffer mathrm(String text) {
		StringBuffer mathrm = new StringBuffer("\\mathrm{");
		mathrm.append(text);
		mathrm.append('}');
		return mathrm;
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	public static StringBuffer mathtext(String text) {
		StringBuffer mathtext = new StringBuffer("\\text{");
		mathtext.append(text);
		mathtext.append('}');
		return mathtext;
	}

	/**
	 * 
	 * @param symbol
	 * @return
	 */
	public static StringBuffer mathrm(char symbol) {
		StringBuffer mathrm = new StringBuffer("\\mathrm{");
		mathrm.append(symbol);
		mathrm.append('}');
		return mathrm;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static StringBuffer abs(StringBuffer value) {
		StringBuffer abs = new StringBuffer("\\left\\lvert");
		abs.append(value);
		abs.append("\\right\\rvert");
		return abs;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static StringBuffer arccos(StringBuffer value) {
		StringBuffer arcos = new StringBuffer("\\arccos{");
		arcos.append(value);
		return arcos;
	}

	/**
	 * Without brackets.
	 * 
	 * @param func
	 * @param value
	 * @return
	 */
	private static StringBuffer function(String func, StringBuffer value) {
		StringBuffer fun = mathrm(func);
		fun.append(value);
		return fun;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static StringBuffer arccosh(StringBuffer value) {
		return function("arccosh", value);
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static StringBuffer ceiling(StringBuffer value) {
		StringBuffer ceiling = new StringBuffer("\\left\\lceil ");
		ceiling.append(value);
		ceiling.append("\\right\\rceil ");
		return ceiling;
	}

	/**
	 * 
	 * @param formula
	 * @return
	 */
	public static StringBuffer floor(StringBuffer formula) {
		StringBuffer floor = new StringBuffer("\\left\\lfloor ");
		floor.append(formula);
		floor.append("\\right\\rfloor ");
		return floor;
	}

	/**
	 * Creates a usepackage command for the given package with the optional
	 * options.
	 * 
	 * @param latexPackage
	 *            the name of the latex package
	 * @param options
	 *            options without commas
	 * @return usepackage command including system-dependent new line character.
	 */
	public static StringBuffer usepackage(String latexPackage,
			String... options) {
		StringBuffer usepackage = new StringBuffer("\\usepackage");
		if (options.length > 0) {
			usepackage.append('[');
			boolean first = true;
			for (String option : options) {
				if (!first)
					usepackage.append(',');
				else
					first = false;
				usepackage.append(option);
			}
			usepackage.append(']');
		}
		usepackage.append('{');
		usepackage.append(latexPackage);
		usepackage.append('}');
		usepackage.append(newLine);
		return usepackage;
	}

	/**
	 * Encloses the given formula in brackets.
	 * 
	 * @param formula
	 * @return
	 */
	public StringBuffer brackets(StringBuffer formula) {
		StringBuffer buffer = new StringBuffer("\\left(");
		buffer.append(formula);
		buffer.append("\\right)");
		return buffer;
	}

	/**
	 * This method returns a <code>StringBuffer</code> representing a properly
	 * LaTeX formatted number. However, if the <code>double</code> argument
	 * contains "Exx" (power of ten), then the returned value starts and ends
	 * with a dollar symbol.
	 * 
	 * @param value
	 * @return
	 */
	public StringBuffer format(double value) {
		StringBuffer sb = new StringBuffer();
		String val = Double.toString(value);
		if (val.contains("E")) {
			String split[] = val.split("E");
			val = "10^{" + format(Double.parseDouble(split[1])) + "}";
			if (split[0].equals("-1.0"))
				val = "-" + val;
			else if (!split[0].equals("1.0"))
				val = format(Double.parseDouble(split[0])) + "\\cdot " + val;
			sb.append(math(val));
		} else if (value - ((int) value) == 0)
			sb.append(((int) value));
		else
			sb.append(val);
		return sb;
	}

	/**
	 * Encloses the given formula in dollar symbols (inline math mode).
	 * 
	 * @param formula
	 * @return
	 */
	public StringBuffer math(Object formula) {
		StringBuffer math = new StringBuffer();
		String f = String.valueOf(formula);
		if (f.length() == 0)
			return math;
		if (f.charAt(0) != '$')
			math.append('$');
		math.append(f);
		if (f.charAt(f.length() - 1) != '$')
			math.append('$');
		return math;
	}

	/**
	 * Creates a head for a longtable in LaTeX.
	 * 
	 * @param columnDef
	 *            without leading and ending brackets, e.g., "lrrc",
	 * @param caption
	 *            caption of this table without leading and ending brackets
	 * @param headLine
	 *            table head without leading and ending brackets and without
	 *            double backslashes at the end
	 * @return
	 */
	public StringBuffer longtableHead(String columnDef, String caption,
			String headLine) {
		StringBuffer buffer = new StringBuffer("\\begin{longtable}[h!]{");
		buffer.append(columnDef);
		buffer.append('}');
		buffer.append(newLine);
		buffer.append("\\caption{");
		buffer.append(caption);
		buffer.append('}');
		buffer.append("\\\\");
		StringBuffer head = new StringBuffer(toprule);
		head.append(headLine);
		head.append("\\\\");
		head.append(midrule);
		buffer.append(head);
		buffer.append("\\endfirsthead");
		// buffer.append(newLine);
		buffer.append(head);
		buffer.append("\\endhead");
		// buffer.append(bottomrule);
		// buffer.append("\\endlastfoot");
		buffer.append(newLine);
		return buffer;
	}

	/**
	 * 
	 * @param string
	 * @param hyphen
	 *            if true a hyphen symbol is introduced at each position where a
	 *            special character has to be masked anyway.
	 * @return
	 */
	public static String maskSpecialChars(String string, boolean hyphen) {
		StringBuffer masked = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			char atI = string.charAt(i);
			if (atI == '<')
				masked.append("$<$");
			else if (atI == '>')
				masked.append("$>$");
			else {
				if ((atI == '_') || (atI == '\\') || (atI == '$')
						|| (atI == '&') || (atI == '#') || (atI == '{')
						|| (atI == '}') || (atI == '~') || (atI == '%')
						|| (atI == '^')) {
					if ((i == 0) || (!hyphen))
						masked.append('\\');
					else if (hyphen && (string.charAt(i - 1) != '\\'))
						masked.append("\\-\\"); // masked.append('\\');
					// } else if ((atI == '[') || (atI == ']')) {
				}
				masked.append(atI);
			}
		}
		return masked.toString().trim();
	}

	/**
	 * Masks all special characters used by LaTeX with a backslash including
	 * hyphen symbols.
	 * 
	 * @param string
	 * @return
	 */
	public static String maskSpecialChars(String string) {
		return maskSpecialChars(string, true);
	}

	/**
	 * Creates a hyper link to the given target and the text to be visible in
	 * the document.
	 * 
	 * @param target
	 *            The target to which this link points to.
	 * @param text
	 *            The text to be written in the link.
	 * @return
	 */
	public StringBuffer href(String target, String text) {
		StringBuffer href = new StringBuffer("\\href{");
		href.append(target);
		href.append("}{");
		href.append(text);
		href.append('}');
		return href;
	}

	/**
	 * This method simplifies the process of creating descriptions. There is an
	 * item entry together with a description. No new line or space is needed
	 * for separation.
	 * 
	 * @param item
	 *            e.g., "my item"
	 * @param description
	 *            e.g., "my description"
	 * @return
	 */
	public StringBuffer descriptionItem(String item, Object description) {
		StringBuffer itemBuffer = new StringBuffer("\\item[");
		itemBuffer.append(item);
		itemBuffer.append("] ");
		itemBuffer.append(description);
		itemBuffer.append(newLine);
		return itemBuffer;
	}

	/**
	 * 
	 * @param number
	 * @return
	 */
	public static String getNumbering(long number) {
		if ((Integer.MIN_VALUE < number) && (number < Integer.MAX_VALUE))
			switch ((int) number) {
			case 1:
				return "first";
			case 2:
				return "second";
			case 3:
				return "third";
			case 5:
				return "fifth";
			case 13:
				return "thirteenth";
			default:
				if (number < 13) {
					String word = StringOperations.getWordForNumber(number);
					return word.endsWith("t") ? word + 'h' : word + "th";
				}
				break;
			}
		String numberWord = Long.toString(number);
		switch (numberWord.charAt(numberWord.length() - 1)) {
		case '1':
			return StringOperations.getWordForNumber(number)
					+ "\\textsuperscript{st}";
		case '2':
			return StringOperations.getWordForNumber(number)
					+ "\\textsuperscript{nd}";
		case '3':
			return StringOperations.getWordForNumber(number)
					+ "\\textsuperscript{rd}";
		default:
			return StringOperations.getWordForNumber(number)
					+ "\\textsuperscript{th}";
		}
	}

	/**
	 * Returns the LaTeX code to set the given String in type writer font within
	 * a math environment.
	 * 
	 * @param id
	 * @return
	 */
	public StringBuffer mathtt(String id) {
		StringBuffer sb = new StringBuffer("\\mathtt{");
		sb.append(id);
		sb.append('}');
		return sb;
	}

	/**
	 * Returns the LaTeX code to set the given String in type writer font.
	 * 
	 * @param id
	 * @return
	 */
	public StringBuffer texttt(String id) {
		StringBuffer sb = new StringBuffer("\\texttt{");
		sb.append(id);
		sb.append('}');
		return sb;
	}
}
