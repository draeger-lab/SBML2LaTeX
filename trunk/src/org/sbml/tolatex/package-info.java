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

/**
 * This package contains parsers for MIRIAM and SBO files, helper classes for
 * LaTeX code writing and GUI layout setup, string operations, an interface for
 * general conversion of SBML into some human-readable report and the actual
 * converter SBML2LaTeX. The class SBMLconverter provides access to the main
 * functions whereas SBML2LaTeX already requires elements defined by libSBML.
 * The whole package depends on libSBML 3.3.1 or later.
 */
package org.sbml.tolatex;
