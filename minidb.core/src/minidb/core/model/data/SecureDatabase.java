package minidb.core.model.data;

import minidb.core.exceptions.ColumnAlreadyExistsException;
import minidb.core.exceptions.InsufficientRightsExcpetion;
import minidb.core.exceptions.InvalidAmountOfInsertValues;
import minidb.core.exceptions.InvalidTableNameException;
import minidb.core.exceptions.InvalidUserException;
import minidb.core.exceptions.UserAlreadyExistsException;
import minidb.core.model.action.Alter;
import minidb.core.model.action.CreateTable;
import minidb.core.model.action.CreateUser;
import minidb.core.model.action.GrantPrivilege;
import minidb.core.model.action.Insert;
import minidb.core.model.action.Select;
import minidb.core.security.AccessManager;


public class SecureDatabase extends Database {

	private AccessManager accessManager;
	
	public SecureDatabase(String name) {
		super(name);
		try {
			accessManager = new AccessManager(this);
		} catch (UserAlreadyExistsException e) {
			e.printStackTrace();
		}
	}

	public ISession login(String username, String password) throws InvalidUserException {
		if (accessManager.isValidLogin(username, password)) {
			return new SecureDBSession(this, username);
		}
		return null;
	}
	
	public class SecureDBSession extends DbSession {
		private String sessionUser;
		private boolean isActive;
		private AccessManager accessManager;
		
		private SecureDBSession(SecureDatabase db, String username) {
			super(db);
			this.accessManager = db.accessManager;
			this.sessionUser = username;
			this.isActive = true;
		}
		
		@Override
		public String select(Select select) {
			try {
				if(accessManager.hasReadAccess(sessionUser, select.getTable())) {
					return db.executeSelect(select);
				}
			} catch (InvalidUserException | InvalidTableNameException e) {
				return e.getMessage();
			}
			return "You have no rights to read this table.";
		}

		@Override
		public String insert(Insert insert) {
			try {
				if(accessManager.hasWriteAccess(sessionUser, insert.getTable())) {
					return db.executeInsert(insert);
				}
			} catch (InvalidUserException | InvalidTableNameException
					| InvalidAmountOfInsertValues e) {
				return e.getMessage();
			}
			return "You have no rights to write in this table.";
		}

		@Override
		public String alter(Alter alter) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String create(CreateTable create) {
			try {
				if(accessManager.isAdmin(sessionUser)) {
					return db.executeCreate(create);
				}
			} catch (InvalidUserException | InvalidTableNameException
					| ColumnAlreadyExistsException e) {
				return e.getMessage();
			}
			return "You have no rights to create tables.";
		}

		@Override
		public boolean isActive() {
			return isActive;
		}

		@Override
		public void disconnect() {
			isActive = false;
		}

		@Override
		public String getSessionUser() {
			return sessionUser;
		}

		@Override
		public String createUser(CreateUser createUser) {
			try {
				if(accessManager.isAdmin(sessionUser)) {
					accessManager.addUser(createUser.getUsername(), createUser.getPassword(), createUser.isAdmin());
				}
			} catch (InvalidUserException | UserAlreadyExistsException e) {
				return e.getMessage();
			}
			return "You have no rights to create users.";
		}

		@Override
		public String grantPrivilege(GrantPrivilege grantPrivilege) {
			try {
				if(grantPrivilege.isReadAccess()) {
					accessManager.grantReadAccess(grantPrivilege.getUser(), grantPrivilege.getTable(), sessionUser);
				}
				if(grantPrivilege.isWriteAccess()) {
					accessManager.grantWriteAccess(grantPrivilege.getUser(), grantPrivilege.getTable(), sessionUser);
				}
			} catch (InvalidUserException | InsufficientRightsExcpetion e) {
				return e.getMessage();
			}
			return null;
		}
	}
}