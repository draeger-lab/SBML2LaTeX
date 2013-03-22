/*
 * $Id: SBML2LaTeX.java 60 2011-03-07 17:20:39Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/tags/version0.9.8/src/org/sbml/tolatex/SBML2LaTeX.java $
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

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.xml.stream.XMLStreamException;

import org.jdom.JDOMException;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.sbml.tolatex.gui.LaTeXExportDialog;
import org.sbml.tolatex.io.LaTeXOptionsIO;
import org.sbml.tolatex.io.LaTeXReportGenerator;
import org.sbml.tolatex.io.MIRIAMparser;

import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.JBrowserPane;
import de.zbit.io.SBFileFilter;
import de.zbit.util.Utils;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * This class displays the whole information content of an SBML model in a LaTeX
 * file which can then be further processed, i.e., to a PDF file.
 * 
 * @author Andreas Dr&auml;ger
 * @date December 4, 2007
 * @version $Rev: 60 $
 */
public class SBML2LaTeX {
    /**
     * 
     */
    private static final MIRIAMparser miriam = new MIRIAMparser();

    /**
     * 
     * @param infile
     * @param outfile
     * @throws HeadlessException
     * @throws IOException
     * @throws SBMLException
     * @throws XMLStreamException
     */
    public static void convert(File infile, File outfile) throws HeadlessException, IOException, SBMLException, XMLStreamException {
	convert(infile, outfile, false);
    }
    
    /**
     * @throws HeadlessException
     * @throws IOException
     * @throws XMLStreamException
     * @throws SBMLException
     */
	public static void convert(File infile, File outfile, boolean gui)
			throws HeadlessException, IOException, SBMLException,
			XMLStreamException {
		if (!SBFileFilter.isSBMLFile(infile)) {
			throw new IOException(String
					.format("File %s is no valid SBML file.", outfile
							.getAbsolutePath()));
		}
		System.out.printf("Converting file\n%s\nto\n%s.\n", infile
				.getAbsolutePath(), outfile.getAbsolutePath());
		SBMLReader reader = new SBMLReader();
		convert(reader.readSBML(infile.getAbsolutePath()), outfile, gui);
	}
    
    /**
     * 
     * @param sbase
     * @param outfile
     * @throws SBMLException 
     * @throws IOException 
     */
    public static void convert(SBase sbase, File outfile) throws IOException, SBMLException {
	convert(sbase, outfile, false);
    }
    
    /**
     * 
     * @param sbase
     * @param outfile
     * @param gui
     * @throws IOException
     * @throws SBMLException
     */
    public static void convert(SBase sbase, File outfile, boolean gui) throws IOException,
	SBMLException {
	String texFile;
	if (SBFileFilter.isPDFFile(outfile)) {
	  // "user.home" darf hier auf keinen fall genommen werden!
	  // Stattdessen der angegebene Pfad!!!
	    /*texFile = String.format("%s/%s.tex", System
		    .getProperty("user.home"), outfile.getName().substring(0,
		outfile.getName().lastIndexOf('.')));*/
	  
	  // Get the path and simply change extension
	  texFile = outfile.getAbsolutePath();
	  texFile = texFile.substring(0, texFile.lastIndexOf('.'))+".tex";
	  
	} else if (SBFileFilter.createTeXFileFilter().accept(outfile)) {
	    texFile = outfile.getAbsolutePath();
	} else {
	    throw new IOException(String.format(
		"File %s is no valid LaTeX file.", outfile.getAbsolutePath()));
	}

	SBPreferences prefsLaTeX = SBPreferences
		.getPreferencesFor(LaTeXOptions.class);
	boolean preDefUnits = prefsLaTeX
		.getBoolean(LaTeXOptions.SHOW_PREDEFINED_UNITS);
	boolean landscape = prefsLaTeX.getBoolean(LaTeXOptions.LANDSCAPE);
	boolean nameInEquations = prefsLaTeX
		.getBoolean(LaTeXOptions.PRINT_NAMES_IF_AVAILABLE);
	boolean titlePage = prefsLaTeX
		.getBoolean(LaTeXOptions.TITLE_PAGE);
	boolean idsInTypeWriter = prefsLaTeX
		.getBoolean(LaTeXOptions.TYPEWRITER);
	boolean miriam = prefsLaTeX.getBoolean(LaTeXOptions.MIRIAM_ANNOTATION);
	boolean reactantsOverviewTable = prefsLaTeX
		.getBoolean(LaTeXOptions.REACTANTS_OVERVIEW_TABLE);
	boolean checkConsistency = prefsLaTeX
		.getBoolean(LaTeXOptions.CHECK_CONSISTENCY);
	boolean printFullODEsystem = prefsLaTeX
		.getBoolean(LaTeXOptions.PRINT_FULL_ODE_SYSTEM);
	short fontSize = prefsLaTeX.getShort(LaTeXOptions.FONT_SIZE);
	String paperSize = prefsLaTeX.get(LaTeXOptions.PAPER_SIZE);
	String fontText = prefsLaTeX.get(LaTeXOptions.FONT_TEXT);
	String fontHeadings = prefsLaTeX.get(LaTeXOptions.FONT_HEADINGS);
	String fontTypeWriter = prefsLaTeX
		.get(LaTeXOptions.FONT_TYPEWRITER);
	//String logoFile = prefsLaTeX.get(LaTeXOptions.LOGO_INPUT_FILE);
	File latexCompiler = LaTeXOptions.LOAD_LATEX_COMPILER
		.parseOrCast(prefsLaTeX.get(LaTeXOptions.LOAD_LATEX_COMPILER));

	try {
	  
	  // Copy the logo-image
	  File logoFile = new File(Utils.ensureSlash(outfile.getParent()) + "SBML2LaTeX.pdf");
	  Utils.copyStream(SBML2LaTeX.class.getResourceAsStream("gui/img/SBML2LaTeX.pdf"), logoFile);
	  String logoFileString = logoFile.getAbsolutePath();
	  if (File.separatorChar=='\\') {
	    logoFileString = logoFileString.replace(File.separatorChar, '/');
	  }
	  
	  
	    long time = System.currentTimeMillis();
	    LaTeXReportGenerator export = new LaTeXReportGenerator();
	    export.setShowPredefinedUnitDeclarations(preDefUnits);
	    export.setFontSize(fontSize);
	    export.setLandscape(landscape);
	    export.setPaperSize(paperSize);
	    export.setPrintNameIfAvailable(nameInEquations);
	    export.setTitlepage(titlePage);
	    export.setTypewriter(idsInTypeWriter);
	    export.setCheckConsistency(checkConsistency);
	    export.setIncludeMIRIAM(miriam);
	    export.setTextFont(fontText);
	    export.setHeadingsFont(fontHeadings);
	    export.setTypewriterFont(fontTypeWriter);
	    export.setArrangeReactionParticipantsInOneTable(reactantsOverviewTable);
	    export.setPrintFullODEsystem(printFullODEsystem);
	    LaTeXReportGenerator.setLogoFile(logoFileString);
	    BufferedWriter buffer = new BufferedWriter(new FileWriter(texFile));
	    if (sbase instanceof SBMLDocument) {
		export.format((SBMLDocument) sbase, buffer);
	    } else if (sbase instanceof Model) {
		export.format((Model) sbase, buffer);
	    } else if (sbase instanceof Reaction) {
		buffer.append(export.toLaTeX((Reaction) sbase));
	    } else {
		throw new IllegalArgumentException(
		    String.format(
				"Only instances of SBMLDocument, Model, or Reaction are acceptable. Received %s.",
				sbase.getClass().getName()));
	    }
	    buffer.close();
	    System.out.printf("Time: %s ms\n", (System.currentTimeMillis() - time));
	} catch (IOException e) {
	    throw new IOException(String.format("Cannot write to file %s.",
		texFile));
	}

	/*
	 * Create a PDF file directly
	 */
	if (SBFileFilter.createPDFFileFilter().accept(outfile)) {
	  File laTeXCompiler = latexCompiler; // the user-definex latex compiler
	  String latexCommand = laTeXCompiler.getAbsolutePath(); // To be auto-infered, if no user-def. available
	  if (!laTeXCompiler.exists() || laTeXCompiler.isDirectory() || !laTeXCompiler.canExecute()) {
	    // Try to auto-detect the latex compiler
	    latexCommand = locateLaTeX();
	    if (latexCommand==null) {
	      // Autodetection failed and user didn't define a compiler.
	      String message = "Please enter a valid path to PDFLaTeX on your system.";
	      if (gui) {
	      JOptionPane.showMessageDialog(null,
	        message,
	        "Missing Path to LaTeX Compiler",
	        JOptionPane.WARNING_MESSAGE);
	      } else {
	        System.out.println(message);
	      }
	    }
	  }
	  
	  if (latexCommand!=null ) {
	    Object jtextarea=null;
	    
	    // compile
	    try {
	      if (gui) {
	        JBrowserPane pane = new JBrowserPane(SBML2LaTeX.class.getResource("cfg/limitations.html"));
	        pane.setPreferredSize(new Dimension(480, 240));
          // Show this mesage, but continue the execution of pdftex
	        GUITools.showMessageDialogInNewThred(new JScrollPane(
            pane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
            "Limitations");
	      }
	      
	      // Execute latex two times to ensure correct compilation
	      for (int i = 0; i < 2; i++) {
	        ProcessBuilder builder = new ProcessBuilder(
	          latexCommand, "-interaction", "nonstopmode", texFile);
	        builder.redirectErrorStream(true);
	        // If you do this, the document will be created inside the user.home
	        //builder.directory(new File(System.getProperty("user.home")));
	        builder.directory(new File(outfile.getParent()));
	        Process p = builder.start();
	        
	        // Show the process output inside a textarea
	        /* We create an object to ensure that no gui elements are
	         * loaded when not wanted!
	         */
	        if (gui) {
	          jtextarea = GUITools.showProcessOutputInTextArea(p,"SBML2LaTeX",(i==0));
	        }
	        
	        p.waitFor();
	      }
	      
	      // move PDF file
	      File pdfFile = new File(texFile.replace(".tex", ".pdf"));
	      pdfFile.renameTo(outfile);
	      
	      // Notify the user
	      if (!outfile.exists() || outfile.length()<1) {
	        String message = "Could not compile the latex document. Please compile it manually.";
	        if (gui) {
	          JOptionPane.showMessageDialog(null,
	            message, "Failed executing the LaTeX Compiler",
	            JOptionPane.WARNING_MESSAGE);
	        } else {
	          System.out.println(message);
	        }
	      } else {
	        if (gui) {
	          // Close eventually open textArea
	          if (jtextarea!=null) {
	            Window w = GUITools.getParentWindow((Component)jtextarea);
	            w.dispose();
	          }
	          // open standard pdf viewer
	          // TODO: Java 1.6 only!
	          Desktop.getDesktop().open(pdfFile);
	        } else {
	          System.out.println("Document compiled succesfully.");
	        }
	      }
	    } catch (InterruptedException exc) {
	      throw new IOException(exc);
	    }
	  }
	} else if (gui) {
	    /*
	     * Create a LaTeX file only.
	     */
	    // open generated LaTeX file.
	    // TODO: Java 1.6 only!
	    Desktop.getDesktop().open(outfile);
	}
	if (prefsLaTeX.getBoolean(LaTeXOptions.CLEAN_WORKSPACE)) {
	    String baseFile = outfile.getAbsolutePath();
	    baseFile = baseFile.substring(0, baseFile.lastIndexOf('.'));
	    String extensions[] = { "aux", "bbl", "blg", "log", "out", "tex~",
		    "tex.backup", "toc" };
	    File file;
	    for (String extension : extensions) {
		file = new File(String.format("%s.%s", baseFile, extension));
		try {
		    if (file.exists() && file.canWrite() && file.isFile()
			    && (Math.abs(file.lastModified()
				    - System.currentTimeMillis()) < 86400000)) {
			// file has been created within the last 24 h = 86400000 ms.
			System.out.printf("deleting temporary file %s.\n", file
				.getAbsolutePath());
			file.delete();
		    }
		} catch (Throwable exc) {
		    exc.printStackTrace();
		}
	    }
	}
    }

    /**
     * 
     * @param sbase
     * @param outfile
     * @throws IOException
     * @throws SBMLException
     */
    public static void convert(SBase sbase, String outfile) throws IOException, SBMLException {
	convert(sbase, outfile, false);
    }
    
    /**
     * 
     * @param sbase
     * @param outfile
     * @param gui
     * @throws SBMLException 
     * @throws IOException 
     */
    public static void convert(SBase sbase, String outfile, boolean gui) throws IOException, SBMLException {
	convert(sbase, new File(outfile), gui);
    }

    /**
     * @param infile
     * @param outfile
     * @throws XMLStreamException
     * @throws SBMLException
     * @throws IOException
     * @throws HeadlessException
     */
    public static void convert(String infile, String outfile)
	throws HeadlessException, IOException, SBMLException,
	XMLStreamException {
	convert(infile, outfile, false);
    }

    /**
     * 
     * @param infile
     * @param outfile
     * @param gui
     * @throws HeadlessException
     * @throws IOException
     * @throws SBMLException
     * @throws XMLStreamException
     */
    public static void convert(String infile, String outfile, boolean gui) throws HeadlessException, IOException, SBMLException, XMLStreamException {
	convert(new File(infile), new File(outfile), gui);
    }

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
		SBProperties props = SBPreferences.analyzeCommandLineArguments(
				getCommandLineOptions(), args);
		new SBML2LaTeX(Boolean.parseBoolean(props.getProperty(GUIOptions.GUI)));
		System.exit(0);
	}
    
    /**
     * 
     * @return
     */
    public static List<Class<? extends KeyProvider>> getCommandLineOptions() {
	LinkedList<Class<? extends KeyProvider>> defAndKeys = new LinkedList<Class<? extends KeyProvider>>();
	defAndKeys.add(LaTeXOptionsIO.class);
	defAndKeys.add(LaTeXOptions.class);
	defAndKeys.add(GUIOptions.class);
	return defAndKeys;
    }

    /**
     * @throws SBMLException 
     * @throws XMLStreamException 
     * @throws IOException 
     * 
     */
    public SBML2LaTeX() throws IOException, XMLStreamException, SBMLException {
	this(true);
    }

    /**
     * 
     * @param gui
     * @throws SBMLException 
     * @throws XMLStreamException 
     * @throws IOException 
     * @throws IOException
     * @throws XMLStreamException
     * @throws SBMLException
     */
	public SBML2LaTeX(boolean gui) throws IOException, XMLStreamException,
			SBMLException {
		this(null, gui);
	}
    
    /**
     * 
     * @param sbase
     * @param gui
     * @throws IOException
     * @throws XMLStreamException
     * @throws SBMLException
     */
    public SBML2LaTeX(SBase sbase, boolean gui) throws IOException, XMLStreamException,
	SBMLException {

	boolean convert = true;
	try {
			if (gui) {
				LaTeXExportDialog.initGUI();
				convert = LaTeXExportDialog.showDialog(sbase);
			}
	} catch (HeadlessException exc) {
	    if (gui) {
		System.err
			.println("Cannot initialize the graphical user interface.");
		exc.printStackTrace();
	    }
	} finally {
	    if (convert) {
		SBPreferences prefsIO = SBPreferences
			.getPreferencesFor(LaTeXOptionsIO.class);
		try {
		    convert(prefsIO.get(LaTeXOptionsIO.SBML_INPUT_FILE),
			prefsIO.get(LaTeXOptionsIO.REPORT_OUTPUT_FILE), gui);
		} catch (Throwable exc) {
		    if (gui) {
			GUITools.showErrorMessage(null, exc);
		    } else {
			exc.printStackTrace();
		    }
		}
	    }
	}
    }
    
    
    /**
     * This function tries to locate the latex executable within
     * the current operating system.
     * @return null if it failed. Else, the executable command
     * (NOT necessarily a full file path object, but an executable
     * command!).
     */
    public static String locateLaTeX() {
      boolean isWindows = (System.getProperty("os.name").toLowerCase().contains("windows"));
      Runtime run = Runtime.getRuntime();
      String executable = null;
      String executableName = "pdflatex";
      
      if (!isWindows) {
        // Search with linux command "which"
        try {
          Process pr = run.exec("which "+executableName);
          pr.waitFor();
          pr.getOutputStream();
          
          // read the child process' output
          InputStreamReader r = new InputStreamReader(pr.getInputStream());
          BufferedReader in = new BufferedReader(r);
          String line = in.readLine(); // one line is enough
          if (!line.toLowerCase().contains("no ") && !line.contains(":"))
            executable = line;
          
        } catch(Throwable e) {}
      }
      
      // On windows systems or if linux which failed
      if (executable==null) {
        try {
          // Simply try to execute the latex command (it is on the
          // path environment variable on most systems).
          Process pr = run.exec(executableName+" -version");
          pr.waitFor();
          
          // If not found, an error is thrown.
          if (pr.exitValue()==0) executable = executableName;
        } catch(Throwable e) {}
      }
      
      return executable;
    }
    
}