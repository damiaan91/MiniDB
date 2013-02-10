package minidb.aspects;

import java.util.Date;

import aspects.core.AbstractStatistics;
import minidb.core.model.action.IAction;
import minidb.core.model.data.ISession;
import minidb.core.config.Defaults;

/**
 * Aspect for monitoring and adapting statistics.
 * @author Damiaan
 */
public aspect Statistics extends AbstractStatistics {
	//CONSTANTS
	public static final String USER_LAST_LOGIN = "_last_login";
	public static final String USER_ACTION_COUNT = "_action_count";
	public static final String SUCCESSFULL_LOGINS = "_successfull_logins";
	public static final String FAILED_LOGINS = "_failed_logins";
	public static final String PERFORMED_ACTIONS = "_performed_actions";
	public static final String USERS_LAST_ACTION = "_last_actions";
	
	/**
	 * Constructor of statistics to setup required stats.
	 */
	public Statistics() {
		statNames.add(Defaults.databaseName+PERFORMED_ACTIONS);
		statNames.add(Defaults.databaseName+FAILED_LOGINS);
		statNames.add(Defaults.databaseName+SUCCESSFULL_LOGINS);
	}
	
	/*
	 * (non-Javadoc)
	 * @see aspects.core.AbstractStatistics#startUp()
	 */
	public pointcut startUp() : call((*..Database+ && !*..Database).new(String));
	
	/**
	 * Pointcut when a user tries to login.
	 */
	public pointcut login() : call(* minidb.core.model.data.SecureDatabase.login(..));
	
	/**
	 * Pointcut when an action is passed to a database.
	 * @param s The session to execute the action with.
	 */
	public pointcut executAction(ISession s) : 
		call(* *.ExecuteUsing(ISession)) &&
		args(s);
	
	/**
	 * Monitor of successful action statistics.
	 * @param a Action that is executed.
	 * @param s Session that is used to execute the action with.
	 */
	after(IAction a, ISession s) returning : InDatabaseLogging.CRUD(a, s) {
		changeStat(Defaults.databaseName+PERFORMED_ACTIONS, getStatAsLong(Defaults.databaseName+PERFORMED_ACTIONS)+1);
	}
	
	/**
	 * Monitor failed and successful login attempts of a database.
	 * @param s The Session which is returned after login.
	 */
	after() returning (ISession s) : login() {
		if(s == null) {
			changeStat(Defaults.databaseName+FAILED_LOGINS, getStatAsLong(Defaults.databaseName+FAILED_LOGINS)+1);
		} else {
			changeStat(Defaults.databaseName+SUCCESSFULL_LOGINS, getStatAsLong(Defaults.databaseName+SUCCESSFULL_LOGINS)+1);
		}
	}
	
	/**
	 * Prevent flooding the database with actions by one user. The uses will be kicked if he executes request 6 actions within 5 seceonds.
	 * @param s The session used to execute the action with.
	 * @return The results of the action or if flooding is detected; you have been kicked message.
	 */
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
