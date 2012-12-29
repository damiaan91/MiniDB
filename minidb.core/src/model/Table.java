package model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import exceptions.ColumnAlreadyExistsException;

public class Table {
	
	private String name;
	private final HashMap<BigInteger, Record> records;
	private final List<String> columnNames;
	private long keyCounter = 0;
	
	public Table(String name, String columnName) {
		this.name = name;
		columnNames = new ArrayList<String>();
		columnNames.add(columnName);
		records = new HashMap<BigInteger, Record>();
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public HashMap<BigInteger, Record> getRecords() {
		return records;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void addColumn(String columnName) throws ColumnAlreadyExistsException {
		if (columnNames.contains(columnName)) throw new ColumnAlreadyExistsException(columnName, this.name);
		
		columnNames.add(columnName);
		for(Record r : records.values()) {
			r.addColumn(columnName);
		}
	}
	
	public Record getRecord(BigInteger key) {
		return records.get(key);
	}

	public BigInteger addRecord(Record r) {
		BigInteger key = getNextKey();
		records.put(key, r);
		return key;
	}
	
	private BigInteger getNextKey() {
		return new BigInteger(String.valueOf(keyCounter++));
	}
}
