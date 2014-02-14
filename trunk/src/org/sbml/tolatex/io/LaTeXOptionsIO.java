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
package org.sbml.tolatex.io;

import java.io.File;
import java.util.ResourceBundle;

import de.zbit.io.filefilter.MultipleFileFilter;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;

/**
 * Provides {@link Option} instances for SBML input and LaTeX output.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-10
 * @version $Rev$
 */
public interface LaTeXOptionsIO extends KeyProvider {
  
  /**
   * 
   */
  static final ResourceBundle resources = ResourceManager
      .getBundle("org.sbml.tolatex.locales.UI");
  
  /**
   * The SBML file to be opened
   */
  public static final Option<File> SBML_INPUT_FILE = new Option<File>(
      "SBML_INPUT_FILE", File.class, resources, new Range<File>(File.class,
          SBFileFilter.createSBMLFileFilter()), new File(System
            .getProperty("user.dir")));
  
  /**
   * The file where to save the generated LaTeX report. The standard way is to
   * let SBML2LaTeX generate a TeX file, which the user can then compile to a
   * different format, or from which parts can be easily extracted to support
   * scientific writing. Additionally, SBML2LaTeX may be used to directly
   * generate a PDF file if the LaTeX compiler pdfLaTeX is specified.
   */
  public static final Option<File> REPORT_OUTPUT_FILE = new Option<File>(
      "REPORT_OUTPUT_FILE", File.class, resources, new Range<File>(File.class,
          new MultipleFileFilter("Report files (*.tex, *.pdf)", SBFileFilter
            .createTeXFileFilter(), SBFileFilter.createPDFFileFilter())),
            new File(System.getProperty("user.dir")));
  
  /**
   * 
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final OptionGroup<?> INPUT_AND_OUTPUT_FILES = new OptionGroup(
    "INPUT_AND_OUTPUT_FILES", resources, SBML_INPUT_FILE, REPORT_OUTPUT_FILE);
  
}
