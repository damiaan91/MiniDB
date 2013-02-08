package minidb.core.model.data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import minidb.core.config.Defaults;
import minidb.core.exceptions.ColumnAlreadyExistsException;
import minidb.core.exceptions.InvalidAmountOfInsertValues;

/**
 * @author  Damiaan
 */
public class Table {
	
	/**
	 * @uml.property  name="name"
	 */
	private String name;
	/**
	 * @uml.property  name="records"
	 */
	private final HashMap<BigInteger, Record> records;
	/**
	 * @uml.property  name="columnNames"
	 */
	private final List<String> columnNames;
	private long keyCounter = 0;
	
	public Table(String name, String columnName) {
		this.name = name;
		columnNames = new ArrayList<String>();
		columnNames.add(columnName);
		records = new HashMap<BigInteger, Record>();
	}

	/**
	 * @return
	 * @uml.property  name="columnNames"
	 */
	public List<String> getColumnNames() {
		return columnNames;
	}

	/**
	 * @return
	 * @uml.property  name="records"
	 */
	public HashMap<BigInteger, Record> getRecords() {
		return records;
	}

	/**
	 * @return
	 * @uml.property  name="name"
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * @uml.property  name="name"
	 */
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
		} else {
			result = columns[0];
			for(int i = 1; i < columns.length; i++) {
				result += String.format(Defaults.SPACING, columns[i]);
			}
			result += "\n----------------------------------------------------------";
			for (Record record : getRecords().values()) {
				result += "\n" + record.toString(Arrays.asList(columns));
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
