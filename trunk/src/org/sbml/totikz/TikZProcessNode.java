/*
 * $Id:  TikZProcessNode.java 14:10:09 Meike Aichele$
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

import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;

import de.zbit.sbml.layout.ProcessNode;

/**
 * @author Meike Aichele
 * @version $Rev$
 */
public class TikZProcessNode extends ProcessNode<String>{

	private double lineWidth = TikZLayoutBuilder.DEFAULT_LINE_WIDTH;

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.SBGNNode#draw(double, double, double, double, double, double)
	 */
	public String draw(double x, double y, double z, double width,
			double height, double depth) {
		String processNodeCode = "\\draw [color = black, line width = " + lineWidth + "pt] ("
		+ (x - (width/2d))
		+ "pt,"
		+ (y - height)
		+ "pt) rectangle ("
		+ (x + (width/2d))
		+ "pt,"
		+ (y + height)
		+ "pt);\n";
		return processNodeCode;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.ProcessNode#draw(double, double, double, double, double, double, double, org.sbml.jsbml.ext.layout.Point)
	 */
	public String draw(double x, double y, double z, double width, 
			double height, double depth, double rotationAngle, Point rotationCenter) {
		return TikZ.drawShapeRectangle("black", lineWidth, x - width / 2d, y - height, x + width / 2d, y + height, rotationAngle, rotationCenter);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.ProcessNode#drawLineSegments()
	 */
	@Override
	public String drawLineSegment(LineSegment lineSegment, double rotationAngle, Point rotationCenter) {
		if ((rotationAngle % 180) == 0) {
			return TikZ.draw("black", lineSegment, lineWidth);
		}
		return TikZ.draw("black", lineSegment, lineWidth, rotationAngle, rotationCenter);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.ProcessNode#getLineWidth()
	 */
	@Override
	public double getLineWidth() {
		return lineWidth;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.ProcessNode#setLineWidth(double)
	 */
	@Override
	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}

}
