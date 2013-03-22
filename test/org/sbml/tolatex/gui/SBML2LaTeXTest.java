/*
 * $Id: SBML2LaTeXTest.java 82 2011-12-13 11:43:28Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/tags/version0.9.9/test/org/sbml/tolatex/gui/SBML2LaTeXTest.java $
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates 
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
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
package org.sbml.tolatex.gui;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.sbml.tolatex.SBML2LaTeX;
import org.sbml.tolatex.io.LaTeXOptionsIO;

import de.zbit.io.SBFileFilter;
import de.zbit.util.prefs.SBPreferences;

/**
 * Systematically compiles the SBML test suit with SBML2LaTeX.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-11
 * @version $Rev: 82 $
 */
public class SBML2LaTeXTest {
  
  /**
   * @param args
   *        one argument: the path to the folder containing all case* folders
   *        with the SBML test suit. Pass this without prefixes.
   * @throws XMLStreamException
   * @throws SBMLException
   * @throws IOException
   */
  public static void main(String[] args) throws XMLStreamException,
    IOException, SBMLException {
    File folder = new File(args[0]);
    File cases[] = folder.listFiles(SBFileFilter.createDirectoryFilter());
    File sbmlFiles[];
    SBMLDocument doc;
    SBPreferences prefsIO;
    SBMLReader reader = new SBMLReader();
    int i, j, bound = 5;
    for (i = 0; i < Math.min(cases.length, bound); i++) {
      sbmlFiles = cases[i].listFiles(SBFileFilter.createSBMLFileFilter());
      for (j = 0; j < sbmlFiles.length; j++) {
        doc = reader.readSBML(sbmlFiles[j]);
        if (LaTeXExportDialog.showDialog(doc)) {
          prefsIO = SBPreferences.getPreferencesFor(LaTeXOptionsIO.class);
          SBML2LaTeX.convert(doc, prefsIO
              .get(LaTeXOptionsIO.REPORT_OUTPUT_FILE));
        }
      }
    }
    System.exit(0);
  }

}
