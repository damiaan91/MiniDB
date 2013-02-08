package aspects;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import aopchat.ConnectionToClient;
import aspects.core.AbstractStatistics;

public aspect Statistics2 extends AbstractStatistics {
	public static final String USER_TIME = "_time";
	public static final String USER_ONLINE_TIME = "_time_online";
	public static final String USER_INFO = "_info";
	
	private final String[] baseLine = {	"Usage statistics for: ", 
			"------------------------------------------",
			"Identified since: ",
			"Total time online: ",
			"Last seen: ",
			"Registered since: "
		  };
	
	public Statistics2() {
	}

	protected pointcut startUp() : 
		Logging.startUp();
	
	//Log user entering
	pointcut userInfoSet(String i, Object s) : 
		(call (void setInfo(String, Object))) && args(i, s);
		
		
	/**
	 * update identified since
	 * @param i
	 * @param s
	 */
	after(String i, Object s) returning: userInfoSet(i,s){
		if(i.equals("name")){
			changeStat(s+USER_TIME, new Date().getTime());
			changeStat(s+USER_ONLINE_TIME, currentTimeOnline((String) s));
			changeStat(s+USER_INFO, 1);
		}
	}
	
	/**
	 * update last seen, total time online
	 * @param i
	 * @param s
	 */
	before(String i, Object s): userInfoSet(i,s){
		if(i.equals("name")){
			changeStat(s+USER_TIME, new Date().getTime());
			changeStat(s+USER_ONLINE_TIME, currentTimeOnline((String) s));
			changeStat(s+USER_INFO, 0);
		}
	}
	
	pointcut userLeft(ConnectionToClient c):
		(call(void clientDisconnected(ConnectionToClient)) || call(void clientException(ConnectionToClient,..))) && args(c,..);

	after(ConnectionToClient c) returning: 
		userLeft(c){
		String name = (String)c.getInfo("name");
		if(containsKey(name+USER_ONLINE_TIME)){
			changeStat(name+USER_TIME, new Date().getTime());
			changeStat(name+USER_ONLINE_TIME, currentTimeOnline(name));
			changeStat(name+USER_INFO, 0);
		}
	}
	
	//THIS STUFF IS DOUBLE, IS ALSO DEFINED IN PRIVILEGES.
		pointcut command(String c, ConnectionToClient i):
			call(void handleCommand(String, ConnectionToClient)) && args(c, i);
		/**
		 * defines 1 new command related to statistics
		 * @param c
		 * @param i
		 */
		void around(String c, ConnectionToClient i): command(c,i){
			String command[] = c.split(" ");
			if(command.length >= 2){
				switch(command[0]){
					case "#stats": showStats(command, i);
					break;
					default: proceed(c,i);
				}
			}else proceed(c,i);
		}
		
		private void showStats(String[] c, ConnectionToClient i){
			if(c.length == 2){
				if(containsKey(c[1]+USER_ONLINE_TIME)){
					String[] result = formattedStats(c[1],(getStatAsLong(c[1]+USER_INFO)>0));
					for(String msg: result){
						try{
							i.sendToClient("[SERVER]: "+ msg);
						}catch(IOException e){System.out.println("error sending message to user");}
					}
					}else{
					try{
						i.sendToClient("User not found.");
					}catch(IOException e){System.out.println("error sending message to user");}
				}
			}else{
				try{
					i.sendToClient("Invalid syntax. Please use #stats <user>");
				}catch(IOException e){System.out.println("error sending message to user");}
			}
		}
		/**
		 * return the usage statistics of a user in a formatted fashion for display. Fuckin' format.
		 */
		private String[] formattedStats(String name, boolean online){
			String result[] = new String[5];
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
			sdf.setTimeZone(TimeZone.getDefault());
			result[0] = baseLine[0]+name;
			result[1] = baseLine[1];
			if(online){
				result[2] = baseLine[2] + sdf.format(new Date(getStatAsLong(name+USER_TIME)));
				long currentTimeOnline = currentTimeOnline(name);
				result[3] = baseLine[3] + (currentTimeOnline/3600000)+":"+((currentTimeOnline%3600000)/60000)+":"+((currentTimeOnline%60000)/1000);
			}
			else{

				result[2] = baseLine[4] + sdf.format(new Date(getStatAsLong(name+USER_TIME)));
				long currentTimeOnline = getStatAsLong(name+USER_ONLINE_TIME);
				result[3] = baseLine[3] + (currentTimeOnline/3600000)+":"+((currentTimeOnline%3600000)/60000)+":"+((currentTimeOnline%60000)/1000);
			}
			//result[4] = baseLine[5] + sdf.format(userStatistics.get(name)[2]);
			
			return result;
		}
		
		/**
		 * Registration of a new user
		 * @param c
		 * @param s
		 */
		pointcut successfulReg(ConnectionToClient c, String s): 
			call (void sendToClient(ConnectionToClient, String)) && 
			args(c, s);
		
		after (ConnectionToClient c, String s) returning: successfulReg(c, s){
			 if (s.equals("[SERVER]: Username registered and set.")){
				 long currentTime = new Date().getTime();
				 long[] result = {currentTime, 0, currentTime, 1};
				 String name = (String)c.getInfo("name");
				 //TODO:
			 }
		}
	
	private long currentTimeOnline(String s){
		long old = getStatAsLong(s+USER_TIME);
		Long total = getStatAsLong(s+USER_ONLINE_TIME);
		long newTimeStamp = new Date().getTime();
		if(total == null) total = new Long(0);
		total += (newTimeStamp-old);
		return total;
	}
}
