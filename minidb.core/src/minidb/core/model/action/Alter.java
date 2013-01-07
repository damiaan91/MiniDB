package minidb.core.model.action;

public class Alter {
	
	private final String table;
	private final String column;
	private final EAction action;
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
	
	public String getTable() {
		return table;
	}

	public String getColumn() {
		return column;
	}

	public EAction getAction() {
		return action;
	}

	public String getUpdateColumn() {
		return updateColumn;
	}

	public void setUpdateColumn(String updateColumn) {
		this.updateColumn = updateColumn;
	}

}
