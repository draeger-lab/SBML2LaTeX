/*
 * FatalErrorException.java
*/

package cz.kebrt.html2latex;

/**
 *  Fatal error - leads to program exit.
 */
public class FatalErrorException extends Exception {

    /**
	 * Generated serial version uid.
	 */
	protected static final long serialVersionUID = -6627726588217106314L;

	/**
     * Cstr.
     * @param str error description
     */
    public FatalErrorException(String str) {
        super("Fatal error: " + str);
    }
}


/**
 * Error - not so heavy as {@link FatalErrorException fatal error}.
*/
class ErrorException extends Exception {

    /**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = 859704762936611954L;

	/**
     * Cstr.
     * @param str error description
     */
    public ErrorException(String str) {
        super("Error: " + str);
    }
}

/**
 *  Configuration item (element, entity, CSS property) wasn't found
 *  in the cofiguration.
*/
class NoItemException extends Exception {

    /**
	 *  Generated serial version uid.
	 */
	private static final long serialVersionUID = -4395603134253916167L;

	/**
     * Cstr.
     * @param item item name
     */
    NoItemException(String item) {
        super("Can't find specified config item " + item);
    }
}
