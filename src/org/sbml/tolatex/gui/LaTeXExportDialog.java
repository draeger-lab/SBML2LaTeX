/*
 * $Id: LaTeXExportDialog.java 60 2011-03-07 17:20:39Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/tags/version0.9.8/src/org/sbml/tolatex/gui/LaTeXExportDialog.java $
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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SBase;
import org.sbml.tolatex.LaTeXOptions;

import de.zbit.gui.GUITools;
import de.zbit.gui.prefs.LaTeXPrefPanel;
import de.zbit.io.SBFileFilter;
import de.zbit.util.StringUtil;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-10-20
 * @version $Rev: 60 $
 */
public class LaTeXExportDialog extends JDialog {
	
	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = -408657221271532557L;
	
	/**
	 * 
	 */
	public static void initGUI() {
		GUITools.initLaF("SBML2LaTeX");
		// ImageTools.initImages(LaTeXExportDialog.class.getResource("img"));
		initImages();
	}
	
	/**
	 * Loads the required icons for SBML2LaTeX into the {@link UIManager}.
	 */
	public static void initImages() {
		String iconPaths[] = { "ICON_LATEX_16.png", "ICON_LATEX_64.png" };
		for (String path : iconPaths) {
			UIManager.put(path.substring(0, path.lastIndexOf('.')), new ImageIcon(
				LaTeXExportDialog.class.getResource("img/" + path)));
		}
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		new LaTeXExportDialog();
		System.exit(0);
	}
	
	/**
     * 
     */
	public LaTeXExportDialog() {
		this((JFrame) null, null);
	}
	
	/**
	 * @param owner
	 */
	public LaTeXExportDialog(Dialog owner) {
		this(owner, null);
	}
	
	/**
	 * @param owner
	 * @param properties
	 * @param sbase
	 */
	public LaTeXExportDialog(Dialog owner, SBase sbase) {
		super(owner, "SBML2LaTeX", true);
	}
	
	/**
	 * @param owner
	 */
	public LaTeXExportDialog(Frame owner) {
		this(owner, null);
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
	}
	
	/**
	 * @return true if user clicked on OK button, false otherwise.
	 * @throws IOException
	 */
	public boolean showExportDialog() throws IOException {
		return showExportDialog(null);
	}
	
	/**
     * 
     */
	private LaTeXExportPanel exportPanel;
	
	/**
	 * @param sbase
	 * @return true if user clicked on OK button, false otherwise.
	 * @throws IOException
	 */
	public boolean showExportDialog(SBase sbase) throws IOException {
		return showExportDialog(sbase, null);
	}
	
	/**
	 * @param sbase
	 * @param targetFile
	 * @return
	 * @throws IOException
	 */
	public boolean showExportDialog(SBase sbase, File targetFile)
		throws IOException {
		exportPanel = new LaTeXExportPanel(sbase, targetFile);
		String title = "LaTeX export";
		if (sbase != null) {
			if (sbase instanceof NamedSBase) {
				NamedSBase nsb = (NamedSBase) sbase;
				title += " for " + (nsb.isSetName() ? nsb.getName() : nsb.getId());
			}
		}
		
		return JOptionPane.showConfirmDialog(this, exportPanel, title,
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, UIManager
					.getIcon("ICON_LATEX_64")) == JOptionPane.OK_OPTION;
	}
	
	/**
	 * @param owner
	 * @return
	 */
	public static boolean showDialog(Window owner) {
		return showDialog(owner, null);
	}
	
	/**
	 * @param owner
	 * @param sbase
	 * @return
	 */
	public static boolean showDialog(Window owner, SBase sbase) {
		return showDialog(owner, sbase, null);
	}
	
	/**
	 * @param owner
	 * @param sbase
	 * @param targetFile
	 *        can be null
	 * @return
	 */
	public static boolean showDialog(Window owner, SBase sbase, File targetFile) {
		LaTeXExportDialog dialog;
		if (owner instanceof Frame) {
			dialog = new LaTeXExportDialog((Frame) owner);
		} else if (owner instanceof Dialog) {
			dialog = new LaTeXExportDialog((Dialog) owner);
		} else {
			dialog = new LaTeXExportDialog((Frame) null);
		}
		boolean accept = false;
		try {
			accept = dialog.showExportDialog(sbase, targetFile);
			if (accept) {
				try {
					LaTeXExportPanel expPanel = dialog.exportPanel;
					LaTeXPrefPanel prefPanel = expPanel.getLaTeXPrefPanel();
					prefPanel.persist();
					if (sbase == null) {
						expPanel.getSelectedSBMLFile();
					}
					if (((targetFile != null) && (SBFileFilter.isPDFFile(targetFile)))
							|| SBFileFilter.isPDFFile(dialog.exportPanel
									.getSelectedOutputFile())) {
						File compiler = prefPanel
								.getProperty(LaTeXOptions.LOAD_LATEX_COMPILER);
						if (!compiler.exists() || !compiler.canExecute()) { throw new FileNotFoundException(
							StringUtil.toHTML(
										"It is not possible to create a PDF report without knowing the path to the LaTeX compiler on your system. Please specify where this program is located.",
										60)); }
					}
				} catch (FileNotFoundException exc) {
					GUITools.showErrorMessage(dialog, exc);
					dialog.dispose();
					return showDialog(owner, sbase);
				} catch (BackingStoreException exc) {
					GUITools.showErrorMessage(dialog, exc);
					dialog.dispose();
					return showDialog(owner, sbase);
				}
			}
		} catch (IOException exc) {
			GUITools.showErrorMessage(dialog, exc);
		}
		return accept;
	}
	
	/**
	 * @return
	 */
	public static boolean showDialog() {
		return showDialog(null, null);
	}
	
	/**
	 * 
	 * @param sbase
	 * @return
	 */
	public static boolean showDialog(SBase sbase) {
		return showDialog(null, sbase);
	}
	
}
