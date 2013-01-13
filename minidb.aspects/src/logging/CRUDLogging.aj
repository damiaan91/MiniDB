package logging;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;

import minidb.core.config.Defaults;
import minidb.core.exceptions.InvalidUserException;
import minidb.core.model.action.CreateTable;
import minidb.core.model.action.CreateUser;
import minidb.core.model.action.IAction;
import minidb.core.model.action.Insert;
import minidb.core.model.action.Select;
import minidb.core.model.data.ISession;
import minidb.core.model.data.SecureDatabase;

public aspect CRUDLogging {
	//CONSTANTS
	public static final String LOGGING_TABLE = "sys.log";
	public static final String LOGGING_ERR_TABLE = "sys.err";
	public static final String[] LOGGING_COLUMNS = {"TIMESTAMP", "MESSAGE", "EXECUTED BY"};
	
	public static final String LOG_USER = "sys.log";
	public static final String LOG_USER_PASSWORD = "bee4be09e3431958f625e66b23349e38";
	
	private HashMap<String, ISession> sessionMap = new HashMap<String, ISession>();
	
	pointcut select(Select a, ISession s) : 
		call(String minidb.core.model.data.ISession.select(Select)) && 
		args(a) && 
		target(s);
	
	pointcut createTable(IAction a, ISession s) : 
		call(String minidb.core.model.data.ISession.create(CreateTable)) && 
		args(a) && 
		target(s);
	
	pointcut insert(IAction a, ISession s) : 
		call(String minidb.core.model.data.ISession.insert(Insert)) && 
		args(a) && 
		target(s);
	
	pointcut createUser(IAction a, ISession s) : 
		call(String minidb.core.model.data.ISession.createUser(CreateUser)) && 
		args(a) && 
		target(s);
		
	pointcut CRUD(IAction a, ISession s) : 
		select(a, s) || createTable(a, s) || 
		(insert(a, s)  && !within(CRUDLogging));
	
	pointcut dbStart(SecureDatabase db) : 
		execution((*..Database+ && !*..Database).new(..)) && target(db);

	after(SecureDatabase db) returning : dbStart(db) {
		System.out.println("Setting up logging...");
		try {
			ISession session = db.login(Defaults.adminUsernam, Defaults.adminUsernam);
			session.createUser(new CreateUser(LOG_USER, LOG_USER_PASSWORD, true));
			session = db.login(LOG_USER, LOG_USER_PASSWORD);
			sessionMap.put(db.getName(), session);
			CreateTable createLoggingTable = new CreateTable(LOGGING_TABLE);
			createLoggingTable.addColumns(LOGGING_COLUMNS);
			session.create(createLoggingTable);
		} catch (InvalidUserException e) {
			System.out.println(e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	after(IAction a, ISession s) : CRUD(a, s) {
		logLine(s, a);
	}
	
	private void logLine(ISession s, IAction action) {
		ISession session = sessionMap.get(s.getDatabaseName());
		Insert insert = new Insert(LOGGING_TABLE);
		insert.addValue(new SimpleDateFormat("hh:mm:ss").format(GregorianCalendar.getInstance().getTime())+" ");
		insert.addValue(action.toString());
		insert.addValue(s.getSessionUser());
		session.insert(insert);
	}
}
