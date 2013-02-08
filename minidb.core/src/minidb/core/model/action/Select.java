package minidb.core.model.action;

import java.util.ArrayList;
import java.util.List;

import minidb.core.model.data.ISession;

/**
 * @author  Damiaan
 */
public class Select implements IAction {
	/**
	 * @uml.property  name="select"
	 */
	private final List<String> select = new ArrayList<String>();
	/**
	 * @uml.property  name="table"
	 */
	private final String table;
	
	public Select(String table) {
		this.table = table;
	}
	
	public void addColumn(final String columnName) {
		if (!select.contains(columnName)) {
			select.add(columnName);
		}
	}
	
	/**
	 * @return
	 * @uml.property  name="select"
	 */
	public List<String> getSelect() {
		return select;
	}

	/**
	 * @return
	 * @uml.property  name="table"
	 */
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

	@Override
	public String ExecuteUsing(ISession session) {
		return session.select(this);
	}

}
