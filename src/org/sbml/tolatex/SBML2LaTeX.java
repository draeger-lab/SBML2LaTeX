/**
 * 
 */
package org.sbml.tolatex;

import java.io.IOException;
import java.util.prefs.BackingStoreException;

import org.jdom.JDOMException;
import org.sbml.tolatex.io.MIRIAMparser;

import de.zbit.util.prefs.SBPreferences;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 */
public class SBML2LaTeX {
	/**
     * 
     */
	private static final MIRIAMparser miriam = new MIRIAMparser();

	/**
	 * @return the miriam parser.
	 * @throws Exception
	 */
	public static MIRIAMparser getMIRIAMparser() throws Exception {
		if (!miriam.isSetDocument()) {
			try {
				miriam.setMIRIAMDocument(SBML2LaTeX.class
						.getResourceAsStream("cfg/MIRIAM.xml"));
			} catch (JDOMException exc) {
				throw new Exception(exc);
			}
		}
		return miriam;
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws BackingStoreException
	 */
	public static void main(String[] args) throws IOException,
			BackingStoreException {
		SBPreferences.analyzeCommandLineArguments(LaTeXOptions.class,
				LaTeXOptions.CONFIG_FILE_LOCATION, true,
				"java SBML2LaTeX [options]", args);
		// TODO: Execute something!
	}
}
