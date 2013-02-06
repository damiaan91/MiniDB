package aspects;

import aopchat.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public aspect Statistics {
	
	//Statistics format:
	//0 == identified since/last seen
	//1 == total time online
	//2 == registered since
	//3 == online (0 or 1)
	
	private HashMap<String, long[]> userStatistics = new HashMap<String, long[]>();
	
	private final String[] baseLine = {	"Usage statistics for: ", 
										"------------------------------------------",
										"Identified since: ",
										"Total time online: ",
										"Last seen: ",
										"Registered since: "
									  };

	pointcut startUp() : Logging.startUp();
	
	before(): startUp(){
		System.out.println("This happens");
		userStatistics = readStatistics();
	}
	
	//Log user entering
	pointcut userInfoSet(String i, Object s):
		(call (void setInfo(String, Object))) && args(i, s);
	
	
	/**
	 * update identified since
	 * @param i
	 * @param s
	 */
	after(String i, Object s) returning: userInfoSet(i,s){
		if(userStatistics.containsKey((String) s)){
		if(i.equals("name")){
			userStatistics.get(s)[0] = new Date().getTime();
			userStatistics.get(s)[3] = 1;
			flushStatistics();
		}
		}
	}
	
	/**
	 * update last seen, total time online
	 * @param i
	 * @param s
	 */
	before(String i, Object s): userInfoSet(i,s){
		if( userStatistics.containsKey((String) s)){
		if(i.equals("name")){
			userStatistics.get(s)[0]=new Date().getTime();
			userStatistics.get(s)[1]=currentTimeOnline((String) s);
			userStatistics.get(s)[3]=0;
			flushStatistics();
		}
		}
	}
	
	
	
	pointcut userLeft(ConnectionToClient c):
		(call(void clientDisconnected(ConnectionToClient)) || call(void clientException(ConnectionToClient,..))) && args(c,..);

	after(ConnectionToClient c) returning: 
		userLeft(c){
		String name = (String)c.getInfo("name");
		if(userStatistics.containsKey(name)){
			userStatistics.get(name)[3] = 0;
			userStatistics.get(name)[1] = currentTimeOnline(name);
			userStatistics.get(name)[0] = new Date().getTime();
		}
		flushStatistics();
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
			if(userStatistics.containsKey(c[1])){
				String[] result = formattedStats(c[1],(userStatistics.get(c[1])[3]>0));
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
	 * Registration of a new user
	 * @param c
	 * @param s
	 */
	pointcut successfulReg(ConnectionToClient c, String s): 
		call (void sendToClient(ConnectionToClient, String)) && args(c, s);
	after (ConnectionToClient c, String s) returning: successfulReg(c, s){
		 if (s.equals("[SERVER]: Username registered and set.")){
			 long currentTime = new Date().getTime();
			 long[] result = {currentTime, 0, currentTime, 1};
			 String name = (String)c.getInfo("name");
			 userStatistics.put(name, result);
			 flushStatistics();
		 }
	}
	
	//Log user last seen
	//Log user total time online (entering+last seen added)
	//Log user banned
	//Log online since
	

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
			result[2] = baseLine[2] + sdf.format(new Date(userStatistics.get(name)[0]));
			long currentTimeOnline = currentTimeOnline(name);
			result[3] = baseLine[3] + (currentTimeOnline/3600000)+":"+((currentTimeOnline%3600000)/60000)+":"+((currentTimeOnline%60000)/1000);
		}
		else{

			result[2] = baseLine[4] + sdf.format(new Date(userStatistics.get(name)[0]));
			long currentTimeOnline = userStatistics.get(name)[1];
			result[3] = baseLine[3] + (currentTimeOnline/3600000)+":"+((currentTimeOnline%3600000)/60000)+":"+((currentTimeOnline%60000)/1000);
		}
		result[4] = baseLine[5] + sdf.format(userStatistics.get(name)[2]);
		
		return result;
	}
	
	
	private void flushStatistics(){
			try{
				BufferedWriter out = new BufferedWriter(new FileWriter("statistics.txt"));
				for(Map.Entry<String, long[]> entry: userStatistics.entrySet()){
					out.write(entry.getKey() + 	" " + entry.getValue()[0] +
												" " + entry.getValue()[1] +
												" " + entry.getValue()[2] +
												" " + entry.getValue()[3]);
				}
				out.close();
			} catch (IOException e) {
				System.out.println("error: could not read file statistics.txt");
			}
	}
		
	private HashMap<String, long[]> readStatistics(){
		HashMap<String, long[]> result = new HashMap<String, long[]>();
		try{
			BufferedReader in = new BufferedReader(new FileReader("statistics.txt"));
			String strln;
			while ((strln = in.readLine()) != null) {
				String[] strArr = strln.split(" ");
				long[] res = new long[]{Long.parseLong(strArr[1]),
										Long.parseLong(strArr[2]),
										Long.parseLong(strArr[3]),
										Long.parseLong(strArr[4])};
				result.put(strArr[0], res);
			}
			in.close();
		}catch(Exception e){
			System.out.println("Error reading statistics.txt");
		}
		return result;
	}
	
	private long currentTimeOnline(String s){
		long old = userStatistics.get(s)[0];
		long total = userStatistics.get(s)[1];
		long newTimeStamp = new Date().getTime();
		total += (newTimeStamp-old);
		return total;
	}

}
