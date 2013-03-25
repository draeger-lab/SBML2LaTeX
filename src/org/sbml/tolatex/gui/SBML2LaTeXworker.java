/*
 * $Id: SBML2LaTeXworker.java 24.05.2012 16:08:12 draeger$
 * $URL: SBML2LaTeXworker.java$
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates 
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2007-2013 by the University of Tuebingen, Germany.
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

import javax.swing.SwingWorker;

import org.sbml.jsbml.SBase;
import org.sbml.tolatex.SBML2LaTeX;
import org.sbml.tolatex.SBML2LaTeXView;

import de.zbit.gui.GUITools;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.0
 * @version $Rev$
 */
public class SBML2LaTeXworker extends SwingWorker<File, Void> {

	private SBase sbase;
	private SBML2LaTeXView view;
	private File outFile;
	public static final String ERROR_CODE = "org.sbml.tolatex.gui.SBML2LaTeXworker.error";

	/**
	 * 
	 */
	public SBML2LaTeXworker(SBase sbase, File outFile, SBML2LaTeXView view) {
		super();
		this.sbase = sbase;
		this.outFile = outFile;
		this.view = view;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected File doInBackground() throws Exception {
		return SBML2LaTeX.convert(sbase, outFile, view);
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		try {
			File result = get();
			view.display(result);
		} catch (Exception exc) {
			GUITools.showErrorMessage(null, exc);
			firePropertyChange(ERROR_CODE , null, null);
		}
	}
	
}
