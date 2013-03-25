/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates 
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2007-2013 by the University of Tuebingen, Germany.
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

import de.zbit.io.FileTools;
import de.zbit.io.filefilter.SBFileFilter;
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
 * @version $Rev$
 */
public interface LaTeXOptions extends KeyProvider {
  
	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 * @since 1.0
	 */
	public static enum SansSerifFont {
		avant, cmss, helvetica;
	}
	
	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 * @since 1.0
	 */
	public static enum SerifFont {
		chancery, charter, cmr, mathptmx, palatino, times, utopia;
	}

	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 * @since 1.0
	 */
	public static enum PaperSize {
		letter, legal, executive, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, d0, d1, d2, d3, d4, d5, d6, d7, d8, d9
	}
	
  /**
   * Helper constant.
   */
  static final Range<Boolean> TRUE_CONDITION = new Range<Boolean>(
    Boolean.class, Boolean.TRUE);
  
  /**
   * Localization support.
   */
  static final ResourceBundle resources = ResourceManager.getBundle("org.sbml.tolatex.locales.UI");
  
  /**
   * If {@code true}, the model consistency check is performed and the results
   * are written in the glossary of the model report.
   */
  public static final Option<Boolean> CHECK_CONSISTENCY = new Option<Boolean>(
    "CHECK_CONSISTENCY", Boolean.class, resources, Boolean.FALSE);
  
  /**
   * If this option is set to {@code true}, all temporary files will be deleted after
   * running SBML2LaTeX. In case of PDF creation, for instance, this will cause
   * even the TeX file to be deleted. However, this option can be meaningful to
   * remove all the temporary files created by your system's LaTeX compiler.
   */
  public static final Option<Boolean> CLEAN_WORKSPACE = new Option<Boolean>(
    "CLEAN_WORKSPACE", Boolean.class, resources, Boolean.FALSE);
  
  /**
   * Allows to select the font of captions and other sans serif text. Default:
   * {@link SansSerifFont#helvetica}
   */
  public static final Option<SansSerifFont> FONT_HEADINGS = new Option<SansSerifFont>(
    "FONT_HEADINGS", SansSerifFont.class, resources, SansSerifFont.helvetica);
  
  /**
   * The font size for LaTeX documents.
   */
  public static final Option<Short> FONT_SIZE = new Option<Short>("FONT_SIZE",
    Short.class, resources, new Range<Short>(Short.class,
      "{8,9,10,11,12,14,17}"), Short.valueOf((short) 11));
  
	/**
	 * Allows to select the font of continuous text. Choosing 'times' is actually
	 * not recommended because in some cases equations might not look as nicely as
	 * they do when using 'mathptmx'. Default: Times font
	 * {@link SerifFont#mathptmx}.
	 */
  public static final Option<SerifFont> FONT_TEXT = new Option<SerifFont>(
    "FONT_TEXT", SerifFont.class, resources, SerifFont.mathptmx);
  
  /**
   * Decides whether to set the LaTeX document in landscape or portrait mode.
   */
  public static final Option<Boolean> LANDSCAPE = new Option<Boolean>(
    "LANDSCAPE", Boolean.class, resources, Boolean.FALSE);
  
  /**
   * If {@code true} (default), MIRIAM annotations are included into the model report if
   * there are any. This option may require the path to the MIRIAM translation
   * file to be specified.
   */
  public static final Option<Boolean> MIRIAM_ANNOTATION = new Option<Boolean>(
    "MIRIAM_ANNOTATION", Boolean.class, resources, Boolean.TRUE);
  
  /**
   * The paper size for LaTeX documents. Default: {@link PaperSize#letter}
   */
  public static final Option<PaperSize> PAPER_SIZE = new Option<PaperSize>(
    "PAPER_SIZE", PaperSize.class, resources, PaperSize.letter);
  
  /**
   * Decides whether to write the names or the identifiers of NamedSBase object
   * in equations.
   */
  public static final Option<Boolean> PRINT_NAMES_IF_AVAILABLE = new Option<Boolean>(
    "PRINT_NAMES_IF_AVAILABLE", Boolean.class, resources, Boolean.FALSE);
  
  /**
   * Decides whether to create a separate title page instead of a simple
   * heading.
   */
  public static final Option<Boolean> TITLE_PAGE = new Option<Boolean>(
    "TITLE_PAGE", Boolean.class, resources, Boolean.FALSE);
  
  /**
   * Key that decides whether or not identifiers should be written in typewriter
   * font when these occur in mathematical equations.
   */
  public static final Option<Boolean> TYPEWRITER = new Option<Boolean>(
    "TYPEWRITER", Boolean.class, resources, Boolean.TRUE);
  
  /**
   * This option decides whether or not a section about compartments should be
   * included in the resulting model report. Note that this option only causes
   * an effect if the model contains compartment declarations.
   */
  public static final Option<Boolean> INCLUDE_SECTION_COMPARTMENTS = new Option<Boolean>(
    "INCLUDE_SECTION_COMPARTMENTS", Boolean.class, resources, Boolean.TRUE);
  
  /**
   * This option decides whether or not a section about compartment types should
   * be included in the resulting model report. Note that this option only
   * causes an effect if the model contains compartment type declarations.
   */
  public static final Option<Boolean> INCLUDE_SECTION_COMPARTMENT_TYPES = new Option<Boolean>(
    "INCLUDE_SECTION_COMPARTMENT_TYPES", Boolean.class, resources, Boolean.TRUE);
  
  /**
   * This option decides whether or not a section about compartments should be
   * included in the resulting model report. Note that this option only causes
   * an effect if the model contains compartment declarations.
   */
  public static final Option<Boolean> INCLUDE_SECTION_CONSTRAINTS = new Option<Boolean>(
    "INCLUDE_SECTION_CONSTRAINTS", Boolean.class, resources, Boolean.TRUE);
  
  /**
   * This option decides whether or not a section about events should be
   * included in the resulting model report. Note that this option only causes
   * an effect if the model contains event declarations.
   */
  public static final Option<Boolean> INCLUDE_SECTION_EVENTS = new Option<Boolean>(
    "INCLUDE_SECTION_EVENTS", Boolean.class, resources, Boolean.TRUE);
  
  /**
   * This option decides whether or not a section about initial assignments
   * should be included in the resulting model report. Note that this option
   * only causes an effect if the model declares any initial assignments.
   */
  public static final Option<Boolean> INCLUDE_SECTION_INITIAL_ASSIGNMENTS = new Option<Boolean>(
    "INCLUDE_SECTION_INITIAL_ASSIGNMENTS", Boolean.class, resources,
    Boolean.TRUE);
  
  /**
   * This option decides whether or not a section about function definitions
   * should be included in the resulting model report. Note that this option
   * only causes an effect if the model declares any function definitions.
   */
  public static final Option<Boolean> INCLUDE_SECTION_FUNCTION_DEFINITIONS = new Option<Boolean>(
    "INCLUDE_SECTION_FUNCTION_DEFINITIONS", Boolean.class, resources,
    Boolean.TRUE);
  
  /**
   * This option decides whether or not a section about reactions should be
   * included in the resulting model report. Note that this option only causes
   * an effect if the model declares any reactions. Furthermore, this option
   * also decides if a summary of the differential equation system that is
   * implied by the given model should be generated. Again, this will only cause
   * an effect if the model contains any species.
   */
  public static final Option<Boolean> INCLUDE_SECTION_REACTIONS = new Option<Boolean>(
    "INCLUDE_SECTION_REACTIONS", Boolean.class, resources, Boolean.TRUE);
  
  /**
   * This option decides whether or not a section about parameters should be
   * included in the resulting model report. Note that this option only causes
   * an effect if the model declares any parameters.
   */
  public static final Option<Boolean> INCLUDE_SECTION_PARAMETERS = new Option<Boolean>(
    "INCLUDE_SECTION_PARAMETERS", Boolean.class, resources, Boolean.TRUE);
  
  /**
   * This option decides whether or not a section about rules should be included
   * in the resulting model report. Note that this option only causes an effect
   * if the model declares any rules, no matter if these are of algebraic,
   * assignment or rate rule type.
   */
  public static final Option<Boolean> INCLUDE_SECTION_RULES = new Option<Boolean>(
    "INCLUDE_SECTION_RULES", Boolean.class, resources, Boolean.TRUE);
  
  /**
   * This option decides whether or not a section about the species in the given
   * model should be included in the resulting model report. Note that this
   * option only causes an effect if the model declares any species.
   */
  public static final Option<Boolean> INCLUDE_SECTION_SPECIES = new Option<Boolean>(
    "INCLUDE_SECTION_SPECIES", Boolean.class, resources, Boolean.TRUE);
  
  /**
   * If this option is selected, a section about species types will occur in the
   * model report. Otherwise, this section will be excluded from the report.
   */
  public static final Option<Boolean> INCLUDE_SECTION_SPECIES_TYPES = new Option<Boolean>(
      "INCLUDE_SECTION_SPECIES_TYPES", Boolean.class, resources, Boolean.TRUE);
  
  /**
   * This option decides whether or not a section about the unit definitions of
   * the given model should be included in the resulting model report. Note that
   * this option only causes an effect if the model declares any unit
   * definitions. However, in some level/version combinations SBML models
   * contain predefined unit definitions which might be included in the model
   * report if this option is active.
   */
  public static final Option<Boolean> INCLUDE_SECTION_UNIT_DEFINITIONS = new Option<Boolean>(
    "INCLUDE_SECTION_UNIT_DEFINITIONS", Boolean.class, resources, Boolean.TRUE);
  
  /**
   * 
   */
  public static final Option<Boolean> INCLUDE_SECTION_LAYOUTS = new Option<Boolean>(
  		"INCLUDE_SECTION_LAYOUTS", Boolean.class, resources, Boolean.TRUE);
 
  /**
   * Allows to print the entire differential equation system for all species
   * instead of generating links to the reactions.
   */
  public static final Option<Boolean> PRINT_FULL_ODE_SYSTEM = new Option<Boolean>(
    "PRINT_FULL_ODE_SYSTEM", Boolean.class,
    String.format(resources.getString("PRINT_FULL_ODE_SYSTEM_TOOLTIP"),
      INCLUDE_SECTION_REACTIONS), Boolean.FALSE, resources
        .getString("PRINT_FULL_ODE_SYSTEM"), INCLUDE_SECTION_REACTIONS,
    TRUE_CONDITION);
  
  /**
   * If {@code true} (default), all predefined unit definitions of SBML are made
   * explicit.
   */
  public static final Option<Boolean> SHOW_PREDEFINED_UNITS = new Option<Boolean>(
    "SHOW_PREDEFINED_UNITS", Boolean.class, String.format(resources
        .getString("SHOW_PREDEFINED_UNITS_TOOLTIP"),
      INCLUDE_SECTION_UNIT_DEFINITIONS), Boolean.TRUE, resources
        .getString("SHOW_PREDEFINED_UNITS"), INCLUDE_SECTION_UNIT_DEFINITIONS,
    TRUE_CONDITION);
    
  /**
   * If {@code true}, the details (identifier and name) of all reactants, modifiers and
   * products participating in a reaction are listed in one table. By default a
   * separate table is created for each one of the three participant groups
   * including its SBO term.
   */
  public static final Option<Boolean> REACTANTS_OVERVIEW_TABLE = new Option<Boolean>(
    "REACTANTS_OVERVIEW_TABLE", Boolean.class, String.format(resources
        .getString("REACTANTS_OVERVIEW_TABLE_TOOLTIP"),
      INCLUDE_SECTION_REACTIONS), Boolean.FALSE, resources
        .getString("REACTANTS_OVERVIEW_TABLE"), INCLUDE_SECTION_REACTIONS,
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
    "LOAD_LATEX_COMPILER", File.class, resources, new Range<File>(File.class,
      SBFileFilter.createAllFileFilter()),
    PDF_LaTeX_COMPILER == null ? new File(System.getProperty("user.dir"))
        : PDF_LaTeX_COMPILER);

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
  public static final OptionGroup<File> CONFIGURATION_FILES = new OptionGroup<File>(
    "CONFIGURATION_FILES", resources, LOAD_LATEX_COMPILER);
  

  /**
   * Configure the layout of the LaTeX reports, what to be included, and if to
   * remove temporary files afterwards.
   */
  @SuppressWarnings("unchecked")
  public static final OptionGroup<Boolean> REPORT_OPTIONS = new OptionGroup<Boolean>(
    "REPORT_OPTIONS", resources, CHECK_CONSISTENCY, MIRIAM_ANNOTATION,
    SHOW_PREDEFINED_UNITS, PRINT_FULL_ODE_SYSTEM, CLEAN_WORKSPACE);
  
  /**
   * These options allow you to influence layout and style of the LaTeX report.
   */
  @SuppressWarnings("unchecked")
  public static final OptionGroup<Boolean> LAYOUT_OPTIONS = new OptionGroup<Boolean>(
    "LAYOUT_OPTIONS", resources, LANDSCAPE, PRINT_NAMES_IF_AVAILABLE,
    TITLE_PAGE, TYPEWRITER, REACTANTS_OVERVIEW_TABLE);
  
  /**
   * Here you can specify general properties such as paper size and font styles.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final OptionGroup<?> TYPOGRAPHICAL_OPTIONS = new OptionGroup(
    "TYPOGRAPHICAL_OPTIONS", resources, FONT_HEADINGS, FONT_SIZE, FONT_TEXT,
    FONT_TYPEWRITER, PAPER_SIZE);

  /**
   * Select sections to be included in the report.
   */
  @SuppressWarnings("unchecked")
  public static final OptionGroup<Boolean> CONTENT_OPTIONS = new OptionGroup<Boolean>(
    "CONTENT_OPTIONS", resources, INCLUDE_SECTION_COMPARTMENT_TYPES,
    INCLUDE_SECTION_COMPARTMENTS, INCLUDE_SECTION_CONSTRAINTS,
    INCLUDE_SECTION_EVENTS, INCLUDE_SECTION_FUNCTION_DEFINITIONS,
    INCLUDE_SECTION_INITIAL_ASSIGNMENTS, INCLUDE_SECTION_PARAMETERS,
    INCLUDE_SECTION_REACTIONS, INCLUDE_SECTION_RULES, INCLUDE_SECTION_SPECIES,
    INCLUDE_SECTION_SPECIES_TYPES, INCLUDE_SECTION_UNIT_DEFINITIONS);
  
}
