package minidb.core.model.action;

import java.util.ArrayList;
import java.util.List;

public class Select {
	private final List<String> select = new ArrayList<String>();
	private final String table;
	
	public Select(String table) {
		this.table = table;
	}
	
	public void addColumn(final String columnName) {
		if (!select.contains(columnName)) {
			select.add(columnName);
		}
	}
	
	public List<String> getSelect() {
		return select;
	}

	public String getTable() {
		return table;
	}
	
	public String toString() {
		String result = "SELECT";
		for(String c : select) {
			result += " " + c;
		}
		return result + " FROM " + table;
	}

}
