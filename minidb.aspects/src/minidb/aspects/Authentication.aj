package minidb.aspects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import minidb.core.exceptions.InvalidTableNameException;
import minidb.core.exceptions.InvalidUserException;
import minidb.core.exceptions.MiniDBCoreException;
import minidb.core.model.action.Insert;
import minidb.core.model.action.Select;
import minidb.core.model.data.ISession;
import minidb.core.security.AccessManager;
import minidb.core.model.data.SecureDatabase.SecureDBSession;

/**
 * Authentication aspects for the MiniDB application.
 * @author Damiaan
 *
 */
public aspect Authentication {
	/**
	 * List of columns to filter if the user is not an admin.
	 */
	public static final String[] filterList = { InDatabaseLogging.LOGGING_COLUMNS[2]
			+ "_" + InDatabaseLogging.LOGGING_TABLE };
	
	/**
	 * Scanner for reading console input.
	 */
	private Scanner in = new Scanner(System.in);
	
	/**
	 * Counter for amount of attempts a user tries to login.
	 */
	private int counter = 0;
	
	/**
	 * Access managers of all monitored databases.
	 */
	private HashMap<String, AccessManager> accessManagers = new HashMap<String, AccessManager>();
	
	/**
	 * Pointcut when a database is instanced.
	 * @param name The name of the instanced database.
	 */
	pointcut dbStart(String name) : 
		execution((*..Database+ && !*..Database).new(..)) && 
		args(name);
	
	/**
	 * Pointcut when a new access manager is instanced for a database.
	 * @param db The name of database.
	 * @param am The access manager.
	 */
	pointcut newAM(String db, AccessManager am) : 
		execution(*..AccessManager.new(..)) && 
		cflow(dbStart(db)) &&
		target(am);
	
	/**
	 * Pointcut when a session is executed.
	 * @param select The select action to execute.
	 */
	pointcut sessionSelect(Select select) : 
		call(* *..ISession.select(Select)) &&
		args(select) && 
		!within(Authentication);
	
	/**
	 * Pointcut when a client tries to login.
	 * @param username The username used to login.
	 * @param password The password used to login.
	 */
	pointcut clientLogin(String username, String password) : 
		databaseLogin(username, password) && 
		cflow(call(* minidb.client.view.Client.login())) && 
		!within(Util);
	
	/**
	 * Poitcut when a login is requested for a database.
	 * @param username The username used to login.
	 * @param password The password used to login.
	 */
	pointcut databaseLogin(String username, String password) :
		call(* minidb.core.model.data.SecureDatabase.login(String, String)) &&
		args(username, password);
	
	/**
	 * List of admin methods.
	 */
	pointcut adminMethods() : 
		execution(String minidb.core.model.action.CreateUser.ExecuteUsing(ISession)) ||
		execution(String minidb.core.model.action.GrantPrivilege.ExecuteUsing(ISession));
	
	/**
	 * List of read methods.
	 */
	pointcut readMethods() :
		execution(String minidb.core.model.action.Select.ExecuteUsing(ISession));
	
	/**
	 * List of write methods.
	 */
	pointcut writeMethods() :
		execution(String minidb.core.model.action.Insert.ExecuteUsing(ISession));
	
	/**
	 * Filter for SecureDBSessions.
	 * @param session The filtered SecureDBSession.
	 */
	pointcut dbSecureSession(ISession session) :
		args(session) && if(session instanceof SecureDBSession);
	
	/**
	 * Advice to monitor the login attempts
	 * @param username The username attempts to login with.
	 * @param password The password attempts to login with.
	 */
	after(String username, String password) : clientLogin(username, password) {
		counter++;
	}
	
	/**
	 * Advice for changing the flow of the login. After 3 login attempts the login process is over.
	 * @param username The username used to login.
	 * @param password The password used to login.
	 * @return A session is the user is successfully logged in.
	 */
	ISession around(String username, String password) : clientLogin(username, password) {
		ISession session = null;
		try {
			session = proceed(username, password);
		} catch (MiniDBCoreException e) {
			System.out.println(e.getMessage());
		}
		while (session == null && counter < 3) {
			System.out.println("Failed to login (" + counter + ")");
			System.out.print("Login: ");
			username = in.nextLine();
			System.out.print("Password: ");
			password = in.nextLine();
			try {
				session = proceed(username, password);
			} catch (MiniDBCoreException e) {
				System.out.println(e.getMessage());
			}
		}
		counter = 0;
		return session;
	}
	
	/**
	 * Advice used to filter columns of a select action.
	 * @param select The select action to filter.
	 * @return The result of the select.
	 */
	String around(Select select) : sessionSelect(select) {
		ISession session = (ISession) thisJoinPoint.getTarget();
		AccessManager am = accessManagers.get(session.getDatabaseName());
		if (session.getDatabase().getTableNames().contains(select.getTable())) {
			try {
				if (!am.isAdmin(session.getSessionUser())) {
					filterSelect(select, session);
				}
			} catch (InvalidUserException | InvalidTableNameException e) {
				return e.getMessage();
			}
		}
		return proceed(select);
	}
	
	/**
	 * Filter the columns of a select action.
	 * @param select The select action.
	 * @param session The session to execute the selection action with.
	 * @throws InvalidTableNameException When table of the select does not exists.
	 */
	private void filterSelect(Select select, ISession session)
			throws InvalidTableNameException {
		String filterString;
		List<String> filterList = Arrays.asList(Authentication.filterList);
		if (select.getSelect().size() == 1
				&& select.getSelect().get(0).equals("*")) {
			select.getSelect().remove(0);
			for (String column : session.getDatabase().getColumns(
					select.getTable())) {
				filterString = column + "_" + select.getTable();
				if (!filterList.contains(filterString)) {
					select.getSelect().add(column);
				}
			}
		} else {
			for (String column : select.getSelect()) {
				filterString = column + "_" + select.getTable();
				if (filterList.contains(filterString)) {
					select.getSelect().remove(column);
					continue;
				}
			}
		}
	}
	
	/**
	 * Monitor access managers of instanced databases.
	 * @param db The database.
	 * @param am The access manager.
	 */
	after(String db, AccessManager am) returning : newAM(db, am) {
		accessManagers.put(db, am);
	}
	
	/**
	 * Advice to handle admin method.
	 * @param session The session to execute the action with.
	 * @return The result of the action of if not allowed a information message why not.
	 */
	String around(ISession session) : adminMethods() && dbSecureSession(session) {
		AccessManager am = accessManagers.get(session.getDatabaseName());
		try {
			if(am.isAdmin(session.getSessionUser())) {
				return proceed(session);
			}
		} catch (InvalidUserException e) {
			return e.getMessage();
		}
		return "You have no admin right to do this."; 
	} 
	
	/**
	 * Advice to handle read methods.
	 * @param session The Session to execute the action with.
	 * @return The result of the action of if not allowed a information message why not.
	 */
	String around(ISession session) : readMethods() && dbSecureSession(session) { 
		AccessManager am = accessManagers.get(session.getDatabaseName());
		String targetTable = null;
		if(thisJoinPoint.getTarget() instanceof Select) {
			targetTable = ((Select) thisJoinPoint.getTarget()).getTable();
		}
		try {
			if(am.hasReadAccess(session.getSessionUser(), targetTable)) {
				return proceed(session);
			}
		} catch (InvalidUserException e) {
			return e.getMessage();
		}
		return "You have no read access to do this."; 
	} 
	
	/**
	 * Advice to handle write methods.
	 * @param session The session to execute the action with.
	 * @return The result of the action of if not allowed a information message why not.
	 */
	String around(ISession session) : writeMethods() && dbSecureSession(session) { 
		AccessManager am = accessManagers.get(session.getDatabaseName());
		String targetTable = null;
		if(thisJoinPoint.getTarget() instanceof Insert) {
			targetTable = ((Insert) thisJoinPoint.getTarget()).getTable();
		}
		try {
			if(am.hasWriteAccess(session.getSessionUser(), targetTable)) {
				return proceed(session);
			}
		} catch (InvalidUserException e) {
			return e.getMessage();
		}
		return "You have no write access to do this."; 
	}

}
