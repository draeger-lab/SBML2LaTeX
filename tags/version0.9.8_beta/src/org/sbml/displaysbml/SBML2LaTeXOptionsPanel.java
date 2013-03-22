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

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBML2LaTeXOptionsPanel extends JPanel implements ActionListener,
		PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5710018975434332271L;
	private JCheckBox checkImplicitUnits;
	private JCheckBox checkIncludeMIRIAM;
	private JCheckBox checkLandscape;
	private JCheckBox checkNameIfAvailalbe;
	private JCheckBox checkOneTableForReactionParticipants;
	private JCheckBox checkShowSBMLerrors;
	private JCheckBox checkTitlepage;
	private JCheckBox checkTypeWriter;
	private JButton openButton;
	private JComboBox optionFontHeadings;
	private JComboBox optionFontSize;
	private JComboBox optionFontText;
	private JComboBox optionFontTypewriter;
	private JComboBox optionPaperSize;
	private JButton saveButton;
	private JTextField sbmlFileField;

	private JTextField teXFileField;

	public SBML2LaTeXOptionsPanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		checkImplicitUnits = new JCheckBox(
				"<html>Include predefined unit<br>declarations</html>", true);
		checkImplicitUnits
				.setToolTipText("<html>If selected, all predefined unit declarations are<br>"
						+ "made explicit in the report file. Otherwise only<br>"
						+ "unit definitions from the model are included.</html>");
		checkLandscape = new JCheckBox("<html>Landscape pages</html>", false);
		checkLandscape
				.setToolTipText("<html>If selected, the paper format will be changed to landscape.<br>"
						+ "By default most pages are in portrait format.</html>");
		checkTypeWriter = new JCheckBox(
				"<html>Identifiers in<br>typewriter font</html>", true);
		checkTypeWriter
				.setToolTipText("<html>If selected, a typewriter font is applied to<br>"
						+ "highlight SBML identifiers.</html>");
		checkTitlepage = new JCheckBox("<html>Create a title page</html>",
				false);
		checkTitlepage
				.setToolTipText("<html>If selected, a separate title page will be created. By<br>"
						+ "default the title is written on the first page.</html>");
		checkNameIfAvailalbe = new JCheckBox(
				"<html>Set name<br>in equations</html>", false);
		checkNameIfAvailalbe
				.setToolTipText("<html>If selected, the names of SBML elements are displayed <br>"
						+ "instead of their identifiers. This can only be done if<br>"
						+ "the element has a name.</html>");
		checkIncludeMIRIAM = new JCheckBox("<html>MIRIAM annotations</html>",
				false);
		checkIncludeMIRIAM
				.setToolTipText("<html>If selected, the MIRIAM.xml file is parsed to provide<br>"
						+ "links to the resources for each annotated element.</html>");
		checkOneTableForReactionParticipants = new JCheckBox(
				"<html>Arrange reaction participants in one table per reaction</html>",
				false);
		checkOneTableForReactionParticipants
				.setToolTipText("<html>If selected, all reactants, modifiers and producst of a<br>"
						+ "reaction are presented in one table. By default, extra<br>"
						+ "tables are created for each of these elements.</html>");
		checkShowSBMLerrors = new JCheckBox("Check SBML consistency", false);
		checkShowSBMLerrors
				.setToolTipText("<html>If selected, the given SBML file is validated using libSBML's <br>"
						+ "SBML validator. The results are written to the appendix of<br>"
						+ "the report file.</html>");
		String[] paperSizes = new String[43];
		paperSizes[0] = "letter";
		paperSizes[1] = "legal";
		paperSizes[2] = "executive";
		int j = 0;
		String[] prefix = new String[] { "a", "b", "c", "d" };
		for (int i = 0; i < prefix.length * 10; i++) {
			paperSizes[3 + i] = prefix[i / 10] + Integer.toString(j);
			j = (i % 10 == 9) ? 0 : j + 1;
		}
		optionPaperSize = new JComboBox(paperSizes);
		optionFontSize = new JComboBox(new Short[] { Short.valueOf((short) 8),
				Short.valueOf((short) 9), Short.valueOf((short) 10),
				Short.valueOf((short) 11), Short.valueOf((short) 12),
				Short.valueOf((short) 14), Short.valueOf((short) 17) });
		optionPaperSize.setSelectedIndex(7);
		optionPaperSize
				.setToolTipText("<html>This option allows you to choose the paper size. Besides the<br>"
						+ "European formats DIN A0..A9, B0..B9,..., D0..D9 the US sizes<br>"
						+ "letter, legal and executive are available.</html>");
		optionFontSize.setSelectedIndex(3);
		optionFontSize
				.setToolTipText("<html>This allows you to select the size of the standard<br>"
						+ "text font. Headings appear with a larger font.</html>");
		sbmlFileField = new JTextField(15);
		sbmlFileField.setEditable(false);
		sbmlFileField
				.setToolTipText("<html>The SBML source file to be converted to LaTeX.</html>");
		sbmlFileField.addPropertyChangeListener(this);
		teXFileField = new JTextField(15);
		teXFileField.setEditable(false);
		teXFileField
				.setToolTipText("<html>Path of the file to which the output will be written.</html>");
		teXFileField.addPropertyChangeListener(this);
		optionFontText = new JComboBox(new String[] { "chancery", "charter",
				"cmr", "palatino", "times", "utopia" });
		optionFontText.setSelectedIndex(3);
		optionFontText
				.setToolTipText("<html>Select the font to be used for standard continuos text</html>");
		optionFontHeadings = new JComboBox(new String[] { "avant", "cmss",
				"helvetica" });
		optionFontHeadings.setSelectedIndex(2);
		optionFontHeadings
				.setToolTipText("<html>Select the font to be used for all headlines.</html>");
		optionFontTypewriter = new JComboBox(new String[] { "cmt", "courier" });
		optionFontTypewriter.setSelectedIndex(0);
		optionFontTypewriter
				.setToolTipText("<html>Select a typewriter font that can be used for identifiers if<br>"
						+ "option 'identifiers in typewriter font' is selected. URLs and<br>"
						+ "other resources are also marked with this font.</html>");

		GridBagLayout gbl = new GridBagLayout();
		JPanel panel = new JPanel(gbl);
		panel.setBorder(BorderFactory
				.createTitledBorder(" Select SBML and TeX files "));
		LayoutHelper.addComponent(panel, gbl, new JLabel("SBML input file "),
				0, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(panel, gbl, sbmlFileField, 1, 0, 1, 1, 1, 0);
		openButton = new JButton("Open", UIManager
				.getIcon("FileView.directoryIcon"));
		openButton.setName("browse sbml");
		openButton.setToolTipText("Click here to choose a file.");
		openButton.addActionListener(this);
		LayoutHelper.addComponent(panel, gbl, openButton, 2, 0, 1, 1, 0, 0);
		LayoutHelper.addComponent(panel, gbl, new JLabel("LaTeX output file "),
				0, 1, 1, 1, 1, 0);
		LayoutHelper.addComponent(panel, gbl, teXFileField, 1, 1, 1, 1, 0, 0);
		saveButton = new JButton("Save", UIManager
				.getIcon("FileView.floppyDriveIcon"));
		saveButton.setName("browse tex");
		saveButton.setToolTipText("Click here to choose a file.");
		saveButton.setEnabled(false);
		saveButton.addActionListener(this);
		LayoutHelper.addComponent(panel, gbl, saveButton, 2, 1, 1, 1, 0, 0);
		add(panel);

		gbl = new GridBagLayout();
		panel = new JPanel(gbl);
		panel.setBorder(BorderFactory.createTitledBorder(" Report Options "));
		LayoutHelper.addComponent(panel, gbl, checkIncludeMIRIAM, 0, 0, 1, 1,
				0, 0);
		LayoutHelper.addComponent(panel, gbl, checkShowSBMLerrors, 1, 0, 1, 1,
				1, 0);
		LayoutHelper.addComponent(panel, gbl, checkImplicitUnits, 0, 1, 1, 1,
				1, 0);
		add(panel);

		gbl = new GridBagLayout();
		panel = new JPanel(gbl);
		panel.setBorder(BorderFactory.createTitledBorder(" Layout Options "));
		LayoutHelper.addComponent(panel, gbl, new JLabel("Paper size "), 0, 0,
				1, 1, 1, 0);
		LayoutHelper
				.addComponent(panel, gbl, optionPaperSize, 1, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(panel, gbl,
				new JLabel("Main text font size"), 0, 2, 1, 1, 0, 0);
		LayoutHelper.addComponent(panel, gbl, optionFontSize, 1, 2, 1, 1, 1, 0);
		LayoutHelper.addComponent(panel, gbl, new JLabel("Main text font"), 0,
				3, 1, 1, 0, 0);
		LayoutHelper.addComponent(panel, gbl, optionFontText, 1, 3, 1, 1, 0, 0);
		LayoutHelper.addComponent(panel, gbl, new JLabel("Headings font"), 0,
				4, 1, 1, 0, 0);
		LayoutHelper.addComponent(panel, gbl, optionFontHeadings, 1, 4, 1, 1,
				0, 0);
		LayoutHelper.addComponent(panel, gbl, new JLabel("Typewriter font"), 0,
				5, 1, 1, 0, 0);
		LayoutHelper.addComponent(panel, gbl, optionFontTypewriter, 1, 5, 1, 1,
				0, 0);
		// Check boxes
		LayoutHelper.addComponent(panel, gbl, checkTitlepage, 0, 6, 1, 1, 1, 0);
		LayoutHelper.addComponent(panel, gbl, checkLandscape, 1, 6, 1, 1, 1, 0);
		LayoutHelper
				.addComponent(panel, gbl, checkTypeWriter, 0, 7, 1, 1, 1, 0);
		LayoutHelper.addComponent(panel, gbl, checkNameIfAvailalbe, 1, 7, 1, 1,
				1, 0);
		LayoutHelper.addComponent(panel, gbl,
				checkOneTableForReactionParticipants, 0, 8, 2, 1, 1, 0);
		add(panel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			String name = ((JButton) e.getSource()).getName();
			JFileChooser chooser = new JFileChooser();
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			File dir = new File(System.getProperty("user.home"));
			if (name.equals("browse sbml")) {
				if (sbmlFileField.getText().length() > 0) {
					File f = new File(sbmlFileField.getText());
					if (f.exists())
						dir = f.getParentFile();
				}
				chooser.setCurrentDirectory(dir);
				chooser.setFileFilter(new FileNameExtensionFilter("SBML files",
						"xml", "sbml"));
				if (chooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
					sbmlFileField.setText(chooser.getSelectedFile()
							.getAbsolutePath());
					sbmlFileField.firePropertyChange("", false, true);
				}
			} else if (name.equals("browse tex")) {
				if (teXFileField.getText().length() > 0) {
					File f = new File(sbmlFileField.getText());
					if (f.exists())
						dir = f.getParentFile();
				}
				chooser.setCurrentDirectory(dir);
				chooser.setFileFilter(new FileNameExtensionFilter(
						"LaTeX files", "tex", "latex"));
				if (chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION)
					teXFileField.setText(chooser.getSelectedFile()
							.getAbsolutePath());
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public String getFontHeadings() {
		return optionFontHeadings.getSelectedItem().toString();
	}

	/**
	 * @return the fontSize
	 */
	public short getFontSize() {
		return Short.parseShort(optionFontSize.getSelectedItem().toString());
	}

	/**
	 * 
	 * @return
	 */
	public String getFontText() {
		return optionFontText.getSelectedItem().toString();
	}

	/**
	 * 
	 * @return
	 */
	public String getFontTypewriter() {
		return optionFontTypewriter.getSelectedItem().toString();
	}

	/**
	 * @return the paperSize
	 */
	public String getPaperSize() {
		return optionPaperSize.getSelectedItem().toString();
	}

	/**
	 * 
	 * @return
	 */
	public boolean getPrintNameIfAvailable() {
		return checkNameIfAvailalbe.isSelected();
	}

	/**
	 * 
	 * @return
	 */
	public String getSBMLFile() {
		return sbmlFileField.getText();
	}

	/**
	 * 
	 * @return
	 */
	public String getTeXFile() {
		return teXFileField.getText();
	}

	/**
	 * 
	 * @return
	 */
	public boolean getTitlePage() {
		return checkTitlepage.isSelected();
	}

	/**
	 * @return the landscape
	 */
	public boolean isLandscape() {
		return checkLandscape.isSelected();
	}

	/**
	 * 
	 * @return If true SBML errors should be made explicit
	 */
	public boolean isSetCheckConsistency() {
		return checkShowSBMLerrors.isSelected();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetIncludeMIRIAM() {
		return checkIncludeMIRIAM.isSelected();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetOneTableForReactionParticipants() {
		return checkOneTableForReactionParticipants.isSelected();
	}

	/**
	 * @return the addMissingUnitDeclarations
	 */
	public boolean isShowImplicitUnitDeclarations() {
		return checkImplicitUnits.isSelected();
	}

	/**
	 * @return the typeWriter
	 */
	public boolean isTypeWriter() {
		return checkTypeWriter.isSelected();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof JTextField) {
			JTextField tf = (JTextField) evt.getSource();
			if (tf.equals(teXFileField) && teXFileField.getText().length() > 0) {
				saveButton.setEnabled(true);
				saveButton.setText("Change");
			} else if (tf.equals(sbmlFileField)) {
				String texFile = sbmlFileField.getText();
				if (texFile.length() > 0) {
					texFile = texFile.substring(0, Math.min(texFile.length(),
							texFile.lastIndexOf('.')));
					teXFileField.setText(texFile + ".tex");
					teXFileField.firePropertyChange("", false, true);
				}
			}
		}
	}

	/**
	 * Allows to choose if SBML errors should be made explicit.
	 * 
	 * @param doit
	 */
	public void setCheckConsistency(boolean doit) {
		checkShowSBMLerrors.setSelected(doit);
	}

	/**
	 * 
	 * @param fontName
	 */
	public void setFontHeadings(String fontName) {
		optionFontHeadings.setSelectedItem(fontName.toLowerCase());
	}

	/**
	 * 
	 * @param fontSize
	 */
	public void setFontSize(short fontSize) {
		boolean found = false;
		for (int i = 0; i < optionFontSize.getItemCount(); i++) {
			if (fontSize == Short.parseShort(optionFontSize.getItemAt(i)
					.toString())) {
				optionFontSize.setSelectedIndex(i);
				found = true;
			}
		}
		if (!found)
			JOptionPane.showMessageDialog(this, "<html><body>Font size "
					+ fontSize
					+ " is not a valid option.<br>Using default value "
					+ Short.parseShort(optionFontSize.getSelectedItem()
							.toString()) + ".</body></html>", "Warning",
					JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * 
	 * @param fontName
	 */
	public void setFontText(String fontName) {
		optionFontText.setSelectedItem(fontName.toLowerCase());
	}

	/**
	 * 
	 * @param fontName
	 */
	public void setFontTypewriter(String fontName) {
		optionFontTypewriter.setSelectedItem(fontName.toLowerCase());
	}

	/**
	 * 
	 * @param include
	 */
	public void setIncludeMIRIAM(boolean include) {
		checkIncludeMIRIAM.setSelected(include);
	}

	/**
	 * 
	 * @param landscape
	 */
	public void setLandscape(boolean landscape) {
		checkLandscape.setSelected(landscape);
	}

	/**
	 * 
	 * @param oneTableForReactionParticipants
	 */
	public void setOneTableForReactionParticipants(
			boolean oneTableForReactionParticipants) {
		checkOneTableForReactionParticipants
				.setSelected(oneTableForReactionParticipants);
	}

	/**
	 * 
	 * @param paperSize
	 */
	public void setPaperSize(String paperSize) {
		boolean found = false;
		for (int i = 0; i < optionPaperSize.getItemCount(); i++) {
			if (paperSize.equals(optionPaperSize.getItemAt(i).toString())) {
				optionPaperSize.setSelectedIndex(i);
				found = true;
			}
		}
		if (!found)
			JOptionPane.showMessageDialog(this, "<html><body>Paper size "
					+ paperSize
					+ " is not a valid option.<br>Using default value "
					+ optionPaperSize.getSelectedItem() + ".</body></html>",
					"Warning", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * 
	 * @param nameIfAvailable
	 */
	public void setPrintNameIfAvailable(boolean nameIfAvailable) {
		checkNameIfAvailalbe.setSelected(nameIfAvailable);
	}

	/**
	 * 
	 * @param f
	 */
	public void setSBMLFile(File f) {
		sbmlFileField.setText(f.getAbsolutePath());
	}

	/**
	 * 
	 * @param show
	 */
	public void setShowImplicitUnitDeclarations(boolean show) {
		checkImplicitUnits.setSelected(show);
	}

	/**
	 * 
	 * @param f
	 */
	public void setTeXFile(File f) {
		teXFileField.setText(f.getAbsolutePath());
	}

	/**
	 * 
	 * @param titlePage
	 */
	public void setTitlePage(boolean titlePage) {
		checkTitlepage.setSelected(titlePage);
	}

	/**
	 * 
	 * @param typeWriter
	 */
	public void setTypeWriter(boolean typeWriter) {
		checkTypeWriter.setSelected(typeWriter);
	}
}
