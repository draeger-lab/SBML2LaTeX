/*
 * $Id:  TikZCubicBezier.java 16:13:24 Meike Aichele$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/trunk/src/org/sbml/totikz/TikZCubicBezier.java $
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2008-2013 by the University of Tuebingen, Germany.
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

import org.sbml.jsbml.ext.layout.CubicBezier;


/**
 * @author Meike Aichele
 * @version $Rev: 249 $
 */
public class TikZCubicBezier {

	/**
	 * Method creates the TikZ-Code for a {@link CubicBezier}.
	 * @param cubicBezier
	 * @param lineWidth
	 * @return
	 */
	public String draw(CubicBezier cubicBezier, double lineWidth) {
		return TikZ.drawCubicBezier("black", cubicBezier, lineWidth);
	}

	/**
	 * Method creates the TikZ-Code for a {@link CubicBezier} without a given line
	 * width.
	 * 
	 * @param cubicBezier
	 * @return
	 */
	public String draw(CubicBezier cubicBezier) {
		return draw(cubicBezier, TikZLayoutBuilder.DEFAULT_LINE_WIDTH);		
	}

}
