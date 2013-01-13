package minidb.core.model.data;

import minidb.core.model.action.*;

public interface ISession {
	String select(Select select);
	String insert(Insert insert);
	String alter(Alter alter);
	String create(CreateTable create);
	String execute(IAction action);
	String getDatabaseName();
	String getSessionUser();
	boolean isActive();
	void disconnect();
	String createUser(CreateUser createUser);
	String grantPrivilege(GrantPrivilege grantPrivilege);
	Database getDatabase();
}
