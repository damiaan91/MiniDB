package authentication;

import java.util.HashMap;

import org.aspectj.lang.ProceedingJoinPoint;

import logging.CRUDLogging;
import minidb.core.exceptions.InvalidUserException;
import minidb.core.model.action.Select;
import minidb.core.model.data.ISession;
import minidb.core.security.AccessManager;

public aspect Authentication {
	public static final String[] filterList = {CRUDLogging.LOGGING_TABLE+"_"+CRUDLogging.LOGGING_COLUMNS[2]};
	
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
	//TODO: filter
	String around(Select select) : sessionSelect(select) {
		ISession session = (ISession) thisJoinPoint.getTarget();
		AccessManager am = accessManagers.get(session.getDatabaseName());
		try {
			if(!am.isAdmin(session.getSessionUser())) {
				return "test";
			}
		} catch (InvalidUserException e) {
			e.printStackTrace();
		}
		return proceed(select);
	}
	
	after(String db, AccessManager am) returning : newAM(db, am) {
		accessManagers.put(db, am);
	}
	

}
