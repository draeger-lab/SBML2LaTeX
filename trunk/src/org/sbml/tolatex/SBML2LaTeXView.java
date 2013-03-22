/*
 * $Id: SBML2LaTeXView.java 24.05.2012 13:44:24 draeger$
 * $URL: SBML2LaTeXView.java$
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
package org.sbml.tolatex;

import java.io.File;
import java.io.IOException;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.0
 * @version $Rev: 206 $
 */
public interface SBML2LaTeXView {

	/**
	 * 
	 */
	public void displayLimitations();

	/**
	 * 
	 * @param process
	 * @param firstLaTeXrun
	 */
	public void displayLaTeXOutput(Process process, boolean firstLaTeXrun);

	/**
	 * 
	 * @param pdfFile
	 * @throws IOException 
	 */
	public void display(File resultFile) throws IOException;
	
}
