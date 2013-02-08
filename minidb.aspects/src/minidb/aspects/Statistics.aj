package minidb.aspects;

import java.util.Date;

import aspects.core.AbstractStatistics;
import minidb.core.model.action.IAction;
import minidb.core.model.data.ISession;
import minidb.core.config.Defaults;

public aspect Statistics extends AbstractStatistics {
	public static final String USER_LAST_LOGIN = "_last_login";
	public static final String USER_ACTION_COUNT = "_action_count";
	public static final String SUCCESSFULL_LOGINS = "_successfull_logins";
	public static final String FAILED_LOGINS = "_failed_logins";
	public static final String PERFORMED_ACTIONS = "_performed_actions";
	public static final String USERS_LAST_ACTION = "_last_actions";
	
	public Statistics() {
		statNames.add(Defaults.databaseName+PERFORMED_ACTIONS);
		statNames.add(Defaults.databaseName+FAILED_LOGINS);
		statNames.add(Defaults.databaseName+SUCCESSFULL_LOGINS);
	}
	
	public pointcut startUp() : call((*..Database+ && !*..Database).new(String));
	public pointcut login() : call(* minidb.core.model.data.SecureDatabase.login(..));
	public pointcut executAction(ISession s) : 
		call(* *.ExecuteUsing(ISession)) &&
		args(s);
	
	after(IAction a, ISession s) returning : InDatabaseLogging.CRUD(a, s) {
		changeStat(Defaults.databaseName+PERFORMED_ACTIONS, getStatAsLong(Defaults.databaseName+PERFORMED_ACTIONS)+1);
	}
	
	after() returning (ISession s) : login() {
		if(s == null) {
			changeStat(Defaults.databaseName+FAILED_LOGINS, getStatAsLong(Defaults.databaseName+FAILED_LOGINS)+1);
		} else {
			changeStat(Defaults.databaseName+SUCCESSFULL_LOGINS, getStatAsLong(Defaults.databaseName+SUCCESSFULL_LOGINS)+1);
		}
	}
	
	String around(ISession s) : executAction(s) {
		Long now = new Date().getTime();
		String key = Defaults.databaseName+"_"+s.getSessionUser()+USERS_LAST_ACTION;
		if(!containsKey(key)) {
			changeStat(key, now+";");
		} else {
			String stat = getStat(key);
			String[] past = stat.split(";");
			if(past.length < 6) {
				changeStat(key, stat + now + ";");
			} else {
				Long pastTime = Long.parseLong(past[0]);
				if (now - pastTime < 5000) {
					s.disconnect();
					return "You have been kicked from the database (reason: flodding the database)";
				} else {
					String newTime = new String();
					for (int i = 1; i < past.length; i++) {
						newTime += past[i]+";";
					}
					changeStat(key, newTime + now + ";");
				}
			}
		}
		return proceed(s);
	}
}
