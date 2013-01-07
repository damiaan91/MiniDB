package minidb.core.exceptions;

import java.text.MessageFormat;

public class ColumnAlreadyExistsException extends Exception {

	private static final long serialVersionUID = -507155806002039532L;

	public ColumnAlreadyExistsException(String columnName, String tableName) {
		super(MessageFormat.format("Column {0} arleady exists in table {1}", columnName, tableName));
	}
}
