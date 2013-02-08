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

public aspect Authentication {
	public static final String[] filterList = { InDatabaseLogging.LOGGING_COLUMNS[2]
			+ "_" + InDatabaseLogging.LOGGING_TABLE };

	private Scanner in = new Scanner(System.in);
	private int counter = 0;

	private HashMap<String, AccessManager> accessManagers = new HashMap<String, AccessManager>();

	pointcut dbStart(String name) : 
		execution((*..Database+ && !*..Database).new(..)) && 
		args(name);

	pointcut newAM(String db, AccessManager am) : 
		execution(*..AccessManager.new(..)) && 
		cflow(dbStart(db)) &&
		target(am);

	pointcut sessionSelect(Select select) : 
		call(* *..ISession.select(Select)) &&
		args(select) && 
		!within(Authentication);

	pointcut clientLogin(String username, String password) : 
		databaseLogin(username, password) && 
		cflow(call(* minidb.client.view.Client.login())) && 
		!within(Util);

	pointcut databaseLogin(String username, String password) :
		call(* minidb.core.model.data.SecureDatabase.login(String, String)) &&
		args(username, password);

	pointcut adminMethods() : 
		execution(String minidb.core.model.action.CreateUser.ExecuteUsing(ISession)) ||
		execution(String minidb.core.model.action.GrantPrivilege.ExecuteUsing(ISession));

	pointcut readMethods() :
		execution(String minidb.core.model.action.Select.ExecuteUsing(ISession));

	pointcut writeMethods() :
		execution(String minidb.core.model.action.Insert.ExecuteUsing(ISession));
	
	pointcut dbSecureSession(ISession session) :
		args(session) && if(session instanceof SecureDBSession);

	after(String username, String password) : clientLogin(username, password) {
		counter++;
	}

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

	after(String db, AccessManager am) returning : newAM(db, am) {
		accessManagers.put(db, am);
	}

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
