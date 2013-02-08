package minidb.core.model.action;

import minidb.core.model.data.ISession;

/**
 * @author  Damiaan
 */
public class GrantPrivilege implements IAction {
	
	/**
	 * @uml.property  name="readAccess"
	 */
	private final boolean readAccess;
	/**
	 * @uml.property  name="writeAccess"
	 */
	private final boolean writeAccess;
	/**
	 * @uml.property  name="table"
	 */
	private final String table;
	/**
	 * @uml.property  name="user"
	 */
	private final String user;
	
	/**
	 * @return
	 * @uml.property  name="readAccess"
	 */
	public boolean isReadAccess() {
		return readAccess;
	}

	/**
	 * @return
	 * @uml.property  name="writeAccess"
	 */
	public boolean isWriteAccess() {
		return writeAccess;
	}

	/**
	 * @return
	 * @uml.property  name="table"
	 */
	public String getTable() {
		return table;
	}

	/**
	 * @return
	 * @uml.property  name="user"
	 */
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
