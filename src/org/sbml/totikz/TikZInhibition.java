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

import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;

import de.zbit.sbml.layout.Inhibition;

/**
 * class that represents an inhibition arc for the TikZ graphical representation
 * 
 * @author Mirjam Gutekunst
 * @since 1.0
 * @version $Rev$
 */
public class TikZInhibition extends TikZSBGNArc implements Inhibition<String> {
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNArc#draw(CurveSegment curveSegment)
   */
  @Override
  public String draw(CurveSegment curveSegment, double lineWidth) {
    LineSegment ls = (LineSegment) curveSegment;
    Point startPoint = ls.getStart();
    Point endPoint = ls.getEnd();
    double startX = startPoint.getX();
    double startY = startPoint.getY();
    double endX = endPoint.getX();
    double endY = endPoint.getY();
    
    //!curveSegment instanceof CubicBezier
    if (!(curveSegment instanceof CubicBezier)) {
      // TODO draw a line instead of a pipe
      // TODO add the possibility to rotate
      return TikZ.drawFromTo("-|", "black", lineWidth, startX, startY, endX - 2d, endY);
      
    } else {
      //curveSegment instanceof CubicBezier
      CubicBezier bezier = (CubicBezier) curveSegment;
      Point basePoint1 = bezier.getBasePoint1();
      Point basePoint2 = bezier.getBasePoint2();
      double basePoint1X = basePoint1.getX();
      double basePoint1Y = basePoint1.getY();
      double basePoint2X = basePoint2.getX();
      double basePoint2Y = basePoint2.getY();
      
      return TikZ.drawCubicBezier("-|", "black", lineWidth, startX, startY, basePoint1X, basePoint1Y, basePoint2X, basePoint2Y, endX - 2d, endY);
    }
  }
  
}
