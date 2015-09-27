package me.krickl.memebotj;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class ConnectionHandler {
	private static final Logger log = Logger.getLogger(ConnectionHandler.class.getName());

	private String server, botNick, password;
	private int port;
	private Socket ircSocket;
	private BufferedReader inFromServer;
	private DataOutputStream outToServer;

	public ConnectionHandler(String server, int port, String botNick, String password) throws IOException {
		// log.addHandler(Memebot.ch);
		// log.setUseParentHandlers(false);

		this.server = server;
		this.port = port;
		this.botNick = botNick;
		this.password = password;

		try {
			this.ircSocket = new Socket(server, port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.inFromServer = new BufferedReader(new InputStreamReader(this.ircSocket.getInputStream(), "UTF-8"));

		this.outToServer = new DataOutputStream(this.ircSocket.getOutputStream());

		log.info(String.format("Connectiong to server %s with username %s on port %d\n", this.server, this.botNick,
				this.port));

		this.outToServer.writeBytes("PASS " + this.password + "\n");
		this.outToServer.writeBytes("NICK " + this.botNick + "\n");
	}

	public void ping() throws IOException {
		log.info("Responding to ping request!");

		this.outToServer.writeBytes("PING :PONG\n");
	}

	public String[] recvData() throws IOException {
		String ircmsg = this.inFromServer.readLine().replace("\n", "").replace("\r", "");
		String channel = "";
		int hashIndex = ircmsg.indexOf(" #");

		if (hashIndex > 0) {
			for (int i = hashIndex + 1; i < ircmsg.length(); i++) {
				if (ircmsg.charAt(i) != ' ') {
					channel = channel + ircmsg.charAt(i);
				} else {
					break;
				}
			}
		}

		log.info("<" + channel + "> " + ircmsg);

		if (ircmsg.contains("PING :")) {
			this.ping();
		}

		String[] returnArray = { channel, ircmsg };
		return returnArray;
	}

	public void close() {
		try {
			this.outToServer.close();
			this.inFromServer.close();
			this.ircSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getBotNick() {
		return botNick;
	}

	public BufferedReader getInFromServer() {
		return inFromServer;
	}

	public DataOutputStream getOutToServer() {
		return outToServer;
	}
}
