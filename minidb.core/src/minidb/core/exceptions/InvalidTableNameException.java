package minidb.core.exceptions;

import java.text.MessageFormat;

public class InvalidTableNameException extends MiniDBCoreException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7356421927393917057L;

	public InvalidTableNameException(String tableName, String databaseName) {
		super(MessageFormat.format("Table {0} doesn't exists in database {1}", tableName, databaseName));
	}

}
