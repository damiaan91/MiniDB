package minidb.core.model.action;

import java.util.ArrayList;
import java.util.List;

public class Create {
	private final String table;
	private final List<String> columns;
	
	
	public Create(String table) {
		this.table = table;
		this.columns = new ArrayList<String>();
	}

	public String getTable() {
		return table;
	}

	public List<String> getColumns() {
		return columns;
	}
	
	public void addColumn(String column) {
		columns.add(column);
	}
	
	public String toString() {
		String result = "CREATE " + table + " COLUMNS";
		for (String c : columns) {
			result += " " + c;
		}
		return result;
	}
	
}
