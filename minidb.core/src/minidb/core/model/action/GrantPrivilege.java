package minidb.core.model.action;

import minidb.core.model.data.ISession;

public class GrantPrivilege implements IAction {
	
	private final boolean readAccess;
	private final boolean writeAccess;
	private final String table;
	private final String user;
	
	public boolean isReadAccess() {
		return readAccess;
	}

	public boolean isWriteAccess() {
		return writeAccess;
	}

	public String getTable() {
		return table;
	}

	public String getUser() {
		return user;
	}

	public GrantPrivilege(String table, String user) {
		this(true, false, table, user);
	}
	
	public GrantPrivilege(boolean readAccess, boolean writeAccess, String table, String user) {
		this.readAccess = readAccess;
		this.writeAccess = writeAccess;
		this.table = table;
		this.user = user;
	}

	@Override
	public String ExecuteUsing(ISession session) {
		return session.grantPrivilege(this);
	}

	@Override
	public String toString() {
		return "GRANT " + (readAccess ? "READ " : "") + 
				(writeAccess ? "WRITE " : "") + "ON " + table + " TO " + user;
	}
	
	

}
