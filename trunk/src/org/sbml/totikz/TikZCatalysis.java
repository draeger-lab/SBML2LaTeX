/*
 * $Id$
 * $URL$
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

import org.sbml.jsbml.ext.layout.CurveSegment;

import de.zbit.sbml.layout.Catalysis;

/**
 * class that represents a catalysis arc for the TikZ graphical representation
 * 
 * @author Mirjam Gutekunst
 * @version $Rev$
 */
public class TikZCatalysis extends TikZSBGNArc implements Catalysis<String> {

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.SBGNArc#draw(CurveSegment curveSegment)
	 */
	public String draw(CurveSegment curveSegment, double lineWidth) {
		return TikZ.drawFromTo("-o", "black", curveSegment, lineWidth);
	}

}
