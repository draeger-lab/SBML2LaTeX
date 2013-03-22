/**
 * 
 */
package org.sbml.tolatex.io;

import java.io.File;

import de.zbit.io.MultipleFileFilter;
import de.zbit.io.SBFileFilter;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;

/**
 * Provides {@link Option} instances for SBML input and LaTeX output.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-10
 */
public interface LaTeXOptionsIO extends KeyProvider {
	
	/**
	 * The SBML file to be opened
	 */
	public static final Option<File> SBML_INPUT_FILE = new Option<File>(
		"SBML_INPUT_FILE", File.class,
		"The SBML source file to be converted to LaTeX.", new Range<File>(
			File.class, SBFileFilter.SBML_FILE_FILTER), new File(System
				.getProperty("user.dir")));
	
	/**
	 * The file where to save the generated LaTeX report. The standard way is to
	 * let SBML2LaTeX generate a TeX file, which the user can then compile to a
	 * different format, or from which parts can be easily extracted to support
	 * scientific writing. Additionally, SBML2LaTeX may be used to directly
	 * generate a PDF file if the LaTeX compiler pdfLaTeX is specified.
	 */
	public static final Option<File> REPORT_OUTPUT_FILE = new Option<File>(
		"REPORT_OUTPUT_FILE",
		File.class,
		"The file where to save the generated LaTeX report. The standard way is to let SBML2LaTeX generate a TeX file, which the user can then compile to a different format, or from which parts can be easily extracted to support scientific writing. Additionally, SBML2LaTeX may be used to directly generate a PDF file if the LaTeX compiler pdfLaTeX is specified.",
		new Range<File>(File.class, new MultipleFileFilter(
			"Report files (*.tex, *.pdf)", SBFileFilter.TeX_FILE_FILTER)), new File(
			System.getProperty("user.dir")));
	
	/**
	 * Standard directory where LaTeX files can be stored.
	 */
	public static final Option<File> LATEX_DIR = new Option<File>("LATEX_DIR",
		File.class, "Standard directory where LaTeX files can be stored",
		new Range<File>(File.class, SBFileFilter.DIRECTORY_FILTER), new File(System
				.getProperty("user.home")));
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static final OptionGroup<?> INPUT_AND_OUTPUT_FILES = new OptionGroup(
		"Configure input and output files",
		"Here you can specify the SBML input file and the location of the LaTeX file for output.",
		SBML_INPUT_FILE, REPORT_OUTPUT_FILE, LATEX_DIR);
	
}
