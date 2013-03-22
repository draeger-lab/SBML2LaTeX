/*
 * $Id: SBMLReportGenerator.java 60 2011-03-07 17:20:39Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/tags/version0.9.8/src/org/sbml/tolatex/io/SBMLReportGenerator.java $
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
package org.sbml.tolatex.io;

import java.io.BufferedWriter;
import java.io.IOException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;

/**
 * @author Dieudonn&eacute; Wouamba
 * @author Andreas Dr&auml;ger
 * @version $Rev: 60 $
 */
public interface SBMLReportGenerator {

	/**
	 * @param doc
	 * @param buffer
	 * @throws IOException
	 * @throws SBMLException 
	 */
	public void format(SBMLDocument doc, BufferedWriter buffer)
			throws IOException, SBMLException;

	/**
	 * @param model
	 * @param buffer
	 * @throws IOException
	 * @throws SBMLException 
	 */
	public void format(Model model, BufferedWriter buffer) throws IOException, SBMLException;

	/**
	 * @param list
	 * @param name
	 * @param buffer
	 * @param section
	 * @throws IOException
	 * @throws SBMLException 
	 */
	public void format(ListOf<? extends SBase> list, BufferedWriter buffer,
			boolean section) throws IOException, SBMLException;

	/**
	 * Creates a heading for a subsubsection
	 * 
	 * @param title
	 *            The title of the subsubsection
	 * @param numbering
	 *            If true the subsubsection will be numbered otherwise not.
	 * @return
	 */
	public StringBuffer subsubsection(String title, boolean numbering);

	/**
	 * @param title
	 * @param numbering
	 * @return
	 */
	public StringBuffer subsection(String title, boolean numbering);

	/**
	 * @param title
	 * @param numbering
	 * @return
	 */
	public StringBuffer section(String title, boolean numbering);
}
