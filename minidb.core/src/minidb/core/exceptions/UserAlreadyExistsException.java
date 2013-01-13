package minidb.core.exceptions;

import java.text.MessageFormat;

public class UserAlreadyExistsException extends MiniDBCoreException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6002592779614407L;
	
	public UserAlreadyExistsException(String username, String databaseName) {
		super(MessageFormat.format("User {0} already exists in database {1}", username, databaseName));
	}
}
