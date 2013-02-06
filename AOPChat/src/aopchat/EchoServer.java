package aopchat;

import java.io.IOException;
import java.util.*;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com

/**
 * This class overrides some of the methods in the abstract superclass in order
 * to give more functionality to the server.
 * 
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer {
	// Class variables *************************************************

	/**
	 * The default port to listen on.
	 */
	final public static int DEFAULT_PORT = 5555;
	private HashMap<String, String> registeredUsers = new HashMap<String, String>();
	private ArrayList<String> bans = new ArrayList<String>();
	private Writer writer;
	public boolean enable_echo = false;

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the echo server.
	 * 
	 * @param port
	 *            The port number to connect on.
	 */
	public EchoServer(int port) {
		super(port);
		writer = new Writer();
		registeredUsers = writer.readUsers();
		bans = writer.readBans();
	}

	// Instance methods ************************************************

	/**
	 * This method handles any messages received from the client.
	 * 
	 * @param msg
	 *            The message received from the client.
	 * @param client
	 *            The connection from which the message originated.
	 */
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		String m = msg.toString();
		if (m.startsWith("#")) {
			handleCommand(m, client);
		} else {
			System.out.println("Message received: " + m + " from " + client);
			if (client.getInfo("name") != null)
				m = client.getInfo("name") + ": " + m;
			else
				m = "Anonymous: " + m;
			this.sendToAllClients(m);
		}
	}

	/**
	 * My very own personal chat command method. Whee.
	 * 
	 * @param m
	 * @param client
	 */
	public void handleCommand(String m, ConnectionToClient client) {
		String command[] = m.split(" ");
		switch (command[0]) {
		case "#rename":
			rename(command, client);
			break;
		case "#register":
			register(command, client);
			break;
		case "#login":
			login(command, client);
			break;
		case "#SERVER_CONNECT":
			handleConnect(command, client);
			break;
		case "#ban":
			ban(command, client);
			break;
		case "#shutdown_server":
			shutdown(command, client);
			break;
		case "#kick":
			kick(command, client);
			break;
		default:
			sendToClient(client, "[SERVER]: not a valid command.");
		}
	}

	/**
	 * Shuts down the server
	 * 
	 * @param command
	 * @param client
	 */
	public void shutdown(String[] command, ConnectionToClient client) {
		this.sendToAllClients("[SERVER]: " + client.getInfo("name")
				+ " has initiated a server shutdown. Goodbye, y'all!");
		try {
			this.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Kicks a user from the server.
	 */
	public void kick(String[] command, ConnectionToClient client) {
		if (command.length == 2) {
			Thread[] c = getClientConnections();
			String name = "";
			boolean kicked = false;
			for (int i = 0; i < c.length; i++) {
				name = (String) ((ConnectionToClient) c[i]).getInfo("name");
				if (name.equals(command[1])) {
					kicked = true;
					sendToClient(
							((ConnectionToClient) c[i]),
							"[SERVER]: You were kicked by "
									+ client.getInfo("name")
									+ ". Disconnecting you. Goodbye!");
					try {
						((ConnectionToClient) c[i]).close();
					} catch (IOException e) {
					}
					this.sendToAllClients("User " + name + " was kicked by "
							+ client.getInfo("name"));
				}
			}
			if (!kicked) {
				sendToClient(client, "[SERVER]: User not found");
			}
		} else {
			sendToClient(client,
					"[SERVER]: Invalid syntax: please use #kick <username>");
		}
	}

	/**
	 * IP bans a user.
	 * 
	 * @param command
	 * @param client
	 */
	public void ban(String[] command, ConnectionToClient client) {
		if (command.length == 2) {
			Thread[] c = getClientConnections();
			String name = "";
			boolean banned = false;
			for (int i = 0; i < c.length; i++) {
				name = (String) ((ConnectionToClient) c[i]).getInfo("name");
				if (name.equals(command[1])) {
					writer.addBan(((ConnectionToClient) c[i]).getInetAddress()
							.toString());
					banned = true;
					sendToClient(
							((ConnectionToClient) c[i]),
							"[SERVER]: You were banned by "
									+ client.getInfo("name")
									+ ". Disconnecting you. Goodbye!");
					try {
						((ConnectionToClient) c[i]).close();
					} catch (IOException e) {
					}
					this.sendToAllClients("User " + name + "was banned by "
							+ client.getInfo("name"));
				}
			}
			if (!banned) {
				sendToClient(client, "[SERVER]: User not found");
			}
		} else
			sendToClient(client,
					"[SERVER]: Invalid syntax: please use #ban <username>");
	}

	/**
	 * renames a user
	 */
	public void rename(String[] command, ConnectionToClient client) {
		if (command.length == 2) {
			if (!registeredUsers.containsKey(command[1])) {
				String oldname = (String) client.getInfo("name");
				client.setInfo("name", command[1]);
				this.sendToAllClients(oldname + " is now known as "
						+ command[1]);
			} else
				sendToClient(
						client,
						"[SERVER]: This is a registered user. Please use the #login <username> <password> command instead.");
		} else
			sendToClient(client,
					"[SERVER]: Invalid syntax. Command should be of format #rename <name>");
	}

	/**
	 * logs in the user
	 * 
	 * @param command
	 * @param client
	 */
	public void login(String[] command, ConnectionToClient client) {
		if (command.length == 3) {
			if (registeredUsers.containsKey(command[1])) {
				if (authenticate(command[1], command[2])) {
					String oldname = (String) client.getInfo("name");
					client.setInfo("name", command[1]);
					this.sendToAllClients(oldname + " is now known as "
							+ command[1]);
				} else {
					sendToClient(client, "[SERVER]: Error: Invalid password.");
				}
			} else {
				sendToClient(client, "[SERVER]: Error: unknown user.");
			}
		} else {
			sendToClient(
					client,
					"[SERVER]: Invalid syntax. Command should be of format #login <name> <password>");
		}
	}

	private boolean authenticate(String usr, String pw) {
		return registeredUsers.get(usr).equals(pw);
	}

	/**
	 * registers a user in the awesome HashMap
	 * 
	 * @param command
	 */
	public void register(String[] command, ConnectionToClient client) {
		if (command.length == 3) {
			if (!(registeredUsers.containsKey(command[1]))) {
				registeredUsers.put(command[1], command[2]);
				writer.addUser(command[1], command[2]);
				String oldname = (String) client.getInfo("name");
				client.setInfo("name", command[1]);
				sendToClient(client, "[SERVER]: Username registered and set.");

				this.sendToAllClients(oldname + " is now known as "
						+ command[1]);
			} else {
				sendToClient(client,
						"[SERVER]: This username is already taken. Please supply a different username");
			}
		} else {
			sendToClient(
					client,
					"[SERVER]: Invalid syntax. Command should be of format #register <name> <password>");

		}
	}

	/**
	 * handles the connection of a new client to the server
	 */
	public void handleConnect(String[] command, ConnectionToClient client) {
		String[] new_command = command;
		if (bans.contains(client.getInetAddress().toString())) {
			sendToClient(
					client,
					"[SERVER]: Unfortunately, you were banned from this server. As such, we cannot admit you. Goodbye!");
			try {
				client.close();
			} catch (IOException e) {
			}
		}

		if (command.length == 3) {
			if (registeredUsers.containsKey(command[1])) {
				new_command[0] = "#login";
				login(new_command, client);
			} else {
				new_command[0] = "#register";
				register(new_command, client);
			}
		} else if (command.length == 2) {
			new_command[0] = "#rename";
			rename(new_command, client);
		}
		sendToClient(
				client,
				"[SERVER]: Welcome, "
						+ (String) client.getInfo("name")
						+ "! Please type a message or any of the commands available. "
						+ "available commands are admin, deadmin, op, deop, register, login, rename, stats, ban, kick and shutdown_server.");
		this.sendToAllClients((String) client.getInfo("name")
				+ " has entered the server.");
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	protected void serverStarted() {
		System.out.println("Server listening for connections on port "
				+ getPort());
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * stops listening for connections.
	 */
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
	}

	// Class methods ***************************************************

	/**
	 * This method is responsible for the creation of the server instance (there
	 * is no UI in this phase).
	 * 
	 * @param args
	 *            [0] The port number to listen on. Defaults to 5555 if no
	 *            argument is entered.
	 */
	public static void main(String[] args) {
		int port = 0; // Port to listen on

		try {
			port = Integer.parseInt(args[0]); // Get port from command line
		} catch (Throwable t) {
			port = DEFAULT_PORT; // Set port to 5555
		}

		EchoServer sv = new EchoServer(port);

		try {
			sv.listen(); // Start listening for connections
		} catch (Exception ex) {
			System.out.println("ERROR - Could not listen for clients!");
		}
	}

	public void sendToClient(ConnectionToClient client, String msg) {
		try {
			client.sendToClient(msg);
		} catch (Exception e) {
		}
		if (enable_echo) {
			System.out.println("Sent:" + msg);
		}
	}

	/**
	 * Overridden version of the AbstractServer method -- overridden because of
	 * the enable_echo variable which enables echoing all messages on the
	 * console.
	 */
	public void sendToAllClients(Object msg) {
		Thread[] clientThreadList = getClientConnections();

		for (int i = 0; i < clientThreadList.length; i++) {
			try {
				((ConnectionToClient) clientThreadList[i]).sendToClient(msg);
			} catch (Exception ex) {
			}
		}
		if (enable_echo) {
			System.out.println("Broadcast: " + msg);
		}
	}

	public void clientDisconnected(ConnectionToClient client) {
		sendToAllClients(client.getInfo("name") + " has left the server.");
	}

	public void clientException(ConnectionToClient client, Throwable exception) {
		sendToAllClients(client.getInfo("name") + " has left the server.");
	}
	
	public HashMap<String, String> getUsers(){
		return registeredUsers;
	}
}
// End of EchoServer class
