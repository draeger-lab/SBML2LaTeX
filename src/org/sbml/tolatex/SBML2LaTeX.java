/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2007-2014 by the University of Tuebingen, Germany.
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.sbml.tolatex.LaTeXOptions.PaperSize;
import org.sbml.tolatex.gui.SBML2LaTeXGUI;
import org.sbml.tolatex.io.LaTeXOptionsIO;
import org.sbml.tolatex.io.LaTeXReportGenerator;

import de.zbit.AppConf;
import de.zbit.Launcher;
import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.io.FileTools;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
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
 * @version $Rev$
 */
public class SBML2LaTeX extends Launcher implements SBML2LaTeXView {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(SBML2LaTeX.class.getName());
  
  /**
   * Localization support.
   */
  private static final transient ResourceBundle bundle = ResourceManager.getBundle("org.sbml.tolatex.locales.UI");
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -7962146074951565502L;
  
  /**
   * The current version number of this program.
   */
  public static final String VERSION_NUMBER = "1.0";
  
  /**
   * 
   * @param infile
   * @param outfile
   * @return
   * @throws IOException
   * @throws XMLStreamException
   */
  public static File convert(File infile, File outfile) throws IOException, XMLStreamException {
    return convert(infile, outfile, new SBML2LaTeX());
  }
  
  /**
   * 
   * @param infile
   * @param outfile
   * @param gui
   * @throws XMLStreamException
   * @throws IOException
   */
  public static File convert(File infile, File outfile, SBML2LaTeXView gui) throws IOException, XMLStreamException {
    if (!SBFileFilter.isSBMLFile(infile)) {
      throw new IOException(MessageFormat.format(
        bundle.getString("INVALID_SBML_FILE"),
        outfile.getAbsolutePath()));
    }
    logger.info(MessageFormat.format(
      bundle.getString("CONVERTING_SBML_FILE_TO_REPORT"),
      infile.getAbsolutePath(),
      outfile.getAbsolutePath()));
    SBMLReader reader = new SBMLReader();
    return convert(reader.readSBML(infile.getAbsolutePath()), outfile, gui);
  }
  
  /**
   * 
   * @param sbase
   * @param outfile
   * @throws IOException
   * @throws XMLStreamException
   * @throws SBMLException
   */
  public static File convert(SBase sbase, File outfile) throws IOException, SBMLException, XMLStreamException {
    return convert(sbase, outfile, new SBML2LaTeX());
  }
  
  /**
   * 
   * @param sbase
   * @param outfile
   * @param gui
   * @throws IOException
   * @throws XMLStreamException
   * @throws SBMLException
   */
  public static File convert(SBase sbase, File outfile, SBML2LaTeXView gui) throws IOException, SBMLException, XMLStreamException {
    String texFile;
    if (SBFileFilter.isPDFFile(outfile)) {
      // Get the path and simply change extension
      texFile = outfile.getAbsolutePath();
      texFile = texFile.substring(0, texFile.lastIndexOf('.')) + ".tex";
      
    } else if (SBFileFilter.createTeXFileFilter().accept(outfile)) {
      texFile = outfile.getAbsolutePath();
    } else {
      throw new IOException(MessageFormat.format(
        bundle.getString("INVALID_LATEX_FILE"),
        outfile.getAbsolutePath()));
    }
    
    SBPreferences prefsLaTeX = SBPreferences.getPreferencesFor(LaTeXOptions.class);
    boolean preDefUnits = prefsLaTeX.getBoolean(LaTeXOptions.SHOW_PREDEFINED_UNITS);
    boolean landscape = prefsLaTeX.getBoolean(LaTeXOptions.LANDSCAPE);
    boolean nameInEquations = prefsLaTeX.getBoolean(LaTeXOptions.PRINT_NAMES_IF_AVAILABLE);
    boolean titlePage = prefsLaTeX.getBoolean(LaTeXOptions.TITLE_PAGE);
    boolean idsInTypeWriter = prefsLaTeX.getBoolean(LaTeXOptions.TYPEWRITER);
    boolean miriam = prefsLaTeX.getBoolean(LaTeXOptions.MIRIAM_ANNOTATION);
    boolean reactantsOverviewTable = prefsLaTeX.getBoolean(LaTeXOptions.REACTANTS_OVERVIEW_TABLE);
    boolean checkConsistency = prefsLaTeX.getBoolean(LaTeXOptions.CHECK_CONSISTENCY);
    boolean printFullODEsystem = prefsLaTeX.getBoolean(LaTeXOptions.PRINT_FULL_ODE_SYSTEM);
    short fontSize = prefsLaTeX.getShort(LaTeXOptions.FONT_SIZE);
    PaperSize paperSize = PaperSize.valueOf(prefsLaTeX.get(LaTeXOptions.PAPER_SIZE));
    String fontText = prefsLaTeX.get(LaTeXOptions.FONT_TEXT);
    String fontHeadings = prefsLaTeX.get(LaTeXOptions.FONT_HEADINGS);
    String fontTypeWriter = prefsLaTeX.get(LaTeXOptions.FONT_TYPEWRITER);
    //String logoFile = prefsLaTeX.get(LaTeXOptions.LOGO_INPUT_FILE);
    File latexCompiler = prefsLaTeX.getFile(LaTeXOptions.LOAD_LATEX_COMPILER);
    
    try {
      toLaTeXreport(sbase, new File(texFile), preDefUnits, landscape,
        nameInEquations, titlePage, idsInTypeWriter, miriam,
        reactantsOverviewTable, checkConsistency, printFullODEsystem, fontSize,
        paperSize, fontText, fontHeadings, fontTypeWriter,
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_COMPARTMENTS),
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_COMPARTMENT_TYPES),
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_CONSTRAINTS),
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_EVENTS),
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_FUNCTION_DEFINITIONS),
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_INITIAL_ASSIGNMENTS),
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_PARAMETERS),
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_REACTIONS),
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_RULES),
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_SPECIES),
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_SPECIES_TYPES),
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_UNIT_DEFINITIONS),
        prefsLaTeX.getBoolean(LaTeXOptions.INCLUDE_SECTION_LAYOUTS));
    } catch (IOException exc) {
      throw new IOException(MessageFormat.format(
        bundle.getString("CANNOT_WRITE_TO_FILE"),
        texFile), exc);
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
        if (latexCommand == null) {
          // Auto-detection failed and user didn't define a compiler.
          throw new NullPointerException(bundle.getString("ENTER_PATH_TO_PDFLATEX"));
        }
      }
      
      if (latexCommand != null ) {
        // compile
        try {
          outfile = toPDFreport(latexCommand, new File(texFile), gui);
        } catch (InterruptedException exc) {
          throw new IOException(exc);
        }
      }
    }
    
    if (prefsLaTeX.getBoolean(LaTeXOptions.CLEAN_WORKSPACE)) {
      cleanUp(outfile);
    }
    
    return outfile;
  }
  
  /**
   * 
   * @param latexCommand
   * @param texFile
   * @param gui
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  public static File toPDFreport(String latexCommand, File texFile, SBML2LaTeXView gui) throws IOException, InterruptedException {
    gui.displayLimitations();
    
    // Execute latex two times to ensure correct compilation
    for (int i = 0; i < 2; i++) {
      ProcessBuilder builder = new ProcessBuilder(
        latexCommand, "-interaction", "nonstopmode", texFile.getAbsolutePath());
      builder.redirectErrorStream(true);
      builder.directory(new File(texFile.getParent()));
      Process p = builder.start();
      
      // Show the process output in the view.
      gui.displayLaTeXOutput(p, i == 0);
      p.waitFor();
      if (p.exitValue() != 0) {
        throw new IOException(MessageFormat.format(bundle.getString("COULD_NOT_COMPILE_FILE"), texFile));
      }
    }
    
    // move PDF file
    File pdfFile = new File(texFile.getAbsolutePath().replace(".tex", ".pdf"));
    
    // Notify the user
    if (!pdfFile.exists() || (pdfFile.length() < 1)) {
      throw new IOException(bundle.getString("COULD_NOT_COMPILE_LATEX_FILE"));
    }
    
    return pdfFile;
  }
  
  /**
   * 
   * @param outfile
   */
  public static void cleanUp(File outfile) {
    String baseFile = outfile.getAbsolutePath();
    baseFile = baseFile.substring(0, baseFile.lastIndexOf('.'));
    String extensions[] = { "aux", "bbl", "blg", "log", "out", "tex~",
        "tex.backup", "toc" };
    File file;
    for (String extension : extensions) {
      file = new File(StringUtil.concat(baseFile, Character.valueOf('.'), extension).toString());
      try {
        if (file.exists() && file.canWrite() && file.isFile()
            && (Math.abs(file.lastModified()
              - System.currentTimeMillis()) < 86400000)) {
          // file has been created within the last 24 h = 86400000 ms.
          logger.info(MessageFormat.format(
            bundle.getString("DELETING_TEMP_FILE"),
            file.getAbsolutePath()));
          file.delete();
        }
      } catch (Throwable exc) {
        logger.log(Level.WARNING, exc.getLocalizedMessage(), exc);
      }
    }
  }
  
  /**
   * 
   * @param sbase
   * @param texFile
   * @param preDefUnits
   * @param landscape
   * @param nameInEquations
   * @param titlePage
   * @param idsInTypeWriter
   * @param miriam
   * @param reactantsOverviewTable
   * @param checkConsistency
   * @param printFullODEsystem
   * @param fontSize
   * @param paperSize
   * @param fontText
   * @param fontHeadings
   * @param fontTypeWriter
   * @param compadrtmentsSection
   * @param compartmentTypesSection
   * @param constraintsSection
   * @param eventsSection
   * @param FunctionDefSection
   * @param initialAssignmentSection
   * @param parameterSection
   * @param reactionsSection
   * @param rulesSection
   * @param speciesSection
   * @param speciesTypesSection
   * @param unitDefSection
   * @param layoutsSection
   * @return
   * @throws IOException
   * @throws XMLStreamException
   * @throws SBMLException
   */
  public static File toLaTeXreport(SBase sbase, File texFile,
    boolean preDefUnits, boolean landscape, boolean nameInEquations,
    boolean titlePage, boolean idsInTypeWriter, boolean miriam,
    boolean reactantsOverviewTable, boolean checkConsistency,
    boolean printFullODEsystem, short fontSize, PaperSize paperSize,
    String fontText, String fontHeadings, String fontTypeWriter,
    boolean compadrtmentsSection, boolean compartmentTypesSection,
    boolean constraintsSection, boolean eventsSection,
    boolean FunctionDefSection, boolean initialAssignmentSection,
    boolean parameterSection, boolean reactionsSection, boolean rulesSection,
    boolean speciesSection, boolean speciesTypesSection, boolean unitDefSection, boolean layoutsSection)
        throws IOException, SBMLException, XMLStreamException {
    
    // Copy the logo-image
    File logoFile = new File(Utils.ensureSlash(texFile.getParent()) + "SBML2LaTeX.pdf");
    FileTools.copyStream(SBML2LaTeX.class.getResourceAsStream("gui/img/SBML2LaTeX.pdf"), logoFile);
    String logoFileString = logoFile.getAbsolutePath();
    if (File.separatorChar == '\\') {
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
    export.setIncludeCompartmentsSection(compadrtmentsSection);
    export.setIncludeCompartmentTypesSection(compartmentTypesSection);
    export.setIncludeConstraintsSection(constraintsSection);
    export.setIncludeEventsSection(eventsSection);
    export.setIncludeFunctionDefinitionsSection(FunctionDefSection);
    export.setIncludeInitialAssignmentsSection(initialAssignmentSection);
    export.setIncludeParametersSection(parameterSection);
    export.setIncludeReactionsSection(reactionsSection);
    export.setIncludeRulesSection(rulesSection);
    export.setIncludeSpeciesSection(speciesSection);
    export.setIncludeSpeciesTypesSection(speciesTypesSection);
    export.setIncludeUnitDefinitionsSection(unitDefSection);
    export.setIncludeLayoutSection(layoutsSection);
    LaTeXReportGenerator.setLogoFile(logoFileString);
    BufferedWriter buffer = new BufferedWriter(new FileWriter(texFile));
    if (sbase instanceof SBMLDocument) {
      export.format((SBMLDocument) sbase, buffer);
    } else if (sbase instanceof Model) {
      export.format((Model) sbase, buffer);
    } else if (sbase instanceof Reaction) {
      buffer.append(export.toLaTeX((Reaction) sbase));
    } else {
      buffer.close();
      throw new IllegalArgumentException(MessageFormat.format(
        bundle.getString("INVALID_SBASE"),
        sbase.getClass().getName()));
    }
    buffer.close();
    logger.info(MessageFormat.format(bundle.getString("TIME_IN_SECONDS"),
      (System.currentTimeMillis() - time)/1000));
    
    return texFile;
  }
  
  /**
   * 
   * @param sbase
   * @param outfile
   * @return
   * @throws IOException
   * @throws XMLStreamException
   * @throws SBMLException
   */
  public static File convert(SBase sbase, String outfile) throws IOException, SBMLException, XMLStreamException {
    return convert(sbase, outfile, new SBML2LaTeX());
  }
  
  /**
   * 
   * @param sbase
   * @param outfile
   * @param gui
   * @return
   * @throws IOException
   * @throws XMLStreamException
   * @throws SBMLException
   */
  public static File convert(SBase sbase, String outfile, SBML2LaTeXView gui) throws IOException, SBMLException, XMLStreamException {
    return convert(sbase, new File(outfile), gui);
  }
  
  /**
   * 
   * @param infile
   * @param outfile
   * @return
   * @throws IOException
   * @throws XMLStreamException
   */
  public static File convert(String infile, String outfile) throws IOException, XMLStreamException {
    return convert(infile, outfile, new SBML2LaTeX());
  }
  
  /**
   * 
   * @param infile
   * @param outfile
   * @param gui
   * @return
   * @throws IOException
   * @throws XMLStreamException
   */
  public static File convert(String infile, String outfile, SBML2LaTeXView gui) throws IOException, XMLStreamException {
    return convert(new File(infile), new File(outfile), gui);
  }
  
  /**
   * This function tries to locate the latex executable within the current
   * operating system.
   * 
   * @return {@code null} if it failed. Else, the executable command (NOT
   *         necessarily a full file path object, but an executable command!).
   */
  public static String locateLaTeX() {
    boolean isWindows = (System.getProperty("os.name").toLowerCase().contains("windows"));
    Runtime run = Runtime.getRuntime();
    String executable = null;
    String executableName = "pdflatex";
    
    logger.info(bundle.getString("SEARCHING_FOR_LATEX_COMPILER"));
    
    if (!isWindows) {
      // Search with Linux/Unix command "which"
      try {
        Process pr = run.exec("which " + executableName);
        pr.waitFor();
        pr.getOutputStream();
        
        // read the child process' output
        InputStreamReader r = new InputStreamReader(pr.getInputStream());
        BufferedReader in = new BufferedReader(r);
        String line = in.readLine(); // one line is enough
        if (!line.toLowerCase().contains("no ") && !line.contains(":")) {
          executable = line;
        }
      } catch (Throwable e) {
      }
    }
    
    // On Windows systems or if Linux/Unix which failed
    if (executable == null) {
      try {
        // Simply try to execute the latex command (it is on the
        // path environment variable on most systems).
        Process pr = run.exec(executableName + " -version");
        pr.waitFor();
        
        // If not found, an error is thrown.
        if (pr.exitValue() == 0) {
          executable = executableName;
        }
      } catch(Throwable e) {
      }
    }
    
    return executable;
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    new SBML2LaTeX(args);
  }
  
  /**
   * 
   */
  public SBML2LaTeX() {
    super();
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
  @Override
  public void commandLineMode(AppConf appConf) {
    SBProperties args = appConf.getCmdArgs();
    File sbmlInput = null, reportOutput = null;
    if (args.containsKey(LaTeXOptionsIO.SBML_INPUT_FILE)) {
      sbmlInput = new File(args.get(LaTeXOptionsIO.SBML_INPUT_FILE));
    }
    if (args.containsKey(LaTeXOptionsIO.REPORT_OUTPUT_FILE)) {
      reportOutput = new File(args.get(LaTeXOptionsIO.REPORT_OUTPUT_FILE));
    }
    try {
      display(convert(sbmlInput, reportOutput, this));
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getCmdLineOptions()
   */
  @Override
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
  @Override
  public List<Class<? extends KeyProvider>> getInteractiveOptions() {
    List<Class<? extends KeyProvider>> defAndKeys = new ArrayList<Class<? extends KeyProvider>>(1);
    defAndKeys.add(LaTeXOptions.class);
    return defAndKeys;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getURLlicenseFile()
   */
  @Override
  public URL getURLlicenseFile() {
    try {
      return new URL("http://www.gnu.org/licenses/gpl.html");
    } catch (MalformedURLException exc) {
      return null;
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getURLOnlineUpdate()
   */
  @Override
  public URL getURLOnlineUpdate() {
    try {
      return new URL("http://www.cogsys.cs.uni-tuebingen.de/software/SBML2LaTeX/downloads/");
    } catch (MalformedURLException exc) {
      return null;
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getVersionNumber()
   */
  @Override
  public String getVersionNumber() {
    return VERSION_NUMBER;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearOfProgramRelease()
   */
  @Override
  public short getYearOfProgramRelease() {
    return (short) 2013;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearWhenProjectWasStarted()
   */
  @Override
  public short getYearWhenProjectWasStarted() {
    return (short) 2007;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#guiMode(de.zbit.AppConf)
   */
  @Override
  public void guiMode(AppConf appConf) {
    setTerminateJVMwhenDone(false);
    SBProperties args = appConf.getCmdArgs();
    SBPreferences prefsIO = SBPreferences.getPreferencesFor(LaTeXOptionsIO.class);
    boolean flush = false;
    if (args.containsKey(LaTeXOptionsIO.SBML_INPUT_FILE)) {
      prefsIO.put(LaTeXOptionsIO.SBML_INPUT_FILE, args.get(LaTeXOptionsIO.SBML_INPUT_FILE));
      flush = true;
    }
    if (args.containsKey(LaTeXOptionsIO.REPORT_OUTPUT_FILE)) {
      prefsIO.put(LaTeXOptionsIO.REPORT_OUTPUT_FILE, args.get(LaTeXOptionsIO.REPORT_OUTPUT_FILE));
      flush = true;
    }
    if (flush) {
      try {
        prefsIO.flush();
      } catch (BackingStoreException exc) {
        GUITools.showErrorMessage(null, exc);
      }
    }
    new SBML2LaTeXGUI(prefsIO.getFile(LaTeXOptionsIO.SBML_INPUT_FILE));
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#initGUI(de.zbit.AppConf)
   */
  @Override
  public java.awt.Window initGUI(AppConf appConf) {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.tolatex.SBML2LaTeXView#displayLimitations()
   */
  @Override
  public void displayLimitations() {
    logger.warning(bundle.getString("LATEX_COMPILATION_PROBLEMS"));
  }
  
  /* (non-Javadoc)
   * @see org.sbml.tolatex.SBML2LaTeXView#displayLaTeXOutput(java.lang.Process, boolean)
   */
  @Override
  public void displayLaTeXOutput(Process process, boolean firstLaTeXrun) {
    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line = null;
    try {
      while ((line = in.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException exc) {
      logger.log(Level.SEVERE, exc.getLocalizedMessage(), exc);
    }
  }
  
  /* (non-Javadoc)
   * @see org.sbml.tolatex.SBML2LaTeXView#display(java.io.File)
   */
  @Override
  public void display(File resultFile) throws IOException {
    logger.info(MessageFormat.format(bundle.getString("DOCUMENT_SUCCESSFULLY_COMPILED"), resultFile.getAbsolutePath()));
  }
  
}
