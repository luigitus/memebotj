/**
memebotj - an irc bot for twitch.tv
Copyright (c) 2015, Lukas Krickl
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. All advertising materials mentioning features or use of this software
   must display the following acknowledgement:
   This product includes software developed by the Lukas Krickl.
4. Neither the name of the Lukas Krickl nor the
   names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Lukas Krickl ''AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Lukas Krickl BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

**/
package me.krickl.memebotj;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Memebot {
	private static final Logger log = Logger.getLogger(ChannelHandler.class.getName());
	
	public static String ircServer = "irc.twitch.tv";
	public static int port = 6667;
	public static String mongoHost = "localhost";
	public static int mongoPort = 27017;
	public static String mongoDBName = "memebot";
	public static String home = System.getProperty("user.home");
	public static String memebotDir = System.getProperty("user.home") + "/.memebot";
	public static String htmlDir; // hard coded for now
	public static String configFile = memebotDir + "/memebot.cfg";
	public static String channelConfig = memebotDir + "/channels.cfg";
	public static String botNick = null;
	public static String botPassword = null;
	public static String clientID = null;
	public static String clientSecret = null;
	public static List<String> botAdmins = new ArrayList<String>();
	public static String mongoUser = "";
	public static String mongoPassword = "";
	public static boolean useMongoAuth = false;
	//public static List<BlacklistModel> blackList = new ArrayList<BlacklistModel>();
	public static int pid = 0;
	public static ArrayList<String> channels = new ArrayList<String>();
	
	//public static ConnectionHandler connection = null;
	public static ArrayList<ChannelHandler> joinedChannels = new ArrayList<ChannelHandler>();
	public static String youtubeAPIKey = "";
	public static boolean useMongo = true;
	//public static boolean updateToMongo = false;
	public static MongoClient mongoClient;
	public static MongoDatabase db;

	public static String lastError = "";

	public static final int messageLimit = 19; // message limit per 30 seconds
	public static int currentMessageCount = 0;

	public static Cooldown messageLimitCooldown = new Cooldown(30);

	public static MongoCollection<Document> internalCollection;
	
	public static String webBaseURL = "";

	// public static final ConsoleHandler ch = new ConsoleHandler();

	public static void main(String[] args) {
		// set up logging
		// ch.setLevel(Level.ALL);
		// ch.setFormatter(new SimpleFormatter());

		// initial setup
		new File(home + "/.memebot").mkdir();
		new File(home + "/.memebot/channels");
		
		// calculate build hash from md-5 sum of jar file
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] jarBytes = Files.readAllBytes(Paths.get(Memebot.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
			byte[] hashBytes = digest.digest(jarBytes);
			
			//to hex string
			for(byte b : hashBytes) {
				BuildInfo.revisionNumber = BuildInfo.revisionNumber + String.format("%02x", b);
			}
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
		
		//read config
		Properties config = new Properties();
		try {
			config.load(new FileReader(Memebot.configFile));
		} catch (FileNotFoundException e2) {
			try {
				new File(Memebot.configFile).createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		//read botadmin file
		Memebot.botAdmins.add("#internal#");
		try {
			Memebot.botAdmins = Files.readAllLines(Paths.get(Memebot.memebotDir + "/botadmins.cfg"));
			Memebot.botAdmins.add("#internal#");
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		Memebot.ircServer = config.getProperty("ircserver", Memebot.ircServer);
		Memebot.port = Integer.parseInt(config.getProperty("ircport", Integer.toString(Memebot.port)));
		Memebot.mongoHost = config.getProperty("mongohost", Memebot.mongoHost);
		Memebot.mongoPort = Integer.parseInt(config.getProperty("mongoport", Integer.toString(Memebot.mongoPort)));
		Memebot.mongoDBName = config.getProperty("mongodbname", Memebot.mongoDBName);
		Memebot.botNick = config.getProperty("botnick", Memebot.botNick);
		Memebot.botPassword = config.getProperty("botpassword", Memebot.botPassword);
		Memebot.clientID = config.getProperty("clientid", Memebot.clientID);
		Memebot.clientSecret = config.getProperty("clientsecret", Memebot.clientSecret);
		Memebot.htmlDir = config.getProperty("htmldir", Memebot.htmlDir);
		Memebot.youtubeAPIKey = config.getProperty("ytapikey", Memebot.youtubeAPIKey);
		Memebot.mongoUser = config.getProperty("mongouser", Memebot.mongoUser);
		Memebot.mongoPassword = config.getProperty("mongopassword", Memebot.mongoPassword);
		Memebot.useMongoAuth = Boolean.parseBoolean(config.getProperty("mongoauth", Boolean.toString(Memebot.useMongoAuth)));
		Memebot.webBaseURL = config.getProperty("weburl", Memebot.webBaseURL);
		
		//save properties
		OutputStream out;
		try {
			out = new FileOutputStream(new File(Memebot.configFile));
			config.store(out, String.format("%s version %s config file", BuildInfo.appName, BuildInfo.version));
			out.close();
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.warning("Process received SIGTERM...");
				for (ChannelHandler ch : Memebot.joinedChannels) {
					ch.writeDBChannelData();
					ch.setJoined(false);
				}
			}
		});
		log.info(String.format("%s version %s build %s built on %s\n", BuildInfo.appName, BuildInfo.version, BuildInfo.revisionNumber, BuildInfo.timeStamp));

		// get pid and write to file
		File f = new File(memebotDir + "/pid");
		BufferedWriter bw;
		try {
			log.info("PID: " + ManagementFactory.getRuntimeMXBean().getName());
			bw = new BufferedWriter(new FileWriter(f));
			bw.write(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
			bw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// set up database
		if (Memebot.useMongo) {
			if (Memebot.useMongoAuth) {
				MongoClientURI authuri = new MongoClientURI(String.format("mongodb://%s:%s@%s/?authSource=%s", Memebot.mongoUser, Memebot.mongoPassword, Memebot.mongoHost, Memebot.mongoDBName));
				Memebot.mongoClient = new MongoClient(authuri);
			}
			else {
				Memebot.mongoClient = new MongoClient(Memebot.mongoHost, Memebot.mongoPort);
			}
			Memebot.db = Memebot.mongoClient.getDatabase(Memebot.mongoDBName);
			Memebot.internalCollection = Memebot.db.getCollection("#internal#");
		}
		
		// read blacklist
		// TODO read blacklist

		try {
			channels = (ArrayList<String>) Files.readAllLines(Paths.get(Memebot.channelConfig),
					Charset.defaultCharset());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// setup connection

		// join channels
		for (String channel : Memebot.channels) {
			try {
				File login = new File(Memebot.memebotDir + "/" + channel.replace("\n\r", "") + ".login");
				if( login.exists() ) {
					ArrayList<String> loginInfo = (ArrayList<String>)Files.readAllLines(Paths.get(Memebot.memebotDir + "/" + channel.replace("\n\r", "") + ".login"));
					
					log.info("Found login file for channel " + channel);;
					
					ChannelHandler newChannel = new ChannelHandler(channel.replace("\n\r", ""),
							new ConnectionHandler(Memebot.ircServer, Memebot.port, loginInfo.get(0), loginInfo.get(1)));
					newChannel.strart();
				}
				else {
					ChannelHandler newChannel = new ChannelHandler(channel.replace("\n\r", ""),
							new ConnectionHandler(Memebot.ircServer, Memebot.port, Memebot.botNick, Memebot.botPassword));
					newChannel.strart();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
