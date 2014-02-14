/*
 * $Id:  TikZUncertainProcessNode.java 13:55:09 Meike Aichele$
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

import de.zbit.sbml.layout.UncertainProcessNode;

/**
 * @author Meike Aichele
 * @since 1.0
 * @version $Rev$
 */
public class TikZUncertainProcessNode extends UncertainProcessNode<String> {
  
  private double lineWidth = TikZLayoutBuilder.DEFAULT_LINE_WIDTH;
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNNode#draw(double, double, double, double, double, double)
   */
  @Override
  public String draw(double x, double y, double z, double width,
    double height, double depth) {
    String uncertainProcessNodeCode = "\\draw [color = black, line width = " + lineWidth + "pt] ("
        + (x - (width/2d))
        + "pt,"
        + (y - (height/2d))
        + "pt) rectangle ("
        + (x + (width/2d))
        + "pt,"
        + (y + (height/2d))
        + "pt);\n"
        // draw label
        + "\\draw ("
        + x
        + "pt,"
        + y
        + "pt) node "
        + "[anchor = center] {\\fontfamily{phv}\\selectfont ?}; \n"
        ;
    return uncertainProcessNodeCode;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.ProcessNode#draw(double, double, double, double, double, double, double, org.sbml.jsbml.ext.layout.Point)
   */
  @Override
  public String draw(double x, double y, double z, double width,
    double height, double depth, double rotationAngle, Point rotationCenter) {
    
    String uncertainProcessCode = "\\draw [color = black, line width = " + lineWidth + "pt, rotate around = {"
        + rotationAngle
        + " : ("
        + rotationCenter.getX()
        + "pt, "
        + rotationCenter.getY()
        + "pt)}] ("
        + (x - (width/2d))
        + "pt,"
        + (y - (height/2d))
        + "pt) rectangle ("
        + (x + (width/2d))
        + "pt,"
        + (y + (height/2d))
        + "pt);\n"
        // draw label
        + "\\draw ("
        + x
        + "pt,"
        + y
        + "pt) node "
        + "[anchor = center, rotate = "
        + (rotationAngle % 90)
        +"] {\\fontfamily{phv}\\selectfont ?}; \n"
        ;
    return uncertainProcessCode;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.ProcessNode#drawLineSegments()
   */
  @Override
  public String drawLineSegment(LineSegment segment, double rotationAngle, Point rotationCenter) {
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
