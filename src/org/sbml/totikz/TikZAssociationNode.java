/*
 * $Id:  TikZAssociationNode.java 15:44:45 Meike Aichele$
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

import de.zbit.sbml.layout.AssociationNode;

/**
 * @author Meike Aichele
 * @version $Rev$
 */
public class TikZAssociationNode extends AssociationNode<String>{

	private double lineWidth = TikZLayoutBuilder.DEFAULT_LINE_WIDTH;
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.AssociationNode#draw(double, double, double, double, double, double)
	 */
	@Override
	public String draw(double x, double y, double z, double width,
			double height, double depth) {
		double radius = (width/2d);
		return TikZ.fillShapeCircle("black", x, y, radius, lineWidth);
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.AssociationNode#drawLineSegment(org.sbml.jsbml.ext.layout.LineSegment, double, org.sbml.jsbml.ext.layout.Point)
	 */
	@Override
	public String drawLineSegment(LineSegment segment,
			double rotationAngle, Point rotationCenter) {
		String lineSegment = null;

		Point start = segment.getStart();
		double x1 = start.getX();
		double y1 = start.getY();
		Point end = segment.getEnd();
		double x2 = end.getX();
		double y2 = end.getY();

		if ((rotationAngle % 180) == 0) {
			lineSegment = TikZ.drawLine("black", lineWidth, x1, y1, x2, y2);
		} else {
			lineSegment = TikZ.drawLine("black", lineWidth, x1, y1, x2, y2, rotationAngle, rotationCenter);
		}
		return lineSegment;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.AssociationNode#getLineWidth()
	 */
	@Override
	public double getLineWidth() {
		return lineWidth;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.AssociationNode#setLineWidth(double)
	 */
	@Override
	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.SBGNReactionNode#draw(double, double, double, double, double, double, double, org.sbml.jsbml.ext.layout.Point)
	 */
	//@Override
	public String draw(double x, double y, double z, double width,
			double height, double depth, double rotationAngle,
			Point rotationCenter) {
		// an association node is round so you don't have to remind the rotation
		return draw(x, y, z, width, height, depth);
	}

}
