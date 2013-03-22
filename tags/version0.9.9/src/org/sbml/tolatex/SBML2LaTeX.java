/*
 * $Id: SBML2LaTeX.java 82 2011-12-13 11:43:28Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/tags/version0.9.9/src/org/sbml/tolatex/SBML2LaTeX.java $
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.StringTools;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.sbml.tolatex.gui.LaTeXExportDialog;
import org.sbml.tolatex.io.LaTeXOptionsIO;
import org.sbml.tolatex.io.LaTeXReportGenerator;

import de.zbit.AppConf;
import de.zbit.Launcher;
import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.JBrowserPane;
import de.zbit.io.SBFileFilter;
import de.zbit.util.FileTools;
import de.zbit.util.ResourceManager;
import de.zbit.util.Utils;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * This class displays the whole information content of an SBML model in a LaTeX
 * file which can then be further processed, i.e., to a PDF file.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date December 4, 2007
 * @version $Rev: 82 $
 */
public class SBML2LaTeX extends Launcher {
  
  /**
   * The logger for this class.
   */
  private static final transient Logger logger = Logger.getLogger(SBML2LaTeX.class.getName());

  /**
   * Access to locale-specific resources.
   */
  private static final transient ResourceBundle bundle = ResourceManager.getBundle("org.sbml.tolatex.locales.UI");
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -6140383974897759824L;
  
  /**
   * 
   * @param infile
   * @param outfile
   * @throws HeadlessException
   * @throws IOException
   * @throws SBMLException
   * @throws XMLStreamException
   */
  public static void convert(File infile, File outfile)
    throws HeadlessException, IOException, SBMLException, XMLStreamException {
    convert(infile, outfile, false);
  }
  
  /**
   * @throws HeadlessException
   * @throws IOException
   * @throws XMLStreamException
   * @throws SBMLException
   */
  public static void convert(File infile, File outfile, boolean gui)
    throws HeadlessException, IOException, SBMLException, XMLStreamException {
    if (!SBFileFilter.isSBMLFile(infile)) {
      throw new IOException(String.format(bundle.getString("INVALID_SBML_FILE"),
        outfile.getAbsolutePath()));
    }
    logger.info(String.format(bundle
        .getString("CONVERTING_SBML_FILE_TO_REPORT"), infile.getAbsolutePath(),
      outfile.getAbsolutePath()));
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
  public static void convert(SBase sbase, File outfile) throws IOException,
    SBMLException {
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
  public static void convert(SBase sbase, File outfile, boolean gui)
    throws IOException, SBMLException {
    String texFile;
    if (SBFileFilter.isPDFFile(outfile)) {
      // Do not use "user.home" at this position!
      // Only the given path!
      /*
       * texFile = String.format("%s/%s.tex", System .getProperty("user.home"),
       * outfile.getName().substring(0, outfile.getName().lastIndexOf('.')));
       */

      // Get the path and simply change extension
      texFile = outfile.getAbsolutePath();
      texFile = FileTools.removeFileExtension(texFile) + ".tex";
      
    } else if (SBFileFilter.createTeXFileFilter().accept(outfile)) {
      texFile = outfile.getAbsolutePath();
    } else {
      throw new IOException(String.format(bundle.getString("INVALID_LATEX_FILE"),
        outfile.getAbsolutePath()));
    }
    
    SBPreferences prefsLaTeX = SBPreferences
        .getPreferencesFor(LaTeXOptions.class);
    //String logoFile = prefsLaTeX.get(LaTeXOptions.LOGO_INPUT_FILE);
    File latexCompiler = LaTeXOptions.LOAD_LATEX_COMPILER
        .parseOrCast(prefsLaTeX.get(LaTeXOptions.LOAD_LATEX_COMPILER));
    
    try {
      
      // Copy the logo-image
      File logoFile = new File(Utils.ensureSlash(outfile.getParent())
          + "SBML2LaTeX.pdf");
      Utils.copyStream(SBML2LaTeX.class
          .getResourceAsStream("gui/img/SBML2LaTeX.pdf"), logoFile);
      String logoFileString = logoFile.getAbsolutePath();
      if (File.separatorChar == '\\') {
        logoFileString = logoFileString.replace(File.separatorChar, '/');
      }
      
      long time = System.currentTimeMillis();
      LaTeXReportGenerator export = new LaTeXReportGenerator();
      export.setShowPredefinedUnitDeclarations(prefsLaTeX
        .getBoolean(LaTeXOptions.SHOW_PREDEFINED_UNITS));
      export.setFontSize(prefsLaTeX.getShort(LaTeXOptions.FONT_SIZE));
      export.setLandscape(prefsLaTeX.getBoolean(LaTeXOptions.LANDSCAPE));
      export.setPaperSize(prefsLaTeX.get(LaTeXOptions.PAPER_SIZE));
      export.setPrintNameIfAvailable(prefsLaTeX
        .getBoolean(LaTeXOptions.PRINT_NAMES_IF_AVAILABLE));
      export.setTitlepage(prefsLaTeX.getBoolean(LaTeXOptions.TITLE_PAGE));
      export.setTypewriter(prefsLaTeX.getBoolean(LaTeXOptions.TYPEWRITER));
      export.setCheckConsistency(prefsLaTeX
        .getBoolean(LaTeXOptions.CHECK_CONSISTENCY));
      export.setIncludeMIRIAM(prefsLaTeX.getBoolean(LaTeXOptions.MIRIAM_ANNOTATION));
      export.setTextFont(prefsLaTeX.get(LaTeXOptions.FONT_TEXT));
      export.setHeadingsFont(prefsLaTeX.get(LaTeXOptions.FONT_HEADINGS));
      export.setTypewriterFont(prefsLaTeX.get(LaTeXOptions.FONT_TYPEWRITER));
      export.setArrangeReactionParticipantsInOneTable(prefsLaTeX
        .getBoolean(LaTeXOptions.REACTANTS_OVERVIEW_TABLE));
      export.setPrintFullODEsystem(prefsLaTeX
        .getBoolean(LaTeXOptions.PRINT_FULL_ODE_SYSTEM));
      export.setIncludeCompartmentsSection(prefsLaTeX
          .getBoolean(LaTeXOptions.INCLUDE_COMPARTMENTS_SECTION));
      export.setIncludeCompartmentTypesSection(prefsLaTeX
          .getBoolean(LaTeXOptions.INCLUDE_COMPARTMENT_TYPES_SECTION));
      export.setIncludeConstraintsSection(prefsLaTeX
          .getBoolean(LaTeXOptions.INCLUDE_CONSTRAINTS_SECTION));
      export.setIncludeEventsSection(prefsLaTeX
          .getBoolean(LaTeXOptions.INCLUDE_EVENTS_SECTION));
      export.setIncludeFunctionDefinitionsSection(prefsLaTeX
          .getBoolean(LaTeXOptions.INCLUDE_FUNCTION_DEFINITIONS_SECTION));
      export.setIncludeInitialAssignmentsSection(prefsLaTeX
          .getBoolean(LaTeXOptions.INCLUDE_INITIAL_ASSIGNMENTS_SECTION));
      export.setIncludeParametersSection(prefsLaTeX
          .getBoolean(LaTeXOptions.INCLUDE_PARAMETERS_SECTION));
      export.setIncludeReactionsSection(prefsLaTeX
          .getBoolean(LaTeXOptions.INCLUDE_REACTIONS_SECTION));
      export.setIncludeRulesSection(prefsLaTeX
          .getBoolean(LaTeXOptions.INCLUDE_RULES_SECTION));
      export.setIncludeSpeciesSection(prefsLaTeX
          .getBoolean(LaTeXOptions.INCLUDE_SPECIES_SECTION));
      export.setIncludeUnitDefinitionsSection(prefsLaTeX
          .getBoolean(LaTeXOptions.INCLUDE_UNIT_DEFINITIONS_SECTION));

      LaTeXReportGenerator.setLogoFile(logoFileString);
      BufferedWriter buffer = new BufferedWriter(new FileWriter(texFile));
      if (sbase instanceof SBMLDocument) {
        export.format((SBMLDocument) sbase, buffer);
      } else if (sbase instanceof Model) {
        export.format((Model) sbase, buffer);
      } else if (sbase instanceof Reaction) {
        buffer.append(export.toLaTeX((Reaction) sbase));
      } else {
        throw new IllegalArgumentException(String.format(bundle
            .getString("INVALID_SBASE"), sbase.getClass().getName()));
      }
      buffer.close();
      logger.info(String.format(bundle.getString("TIME_IN_SECONDS"),
        StringTools.toString((System.currentTimeMillis() - time) / 1E3d)));
    } catch (IOException e) {
      throw new IOException(String.format(bundle
          .getString("CANNOT_WRITE_TO_FILE"), texFile));
    }
    
    /*
     * Create a PDF file directly
     */
    if (SBFileFilter.createPDFFileFilter().accept(outfile)) {
      File laTeXCompiler = latexCompiler; // the user-definex latex compiler
      String latexCommand = laTeXCompiler.getAbsolutePath(); // To be auto-infered, if no user-def. available
      if (!laTeXCompiler.exists() || laTeXCompiler.isDirectory()
          || !laTeXCompiler.canExecute()) {
        // Try to auto-detect the latex compiler
        latexCommand = locateLaTeX();
        if (latexCommand == null) {
          // Autodetection failed and user didn't define a compiler.
          String message = bundle.getString("ENTER_PATH_TO_PDFLATEX");
          if (gui) {
            JOptionPane.showMessageDialog(null, message, bundle
                .getString("MISSING_PDFLATEX"), JOptionPane.WARNING_MESSAGE);
          } else {
            logger.warning(message);
          }
        }
      }
      
      if (latexCommand != null) {
        Object jtextarea = null;
        
        // compile
        try {
          if (gui) {
            JBrowserPane pane = new JBrowserPane(SBML2LaTeX.class
                .getResource("gui/html/limitations.html"));
            pane.setPreferredSize(new Dimension(480, 240));
            // Show this message, but continue the execution of pdftex
            GUITools.showMessageDialogInNewThread(new JScrollPane(pane),
              bundle.getString("LIMITATIONS"));
          }
          
          // Execute latex two times to ensure correct compilation
          for (int i = 0; i < 2; i++) {
            ProcessBuilder builder = new ProcessBuilder(latexCommand,
              "-interaction", "nonstopmode", texFile);
            builder.redirectErrorStream(true);
            // If you do this, the document will be created inside the user.home
            //builder.directory(new File(System.getProperty("user.home")));
            builder.directory(new File(outfile.getParent()));
            Process p = builder.start();
            
            // Show the process output inside a textarea
            /*
             * We create an object to ensure that no gui elements are loaded
             * when not wanted!
             */
            if (gui) {
              jtextarea = GUITools.showProcessOutputInTextArea(p,
                SBML2LaTeX.class.getSimpleName(), (i == 0));
            }
            
            p.waitFor();
          }
          
          // move PDF file
          File pdfFile = new File(texFile.replace(".tex", ".pdf"));
          pdfFile.renameTo(outfile);
          
          // Notify the user
          if (!outfile.exists() || outfile.length() < 1) {
            String message = bundle.getString("COULD_NOT_COMPILE_LATEX_FILE");
            if (gui) {
              JOptionPane.showMessageDialog(null, message, bundle
                  .getString("COMPILATION_ERROR"), JOptionPane.WARNING_MESSAGE);
            } else {
              logger.warning(message);
            }
          } else {
            if (gui) {
              // Close eventually open textArea
              if (jtextarea != null) {
                Window w = GUITools.getParentWindow((Component) jtextarea);
                w.dispose();
              }
              // open standard pdf viewer
              // TODO: Java 1.6 only!
              Desktop.getDesktop().open(pdfFile);
            } else {
              logger.info("COMPILATION_SUCCESSFULL");
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
      String extensions[] = { "aux", "bbl", "blg", "idx", "ilg", "ind", "log",
          "out", "tex~", "tex.backup", "toc" };
      File file;
      for (String extension : extensions) {
        file = new File(String.format("%s.%s", baseFile, extension));
        try {
          if (file.exists() && file.canWrite() && file.isFile()
              && (Math.abs(file.lastModified() - System.currentTimeMillis()) < 86400000)) {
            // file has been created within the last 24 h = 86400000 ms.
            logger.info(String.format(bundle.getString("DELETING_TEMP_FILE"),
              file.getAbsolutePath()));
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
  public static void convert(SBase sbase, String outfile) throws IOException,
    SBMLException {
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
  public static void convert(SBase sbase, String outfile, boolean gui)
    throws IOException, SBMLException {
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
    throws HeadlessException, IOException, SBMLException, XMLStreamException {
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
  public static void convert(String infile, String outfile, boolean gui)
    throws HeadlessException, IOException, SBMLException, XMLStreamException {
    convert(new File(infile), new File(outfile), gui);
  }
    
  /**
   * This function tries to locate the latex executable within the current
   * operating system.
   * 
   * @return <code>null</code> if it failed. Else, the executable command (NOT
   *         necessarily a full file path object, but an executable command!).
   */
  public static String locateLaTeX() {
    String executableName = "pdflatex";
    String executable = FileTools.which("pdflatex").getAbsolutePath();
    
    try {
      executable = FileTools.which(executableName).getAbsolutePath();
    } catch (Throwable exc) {
      logger.fine(exc.getLocalizedMessage());
    }
    
    // On windows systems or if linux which failed
    if (executable == null) {
      try {
        // Simply try to execute the latex command (it is on the
        // path environment variable on most systems).
        Process pr = Runtime.getRuntime().exec(executableName + " -version");
        pr.waitFor();
        
        // If not found, an error is thrown.
        if (pr.exitValue() == 0) {
          executable = executableName;
        }
      } catch(Throwable exc) {
        logger.fine(exc.getLocalizedMessage());
      }
    }
    
    return executable;
  }
  
  /**
   * @param args
   * @throws IOException
   * @throws XMLStreamException
   * @throws SBMLException
   */
  public static void main(String[] args) throws IOException,
    XMLStreamException, SBMLException {
    new SBML2LaTeX(args);
  }
	
	/**
	 * 
	 * @param args
	 */
	public SBML2LaTeX(String args[]) {
		super(args);
	}

	/* (non-Javadoc)
	 * @see de.zbit.Launcher#commandLineMode(de.zbit.AppConf)
	 */
  public void commandLineMode(AppConf appConf) {
    try {
      SBProperties props = appConf.getCmdArgs(); 
      String input = props.get(LaTeXOptionsIO.SBML_INPUT_FILE);
      String output = props.get(LaTeXOptionsIO.REPORT_OUTPUT_FILE);
      if (input == null) {
        logger.fine(bundle.getString("NO_INPUT_GIVEN"));
        System.exit(1);
      }
      if (output == null) {
        logger.fine(bundle.getString("NO_OUTPUT_GIVEN"));
        System.exit(1);
      }
      convert(input, output, false);
    } catch (Throwable exc) {
      logger.fine(exc.getLocalizedMessage());
    }
  }

	/* (non-Javadoc)
	 * @see de.zbit.Launcher#getAppName()
	 */
	public String getAppName() {
		return getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see de.zbit.Launcher#getCmdLineOptions()
	 */
	public List<Class<? extends KeyProvider>> getCmdLineOptions() {
		List<Class<? extends KeyProvider>> defAndKeys = new ArrayList<Class<? extends KeyProvider>>(3);
		defAndKeys.add(LaTeXOptionsIO.class);
		defAndKeys.add(LaTeXOptions.class);
		defAndKeys.add(GUIOptions.class);
		return defAndKeys;
	}

	/* (non-Javadoc)
   * @see de.zbit.Launcher#getInteractiveOptions()
   */
  public List<Class<? extends KeyProvider>> getInteractiveOptions() {
    List<Class<? extends KeyProvider>> defAndKeys = new ArrayList<Class<? extends KeyProvider>>(3);
    defAndKeys.add(LaTeXOptions.class);
    return defAndKeys;
  }

	/* (non-Javadoc)
	 * @see de.zbit.Launcher#getLogPackages()
	 */
	public String[] getLogPackages() {
		return new String[] {"de.zbit", "org.sbml"};
	}

	/* (non-Javadoc)
   * @see de.zbit.Launcher#getURLlicenseFile()
   */
  public URL getURLlicenseFile() {
    try {
      return new URL("http://www.gnu.org/licenses/gpl.html");
    } catch (MalformedURLException e) {
      return null;
    }
  }

	/* (non-Javadoc)
	 * @see de.zbit.Launcher#getURLOnlineUpdate()
	 */
	public URL getURLOnlineUpdate() {
		try {
      return new URL("http://www.cogsys.cs.uni-tuebingen.de/software/SBML2LaTeX/downloads/");
    } catch (MalformedURLException e) {
      return null;
    }
	}

  /* (non-Javadoc)
	 * @see de.zbit.Launcher#getVersionNumber()
	 */
	public String getVersionNumber() {
		return "0.9.9";
	}

  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearOfProgramRelease()
   */
  public short getYearOfProgramRelease() {
    return (short) 2011;
  }

  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearWhenProjectWasStarted()
   */
  public short getYearWhenProjectWasStarted() {
    return (short) 2007;
  }

  /* (non-Javadoc)
	 * @see de.zbit.Launcher#guiMode(de.zbit.util.prefs.SBProperties)
	 */
	@Override
	public void guiMode(AppConf props) {
		LaTeXExportDialog.initGUI();
		if (LaTeXExportDialog.showDialog((SBase) null)) {
			SBPreferences prefsIO = SBPreferences
					.getPreferencesFor(LaTeXOptionsIO.class);
			try {
				convert(prefsIO.get(LaTeXOptionsIO.SBML_INPUT_FILE),
						prefsIO.get(LaTeXOptionsIO.REPORT_OUTPUT_FILE), true);
			} catch (Throwable exc) {
				GUITools.showErrorMessage(null, exc);
			}
		}
	}

  /* (non-Javadoc)
   * @see de.zbit.Launcher#initGUI(de.zbit.AppConf)
   */
	public Window initGUI(AppConf appConf) {
		return null;
	}
    
}