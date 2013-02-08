package minidb.core.model.action;

import java.util.ArrayList;
import java.util.List;

import minidb.core.model.data.ISession;

/**
 * @author  Damiaan
 */
public class CreateTable implements IAction {
	/**
	 * @uml.property  name="table"
	 */
	private final String table;
	/**
	 * @uml.property  name="columns"
	 */
	private final List<String> columns;
	
	
	public CreateTable(String table) {
		this.table = table;
		this.columns = new ArrayList<String>();
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
	 * @uml.property  name="columns"
	 */
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
