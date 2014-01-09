/*
 * $Id$
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

import de.zbit.sbml.layout.UnspecifiedNode;

/**
 * class that represents a unspecified entity node for the TikZ graphical
 * representation
 * 
 * @author Mirjam Gutekunst
 * @version $Rev$
 */
public class TikZUnspecifiedNode extends UnspecifiedNode<String> {
	
	private double lineWidth = TikZLayoutBuilder.DEFAULT_LINE_WIDTH;
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.SBGNNode#draw(double x, double y, double z, double width, double height, double depth)
	 */
	public String draw(double x, double y, double z, double width, double height, double depth) {
		
		width = width / 2d;
		height = height / 2d;
		
		if (isSetCloneMarker()) {
			return TikZ.drawCloneMarkerEllipse("black", x, y, width, height);
		}
		
		return TikZ.fillShapeEllipse("unspecifiedEntity!50", "white", lineWidth, x, y, width, height);
	}
	
}
