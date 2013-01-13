package minidb.core.model.action;

import minidb.core.model.data.ISession;

public class CreateUser implements IAction {
	
	private final String username;
	private final String password;
	private final boolean asAdmin;
	
	public CreateUser(String username, String password){
		this(username, password, false);
	}

	public CreateUser(String username, String password, boolean asAdmin) {
		this.username = username;
		this.password = password;
		this.asAdmin = asAdmin;
	}
	
	@Override
	public String ExecuteUsing(ISession session) {
		return session.createUser(this);
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean isAdmin() {
		return asAdmin;
	}

	@Override
	public String toString() {
		return "CREATE USER " + username + " WITH PASSWORD " + password + (asAdmin ? " AS ADMIN" : "");
	}

}
