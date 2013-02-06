package aopchat;

import java.io.*;
import java.util.*;

public class Writer {

	/**
	 * add a user to users.txt
	 * 
	 * @param user
	 * @param pw
	 */
	public void addUser(String user, String pw) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("users.txt",
					true));
			out.write(user + " " + pw + System.getProperty("line.separator"));
			out.close();
		} catch (IOException e) {
			System.out.println("error: could not open users.txt");
		}
	}

	public void addBan(String ip) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("bans.txt",
					true));
			out.write(ip + System.getProperty("line.separator"));
			out.close();
		} catch (IOException e) {
			System.out.println("error: could not open bans.txt");
		}
	}

	public HashMap<String, String> readUsers() {
		HashMap<String, String> result = new HashMap<String, String>();

		try {
			BufferedReader in = new BufferedReader(new FileReader("users.txt"));
			String strln;
			while ((strln = in.readLine()) != null) {
				String[] strArr = strln.split(" ");
				result.put(strArr[0], strArr[1]);
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("error: could not find file users.txt");
		} catch (IOException e) {
			System.out.println("error: could not read file users.txt");
		}
		return result;
	}

	public ArrayList<String> readBans() {
		ArrayList<String> result = new ArrayList<String>();

		try {
			BufferedReader in = new BufferedReader(new FileReader("bans.txt"));
			String strln;
			while ((strln = in.readLine()) != null) {
				result.add(strln);
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("error: could not find file bans.txt");
		} catch (IOException e) {
			System.out.println("error: could not read from file bans.txt");
		}
		return result;
	}
}
