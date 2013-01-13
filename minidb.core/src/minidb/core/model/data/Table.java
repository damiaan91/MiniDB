package minidb.core.model.data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import minidb.core.config.Defaults;
import minidb.core.exceptions.ColumnAlreadyExistsException;
import minidb.core.exceptions.InvalidAmountOfInsertValues;

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
	
	public String selectColumns(String[] columns) {
		String result = null;
		if (columns[0].equals("*")) {
			result = columnNames.get(0);
			for (int i = 1; i < columnNames.size(); i++) {
				result += String.format(Defaults.SPACING, columnNames.get(i));
			}
			result += "\n----------------------------------------------------------";
			for (Record record : getRecords().values()) {
				result += "\n" + record.toString(columnNames);
			}
		}
		return result;
	}
	
	public Record getRecord(BigInteger key) {
		return records.get(key);
	}
	
	public BigInteger addRecord(List<String> values) throws InvalidAmountOfInsertValues {
		if (values.size() != columnNames.size()) throw new InvalidAmountOfInsertValues(columnNames.size(), values.size(), name);
		Record r = new Record();
		for(int i = 0; i < values.size(); i++) {
			r.addColumn(columnNames.get(i));
			r.setColVal(columnNames.get(i), values.get(i));
		}
		return addRecord(r);
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
