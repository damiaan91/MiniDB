package minidb.core.security;

import java.util.HashMap;

import minidb.core.config.Defaults;
import minidb.core.exceptions.InsufficientRightsExcpetion;
import minidb.core.exceptions.InvalidUserException;
import minidb.core.exceptions.UserAlreadyExistsException;
import minidb.core.model.data.Database;

/**
 * @author  Damiaan
 */
public class AccessManager {
	
	private HashMap<String, Rule> rules = new HashMap<String, Rule>();
	/**
	 * @uml.property  name="userManager"
	 * @uml.associationEnd  
	 */
	private UserManager userManager;
	private String databaseName;
	
	public AccessManager(Database database) throws UserAlreadyExistsException{
		this.databaseName = database.getName();
		userManager = new UserManager(database);
		userManager.addUser(Defaults.adminUsernam, Defaults.adminPassword, true);
	}
	
	public void grandFullAccess(String username, String tableName, String currentUser) throws InsufficientRightsExcpetion, InvalidUserException {
		grantReadAccess(username, tableName, currentUser);
		grantWriteAccess(username, tableName, currentUser);
	}
	
	public void grantReadAccess(String username, String tableName, String currentUser) throws InsufficientRightsExcpetion, InvalidUserException {
		if(!userManager.isAdmin(currentUser)) throw new InsufficientRightsExcpetion(currentUser, databaseName, "grant rights");
		
		String key = createKey(username, tableName);
		if(rules.containsKey(key)) {
			rules.get(key).writeAccess = true;
		} else {
			rules.put(key, new Rule(tableName, true, false));
		}
	}
	
	public void grantWriteAccess(String username, String tableName, String currentUser) throws InvalidUserException, InsufficientRightsExcpetion {
		if(!userManager.isAdmin(currentUser)) throw new InsufficientRightsExcpetion(currentUser, databaseName, "grant rights");
		
		String key = createKey(username, tableName);
		if(rules.containsKey(key)) {
			rules.get(key).writeAccess = true;
		} else {
			rules.put(key, new Rule(tableName, false, true));
		}
	}
	
	public boolean hasWriteAccess(String username, String tableName) throws InvalidUserException {
		if (userManager.isAdmin(username)) return true;
		String key = createKey(username, tableName);
		if(rules.containsKey(key)) {
			return rules.get(key).writeAccess;
		}
		return false;
	}
	
	public boolean hasReadAccess(String username, String tableName) throws InvalidUserException {
		if (userManager.isAdmin(username)) return true;
		String key = createKey(username, tableName);
		if(rules.containsKey(key)) {
			return rules.get(key).readAccess;
		}
		return false;
	}
	
	public void addUser(String username, String password, boolean isAdmin) throws UserAlreadyExistsException {
		userManager.addUser(username, password, isAdmin);
	}
	
	public void deleteUser(String username) throws UserAlreadyExistsException, InvalidUserException {
		userManager.deleteUser(username);
	}
	
	public boolean isAdmin(String username) throws InvalidUserException {
		return userManager.isAdmin(username);
	}
	
	public boolean isValidLogin(String username, String password) throws InvalidUserException {
		return userManager.isValidLogin(username, password);
	}
	
	private String createKey(final String userName, final String tableName) {
		return userName + "_" + tableName;
	}
	

	private class Rule {
		private String tableName;
		private boolean readAccess;
		private boolean writeAccess;
		
		public Rule (String table, boolean readAccess, boolean writeAccess) {
			this.tableName = table;
			this.readAccess = readAccess;
			this.writeAccess = writeAccess;
		}
	}

}
