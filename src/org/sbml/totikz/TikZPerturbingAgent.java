/*
 * $Id:  TikZPerturbingAgent.java 17:35:15 Meike Aichele$
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

import de.zbit.sbml.layout.PerturbingAgent;

/**
 * @author Meike Aichele
 * @since 1.0
 * @version $Rev$
 */
public class TikZPerturbingAgent extends PerturbingAgent<String>{
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNNode#draw(double, double, double, double, double, double)
   */
  @Override
  public String draw(double x, double y, double z, double width,
    double height, double depth) {
    String tikZ = "\\filldraw [fill = PerturbingAgent!50] (" +
        (x - (width / 6)) + "pt," + y + "pt) -- (" +
        (x + width + (width / 6)) + "pt," + y + "pt) -- (" +
        (x + width) + "pt," + (y + (height / 2)) + "pt) -- (" +
        (x + width + (width / 6)) + "pt," + (y + height)  + "pt) -- (" +
        (x - (width / 6)) + "pt," + (y + height)  + "pt) -- (" +
        x + "pt," + (y + (height / 2))  + "pt) -- (" +
        (x - (width / 6)) + "pt," + y + "pt);";
    return tikZ;
  }
  
}
