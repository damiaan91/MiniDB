package minidb.core.exceptions;

import java.text.MessageFormat;

public class InvalidUserException extends MiniDBCoreException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2022325415961000179L;

	public InvalidUserException(String username, String databaseName) {
		super(MessageFormat.format("User {0} doesn't exists in database {1}", username, databaseName));
	}

}
