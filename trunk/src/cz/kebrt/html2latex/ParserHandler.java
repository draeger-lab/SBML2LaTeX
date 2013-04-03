/*
 * ParserHandler.java
 */

package cz.kebrt.html2latex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Handles events sent from the {@link Parser Parser} class.
 * Calls appropriate methods from the {@link Convertor Convertor} class.
 * @version $Rev$
 * @since 0.9.3
 */
class ParserHandler implements IParserHandler {
	
	/**
	 * A {@link Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(ParserHandler.class.getName());
	
	/** Convertor. */
	private Convertor _conv;
	
	/**
	 * Cstr.
	 * @param outputFile output LaTeX file
	 * @throws FatalErrorException fatal error (ie. output file can't be closed) occurs
	 */
	ParserHandler(File outputFile) throws FatalErrorException {
		_conv = new Convertor(outputFile);
	}
	
	ParserHandler(BufferedWriter bw) throws FatalErrorException {
		_conv = new Convertor(bw);
	}
	
	
	/**
	 * Called when a start element is reached in the input document.
	 * Calls {@link Convertor#commonElementStart(ElementStart) commonElementStart()}
	 * for non-special elements and specials methods for the elements requiring
	 * special care (ie. {@link Convertor#tableRowStart(ElementStart) tableRowStart()}
	 * for {@code &lt;table&gt;</table>)
	 * @param element start element reached
	 */
	public void startElement(ElementStart element) {
		try {
			String name = element.getElementName();
			
			if (name.equals("a")) _conv.anchorStart(element);
			else if (name.equals("tr")) _conv.tableRowStart(element);
			else if (name.equals("td")) _conv.tableCellStart(element);
			else if (name.equals("th")) _conv.tableCellStart(element);
			else if (name.equals("meta")) _conv.metaStart(element);
			else if (name.equals("body")) _conv.bodyStart(element);
			else if (name.equals("font")) _conv.fontStart(element);
			else if (name.equals("img")) _conv.imgStart(element);
			else if (name.equals("table")) _conv.tableStart(element);
			else _conv.commonElementStart(element);
			
			_conv.cssStyleStart(element);
		} catch (IOException e) {
			logger.warning("Can't write into output file");
		} catch (NoItemException e) {
			logger.warning(e.toString());
			//e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Called when an end element is reached in the input document.
	 * Calls {@link Convertor#commonElementEnd(ElementEnd, ElementStart) commonElementEnd()}
	 * for non-special elements and specials methods for the elements requiring
	 * special care (ie. {@link Convertor#tableRowEnd(ElementEnd, ElementStart) tableRowEnd()}
	 * for {@code &lt;/table&gt;</table>)
	 * @param element end element reached
	 * @param elementStart corresponding start element
	 */
	public void endElement(ElementEnd element, ElementStart elementStart) {
		try {
			String name = element.getElementName();
			
			_conv.cssStyleEnd(elementStart);
			
			if (name.equals("a")) _conv.anchorEnd(element, elementStart);
			else if (name.equals("tr")) _conv.tableRowEnd(element, elementStart);
			else if (name.equals("th")) _conv.tableCellEnd(element, elementStart);
			else if (name.equals("td")) _conv.tableCellEnd(element, elementStart);
			else if (name.equals("table")) _conv.tableEnd(element, elementStart);
			else if (name.equals("body")) _conv.bodyEnd(element, elementStart);
			else if (name.equals("font")) _conv.fontEnd(element, elementStart);
			else _conv.commonElementEnd(element, elementStart);
			
		} catch (IOException e) {
			logger.warning("Can't write into output file.");
		} catch (NoItemException e) {
			logger.warning(e.getMessage());
		}
	}
	
	
	/**
	 * Called when the text content of an element is read.
	 * Calls {@link Convertor#characters(String) characters()} method
	 * of the {@link Convertor Convertor} class.
	 * @param content ie. &quot;foo&quot; for the &quot;&lt;b&gt;foo&lt;/b&gt;&quot;
	 */
	public void characters(String content) {
		try {
			_conv.characters(content);
		} catch (IOException e) {
			logger.warning("Can't write into output file.");
		}
	}
	
	
	/**
	 * Called when the comment is reached in input document.
	 * Calls {@link Convertor#comment(String) comment()} method
	 * of the {@link Convertor Convertor} class.
	 * @param comment ie. &quot;foo&quot; for the &quot;&lt;!--&gt;foo&lt;/--&gt;&quot;
	 */
	public void comment(String comment) {
		try {
			_conv.comment(comment);
		} catch (IOException e) {
			logger.warning("Can't write into output file.");
		}
	}
	
	
	/**
	 * Called when the whole input document is read.
	 * Calls {@link Convertor#destroy() destroy()} method
	 * of the {@link Convertor Convertor} class.
	 */
	public void endDocument() {
		_conv.destroy();
	}
	
}
