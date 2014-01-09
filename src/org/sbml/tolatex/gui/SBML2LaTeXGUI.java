/*
 * $Id: SBML2LaTeXGUI.java 24.05.2012 13:50:12 draeger$
 * $URL: SBML2LaTeXGUI.java$
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
package org.sbml.tolatex.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.tolatex.SBML2LaTeX;
import org.sbml.tolatex.SBML2LaTeXView;
import org.sbml.tolatex.io.LaTeXOptionsIO;

import de.zbit.gui.GUITools;
import de.zbit.gui.JBrowserPane;
import de.zbit.io.OpenedFile;
import de.zbit.sbml.gui.SBMLReadingTask;
import de.zbit.util.prefs.SBPreferences;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.0
 * @version $Rev$
 */
public class SBML2LaTeXGUI implements SBML2LaTeXView, PropertyChangeListener {
	
	/**
	 * 
	 */
	private JFrame f;
	
	/**
	 * 
	 * @param sbmlInputFile
	 */
	public SBML2LaTeXGUI(File sbmlInputFile) {
		super();
		LaTeXExportDialog.initImages();
		
		f = new JFrame("SBML2LaTeX");
		List<Image> listOfIcons = new ArrayList<Image>(2);
		listOfIcons.add(((ImageIcon) UIManager.getIcon("ICON_LATEX_16")).getImage());
		listOfIcons.add(((ImageIcon) UIManager.getIcon("ICON_LATEX_64")).getImage());
		f.setIconImages(listOfIcons);
		f.pack();
		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		f.setLocationRelativeTo(null);
		
		if (LaTeXExportDialog.showDialog(f)) {
			SBPreferences prefs = SBPreferences.getPreferencesFor(LaTeXOptionsIO.class);
			sbmlInputFile = prefs.getFile(LaTeXOptionsIO.SBML_INPUT_FILE);
			try {
				SBMLReadingTask reader = new SBMLReadingTask(
					sbmlInputFile,
					null,
					EventHandler.create(PropertyChangeListener.class, this, "convert", "newValue"));
				reader.execute();
			} catch (Throwable exc) {
				GUITools.showErrorMessage(null, exc);
				System.exit(1);
			}
		} else {
			System.exit(0);
		}
	}

	/**
	 * 
	 * @param sbase
	 */
	@SuppressWarnings("unchecked")
	public void convert(Object sbase) {
		if (sbase == null) {
			// reading must have been canceled.
			System.exit(0);
		}
		if (sbase instanceof OpenedFile<?>) {
			sbase = ((OpenedFile<SBMLDocument>) sbase).getDocument();
		}
		if (sbase instanceof SBase) {
			SBPreferences prefsIO = SBPreferences.getPreferencesFor(LaTeXOptionsIO.class);
			SBML2LaTeXworker worker = new SBML2LaTeXworker((SBase) sbase, prefsIO.getFile(LaTeXOptionsIO.REPORT_OUTPUT_FILE), this);
			worker.addPropertyChangeListener(this);
			worker.execute();
		}
	}

	/* (non-Javadoc)
	 * @see org.sbml.tolatex.SBML2LaTeXView#display(java.io.File)
	 */
	@Override
	public void display(File resultFile) throws IOException {
		if (resultFile != null) {
			// Open standard file viewer
			Desktop.getDesktop().open(resultFile);
			System.exit(0);
		}
	}

	/* (non-Javadoc)
	 * @see org.sbml.tolatex.SBML2LaTeXView#displayLaTeXOutput(java.lang.Process, boolean)
	 */
	@Override
	public void displayLaTeXOutput(Process process, boolean firstLaTeXrun) {
		GUITools.showProcessOutputInTextArea(process, f, firstLaTeXrun);
	}

	/* (non-Javadoc)
	 * @see org.sbml.tolatex.SBML2LaTeXView#displayLimitations()
	 */
	@Override
	public void displayLimitations() {
		Runnable displayLimitations = new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				JBrowserPane pane = new JBrowserPane(
					SBML2LaTeX.class.getResource("gui/html/limitations.html"));
				pane.setPreferredSize(new Dimension(480, 240));
				// Show this message, but continue the execution of pdftex
				JOptionPane.showMessageDialog(f, new JScrollPane(pane), "Limitations",
					JOptionPane.INFORMATION_MESSAGE);
			}
		};
		GUITools.processOnSwingEventThread(displayLimitations);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	//@Override
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(SBML2LaTeXworker.ERROR_CODE)) {
			System.exit(1);
		}
	}
	
}
