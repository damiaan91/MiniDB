package aspects.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public abstract aspect FileLogger extends AbstractLogger<String> {
	protected boolean FILE_LOGGING_ENABLED = true;

	@Override
	protected void handleLogMessage(String line) {
		writeLine(line);		
	}
	
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
