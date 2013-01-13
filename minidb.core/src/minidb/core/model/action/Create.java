package minidb.core.model.action;

import java.util.ArrayList;
import java.util.List;

import minidb.core.model.data.ISession;

public class Create implements IAction {
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
		if(!columns.contains(column)) {
			columns.add(column);
		}
	}
	
	public void addColumns(String[] columns) {
		for(String column : columns) {
			addColumn(column);
		}
	}
	
	public String toString() {
		String result = "CREATE " + table + " COLUMNS";
		for (String c : columns) {
			result += " " + c;
		}
		return result;
	}

	@Override
	public String ExecuteUsing(ISession session) {
		return session.create(this);
	}
	
}
