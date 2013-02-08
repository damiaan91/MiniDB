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


/**
 * @author  Damiaan
 */
public class SecureDatabase extends Database {

	/**
	 * @uml.property  name="accessManager"
	 * @uml.associationEnd  
	 */
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
	
	/**
	 * @author  Damiaan
	 */
	public class SecureDBSession extends DbSession {
		/**
		 * @uml.property  name="sessionUser"
		 */
		private String sessionUser;
		/**
		 * @uml.property  name="isActive"
		 */
		private boolean isActive;
		/**
		 * @uml.property  name="accessManager"
		 * @uml.associationEnd  
		 */
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
				return db.executeSelect(select);
			} catch (InvalidTableNameException e) {
				return e.getMessage();
			}
		}

		@Override
		public String insert(Insert insert) {
			try {
				return db.executeInsert(insert);
			} catch (InvalidTableNameException | InvalidAmountOfInsertValues e) {
				return e.getMessage();
			}
		}

		@Override
		public String alter(Alter alter) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String create(CreateTable create) {
			try {
				return db.executeCreate(create);
			} catch (InvalidTableNameException| ColumnAlreadyExistsException e) {
				return e.getMessage();
			}
		}

		/**
		 * @return
		 * @uml.property  name="isActive"
		 */
		@Override
		public boolean isActive() {
			return isActive;
		}

		@Override
		public void disconnect() {
			isActive = false;
		}

		/**
		 * @return
		 * @uml.property  name="sessionUser"
		 */
		@Override
		public String getSessionUser() {
			return sessionUser;
		}

		@Override
		public String createUser(CreateUser createUser) {
			try {
				accessManager.addUser(createUser.getUsername(), createUser.getPassword(), createUser.isAdmin());
				return "User is succesfully added.";
			} catch (UserAlreadyExistsException e) {
				return e.getMessage();
			}
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
