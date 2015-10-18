package me.krickl.memebotj.api;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.Memebot;

public class APIConnectionHandler implements Runnable {
	private static final Logger log = Logger.getLogger(APIConnectionHandler.class.getName());

	private DatagramSocket socket = null;
	private Thread t;
	private boolean runapi = true;

	public APIConnectionHandler(int port) {
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendData(String data, String ip, int port, ChannelHandler channel) {
		byte[] dataBytes = new byte[1024];
		try {
			InetAddress IPAddress = InetAddress.getByName(ip);
			dataBytes = data.getBytes();

			DatagramPacket packet = new DatagramPacket(dataBytes, dataBytes.length, IPAddress, port);
			socket.send(packet);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info("<API> IP: " + ip + ":" + Integer.toString(port) + " Channel:  " + channel + ">>" + data);
	}

	public String[] receiveData() {
		byte[] data = new byte[1024];
		String[] dataReturn = new String[3];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		try {
			socket.receive(packet);
			dataReturn[0] = new String(packet.getData());
			dataReturn[1] = packet.getAddress().toString();
			dataReturn[2] = Integer.toString(packet.getPort());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info("<API> IP: " + dataReturn[1] + ":" + dataReturn[2] + ">>" + dataReturn[0]);

		//0 is data | 1 is ip | 2 is port
		return dataReturn;
	}

	@Override
	public void run() {
		while(this.runapi) {
			String[] data = this.receiveData();
			String message = data[0];
			String ip = data[1];
			int port = Integer.parseInt(data[2]);

			//messages will always have the following format:
			/*
			 * pkey=<private key>;;sender=<sender name, sender private key or application name>;;
			 * request=<handshake (other request types will be added later)>;;message=<Content of message> 
			 */

			String[] buffer = message.split(";;");
			String pkey = "";
			String sender = "";
			String request = "";
			String parsedMessage = "";

			for(int i = 0; i < buffer.length; i++) {
				if(i==0) {
					pkey = buffer[i].replace("pkey=", "");
				} else if(i==1) {
					sender = buffer[i].replace("sender=", "");
				} else if(i==2) {
					request = buffer[i].replace("request=", "");
				} else if(i==3) {
					parsedMessage = buffer[i].replace("message=", "");
				}
			}

			boolean success = false;

			if(request.equals("handshake")) {
				if(pkey.equals(Memebot.apiMasterKey())) {
					this.sendData("pkey=apisource;;sender=apisource;;request=hello;;message=Access Granted", ip, port, null);
					success = true;
				}

				for(ChannelHandler ch : Memebot.joinedChannels()) {
					if(ch.getPrivateKey().equals(pkey)) {
						ch.setApiConnectionIP(ip);
						this.sendData("pkey=apisource;;sender=apisource;;request=hello;;message=Access Granted", ip, port, ch);
						success = true;
					}
				}
			}

			if(!success) {
				this.sendData("pkey=apisource;;sender=apisource;;request=invalid;;message=Connection Failed", ip, port, null);
			}
		}

	}

	public void strart() {
		if (t == null) {
			t = new Thread(this, "api.thread");
			t.start();
		}
	}
}
