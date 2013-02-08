package minidb.core.model.action;

/**
 * @author  Damiaan
 */
public class Alter {
	
	/**
	 * @uml.property  name="table"
	 */
	private final String table;
	/**
	 * @uml.property  name="column"
	 */
	private final String column;
	/**
	 * @uml.property  name="action"
	 * @uml.associationEnd  
	 */
	private final EAction action;
	/**
	 * @uml.property  name="updateColumn"
	 */
	private String updateColumn;
	
	public Alter(String table, String column, String updateColumn) {
		this(table, column, EAction.UPDATE);
		this.setUpdateColumn(updateColumn);
	}
	
	public Alter(String table, String column, EAction action) {
		this.table = table;
		this.column = column;
		this.action = action;
	}
	
	/**
	 * @return
	 * @uml.property  name="table"
	 */
	public String getTable() {
		return table;
	}

	/**
	 * @return
	 * @uml.property  name="column"
	 */
	public String getColumn() {
		return column;
	}

	/**
	 * @return
	 * @uml.property  name="action"
	 */
	public EAction getAction() {
		return action;
	}

	/**
	 * @return
	 * @uml.property  name="updateColumn"
	 */
	public String getUpdateColumn() {
		return updateColumn;
	}

	/**
	 * @param updateColumn
	 * @uml.property  name="updateColumn"
	 */
	public void setUpdateColumn(String updateColumn) {
		this.updateColumn = updateColumn;
	}

}
