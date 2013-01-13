package minidb.core.exceptions;

import java.text.MessageFormat;

public class InvalidAmountOfInsertValues extends MiniDBCoreException {

	public InvalidAmountOfInsertValues(int size1, int size2, String table) {
		super(MessageFormat.format("Invalid amount of values ({0, number}) for table {2} expected a amount of {1, number}", size1, size2, table));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -9203492132308399344L;

}
