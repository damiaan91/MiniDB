package minidb.client.view;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Scanner;


import minidb.client.exceptions.InvalidStatementException;
import minidb.core.config.Defaults;
import minidb.core.config.Version;
import minidb.core.exceptions.ColumnAlreadyExistsException;
import minidb.core.exceptions.InvalidAmountOfInsertValues;
import minidb.core.exceptions.InvalidTableNameException;
import minidb.core.exceptions.InvalidUserException;
import minidb.core.exceptions.UserAlreadyExistsException;
import minidb.core.model.action.Create;
import minidb.core.model.action.Insert;
import minidb.core.model.action.Select;
import minidb.core.model.data.SecureDatabase;


public class Client {

	private Scanner in = new Scanner(System.in);
	private SecureDatabase db;
	private boolean isLoggedIn;
	private String currentUser;
	
	public Client() {
		this(Defaults.databaseName);
	}
	
	public Client(String dbName) {
		System.out.println(MessageFormat.format("Starting MiniDB version {0}", Version.VERSION));
		createDB(dbName);
		createExamples();
		login();
		handleActions();
		System.out.println("Shutdown MiniDB....");
		in.nextLine();
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
		
		System.out.print("Login: ");
		username = in.nextLine();
		System.out.print("Password: ");
		password = in.nextLine();
		
		try {
			isLoggedIn = db.isValidLogin(username, password);
			if(isLoggedIn) {
				this.currentUser = username;
			}
		} catch (InvalidUserException e) {
			System.out.println("Error: " + e.getMessage());
			isLoggedIn = false;
		}
	}
	
	public void handleActions() {
		while(isLoggedIn) {
			try {
				parseAction(in.nextLine());
			} catch (Exception e) {
				handleException(e);
			}
		}
	}
	
	private void parseAction(String actionMsg) throws InvalidUserException, InvalidTableNameException, InvalidAmountOfInsertValues, InvalidStatementException, ColumnAlreadyExistsException {
		if (!actionMsg.isEmpty() && !actionMsg.trim().isEmpty()) {
			if(actionMsg.equals("logout") || actionMsg.equals("exit")) {
				isLoggedIn = false;
			} else {
				String[] statementParts = actionMsg.split(" ");
				String result;
				if (statementParts[0].equalsIgnoreCase("SELECT")) {
					result = db.executeSelect(createSelect(statementParts), currentUser);
				} else if (statementParts[0].equalsIgnoreCase("INSERT")) {
					result = db.executeInsert(createInsert(statementParts), currentUser);
				} else if (statementParts[0].equalsIgnoreCase("CREATE")) {
					result = db.executeCreate(createCreate(statementParts), currentUser);
				} else {
					result = "Unkown statement";
				}
				System.out.println(result);
			}
		}
	}
	
	private Select createSelect(String[] statement) throws InvalidStatementException {
		if (statement.length < 2) throw new InvalidStatementException("empty", "_COLUMNNAME_");
		int i = 1;
		for (i = 1; i < statement.length; i++) {
			if(statement[i+1].equalsIgnoreCase("FROM")) break;
		}
		if (!statement[i+1].equalsIgnoreCase("FROM")) throw new InvalidStatementException(statement[i+1], "FROM");
		if (statement.length < i + 3) throw new InvalidStatementException("empty", "_TABLENAME_");
		
		Select select = new Select(statement[i + 2]);
		for (int j = 1; j < i+1; j++) {
			select.addColumn(statement[j]);
		}
		return select;
	}

	private Create createCreate(String[] statement) throws InvalidStatementException {
		if (statement.length < 2) throw new InvalidStatementException("empty", "_TABLENAME_");
		if (!statement[2].equalsIgnoreCase("COLUMNS")) throw new InvalidStatementException(statement[2], "COLUMNS");
		Create create = new Create(statement[1]);
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
		currentUser = Defaults.adminUsernam;
		try {
			parseAction("CREATE test COLUMNS firstName lastName");
			parseAction("INSERT INTO test VALUES Christoph Bockisch");
			parseAction("INSERT INTO test VALUES Roeland Kegel");
			parseAction("INSERT INTO test VALUES Damiaan Kruk");
		} catch (Exception e) {
			e.printStackTrace();
		}
		currentUser = null;
	}

	private void handleException(Exception e) {
		System.out.println("Error: " + e.getMessage());
	}

	private void createDB(String dbName) {
		try {
			db = new SecureDatabase(dbName);
			System.out.println(MessageFormat.format("SecureDatabase created with name {0}", dbName));
		} catch (UserAlreadyExistsException e) {
			e.printStackTrace();
		}
	}
	

}
