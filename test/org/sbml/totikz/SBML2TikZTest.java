/*
 * $Id:  SBML2TikZTest.java 13:37:59 Meike Aichele$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2008-2014 by the University of Tuebingen, Germany.
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
package org.sbml.totikz;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

/**
 * class to test the TikZ-package.
 * @author Meike Aichele
 * @version $Rev$
 */
public class SBML2TikZTest {

	/**
	 * Run SBML2TikZ: <br>
	 * - arg[0] = the layouted SBMLDocument <br>
	 * - arg[1] = layout index
	 * - arg[2] = the output file (tex-file where the result of the computation is saved)
	 * @param args
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public static void main(String args[]) throws XMLStreamException, IOException {
		new SBML2TikZ(new File(args[0]), Integer.parseInt(args[1]), new File(args[2]));
	}

}
