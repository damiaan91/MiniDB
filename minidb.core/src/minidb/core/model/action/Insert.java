package minidb.core.model.action;

import java.util.ArrayList;
import java.util.List;

import minidb.core.model.data.ISession;

/**
 * @author  Damiaan
 */
public class Insert implements IAction{
	/**
	 * @uml.property  name="table"
	 */
	private final String table;
	/**
	 * @uml.property  name="values"
	 */
	private final ArrayList<String> values;
	
	public Insert(String table) {
		this.table = table;
		this.values = new ArrayList<String>();
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
	 * @uml.property  name="values"
	 */
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

	@Override
	public String ExecuteUsing(ISession session) {
		return session.insert(this);
	}
}
