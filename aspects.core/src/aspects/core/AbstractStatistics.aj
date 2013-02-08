package aspects.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public abstract aspect AbstractStatistics {
	
	private Properties stats = new Properties();
	protected String fileName = "statistics2.txt";
	protected List<String> statNames = new ArrayList<String>();
	
	protected abstract pointcut startUp();
	
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
	
	public Long getStatAsLong(final String statName) {
		Long result = null;
		if(stats.containsKey(statName)) {
			result = new Long(stats.getProperty(statName));
		}
		return result;
	}
	
	public String getStat(final String statName) {
		String result = null;
		if(stats.containsKey(statName)) {
			result = stats.getProperty(statName);
		}
		return result;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void changeStat(final String statName, final long l) {
		changeStat(statName, String.valueOf(l));
	}
	
	public void changeStat(final String statName, final String value) {
		stats.setProperty(statName, value);
		saveStats();
	}
	
	public void addStat(final String key) {
		addStat(key, 0);
	}
	
	public void addStat(final String key, final long value) {
		addStat(key, String.valueOf(value));
	}
	
	public void addStat(final String key, final String value) {
		if(!stats.containsKey(key)) {
			stats.setProperty(key, value);
			saveStats();
		}
		if(!statNames.contains(key)) {
			statNames.add(key);
		}
	}
	
	public boolean containsKey(final String key) {
		return stats.containsKey(key);
	}
	
	private void saveStats() {
		try {
			stats.store(new FileOutputStream(fileName), fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
