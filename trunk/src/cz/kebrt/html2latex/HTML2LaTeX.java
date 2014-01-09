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
package cz.kebrt.html2latex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import de.zbit.io.OpenFile;

/**
 * Program main class.
 * @version $Rev$
 * @since 0.9.3
 */
public class HTML2LaTeX {

	/** Configuration file. */
	private static String _configFile = "config.xml";
	/** File with CSS. */
	private static String _cssFile = "";

	public HTML2LaTeX(BufferedReader br, BufferedWriter bw) throws IOException {
		try {
			Parser parser = new Parser();
			parser.parse(br, new ParserHandler(bw));
		} catch (FatalErrorException exc) {
			throw new IOException(exc);
		}
	}

	/**
	 * Returns name of the file with CSS.
	 * 
	 * @return name of the file with CSS
	 * @throws URISyntaxException 
	 */
	public static File getCSSFile() throws URISyntaxException {
		return OpenFile.searchFile(_cssFile);
	}

	/**
	 * Returns name of the file with configuration.
	 * 
	 * @return name of the file with configuration
	 * @throws URISyntaxException 
	 */
	public static File getConfigFile() throws URISyntaxException {
		return OpenFile.searchFile(_configFile);
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isSetCSSFile() {
		return (_cssFile != null) && !_cssFile.equals("");
	}

}
