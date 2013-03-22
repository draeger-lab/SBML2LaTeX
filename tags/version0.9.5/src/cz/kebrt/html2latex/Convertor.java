/*
 * Convertor.java
 */

package cz.kebrt.html2latex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class which converts HTML into LaTeX format. Plain HTML elements are
 * converted using
 * {@link Convertor#commonElementStart(ElementStart) commonElementStart()} and
 * {@link Convertor#commonElementEnd(ElementEnd, ElementStart) commonElementEnd()}
 * methods. Elements requiring special care during the conversion are converted
 * by calling special methods like
 * {@link Convertor#tableRowStart(ElementStart) tableRowStart() }.
 */
class Convertor {

	/** Program configuration. */
	private Configuration _config;
	/** Output file. */
	private File _outputFile;
	/** Output file. */
	private FileWriter _fw;
	/** Output file. */
	private BufferedWriter _writer;
	/** temporary writer necessary to count the columns in a table */
	private BufferedWriter _tmpWriter;
	/** necessary to count the columns in a table */
	private StringWriter _strWriter;
	/** counter for columns in tables */
	private int _LaTeXcols;
	/** the maximal number of columns in a table */
	private int _maxLaTeXcols;
	/**
	 * New line symbol of the operating system.
	 */
	private static final String newLine = System.getProperty("line.separator");

	/**
	 * Counter telling in how many elements with &quot;leaveText&quot; attribute
	 * the parser is.
	 */
	private int _countLeaveTextElements = 0;

	/**
	 * Counter telling in how many elements with &quot;ignoreContent&quot;
	 * attribute the parser is.
	 */
	private int _countIgnoreContentElements = 0;

	/** If table cell is reached is it first table cell? */
	private boolean _firstCell = true;
	/** If table row is reached is it first table row? */
	private boolean _firstRow = true;
	/** Shall border be printed in current table. */
	private boolean _printBorder = false;

	/**
	 * Document's bibliography. <br />
	 * key : bibitem name <br />
	 * value : bibitem description
	 */
	private HashMap<String, String> _biblio = new HashMap<String, String>(10);
	private boolean _LaTeXcolsUnknown = false;

	/**
	 * Opens the output file.
	 *
	 * @param outputFile
	 *            output LaTeX file
	 * @throws FatalErrorException
	 *             when output file can't be opened
	 */
	Convertor(File outputFile) throws FatalErrorException {

		_config = new Configuration();

		try {
			_outputFile = outputFile;
			_fw = new FileWriter(_outputFile);
			_writer = new BufferedWriter(_fw);
		} catch (IOException e) {
			throw new FatalErrorException("Can't open the output file: "
					+ _outputFile.getName());
		}
	}

	Convertor(BufferedWriter bw) throws FatalErrorException {
		_config = new Configuration();
		_writer = new BufferedWriter(bw);
	}

	/**
	 * Closes the output file.
	 */
	public void destroy() {
		try {
			_writer.close();
		} catch (IOException e) {
			System.err.println("Can't close the output file: "
					+ _outputFile.getName());
		}
	}

	/**
	 * Called when HTML start element is reached and special method for the
	 * element doesn't exist.
	 *
	 * @param element
	 *            HTML start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void commonElementStart(ElementStart element) throws IOException,
			NoItemException {

		ElementConfigItem item = _config.getElement(element.getElementName());

		if (item.leaveText())
			++_countLeaveTextElements;
		if (item.ignoreContent())
			++_countIgnoreContentElements;

		String str = item.getStart();
		if (str.equals(""))
			return;

		_writer.write(str);
	}

	/**
	 * Called when HTML end element is reached and special method for the
	 * element doesn't exist.
	 *
	 * @param element
	 *            corresponding end tag
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void commonElementEnd(ElementEnd element, ElementStart es)
			throws IOException, NoItemException {

		ElementConfigItem item = _config.getElement(element.getElementName());

		if (item.leaveText())
			--_countLeaveTextElements;
		if (item.ignoreContent())
			--_countIgnoreContentElements;

		String str = item.getEnd();
		if (str.equals(""))
			return;

		_writer.write(str);
		processAttributes(es);
	}

	/**
	 * Called when text content is reached in the input HTML document.
	 *
	 * @param str
	 *            text content reached
	 * @throws IOException
	 *             when output error occurs
	 */
	public void characters(String str) throws IOException {
		if (_countLeaveTextElements == 0)
			str = str.replace(newLine, "").replace("\t", "");

		if (str.equals("") || str.trim().equals(""))
			return;

		if (_countIgnoreContentElements > 0)
			return;

		if (_countLeaveTextElements == 0)
			str = convertCharEntitites(convertLaTeXSpecialChars(str));
		else
			str = convertCharEntitites(str);
		_writer.write(str);
	}

	/**
	 * Called when comment is reached in the input HTML document.
	 *
	 * @param comment
	 *            comment (without &lt;!-- and --&gt;)
	 * @throws IOException
	 *             when output error occurs
	 */
	public void comment(String comment) throws IOException {
		// is it comment for LaTeX
		if (comment.trim().toLowerCase().startsWith("latex:")) {
			comment = comment.trim();
			comment = comment.substring(6, comment.length());
			_writer.write(comment + newLine);
			return;
		}
		comment = "% " + comment;
		comment = newLine + comment.replace(newLine, newLine + "% ");
		comment += newLine;

		_writer.write(comment);
	}

	/**
	 * Converts LaTeX special characters (ie. '{') to LaTeX commands.
	 *
	 * @param str
	 *            input string
	 * @return converted string
	 */
	private String convertLaTeXSpecialChars(String str) {
		str = str.replace("\\", "@-DOLLAR-\\backslash@-DOLLAR-").replace("&#",
				"&@-HASH-").replace("$", "\\$").replace("#", "\\#").replace(
				"%", "\\%").replace("~", "\\textasciitilde")
				.replace("_", "\\_").replace("^", "\\textasciicircum").replace(
						"{", "\\{").replace("}", "\\}").replace("@-DOLLAR-",
						"$").replace("@-HASH-", "#");

		return str;
	}

	/**
	 * Converts HTML character entities to LaTeX commands.
	 *
	 * @param str
	 *            input string
	 * @return converted string
	 */
	private String convertCharEntitites(String str) {
		StringBuffer entity = new StringBuffer("");

		int len = str.length();
		boolean addToBuffer = false;
		for (int i = 0; i < len; ++i) {
			// new entity started
			if (str.charAt(i) == '&') {
				addToBuffer = true;
				entity.delete(0, entity.length());
				continue;
			}

			if (addToBuffer && (str.charAt(i) == ';')) {
				// find symbol
				try {
					String repl = "";
					boolean ok = true;

					if (entity.charAt(0) == '#') {
						try {
							Integer entityNum;
							if ((entity.charAt(1) == 'x')
									|| entity.charAt(1) == 'X') {
								entityNum = Integer.valueOf(entity.substring(2,
										entity.length()), 16);
							} else {
								entityNum = Integer.valueOf(entity.substring(1,
										entity.length()));
							}
							repl = _config.getChar(entityNum);
						} catch (NumberFormatException ex) {
							System.err.println("Not a number in entity.");
							ok = false;
						}
					} else {
						repl = _config.getChar(entity.toString());
					}
					if (ok) {
						str = str.replace("&" + entity.toString() + ";", repl);
						len = str.length();
						i += repl.length() - (entity.length() + 2);
					}

				} catch (NoItemException e) {
					System.err.println(e.toString());
				}

				addToBuffer = false;
				entity.delete(0, entity.length());
				continue;
			}

			if (addToBuffer) {
				// char c = str.charAt(i);
				entity.append(str.charAt(i));
			}
		}

		return str;
	}

	/**
	 * Processes HTML elements' attributes. "Title" and "cite" attributes are
	 * converted to footnotes.
	 *
	 * @param element
	 *            HTML start tag
	 * @throws IOException
	 *             when output error occurs
	 */
	private void processAttributes(ElementStart element) throws IOException {
		HashMap<String, String> map = element.getAttributes();
		if (element.getElementName().equals("a"))
			return;

		if (map.get("title") != null)
			_writer.write("\\footnote{" + map.get("title") + "}");

		if (map.get("cite") != null)
			_writer.write("\\footnote{" + map.get("cite") + "}");
	}

	/**
	 * Prints CSS style converted to LaTeX command. Called when HTML start
	 * element is reached.
	 *
	 * @param e
	 *            HTML start element
	 * @throws IOException
	 *             when output error occurs
	 */
	public void cssStyleStart(ElementStart e) throws IOException {
		CSSStyle[] styles = findStyles(e);
		for (int i = 0; i < styles.length; ++i) {
			if (styles[i] == null)
				continue;
			if (_config.getMakeCmdsFromCSS())
				_writer.write(_config.getCmdStyleName(styles[i].getName())
						+ "{");
			else
				_writer.write(styles[i].getStart());
		}
	}

	/**
	 * Prints CSS style converted to LaTeX command. Called when HTML end element
	 * is reached.
	 *
	 * @param e
	 *            corresponding HTML start element
	 * @throws IOException
	 *             when output error occurs
	 */
	public void cssStyleEnd(ElementStart e) throws IOException {
		CSSStyle[] styles = findStyles(e);
		for (int i = styles.length - 1; i >= 0; --i) {
			if (styles[i] == null)
				continue;
			if (_config.getMakeCmdsFromCSS())
				_writer.write("}");
			else
				_writer.write(styles[i].getEnd());
		}
	}

	/**
	 * Finds styles for the specified element.
	 *
	 * @param e
	 *            HTML element
	 * @return array with styles in this order: element name style, 'class'
	 *         style, 'id' style (if style not found null is stored in the
	 *         array)
	 */
	private CSSStyle[] findStyles(ElementStart e) {
		try {
			if (_config.getElement(e.getElementName()).ignoreStyles())
				return null;
		} catch (NoItemException ex) {
		}

		String[] styleNames = { e.getElementName(), "", "" };
		CSSStyle[] styles = { null, null, null };
		CSSStyle style;

		if (e.getAttributes().get("class") != null)
			styleNames[1] = e.getAttributes().get("class");

		if (e.getAttributes().get("id") != null)
			styleNames[2] = e.getAttributes().get("id");

		if ((style = _config.findStyle(styleNames[0])) != null)
			styles[0] = style;

		if ((style = _config.findStyleClass(styleNames[1], e.getElementName())) != null)
			styles[1] = style;

		if ((style = _config.findStyleId(styleNames[2], e.getElementName())) != null)
			styles[2] = style;

		return styles;
	}

	/**
	 * Called when A start element is reached.
	 *
	 * @param e
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void anchorStart(ElementStart e) throws IOException, NoItemException {

		String href = e.getAttributes().get("href"), name = e.getAttributes()
				.get("name"), title = e.getAttributes().get("title");
		if (href == null)
			href = "";
		if (name == null)
			name = "";
		if (title == null)
			title = "";

		switch (_config.getLinksConversionType()) {
		case FOOTNOTES:
			break;
		case BIBLIO:
			break;
		case HYPERTEX:
			if (href.startsWith("#")) {
				_writer.write("\\hyperlink{" + href.substring(1, href.length())
						+ "}{");
				break;
			}

			if (!name.equals("")) {
				_writer.write("\\hypertarget{" + name + "}{");
				break;
			}

			if (!href.equals("")) {
				_writer.write("\\href{" + href + "}{");
				break;
			}
		case IGNORE:
			break;
		}
	}

	/**
	 * Called when A end element is reached.
	 *
	 * @param element
	 *            corresponding end tag
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void anchorEnd(ElementEnd element, ElementStart es)
			throws IOException, NoItemException {

		String href = es.getAttributes().get("href"), name = es.getAttributes()
				.get("name"), title = es.getAttributes().get("title");
		if (href == null)
			href = "";
		if (name == null)
			name = "";
		if (title == null)
			title = "";

		switch (_config.getLinksConversionType()) {
		case FOOTNOTES:
			if (href.equals(""))
				return;
			if (href.startsWith("#"))
				return;

			_writer.write("\\footnote{" + es.getAttributes().get("href") + "}");
			break;

		case BIBLIO:
			if (href.equals(""))
				return;
			if (href.startsWith("#"))
				return;

			String key = "",
			value = "";
			if (es.getAttributes().get("name") != null)
				key = es.getAttributes().get("name");
			else
				key = es.getAttributes().get("href");

			value = "\\verb|" + es.getAttributes().get("href") + "|.";
			if (es.getAttributes().get("title") != null)
				value += " " + es.getAttributes().get("title");
			_biblio.put(key, value);

			_writer.write("\\cite{" + key + "}");
			break;

		case HYPERTEX:
			if (!name.equals("")) {
				_writer.write("}");
				break;
			}

			if (href.startsWith("#")) {
				_writer.write("}");
				break;
			}

			if (!href.equals("")) {
				_writer.write("}");
				break;
			}
			break;

		case IGNORE:
			break;
		}
	}

	/**
	 * Called when TR start element is reached.
	 *
	 * @param e
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void tableRowStart(ElementStart e) throws IOException,
			NoItemException {
		if (_firstRow && _LaTeXcolsUnknown) {
			_tmpWriter = _writer;
			_strWriter = new StringWriter();
			_writer = new BufferedWriter(_strWriter);
			_tmpWriter.append('{');
			_LaTeXcols = _maxLaTeXcols = 0;
		}
		// if (!_firstRow && !_printBorder) {
		// _writer.write("\\\\");
		// _writer.write(newLine);
		// }
	}

	/**
	 * Called when TR end element is reached.
	 *
	 * @param e
	 *            corresponding end tag
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 */
	public void tableRowEnd(ElementEnd e, ElementStart es) throws IOException {
		if ((_LaTeXcolsUnknown) && (_maxLaTeXcols < _LaTeXcols))
			_maxLaTeXcols = _LaTeXcols;
		_LaTeXcols = 0;
		_firstCell = true;
		if (_firstRow) {
			_firstRow = false;
			_writer.write(" \\\\" + newLine + "\\midrule" + newLine);
		} else if (_printBorder) {
			_writer.write(" \\\\" + newLine + "\\hline" + newLine);
		} else
			_writer.write(" \\\\" + newLine);
	}

	/**
	 * Called when TD start element is reached.
	 *
	 * @param e
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void tableCellStart(ElementStart e) throws IOException,
			NoItemException {
		if (_LaTeXcolsUnknown)
			_LaTeXcols++;
		if (!_firstCell)
			_writer.write(" & ");
		else
			_firstCell = false;
		_writer.write(_config.getElement(e.getElementName()).getStart());
	}

	/**
	 * Called when TD end element is reached.
	 *
	 * @param element
	 *            corresponding end tag
	 * @param e
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void tableCellEnd(ElementEnd element, ElementStart e)
			throws IOException, NoItemException {

		_writer.write(_config.getElement(e.getElementName()).getEnd());
	}

	/**
	 * Called when TABLE start element is reached.
	 *
	 * @param e
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void tableStart(ElementStart e) throws IOException, NoItemException {
		_firstRow = true;
		_writer.write(_config.getElement(e.getElementName()).getStart());
		String str;

		if ((str = e.getAttributes().get("latexcols")) != null)
			_writer.write("{" + str + "}" + newLine);
		else
			_LaTeXcolsUnknown = true;

		if ((str = e.getAttributes().get("border")) != null)
			if (!str.equals("0"))
				_printBorder = true;

		// if (_printBorder)
		// _writer.write("\\toprule" + newLine); // ("\\hline \n");
	}

	/**
	 * Called when TABLE end element is reached.
	 *
	 * @param e
	 *            corresponding end tag
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void tableEnd(ElementEnd e, ElementStart es) throws IOException,
			NoItemException {
		if (_LaTeXcolsUnknown) {
			if (_maxLaTeXcols < _LaTeXcols)
				_maxLaTeXcols = _LaTeXcols;
			double width = 0.9 / Math.max(1, _maxLaTeXcols);
			if (_printBorder)
				_tmpWriter.append('|');
			for (int i = 0; i < _maxLaTeXcols; i++) {
				_tmpWriter.append(width > 0.1 ? "p{" + width + "\\textwidth}"
						: "l");
				if (_printBorder)
					_tmpWriter.append('|');
			}
			_tmpWriter.append('}');
			_tmpWriter.newLine();
			// if (_printBorder) {
			_tmpWriter.append("\\toprule");
			_tmpWriter.newLine();
			// }
			_writer.close();
			_strWriter.close();
			if (_printBorder)
				_tmpWriter.append(_strWriter.getBuffer().substring(0,
						_strWriter.getBuffer().lastIndexOf("\\hline")));
			else
				_tmpWriter.append(_strWriter.getBuffer());
			_writer = _tmpWriter;
			_writer.append("\\bottomrule");
		}
		_writer.write(_config.getElement(e.getElementName()).getEnd());
	}

	/**
	 * Called when BODY start element is reached.
	 *
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void bodyStart(ElementStart es) throws IOException, NoItemException {
		if (_config.getLinksConversionType() == LinksConversion.HYPERTEX)
			_writer.write(newLine + "\\usepackage{hyperref}");

		if (_config.getMakeCmdsFromCSS())
			_writer.write(_config.makeCmdsFromCSS());

		_writer.write(_config.getElement(es.getElementName()).getStart());
	}

	/**
	 * Called when IMG start element is reached.
	 *
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void imgStart(ElementStart es) throws IOException, NoItemException {

		_writer.write(newLine + "\\includegraphics{"
				+ es.getAttributes().get("src") + "}");
	}

	/**
	 * Called when META start element is reached. Recognizes basic charsets
	 * (cp1250, utf8, latin2)
	 *
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void metaStart(ElementStart es) throws IOException, NoItemException {

		String str, str2 = "";
		if ((str = es.getAttributes().get("http-equiv")) != null) {
			if ((str.compareToIgnoreCase("content-type") == 0)
					&& ((str2 = es.getAttributes().get("content")) != null)) {

				str2 = str2.toLowerCase();
				if (str2.contains("windows-1250"))
					_writer.write(newLine + "\\usepackage[cp1250]{inputenc}");
				else if (str2.contains("iso-8859-2"))
					_writer.write(newLine + "\\usepackage[latin2]{inputenc}");
				else if (str2.contains("utf-8"))
					_writer.write(newLine + "\\usepackage[utf8]{inputenc}");
			}
		}
	}

	/**
	 * Called when FONT start element is reached.
	 *
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void fontStart(ElementStart es) throws IOException, NoItemException {

		if (es.getAttributes().get("size") != null) {
			String command;
			try {
				Integer size = Integer.valueOf(es.getAttributes().get("size"));
				switch (size) {
				case 1:
					command = "{\\tiny";
					break;
				case 2:
					command = "{\\footnotesize";
					break;
				case 3:
					command = "{\\normalsize";
					break;
				case 4:
					command = "{\\large";
					break;
				case 5:
					command = "{\\Large";
					break;
				case 6:
					command = "{\\LARGE";
					break;
				case 7:
					command = "{\\Huge";
					break;
				default:
					command = "{\\normalsize";
					break;
				}
			} catch (NumberFormatException ex) {
				command = "{\\normalsize";
			}

			_writer.write(command + " ");
		}
	}

	/**
	 * Called when FONT end element is reached.
	 *
	 * @param e
	 *            corresponding end tag
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void fontEnd(ElementEnd e, ElementStart es) throws IOException,
			NoItemException {

		if (es.getAttributes().get("size") != null) {
			_writer.write("}");
		}
	}

	/**
	 * Called when end element is reached.
	 *
	 * @param element
	 *            corresponding end tag
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void bodyEnd(ElementEnd element, ElementStart es)
			throws IOException, NoItemException {

		if (!_biblio.isEmpty()) {
			_writer.write(newLine + "\\begin{thebibliography}{"
					+ _biblio.size() + "}" + newLine);
			for (Iterator<Map.Entry<String, String>> iterator = _biblio
					.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, String> entry = iterator.next();
				String key = entry.getKey();
				String value = entry.getValue();
				_writer.write("\t\\bibitem{" + key + "}" + value + newLine);
			}
			_writer.write("\\end{thebibliography}");
		}
		commonElementEnd(element, es);
	}

}
