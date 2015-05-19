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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import de.zbit.sbml.layout.LayoutDirector;

/**
 * class starts a {@link LayoutDirector} with a {@link TikZLayoutBuilder}
 * 
 * @author Mirjam Gutekunst
 * @author Andreas Dr&auml;ger
 * @since 1.0
 * @version $Rev$
 */
public class SBML2TikZ {

  /**
   * Method to run the methods for drawing the SBMLDocument in TikZ with a flux file. For the reactions in the flux file
   * the edges will be drawn thicker in correlation to the given flux values.
   * @param inputFile
   * @param outputFile
   * @param fluxesFile ATTENTION: The ids in this file have to be the ids of the reaction glyphs.
   * @throws XMLStreamException
   * @throws IOException
   */
  public SBML2TikZ(File inputFile, File outputFile, File fluxesFile) throws XMLStreamException, IOException {
    LayoutDirector<BufferedWriter> director = new LayoutDirector<BufferedWriter>(inputFile, createTikZLayoutBuilder(outputFile), new TikZLayoutAlgorithm(), fluxesFile);
    director.run();
  }

  /**
   * 
   * @param outputFile
   * @return
   * @throws IOException
   */
  private TikZLayoutBuilder<BufferedWriter> createTikZLayoutBuilder(File outputFile) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
    return new TikZLayoutBuilder<BufferedWriter>(writer);
  }

  /**
   * Method to run the methods for drawing the SBMLDocument in TikZ without considering the fluxes.
   * @param inputFile
   * @param outputFile
   * @throws XMLStreamException
   * @throws IOException
   */
  public SBML2TikZ(File inputFile, File outputFile) throws XMLStreamException, IOException {
    LayoutDirector<BufferedWriter> director = new LayoutDirector<BufferedWriter>(inputFile, createTikZLayoutBuilder(outputFile), new TikZLayoutAlgorithm());
    director.run();
  }

  /**
   * Method to run the methods for drawing the SBMLDocument in TikZ without considering the fluxes
   * but choosing a layout number.
   * @param inputFile
   * @param Layoutnumber
   * @param outputFile
   * @throws XMLStreamException
   * @throws IOException
   */
  public SBML2TikZ(File inputFile, int Layoutnumber, File outputFile) throws XMLStreamException, IOException {
    LayoutDirector<BufferedWriter> director = new LayoutDirector<BufferedWriter>(inputFile, createTikZLayoutBuilder(outputFile), new TikZLayoutAlgorithm());
    director.setLayoutIndex(Layoutnumber);
    director.run();
  }

}
