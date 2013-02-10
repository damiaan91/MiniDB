package aspects.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * A base to create a statistics aspect.
 * @author Damiaan
 *
 */
public abstract aspect AbstractStatistics {
	
	/**
	 * Properties field containing the defined statistics
	 */
	private Properties stats = new Properties();
	
	/**
	 * The file to store the statistics.
	 */
	protected String fileName = "statistics2.txt";
	
	/**
	 * A list of enabled statistics.
	 */
	protected List<String> statNames = new ArrayList<String>();
	
	/**
	 * Pointctus used to configurate the statistics.
	 */
	protected abstract pointcut startUp();
	
	/**
	 * Advice for configuration the statistics.
	 */
	before() : startUp() {
		if(new File(fileName).isFile()) {
			try {
				stats.load(new FileInputStream(fileName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Iterator<String> it = statNames.iterator();
		while(it.hasNext()) {
			addStat(it.next());
		}
		saveStats();
	}
	
	/**
	 * Get the statistic as a long.
	 * @param statName The name of the statistics you want.
	 * @return The asked statistic as a Long.
	 */
	public Long getStatAsLong(final String statName) {
		Long result = null;
		if(stats.containsKey(statName)) {
			result = new Long(stats.getProperty(statName));
		}
		return result;
	}
	
	/**
	 * Get the statistics as a String.
	 * @param statName The name of the statistics you want.
	 * @return The asked statistic as a String.
	 */
	public String getStat(final String statName) {
		String result = null;
		if(stats.containsKey(statName)) {
			result = stats.getProperty(statName);
		}
		return result;
	}
	
	/**
	 * Get the file name of the file where the statistics are stored in.
	 * @return The file name of statistics file.
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Change a statistic using a long.
	 * @param statName The statistic you want to change.
	 * @param l The Long value to change the statistic with.
	 */
	public void changeStat(final String statName, final long l) {
		changeStat(statName, String.valueOf(l));
	}
	
	/**
	 * Change a statisitc using a String.
	 * @param statName The statis you want to change.
	 * @param value The String value to change the statistic with.
	 */
	public void changeStat(final String statName, final String value) {
		stats.setProperty(statName, value);
		saveStats();
	}
	
	/**
	 * Add a new statistic.
	 * @param key New statistic name.
	 */
	public void addStat(final String key) {
		addStat(key, 0);
	}
	
	/**
	 * Add a new statistic with a default Long value.
	 * @param key New statistic name.
	 * @param value Long value to initiate the statistic with.
	 */
	public void addStat(final String key, final long value) {
		addStat(key, String.valueOf(value));
	}
	
	/**
	 * Add a new static with a default String value.
	 * @param key New statistic name.
	 * @param value String value to initiate the statistic with.
	 */
	public void addStat(final String key, final String value) {
		if(!stats.containsKey(key)) {
			stats.setProperty(key, value);
			saveStats();
		}
		if(!statNames.contains(key)) {
			statNames.add(key);
		}
	}
	
	/**
	 * Look if the a statistic is present.
	 * @param key Statistic to look for
	 * @return True if the statistic is present.
	 */
	public boolean containsKey(final String key) {
		return stats.containsKey(key);
	}
	
	/**
	 * Save stats to statistic file.
	 */
	private void saveStats() {
		try {
			stats.store(new FileOutputStream(fileName), fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
