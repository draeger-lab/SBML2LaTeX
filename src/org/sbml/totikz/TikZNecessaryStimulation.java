/*
 * $Id:  TikZNecessaryStimulation.java 13:35:15 Meike Aichele$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/trunk/src/org/sbml/totikz/TikZNecessaryStimulation.java $
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
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Point;

import de.zbit.sbml.layout.NecessaryStimulation;
import de.zbit.sbml.layout.SimpleLayoutAlgorithm;

/**
 * Class to draw a necessary stimulation
 * @author Meike Aichele
 * @version $Rev: 247 $
 */
public class TikZNecessaryStimulation extends TikZSBGNArc implements NecessaryStimulation<String> {

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.SBGNArc#draw(org.sbml.jsbml.ext.layout.CurveSegment, double)
	 */
	@Override
	public String draw(CurveSegment curveSegment, double lineWidth) {
		Point startPoint = curveSegment.getStart();
		Point endPoint = curveSegment.getEnd();
		double startX = startPoint.getX();
		double startY = startPoint.getY();
		double endX = endPoint.getX();
		double endY = endPoint.getY();

		Point middlePoint = calculateArrow(startPoint, endPoint);

		double middleX = middlePoint.getX();
		double middleY = middlePoint.getY();

		String tikzStart = TikZ.drawLine("-|", "black", lineWidth, startX, startY, middleX, middleY);
		// TODO triangle must have variable size
		String tikzEnd   = TikZ.drawLine("-open triangle 60", "black", lineWidth, middleX, middleY, endX, endY);

		//curveSegment instanceof LineSegment
		if (!curveSegment.isSetBasePoint1() || !curveSegment.isSetBasePoint2()) {
			return tikzStart + ";\n" + tikzEnd + ";\n";
			
		}

		//curveSegment instanceof CubicBezier
		CubicBezier bezier = (CubicBezier) curveSegment;
		Point basePoint1 = bezier.getBasePoint1();
		Point basePoint2 = bezier.getBasePoint2();
		double basePoint1X = basePoint1.getX();
		double basePoint1Y = basePoint1.getY();
		double basePoint2X = basePoint2.getX();
		double basePoint2Y = basePoint2.getY();
		
		return tikzStart
				+ ".. controls ("
				+ basePoint1X
				+ "pt,"
				+ basePoint1Y
				+ "pt) and ("
				+ basePoint2X
				+ "pt,"
				+ basePoint2Y
				+ "pt) .. "
				+ tikzEnd;

	}

	/**
	 * 
	 */
	private Point calculateArrow(Point startPoint, Point endPoint) {
		double angle = SimpleLayoutAlgorithm.calculateRotationAngle(startPoint, endPoint);
		double c = 12d;
		double b = Math.abs(Math.cos(Math.toRadians(angle)) * c);
		double a = Math.abs(Math.sin(Math.toRadians(angle)) * c);

		Point dockingPoint = new Point();
		if ((angle >= 0) && (angle < 90)) {
			dockingPoint.setX(endPoint.getX() - b);
			dockingPoint.setY(endPoint.getY() - a);
			dockingPoint.setZ(endPoint.getZ());
		} else if ((angle >= 90) && (angle < 180)) {
			dockingPoint.setX(endPoint.getX() + a);
			dockingPoint.setY(endPoint.getY() - b);
			dockingPoint.setZ(endPoint.getZ());
		} else if ((angle >= 180) && (angle < 270)) {
			dockingPoint.setX(endPoint.getX() + b);
			dockingPoint.setY(endPoint.getY() + a);
			dockingPoint.setZ(endPoint.getZ());
		} else {
			dockingPoint.setX(endPoint.getX() - a);
			dockingPoint.setY(endPoint.getY() + b);
			dockingPoint.setZ(endPoint.getZ());
		}
		return dockingPoint;
	}

}
