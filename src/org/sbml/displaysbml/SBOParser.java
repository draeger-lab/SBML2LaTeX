/*
    SBML2LaTeX converts SBML files (http://sbml.org) into LaTeX files.
    Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.displaysbml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

/**
 * @author wouamba
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 */
public class SBOParser {

	/**
	 * The SBO definition file to be parsed in OBO format.
	 */
	private static File obo = new File(System.getProperty("user.dir")
			+ System.getProperty("file.separator") + "resources"
			+ System.getProperty("file.separator") + "SBO_OBO.obo");

	/**
	 * A hash to save the SBO name for a given number.
	 */
	private static Hashtable<Integer, String> sboName = new Hashtable<Integer, String>();
	/**
	 * A hash to save the SBO definition for a given number.
	 */
	private static Hashtable<Integer, String> sboDefinition = new Hashtable<Integer, String>();

	/**
	 * Returns the location of the SBO definition file.
	 * 
	 * @return
	 */
	public static String getSBOOboFile() {
		return obo.getAbsolutePath();
	}

	/**
	 * This method allows you to specify the location of the obo file containing
	 * the definitions of all SBO terms.
	 * 
	 * @param sboFilePath
	 *            Example: /home/user/controlledVocabulary/SBO.obo
	 */
	public static void setSBOOboFile(String path) {
		obo = new File(path);
	}

	/**
	 * This method returns by a giving SBO term id the corresponding SBO term
	 * name
	 * 
	 * @param sboTermID
	 * @return SBOTermName
	 * @throws IOException
	 */
	public static String getSBOTermName(int sboTermID) throws IOException {
		Integer id = Integer.valueOf(sboTermID);
		if (sboName.containsKey(id))
			return sboName.get(id);
		String name = "";
		BufferedReader input = new BufferedReader(new FileReader(obo));
		String line = null;
		while ((line = input.readLine()) != null) {
			if (line.equals("") || !line.startsWith("id: SBO:"))
				continue;
			else if (line.startsWith("id: SBO:")) {
				if (Integer.parseInt((line.substring(8, line.length()))) == sboTermID) {
					line = input.readLine();
					name = line.substring(6, line.length());
					break;
				}
			}
		}
		if (name.length() == 0)
			name = "Unknown SBO id " + sboTermID;
		else {
			name.replaceAll("\\\\,", ",").replaceAll("\\\\n", " ").replaceAll(
					"\\\\:", ":").replaceAll("\\\\\"", "\"").trim();
			if (name.endsWith("\"") && !name.startsWith("\""))
				name = name.substring(0, name.length() - 2);
		}
		sboName.put(id, name);
		return name;
	}

	/**
	 * This method returns by a giving SBO term id the corresponding SBO term
	 * definition
	 * 
	 * @param SBOTermID
	 * @return SBOTermDesc
	 */
	public static String getSBOTermDef(int sboTermID) throws IOException {
		Integer id = Integer.valueOf(sboTermID);
		if (sboDefinition.containsKey(id))
			return sboDefinition.get(id);
		String def = "";
		BufferedReader input = new BufferedReader(new FileReader(obo));
		String line = null;
		while ((line = input.readLine()) != null) {
			if (line.equals("") || !line.startsWith("id: SBO:"))
				continue;
			else if (line.startsWith("id: SBO:")) {
				if (Integer.parseInt((line.substring(8, line.length()))) == sboTermID) {
					line = input.readLine();
					line = input.readLine();
					int last = 0;
					for (int k = 0; k < line.length(); k++) {
						last++;
						if (line.charAt(k) == '<' || line.charAt(k) == '[')
							break;
					}
					if (last > 0)
						def = line.substring(6, last - 1);
					break;
				}
			}
		}
		if (def.length() == 0)
			def = "Unknown SBO id " + sboTermID;
		else {
			def = def.replaceAll("\\\\,", ",").replaceAll("\\\\n", " ")
					.replaceAll("\\\\:", ":").replaceAll("\\\\\"", "\"").trim();
			if (def.endsWith("\"") && !def.startsWith("\""))
				def = def.substring(0, def.length() - 2);
		}
		sboDefinition.put(id, def);
		return def;
	}
}
