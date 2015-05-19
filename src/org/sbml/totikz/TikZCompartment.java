/*
 * $Id:  TikZCompartment.java 17:30:34 Meike Aichele$
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

import de.zbit.sbml.layout.Compartment;

/**
 * @author Meike Aichele
 * @since 1.0
 * @version $Rev$
 */
public class TikZCompartment extends Compartment<String> {

  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNNode#draw(double, double, double, double, double, double)
   */
  @Override
  public String draw(double x, double y, double z, double width,
    double height, double depth) {
    double offset = 9d, rounded = 10d, lineWidth = TikZLayoutBuilder.DEFAULT_LINE_WIDTH;
    StringBuilder sb = new StringBuilder();
    sb.append(TikZ.fillShapeRectangle("compartment!50", "compartment", lineWidth, x, y, width, height, rounded));
    sb.append(TikZ.fillShapeRectangle("white", "compartment", lineWidth, x + offset, y + offset, width - 2d * offset, height - 2d * offset, .75d * rounded));
    return sb.toString();
  }

}
