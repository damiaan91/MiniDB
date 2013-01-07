package minidb.core.exceptions;

import java.text.MessageFormat;

public class InsufficientRightsExcpetion extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6849571776620136878L;

	public InsufficientRightsExcpetion(String username, String databaseName, String action) {
		super(MessageFormat.format("User {0} has insufficient right to {1} in database {2}", username, action, databaseName));
	}

}
