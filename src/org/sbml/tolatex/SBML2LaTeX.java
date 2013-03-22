/*
 * SBML2LaTeX converts SBML files (http://sbml.org) into LaTeX files. Copyright
 * (C) 2009 ZBIT, University of Tübingen, Andreas Dräger This program is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.tolatex;

import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import org.jdom.JDOMException;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.sbml.tolatex.gui.LaTeXExportDialog;
import org.sbml.tolatex.io.LaTeXExport;
import org.sbml.tolatex.io.LaTeXOptionsIO;
import org.sbml.tolatex.io.MIRIAMparser;

import de.zbit.gui.GUIOptions;
import de.zbit.io.SBFileFilter;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;

/**
 * This class displays the whole information content of an SBML model in a LaTeX
 * file which can then be further processed, i.e., to a PDF file.
 * 
 * @author draeger
 * @date December 4, 2007
 */
public class SBML2LaTeX {
	/**
     * 
     */
	private static final MIRIAMparser miriam = new MIRIAMparser();
	
	/**
	 * @return the MIRIAM parser.
	 * @throws Exception
	 */
	public static MIRIAMparser getMIRIAMparser() throws Exception {
		if (!miriam.isSetDocument()) {
			try {
				miriam.setMIRIAMDocument(SBML2LaTeX.class
						.getResourceAsStream("cfg/MIRIAM.xml"));
			} catch (JDOMException exc) {
				throw new Exception(exc);
			}
		}
		return miriam;
	}
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws SBMLException
	 */
	public static void main(String[] args) throws IOException,
		XMLStreamException, SBMLException {
		LinkedList<Class<? extends KeyProvider>> defAndKeys = new LinkedList<Class<? extends KeyProvider>>();
		defAndKeys.add(LaTeXOptionsIO.class);
		defAndKeys.add(LaTeXOptions.class);
		defAndKeys.add(GUIOptions.class);
		SBPreferences.analyzeCommandLineArguments(defAndKeys, args);
		
		new SBML2LaTeX();
		
		System.exit(0);
	}
	
	/**
	 * 
	 */
	private SBPreferences prefsGUI, prefsLaTeX, prefsIO;
	
	/**
	 * 
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws SBMLException
	 */
	public SBML2LaTeX() throws IOException, XMLStreamException, SBMLException {
		
		prefsGUI = SBPreferences.getPreferencesFor(GUIOptions.class);
		prefsIO = SBPreferences.getPreferencesFor(LaTeXOptionsIO.class);
		prefsLaTeX = SBPreferences.getPreferencesFor(LaTeXOptions.class);
		
		boolean convert = true;
		try {
			if (prefsGUI.getBoolean(GUIOptions.GUI)) {
				LaTeXExportDialog.initGUI();
				convert = LaTeXExportDialog.showDialog();
			}
		} catch (HeadlessException exc) {
			if (prefsGUI.getBoolean(GUIOptions.GUI)) {
				System.err.println("Cannot initialize the graphical user interface.");
				exc.printStackTrace();
			}
		} finally {
			if (convert) {
				convert(prefsIO.get(LaTeXOptionsIO.SBML_INPUT_FILE), prefsIO
						.get(LaTeXOptionsIO.REPORT_OUTPUT_FILE));
			}
		}
	}
	
	/**
	 * @throws HeadlessException
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws SBMLException
	 */
	public void convert(File infile, File outfile) throws HeadlessException,
		IOException, SBMLException, XMLStreamException {
		if (!SBFileFilter.SBML_FILE_FILTER.accept(infile)) { throw new IOException(
			String
					.format("File %s is no valid SBML file.", outfile.getAbsolutePath())); }
		System.out.printf("Converting file\n%s\nto\n%s.\n", infile
				.getAbsolutePath(), outfile.getAbsolutePath());
		convert(SBMLReader.readSBML(infile.getAbsolutePath()), outfile);
	}
	
	/**
	 * 
	 * @param sbase
	 * @param outfile
	 * @throws IOException
	 * @throws SBMLException
	 */
	public void convert(SBase sbase, File outfile) throws IOException,
		SBMLException {
		String texFile;
		if (SBFileFilter.PDF_FILE_FILTER.accept(outfile)) {
			texFile = System.getProperty("user.home") + "/"
					+ outfile.getName().substring(0, outfile.getName().lastIndexOf('.'))
					+ ".tex";
		} else if (SBFileFilter.TeX_FILE_FILTER.accept(outfile)) {
			texFile = outfile.getAbsolutePath();
		} else {
			throw new IOException(String.format("File %s is no valid LaTeX file.",
				outfile.getAbsolutePath()));
		}
		
		try {
			long time = System.currentTimeMillis();
			LaTeXExport export = new LaTeXExport();
			export.setShowPredefinedUnitDeclarations(prefsLaTeX
					.getBoolean(LaTeXOptions.SHOW_PREDEFINED_UNITS));
			export.setFontSize(prefsLaTeX.getShort(LaTeXOptions.LATEX_FONT_SIZE));
			export.setLandscape(prefsLaTeX.getBoolean(LaTeXOptions.LATEX_LANDSCAPE));
			export.setPaperSize(prefsLaTeX.get(LaTeXOptions.LATEX_PAPER_SIZE));
			export.setPrintNameIfAvailable(prefsLaTeX
					.getBoolean(LaTeXOptions.LATEX_NAMES_IN_EQUATIONS));
			export.setTitlepage(prefsLaTeX.getBoolean(LaTeXOptions.LATEX_TITLE_PAGE));
			export.setTypewriter(prefsLaTeX
					.getBoolean(LaTeXOptions.LATEX_IDS_IN_TYPEWRITER_FONT));
			export.setCheckConsistency(prefsLaTeX
					.getBoolean(LaTeXOptions.CHECK_CONSISTENCY));
			export.setIncludeMIRIAM(prefsLaTeX
					.getBoolean(LaTeXOptions.MIRIAM_ANNOTATION));
			export.setTextFont(prefsLaTeX.get(LaTeXOptions.LATEX_FONT_TEXT));
			export.setHeadingsFont(prefsLaTeX.get(LaTeXOptions.LATEX_FONT_HEADINGS));
			export.setTypewriterFont(prefsLaTeX
					.get(LaTeXOptions.LATEX_FONT_TYPEWRITER));
			export.setArrangeReactionParticipantsInOneTable(prefsLaTeX
					.getBoolean(LaTeXOptions.REACTANTS_OVERVIEW_TABLE));
			LaTeXExport.setLogoFile(prefsLaTeX.get(LaTeXOptions.LOGO_INPUT_FILE));
			BufferedWriter buffer = new BufferedWriter(new FileWriter(texFile));
			if (sbase instanceof SBMLDocument) {
				export.format((SBMLDocument) sbase, buffer);
			} else if (sbase instanceof Model) {
				export.format((Model) sbase, buffer);
			} else if (sbase instanceof Reaction) {
				buffer.append(export.toLaTeX((Reaction) sbase));
			} else {
				throw new IllegalArgumentException(
					String
							.format(
								"Only instances of SBMLDocument, Model, or Reaction are acceptable. Received %s.",
								sbase.getClass().getName()));
			}
			buffer.close();
			System.out
					.println("Time: " + (System.currentTimeMillis() - time) + " ms");
		} catch (IOException e) {
			throw new IOException(String.format("Cannot write to file %s.", texFile));
		}
		
		/*
		 * Create a PDF file directly
		 */
		if (SBFileFilter.PDF_FILE_FILTER.accept(outfile)) {
			File laTeXCompiler = LaTeXOptions.LOAD_LATEX_COMPILER
					.parseOrCast(prefsLaTeX.get(LaTeXOptions.LOAD_LATEX_COMPILER));
			if (!laTeXCompiler.exists() || !laTeXCompiler.canExecute()) {
				JOptionPane.showMessageDialog(null,
					"Please enter a valid path to PDFLaTeX on your system.",
					"Missing Path to LaTeX Compiler", JOptionPane.WARNING_MESSAGE);
			} else {
				// compile
				try {
					for (int i = 0; i < 2; i++) {
						ProcessBuilder builder = new ProcessBuilder(laTeXCompiler
								.getAbsolutePath(), "-interaction", "batchmode", texFile);
						builder.directory(new File(System.getProperty("user.home")));
						Process p = builder.start();
						Scanner s = new Scanner(p.getInputStream()).useDelimiter("\\Z");
						System.out.println(s.next());
						p.waitFor();
					}
					
					// move PDF file
					File pdfFile = new File(texFile.replace(".tex", ".pdf"));
					pdfFile.renameTo(outfile);
					
					// open standard pdf viewer
					// TODO: Java 1.6 only!
					Desktop.getDesktop().open(pdfFile);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			/*
			 * Create a LaTeX file only.
			 */
			// open generated LaTeX file.
			// TODO: Java 1.6 only!
			Desktop.getDesktop().open(outfile);
		}
	}
	
	/**
	 * 
	 * @param infile
	 * @param outfile
	 * @throws XMLStreamException
	 * @throws SBMLException
	 * @throws IOException
	 * @throws HeadlessException
	 */
	public void convert(String infile, String outfile) throws HeadlessException,
		IOException, SBMLException, XMLStreamException {
		convert(new File(infile), new File(outfile));
	}
	
}
