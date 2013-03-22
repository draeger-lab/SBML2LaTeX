/*
 * $Id: LaTeXOptions.java 60 2011-03-07 17:20:39Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/tags/version0.9.8/src/org/sbml/tolatex/LaTeXOptions.java $
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates 
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
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
package org.sbml.tolatex;

import java.io.File;

import de.zbit.io.SBFileFilter;
import de.zbit.util.FileTools;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;

/**
 * A collection of meaningful {@link Option} instances for the configuration of
 * {@link SBML2LaTeX}.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 * @version $Rev: 60 $
 */
public interface LaTeXOptions extends KeyProvider {
    
    /**
     * If true, the libSBML model consistency check is performed and the results
     * are written in the glossary of the model report.
     */
    public static final Option<Boolean> CHECK_CONSISTENCY = new Option<Boolean>(
	"CHECK_CONSISTENCY",
	Boolean.class,
	"If true, the automatic model consistency check is performed and the results are written in the appendix of the model report file.",
	Boolean.FALSE);
    
    /**
     * If this option is set to true, all temporary files will be deleted after
     * running SBML2LaTeX. In case of PDF creation, for instance, this will
     * cause even the TeX file to be deleted. However, this option can be
     * meaningful to remove all the temporary files created by your system's
     * LaTeX compiler.
     */
    public static final Option<Boolean> CLEAN_WORKSPACE = new Option<Boolean>(
	"CLEAN_WORKSPACE",
	Boolean.class,
	"If this option is set to true, all temporary files will be deleted after running SBML2LaTeX. In case of PDF creation, for instance, this will cause even the TeX file to be deleted. However, this option can be meaningful to remove all the temporary files created by your system's LaTeX compiler.",
	Boolean.FALSE);

    /**
     * Allows to select the font of captions and other sans serif text. Default:
     * helvetica
     */
    public static final Option<String> FONT_HEADINGS = new Option<String>(
	"FONT_HEADINGS",
	String.class,
	"Allows to select the font of captions and other (by default sans serif) text.",
	new Range<String>(String.class, "{avant,cmss,helvetica}"), "helvetica");

    /**
     * The font size for LaTeX documents.
     */
    public static final Option<Short> FONT_SIZE = new Option<Short>(
	"FONT_SIZE",
	Short.class,
	"This option allows you to select the size of the standard text font. Headings appear with a larger font.",
	new Range<Short>(Short.class, "{8,9,10,11,12,14,17}"), Short
		.valueOf((short) 11));
    
    /**
     * Allows to select the font of continuous text. Choosing 'times' is
     * actually not recommended
     * because in some cases equations might not look as nicely as they do when
     * using 'mathptmx'. Default: Times font
     * mathptmx.
     */
    public static final Option<String> FONT_TEXT = new Option<String>(
	"FONT_TEXT",
	String.class,
	"Allows to select the font of continuous text. Choosing 'times' is actually not recommended because in some cases equations might not look as nicely as they do when using 'mathptmx'. Default: Times font mathptmx.",
	new Range<String>(String.class,
	    "{chancery,charter,cmr,mathptmx,palatino,times,utopia}"),
	"mathptmx");
    
    /**
     * Decides whether to set the LaTeX document in landscape or portrait mode.
     */
    public static final Option<Boolean> LANDSCAPE = new Option<Boolean>(
	"LANDSCAPE",
	Boolean.class,
	"This option decides whether to set the LaTeX document in landscape or portrait mode. By default most pages are in portrait format.",
	Boolean.FALSE);
    
    /**
     * The PDF LaTeX compiler of the system. If this is null, the program pdflatex cannot be found.
     */
    public static final File PDF_LaTeX_COMPILER = FileTools.which("pdflatex");

    /**
     * The path to the LaTeX compiler to generate PDF, DVI or other files from
     * the
     * created LaTeX report file.
     */
    public static final Option<File> LOAD_LATEX_COMPILER = new Option<File>(
	"LOAD_LATEX_COMPILER",
	File.class,
	"The path to the LaTeX compiler to generate PDF, DVI or other files from the created LaTeX report file.",
	new Range<File>(File.class, SBFileFilter.createAllFileFilter()), PDF_LaTeX_COMPILER == null ? new File(System.getProperty("user.dir")) : PDF_LaTeX_COMPILER);
    
    /**
     * If true (default), MIRIAM annotations are included into the model report
     * if
     * there are any. This option may require the path to the MIRIAM translation
     * file to be specified.
     */
    public static final Option<Boolean> MIRIAM_ANNOTATION = new Option<Boolean>(
	"MIRIAM_ANNOTATION",
	Boolean.class,
	"If true (default), MIRIAM annotations are included into the model report if there are any. In this case, SBML2LaTeX generates links to the resources for each annotated element. This option may require the path to the MIRIAM translation file to be specified.",
	Boolean.TRUE);

    /**
     * The paper size for LaTeX documents.
     */
    public static final Option<String> PAPER_SIZE = new Option<String>(
	"PAPER_SIZE",
	String.class,
	"The paper size for LaTeX documents. With this option the paper format can be influenced. Default paper size: DIN A4. All sizes a?, b?, c? and d? are European DIN sizes. Letter, legal and executive are US paper formats.",
	new Range<String>(
	    String.class,
	    "{letter,legal,executive,a0,a1,a2,a3,a4,a5,a6,a7,a8,a9,b0,b1,b2,b3,b4,b5,b6,b7,b8,b9,c0,c1,c2,c3,c4,c5,c6,c7,c8,c9,d0,d1,d2,d3,d4,d5,d6,d7,d8,d9}"),
	"a4");

    /**
     * Allows to print the entire differential equation system for all species instead of generating
     * links to the reactions.
     */
    public static final Option<Boolean> PRINT_FULL_ODE_SYSTEM = new Option<Boolean>(
	"PRINT_FULL_ODE_SYSTEM",
	Boolean.class,
	"If set to true, the entire rate of change will be written for each species. By default, SBML2LaTeX only prints the sum of the individual reaction rates, which are hyperlinked but displayed at a different position of the report.",
	Boolean.FALSE);
    
    /**
     * Decides whether to write the names or the identifiers of NamedSBase
     * object
     * in equations.
     */
    public static final Option<Boolean> PRINT_NAMES_IF_AVAILABLE = new Option<Boolean>(
	"PRINT_NAMES_IF_AVAILABLE",
	Boolean.class,
	"If selected, the names of SBML elements (NamedSBase) are displayed instead of their identifiers. This can only be done if the element has a name.",
	Boolean.FALSE);
    
    /**
     * If true, the details (identifier and name) of all reactants, modifiers
     * and
     * products participating in a reaction are listed in one table. By default
     * a
     * separate table is created for each one of the three participant groups
     * including its SBO term.
     */
    public static final Option<Boolean> REACTANTS_OVERVIEW_TABLE = new Option<Boolean>(
	"REACTANTS_OVERVIEW_TABLE",
	Boolean.class,
	"If true, the details (identifier and name) of all reactants, modifiers and products participating in a reaction are listed in one table. By default a separate table is created for each one of the three participant groups including its SBO term.",
	Boolean.FALSE);

    /**
     * If true (default), all predefined unit definitions of SBML are made
     * explicit.
     */
    public static final Option<Boolean> SHOW_PREDEFINED_UNITS = new Option<Boolean>(
	"SHOW_PREDEFINED_UNITS",
	Boolean.class,
	"If true (default), all predefined unit declarations of the SBML are made explicit in the report file as these are defined by the corresponding SBML Level and Version. Otherwise only unit definitions from the model are included.",
	Boolean.TRUE);
    
    /**
     * Decides whether to create a separate title page instead of a simple
     * heading.
     */
    public static final Option<Boolean> TITLE_PAGE = new Option<Boolean>(
	"TITLE_PAGE",
	Boolean.class,
	"If true, a separate title page will be created. By default the title is written as a simple heading on the first page.",
	Boolean.FALSE);
    
    /**
     * Key that decides whether or not identifiers should be written in
     * typewriter
     * font when these occur in mathematical equations.
     */
    public static final Option<Boolean> TYPEWRITER = new Option<Boolean>(
	"TYPEWRITER",
	Boolean.class,
	"This option decides whether a typewriter font should be applied to highlight SBML identifiers. This is particularly important when these occur in mathematical equations.",
	Boolean.TRUE);
    
    /**
     * Selects the font to be used for typewriter text. Default: cmt.
     */
    public static final Option<String> FONT_TYPEWRITER = new Option<String>(
	"FONT_TYPEWRITER",
	String.class,
	String.format(
		    "Select a typewriter font that can be used for identifiers if option '%s' is selected. URLs and other resources are also marked with this font.",
		    TYPEWRITER), new Range<String>(
	    String.class, "{cmt,courier}"), "cmt");
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public static final OptionGroup<?> TYPOGRAPHICAL_OPTIONS = new OptionGroup(
	"Typographical options",
	"Here you can specify general properties such as paper size and font styles.",
	FONT_HEADINGS, FONT_SIZE, FONT_TEXT,
	FONT_TYPEWRITER, PAPER_SIZE);

    /**
     * Here you can specify also the location of the LaTeX compiler on your
     * operating system.
     * Previously, this method also allowed the user to specify the location of
     * the SBML2LaTeX logo file, but this one is now included in the JAR and
     * will not depend on the user any more.
     */
    @SuppressWarnings("unchecked")
    public static final OptionGroup<?> CONFIGURATION_FILES = new OptionGroup(
	"LaTeX compiler location",
	"Here you can specify the location of the LaTeX compiler on your operating system.",
	LOAD_LATEX_COMPILER);

    /**
     * These options allow you to influence layout and style of the LaTeX
     * report.
     */
    @SuppressWarnings("unchecked")
    public static final OptionGroup<Boolean> LAYOUT_OPTIONS = new OptionGroup<Boolean>(
	"Layout options",
	"These options allow you to influence layout and style of the LaTeX report.",
	LANDSCAPE, PRINT_NAMES_IF_AVAILABLE, TITLE_PAGE, TYPEWRITER,
	REACTANTS_OVERVIEW_TABLE);

    /**
     * Configure the layout of the LaTeX reports, what to be included, and if to
     * remove temporary files afterwards.
     */
    @SuppressWarnings("unchecked")
    public static final OptionGroup<Boolean> REPORT_OPTIONS = new OptionGroup<Boolean>(
	"Report options",
	"Configure the layout of the LaTeX reports, what to be included, and if to remove temporary files afterwards.",
	CHECK_CONSISTENCY, MIRIAM_ANNOTATION, SHOW_PREDEFINED_UNITS, PRINT_FULL_ODE_SYSTEM,
	CLEAN_WORKSPACE);
}
