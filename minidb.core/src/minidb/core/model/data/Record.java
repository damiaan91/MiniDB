package minidb.core.model.data;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import minidb.core.config.Defaults;
import minidb.core.exceptions.InvalidColumnException;



/**
 * @author  Damiaan
 */
public class Record {
	
	/**
	 * @uml.property  name="key"
	 */
	private BigInteger key;
	private final HashMap<String, String> data;
	
	public Record() {
		this(null);
	}
	
	/**
	 * @return
	 * @uml.property  name="key"
	 */
	public BigInteger getKey() {
		return key;
	}

	/**
	 * @param key
	 * @uml.property  name="key"
	 */
	public void setKey(BigInteger key) {
		this.key = key;
	}

	public Record(BigInteger key) {
		this.key = key;
		data = new HashMap<String, String>();
	}

	public void addColumn(String columnName) {
		setColVal(columnName, null);
	}
	
	public void setColVal(String columnName, String val) {
		data.put(columnName, val);
	}
	
	public String getValOfCol(String ColumnName) throws InvalidColumnException {
		if (!data.containsKey(ColumnName)) throw new InvalidColumnException(ColumnName);
		return data.get(ColumnName);
	}
	
	public Set<String> getColumns() {
		return data.keySet();
	}
	
	public String toString(List<String> columnNames) {
		Iterator<String> it = columnNames.iterator();
		String result = null;
		if (it.hasNext()) {
			result = data.get(it.next());
			while(it.hasNext()) {
				result += String.format(Defaults.SPACING, data.get(it.next()));
			}
		}
		return result;
		
	}

}
