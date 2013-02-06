package minidb.aspects;

import minidb.core.config.Defaults;
import minidb.core.exceptions.InvalidUserException;
import minidb.core.model.action.CreateTable;
import minidb.core.model.action.CreateUser;
import minidb.core.model.data.ISession;
import minidb.core.model.data.SecureDatabase;

public class Util {
	private Util() {};
	
	public static void createTable(String tableName, String[] columns, ISession session) {
		CreateTable createLoggingTable = new CreateTable(tableName);
		createLoggingTable.addColumns(columns);
		session.create(createLoggingTable);
	}

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
