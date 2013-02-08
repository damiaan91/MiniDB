package aspects.core;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 * A abstract aspect used a base for a logger
 * @author Damiaan
 *
 * @param <T> Input argument of the method to log.
 */
public abstract aspect AbstractLogger<T> {
	protected String DATETIME_FORMAT = "hh:mm:ss";
	protected String LOG_PREFIX = "ASPECT";
	protected boolean CONSOLE_LOGGING_ENABLED = true;
	
	/**
	 * pointcut to methods which need to be logged.
	 */
	protected abstract pointcut logMethods();
	
	/**
	 * After a succesfully executed logMethod this advice is triggered.
	 * @param s object used for logging.
	 */
	after() returning (T s) : logMethods() && !within(AbstractLogger) {
		handleLogMessage(s);
	}
	
	/**
	 * Implementation of the logger.
	 * @param line Line to log.
	 */
	protected abstract void handleLogMessage(T line);
	
	/**
	 * Creates a timestamp in the DATETIME_FORMAT.
	 * @return a timestamp as String.
	 */
	protected final String getTimeStamp() {
		return getTimeStamp(null);
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	protected String getTimeStamp(String s){
		String timestamp = "[" + new SimpleDateFormat(DATETIME_FORMAT).format(GregorianCalendar.getInstance().getTime())+"]";
		return (s == null ? timestamp : timestamp + s);
	}
	
	/**
	 * Print line to console.
	 * @param s line to print.
	 */
	protected void echoLine(String s){
		if(CONSOLE_LOGGING_ENABLED) System.out.println("["+LOG_PREFIX+"]: "+ s);
	}
}
