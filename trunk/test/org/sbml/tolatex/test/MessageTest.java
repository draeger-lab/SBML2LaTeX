/*
 * $Id: MessageTest.java 18.02.2013 08:39:00 draeger$
 * $URL: MessageTest.java$
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
package org.sbml.tolatex.test;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import de.zbit.text.LaTeXFormatter;
import de.zbit.util.ResourceManager;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.0
 * @version $Rev$
 */
public class MessageTest {
	
	/**
	 * 
	 */
	private static final ResourceBundle bundle = ResourceManager.getBundle("org.sbml.tolatex.locales.SBMLreport");
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testSpeciesBoundaryCondition();
	}

	/**
	 * 
	 */
	public static void testSpeciesBoundaryCondition() {
		int specWithBound = 2, speciesCount = 1;
		LaTeXFormatter formatter = new LaTeXFormatter();
		formatter.setUsingTypewriterFont(true);
		
		System.out.println(MessageFormat.format(
			bundle.getString("SPECIES_WITH_BOUNDARY_CONDITION_COUNT"),
			MessageFormat.format(bundle.getString("NUMERALS"), specWithBound),
			formatter.texttt(bundle.getString("TRUE")),
			speciesCount,
			speciesCount,
			speciesCount));
		System.out.println(MessageFormat.format(
			bundle.getString("SPECIES_SECTION_ODE_REFERENCE"),
			formatter.protectedBlank(),
			formatter.ref("sec:DerivedRateEquations")));

		System.out.println(MessageFormat.format(
			"The given SBML document contains {0} {1,choice,0#issues| 1#issue| 1<issues}, which {1,choice,0#are| 1#is| 1<are} listed in the remainder of this model report. The messages and identification codes shown here are those reported by the {2}.",
			"one",
			Integer.valueOf(1), 
			"\\href{http://sbml.org/Facilities/Validator}{SBML.org online validator}"));
		
		
		System.out.println(MessageFormat.format(
			"The values of the assignment {0,choice,0#formulae| 1#formula| 1<formulae} {0,choice,0#are| 1#is| 1<are} computed at the moment this event fires{1,choice,0#.| 1#, not after the delay.}",
			Integer.valueOf(3),
			Integer.valueOf(1)));
	}
	
}
