package authentication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import logging.CRUDLogging;
import minidb.core.exceptions.InvalidTableNameException;
import minidb.core.exceptions.InvalidUserException;
import minidb.core.model.action.Select;
import minidb.core.model.data.ISession;
import minidb.core.security.AccessManager;

public aspect Authentication {
	public static final String[] filterList = {CRUDLogging.LOGGING_COLUMNS[2]+"_"+CRUDLogging.LOGGING_TABLE};
	
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

	String around(Select select) : sessionSelect(select) {
		ISession session = (ISession) thisJoinPoint.getTarget();
		AccessManager am = accessManagers.get(session.getDatabaseName());
		if(session.getDatabase().getTableNames().contains(select.getTable())) {
			try {
				if(!am.isAdmin(session.getSessionUser())) {
					filterSelect(select, session);
				}
			} catch (InvalidUserException | InvalidTableNameException e) {
				return e.getMessage();
			}
		}
		return proceed(select);
	}

	private void filterSelect(Select select, ISession session) throws InvalidTableNameException {
		String filterString;
		List<String> filterList = Arrays.asList(Authentication.filterList);
		if(select.getSelect().size() == 1 && select.getSelect().get(0).equals("*")) {
			select.getSelect().remove(0);
			for(String column : session.getDatabase().getColumns(select.getTable())) {
				filterString = column + "_" + select.getTable();
				if(!filterList.contains(filterString)) {
					select.getSelect().add(column);
				}
			}
		} else {
			for(String column : select.getSelect()) {
				filterString = column + "_" + select.getTable();
				if(filterList.contains(filterString)) {
					select.getSelect().remove(column);
					continue;
				}
			}
		}
	}
	
	after(String db, AccessManager am) returning : newAM(db, am) {
		accessManagers.put(db, am);
	}
}
