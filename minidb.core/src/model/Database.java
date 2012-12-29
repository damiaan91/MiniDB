package model;

import java.text.MessageFormat;
import java.util.HashMap;

import exceptions.ColumnAlreadyExistsException;
import exceptions.InvalidTableNameException;

public class Database {
	
	private String name;
	private final HashMap<String, Table> tables = new HashMap<String, Table>();
	
	public Database(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, Table> getTables() {
		return tables;
	}
	
	public void createTable(String tName, String[] cNames) throws InvalidTableNameException, ColumnAlreadyExistsException {
		if (tables.containsKey(tName)) throw new InvalidTableNameException(tName, name);
		if (cNames.length < 1) throw new IllegalArgumentException(MessageFormat.format("No column names found for new table {0} (Database: {1}, arguments)", tName, name));
		
		Table t = new Table(tName, cNames[0]);
		for (int i = 1; i < cNames.length; i++) {
			t.addColumn(cNames[i]);
		}
		tables.put(tName, t);
	}
}
