/*
 * $Id: LaTeXOptions.java 72 2011-11-17 10:57:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/tags/version0.9.9/src/org/sbml/tolatex/LaTeXOptions.java $
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
package org.sbml.tolatex;

import java.io.File;
import java.util.ResourceBundle;

import de.zbit.io.SBFileFilter;
import de.zbit.util.FileTools;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;

/**
 * A collection of meaningful {@link Option} instances for the configuration of
 * {@link SBML2LaTeX}.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 * @version $Rev: 72 $
 */
public interface LaTeXOptions extends KeyProvider {
  
  /**
   * 
   */
  static final Range<Boolean> TRUE_CONDITION = new Range<Boolean>(
    Boolean.class, Boolean.TRUE);
  
  /**
   * 
   */
  static final ResourceBundle resources = ResourceManager
      .getBundle("org.sbml.tolatex.locales.UI");
  
  /**
   * If true, the libSBML model consistency check is performed and the results
   * are written in the glossary of the model report.
   */
  public static final Option<Boolean> CHECK_CONSISTENCY = new Option<Boolean>(
    "CHECK_CONSISTENCY", Boolean.class, resources
        .getString("CHECK_CONSISTENCY_TOOLTIP"), Boolean.FALSE, resources
        .getString("CHECK_CONSISTENCY"));
  
  /**
   * If this option is set to true, all temporary files will be deleted after
   * running SBML2LaTeX. In case of PDF creation, for instance, this will cause
   * even the TeX file to be deleted. However, this option can be meaningful to
   * remove all the temporary files created by your system's LaTeX compiler.
   */
  public static final Option<Boolean> CLEAN_WORKSPACE = new Option<Boolean>(
    "CLEAN_WORKSPACE", Boolean.class, resources
        .getString("CLEAN_WORKSPACE_TOOLTIP"), Boolean.FALSE, resources
        .getString("CLEAN_WORKSPACE"));
  
  /**
   * Allows to select the font of captions and other sans serif text. Default:
   * helvetica
   */
  public static final Option<String> FONT_HEADINGS = new Option<String>(
    "FONT_HEADINGS", String.class,
    resources.getString("FONT_HEADINGS_TOOLTIP"), new Range<String>(
      String.class, "{avant,cmss,helvetica}"), "helvetica", resources
        .getString("FONT_HEADINGS"));
  
  /**
   * The font size for LaTeX documents.
   */
  public static final Option<Short> FONT_SIZE = new Option<Short>("FONT_SIZE",
    Short.class, resources.getString("FONT_SIZE_TOOLTIP"), new Range<Short>(
      Short.class, "{8,9,10,11,12,14,17}"), Short.valueOf((short) 11),
    resources.getString("FONT_SIZE"));
  
  /**
   * Allows to select the font of continuous text. Choosing 'times' is actually
   * not recommended because in some cases equations might not look as nicely as
   * they do when using 'mathptmx'. Default: Times font mathptmx.
   */
  public static final Option<String> FONT_TEXT = new Option<String>(
    "FONT_TEXT", String.class, resources.getString("FONT_TEXT_TOOLTIP"),
    new Range<String>(String.class,
      "{chancery,charter,cmr,mathptmx,palatino,times,utopia}"), "mathptmx",
    resources.getString("FONT_TEXT"));
  
  /**
   * Decides whether to set the LaTeX document in landscape or portrait mode.
   */
  public static final Option<Boolean> LANDSCAPE = new Option<Boolean>(
    "LANDSCAPE", Boolean.class, resources.getString("LANDSCAPE_TOOLTIP"),
    Boolean.FALSE, resources.getString("LANDSCAPE"));
  
  /**
   * If true (default), MIRIAM annotations are included into the model report if
   * there are any. This option may require the path to the MIRIAM translation
   * file to be specified.
   */
  public static final Option<Boolean> MIRIAM_ANNOTATION = new Option<Boolean>(
    "MIRIAM_ANNOTATION", Boolean.class, resources
        .getString("MIRIAM_ANNOTATION_TOOLTIP"), Boolean.TRUE, resources
        .getString("MIRIAM_ANNOTATION"));
  
  /**
   * The paper size for LaTeX documents.
   */
  public static final Option<String> PAPER_SIZE = new Option<String>(
    "PAPER_SIZE", String.class, resources.getString("PAPER_SIZE_TOOLTIP"),
    new Range<String>(String.class,
      "{letter,legal,executive,a0,a1,a2,a3,a4,a5,a6,a7,a8,a9,b0,b1,b2,b3,b4,b5,b6,b7,b8,b9,c0,c1,c2,c3,c4,c5,c6,c7,c8,c9,d0,d1,d2,d3,d4,d5,d6,d7,d8,d9}"),
    "a4", resources.getString("PAPER_SIZE"));
  
  /**
   * Decides whether to write the names or the identifiers of NamedSBase object
   * in equations.
   */
  public static final Option<Boolean> PRINT_NAMES_IF_AVAILABLE = new Option<Boolean>(
    "PRINT_NAMES_IF_AVAILABLE", Boolean.class, resources
        .getString("PRINT_NAMES_IF_AVAILABLE_TOOLTIP"), Boolean.FALSE,
    resources.getString("PRINT_NAMES_IF_AVAILABLE"));
  
  /**
   * Decides whether to create a separate title page instead of a simple
   * heading.
   */
  public static final Option<Boolean> TITLE_PAGE = new Option<Boolean>(
    "TITLE_PAGE", Boolean.class, resources.getString("TITLE_PAGE_TOOLTIP"),
    Boolean.FALSE, resources.getString("TITLE_PAGE"));
  
  /**
   * Key that decides whether or not identifiers should be written in typewriter
   * font when these occur in mathematical equations.
   */
  public static final Option<Boolean> TYPEWRITER = new Option<Boolean>(
    "TYPEWRITER", Boolean.class, resources.getString("TYPEWRITER_TOOLTIP"),
    Boolean.TRUE, resources.getString("TYPEWRITER"));
  
  /**
   * 
   */
  public static final Option<Boolean> INCLUDE_COMPARTMENTS_SECTION = new Option<Boolean>(
    "INCLUDE_COMPARTMENTS_SECTION", Boolean.class, resources
        .getString("INCLUDE_COMPARTMENTS_SECTION_TOOLTIP"), Boolean.TRUE,
    resources.getString("INCLUDE_COMPARTMENTS_SECTION"));

  /**
   * 
   */
  public static final Option<Boolean> INCLUDE_COMPARTMENT_TYPES_SECTION = new Option<Boolean>(
    "INCLUDE_COMPARTMENT_TYPES_SECTION", Boolean.class, resources
        .getString("INCLUDE_COMPARTMENT_TYPES_SECTION_TOOLTIP"), Boolean.TRUE,
    resources.getString("INCLUDE_COMPARTMENT_TYPES_SECTION"));
  
  /**
   * 
   */
  public static final Option<Boolean> INCLUDE_CONSTRAINTS_SECTION = new Option<Boolean>(
    "INCLUDE_CONSTRAINTS_SECTION", Boolean.class, resources
        .getString("INCLUDE_CONSTRAINTS_SECTION_TOOLTIP"), Boolean.TRUE,
    resources.getString("INCLUDE_CONSTRAINTS_SECTION"));
  
  /**
   * 
   */
  public static final Option<Boolean> INCLUDE_EVENTS_SECTION = new Option<Boolean>(
    "INCLUDE_EVENTS_SECTION", Boolean.class, resources
        .getString("INCLUDE_EVENTS_SECTION_TOOLTIP"), Boolean.TRUE, resources
        .getString("INCLUDE_EVENTS_SECTION"));
  
  /**
   * 
   */
  public static final Option<Boolean> INCLUDE_INITIAL_ASSIGNMENTS_SECTION = new Option<Boolean>(
    "INCLUDE_INITIAL_ASSIGNMENTS_SECTION", Boolean.class, resources
        .getString("INCLUDE_INITIAL_ASSIGNMENTS_SECTION_TOOLTIP"),
    Boolean.TRUE, resources.getString("INCLUDE_INITIAL_ASSIGNMENTS_SECTION"));
  
  /**
   * 
   */
  public static final Option<Boolean> INCLUDE_FUNCTION_DEFINITIONS_SECTION = new Option<Boolean>(
    "INCLUDE_FUNCTION_DEFINITIONS_SECTION", Boolean.class, resources
        .getString("INCLUDE_FUNCTION_DEFINITIONS_SECTION_TOOLTIP"),
    Boolean.TRUE, resources.getString("INCLUDE_FUNCTION_DEFINITIONS_SECTION"));
  
  /**
   * 
   */
  public static final Option<Boolean> INCLUDE_REACTIONS_SECTION = new Option<Boolean>(
    "INCLUDE_REACTIONS_SECTION", Boolean.class, resources
        .getString("INCLUDE_REACTIONS_SECTION_TOOLTIP"), Boolean.TRUE,
    resources.getString("INCLUDE_REACTIONS_SECTION"));
  
  /**
   * 
   */
  public static final Option<Boolean> INCLUDE_PARAMETERS_SECTION = new Option<Boolean>(
    "INCLUDE_PARAMETERS_SECTION", Boolean.class, resources
        .getString("INCLUDE_PARAMETERS_SECTION_TOOLTIP"), Boolean.TRUE,
    resources.getString("INCLUDE_PARAMETERS_SECTION"));
  
  /**
   * 
   */
  public static final Option<Boolean> INCLUDE_RULES_SECTION = new Option<Boolean>(
    "INCLUDE_RULES_SECTION", Boolean.class, resources
        .getString("INCLUDE_RULES_SECTION_TOOLTIP"), Boolean.TRUE, resources
        .getString("INCLUDE_RULES_SECTION"));
  
  /**
   * 
   */
  public static final Option<Boolean> INCLUDE_SPECIES_SECTION = new Option<Boolean>(
    "INCLUDE_SPECIES_SECTION", Boolean.class, resources
        .getString("INCLUDE_SPECIES_SECTION_TOOLTIP"), Boolean.TRUE, resources
        .getString("INCLUDE_SPECIES_SECTION"));
  
  /**
   * 
   */
  public static final Option<Boolean> INCLUDE_UNIT_DEFINITIONS_SECTION = new Option<Boolean>(
    "INCLUDE_UNIT_DEFINITIONS_SECTION", Boolean.class, resources
        .getString("INCLUDE_UNIT_DEFINITIONS_SECTION_TOOLTIP"), Boolean.TRUE,
    resources.getString("INCLUDE_UNIT_DEFINITIONS_SECTION"));
 
  /**
   * Allows to print the entire differential equation system for all species
   * instead of generating links to the reactions.
   */
  public static final Option<Boolean> PRINT_FULL_ODE_SYSTEM = new Option<Boolean>(
    "PRINT_FULL_ODE_SYSTEM", Boolean.class,
    String.format(resources.getString("PRINT_FULL_ODE_SYSTEM_TOOLTIP"),
      INCLUDE_REACTIONS_SECTION), Boolean.FALSE, resources
        .getString("PRINT_FULL_ODE_SYSTEM"), INCLUDE_REACTIONS_SECTION,
    TRUE_CONDITION);
  
  /**
   * If true (default), all predefined unit definitions of SBML are made
   * explicit.
   */
  public static final Option<Boolean> SHOW_PREDEFINED_UNITS = new Option<Boolean>(
    "SHOW_PREDEFINED_UNITS", Boolean.class, String.format(resources
        .getString("SHOW_PREDEFINED_UNITS_TOOLTIP"),
      INCLUDE_UNIT_DEFINITIONS_SECTION), Boolean.TRUE, resources
        .getString("SHOW_PREDEFINED_UNITS"), INCLUDE_UNIT_DEFINITIONS_SECTION,
    TRUE_CONDITION);
    
  /**
   * If true, the details (identifier and name) of all reactants, modifiers and
   * products participating in a reaction are listed in one table. By default a
   * separate table is created for each one of the three participant groups
   * including its SBO term.
   */
  public static final Option<Boolean> REACTANTS_OVERVIEW_TABLE = new Option<Boolean>(
    "REACTANTS_OVERVIEW_TABLE", Boolean.class, String.format(resources
        .getString("REACTANTS_OVERVIEW_TABLE_TOOLTIP"),
      INCLUDE_REACTIONS_SECTION), Boolean.FALSE, resources
        .getString("REACTANTS_OVERVIEW_TABLE"), INCLUDE_REACTIONS_SECTION,
    TRUE_CONDITION);
  
  /**
   * Selects the font to be used for typewriter text. Default: cmt.
   */
  public static final Option<String> FONT_TYPEWRITER = new Option<String>(
    "FONT_TYPEWRITER", String.class, String.format(resources
        .getString("FONT_TYPEWRITER_TOOLTIP"), TYPEWRITER), new Range<String>(
      String.class, "{cmt,courier}"), "cmt", resources
        .getString("FONT_TYPEWRITER"), TYPEWRITER, TRUE_CONDITION);
  
  /**
   * The PDF LaTeX compiler of the system. If this is null, the program pdflatex
   * cannot be found.
   */
  public static final File PDF_LaTeX_COMPILER = FileTools.which("pdflatex");
  
  /**
   * The path to the LaTeX compiler to generate PDF, DVI or other files from the
   * created LaTeX report file.
   */
  public static final Option<File> LOAD_LATEX_COMPILER = new Option<File>(
    "LOAD_LATEX_COMPILER", File.class, resources
        .getString("LOAD_LATEX_COMPILER_TOOLTIP"), new Range<File>(File.class,
      SBFileFilter.createAllFileFilter()),
    PDF_LaTeX_COMPILER == null ? new File(System.getProperty("user.dir"))
        : PDF_LaTeX_COMPILER, resources.getString("LOAD_LATEX_COMPILER"));
  
  /* 
   * =================================================================================
   * OPTION GROUPS
   * =================================================================================
   */
  
  /**
   * Here you can specify also the location of the LaTeX compiler on your
   * operating system. Previously, this method also allowed the user to specify
   * the location of the SBML2LaTeX logo file, but this one is now included in
   * the JAR and will not depend on the user any more.
   */
  @SuppressWarnings("unchecked")
  public static final OptionGroup<?> CONFIGURATION_FILES = new OptionGroup(
    resources.getString("CONFIGURATION_FILES"), resources
        .getString("CONFIGURATION_FILES_TOOLTIP"), LOAD_LATEX_COMPILER);
  

  /**
   * Configure the layout of the LaTeX reports, what to be included, and if to
   * remove temporary files afterwards.
   */
  @SuppressWarnings("unchecked")
  public static final OptionGroup<Boolean> REPORT_OPTIONS = new OptionGroup<Boolean>(
    resources.getString("REPORT_OPTIONS"), resources
        .getString("REPORT_OPTIONS_TOOLTIP"), CHECK_CONSISTENCY,
    MIRIAM_ANNOTATION, SHOW_PREDEFINED_UNITS, PRINT_FULL_ODE_SYSTEM,
    CLEAN_WORKSPACE);
  
  /**
   * These options allow you to influence layout and style of the LaTeX report.
   */
  @SuppressWarnings("unchecked")
  public static final OptionGroup<Boolean> LAYOUT_OPTIONS = new OptionGroup<Boolean>(
    resources.getString("LAYOUT_OPTIONS"), resources
        .getString("LAYOUT_OPTIONS_TOOLTIP"), LANDSCAPE,
    PRINT_NAMES_IF_AVAILABLE, TITLE_PAGE, TYPEWRITER, REACTANTS_OVERVIEW_TABLE);
  
  /**
   * 
   */
  @SuppressWarnings("unchecked")
  public static final OptionGroup<?> TYPOGRAPHICAL_OPTIONS = new OptionGroup(
    resources.getString("TYPOGRAPHICAL_OPTIONS"),
    resources.getString("TYPOGRAPHICAL_OPTIONS_TOOLTIP"),
    FONT_HEADINGS, FONT_SIZE, FONT_TEXT, FONT_TYPEWRITER, PAPER_SIZE);

  /**
   * Select sections to be included in the report.
   */
  @SuppressWarnings("unchecked")
  public static final OptionGroup<Boolean> CONTENT_OPTIONS = new OptionGroup<Boolean>(
    resources.getString("CONTENT_OPTIONS"), resources
        .getString("CONTENT_OPTIONS_TOOLTIP"),
    INCLUDE_COMPARTMENT_TYPES_SECTION, INCLUDE_COMPARTMENTS_SECTION,
    INCLUDE_CONSTRAINTS_SECTION, INCLUDE_EVENTS_SECTION,
    INCLUDE_FUNCTION_DEFINITIONS_SECTION, INCLUDE_INITIAL_ASSIGNMENTS_SECTION,
    INCLUDE_PARAMETERS_SECTION, INCLUDE_REACTIONS_SECTION,
    INCLUDE_RULES_SECTION, INCLUDE_SPECIES_SECTION,
    INCLUDE_UNIT_DEFINITIONS_SECTION);
  
}
