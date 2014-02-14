/*
 * Element.java
 */
package cz.kebrt.html2latex;

import java.util.HashMap;


/**
 * Class representing HTML start element.
 * @version $Rev$
 * @since 0.9.3
 */
class ElementStart extends MyElement {
  
  /** Map containing all element's attributtes with their values. */
  private HashMap<String, String> _attributes;
  
  /**
   * Cstr.
   * @param element element's name
   * @param attributes element's attributes
   */
  public ElementStart(String element, HashMap<String, String> attributes) {
    _element = element;
    _attributes = attributes;
  }
  
  /**
   * Returns element's attributes.
   * @return element's attributes
   */
  HashMap<String, String> getAttributes() {
    return _attributes;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "<" + getElementName() + " " + getAttributes().toString().substring(1).replace("}", "") + ">";
  }
  
}


/**
 *  Class representing HTML end element.
 */
class ElementEnd extends MyElement {
  
  /**
   * Cstr.
   * @param element element's name
   */
  ElementEnd(String element) {
    _element = element;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "</" + getElementName() + ">";
  }
  
}


/**
 * Abstract class for HTML start and end elements (tags).
 */
abstract class MyElement {
  /** Element's name */
  protected String _element;
  
  /**
   * Returns element's name.
   * @return element's name
   */
  String getElementName() {
    return _element;
  }
}


