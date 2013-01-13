package minidb.client.view;

import java.text.MessageFormat;
import java.util.Scanner;

import minidb.client.exceptions.InvalidStatementException;
import minidb.client.exceptions.MiniDBClientException;
import minidb.core.config.Defaults;
import minidb.core.config.Version;
import minidb.core.exceptions.ColumnAlreadyExistsException;
import minidb.core.exceptions.InvalidAmountOfInsertValues;
import minidb.core.exceptions.InvalidTableNameException;
import minidb.core.exceptions.InvalidUserException;
import minidb.core.exceptions.MiniDBCoreException;
import minidb.core.model.action.CreateTable;
import minidb.core.model.action.CreateUser;
import minidb.core.model.action.GrantPrivilege;
import minidb.core.model.action.IAction;
import minidb.core.model.action.Insert;
import minidb.core.model.action.Select;
import minidb.core.model.data.ISession;
import minidb.core.model.data.SecureDatabase;


public class Client {

	private Scanner in = new Scanner(System.in);
	private SecureDatabase db;
	private ISession session;
	private boolean exitProgram;
	
	public Client() {
		this(Defaults.databaseName);
		this.exitProgram = false;
	}
	
	public Client(String dbName) {
		System.out.println(MessageFormat.format("Starting MiniDB version {0}", Version.VERSION));
		createDB(dbName);
		createExamples();
		while (!exitProgram) {
			login();
			handleActions();
		}
		System.out.println("Shutdown MiniDB....");
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Client();
	}
	
	public void login() {
		String username;
		String password;
		session = null;
		
		for(int i = 0; i < 3 && session == null; i++) {
			System.out.print("Login: ");
			username = in.nextLine();
			System.out.print("Password: ");
			password = in.nextLine();
			try {
				session = db.login(username, password);
			} catch (InvalidUserException e) {
				System.out.println(e.getMessage());
			}
			if (session == null) {
				System.out.println("Invalid password.");
			}
		}
		if(session == null) {
			System.out.println("Failed to login.");
			exitProgram = true;
		}
		
	}
	
	public void handleActions() {
		if(exitProgram) return;
		while(session.isActive()) {
			try {
				parseAction(in.nextLine());
			} catch (MiniDBClientException e) {
				handleException(e);
			} catch (MiniDBCoreException e) {
				handleException(e);
			}
		}
	}
	
	private void parseAction(String actionMsg) throws InvalidUserException, InvalidTableNameException, InvalidAmountOfInsertValues, InvalidStatementException, ColumnAlreadyExistsException {
		if (!actionMsg.isEmpty() && !actionMsg.trim().isEmpty()) {
			boolean sysCmd = false;
			switch (actionMsg.toUpperCase()) {
				case "EXIT": exitProgram = true;
				case "LOGOUT": session.disconnect();
				sysCmd = true;
				break;
				case "?":
				case "/?":
				case "HELP": printHelp();
				sysCmd = true;
				break;
			}
			if (session.isActive() && !sysCmd) {
				String[] statementParts = actionMsg.split(" ");
				IAction action = null;
				switch (statementParts[0].toUpperCase()) {
					case "SELECT": action = createSelect(statementParts); 
					break;
					case "INSERT": action = createInsert(statementParts); 
					break;
					case "CREATE": action = createCreate(statementParts); 
					break;
				}
				if (action == null) {
					System.out.println("Unkown statement (Use \"HELP\" command for supported commands.");
				} else {
					System.out.println(action.ExecuteUsing(session));
				}
			}
		}
	}
	
	private void printHelp() {
		System.out.println("List with supported commands:");
		System.out.println("- SELECT * FROM _TABLENAME_");
		System.out.println("- INSERT INTO _TABLENAME_ VALUES _VALUE1_[, _VALUEN_]");
		System.out.println("- CREATE _TABLENAME_ COLUMNS _COLUMN1_[, _COLUMNN_]");
		System.out.println("- EXIT");
		System.out.println("- LOGOUT");
	}

	private Select createSelect(String[] statement) throws InvalidStatementException {
		if (statement.length < 2) throw new InvalidStatementException("empty", "_COLUMNNAME_");
		int i = 1;
		for (i = 1; i < statement.length; i++) {
			if(i+1 < statement.length) {
				if(statement[i+1].equalsIgnoreCase("FROM")) break;
			}
		}
		if(i == statement.length) throw new InvalidStatementException("empty", "FROM");
		if (!statement[i+1].equalsIgnoreCase("FROM")) throw new InvalidStatementException(statement[i+1], "FROM");
		if (statement.length < i + 3) throw new InvalidStatementException("empty", "_TABLENAME_");
		
		Select select = new Select(statement[i + 2]);
		for (int j = 1; j < i+1; j++) {
			select.addColumn(statement[j]);
		}
		return select;
	}

	private CreateTable createCreate(String[] statement) throws InvalidStatementException {
		if (statement.length < 2) throw new InvalidStatementException("empty", "_TABLENAME_");
		if (!statement[2].equalsIgnoreCase("COLUMNS")) throw new InvalidStatementException(statement[2], "COLUMNS");
		CreateTable create = new CreateTable(statement[1]);
		for(int i = 3; i < statement.length; i++) {
			create.addColumn(statement[i].replace(",", ""));
		}
		return create;
	}
	
	private Insert createInsert(String[] statement) throws InvalidStatementException {
		//Validate
		if (!statement[1].equalsIgnoreCase("INTO")) throw new InvalidStatementException(statement[1], "INTO");
		if (statement.length < 3) throw new InvalidStatementException("empty", "_TABLENAME_");
		if (!statement[3].equalsIgnoreCase("VALUES")) throw new InvalidStatementException(statement[3], "VALUES");
		Insert insert = new Insert(statement[2]);
		for (int i = 4; i < statement.length; i++) {
			insert.addValue(statement[i]);
		}
		return insert;
	}
	
	private void createExamples() {
		System.out.println("Creating example tables....");
		try {
			this.session = db.login(Defaults.adminUsernam, Defaults.adminPassword);
			parseAction("CREATE test COLUMNS firstName lastName");
			parseAction("INSERT INTO test VALUES Christoph Bockisch");
			parseAction("INSERT INTO test VALUES Roeland Kegel");
			parseAction("INSERT INTO test VALUES Damiaan Kruk");
			session.createUser(new CreateUser("demo", "demo"));
			session.grantPrivilege(new GrantPrivilege("test", "demo"));
			session.grantPrivilege(new GrantPrivilege("sys.log", "demo"));
		} catch (MiniDBCoreException | MiniDBClientException e) {
			System.out.println(e.getMessage());
			exitProgram = true;
		} finally {
			this.session = null;
		}
	}

	private void handleException(Exception e) {
		System.out.println("Error: " + e.getMessage());
	}

	private void createDB(String dbName) {
		db = new SecureDatabase(dbName);
		System.out.println(MessageFormat.format("SecureDatabase created with name {0}", dbName));
	}
	

}
