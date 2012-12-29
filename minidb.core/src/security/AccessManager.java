package security;

import java.util.HashMap;

import model.Table;

public class AccessManager {
	
	private HashMap<User, Rule[]> rules = new HashMap<User, Rule[]>();
	
	public AccessManager(){};
	
	public boolean grandFullAccessTo(User user, Table table) {
		return false;
	}

}
