/*
 * CSSParser.java
 */
package cz.kebrt.html2latex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.zbit.util.ResourceManager;

/**
 *  CSS parser.
 *  @version $Rev$
 *  @since 0.9.3
 */
public class CSSParser {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(CSSParser.class.getName());
  
  /**
   * Localization support.
   */
  private static final ResourceBundle bundle = ResourceManager.getBundle("cz.kebrt.html2latex.messages");
  
  /** Input file. */
  private InputStream _stream;
  /** Input file. */
  private Reader _sr;
  /** Input file. */
  private BufferedReader _reader;
  /** Handler which receives events from the parser. */
  private ICSSParserHandler _handler;
  
  /** Parser the CSS file and sends events to the handler.
   *  @param stream CSS file stream
   *  @param handler handler receiving events
   */
  public void parse(InputStream stream, ICSSParserHandler handler) {
    _stream = stream;
    _handler = handler;
    
    try {
      init();
      
      try {
        doParsing();
      } catch (IOException e) {
        logger.warning(bundle.getString("CANNOT_FIND_CSS_FILE"));
      }
      
      destroy();
    } catch (ErrorException e) {
      logger.warning(e.getMessage());
    }
  }
  
  
  /**
   *  Opens the file specified in the
   *  {@link CSSParser#parse(File, ICSSParserHandler) parse()} method.
   *  @throws ErrorException when the file can't be opened
   */
  private void init() throws ErrorException {
    _sr = new InputStreamReader(_stream);
    _reader = new BufferedReader(_sr);
  }
  
  
  /**
   *  Closes the file specified in the
   *  {@link CSSParser#parse(File, ICSSParserHandler) parse()} method.
   *  @throws ErrorException when the file can't be closed
   */
  private void destroy() throws ErrorException {
    if (_sr != null) {
      try {
        _sr.close();
      } catch (IOException e) {
        logger.warning(bundle.getString("CANNOT_FIND_CSS_FILE"));
      }
    }
  }
  
  
  /**
   * Parses the CSS file.
   * It skips CSS comments and reads each style separately.
   * When the style is read (ie. {@code ".bold { font-weight:bold; } })
   * {@link CSSParser#parseStyle(String) parseStyle()} method is called.
   * @throws IOException input error occurs
   */
  private void doParsing() throws IOException {
    int c;
    char ch, prevCh = 'X';
    StringBuffer str = new StringBuffer(100);
    // is reader in CSS comment ( /* .... */ )
    boolean inComment = false;
    while ((c = _reader.read()) != -1) {
      ch = (char)c;
      
      // begin of a comment
      if (str.toString().endsWith("/") && (ch == '*')) {
        inComment = true;
        str.deleteCharAt(str.length()-1);
        continue;
        // end of a comment
      } else if (inComment && (prevCh == '*') && (ch == '/')) {
        inComment = false;
        continue;
        // inside comment
      } else if (inComment) {
        prevCh = ch;
        continue;
        // "normal" content
      } else {
        str.append(ch);
        prevCh = ch;
        
        // end of a style definition
        if (ch == '}') {
          logger.fine(str.toString().replace("\n", " "));
          parseStyle(str.toString());
          str.delete(0, str.length());
        }
      }
    }
  }
  
  
  /**
   *  Parses single CSS style.
   *  Separates style name and makes a map from the properties - pairs
   *  (property name, property value).
   *  @param style style definition (including style name and curly brackets)
   */
  private void parseStyle(String style) {
    String split[] = style.split("\\{", 2);
    if (split.length != 2) {
      return;
    }
    
    String styleName = split[0].trim();
    // style properties
    String attrs = split[1].replace("\\}", "").trim();
    // style properties separately
    split = attrs.split(";");
    
    HashMap<String, String> attrsMap = new HashMap<String, String>(5);
    for (int i = 0; i < split.length; ++i) {
      // separate property name and value
      String[] property = split[i].split(":");
      if (property.length != 2) {
        continue;
      }
      attrsMap.put(property[0].trim().toLowerCase(), property[1].trim().toLowerCase());
    }
    
    // send new style to the handler
    _handler.newStyle(styleName, attrsMap);
  }
}
