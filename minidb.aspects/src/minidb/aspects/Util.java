package minidb.aspects;

import minidb.core.config.Defaults;
import minidb.core.exceptions.InvalidUserException;
import minidb.core.model.action.CreateTable;
import minidb.core.model.action.CreateUser;
import minidb.core.model.data.ISession;
import minidb.core.model.data.SecureDatabase;

/**
 * Util class for MiniDB aspects.
 * @author Damiaan
 *
 */
public class Util {
	/**
	 * private constructor because of util class.
	 */
	private Util() {};
	
	/**
	 * Create a new table.
	 * @param tableName The new table name.
	 * @param columns The columns for the new table.
	 * @param session The session used to create a new table.
	 */
	public static void createTable(String tableName, String[] columns, ISession session) {
		CreateTable createLoggingTable = new CreateTable(tableName);
		createLoggingTable.addColumns(columns);
		session.create(createLoggingTable);
	}
	
	/**
	 * Create a new system/admin user and directly login with it.
	 * @param db The database to add the new user.
	 * @param username The username to create the new account with.
	 * @param password The password to create the new account with.
	 * @return The session of the new logged in user. 
	 */
	public static ISession createSystemUser(SecureDatabase db, String username, String password) {
		try {
			ISession session = db.login(Defaults.adminUsernam, Defaults.adminUsernam);
			session.createUser(new CreateUser(username, password, true));
			return session = db.login(username, password);
		} catch (InvalidUserException e) {
			System.out.println(e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
