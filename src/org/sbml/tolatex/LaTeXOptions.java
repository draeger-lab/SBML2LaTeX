package org.sbml.tolatex;

import java.io.File;

import de.zbit.io.MultipleFileFilter;
import de.zbit.io.SBFileFilter;
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
	 * The path to the LaTeX compiler to generate PDF, DVI or other files from the
	 * created LaTeX report file.
	 */
	public static final Option<File> LOAD_LATEX_COMPILER = new Option<File>(
		"LOAD_LATEX_COMPILER",
		File.class,
		"The path to the LaTeX compiler to generate PDF, DVI or other files from the created LaTeX report file.",
		new Range<File>(File.class, SBFileFilter.ALL_FILE_FILTER), new File(System
				.getProperty("user.dir")));
	/**
	 * Allows to select the font of captions and othe sans serif text. Default:
	 * helvetica
	 */
	public static final Option<String> LATEX_FONT_HEADINGS = new Option<String>(
		"LATEX_FONT_HEADINGS",
		String.class,
		"Allows to select the font of captions and other (by default sans serif) text.",
		new Range<String>(String.class, "{avant,cmss,helvetica}"), "helvetica");
	
	/**
	 * The font size for LaTeX documents.
	 */
	public static final Option<Short> LATEX_FONT_SIZE = new Option<Short>(
		"LATEX_FONT_SIZE",
		Short.class,
		"This option allows you to select the size of the standard text font. Headings appear with a larger font.",
		new Range<Short>(Short.class, "{8,9,10,11,12,14,17}"), Short
				.valueOf((short) 11));
	
	/**
	 * Allows to select the font of continuous text. Default: times.
	 */
	public static final Option<String> LATEX_FONT_TEXT = new Option<String>(
		"LATEX_FONT_TEXT", String.class,
		"Allows you to select the font to be used for standard continuous text.",
		new Range<String>(String.class,
			"{chancery,charter,cmr,palatino,times,utopia}"), "times");
	/**
	 * Key that decides whether or not identifiers should be written in typewriter
	 * font when these occur in mathematical equations.
	 */
	public static final Option<Boolean> LATEX_IDS_IN_TYPEWRITER_FONT = new Option<Boolean>(
		"LATEX_IDS_IN_TYPEWRITER_FONT",
		Boolean.class,
		"This option decides whether a typewriter font should be applied to highlight SBML identifiers. This is particularly important when these occur in mathematical equations.",
		Boolean.TRUE);
	/**
	 * Selects the font to be used for typewriter text. Default: cmt.
	 */
	public static final Option<String> LATEX_FONT_TYPEWRITER = new Option<String>(
		"LATEX_FONT_TYPEWRITER",
		String.class,
		String
				.format(
					"Select a typewriter font that can be used for identifiers if option '%s' is selected. URLs and other resources are also marked with this font.",
					LATEX_IDS_IN_TYPEWRITER_FONT), new Range<String>(String.class,
			"{cmt,courier}"), "cmt");
	/**
	 * Decides whether to set the LaTeX document in landscape or portrait mode.
	 */
	public static final Option<Boolean> LATEX_LANDSCAPE = new Option<Boolean>(
		"LATEX_LANDSCAPE",
		Boolean.class,
		"This option decides whether to set the LaTeX document in landscape or portrait mode. By default most pages are in portrait format.",
		Boolean.FALSE);
	/**
	 * Decides whether to write the names or the identifiers of NamedSBase object
	 * in equations.
	 */
	public static final Option<Boolean> LATEX_NAMES_IN_EQUATIONS = new Option<Boolean>(
		"LATEX_NAMES_IN_EQUATIONS",
		Boolean.class,
		"If selected, the names of SBML elements (NamedSBase) are displayed instead of their identifiers. This can only be done if the element has a name.",
		Boolean.FALSE);
	
	/**
	 * The paper size for LaTeX documents.
	 */
	public static final Option<String> LATEX_PAPER_SIZE = new Option<String>(
		"LATEX_PAPER_SIZE",
		String.class,
		"The paper size for LaTeX documents. With this option the paper format can be influenced. Default paper size: DIN A4. All sizes a?, b?, c? and d? are European DIN sizes. Letter, legal and executive are US paper formats.",
		new Range<String>(
			String.class,
			"{letter,legal,executive,a0,a1,a2,a3,a4,a5,a6,a7,a8,a9,b0,b1,b2,b3,b4,b5,b6,b7,b8,b9,c0,c1,c2,c3,c4,c5,c6,c7,c8,c9,d0,d1,d2,d3,d4,d5,d6,d7,d8,d9}"),
		"a4");
	/**
	 * Decides whether to create a separate title page instead of a simple
	 * heading.
	 */
	public static final Option<Boolean> LATEX_TITLE_PAGE = new Option<Boolean>(
		"LATEX_TITLE_PAGE",
		Boolean.class,
		"If true, a separate title page will be created. By default the title is written as a simple heading on the first page.",
		Boolean.FALSE);
	/**
	 * The SBML logo to be displayed at the beginning of the model report. Default
	 * path: resources/SBML2LaTeX.pdf
	 */
	public static final Option<File> LOGO_INPUT_FILE = new Option<File>(
		"LOGO_INPUT_FILE", File.class,
		"The SBML logo to be displayed at the beginning of the model report.",
		new Range<File>(File.class, new MultipleFileFilter(
			"Image file (*.pdf, *.png, *.jpg)", SBFileFilter.PDF_FILE_FILTER,
			SBFileFilter.PNG_FILE_FILTER, SBFileFilter.JPEG_FILE_FILTER)), new File(
			System.getProperty("user.dir") + "/resources/SBML2LaTeX.pdf"));
	
	/**
	 * If true (default), MIRIAM annotations are included into the model report if
	 * there are any. This option may require the path to the MIRIAM translation
	 * file to be specified.
	 */
	public static final Option<Boolean> MIRIAM_ANNOTATION = new Option<Boolean>(
		"MIRIAM_ANNOTATION",
		Boolean.class,
		"If true (default), MIRIAM annotations are included into the model report if there are any. In this case, SBML2LaTeX generates links to the resources for each annotated element. This option may require the path to the MIRIAM translation file to be specified.",
		Boolean.TRUE);
	
	/**
	 * If true, the details (identifier and name) of all reactants, modifiers and
	 * products participating in a reaction are listed in one table. By default a
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
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static final OptionGroup<?> CONFIGURATION_FILES = new OptionGroup(
		"Specify the location of configuration files",
		"Here you can specify the location of the SBML2LaTeX logo file and also the location of the LaTeX compiler on your operating system.",
		LOGO_INPUT_FILE, LOAD_LATEX_COMPILER);
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static final OptionGroup<Boolean> REPORT_OPTIONS = new OptionGroup<Boolean>(
		"Report options",
		"Configure the layout of the LaTeX reports and what to be included.",
		CHECK_CONSISTENCY, MIRIAM_ANNOTATION, SHOW_PREDEFINED_UNITS);
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static final OptionGroup<Boolean> LAYOUT_OPTIONS = new OptionGroup<Boolean>(
		"Layout options",
		"These options allow you to influence layout and style of the LaTeX report.",
		LATEX_LANDSCAPE, LATEX_NAMES_IN_EQUATIONS, LATEX_TITLE_PAGE,
		LATEX_IDS_IN_TYPEWRITER_FONT, REACTANTS_OVERVIEW_TABLE);
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static final OptionGroup<?> TYPOGRAPHICAL_OPTIONS = new OptionGroup(
		"Typographical options",
		"Here you can specify general properties such as paper size and font styles.",
		LATEX_FONT_HEADINGS, LATEX_FONT_SIZE, LATEX_FONT_TEXT,
		LATEX_FONT_TYPEWRITER, LATEX_PAPER_SIZE);
	
}
