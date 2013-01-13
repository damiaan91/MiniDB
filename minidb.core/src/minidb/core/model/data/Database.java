package minidb.core.model.data;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;

import minidb.core.exceptions.ColumnAlreadyExistsException;
import minidb.core.exceptions.InvalidAmountOfInsertValues;
import minidb.core.exceptions.InvalidTableNameException;
import minidb.core.model.action.Create;
import minidb.core.model.action.IAction;
import minidb.core.model.action.Insert;
import minidb.core.model.action.Select;


public abstract class Database {
	
	private String name;
	private final HashMap<String, Table> tables = new HashMap<String, Table>();
	
	protected Database(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected Collection<Table> getTables() {
		return tables.values();
	}
	
	protected void createTable(String tName, String[] cNames) throws InvalidTableNameException, ColumnAlreadyExistsException {
		if (tables.containsKey(tName)) throw new InvalidTableNameException(tName, name);
		if (cNames.length < 1) throw new IllegalArgumentException(MessageFormat.format("No column names found for new table {0} (Database: {1}, arguments)", tName, name));
		
		Table t = new Table(tName, cNames[0]);
		for (int i = 1; i < cNames.length; i++) {
			t.addColumn(cNames[i]);
		}
		tables.put(tName, t);
	}

	protected String executeInsert(Insert insert) throws InvalidTableNameException, InvalidAmountOfInsertValues {
		if (!tables.containsKey(insert.getTable())) throw new InvalidTableNameException(insert.getTable(), name);
		Table table = tables.get(insert.getTable());
		table.addRecord(insert.getValues());
		return MessageFormat.format("Result ({0, number})", insert.getValues().size());
	}

	protected String executeCreate(Create createCreate) throws InvalidTableNameException, ColumnAlreadyExistsException {
		createTable(createCreate.getTable(), createCreate.getColumns().toArray(new String[0]));
		return MessageFormat.format("Successfully created table {0}", createCreate.getTable());
	}

	protected String executeSelect(Select select) throws InvalidTableNameException {
		if (!tables.containsKey(select.getTable())) throw new InvalidTableNameException(select.getTable(), name);
		Table table = tables.get(select.getTable());	
		return table.selectColumns(select.getSelect().toArray(new String[select.getSelect().size()]));
	}
	
	protected abstract class DbSession implements ISession {
		
		protected Database db;
		
		protected DbSession(Database db) {
			this.db = db;
		}
		
		@Override
		public String execute(IAction action) {
			return action.ExecuteUsing(this);
		}

		@Override
		public String getDatabaseName() {
			return db.getName();
		}
		
	}
}
