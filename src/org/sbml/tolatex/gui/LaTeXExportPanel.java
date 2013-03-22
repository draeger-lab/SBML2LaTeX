/*
 * $Id: LaTeXExportPanel.java 60 2011-03-07 17:20:39Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/tags/version0.9.8/src/org/sbml/tolatex/gui/LaTeXExportPanel.java $
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates 
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2008-2011 by the University of Tuebingen, Germany.
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
package org.sbml.tolatex.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.sbml.jsbml.SBase;
import org.sbml.tolatex.io.LaTeXOptionsIO;

import de.zbit.gui.GUIOptions;
import de.zbit.gui.LayoutHelper;
import de.zbit.gui.prefs.FileSelector;
import de.zbit.gui.prefs.LaTeXPrefPanel;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.SBPreferences;

/**
 * This {@link JPanel} provides a user interface to select the SBML input file
 * (if necessary, i.e., this element can also be initialized with a given
 * {@link SBase} indicating that no input file is necessary) and the destination
 * path for the TeX output file plus the full configuration of SBML2LaTeX as
 * provided by the {@link LaTeXPrefPanel}. In this way, this
 * {@link LaTeXExportPanel} fully qualifies all necessary settings to invoke
 * SBML2LaTeX.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-10
 * @version $Rev: 60 $
 */
public class LaTeXExportPanel extends JPanel {

    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = -5356369953204413478L;

    /**
     * Selectors for the SBML input and TeX output file.
     */
    private FileSelector fsSBML, fsTeX;

    /**
     * Preferences for GUI and input/output.
     */
    private SBPreferences prefsIO, prefsGUI;
    /**
     * The configuration of SBML2LaTeX.
     */
    private LaTeXPrefPanel prefsPanelLaTeX;

    /**
     * Creates a new {@link LaTeXExportPanel} with a {@link FileSelector} for
     * the
     * SBML input file.
     * 
     * @throws IOException
     */
    public LaTeXExportPanel() throws IOException {
	this(null);
    }

    /**
     * Creates a new {@link LaTeXExportPanel} with a {@link FileSelector} for
     * the
     * SBML input file only if the given {@link SBase} is null.
     * 
     * @param sbase
     *        might be null to indicate that a file has to be selected by the
     *        user
     *        for reading SBML.
     * @throws IOException
     */
    public LaTeXExportPanel(SBase sbase) throws IOException {
	this(sbase, null);
    }

    /**
     * @param sbase
     * @param targetFile
     * @throws IOException
     */
    public LaTeXExportPanel(SBase sbase, File targetFile) throws IOException {
	super();

	prefsIO = SBPreferences.getPreferencesFor(LaTeXOptionsIO.class);
	prefsGUI = SBPreferences.getPreferencesFor(GUIOptions.class);

	File path = new File(prefsIO.get(LaTeXOptionsIO.SBML_INPUT_FILE));
	if (!(path.exists() || path.canRead() || path.isFile())) {
	    path = new File(prefsGUI.get(GUIOptions.OPEN_DIR));
	    prefsIO.remove(LaTeXOptionsIO.SBML_INPUT_FILE);
	}

	fsSBML = new FileSelector(FileSelector.Type.OPEN, path
		.getAbsolutePath(), (FileFilter) LaTeXOptionsIO.SBML_INPUT_FILE
		.getRange().getConstraints());

	path = targetFile == null ? new File(prefsIO
		.get(LaTeXOptionsIO.REPORT_OUTPUT_FILE)) : targetFile;
	if (!(path.exists() || path.canWrite() || path.isFile())) {
	    path = new File(prefsIO.get(LaTeXOptionsIO.LATEX_DIR));
	    prefsIO.remove(LaTeXOptionsIO.REPORT_OUTPUT_FILE);
	}

	fsTeX = new FileSelector(FileSelector.Type.SAVE,
	    path.getAbsolutePath(),
	    (FileFilter) LaTeXOptionsIO.REPORT_OUTPUT_FILE.getRange()
		    .getConstraints());

	JPanel panel = new JPanel();
	LayoutHelper helper = new LayoutHelper(this);
	if ((sbase == null) || (targetFile == null)) {
	    LayoutHelper lh = new LayoutHelper(panel);
	    if (sbase == null) {
		FileSelector.addSelectorsToLayout(lh, fsSBML);
		fsSBML.setLabelText(LaTeXOptionsIO.SBML_INPUT_FILE
			.formatOptionName());
		fsSBML.setToolTipText(StringUtil.toHTML(
		    LaTeXOptionsIO.SBML_INPUT_FILE.getToolTip(), 60));
	    }
	    if (targetFile == null) {
		FileSelector.addSelectorsToLayout(lh, fsTeX);
		fsTeX.setLabelText(LaTeXOptionsIO.REPORT_OUTPUT_FILE
			.formatOptionName());
		fsTeX.setToolTipText(StringUtil.toHTML(
		    LaTeXOptionsIO.REPORT_OUTPUT_FILE.getToolTip(), 60));
	    }

	    String title = sbase == null ? "Choose the TeX output file"
		    : LaTeXOptionsIO.INPUT_AND_OUTPUT_FILES.getName();

	    panel
		    .setBorder(BorderFactory.createTitledBorder(" " + title
			    + " "));
	    panel.setToolTipText(LaTeXOptionsIO.INPUT_AND_OUTPUT_FILES
		    .getToolTip());

	    helper.add(panel);
	}

	prefsPanelLaTeX = new LaTeXPrefPanel();
	helper.add(prefsPanelLaTeX);
    }

    /**
     * Provides access to the underlying {@link LaTeXPrefPanel}. This can be
     * used,
     * e.g., to call the {@link LaTeXPrefPanel#persist()} method.
     */
    public LaTeXPrefPanel getLaTeXPrefPanel() {
	return prefsPanelLaTeX;
    }

    /**
     * Yields the user-selected SBML {@link File} or throws an
     * {@link IOException} if this is not possible. This method also tries to
     * persistently store the {@link File } in the {@link LaTeXOptionsIO}
     * preferences and the {@link GUIOptions#OPEN_DIR}.
     * 
     * @return
     * @throws IOException
     * @throws BackingStoreException
     */
    public File getSelectedSBMLFile() throws IOException, BackingStoreException {
	File file = fsSBML.getSelectedFile();
	if (file == null) {
	    throw new FileNotFoundException(
		"No SBML file has been selected as input.");
	}
	prefsIO.put(LaTeXOptionsIO.SBML_INPUT_FILE, file);
	prefsIO.flush();
	prefsGUI.put(GUIOptions.OPEN_DIR, file.getParent());
	prefsGUI.flush();
	return file;
    }

    /**
     * Yields the user-selected TeX {@link File} or throws an
     * {@link IOException} if this is not possible. This method also tries to
     * persistently store the {@link File} in the {@link LaTeXOptionsIO}
     * preferences and the {@link LaTeXOptionsIO#LATEX_DIR}.
     * 
     * @return
     * @throws IOException
     * @throws BackingStoreException
     */
    public File getSelectedOutputFile() throws IOException,
	BackingStoreException {
	File file = fsTeX.getSelectedFile();
	if (file == null) {
	    throw new FileNotFoundException(
		"No file has been selected as output.");
	}
	prefsIO.put(LaTeXOptionsIO.REPORT_OUTPUT_FILE, file);
	prefsIO.put(LaTeXOptionsIO.LATEX_DIR, file.getParent());
	prefsIO.flush();
	return file;
    }

}
