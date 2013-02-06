package aopchat;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class ChatGUI implements ChatIF {

	// ChatClient
	private ChatClient chat;

	// Chat functionality
	private JFrame f = new JFrame("Chatprogram AOP");
	private JTextArea chatWindow = new JTextArea("", 40, 50);
	private JScrollPane chatWrapper = new JScrollPane(chatWindow);
	private JTextField chatLine = new JTextField(50);

	// Connectivity functionality
	private JPanel connPanel = new JPanel();
	private JLabel portLabel = new JLabel("port: ");
	private JLabel ipLabel = new JLabel("Server IP: ");
	private JLabel nickLabel = new JLabel("Nickname: ");
	private JLabel passLabel = new JLabel("Password: ");
	private JLabel connStatus = new JLabel("Disconnected");

	private JTextField portField = new JTextField("5555", 5);
	private JTextField ipField = new JTextField("127.0.0.1", 16);
	private JTextField nickField = new JTextField(32);
	private JTextField passField = new JTextField(32);

	private JButton connect = new JButton("Connect");
	private JButton quit = new JButton("Quit");

	/**
	 * Constructor for the Simple GUI for the chatprogram thing
	 */
	public ChatGUI() {
		// outer frame, chat funcs
		f.setLayout(new BorderLayout());
		chatWindow.setLineWrap(true);
		chatWrapper
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		chatWrapper.setPreferredSize(new Dimension(800, 500));
		((DefaultCaret) chatWindow.getCaret())
				.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		f.add(connStatus, BorderLayout.NORTH);
		f.add(chatWrapper, BorderLayout.CENTER);
		f.add(chatLine, BorderLayout.SOUTH);

		// frame to the right, connection
		GridLayout connGrid = new GridLayout(5, 2);
		connGrid.setVgap(20);
		connGrid.setHgap(20);
		connGrid.preferredLayoutSize(connPanel);
		connPanel.setLayout(connGrid);
		connPanel.add(portLabel);
		connPanel.add(portField);
		connPanel.add(ipLabel);
		connPanel.add(ipField);
		connPanel.add(nickLabel);
		connPanel.add(nickField);
		connPanel.add(passLabel);
		connPanel.add(passField);
		connPanel.add(connect);
		connPanel.add(quit);

		connect.addActionListener(new Connect(this));
		nickField.addActionListener(new Connect(this));
		passField.addActionListener(new Connect(this));
		chatLine.addActionListener(new ChatAction(this));
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					chat.quit();
				} catch (Exception ex) {
				}
				System.exit(0);
			}
		});

		f.add(connPanel, BorderLayout.EAST);

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}

	public void notifyDisconnect() {
		connStatus.setText("Disconnected");
		chat = null;
	}

	public void display(String msg) {
		chatWindow.append(msg + "\n");
	}

	/**
	 * Main method for the starting of the chatprogram thing -- will be
	 * invalidated once a proper launcher is constructed.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		ChatGUI c = new ChatGUI();
	}

	// Connecting, pewpew
	private class Connect implements ActionListener {
		ChatGUI gui;

		/**
		 * Constructor
		 * 
		 * @param c
		 */
		public Connect(ChatGUI c) {
			gui = c;
		}

		public void actionPerformed(ActionEvent e) {
			String host = ipField.getText();
			int port = 0;
			if (chat == null) {
				try {
					port = Integer.parseInt(portField.getText());
					if (!(port > 0 && port <= 65535))
						throw new NumberFormatException();
				} catch (NumberFormatException error) {
					gui.display("Error: please provide a valid port number");
				}
				try {
					chat = new ChatClient(host, port, gui);
					display("connected to the server on " + ipField.getText());
					chat.handleMessageFromClientUI("#SERVER_CONNECT "
							+ nickField.getText() + " " + passField.getText());
					connStatus.setText("Connected");
				} catch (IOException exception) {
					gui.display("Error: Can't setup connection!");
				}
			} else
				display("already connected");
		}

	}

	private class ChatAction implements ActionListener {
		ChatGUI gui;

		public ChatAction(ChatGUI c) {
			gui = c;
		}

		public void actionPerformed(ActionEvent e) {
			String msg = chatLine.getText();
			try {
				gui.chat.handleMessageFromClientUI(msg);
			} catch (NullPointerException error) {
				display("Error! No connection with server.");
			}
			chatLine.setText("");
		}
	}
}
