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

import java.awt.Image;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdom.JDOMException;
import org.sbml.libsbml.SBMLReader;

import cz.kebrt.html2latex.HTML2LaTeX;

/**
 * Converts an SBML file into a LaTeX file
 * 
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @date Mar 19, 2008
 */
public class SBMLconverter {

	private boolean showGUI = false;
	private boolean showPredefinedUnits = true;
	private boolean landscape = false;
	private boolean nameIfAvailable = false;
	private boolean titlePage = false;
	private boolean typeWriter = true;
	private boolean printHelp = false;
	private boolean checkConsistency = false;
	private boolean includeMIRIAM = false;
	private short fontSize = 11;
	private File infile = null;
	private File outfile = null;
	private String paperSize = "a4";
	private String logo = "";
	private String sbo = "";
	private String miriam = "";
	private String htmlConfig = "";
	private String fontText = "times";
	private String fontHeadings = "helvet";
	private String fontTypewriter = "cmt";
	private ImageIcon icon = null;
	private boolean oneTableForReactionParticipants = false;

	/**
	 * This is not for public use.
	 * 
	 * @param args
	 */
	private SBMLconverter(String[] args) {
		analyzeArguments(args);
		if (printHelp)
			printHelp();
		if (showGUI)
			showGUI();
		if ((infile != null) && (outfile != null))
			try {
				convert(infile, outfile);
			} catch (IOException exc) {
				if (showGUI)
					JOptionPane.showMessageDialog(null, exc.getMessage(), exc
							.getClass().getName(), JOptionPane.WARNING_MESSAGE,
							icon);
				exc.printStackTrace();
			} catch (JDOMException exc) {
				exc.printStackTrace();
			}
	}

	/**
	 * 
	 * @param infile
	 * @param outfile
	 * @throws IOException
	 * @throws JDOMException
	 */
	private void convert(File infile, File outfile) throws IOException,
			JDOMException {
		System.out.println("Converting file");
		System.out.println(infile.getAbsolutePath());
		System.out.println("to");
		System.out.println(outfile.getAbsolutePath());
		if ((infile != null) && infile.exists() && infile.isFile()
				&& infile.canRead()) {
			long time = System.currentTimeMillis();
			System.loadLibrary("sbmlj");
			SBML2LaTeX export = new SBML2LaTeX();
			export.setShowPredefinedUnitDeclarations(showPredefinedUnits);
			export.setFontSize(fontSize);
			export.setLandscape(landscape);
			export.setPaperSize(paperSize);
			export.setPrintNameIfAvailable(nameIfAvailable);
			export.setTitlepage(titlePage);
			export.setTypewriter(typeWriter);
			export.setCheckConsistency(checkConsistency);
			export.setIncludeMIRIAM(includeMIRIAM);
			export.setTextFont(fontText);
			export.setHeadingsFont(fontHeadings);
			export.setTypewriterFont(fontTypewriter);
			export
					.setArrangeReactionParticipantsInOneTable(oneTableForReactionParticipants);
			if (logo.length() > 0)
				SBML2LaTeX.setLogoFile(logo);
			if (sbo.length() > 0)
				SBOParser.setSBOOboFile(sbo);
			if (miriam.length() > 0)
				SBML2LaTeX.setMIRIAMfile(miriam);
			if (htmlConfig.length() > 0)
				HTML2LaTeX.setConfigFile(htmlConfig);
			BufferedWriter buffer = new BufferedWriter(new FileWriter(outfile));
			export.format(
					(new SBMLReader()).readSBML(infile.getAbsolutePath()),
					buffer);
			buffer.close();
			System.out.println("Time: " + (System.currentTimeMillis() - time)
					+ " ms");
		}
	}

	/**
	 * 
	 */
	private void showGUI() {
		try {
			// String fileSeparator = System.getProperty("file.separator");
			Image image = ImageIO.read(getClass().getResource(
					"SBML2LaTeX_vertical_small.png"));
			// System.getProperty("user.dir") + fileSeparator
			// + "resources" + fileSeparator
			// + "SBML2LaTeX_vertical_small.png"));
			icon = new ImageIcon(image);
			// .getScaledInstance(100, 100, Image.SCALE_SMOOTH));
			JOptionPane.getRootFrame().setIconImage(image);
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(null, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException exc) {
			JOptionPane.showMessageDialog(null, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.WARNING_MESSAGE, icon);
			exc.printStackTrace();
		} catch (InstantiationException exc) {
			JOptionPane.showMessageDialog(null, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.WARNING_MESSAGE, icon);
			exc.printStackTrace();
		} catch (IllegalAccessException exc) {
			JOptionPane.showMessageDialog(null, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.WARNING_MESSAGE, icon);
			exc.printStackTrace();
		} catch (UnsupportedLookAndFeelException exc) {
			JOptionPane.showMessageDialog(null, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.WARNING_MESSAGE, icon);
			exc.printStackTrace();
		}

		SBML2LaTeXOptionsPanel settingsPanel = new SBML2LaTeXOptionsPanel();
		settingsPanel.setShowImplicitUnitDeclarations(showPredefinedUnits);
		settingsPanel.setFontSize(fontSize);
		settingsPanel.setLandscape(landscape);
		settingsPanel.setPaperSize(paperSize);
		settingsPanel.setPrintNameIfAvailable(nameIfAvailable);
		settingsPanel.setTitlePage(titlePage);
		settingsPanel.setTypeWriter(typeWriter);
		settingsPanel.setCheckConsistency(checkConsistency);
		settingsPanel.setIncludeMIRIAM(includeMIRIAM);
		settingsPanel.setFontHeadings(fontHeadings);
		settingsPanel.setFontText(fontText);
		settingsPanel.setFontTypewriter(fontTypewriter);
		settingsPanel
				.setOneTableForReactionParticipants(oneTableForReactionParticipants);
		if (infile != null)
			settingsPanel.setSBMLFile(infile);
		if (outfile != null)
			settingsPanel.setTeXFile(outfile);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(settingsPanel);

		int returnValue = JOptionPane.showConfirmDialog(null, panel,
				"SBML2LaTeX", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, icon);
		if (JOptionPane.OK_OPTION == returnValue) {
			landscape = settingsPanel.isLandscape();
			typeWriter = settingsPanel.isTypeWriter();
			fontSize = settingsPanel.getFontSize();
			paperSize = settingsPanel.getPaperSize();
			showPredefinedUnits = settingsPanel
					.isShowImplicitUnitDeclarations();
			titlePage = settingsPanel.getTitlePage();
			checkConsistency = settingsPanel.isSetCheckConsistency();
			nameIfAvailable = settingsPanel.getPrintNameIfAvailable();
			includeMIRIAM = settingsPanel.isSetIncludeMIRIAM();
			oneTableForReactionParticipants = settingsPanel
					.isSetOneTableForReactionParticipants();
			fontText = settingsPanel.getFontText();
			fontHeadings = settingsPanel.getFontHeadings();
			fontTypewriter = settingsPanel.getFontTypewriter();
			if (settingsPanel.getSBMLFile().length() == 0)
				JOptionPane.showMessageDialog(null,
						"Nothing to do because no SBML file was specified.");
			else if (settingsPanel.getTeXFile().length() == 0)
				JOptionPane.showMessageDialog(null,
						"Nothing to do because no TeX file was specified");
			else {
				infile = new File(settingsPanel.getSBMLFile());
				outfile = new File(settingsPanel.getTeXFile());
			}
		} else if ((JOptionPane.CLOSED_OPTION == returnValue)
				|| (JOptionPane.CANCEL_OPTION == returnValue))
			System.exit(0);
	}

	/**
	 * 
	 */
	private void printHelp() {
		System.out.println("Usage:");
		System.out.println("SBMLconverter in_file.xml out_file.tex [options]");
		System.out.println();
		System.out.println("Possible command line options (case and ordering");
		System.out.println("are ignored):");
		System.out.println();

		System.out.println("1. Options to include or exclude SBML information");
		System.out.println();

		System.out.println("--show-predefined-units=<true|false>");
		System.out.println("\tIf true (default), all predefined unit");
		System.out.println("\tdefinitions of SBML are made explicit.");

		System.out.println("--miriam=<true|false>");
		System.out.println("\tIf true (default), MIRIAM annotations are");
		System.out.println("\tincluded into the model report if there");
		System.out.println("\tare any. This option may require the path");
		System.out.println("\tto the MIRIAM translation file to be specified.");

		System.out.println("--check-consistency=<true|false>");
		System.out.println("\tIf true, the libSBML model consistency check");
		System.out.println("\tis performed and the results are written in");
		System.out.println("\tthe glossary of the model report.");
		System.out.println();

		System.out.println("2. Layout options for the model report");
		System.out.println();

		System.out.println("--paper-size=<a<0..9>|b<0..9>|c<0..9>|d<0..9>|");
		System.out.println("    letter|legal|executive>");
		System.out.println("\tWith this option the paper format can be");
		System.out.println("\tinfluenced. Default paper size: DIN A4.");
		System.out.println("\tAll sizes a?, b?, c? and d? are European");
		System.out.println("\tDIN sizes. Letter, legal and executive are");
		System.out.println("\tUS paper formats.");

		System.out.println("--landscape=<true|false>");
		System.out.println("\tIf true, the whole report is written on");
		System.out.println("\tlandscape paper instead of portrait format.");

		System.out.println("--title-page=<true|false>");
		System.out.println("\tIf true, an extra title page will be created.");

		System.out.println("--font-size=<8|9|10|11|12|14|17>");
		System.out.println("\tThis option allows you to select a smaller or");
		System.out.println("\tlarger font (for continuous text).");
		System.out.println("\tDefault size: 11 pt.");

		System.out.println("--font-text=<chancery|charter|cmr|palatino|");
		System.out.println("    times|utopia>");
		System.out.println("\tAllows to select the font of continuous text.");
		System.out.println("\tDefault: times.");

		System.out.println("--font-headings=<avant|cmss|helvetica>");
		System.out.println("\tAllows to select the font of captions and other");
		System.out.println("\tsans serif text. Default: helvetica");

		System.out.println("--typewriter=<true|false>");
		System.out.println("\tIf true (default) all identifiers are written");
		System.out.println("\tin typewriter font.");

		System.out.println("--font-typewriter=<cmt|courier>");
		System.out.println("\tSelects the font to be used for typewriter");
		System.out.println("\ttext. Default: cmt.");

		System.out.println("--print-name-if-available=<true|false>");
		System.out.println("\tIf true, the names of elements are used");
		System.out.println("\tin equations instead of their identifiers.");

		System.out.println("--reactants-overview-table=<true|false>");
		System.out.println("\tIf true, the details (identifier and name)");
		System.out.println("\t of all reactants, modifiers and products");
		System.out.println("\tparticipating in a reaction are listed in");
		System.out.println("\tone table. By default a separate table");
		System.out.println("\tis created for each one of the three");
		System.out.println("\tparticipant groups including its SBO term.");
		System.out.println();

		System.out.println("3. Definition of required file paths");
		System.out.println();

		System.out.println("--logo-file=<path to logo file (a graphics file)>");
		System.out.println("\tThe SBML logo to be displayed at the");
		System.out.println("\tbeginning of the model report. Default path:");
		System.out.println("\tresources/SBML2LaTeX.pdf");

		System.out.println("--sbo-file=<path to SBO obo file>");
		System.out.println("\tThe file containing the SBO translation in");
		System.out.println("\tOBO format. Default path:");
		System.out.println("\tresources/SBO_OBO.obo");

		System.out.println("--miriam-file=<path to MIRIAM XML file>");
		System.out.println("\tThe file containing the translation of");
		System.out.println("\tURNs/URIs to actual URLs. Default path:");
		System.out.println("\tresources/MIRIAM.xml");

		System.out.println("--html-config-file=<path to HTML2LaTeX");
		System.out.println("    configuration file>");
		System.out.println("\tThe configuration file for the translation");
		System.out.println("\tof notes in XHTML format to LaTeX commands");
		System.out.println("\tand text. Default path: resources/config.xml");
		System.out.println();

		System.out.println("4. General program options");
		System.out.println();

		System.out.println("--gui");
		System.out.println("\tOpens a dialog window for convenient usage.");
		System.out.println("\tWith this option no in/out-file needs to be");
		System.out.println("\tspecified. All other command line options are");
		System.out.println("\taccepted and their values influence the");
		System.out.println("\tbehavior of the dialog window.");

		System.out.println("--help");
		System.out.println("\tShows this overview.");
	}

	/**
	 * 
	 * @param args
	 */
	private void analyzeArguments(String[] args) {
		int i;
		for (i = 0; i < args.length; i++) {
			if (args[i].toLowerCase().equals("--gui"))
				showGUI = true;
			if (args[i].equalsIgnoreCase("--help"))
				printHelp = true;
		}
		if ((args.length < 2) && !showGUI)
			printHelp = true;
		else if (args.length > 1) {
			if (!args[0].startsWith("-"))
				infile = new File(args[0]);
			if (!args[1].startsWith("-"))
				outfile = new File(args[1]);
			for (i = 0; i < args.length; i++) {
				if (!args[i].startsWith("-"))
					continue;
				String field[] = args[i].split("=");
				if (field.length == 2) {
					if (field[0].equalsIgnoreCase("--show-predefined-units"))
						showPredefinedUnits = Boolean.parseBoolean(field[1]
								.toLowerCase());
					else if (field[0].equalsIgnoreCase("--landscape"))
						landscape = Boolean
								.parseBoolean(field[1].toLowerCase());
					else if (field[0]
							.equalsIgnoreCase("--print-name-if-available"))
						nameIfAvailable = Boolean.parseBoolean(field[1]
								.toLowerCase());
					else if (field[0].equalsIgnoreCase("--title-page"))
						titlePage = Boolean
								.parseBoolean(field[1].toLowerCase());
					else if (field[0].equalsIgnoreCase("--font-size"))
						fontSize = Short.parseShort(field[1]);
					else if (field[0].equalsIgnoreCase("--font-text"))
						fontText = field[1].toLowerCase();
					else if (field[0].equalsIgnoreCase("--font-headings"))
						fontHeadings = field[1].toLowerCase();
					else if (field[0].equalsIgnoreCase("--typewriter"))
						typeWriter = Boolean.parseBoolean(field[1]
								.toLowerCase());
					else if (field[0].equalsIgnoreCase("--font-typewriter"))
						fontTypewriter = field[1].toLowerCase();
					else if (field[0].equalsIgnoreCase("--paper-size"))
						paperSize = field[1].toLowerCase();
					else if (field[0].equalsIgnoreCase("--check-consistency"))
						checkConsistency = Boolean.parseBoolean(field[1]
								.toLowerCase());
					else if (field[0].equalsIgnoreCase("--miriam"))
						includeMIRIAM = Boolean.parseBoolean(field[1]
								.toLowerCase());
					else if (field[0].equalsIgnoreCase("--logo-file"))
						logo = field[1];
					else if (field[0].equalsIgnoreCase("--sbo-file"))
						sbo = field[1];
					else if (field[0].equalsIgnoreCase("--miriam-file"))
						miriam = field[1];
					else if (field[0].equalsIgnoreCase("--html-config-file"))
						htmlConfig = field[1];
					else if (field[0]
							.equalsIgnoreCase("--reactants-overview-table"))
						oneTableForReactionParticipants = Boolean
								.parseBoolean(field[1].toLowerCase());
					else {
						System.err.println("Illegal option/value pair:\t"
								+ field[0] + "\t" + field[1]);
						printHelp = true;
					}
				} else if (!args[i].equalsIgnoreCase("--gui"))
					printHelp = true;
			}
		}
	}

	/**
	 * Starts the SBMLconverter program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.print("SBML2LaTeX. Copyright (C) 2009 Center for ");
		System.out.println("Bioinformatics T\u00fcbingen (ZBIT),");
		System.out
				.println(" University of T\u00fcbingen, Andreas Dr\u00e4ger.");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY.");
		System.out.println("This is free software, and you are welcome to");
		System.out.println("redistribute it under certain conditions.");
		System.out.print("For details see ");
		System.out.println("<http://www.gnu.org/licenses/gpl-3.0.html>.");
		System.out.println("Third party libraries used by this program:");
		System.out.print("JDOM  Copyright (C) 2000-2004 Jason Hunter and");
		System.out.println(" Brett McLaughlin. All rights reserved.");
		System.out.print("Jaxen Copyright (C) 2001 werken digital.");
		System.out.println(" All Rights Reserved.");
		System.out.print("Xalan Copyright (c) 1999 The Apache Software");
		System.out.println(" Foundation. All rights reserved.");
		System.out.println();
		new SBMLconverter(args);
	}
}

/**
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 */
class JTextAreaStream extends FilterOutputStream {
	private JTextArea area;

	public JTextAreaStream(JTextArea area) {
		super(new ByteArrayOutputStream());
		this.area = area;
	}

	@Override
	public void write(byte b[]) throws IOException {
		area.append(new String(b));
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		area.append(new String(b, off, len));
	}
}
