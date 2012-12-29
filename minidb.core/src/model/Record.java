package model;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Set;

import exceptions.InvalidColumnException;

public class Record {
	
	private BigInteger key;
	private final HashMap<String, String> data;
	
	public Record() {
		this(null);
	}
	
	public BigInteger getKey() {
		return key;
	}

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

}
