package org.sbml.tolatex;

import java.io.File;

import de.zbit.io.SBFileFilter;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
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
     * The path to the associated configuration file, which contains one default
     * value for each option defined in this interface.
     */
    public static final String CONFIG_FILE_LOCATION = "cfg/SBML2LaTeX.xml";
    /**
     * Standard directory where LaTeX files can be stored.
     */
    public static final Option<File> LATEX_DIR = new Option<File>("LATEX_DIR",
	File.class, "Standard directory where LaTeX files can be stored",
	new Range<File>(File.class, SBFileFilter.DIRECTORY_FILTER));
    /**
     * The font size for LaTeX documents.
     */
    public static final Option<Short> LATEX_FONT_SIZE = new Option<Short>(
	"LATEX_FONT_SIZE", Short.class, "The font size for LaTeX documents",
	new Range<Short>(Short.class, "{8,9,10,11,12,14,17}"));
    /**
     * Key that decides whether or not identifiers should be written in
     * typewriter font when these occur in mathematical equations.
     */
    public static final Option<Boolean> LATEX_IDS_IN_TYPEWRITER_FONT = new Option<Boolean>(
	"LATEX_IDS_IN_TYPEWRITER_FONT",
	Boolean.class,
	"Decides whether or not identifiers should be written in typewriter font when these occur in mathematical equations");
    /**
     * Decides whether to set the LaTeX document in landscape or portrait mode.
     */
    public static final Option<Boolean> LATEX_LANDSCAPE = new Option<Boolean>(
	"LATEX_LANDSCAPE", Boolean.class,
	"Whether to set the LaTeX document in landscape or portrait mode");
    /**
     * Decides whether to write the names or the identifiers of NamedSBase
     * object in equations.
     */
    public static final Option<Boolean> LATEX_NAMES_IN_EQUATIONS = new Option<Boolean>(
	"LATEX_NAMES_IN_EQUATIONS",
	Boolean.class,
	"Whether to write the names or the identifiers of NamedSBase object in equations");
    /**
     * The paper size for LaTeX documents.
     */
    public static final Option<String> LATEX_PAPER_SIZE = new Option<String>(
	"LATEX_PAPER_SIZE",
	String.class,
	"The paper size for LaTeX documents",
	new Range<String>(
	    String.class,
	    "{letter,legal,executive,a0,a1,a2,a3,a4,a5,a6,a7,a8,a9,b0,b1,b2,b3,b4,b5,b6,b7,b8,b9,c0,c1,c2,c3,c4,c5,c6,c7,c8,c9,d0,d1,d2,d3,d4,d5,d6,d7,d8,d9}"));
    /**
     * Decides whether to create a separate title page instead of a simple
     * heading.
     */
    public static final Option<Boolean> LATEX_TITLE_PAGE = new Option<Boolean>(
	"LATEX_TITLE_PAGE", Boolean.class,
	"Whether to create a separate title page instead of a simple heading");
}
