package logging;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;

import minidb.core.config.Defaults;
import minidb.core.exceptions.InvalidUserException;
import minidb.core.model.action.Create;
import minidb.core.model.action.IAction;
import minidb.core.model.action.Insert;
import minidb.core.model.action.Select;
import minidb.core.model.data.ISession;
import minidb.core.model.data.SecureDatabase;

public aspect CRUDLogging {
	//CONSTANTS
	public static final String LOGGING_TABLE = "sys.log";
	public static final String[] LOGGING_COLUMNS = {"TIMESTAMP", "MESSAGE"};
	
	private HashMap<String, ISession> sessionMap = new HashMap<String, ISession>();
	
	pointcut select(Select a, ISession s) : 
		call(String minidb.core.model.data.ISession.select(Select)) 
		&& args(a) 
		&& target(s);
	
	pointcut create(IAction a, ISession s) : 
		call(String minidb.core.model.data.ISession.create(Create))
		&& args(a) 
		&& target(s);
	
	pointcut insert(IAction a, ISession s) : 
		call(String minidb.core.model.data.ISession.insert(Insert))
		&& args(a) 
		&& target(s);;
		
	pointcut CRUD(IAction a, ISession s) : select(a, s) || create(a, s) || insert(a, s) && !within(CRUDLogging);
	
	pointcut dbStart(SecureDatabase db) : 
		execution((*..Database+ && !*..Database).new(..)) && target(db);
	
	after(SecureDatabase db) returning : dbStart(db) {
		System.out.println("Setting up logging...");
		ISession session;
		try {
			session = db.login(Defaults.adminUsernam, Defaults.adminUsernam);
			sessionMap.put(db.getName(), db.login(Defaults.adminUsernam, Defaults.adminUsernam));
			Create createLoggingTable = new Create(LOGGING_TABLE);
			createLoggingTable.addColumns(LOGGING_COLUMNS);
			session.create(createLoggingTable);
		} catch (InvalidUserException e) {
			System.out.println(e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	after(IAction a, ISession s) : CRUD(a, s) {
		logLine(s.getDatabaseName(), a);
	}
	
	private void logLine(String dbName, IAction action) {
		ISession session = sessionMap.get(dbName);
		Insert insert = new Insert(LOGGING_TABLE);
		insert.addValue(new SimpleDateFormat("hh:mm:ss").format(GregorianCalendar.getInstance().getTime()));
		insert.addValue(action.toString());
		session.insert(insert);
	}
}
