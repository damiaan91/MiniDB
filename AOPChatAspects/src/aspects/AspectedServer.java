package aspects;

import aopchat.EchoServer;

public class AspectedServer {
	public static void main(String[] args) {
		int port = 5555; // Port to listen on
		EchoServer sv = new EchoServer(port);
		try {
			sv.listen(); // Start listening for connections
		} catch (Exception ex) {
			System.out.println("ERROR - Could not listen for clients!");
		}
	}
}