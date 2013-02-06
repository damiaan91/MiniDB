package minidb.aspects;

import minidb.core.model.action.Select;
import minidb.core.model.data.ISession;
import minidb.core.model.data.SecureDatabase;

public aspect Statistics {
	/*public static final String STATS_TABLE = "sys.stats";
	public static final String[] STATS_TALBE_COLUMNS = {"Stat", "Value"};
	public static final String STATS_USER = "sys.stats";
	public static final String STATS_USER_PASSWORD = "********";
	
	
	
	after(SecureDatabase db) returning : Logging.dbStart(db) {
		ISession session = Util.createSystemUser(db, STATS_USER, STATS_USER_PASSWORD);
		Util.createTable(STATS_TABLE, STATS_TALBE_COLUMNS, session);
	}
	
	after(Select a, ISession s) returning : Logging.select(a, s) {
		
	}*/

}
