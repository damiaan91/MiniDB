package minidb.core.exceptions;

import java.text.MessageFormat;

public class InvalidColumnException extends MiniDBCoreException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8882070513464248170L;
	
	public InvalidColumnException(String columnName) {
		super(MessageFormat.format("Column {0} doesn't exists", columnName));
	}

}
