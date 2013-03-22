/*
 * $Id: EventTest.java 60 2011-03-07 17:20:39Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/tags/version0.9.8/test/org/sbml/tolatex/gui/EventTest.java $
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates 
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
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

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbml.jsbml.xml.stax.SBMLWriter;
import org.sbml.tolatex.SBML2LaTeX;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev: 60 $
 */
public class EventTest {

    /**
     * @param args
     * @throws ParseException 
     * @throws SBMLException 
     * @throws XMLStreamException 
     */
	public static void main(String[] args) throws ParseException,
			XMLStreamException, SBMLException {
		SBMLDocument doc = new SBMLDocument(3, 1);
		Model model = doc.createModel("event_model");
		Compartment c = model.createCompartment("compartment");
		model.createSpecies("s1", c);
		model.createSpecies("s2", c);
		Event ev = model.createEvent();
		ev.createTrigger(false, true, ASTNode.leq(new ASTNode(3),
				new ASTNode(2)));
		ev.createPriority(ASTNode.parseFormula("25"));
		ev.createDelay(ASTNode.parseFormula("2"));
		ev.createEventAssignment("s1", ASTNode.parseFormula("s2"));
		SBMLWriter writer = new SBMLWriter();
		writer.write(doc, System.out);
		try {
			SBML2LaTeX.convert(doc, "/home/draeger/test.pdf");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
