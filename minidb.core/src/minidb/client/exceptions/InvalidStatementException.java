package minidb.client.exceptions;

import java.text.MessageFormat;

public class InvalidStatementException extends MiniDBClientException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7865932265629154044L;

	public InvalidStatementException(String actual, String expected) {
		super(MessageFormat.format("Invalid token {0} (expected {1})", actual, expected));
	}

}
