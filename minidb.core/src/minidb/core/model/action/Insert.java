package minidb.core.model.action;

import java.util.ArrayList;
import java.util.List;

public class Insert {
	private final String table;
	private final ArrayList<String> values;
	
	public Insert(String table) {
		this.table = table;
		this.values = new ArrayList<String>();
	}

	public String getTable() {
		return table;
	}
	
	public List<String> getValues() {
		return values;
	}
	
	public void addValue(String value) {
		values.add(value);
	}
	
	public String toString() {
		String result = "INSERT INTO " + table + " VALUES";
		for(String v : values) {
			result += " " + v;
		}
		return result;
	}
}
