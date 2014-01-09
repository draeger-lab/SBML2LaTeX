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
package org.sbml.tolatex.util;

import de.zbit.util.StringUtil;

/**
 * 
 * @author Andreas Dr&auml;ger
 * @date 2009-01-03
 * @version $Rev$
 */
public class LaTeX extends StringUtil {
	
	/**
	 * Requires LaTeX package booktabs. Produces a fancy line at the bottom of a
	 * table. This variable also includes the {@code \end{longtable}} command
	 * and a new line.
	 */
	public static final String bottomrule = "\\bottomrule\\end{longtable}"
			+ newLine();
	
	public static final String CONSTANT_E = mathrm("e").toString();
	
	public static final String CONSTANT_FALSE = mathrm("false").toString();
	
	/**
	 * The constant pi
	 */
	public static final String CONSTANT_PI = "\\pi";
	
	public static final String CONSTANT_TRUE = mathrm("true").toString();
	
	/**
	 * Surrounded by new line symbols. The begin of a description environment in
	 * LaTeX.
	 */
	public static final String descriptionBegin = "\\begin{description}"
			+ newLine();
	
	/**
	 * Surrounded by new line symbols. The end of a description environment.
	 */
	public static final String descriptionEnd = "\\end{description}"
			+ newLine();
	
	/**
	 * Surrounded by new line symbols. Begin equation. This type of equation
	 * requires the LaTeX package breqn. It will produce equations with automatic
	 * line breaks (LaTeX will compute the optimal place for line breaks).
	 * Unfortunately, this does not work for very long denominators.
	 */
	public static final String eqBegin = newLine() + "\\begin{dmath}"
			+ newLine(); // equation
	
	/**
	 * End equation; cf. eqBegin. Surrounded by new line symbols.
	 */
	public static final String eqEnd = newLine() + "\\end{dmath}" + newLine(); // equation
	
	/**
	 * This is a LaTeX line break. The line break symbol double backslash followed
	 * by a new line symbol of the operating system.
	 */
	public static final String lineBreak = "\\\\" + newLine();
	
	/**
	 * Produces a fancy line in tables. Requires LaTeX package booktabs. Starts
	 * and ends with a new line.
	 */
	public static final String midrule = newLine() + "\\midrule" + newLine();
	/**
	 * 
	 */
	public static final String NEGATIVE_ININITY = "-\\infty";
	/**
	 * 
	 */
	public static final String POSITIVE_INFINITY = "\\infty";
	
	/**
	 * Needed for the beginning of a table. Requires LaTeX package booktabs.
	 * Surounded by new line symbols.
	 */
	public static final String toprule = newLine() + "\\toprule" + newLine();
	
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
		return command("arccos", value);
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
	 * @param command
	 * @param what
	 * @return
	 */
	private static StringBuffer command(String command, Object what) {
		StringBuffer sb = new StringBuffer("\\");
		sb.append(command);
		sb.append('{');
		sb.append(what);
		sb.append('}');
		return sb;
	}
	
	/**
	 * 
	 * @param command
	 * @param first
	 * @param second
	 * @return
	 */
	private static StringBuffer command(String command, Object first,
		Object second) {
		StringBuffer sb = command(command, first);
		sb.append('{');
		sb.append(second);
		sb.append('}');
		return sb;
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
	 * 
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	public static StringBuffer frac(Object numerator, Object denominator) {
		return command("frac", numerator, denominator);
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
	 * @param number
	 * @return
	 */
	public static String getNumbering(int number) {
		if ((Integer.MIN_VALUE < number) && (number < Integer.MAX_VALUE)) {
			switch (number) {
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
						String word = getWordForNumber(number);
						return word.endsWith("t") ? word + 'h' : word + "th";
					}
					break;
			}
		}
		String numberWord = Long.toString(number);
		switch (numberWord.charAt(numberWord.length() - 1)) {
			case '1':
				return getWordForNumber(number) + "\\textsuperscript{st}";
			case '2':
				return getWordForNumber(number) + "\\textsuperscript{nd}";
			case '3':
				return getWordForNumber(number) + "\\textsuperscript{rd}";
			default:
				return getWordForNumber(number) + "\\textsuperscript{th}";
		}
	}
	
	/**
	 * Creates head lines.
	 * 
	 * @param kind
	 *        E.g., section, subsection, subsubsection, paragraph etc.
	 * @param title
	 *        The title of the heading.
	 * @param numbering
	 *        If true a number will be placed in front of the title.
	 * @return
	 */
	private static StringBuffer heading(String kind, String title,
		boolean numbering) {
		StringBuffer heading = new StringBuffer(newLine());
		heading.append("\\");
		heading.append(kind);
		if (!numbering) {
			heading.append('*');
		}
		heading.append('{');
		heading.append(title);
		heading.append('}');
		heading.append(newLine());
		return heading;
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
	 * 
	 * @param string
	 * @param hyphen
	 *        if true a hyphen symbol is introduced at each position where a
	 *        special character has to be masked anyway.
	 * @return
	 */
	public static String maskSpecialChars(String string, boolean hyphen) {
		StringBuffer masked = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			char atI = string.charAt(i);
			if (atI == '<') {
				masked.append("$<$");
			} else if (atI == '>') {
				masked.append("$>$");
			} else {
				if ((atI == '_') || (atI == '\\') || (atI == '$') || (atI == '&')
						|| (atI == '#') || (atI == '{') || (atI == '}') || (atI == '~')
						|| (atI == '%') || (atI == '^')) {
					if ((i == 0) || (!hyphen)) {
						masked.append('\\');
					} else if (hyphen && (string.charAt(i - 1) != '\\'))
					 {
						masked.append("\\-\\"); // masked.append('\\');
					// } else if ((atI == '[') || (atI == ']')) {
					}
				}
				masked.append(atI);
			}
		}
		return masked.toString().trim();
	}
	
	/**
	 * 
	 * @param symbol
	 * @return
	 */
	public static StringBuffer mathrm(char symbol) {
		return command("mathrm", Character.valueOf(symbol));
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public static StringBuffer mathrm(String text) {
		return command("mathrm", text);
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public static StringBuffer mathtext(String text) {
		return command("text", text);
	}
	
	/**
	 * 
	 * @param degree
	 * @param value
	 * @return
	 */
	public static StringBuffer root(Object degree, Object value) {
		if (degree.toString().equals("2")) {
			return sqrt(value);
		}
		StringBuffer sqrt = new StringBuffer("\\sqrt");
		sqrt.append('[');
		sqrt.append(degree);
		sqrt.append("]{");
		sqrt.append(value);
		sqrt.append('}');
		return sqrt;
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	public static StringBuffer sqrt(Object value) {
		return command("sqrt", value);
	}
	
	/**
	 * Creates a usepackage command for the given package with the optional
	 * options.
	 * 
	 * @param latexPackage
	 *        the name of the latex package
	 * @param options
	 *        options without commas
	 * @return usepackage command including system-dependent new line character.
	 */
	public static StringBuffer usepackage(String latexPackage, String... options) {
		StringBuffer usepackage = new StringBuffer("\\usepackage");
		if (options.length > 0) {
			usepackage.append('[');
			boolean first = true;
			for (String option : options) {
				if (!first) {
					usepackage.append(',');
				} else {
					first = false;
				}
				usepackage.append(option);
			}
			usepackage.append(']');
		}
		usepackage.append('{');
		usepackage.append(latexPackage);
		usepackage.append('}');
		usepackage.append(newLine());
		return usepackage;
	}
	
	/**
	 * An opening quotation mark.
	 */
	public static final String leftQuotationMark = "``";
	
	/**
	 * An closing quotation mark.
	 */
	public static final String rightQuotationMark = "\"";
	
	/**
	 * Encloses the given formula in brackets.
	 * 
	 * @param formula
	 * @return
	 */
	public StringBuffer brackets(Object formula) {
		StringBuffer buffer = new StringBuffer("\\left(");
		buffer.append(formula);
		buffer.append("\\right)");
		return buffer;
	}
	
	/**
	 * 
	 * @param color
	 * @param what
	 * @return
	 */
	public StringBuffer colorbox(String color, Object what) {
		return command("colorbox", color, what);
	}
	
	/**
	 * This method simplifies the process of creating descriptions. There is an
	 * item entry together with a description. No new line or space is needed for
	 * separation.
	 * 
	 * @param item
	 *        e.g., "my item"
	 * @param description
	 *        e.g., "my description"
	 * @return
	 */
	public StringBuffer descriptionItem(String item, Object description) {
		StringBuffer itemBuffer = new StringBuffer("\\item[");
		itemBuffer.append(item);
		itemBuffer.append("] ");
		itemBuffer.append(description);
		itemBuffer.append(newLine());
		return itemBuffer;
	}
	
	/**
	 * This method returns a {@code StringBuffer} representing a properly
	 * LaTeX formatted number. However, if the {@code double} argument
	 * contains "Exx" (power of ten), then the returned value starts and ends with
	 * a dollar symbol.
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
			if (split[0].equals("-1.0")) {
				val = "-" + val;
			} else if (!split[0].equals("1.0")) {
				val = format(Double.parseDouble(split[0])) + "\\cdot " + val;
			}
			sb.append(math(val));
		} else if (value - ((int) value) == 0) {
			sb.append(((int) value));
		} else {
			sb.append(val);
		}
		return sb;
	}
	
	/**
	 * Creates a hyper link to the given target and the text to be visible in the
	 * document.
	 * 
	 * @param target
	 *        The target to which this link points to.
	 * @param text
	 *        The text to be written in the link.
	 * @return
	 */
	public StringBuffer href(String target, Object text) {
		return command("href", target, text);
	}
	
	/**
	 * 
	 * @param target
	 * @param text
	 * @return
	 */
	public StringBuffer hyperref(String target, Object text) {
		StringBuffer sb = new StringBuffer("\\hyperref[");
		sb.append(target);
		sb.append("]{");
		sb.append(text);
		sb.append('}');
		return sb;
	}
	
	public StringBuffer label(String id) {
		return command("label", new StringBuffer(id));
	}
	
	/**
	 * Creates a head for a longtable in LaTeX.
	 * 
	 * @param columnDef
	 *        without leading and ending brackets, e.g., "lrrc",
	 * @param caption
	 *        caption of this table without leading and ending brackets
	 * @param headLine
	 *        table head without leading and ending brackets and without double
	 *        backslashes at the end
	 * @return
	 */
	public StringBuffer longtableHead(String columnDef, String caption,
		String... headLine) {
		StringBuffer buffer = new StringBuffer("\\begin{longtable}[h!]{");
		if (!columnDef.startsWith("@{}")) {
			buffer.append("@{}");
		}
		buffer.append(columnDef);
		if (!columnDef.endsWith("@{}")) {
			buffer.append("@{}");
		}
		buffer.append('}');
		buffer.append(newLine());
		buffer.append("\\caption{");
		buffer.append(caption);
		buffer.append('}');
		buffer.append("\\\\");
		StringBuffer head = new StringBuffer(toprule);
		if (headLine != null) {
			for (int i = 0; i < headLine.length; i++) {
				head.append(headLine[i]);
				if (i < headLine.length - 1) {
					head.append('&');
				}
			}
		}
		head.append("\\\\");
		head.append(midrule);
		buffer.append(head);
		buffer.append("\\endfirsthead");
		// buffer.append(newLine());
		buffer.append(head);
		buffer.append("\\endhead");
		// buffer.append(bottomrule);
		// buffer.append("\\endlastfoot");
		buffer.append(newLine());
		return buffer;
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
		if (f.length() == 0) {
			return math;
		}
		if (f.charAt(0) != '$') {
			math.append('$');
		}
		math.append(f);
		if (f.charAt(f.length() - 1) != '$') {
			math.append('$');
		}
		return math;
	}
	
	/**
	 * Returns the LaTeX code to set the given String in type writer font within a
	 * math environment.
	 * 
	 * @param id
	 * @return
	 */
	public StringBuffer mathtt(String id) {
		return command("mathtt", new StringBuffer(id));
	}
	
	public StringBuffer section(String title, boolean numbering) {
		return heading("section", title, numbering);
	}
	
	/**
	 * 
	 * @param title
	 * @param numbering
	 * @return
	 */
	public StringBuffer subsection(String title, boolean numbering) {
		return heading("subsection", title, numbering);
	}
	
	/**
	 * 
	 * @param title
	 * @param numbering
	 * @return
	 */
	public StringBuffer subsubsection(String title, boolean numbering) {
		return heading("subsubsection", title, numbering);
	}
	
	/**
	 * 
	 * @param color
	 * @param text
	 * @return
	 */
	public StringBuffer textcolor(String color, Object text) {
		return command("textcolor", color, text);
	}
	
	/**
	 * Returns the LaTeX code to set the given String in type writer font.
	 * 
	 * @param id
	 * @return
	 */
	public StringBuffer texttt(String id) {
		return command("texttt", new StringBuffer(id));
	}

	/**
	 * 
	 * @return
	 */
	public static String endDocument() {
		return "\\end{document}\n\n";
	}

	/**
	 * 
	 * @return
	 */
	public static String endCenter() {
		return "\\end{center}\n";
	}

	/**
	 * 
	 * @return
	 */
	public static String beginCenter() {
		return "\\begin{center}\n";
	}

	/**
	 * 
	 * @param scale
	 * @return
	 */
	public static String scaleFont(double scale) {
		return "\\scalefont{" + scale + "}\n";
	}

	/**
	 * 
	 * @return
	 */
	public static String beginDocument() {
		return "\\begin{document}\n";
	}

	/**
	 * 
	 * @param style
	 * @return
	 */
	public static String pageStyle(String style) {
		return "\\pagestyle{" + style + "}\n";
	}

	/**
	 * 
	 * @param colorName
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static String defineColor(String colorName, double r, double g, double b) {
		return "\\definecolor{" + colorName + "}{RGB}{" + r + ", " + g + ", " + b + "}\n";
	}

	/**
	 * 
	 * @param docType
	 * @param fontSize
	 * @return
	 */
	public static String dcoumentClass(String docType, int fontSize) {
		return "\\documentclass[" + fontSize + "pt]{" + docType + "}\n";
	}

	/**
	 * 
	 * @return
	 */
	public static String makeTitle() {
		return "\\maketitle";
	}

}
