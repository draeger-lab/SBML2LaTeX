/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.tolatex.io;

import java.io.BufferedWriter;
import java.io.IOException;

import org.sbml.jsbml.Event;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;

/**
 * A super class for all export methods that convert the contence of JSBML
 * objects into a human-readable format.
 * 
 * @author Dieudonn&eacute; Motsuo Wouamba
 * @author Andreas Dr&auml;ger <andreas.draeger@uni-tuebingen.de>
 * @since 1.1
 */
public interface DisplaySBML {

	/**
	 * 
	 * @param list
	 * @param name
	 * @param buffer
	 * @param section
	 * @throws IOException
	 */
	public void format(ListOf<?> list, BufferedWriter buffer, boolean section)
			throws IOException;

	/**
	 * 
	 * @param events
	 * @param buffer
	 * @throws IOException
	 */
	public void format(ListOf<Event> events, BufferedWriter buffer)
			throws IOException;

	/**
	 * 
	 * @param model
	 * @param buffer
	 * @throws IOException
	 */
	public void format(Model model, BufferedWriter buffer) throws IOException;

	/**
	 * 
	 * @param doc
	 * @param buffer
	 * @throws IOException
	 */
	public void format(SBMLDocument doc, BufferedWriter buffer)
			throws IOException;
}
