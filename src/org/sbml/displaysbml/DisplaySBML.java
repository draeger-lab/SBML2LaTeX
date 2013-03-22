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

import java.io.BufferedWriter;
import java.io.IOException;

import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.ListOfEvents;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;

/**
 *
 * @author wouamba
 * @author Andreas Dr&auml;ger <andreas.draeger@uni-tuebingen.de>
 */
public interface DisplaySBML {

	/**
	 *
	 * @param doc
	 * @param buffer
	 * @throws IOException
	 */
	public void format(SBMLDocument doc, BufferedWriter buffer) throws IOException;

	/**
	 *
	 * @param model
	 * @param buffer
	 * @throws IOException
	 */
	public void format(Model model, BufferedWriter buffer) throws IOException;


	/**
	 *
	 * @param list
	 * @param name
	 * @param buffer
	 * @param section
	 * @throws IOException
	 */
	public void format(ListOf list, BufferedWriter buffer, boolean section) throws IOException;


	/**
	 *
	 * @param events
	 * @param buffer
	 * @throws IOException
	 */
	public void format(ListOfEvents events, BufferedWriter buffer) throws IOException;
	
	/**
	 * Creates a heading for a subsubsection
	 * @param title The title of the subsubsection
	 * @param numbering If true the subsubsection will be numbered otherwise not.
	 * @return
	 */
	public StringBuffer subsubsection(String title, boolean numbering);
	
	/**
	 * 
	 * @param title
	 * @param numbering
	 * @return
	 */
	public StringBuffer subsection(String title, boolean numbering);
	
	/**
	 * 
	 * @param title
	 * @param numbering
	 * @return
	 */
	public StringBuffer section(String title, boolean numbering);
}
