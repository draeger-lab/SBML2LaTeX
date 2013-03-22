/*
 * SBML2LaTeX converts SBML files (http://sbml.org) into LaTeX files.
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
package org.sbml.tolatex.io;

import java.io.BufferedWriter;
import java.io.IOException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;

/**
 * @author wouamba
 * @author Andreas Dr&auml;ger
 */
public interface SBMLReporter {

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
