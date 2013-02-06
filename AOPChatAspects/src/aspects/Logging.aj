package aspects;

import aopchat.ConnectionToClient;
import aspects.core.FileLogger;

public aspect Logging extends FileLogger {
	
	public pointcut logMethods() : call(String aspects.Logging.log*(..));
	
	public boolean chat_logging 		= true;
	public boolean entermessage_logging	= true;
	public boolean command_logging		= true;
	public boolean password_redaction	= true;
	
	/**
	 * Initialize the logging
	 */
	public pointcut startUp(): call(void listen());
	
	/**
	 * Finalize the logging
	 */
	pointcut shutDown(): call(void stopListening());
	
	after() returning : startUp() {
		logStartUp();
	}
	after() returning: shutDown(){
		logShutDown();
	}
	
	private String logShutDown() {
		return "Server shut down";
	}
	private String logStartUp() {
		return "Start monitoring";
	}

	//Incoming base message
	pointcut handleMessage(Object s, ConnectionToClient c):
		call(void handleMessageFromClient(Object, ConnectionToClient)) && args(s,c);
	
	//outgoing message from server
	pointcut outgoingMessage(Object m):
		call(void sendToAllClients(Object)) && args(m);
	
	//Defines a command cflow
	pointcut command_flow():
		cflow(command(String, ConnectionToClient));
	
	//Defines the start of handling a command
	pointcut command(String s, ConnectionToClient c):
		call(void handleCommand(String, ConnectionToClient)) && args(s,c);
	
	after(String s, ConnectionToClient c) returning: command(s, c){
		if (command_logging) logCommand(s, c);
	}
	
	public String logCommand(String s, ConnectionToClient c) {
		return c.getInfo("name")+" issued command: " + s;
	}
	
	//Defines a chat message (not a command or announcement)
	pointcut chatMessage(Object m):
		outgoingMessage(m) && !command_flow()
		&& !(clientLeaving());
	
	//Defines an announcement of a client leaving the server
	pointcut clientLeaving():
		cflow(call(void clientException(ConnectionToClient,..))) || 
		cflow(call(void clientDisconnected(ConnectionToClient)));
	
	//Message is sent
	after(Object m) returning: chatMessage(m){
		if (chat_logging) logMessage(m);
	}
	
	public String logMessage(Object m) {
		return m.toString();
	}
	
	pointcut leaveMessage(Object m):
		outgoingMessage(m) && !command_flow()
		&& (clientLeaving());
	after(Object m) returning: leaveMessage(m){
		if (entermessage_logging) logMessage(m);
	}
	
	pointcut enterMessage(String[] s, ConnectionToClient c):
		call(void handleConnect(String[] , ConnectionToClient)) && args(s,c);
	
	after(String[] s, ConnectionToClient c) returning: enterMessage(s,c){
		//TODO: IMPLEMENT THIS
	}
}