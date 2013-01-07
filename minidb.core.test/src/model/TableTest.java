package model;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.math.BigInteger;

import minidb.core.exceptions.ColumnAlreadyExistsException;
import minidb.core.model.data.Record;
import minidb.core.model.data.Table;

import org.junit.Before;
import org.junit.Test;


public class TableTest {
	
	private Table table;
	
	public static final String TABLE_NAME = "Table1";	
	public static final String COLUMN_NAME = "Column1";	
	public static final String COLUMN_NAME_TEST = "Column2";
	public static final String RECORD_VALUE = "Value1";
	public static final String RECORD_VALUE2 = "Value2";
	
	@Before
	public void before() {
		table = new Table(TABLE_NAME, COLUMN_NAME);
	}

	@Test
	public void tableConstructorTest() {
		assertThat(table.getName(), is(TABLE_NAME));
		assertThat(table.getColumnNames().size(), is(1));
		assertThat(table.getColumnNames(), hasItem(COLUMN_NAME));
		assertThat(table.getRecords().size(), is(0));
	}
	
	@Test
	public void addColumnTest() throws ColumnAlreadyExistsException {
		table.addColumn(COLUMN_NAME_TEST);
		assertThat(table.getColumnNames().size(), is(2));
		assertThat(table.getColumnNames(), hasItem(COLUMN_NAME_TEST));
	}
	
	@Test(expected=ColumnAlreadyExistsException.class)
	public void addDubbelColumnTest() throws ColumnAlreadyExistsException {
		table.addColumn(COLUMN_NAME);
	}
	
	@Test
	public void addRecordTest() {
		Record r = new Record();
		Record r2 = new Record();
		r.setColVal(COLUMN_NAME, RECORD_VALUE);
		r2.setColVal(COLUMN_NAME, RECORD_VALUE2);
		
		BigInteger key1 = table.addRecord(r);
		assertThat(table.getRecords().size(), is(1));
		assertThat(table.getRecords().get(key1), is(r));
		
		BigInteger key2 = table.addRecord(r);
		assertThat(table.getRecords().size(), is(2));
		assertThat(table.getRecords().get(key2), is(r));
	}
	
	@Test
	public void updateTableColumn() throws ColumnAlreadyExistsException {
		Record r = new Record();
		r.setColVal(COLUMN_NAME, RECORD_VALUE);
		table.addRecord(r);
		table.addColumn(COLUMN_NAME_TEST);
		
		assertThat(r.getColumns(), hasItem(COLUMN_NAME_TEST));
	}

}
