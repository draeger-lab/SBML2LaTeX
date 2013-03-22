/*
 * FatalErrorException.java
 */

package cz.kebrt.html2latex;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import de.zbit.util.ResourceManager;

/**
 *  Fatal error - leads to program exit.
 *  @version $Rev: 249 $
 *  @since 0.9.3
 */
class FatalErrorException extends Exception {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -804105122394121501L;

	/**
	 * Localization support.
	 */
	private static final ResourceBundle bundle = ResourceManager.getBundle("cz.kebrt.html2latex.messages");
	
	/**
	 * Cstr.
	 * @param str error description
	 */
	public FatalErrorException(String str) {
		super(MessageFormat.format(bundle.getString("FATAL_ERROR"), str));
	}    
}


/**
 * Error - not so heavy as {@link FatalErrorException fatal error}.
 * @version $Rev: 249 $
 * @since 0.9.3
 */
class ErrorException extends Exception {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 859704762936611954L;

	/**
	 * Localization support.
	 */
	private static final ResourceBundle bundle = ResourceManager.getBundle("cz.kebrt.html2latex.messages");
	
	/**
	 * Cstr.
	 * @param str error description
	 */
	public ErrorException(String str) {
		super(MessageFormat.format(bundle.getString("ERROR"), str));
	}    
}

/**
 *  Configuration item (element, entity, CSS property) wasn't found
 *  in the cofiguration.
 *  @version $Rev: 249 $
 *  @since 0.9.3
 */
class NoItemException extends Exception {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -4395603134253916167L;

	/**
	 * Localization support.
	 */
	private static final ResourceBundle bundle = ResourceManager.getBundle("cz.kebrt.html2latex.messages");
	
	/**
	 * Cstr.
	 * @param item item name
	 */
	NoItemException(String item) {
		super(MessageFormat.format(bundle.getString("NO_SUCH_CONFIG_ITEM"), item));
	}
}
