package logging;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import minidb.core.config.Defaults;
import minidb.core.model.action.Insert;
import minidb.core.model.action.Select;
import minidb.core.model.data.SecureDatabase;

public aspect CRUDLogging {
	public static final String LOGGING_TABLE = "sys.log";
	
	pointcut select(Select s) : call(* *.*Select(Select, String)) && args(s, *);
	pointcut create() : call(* *.*Create(..));
	pointcut insert() : call(* *.*Insert(..));
	
	pointcut dbStart(SecureDatabase db) : 
		execution((*..Database+ && !*..Database).new(..)) && target(db);
	
	
	pointcut selectLog(SecureDatabase db, Select s) :
		select(s) && target(db);
	

	after(SecureDatabase db) : dbStart(db) {
		System.out.println("Setting up logging...");
		String[] columns = new String[2];
		columns[0] = "timestamp";
		columns[1] = "message";
		try {
			db.createTable(LOGGING_TABLE, columns);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	before(SecureDatabase db, Select s) : selectLog(db, s) {
		Insert insert = new Insert(LOGGING_TABLE);
		insert.addValue(new SimpleDateFormat("hh:mm:ss").format(GregorianCalendar.getInstance().getTime()));
		insert.addValue(s.toString());
		try {
			db.executeInsert(insert, Defaults.adminUsernam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
