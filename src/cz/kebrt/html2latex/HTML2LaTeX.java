/*
 * Main.java
 */

package cz.kebrt.html2latex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

/**
 * Program main class.
 */
public class HTML2LaTeX {

	private static final String fileSeparator = System
			.getProperty("file.separator");
	/** Input HTML file. */
	private static String _inputFile = "";
	/** Output LaTeX file. */
	private static String _outputFile = "";
	/** Configuration file. */
	private static String _configFile = System.getProperty("user.dir")
			+ fileSeparator + "resources" + fileSeparator + "config.xml";
	/** File with CSS. */
	private static String _cssFile = "";

	/**
	 * Creates {@link Parser Parser} instance and runs its
	 * {@link Parser#parse(File, ParserHandler) parse()} method.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		try {
			processCmdLineArgs(args);

			if (_inputFile.equals("") || _outputFile.equals("")) {
				System.err.println("Input or (and) output file not specified.");
				return;
			}

			Parser parser = new Parser();
			parser.parse(new File(_inputFile), new ParserHandler(new File(
					_outputFile)));

		} catch (FatalErrorException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

	public static void convert(BufferedReader br, BufferedWriter bw)
			throws FatalErrorException {
		convert(br, bw, "", _configFile);
	}

	public static void convert(BufferedReader br, BufferedWriter bw,
			String pathToCSSFile, String pathToConfigFile)
			throws FatalErrorException {
		_cssFile = pathToCSSFile;
		_configFile = pathToConfigFile;
		Parser parser = new Parser();
		parser.parse(br, new ParserHandler(bw));
	}

	/**
	 * Processes command line arguments.
	 * <ul>
	 * <li>-input &lt;fileName&gt;</li>
	 * <li>-output &lt;fileName&gt;</li>
	 * <li>-config &lt;fileName&gt;</li>
	 * <li>-css &lt;fileName&gt;</li>
	 * </ul>
	 * 
	 * @param args
	 *            command line arguments
	 */
	private static void processCmdLineArgs(String[] args) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("-input")) {
				if (i < (args.length - 1)) {
					_inputFile = args[i + 1];
					++i;
				}
			}

			if (args[i].equals("-output")) {
				if (i < (args.length - 1)) {
					_outputFile = args[i + 1];
					++i;
				}
			}

			if (args[i].equals("-config")) {
				if (i < (args.length - 1)) {
					_configFile = args[i + 1];
					++i;
				}
			}

			if (args[i].equals("-css")) {
				if (i < (args.length - 1)) {
					_cssFile = args[i + 1];
					++i;
				}
			}
		}
	}

	/**
	 * Returns name of the file with CSS.
	 * 
	 * @return name of the file with CSS
	 */
	public static String getCSSFile() {
		return _cssFile;
	}

	/**
	 * Returns name of the file with configuration.
	 * 
	 * @return name of the file with configuration
	 */
	public static String getConfigFile() {
		return _configFile;
	}

	/**
	 * Set the configuration file to the given path.
	 * 
	 * @param path
	 */
	public static void setConfigFile(String path) {
		_configFile = path;
	}

}
