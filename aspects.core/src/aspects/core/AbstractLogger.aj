package aspects.core;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public abstract aspect AbstractLogger<T> {
	protected String DATETIME_FORMAT = "hh:mm:ss";
	protected String LOG_PREFIX = "ASPECT";
	protected boolean CONSOLE_LOGGING_ENABLED = true;
	
	protected abstract pointcut logMethods();
	
	after() returning (T s) : logMethods() && !within(AbstractLogger) {
		handleLogMessage(s);
	}
	
	protected abstract void handleLogMessage(T line);
	
	protected final String getTimeStamp() {
		return getTimeStamp(null);
	}
	
	protected String getTimeStamp(String s){
		String timestamp = "[" + new SimpleDateFormat(DATETIME_FORMAT).format(GregorianCalendar.getInstance().getTime())+"]";
		return (s == null ? timestamp : timestamp + s);
	}
	
	protected void echoLine(String s){
		if(CONSOLE_LOGGING_ENABLED) System.out.println("["+LOG_PREFIX+"]: "+ s);
	}
}
