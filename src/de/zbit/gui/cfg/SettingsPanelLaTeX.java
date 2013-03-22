/*
 * SBMLsqueezer creates rate equations for reactions in SBML files
 * (http://sbml.org).
 * Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.zbit.gui.cfg;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Field;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;

import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.gui.LaTeXExportDialog;

import de.zbit.gui.GUITools;
import de.zbit.gui.ImageTools;
import de.zbit.gui.LayoutHelper;
import de.zbit.io.SBFileFilter;
import de.zbit.util.prefs.SBPreferences;

/**
 * A {@link JPanel} to configure all necessary options to perform a LaTeX export
 * of a model.
 * 
 * @since 1.2
 * @author Hannes Borch
 * @author Andreas Dr&auml;ger
 * @date Jan 2009
 */
public class SettingsPanelLaTeX extends PreferencesPanel implements ActionListener {

    /**
	 * 
	 */
    private static final Short[] fontSizes = new Short[] { 8, 9, 10, 11, 12,
	    14, 17 };
    /**
	 * 
	 */
    private static String[] paperSizes;

    /**
     * Generated serial version id.
     */
    private static final long serialVersionUID = 5056629254462180004L;

    static {
	GUITools.initLaF("SBML2LaTeX");
	ImageTools.initImages(LaTeXExportDialog.class.getResource("img"));
	paperSizes = new String[43];
	paperSizes[0] = "letter";
	paperSizes[1] = "legal";
	paperSizes[2] = "executive";
	char[] prefixes = new char[] { 'a', 'b', 'c', 'd' };
	for (int i = 0; i < prefixes.length; i++) {
	    for (int j = 0; j < 10; j++) {
		paperSizes[3 + i * 10 + j] = prefixes[i] + String.valueOf(j);
	    }
	}
    }

    private final boolean browse;
    private JTextField fileField;
    private JCheckBox jCheckBoxIDsInTWFont, jCheckBoxLandscape,
	    jCheckBoxNameInEquations, jCheckBoxTitlePage;
    private JComboBox jComboBoxFontSize, jComboBoxPaperSize;

    /**
     * @param properties
     * @throws IOException
     */
    public SettingsPanelLaTeX() throws IOException {
	this(false);
    }

    /**
     * @param properties
     *        The settings for this panel
     * @param browse
     *        if true a browse button will appear that allows to select a
     *        LaTeX file. If false this button will only allow to select a
     *        directory for LaTeX files.
     * @throws IOException
     */
    public SettingsPanelLaTeX(boolean browse) throws IOException {
	super();
	this.browse = browse;
	setProperties(properties);
    }

    /*
     * (non-Javadoc)
     * @see de.zbit.gui.cfg.SettingsPanel#accepts(java.lang.Object)
     */
    @Override
    public boolean accepts(Object key) {
	for (Field field : LaTeXOptions.class.getFields()) {
	    if (field.getName().equals(key)) {
		return true;
	    }
	}
	return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() instanceof JButton) {
	    JFileChooser chooser = GUITools.createJFileChooser(fileField
		    .getText(), false, false, JFileChooser.FILES_ONLY);
	    boolean approve = false;
	    if (browse) {
		chooser.addChoosableFileFilter(SBFileFilter.TeX_FILE_FILTER);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		approve = chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION;
	    } else {
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		approve = chooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION;
	    }
	    if (approve) {
		String path = chooser.getSelectedFile().getAbsolutePath();
		fileField.setText(path);
		if (!browse) {
		    properties.put(LaTeXOptions.LATEX_DIR, path);
		}
		super.stateChanged(new ChangeEvent(e));
	    }
	}
    }

    /**
     * @return
     */
    public String getTeXFile() {
	return fileField.getText();
    }

    /*
     * (non-Javadoc)
     * @see org.sbml.squeezer.gui.SettingsPanel#getTitle()
     */
    @Override
    public String getTitle() {
	return "LaTeX output settings";
    }

    /*
     * (non-Javadoc)
     * @see org.sbml.squeezer.gui.SettingsPanel#init()
     */
    @Override
    public void init() {
	setLayout(new BorderLayout());
	JPanel filePanel = new JPanel();
	LayoutHelper lh = new LayoutHelper(filePanel);
	fileField = new JTextField(15);
	fileField.setText(properties.get(LaTeXOptions.LATEX_DIR.toString())
		.toString());
	fileField.setName(LaTeXOptions.LATEX_DIR.toString());
	fileField.setEditable(false);
	lh.add(new JPanel(), 0, 0, 4, 1, 0, 0);
	lh.add(new JPanel(), 0, 1, 1, 1, 0, 0);
	lh.add(fileField, 1, 1, 1, 1, .7, .8);
	lh.add(new JPanel(), 2, 1, 1, 1, 0, 0);
	JButton jButtonTeXFile;
	if (browse) {
	    filePanel
		    .setBorder(BorderFactory
			    .createTitledBorder(" Select a LaTeX file for the output "));
	    jButtonTeXFile = new JButton("Browse", UIManager
		    .getIcon("ICON_SAVE"));
	} else {
	    filePanel
		    .setBorder(BorderFactory
			    .createTitledBorder(" Select the standard directory for LaTeX files "));
	    jButtonTeXFile = new JButton("Browse", UIManager
		    .getIcon("ICON_OPEN"));
	}
	jButtonTeXFile.addActionListener(this);
	lh.add(jButtonTeXFile, 3, 1, 1, 1, 0, .8);
	lh.add(new JPanel(), 4, 1, 1, 1, 0, 0);
	lh.add(new JPanel(), 0, 2, 4, 1, 0, 0);
	add(filePanel, BorderLayout.NORTH);

	int i = 0;
	while (i < paperSizes.length
		&& !paperSizes[i].equals(properties.get(
		    LaTeXOptions.LATEX_PAPER_SIZE.toString()).toString())) {
	    i++;
	}
	jComboBoxPaperSize = new JComboBox(paperSizes);
	jComboBoxPaperSize.setSelectedIndex(i);
	i = 0;
	while (i < fontSizes.length
		&& fontSizes[i].shortValue() != Short.parseShort(properties
			.get(LaTeXOptions.LATEX_FONT_SIZE.toString())
			.toString())) {
	    i++;
	}
	jComboBoxFontSize = new JComboBox(fontSizes);
	jComboBoxFontSize.setSelectedIndex(i);
	int row = -1;
	JPanel formatPanel = new JPanel();
	formatPanel.setBorder(BorderFactory
		.createTitledBorder(" Format options "));
	lh = new LayoutHelper(formatPanel);
	lh.add(new JPanel(), 0, ++row, 5, 1, 0, 0);
	lh.add(new JPanel(), 0, ++row, 1, 1, 0, 0);
	lh.add(new JLabel("Paper size"), 1, ++row, 1, 1, 0, 0);
	lh.add(new JPanel(), 2, row, 1, 1, 0, 0);
	lh.add(jComboBoxPaperSize, 3, row, 1, 1, 1, 0);
	lh.add(new JPanel(), 5, row, 1, 1, 0, 0);
	lh.add(new JPanel(), 0, ++row, 5, 1, 0, 0);
	lh.add(new JLabel("Font size"), 1, ++row, 1, 1, 0, 0);
	lh.add(jComboBoxFontSize, 3, row, 1, 1, 1, 0);
	lh.add(new JPanel(), 0, ++row, 5, 1, 0, 0);

	jCheckBoxIDsInTWFont = new JCheckBox("IDs in typewriter font", (Boolean
		.parseBoolean(properties.get(
		    LaTeXOptions.LATEX_IDS_IN_TYPEWRITER_FONT.toString())
			.toString())));
	jCheckBoxLandscape = new JCheckBox("Landscape", (Boolean
		.parseBoolean(properties.get(
		    LaTeXOptions.LATEX_LANDSCAPE.toString()).toString())));
	jCheckBoxTitlePage = new JCheckBox("Create title page", (Boolean
		.parseBoolean(properties.get(
		    LaTeXOptions.LATEX_TITLE_PAGE.toString()).toString())));
	jCheckBoxNameInEquations = new JCheckBox("Set name in equations",
	    (Boolean.parseBoolean(properties.get(
		LaTeXOptions.LATEX_NAMES_IN_EQUATIONS.toString()).toString())));
	lh.add(jCheckBoxIDsInTWFont, 1, ++row, 2, 1, 1, 1);
	if (!browse) {
	    JPanel panel = new JPanel(new BorderLayout());
	    JPanel p = new JPanel(new BorderLayout());
	    p.add(new JLabel(UIManager.getIcon("ICON_LATEX_SMALL")),
		BorderLayout.SOUTH);
	    panel.add(p, BorderLayout.EAST);
	    lh.add(panel, 3, row, 2, 4, 1, 1);
	}
	lh.add(jCheckBoxLandscape, 1, ++row, 2, 1, 1, 1);
	lh.add(jCheckBoxTitlePage, 1, ++row, 2, 1, 1, 1);
	lh.add(jCheckBoxNameInEquations, 1, ++row, 2, 1, 1, 1);
	lh.add(new JPanel(), 0, ++row, 5, 1, 0, 0);
	add(formatPanel, BorderLayout.CENTER);

	jComboBoxPaperSize.addItemListener(this);
	jComboBoxPaperSize.setName(LaTeXOptions.LATEX_PAPER_SIZE.toString());
	jComboBoxFontSize.addItemListener(this);
	jComboBoxFontSize.setName(LaTeXOptions.LATEX_FONT_SIZE.toString());
	jCheckBoxIDsInTWFont.addItemListener(this);
	jCheckBoxIDsInTWFont.setName(LaTeXOptions.LATEX_IDS_IN_TYPEWRITER_FONT
		.toString());
	jCheckBoxLandscape.addItemListener(this);
	jCheckBoxLandscape.setName(LaTeXOptions.LATEX_LANDSCAPE.toString());
	jCheckBoxTitlePage.addItemListener(this);
	jCheckBoxTitlePage.setName(LaTeXOptions.LATEX_TITLE_PAGE.toString());
	jCheckBoxNameInEquations.addItemListener(this);
	jCheckBoxNameInEquations.setName(LaTeXOptions.LATEX_NAMES_IN_EQUATIONS
		.toString());
    }

    /*
     * (non-Javadoc)
     * @see de.zbit.gui.cfg.SettingsPanel#loadPreferences()
     */
    @Override
    protected SBPreferences loadPreferences() throws IOException {
	return SBPreferences.getPreferencesFor(LaTeXOptions.class,
	    LaTeXOptions.CONFIG_FILE_LOCATION);
    }
}
