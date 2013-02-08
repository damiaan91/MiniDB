package minidb.core.model.action;

import minidb.core.model.data.ISession;

/**
 * @author  Damiaan
 */
public class CreateUser implements IAction {
	
	/**
	 * @uml.property  name="username"
	 */
	private final String username;
	/**
	 * @uml.property  name="password"
	 */
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

	/**
	 * @return
	 * @uml.property  name="username"
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return
	 * @uml.property  name="password"
	 */
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
