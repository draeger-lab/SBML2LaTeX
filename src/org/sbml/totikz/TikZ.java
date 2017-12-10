/*
 * $Id: TiKZ.java 04.03.2013 08:59:42 draeger$
 * $URL: TiKZ.java$
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2007-2014 by the University of Tuebingen, Germany.
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.util.StringTools;

import de.zbit.sbml.layout.SBGNArc;

/**
 * A collection of methods to create TiKZ commands in LaTeX documents.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.0
 * @version $Rev$
 */
public class TikZ {

  /**
   * This class follows the singleton pattern and should therefore not be
   * instanciated.
   */
  private TikZ() {
    super();
  }

  /**
   * 
   * @param width
   * @param height
   * @return
   */
  public static String beginTikZPicture(double width, double height) {
    DecimalFormat df = new DecimalFormat(StringTools.DECIMAL_FORMAT,
      new DecimalFormatSymbols(Locale.ENGLISH));
    String xScale = df.format(1d / width), yScale = df.format(1d / height);
    StringBuilder sb = new StringBuilder();
    sb.append("\\begin{tikzpicture}[xscale = ");
    sb.append(xScale);
    sb.append("\\textwidth, yscale = -");
    sb.append(yScale);
    sb.append("\\textwidth]\n");
    return sb.toString();
  }

  /**
   * 
   * @param colorName
   * @param curve
   * @param lineWidth
   * @param tikZCatalysis
   * @return
   */
  public static String draw(String colorName, Curve curve, double lineWidth,
    SBGNArc<String> arc) {
    StringBuilder tikZString = new StringBuilder();
    CurveSegment curveSegment;
    for (int i = 0; i < curve.getCurveSegmentCount(); i++) {
      curveSegment = curve.getCurveSegment(i);
      if (i == curve.getCurveSegmentCount() - 1) {
        tikZString.append(arc.draw(curveSegment, lineWidth));
      } else {
        tikZString.append(draw(colorName, curveSegment, lineWidth));
      }
    }
    return tikZString.toString();
  }

  /**
   * draws a line without an arrow
   * @param colorName
   * @param curveSegment
   * @param lineWidth
   * @return
   */
  public static String draw(String colorName, CurveSegment curveSegment, double lineWidth) {
    return draw(null, colorName, curveSegment, lineWidth);
  }

  /**
   * 
   * @param colorName
   * @param lineSegment
   * @param lineWidth
   * @return
   */
  public static String draw(String colorName, LineSegment lineSegment,
    double lineWidth) {
    return draw(colorName, lineSegment, lineWidth, 0d, null);
  }

  /**
   * 
   * @param colorName
   * @param lineSegment
   * @param lineWidth
   * @param rotationAngle
   * @param rotationCenter
   * @return
   */
  public static String draw(String colorName, CurveSegment lineSegment,
    double lineWidth, double rotationAngle, Point rotationCenter) {
    Point start = lineSegment.getStart();
    Point end = lineSegment.getEnd();
    double x1 = start.getX();
    double y1 = start.getY();
    double x2 = end.getX();
    double y2 = end.getY();
    return drawLine("black", lineWidth, x1, y1, x2, y2, rotationAngle, rotationCenter);
  }

  /**
   * 
   * @param lineHead
   * @param colorName
   * @param curveSegment
   * @param lineWidth
   * @return
   */
  public static String draw(String lineHead, String colorName, CurveSegment curveSegment, double lineWidth) {
    if (!(curveSegment instanceof CubicBezier)) {
      LineSegment ls = (LineSegment) curveSegment;
      // curveSegment instanceof LineSegment
      Point startPoint = ls.getStart();
      Point endPoint = ls.getEnd();
      double startX = startPoint.getX();
      double startY = startPoint.getY();
      double endX = endPoint.getX();
      double endY = endPoint.getY();
      return drawLine(lineHead, colorName, lineWidth, startX, startY, endX, endY);
    }

    //curveSegment instanceof CubicBezier
    CubicBezier bezier = (CubicBezier) curveSegment;
    return drawCubicBezier("-open diamond", "black", bezier, lineWidth);
  }

  /**
   * 
   * @param colorName
   * @param lineWidth
   * @param x
   * @param y
   * @param radius
   * @return
   */
  public static String drawCircle(String colorName, double lineWidth, double x, double y, double radius) {
    StringBuilder sb = new StringBuilder();
    sb.append("\\draw [color = ");
    sb.append(colorName);
    sb.append(", line width = ");
    sb.append(lineWidth);
    sb.append("pt] (");
    sb.append(x);
    sb.append("pt, ");
    sb.append(y);
    sb.append("pt) circle (");
    sb.append(radius);
    sb.append("pt);\n");
    return sb.toString();
  }

  /**
   * 
   * @param cloneMarkerColorName
   * @param x
   * @param y
   * @param width
   * @param height
   * @return
   */
  public static String drawCloneMarkerEllipse(String cloneMarkerColorName, double x, double y,
    double width, double height) {
    StringBuilder sb = new StringBuilder();
    sb.append("\\begin{scope}\n\\clip (");
    sb.append((x + width));
    sb.append("pt,");
    sb.append((y + height));
    sb.append("pt) ellipse (");
    sb.append(width);
    sb.append("pt and ");
    sb.append(height);
    sb.append("pt);\n");
    sb.append("\\fill[");
    sb.append(cloneMarkerColorName);
    sb.append(")] (");
    sb.append(x);
    sb.append("pt,");
    sb.append((y + ((4d / 3d) * height)));
    sb.append("pt) rectangle (");
    sb.append((x + (2d * width)));
    sb.append("pt,");
    sb.append((y + (2d * height)));
    sb.append("pt);\n");
    sb.append("\\end{scope}\n");
    return sb.toString();
  }

  /**
   * 
   * @param colorName
   * @param bezier
   * @param lineWidth
   * @return
   */
  public static String drawCubicBezier(String colorName,
    CubicBezier bezier, double lineWidth) {
    return drawCubicBezier(null, colorName, bezier, lineWidth);
  }

  /**
   * 
   * @param colorName
   * @param lineWidth
   * @param xStart
   * @param yStart
   * @param xBase1
   * @param yBase1
   * @param xBase2
   * @param yBase2
   * @param xEnd
   * @param yEnd
   * @return
   */
  public static String drawCubicBezier(String colorName, double lineWidth,
    double xStart, double yStart, double xBase1, double yBase1, double xBase2,
    double yBase2, double xEnd, double yEnd) {
    return drawCubicBezier(null, colorName, lineWidth, xStart, yStart, xBase1, yBase1, xBase2, yBase2, xEnd, yEnd);
  }

  /**
   * 
   * @param lineHead
   * @param colorName
   * @param bezier
   * @param lineWidth
   * @return
   */
  public static String drawCubicBezier(String lineHead, String colorName,
    CubicBezier bezier, double lineWidth) {
    Point start = bezier.getStart();
    Point basePoint1 = bezier.getBasePoint1();
    Point basePoint2 = bezier.getBasePoint2();
    Point end = bezier.getEnd();
    double startX = start.getX();
    double startY = start.getY();
    double basePoint1X = basePoint1.getX();
    double basePoint1Y = basePoint1.getY();
    double basePoint2X = basePoint2.getX();
    double basePoint2Y = basePoint2.getY();
    double endX = end.getX();
    double endY = end.getY();
    return drawCubicBezier(lineHead, "black", lineWidth, startX, startY, basePoint1X, basePoint1Y, basePoint2X, basePoint2Y, endX, endY);
  }

  /**
   * 
   * @param lineHead
   * @param colorName
   * @param lineWidth
   * @param xStart
   * @param yStart
   * @param xBase1
   * @param yBase1
   * @param xBase2
   * @param yBase2
   * @param xEnd
   * @param yEnd
   * @return
   */
  public static String drawCubicBezier(String lineHead, String colorName, double lineWidth,
    double xStart, double yStart, double xBase1, double yBase1, double xBase2,
    double yBase2, double xEnd, double yEnd) {
    StringBuilder sb = new StringBuilder();
    sb.append("\\draw [");
    if ((lineHead != null) && (lineHead.length() > 0)) {
      sb.append(lineHead);
      sb.append(", ");
    }
    sb.append("color = ");
    sb.append(colorName);
    sb.append(", line width = ");
    sb.append(lineWidth);
    sb.append("pt] (");
    sb.append(xStart);
    sb.append("pt,");
    sb.append(yStart);
    sb.append("pt) ..  controls (");
    sb.append(xBase1);
    sb.append("pt, ");
    sb.append(yBase1);
    sb.append("pt) and (");
    sb.append(xBase2);
    sb.append("pt, ");
    sb.append(yBase2);
    sb.append("pt) .. (");
    sb.append(xEnd);
    sb.append("pt, ");
    sb.append(yEnd);
    sb.append("pt);\n");
    return sb.toString();
  }

  /**
   * 
   * @param lineHead
   * @param colorName
   * @param curveSegment
   * @param lineWidth
   * @return
   */
  public static String drawFromTo(String lineHead, String colorName,
    CurveSegment curveSegment, double lineWidth) {
    if (!(curveSegment instanceof CubicBezier)) {
      LineSegment ls = (LineSegment) curveSegment;
      Point startPoint = ls.getStart();
      Point endPoint = ls.getEnd();
      double startX = startPoint.getX();
      double startY = startPoint.getY();
      double endX = endPoint.getX();
      double endY = endPoint.getY();

      return drawFromTo(lineHead, colorName, lineWidth, startX, startY, endX, endY);
    }
    return draw(lineHead, colorName, curveSegment, lineWidth);
  }

  /**
   * 
   * @param lineHead
   * @param colorName
   * @param lineWidth
   * @param startX
   * @param startY
   * @param endX
   * @param endY
   * @return
   */
  public static String drawFromTo(String lineHead, String colorName, double lineWidth,
    double startX, double startY, double endX, double endY) {
    return drawLine(lineHead, colorName, lineWidth, startX, startY, "to", endX, endY, 0d, null);
  }

  /**
   * Draws a line with the given parameters from start (X, Y) to end (X, Y).
   * 
   * @param colorName
   * @param lineWidth
   * @param startX
   * @param startY
   * @param endX
   * @param endY
   * @return
   */
  public static String drawLine(String colorName, double lineWidth, double startX, double startY, double endX, double endY) {
    return drawLine(null, colorName, lineWidth, startX, startY, endX, endY);
  }

  /**
   * 
   * @param colorName
   * @param lineWidth
   * @param startX
   * @param startY
   * @param endX
   * @param endY
   * @param rotationAngle
   * @param rotationCenter
   * @return
   */
  public static String drawLine(String colorName, double lineWidth, double startX,
    double startY, double endX, double endY, double rotationAngle, Point rotationCenter) {
    return drawLine(null, colorName, lineWidth, startX, startY, endX, endY, rotationAngle, rotationCenter);
  }

  /**
   * 
   * @param lineHead
   * @param colorName
   * @param lineWidth
   * @param startX
   * @param startY
   * @param endX
   * @param endY
   * @return
   */
  public static String drawLine(String lineHead, String colorName, double lineWidth, double startX, double startY, double endX, double endY) {
    return drawLine(lineHead, colorName, lineWidth, startX, startY, endX, endY, 0d, null);
  }

  /**
   * 
   * @param lineHead
   * @param colorName
   * @param lineWidth
   * @param startX
   * @param startY
   * @param endX
   * @param endY
   * @param rotationAngle
   * @param rotationCenter
   * @return
   */
  public static String drawLine(String lineHead, String colorName, double lineWidth, double startX,
    double startY, double endX, double endY, double rotationAngle, Point rotationCenter) {
    return drawLine(lineHead, colorName, lineWidth, startX, startY, "--", endX, endY, rotationAngle, rotationCenter);
  }

  /**
   * 
   * @param lineHead
   * @param colorName
   * @param lineWidth
   * @param startX
   * @param startY
   * @param connection e.g., {@code --} or {@code to} (the latter one with any options)
   * @param endX
   * @param endY
   * @param rotationAngle
   * @param rotationCenter
   * @return
   */
  public static String drawLine(String lineHead, String colorName,
    double lineWidth, double startX, double startY, String connection, double endX,
    double endY, double rotationAngle, Point rotationCenter) {
    StringBuilder sb = new StringBuilder();
    sb.append("\\draw [");
    if ((lineHead != null) && (lineHead.length() > 0)) {
      sb.append(lineHead);
      sb.append(", ");
    }
    sb.append("color = ");
    sb.append(colorName);
    sb.append(", line width = ");
    sb.append(lineWidth);
    sb.append("pt");
    if ((rotationAngle % 360) != 0) {
      double x, y;
      if (rotationCenter == null) {
        x = y = 0d;
      } else {
        x = rotationCenter.getX();
        y = rotationCenter.getY();
      }
      sb.append(", rotate around = {");
      sb.append(rotationAngle);
      sb.append(" : (");
      sb.append(x);
      sb.append("pt, ");
      sb.append(y);
      sb.append("pt)}");
    }
    sb.append("] (");
    sb.append(startX);
    sb.append("pt, ");
    sb.append(startY);
    sb.append("pt) ");
    sb.append(connection);
    sb.append(" (");
    sb.append(endX);
    sb.append("pt, ");
    sb.append(endY);
    sb.append("pt);\n");
    return sb.toString();
  }

  /**
   * 
   * @param colorName
   * @param lineWidth
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @return
   */
  public static String drawShapeRectangle(String colorName, double lineWidth,
    double x1, double y1, double x2, double y2) {
    return drawShapeRectangle(colorName, lineWidth, x1, y1, x2, y2, 0d, null);
  }

  /**
   * 
   * @param colorName
   * @param lineWidth
   * @param x
   * @param y
   * @param width
   * @param height
   * @param rotationAngle
   * @param rotationCenter
   * @return
   */
  public static String drawShapeRectangle(String colorName, double lineWidth,
    double x1, double y1, double x2, double y2, double rotationAngle,
    Point rotationCenter) {
    StringBuilder sb = new StringBuilder();
    sb.append("\\draw [color = ");
    sb.append(colorName);
    sb.append(", line width = ");
    sb.append(lineWidth);
    sb.append("pt, rotate around = {");
    sb.append(rotationAngle);
    sb.append(" : (");
    sb.append(rotationCenter.getX());
    sb.append("pt, ");
    sb.append(rotationCenter.getY());
    sb.append("pt)}] (");
    sb.append(x1);
    sb.append("pt, ");
    sb.append(y1);
    sb.append("pt) rectangle (");
    sb.append(x2);
    sb.append("pt, ");
    sb.append(y2);
    sb.append("pt);\n");
    return sb.toString();
  }

  /**
   * 
   * @param x
   * @param y
   * @param orientation
   * @param fontFamily phv is the short of Helvetica font family
   * @param text
   * @return
   */
  public static String drawText(double x, double y, String orientation, String fontFamily,
    String text) {
    StringBuilder sb = new StringBuilder();
    sb.append("\\draw (");
    sb.append(x);
    sb.append("pt,");
    sb.append(y);
    sb.append("pt) node [");
    sb.append(orientation);
    sb.append("] {\\fontfamily{");
    sb.append(fontFamily);
    sb.append("}\\selectfont ");
    sb.append(text);
    sb.append("};\n");
    return sb.toString();
  }

  /**
   * 
   * @return
   */
  public static String endTikZPicture() {
    return "\\end{tikzpicture}\n";
  }

  /**
   * 
   * @param fillColorName
   * @param x
   * @param y
   * @param radius
   * @param lineWidth
   * @return
   */
  public static String fillShapeCircle(String fillColorName, double x, double y, double radius, double lineWidth) {
    StringBuilder sb = new StringBuilder();
    sb.append("\\filldraw [fill = ");
    sb.append(fillColorName);
    sb.append(", line width = ");
    sb.append(lineWidth);
    sb.append("pt");
    sb.append("] (");
    sb.append(x);
    sb.append("pt, ");
    sb.append(y);
    sb.append("pt) circle (");
    sb.append(radius);
    sb.append("pt);\n");
    return sb.toString();
  }

  /**
   * 
   * @param fillColorName
   * @param lineColorName
   * @param lineWidth
   * @param x
   * @param y
   * @param width
   * @param height
   * @return
   */
  public static String fillShapeEllipse(String fillColorName,
    String lineColorName, double lineWidth, double x, double y,
    double width, double height) {
    StringBuilder sb = new StringBuilder();
    sb.append("\\filldraw [fill = ");
    sb.append(fillColorName);
    sb.append(", line width = ");
    sb.append(lineWidth);
    sb.append("pt, draw = ");
    sb.append(lineColorName);
    sb.append("] (");
    sb.append((x + width));
    sb.append("pt,");
    sb.append((y + height));
    sb.append("pt) ellipse (");
    sb.append(width);
    sb.append("pt and ");
    sb.append(height);
    sb.append("pt);\n");
    return sb.toString();
  }

  /**
   * 
   * @param fillColorName
   * @param lineColorName
   * @param lineWidth
   * @param x
   * @param y
   * @param width
   * @param height
   * @param rounded
   * @return
   */
  public static String fillShapeRectangle(String fillColorName, String lineColorName,
    double lineWidth, double x, double y, double width, double height,
    double rounded) {
    StringBuilder sb = new StringBuilder();
    sb.append("\\filldraw [fill = ");
    sb.append(fillColorName);
    sb.append(", line width = ");
    sb.append(lineWidth);
    sb.append("pt, draw = ");
    sb.append(lineColorName);
    sb.append(", rounded corners = ");
    sb.append(rounded);
    sb.append("pt] (");
    sb.append(x);
    sb.append("pt, ");
    sb.append(y);
    sb.append("pt) rectangle (");
    sb.append(x + width);
    sb.append("pt, ");
    sb.append(y + height);
    sb.append("pt);\n");
    return sb.toString();
  }

  /**
   * 
   * @param options
   * @return
   */
  public static String useTikZLibrary(String... options) {
    StringBuilder sb = new StringBuilder();
    sb.append("\\usetikzlibrary{");
    for (int i = 0; (options != null) && (i < options.length); i++) {
      sb.append(options[i]);
      if (i < options.length - 1) {
        sb.append(", ");
      }
    }
    sb.append("}\n");
    return sb.toString();
  }

}
