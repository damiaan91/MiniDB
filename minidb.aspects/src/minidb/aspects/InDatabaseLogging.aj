package minidb.aspects;

import java.util.HashMap;

import minidb.aspects.InDatabaseLogging.SessionAction;
import minidb.core.model.action.CreateTable;
import minidb.core.model.action.CreateUser;
import minidb.core.model.action.IAction;
import minidb.core.model.action.Insert;
import minidb.core.model.action.Select;
import minidb.core.model.data.ISession;
import minidb.core.model.data.SecureDatabase;
import aspects.core.AbstractLogger;

/**
 * Aspect for logging executed statements in the database itself.
 */
public aspect InDatabaseLogging extends AbstractLogger<SessionAction> {
	//CONSTANTS
	public static final String LOGGING_TABLE = "sys.log";
	public static final String LOGGING_ERR_TABLE = "sys.err";
	public static final String[] LOGGING_COLUMNS = {"TIMESTAMP", "MESSAGE", "EXECUTED BY"};
	
	public static final String LOG_USER = "sys.log";
	public static final String LOG_USER_PASSWORD = "bee4be09e3431958f625e66b23349e38";
	
	/**
	 * Methods to log
	 */
	public pointcut logMethods() : call(SessionAction minidb.aspects.InDatabaseLogging.log*(..));
	
	/**
	 * A map holding the log session to a specific database. For identification the database name is used.
	 */
	private HashMap<String, ISession> sessionMap = new HashMap<String, ISession>();
	
	/**
	 * Initialize base logging
	 */
	public pointcut startUpPrefix(Object prefix) : dbStart(prefix);
	
	/**
	 * Pointcut when a select is called.
	 * @param a Select action
	 * @param s Executing session
	 */
	public pointcut select(Select a, ISession s) : 
		call(String minidb.core.model.data.ISession.select(Select)) && 
		args(a) && 
		target(s);
	
	/**
	 * Pointcut when a create table is called.
	 * @param a Create table action
	 * @param s Executing session
	 */
	public pointcut createTable(IAction a, ISession s) : 
		call(String minidb.core.model.data.ISession.create(CreateTable)) && 
		args(a) && 
		target(s);
	
	/**
	 * Pointcut when a insert is called.
	 * @param a Insert action
	 * @param s Executing session
	 */
	public pointcut insert(IAction a, ISession s) : 
		call(String minidb.core.model.data.ISession.insert(Insert)) && 
		args(a) && 
		target(s);
	
	/**
	 * Pointcut when a create user is called
	 * @param a Create user action
	 * @param s Executing session
	 */
	public pointcut createUser(IAction a, ISession s) : 
		call(String minidb.core.model.data.ISession.createUser(CreateUser)) && 
		args(a) && 
		target(s);
	
	/**
	 * Pointcut collection of different actions.
	 * @param a The specific action
	 * @param s Executing session
	 */
	public pointcut CRUD(IAction a, ISession s) : 
		select(a, s) || createTable(a, s) || 
		(insert(a, s)  && !within(InDatabaseLogging));
	
	/**
	 * Executes when a new  secure database is created.
	 * @param db The instanced database
	 */
	public pointcut dbStart(SecureDatabase db) : 
		execution((*..Database+ && !*..Database).new(String)) 
		&& target(db);
	
	/**
	 * Create logging tables on instantiation of a database.
	 * @param db The specific database
	 */
	after(SecureDatabase db) returning : dbStart(db) {
		System.out.println("Setting up logging...");
		ISession session = Util.createSystemUser(db, LOG_USER, LOG_USER_PASSWORD);
		sessionMap.put(db.getName(), session);
		Util.createTable(LOGGING_TABLE, LOGGING_COLUMNS, session);
	}
	
	/*
	 * (non-Javadoc)
	 * @see aspects.core.AbstractLogger#handleLogMessage(java.lang.Object)
	 */
	@Override
	public void handleLogMessage(SessionAction sa){
		ISession session = sessionMap.get(sa.session.getDatabaseName());
		Insert insert = new Insert(LOGGING_TABLE);
		insert.addValue(getTimeStamp());
		insert.addValue(sa.action.toString());
		insert.addValue(sa.session.getSessionUser());
		session.insert(insert);
	}
	
	/**
	 * Log successful actions.
	 * @param a The specific action to log
	 * @param s Executing session
	 */
	after(IAction a, ISession s) returning : CRUD(a, s) {
		logSuccesfullAction(a, s);
	}
	
	/**
	 * Delivers the right format for the logger.
	 * @param a The specific action to log
	 * @param s Executing session
	 * @return Log format
	 */
	public SessionAction logSuccesfullAction(IAction a, ISession s) {
		return new SessionAction(a, s);
	}
	
	/**
	 * Data holder class
	 */
	public class SessionAction {
		private ISession session;
		private IAction action;
		
		public SessionAction(IAction action, ISession session) {
			this.session = session;
			this.action = action;
		}
	}
}
