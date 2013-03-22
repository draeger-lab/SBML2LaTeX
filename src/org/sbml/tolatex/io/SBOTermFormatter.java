/*
 * $Id: SBOTermFormatter.java 60 2011-03-07 17:20:39Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/tags/version0.9.8/src/org/sbml/tolatex/io/SBOTermFormatter.java $
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates 
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2008-2011 by the University of Tuebingen, Germany.
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

import org.sbml.jsbml.SBO.Term;
import org.sbml.jsbml.util.StringTools;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-12-14
 * @version $Rev: 60 $
 */
public class SBOTermFormatter {

    /**
     * @param term
     * @return
     */
    public static String getShortDefinition(Term term) {
	String def = term.getDefinition();
	String definition = def.toString().replace("\\, ", ", ");
	if (definition.startsWith("\"")) {
	    definition = definition.substring(1);
	}
	int pos = definition.length() - 1;
	String endWords[] = new String[] { "\n", "xmlns=", "[", "\"" };
	for (String word : endWords) {
	    int end = definition.indexOf(word);
	    if (0 < end && end < pos) {
		pos = end;
	    }
	}
	if (pos > 0) {
	    definition = definition.subSequence(0, pos).toString();
	}
	return StringTools.firstLetterUpperCase(definition.trim());
    }

}
