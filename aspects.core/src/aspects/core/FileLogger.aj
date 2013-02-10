package aspects.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A logger implementation to log to a file.
 * @author Damiaan
 *
 */
public abstract aspect FileLogger extends AbstractLogger<String> {
	/**
	 * property to enable the file logging (default: enabled).
	 */
	protected boolean FILE_LOGGING_ENABLED = true;
	
	/*
	 * (non-Javadoc)
	 * @see aspects.core.AbstractLogger#handleLogMessage(java.lang.Object)
	 */
	@Override
	protected void handleLogMessage(String line) {
		writeLine(line);		
	}
	
	/**
	 * Write a line to file.
	 * @param s Log line to write.
	 */
	protected final void writeLine(String s) {
		writeLine(s, null);
	}

	/**
	 * Writes to a log.txt file
	 * @param s
	 */
	protected void writeLine(String s, Object prefix) {
		if (FILE_LOGGING_ENABLED) {
			String file = "log.txt";
			if(prefix != null) file = prefix.toString() + "-" + file;
			echoLine(s);
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
				out.write(getTimeStamp(s) + System.getProperty("line.separator"));
				out.close();
			} catch (IOException e) {
				System.err.println("error: could not open log.txt");
			}
		}
	}
}
