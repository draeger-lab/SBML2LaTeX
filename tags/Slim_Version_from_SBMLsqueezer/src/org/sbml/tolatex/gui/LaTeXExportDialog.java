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
package org.sbml.tolatex.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.io.LaTeXExport;

import de.zbit.gui.GUITools;
import de.zbit.gui.ImageTools;
import de.zbit.gui.cfg.SettingsPanelLaTeX;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.SBPreferences;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-10-20
 */
public class LaTeXExportDialog extends JDialog {

    static {
	GUITools.initLaF("SBML2LaTeX");
	ImageTools.initImages(LaTeXExportDialog.class.getResource("img"));
    }

    /**
     * Generated serial version identifier
     */
    private static final long serialVersionUID = -408657221271532557L;

    /**
	 * 
	 */
    private static final String NO_FILE_SELECTED_WARNING = StringUtil
	    .toHTML(
		"No appropriate file was selected and therefore no TeX file was created.",
		40);

    /**
     * @param owner
     * @param properties
     * @param sbase
     */
    public LaTeXExportDialog(Dialog owner, SBase sbase) {
	super(owner, "SBML2LaTeX", true);
	try {
	    init(sbase);
	} catch (Exception exc) {
	    GUITools.showErrorMessage(this, exc);
	}
    }

    /**
     * This constructor allows us to store the given model or the given reaction
     * in a text file. This can be a LaTeX or another format.
     * 
     * @param owner
     * @param sbase
     *        allowed are a reaction or a model instance.
     */
    public LaTeXExportDialog(Frame owner, SBase sbase) {
	super(owner, "SBML2LaTeX", true);
	try {
	    init(sbase);
	} catch (Exception exc) {
	    GUITools.showErrorMessage(this, exc);
	}
    }

    /**
     * @param sbase
     * @throws IOException
     */
    private void init(SBase sbase) throws IOException {
	SBPreferences prefs = SBPreferences.getPreferencesFor(
	    LaTeXOptions.class, LaTeXOptions.CONFIG_FILE_LOCATION);
	SettingsPanelLaTeX panel = new SettingsPanelLaTeX(true);
	if (JOptionPane.showConfirmDialog(this, panel, "LaTeX export",
	    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
	    UIManager.getIcon("ICON_LATEX_SMALL")) == JOptionPane.OK_OPTION) {
	    try {
		final File f = new File(panel.getTeXFile());
		if (f.exists() && f.isDirectory()) {
		    JOptionPane.showMessageDialog(this,
			NO_FILE_SELECTED_WARNING, "Warning",
			JOptionPane.WARNING_MESSAGE);
		} else if (!f.exists()
			|| GUITools.overwriteExistingFileDialog(getParent(), f) == JOptionPane.YES_OPTION) {
		    BufferedWriter buffer = new BufferedWriter(
			new FileWriter(f));
		    prefs.putAll(panel.getProperties());
		    LaTeXExport exporter = new LaTeXExport(prefs);
		    if (sbase instanceof SBMLDocument) {
			buffer.write(exporter.toLaTeX(
			    ((SBMLDocument) sbase).getModel()).toString());
		    } else if (sbase instanceof Model) {
			buffer
				.write(exporter.toLaTeX((Model) sbase)
					.toString());
		    } else if (sbase instanceof Reaction) {
			buffer.write(exporter.toLaTeX((Reaction) sbase)
				.toString());
		    }
		    buffer.close();
		    // new Thread(new Runnable() {
		    //
		    // public void run() {
		    // try {
		    // Desktop.getDesktop().open(f);
		    // } catch (IOException e) {
		    // // e.printStackTrace();
		    // }
		    // }
		    // }).start();
		}
		dispose();
	    } catch (Exception exc) {
		GUITools.showErrorMessage(this, exc);
	    }
	}
    }
}
