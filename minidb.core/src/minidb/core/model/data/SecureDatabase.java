package minidb.core.model.data;

import java.util.Collection;

import minidb.core.exceptions.ColumnAlreadyExistsException;
import minidb.core.exceptions.InvalidAmountOfInsertValues;
import minidb.core.exceptions.InvalidTableNameException;
import minidb.core.exceptions.InvalidUserException;
import minidb.core.exceptions.UserAlreadyExistsException;
import minidb.core.model.action.Create;
import minidb.core.model.action.Insert;
import minidb.core.model.action.Select;
import minidb.core.security.AccessManager;


public class SecureDatabase extends Database {

	private AccessManager accessManager;
	
	public SecureDatabase(String name) throws UserAlreadyExistsException {
		super(name);
		accessManager = new AccessManager(this);
	}

	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public void setName(String name) {
		super.setName(name);
	}

	@Override
	public void createTable(String tName, String[] cNames)
			throws InvalidTableNameException, ColumnAlreadyExistsException {
		// TODO Auto-generated method stub
		super.createTable(tName, cNames);
	}
	
	public boolean isValidLogin(String username, String password) throws InvalidUserException {
		return accessManager.isValidLogin(username, password);
	}
	
	public String executeInsert(Insert insert, String username) throws InvalidUserException, InvalidTableNameException, InvalidAmountOfInsertValues {
		if(accessManager.hasWriteAccess(username, insert.getTable())) {
			return super.executeInsert(insert);
		}
		return "You have no rights to write in this table.";
	}
	
	public String executeCreate(Create createCreate, String currentUser) throws InvalidTableNameException, ColumnAlreadyExistsException, InvalidUserException {
		if(accessManager.isAdmin(currentUser)) {
			return super.executeCreate(createCreate);
		}
		return "You have no rights to create tables.";
	}

	public String executeSelect(Select select, String currentUser) throws InvalidUserException, InvalidTableNameException {
		if(accessManager.hasReadAccess(currentUser, select.getTable())) {
			return super.executeSelect(select);
		}
		return "You have no rights to read this table.";
	}
}
