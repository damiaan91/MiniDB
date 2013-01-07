package minidb.core.security;

import java.util.HashMap;


import minidb.core.exceptions.InvalidUserException;
import minidb.core.exceptions.UserAlreadyExistsException;
import minidb.core.model.data.Database;

public class UserManager {
	
	private HashMap<String, User> users = new HashMap<String, User>();
	private String databaseName;
	
	public UserManager(Database database) {
		this.databaseName = database.getName();
	}
	
	public void addUser(String username, String password, boolean isAdmin) throws UserAlreadyExistsException {
		if(users.containsKey(username)) throw new UserAlreadyExistsException(username, databaseName); 
		
		users.put(username, new User(username, password, isAdmin));
	}
	
	public void deleteUser(String username) throws InvalidUserException {
		if(!users.containsKey(username)) throw new InvalidUserException(username, databaseName);
		
		users.remove(username);
	}
	
	public void grantAdminRights(String username) throws InvalidUserException {
		if(!users.containsKey(username)) throw new InvalidUserException(username, databaseName);
		
		users.get(username).grantAdminRights();
	}
	
	public boolean isAdmin(String username) throws InvalidUserException {
		if(!users.containsKey(username)) throw new InvalidUserException(username, databaseName);
		
		return users.get(username).isAdmin;
	}
	
	public boolean checkPassword(final String username, final String password) throws InvalidUserException {
		if(!users.containsKey(username)) throw new InvalidUserException(username, databaseName);
		
		return users.get(username).password.equals(password);
	}
	
	public boolean isValidLogin(final String username, final String password) throws InvalidUserException {
		if(!users.containsKey(username)) throw new InvalidUserException(username, databaseName);
		
		return users.get(username).password.equals(password);
	}
	
	private class User {
		private String name;
		private String password;
		private boolean isAdmin;
		private boolean canDeleteTables;
		private boolean canRemaneTables;
		
		public User(String name, String password, boolean isAdmin) {
			this.name = name;
			this.password = password;
			this.isAdmin = isAdmin;
			this.canDeleteTables = isAdmin;
			this.canRemaneTables = isAdmin;
		}
		
		public void grantAdminRights() {
			isAdmin = true;
			canDeleteTables = true;
			canRemaneTables = true;
		}
	}
}
