package aspects;

import aopchat.ConnectionToClient;
import aspects.core.FileLogger;

/**
 * A specific file logger for the AOPChat.
 */
public aspect Logging extends FileLogger {
	
	/*
	 * (non-Javadoc)
	 * @see aspects.core.AbstractLogger#logMethods()
	 */
	public pointcut logMethods() : call(String aspects.Logging.log*(..));
	
	/**
	 * Property to enable chat logging (Default: enabled).
	 */
	public boolean chat_logging 		= true;
	
	/**
	 * Property to enable enter massages logging (Default: enabled.
	 */
	public boolean entermessage_logging	= true;
	
	/**
	 * Property to enable command logging (Default: enabled).
	 */
	public boolean command_logging		= true;
	
	/**
	 * Initialize the logging
	 */
	public pointcut startUp(): call(void listen());
	
	/**
	 * Finalize the logging
	 */
	pointcut shutDown(): call(void stopListening());
	
	/**
	 * Advice for log start up.
	 */
	after() returning : startUp() {
		logStartUp();
	}
	
	/**
	 * Advice for log shutdown.
	 */
	after() returning: shutDown(){
		logShutDown();
	}
	
	/**
	 * Creates shutdown log message.
	 * @return Shutdown log message.
	 */
	private String logShutDown() {
		return "Server shut down";
	}
	
	/**
	 * Creates a start up log message.
	 * @return Start up log message.
	 */
	private String logStartUp() {
		return "Start monitoring";
	}
	
	/**
	 * Incoming base message.
	 */
	pointcut handleMessage(Object s, ConnectionToClient c):
		call(void handleMessageFromClient(Object, ConnectionToClient)) && args(s,c);
	
	/**
	 * Outgoing message from server
	 */
	pointcut outgoingMessage(Object m):
		call(void sendToAllClients(Object)) && args(m);
	
	/**
	 * Defines a command cflow.
	 */
	pointcut command_flow():
		cflow(command(String, ConnectionToClient));
	
	/**
	 * Defines the start of handling a command.
	 */
	pointcut command(String s, ConnectionToClient c):
		call(void handleCommand(String, ConnectionToClient)) && args(s,c);
	
	/**
	 * Advice to log a command message.
	 * @param s Command which is issued.
	 * @param c Clients connection.
	 */
	after(String s, ConnectionToClient c) returning: command(s, c){
		if (command_logging) logCommand(s, c);
	}
	
	/**
	 * Generates a command log message.
	 * @param s Command which is issued.
	 * @param c Clients connection.
	 * @return Command log message.
	 */
	public String logCommand(String s, ConnectionToClient c) {
		return c.getInfo("name")+" issued command: " + s;
	}
	
	/**
	 * Defines a chat message (not a command or announcement).
	 */
	pointcut chatMessage(Object m):
		outgoingMessage(m) && !command_flow()
		&& !(clientLeaving());
	
	/**
	 * Defines an announcement of a client leaving the server.
	 */
	pointcut clientLeaving():
		cflow(call(void clientException(ConnectionToClient,..))) || 
		cflow(call(void clientDisconnected(ConnectionToClient)));
	
	/**
	 * Advice to log sent messages.
	 * @param m The message.
	 */
	after(Object m) returning: chatMessage(m){
		if (chat_logging) logMessage(m);
	}
	
	/**
	 * Generates a simple log message.
	 * @param m The message.
	 * @return Simple log message.
	 */
	public String logMessage(Object m) {
		return m.toString();
	}
	
	/**
	 * Pointcut for outgoing messages.
	 */
	pointcut leaveMessage(Object m):
		outgoingMessage(m) && !command_flow()
		&& (clientLeaving());
	
	/**
	 * Advice for logging outgoing messages.
	 * @param m The outgoing message.
	 */
	after(Object m) returning: leaveMessage(m){
		if (entermessage_logging) logMessage(m);
	}
}