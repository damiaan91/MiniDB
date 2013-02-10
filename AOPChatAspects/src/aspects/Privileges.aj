package aspects;

import aopchat.ConnectionToClient;
import aopchat.EchoServer;
import java.io.*; 
import java.util.HashMap;
import java.util.Map;

/**
 * Privileges aspect for AOPChat.
 */
public aspect Privileges {
	
	/**
	 * List of privileged users.
	 */
	private HashMap<String, String> privilegedUsers = new HashMap<String, String>();
	
	/**
	 * Privileges constructor.
	 */
	public Privileges() {
		readPrivileges();
	}
	
	/**
	 * defines 4 new commands related to modifying permissions
	 * @param c
	 * @param i
	 */
	void around(String c, ConnectionToClient i): Logging.command(c,i){
		String command[] = c.split(" ");
		if(command.length >= 2){
			switch(command[0]){
				case "#op": opUser(command, i);
				break;
				case "#deop": deOpUser(command, i);
				break;
				case "#admin": adminUser(command, i);
				break;
				case "#deAdmin": deAdminUser(command, i);
				break;
				default: proceed(c,i);
			}
		}else proceed(c,i);
	}
	
	/**
	 * grants op status to a user
	 */
	public void opUser(String[] command, ConnectionToClient issuer){
		if(!((EchoServer)issuer.getServer()).getUsers().containsKey(command[1])){
			try{issuer.sendToClient("User not found.");}
			catch(Exception e){}
			return;
		}
		privilegedUsers.put(command[1], "op");
		issuer.getServer().sendToAllClients(command[1]+ " was promoted to op");
		flushPermissions();
	}
	
	/**
	 * demotes op to user
	 */
	public void deOpUser(String[] command, ConnectionToClient issuer){
		if(!((EchoServer)issuer.getServer()).getUsers().containsKey(command[1])){
			try{issuer.sendToClient("User not found.");}
			catch(Exception e){}
			return;
		}
		privilegedUsers.remove(command[1]);
		issuer.getServer().sendToAllClients(command[1]+ " was demoted to user");
		flushPermissions();
	}
	
	/**
	 * grants admin status to a user
	 */
	public void adminUser(String[] command, ConnectionToClient issuer){
		if(!((EchoServer)issuer.getServer()).getUsers().containsKey(command[1])){
			try{issuer.sendToClient("User not found.");}
			catch(Exception e){}
			return;
		}
		privilegedUsers.put(command[1], "admin");
		issuer.getServer().sendToAllClients(command[1]+ " was promoted to admin");
		flushPermissions();
	}
	
	
	/**
	 * demotes an admin to op
	 */
	public void deAdminUser(String[] command, ConnectionToClient issuer){
		if(!((EchoServer)issuer.getServer()).getUsers().containsKey(command[1])){
			try{issuer.sendToClient("User not found.");}
			catch(Exception e){}
			return;
		}
		privilegedUsers.put(command[1], "op");
		issuer.getServer().sendToAllClients(command[1]+ " was demoted to op");
		flushPermissions();
	}
	
	/**
	 * Flush and save permissions to file.
	 */
	private void flushPermissions(){
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter("privileges.txt"));
			for(Map.Entry<String, String> entry: privilegedUsers.entrySet()){
				out.write(entry.getKey() + " " + entry.getValue());
			}
			out.close();
		} catch (IOException e) {
			System.out.println("error: could not read file privileges.txt");
		}
		
		
	}
	
	/**
	 * Read privileges form file.
	 */
	private void readPrivileges(){
		try {
			BufferedReader in = new BufferedReader(new FileReader("privileges.txt"));
			String strln;
			while ((strln = in.readLine()) != null) {
				String[] strArr = strln.split(" ");
				privilegedUsers.put(strArr[0], strArr[1]);
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("error: could not find file privileges.txt");
		} catch (IOException e) {
			System.out.println("error: could not read file privileges.txt");
		}
	}
	
	
	//--------------protected command list. --------------------//
	
	/**
	 * The if the user is an admin.
	 */
	private boolean checkPerms(int l, ConnectionToClient i){
		boolean result = false;
		int level = 0;
		if (((EchoServer)i.getServer()).getUsers().containsKey(i.getInfo("name"))) level++;
		String plevel = privilegedUsers.get(i.getInfo("name"));
		if (plevel != null){ 
			level++;
			if (plevel.equals("admin")) level++;
		}
		if (level >= l) result = true;
		return result;
	}
	
	/**
	 * Pointcut of OP commands.
	 */
	pointcut opCommands(String[] c, ConnectionToClient i): 
		(call(void kick(..)) && args(c,i)) || 
		(call (void ban(..)) && args(c,i)) ||
		(call (void deOpUser(..)) && args(c,i)) ||
		(call (void opUser(..)) && args(c,i)); 

	/**
	 * Pointcut of admin commands.
	 */
	pointcut adminCommands(String[] c, ConnectionToClient i):
		(call(void adminUser(..)) && args(c,i)) ||
		(call(void deAdminuser(..)) && args(c,i)) || 
		call(void shutdown(..)) && args(c,i);
	
	/**
	 * Advice for handling OP commands.
	 */
	void around(String[] c, ConnectionToClient i): opCommands(c,i){
		System.out.println("op command given");
		if(checkPerms(2,i)){proceed(c, i);}
		else{try{i.sendToClient("[SERVER]: You have insufficient rights to perform this operation.");}
			catch(Exception e){System.out.println("error! could not send denial to issuer!");}}
	}
	
	/**
	 * Advice for handling admin commands.
	 */
	void around(String[] c, ConnectionToClient i): adminCommands(c,i){
		System.out.println("Admin Command Given");
		if(checkPerms(3,i)){proceed(c, i);}
		else{try{i.sendToClient("[SERVER]: You have insufficient rights to perform this operation.");}
			catch(Exception e){System.out.println("error! could not send denial to issuer!");}}
	}
}
