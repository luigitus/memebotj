package me.krickl.memebotj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import me.krickl.memebotj.InternalCommands.APIInformationCommand;
import me.krickl.memebotj.InternalCommands.AboutCommand;
import me.krickl.memebotj.InternalCommands.AddCommandHandler;
import me.krickl.memebotj.InternalCommands.AliasCommand;
import me.krickl.memebotj.InternalCommands.AutogreetCommand;
import me.krickl.memebotj.InternalCommands.ChannelInfoCommand;
import me.krickl.memebotj.InternalCommands.CommandList;
import me.krickl.memebotj.InternalCommands.CommandManager;
import me.krickl.memebotj.InternalCommands.DampeCommand;
import me.krickl.memebotj.InternalCommands.DebugCommand;
import me.krickl.memebotj.InternalCommands.DeletCommandHandler;
import me.krickl.memebotj.InternalCommands.EditChannel;
import me.krickl.memebotj.InternalCommands.EditCommand;
import me.krickl.memebotj.InternalCommands.EditUserCommand;
import me.krickl.memebotj.InternalCommands.FilenameCommand;
import me.krickl.memebotj.InternalCommands.GiveAwayPollCommand;
import me.krickl.memebotj.InternalCommands.HelpCommand;
import me.krickl.memebotj.InternalCommands.HugCommand;
import me.krickl.memebotj.InternalCommands.HypeCommand;
import me.krickl.memebotj.InternalCommands.JoinCommand;
import me.krickl.memebotj.InternalCommands.ModeratorsCommand;
import me.krickl.memebotj.InternalCommands.MujuruGame;
import me.krickl.memebotj.InternalCommands.PartCommand;
import me.krickl.memebotj.InternalCommands.PointsCommand;
import me.krickl.memebotj.InternalCommands.PyramidCommand;
import me.krickl.memebotj.InternalCommands.QuitCommand;
import me.krickl.memebotj.InternalCommands.RaceCommand;
import me.krickl.memebotj.InternalCommands.SaveCommand;
import me.krickl.memebotj.InternalCommands.SendMessageCommand;
import me.krickl.memebotj.InternalCommands.SpeedrunCommand;
import me.krickl.memebotj.InternalCommands.UptimeCommand;
import me.krickl.memebotj.InternalCommands.WhoisCommand;

/**
 * This class handles everything related to channels.
 * @author unlink
 *
 */
public class ChannelHandler implements Runnable {
	private static final Logger log = Logger.getLogger(ChannelHandler.class.getName());

	private String channel;
	private ConnectionHandler connection;
	private String broadcaster;
	// private ArrayList<String> modList = new ArrayList<String>();
	// private ArrayList<String> viewerList = new ArrayList<String>();
	private HashMap<String, UserHandler> userList = new HashMap<String, UserHandler>();
	private Cooldown updateCooldown = new Cooldown(60);
	private ArrayList<CommandHandler> channelCommands = new ArrayList<CommandHandler>();
	private ArrayList<CommandHandler> internalCommands = new ArrayList<CommandHandler>();
	private String followerNotification = ""; // if notification is empty it'll
												// not send
	private String channelInfoURL = "";
	private String channelFollowersURL = "";
	private String raceBaseURL = "http://kadgar.net/live";
	private String greetMessage = "Hello I'm {appname} {version} build {build} the dankest irc bot ever RitzMitz";
	private String currentRaceURL = "";
	private ArrayList<String> fileNameList = new ArrayList<String>();
	private ArrayList<String> aliasList = new ArrayList<String>();
	private int maxFileNameLen = 8;
	private String currentFileName = "";
	private int streamStartTime = 0;

	private HashMap<String, String> builtInStrings = new HashMap<String, String>();
	// private ArrayList<String> songList = new ArrayList<String>();
	// private int maxSongLen = 600; // song length in seconds
	// private String emebdCodeYT = "<iframe width=\"420\" height=\"315\"
	// src=\"{url}\" frameborder=\"0\" allowfullscreen></iframe>";
	private String channelPageURL;
	private String channelPageBaseURL;
	private String htmlDir;
	private String youtubeAPIURL = "https://www.googleapis.com/youtube/v3/videos?id={videoid}&part=contentDetails&key="
			+ Memebot.youtubeAPIKey();

	private ArrayList<String> otherLoadedChannels = new ArrayList<String>();
	private HashMap<String, String> autogreetList = new HashMap<String, String>();

	private MongoCollection<Document> channelCollection;

	private double pointsPerUpdate = 1.0f;

	private Thread t;
	private boolean isJoined = true;
	private boolean allowAutogreet = true;
	private boolean isLive = false;

	private int currentMessageCount = 0;
	private Cooldown messageLimitCooldown = new Cooldown(30);
	private Cooldown preventMessageCooldown = new Cooldown(30);

	private String currentGame = "Not Playing";
	
	private String privateKey = "";
	private SecureRandom random = new SecureRandom();
	private String apiConnectionIP = "";
	
	private String urlRegex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	private boolean purgeURLS = false;
	private boolean purgeURLSNewUsers = false;
	private int linkTimeout = 1;

	public ChannelHandler(String channel, ConnectionHandler connection) {
		// log.addHandler(Memebot.ch);
		// log.setUseParentHandlers(false);

		this.channel = channel;
		this.connection = connection;
		this.broadcaster = this.channel.replace("#", "");
		this.channelInfoURL = "https://api.twitch.tv/kraken/channels/" + this.broadcaster;
		this.channelFollowersURL = channelInfoURL + "/follows/?limit=";
		this.channelPageBaseURL = Memebot.webBaseURL() + this.broadcaster;
		this.channelPageURL = Memebot.webBaseURL() + this.broadcaster + "/index.html";
		this.htmlDir = Memebot.htmlDir() + "/" + this.broadcaster;

		log.info("Joining channel " + this.channel);

		UserHandler broadcasterHandler = new UserHandler(this.broadcaster, this.channel);
		broadcasterHandler.setBroadcaster(true);
		broadcasterHandler.setMod(true);
		this.userList.put(this.broadcaster, broadcasterHandler);

		// set default built in strings
		builtInStrings.put("HELP_NOT_FOUND", "Could not find help for that command");
		builtInStrings.put("HELP_SYNTAX", "Syntax: {param1}");
		builtInStrings.put("ADDCOM_SYNTAX", "Syntax: {param1}");
		builtInStrings.put("CHMOD_SYNTAX", "Usage: {param1}");
		builtInStrings.put("EDITCOMMAND_OK", "Edited command {param1}. Changed {param2} to {param3}.");
		builtInStrings.put("EDITCOMMAND_FAIL", "Could not edit command");
		builtInStrings.put("DELCOM_SYNTAX", "Syntax: {param1}");
		builtInStrings.put("DELCOM_NOT_FOUND", "Could not find command {param1}");
		builtInStrings.put("DELCOM_OK", "{param1} removed");
		builtInStrings.put("CHCHANNEL_SYNTAX", "Syntax: {param1}");
		builtInStrings.put("CURRENCY_NAME", "points");
		builtInStrings.put("CURRENCY_EMOTE", "points");
		builtInStrings.put("COMMANDMANAGER_SYNTAX", "Usage: {param1}");

		//generate private key
		this.privateKey = new BigInteger(130, random).toString(32);
		
		// create dirs
		File htmlDirF = new File(this.htmlDir);
		if (!htmlDirF.exists()) {
			htmlDirF.mkdirs();
		}

		this.joinChannel(this.channel);

		if (Memebot.useMongo()) {
			this.channelCollection = Memebot.db().getCollection(this.channel);
		}

		// this.loadChannelData();
		this.readDBChannelData();
		try {
			this.connection.getOutToServer().writeBytes("CAP REQ :twitch.tv/membership\n");
			this.connection.getOutToServer().writeBytes("CAP REQ :twitch.tv/commands\n");
			this.connection.getOutToServer().writeBytes("CAP REQ :twitch.tv/tags\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// set up internal commands
		this.internalCommands.add(new AboutCommand(this.channel, "!about", "#internal#"));
		this.internalCommands.add(new AddCommandHandler(this.channel, "!addcommand", "#internal#"));
		this.internalCommands.add(new AutogreetCommand(this.channel, "!autogreet", "#internal#"));
		this.internalCommands.add(new EditChannel(this.channel, "!editchannel", "#internal#"));
		this.internalCommands.add(new EditCommand(this.channel, "!editcommand", "#internal#"));
		this.internalCommands.add(new CommandList(this.channel, "!commands", "#internal#"));
		this.internalCommands.add(new DeletCommandHandler(this.channel, "!deletecommand", "#internal#"));
		this.internalCommands.add(new HelpCommand(this.channel, "!help", "#internal#"));
		this.internalCommands.add(new HugCommand(this.channel, "!mehug", "#internal#"));
		this.internalCommands.add(new ModeratorsCommand(this.channel, "!moderators", "#internal#"));
		this.internalCommands.add(new JoinCommand(this.channel, "!mejoin", "#internal#"));
		this.internalCommands.add(new PartCommand(this.channel, "!mepart", "#internal#"));
		this.internalCommands.add(new PointsCommand(this.channel, "!points", "#internal#"));
		this.internalCommands.add(new QuitCommand(this.channel, "!mequit", "#internal#"));
		this.internalCommands.add(new RaceCommand(this.channel, "!race", "#internal#"));
		this.internalCommands.add(new SaveCommand(this.channel, "!mesave", "#internal#"));
		this.internalCommands.add(new WhoisCommand(this.channel, "!whois", "#internal#"));
		this.internalCommands.add(new MujuruGame(this.channel, "!mujuru", "#internal#"));
		this.internalCommands.add(new HypeCommand(this.channel, "!hype", "#internal#"));
		this.internalCommands.add(new FilenameCommand(this.channel, "!name", "#internal#"));
		this.internalCommands.add(new SpeedrunCommand(this.channel, "!wr", "#internal#"));
		this.internalCommands.add(new EditUserCommand(this.channel, "!edituser", "#internal#"));
		this.internalCommands.add(new SendMessageCommand(this.channel, "!sm", "#internal#"));
		this.internalCommands.add(new DampeCommand(this.channel, "!dampe", "#internal#"));
		this.internalCommands.add(new GiveAwayPollCommand(this.channel, "!giveaway", "#internal#"));
		this.internalCommands.add(new DebugCommand(this.channel, "!debug", "#debug#"));
		this.internalCommands.add(new PyramidCommand(this.channel, "!pyramid", "#internal#"));
		this.internalCommands.add(new CommandManager(this.channel, "!command", "#internal#"));
		this.internalCommands.add(new APIInformationCommand(this.channel, "!apiinfo", "#internal#"));
		this.internalCommands.add(new ChannelInfoCommand(this.channel, "!ci", "#internal#"));
		this.internalCommands.add(new UptimeCommand(this.channel, "!uptime", "#internal#"));
		this.internalCommands.add(new AliasCommand(this.channel, "!alias", "#internal#"));

		// internal commands without special classes
		CommandHandler fileNameList = new CommandHandler(this.channel, "!namelist", "#internal#");
		fileNameList.editCommand("output", this.channelPageBaseURL + "/filenames.html",
				new UserHandler("#internal#", this.channel), userList);
		this.internalCommands.add(fileNameList);

		CommandHandler issueCommand = new CommandHandler(this.channel, "!issue", "#internal#");
		issueCommand.editCommand("output",
				"Having issues? Write a bugreport at https://github.com/unlink2/memebotj/issues",
				new UserHandler("#internal#", this.channel), userList);
		this.internalCommands.add(issueCommand);

		CommandHandler mrDestructoidCommand = new CommandHandler(this.channel, "!noamidnightonthethirdday",
				"#internal#");
		mrDestructoidCommand.editCommand("output", "MrDestructoid Midnight Raid MrDestructoid",
				new UserHandler("#internal#", this.channel), userList);
		mrDestructoidCommand.setExcludeFromCommandList(true);
		this.internalCommands.add(mrDestructoidCommand);

		this.sendMessage(
				this.greetMessage.replace("{appname}", BuildInfo.appName()).replace("{version}", BuildInfo.version())
						.replace("{build}", BuildInfo.buildNumber()).replace("{builddate}", BuildInfo.timeStamp()),
				this.channel);
		
		log.info(String.format("Private key for channel %s is %s", this.channel, this.privateKey));
	}

	private void joinChannel(String channel) {
		try {
			this.connection.getOutToServer().writeBytes("JOIN " + channel + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean isInList = false;
		for (ChannelHandler ch : Memebot.joinedChannels()) {
			if (ch.getChannel().equalsIgnoreCase(channel)) {
				isInList = true;
				break;
			}
		}

		if (!isInList) {
			Memebot.joinedChannels().add(this);

			// save list
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(Memebot.channelConfig()));
				for (ChannelHandler ch : Memebot.joinedChannels()) {
					bw.write(ch.getChannel() + "\n");
				}
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		this.isJoined = true;
	}

	public void partChannel(String channel) {
		try {
			this.connection.getOutToServer().writeBytes("PART " + channel + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean isInList = false;
		ChannelHandler removeThisCH = null;
		for (ChannelHandler ch : Memebot.joinedChannels()) {
			if (ch.getChannel().equalsIgnoreCase(channel)) {
				isInList = true;
				removeThisCH = ch;
				break;
			}
		}

		if (!isInList && removeThisCH != null) {
			Memebot.joinedChannels().remove(removeThisCH);

			// save list
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(Memebot.channelConfig()));
				for (ChannelHandler ch : Memebot.joinedChannels()) {
					bw.write(ch.getChannel() + "\n");
				}
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.sendMessage("Leaving channel :(", this.channel);
		this.isJoined = false;
		this.t.interrupt();
		this.connection.close();
		Memebot.joinedChannels().remove(removeThisCH);
	}

	public void writeHTML() {
		if(!Memebot.useWeb()) {
			return;
		}
		
		// index.html
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(this.htmlDir + "/index.html"));
			bw.write("<head><link rel=\"stylesheet\" type=\"text/css\" href=\"../style.css\"></head>");
			bw.write("<html>");
			bw.write("<h1>Index for " + this.broadcaster + "</h1>");
			bw.write("<table style=\"width:100%\">");
			bw.write("<tr>");
			bw.write("<td>");
			bw.write("Command");
			bw.write("</td>");
			bw.write("<td>");
			bw.write("Help");
			bw.write("</td>");
			bw.write("<td>");
			bw.write("Output");
			bw.write("</td>");
			bw.write("<td>");
			bw.write("Access");
			bw.write("</td>");
			bw.write("<td>");
			bw.write("Command Type");
			bw.write("</td>");
			bw.write("</tr>");

			// internal commands
			for (CommandHandler ch : this.internalCommands) {
				if (ch.isExcludeFromCommandList()) {
					continue;
				}
				bw.write("<tr>");
				bw.write("<td>");
				if (ch.getCmdtype().equals("list")) {
					bw.write("<a href=\"" + this.channelPageBaseURL + "/" + ch.getCommand() + ".html\">"
							+ ch.getCommand() + "</a>");

					// write quote html
					BufferedWriter bwq = new BufferedWriter(
							new FileWriter(this.htmlDir + "/" + URLEncoder.encode(ch.getCommand(), "UTF-8") + ".html"));
					bwq.write("<head><link rel=\"stylesheet\" type=\"text/css\" href=\"../style.css\"></head>");
					bwq.write("<html>");
					bwq.write("<h1>");
					bwq.write(ch.getCommand());
					bwq.write("</h1>");

					bwq.write("<table style=\"width:100%\">");
					bwq.write("<tr>");

					bwq.write("<td>");
					bwq.write("#");
					bwq.write("</td>");

					bwq.write("<td>");
					bwq.write("Content");
					bwq.write("</td>");
					bwq.write("</tr>");

					int c = 0;
					for (String item : ch.getListContent()) {
						bwq.write("<tr>");
						bwq.write("<td>");
						bwq.write(Integer.toString(c));
						bwq.write("</td>");

						bwq.write("<td>");
						bwq.write(item);
						bwq.write("</td>");

						bwq.write("</tr>");

						c++;
					}

					bwq.write("</table>");
					bwq.write("</html>");
					bwq.close();
				} else {
					bw.write(ch.getCommand());
				}
				bw.write("</td>");
				bw.write("<td>");
				bw.write(ch.getHelptext());
				bw.write("</td>");
				bw.write("<td>");
				bw.write(ch.getUnformattedOutput());
				bw.write("</td>");
				bw.write("<td>");
				bw.write(Integer.toString(ch.getNeededCommandPower()));
				bw.write("</td>");
				bw.write("<td>");
				bw.write(ch.getCmdtype());
				bw.write("</td>");
				bw.write("</tr>");
			}

			// channel commands
			for (CommandHandler ch : this.channelCommands) {
				if (ch.isExcludeFromCommandList()) {
					continue;
				}
				bw.write("<tr>");
				bw.write("<td>");
				if (ch.getCmdtype().equals("list")) {
					bw.write("<a href=\"" + this.channelPageBaseURL + "/" + ch.getCommand() + ".html\">"
							+ ch.getCommand() + "</a>");

					// write quote html
					BufferedWriter bwq = new BufferedWriter(
							new FileWriter(this.htmlDir + "/" + ch.getCommand() + ".html"));
					bwq.write("<head><link rel=\"stylesheet\" type=\"text/css\" href=\"../style.css\"></head>");
					bwq.write("<html>");
					bwq.write("<h1>");
					bwq.write(ch.getCommand());
					bwq.write("</h1>");

					bwq.write("<table style=\"width:100%\">");
					bwq.write("<tr>");

					bwq.write("<td>");
					bwq.write("#");
					bwq.write("</td>");

					bwq.write("<td>");
					bwq.write("Content");
					bwq.write("</td>");
					bwq.write("</tr>");

					int c = 0;
					for (String item : ch.getListContent()) {
						bwq.write("<tr>");
						bwq.write("<td>");
						bwq.write(Integer.toString(c));
						bwq.write("</td>");

						bwq.write("<td>");
						bwq.write(item);
						bwq.write("</td>");

						bwq.write("</tr>");

						c++;
					}

					bwq.write("</table>");
					bwq.write("</html>");
					bwq.close();
				} else {
					bw.write(ch.getCommand());
				}
				bw.write("</td>");
				bw.write("<td>");
				bw.write(ch.getHelptext());
				bw.write("</td>");
				bw.write("<td>");
				bw.write(ch.getUnformattedOutput());
				bw.write("</td>");
				bw.write("<td>");
				bw.write(Integer.toString(ch.getNeededCommandPower()));
				bw.write("</td>");
				bw.write("<td>");
				bw.write(ch.getCmdtype());
				bw.write("</td>");
				bw.write("</tr>");
			}

			bw.write("</table>");
			bw.write("</html>");
			bw.close();

			// write file name list
			BufferedWriter bwf = new BufferedWriter(new FileWriter(this.htmlDir + "/filenames.html"));

			bwf.write("<head><link rel=\"stylesheet\" type=\"text/css\" href=\"../style.css\"></head>");
			bwf.write("<html>");
			bwf.write("<h1>");
			bwf.write("Filenames");
			bwf.write("</h1>");

			bwf.write("<table style=\"width:100%\">");
			bwf.write("<tr>");

			bwf.write("<td>");
			bwf.write("Filename");
			bwf.write("</td>");

			bwf.write("<td>");
			bwf.write("Suggested by");
			bwf.write("</td>");
			bwf.write("</tr>");

			for (String name : this.fileNameList) {
				bwf.write("<tr>");
				bwf.write("<td>");
				bwf.write(name.split("#")[0]);
				bwf.write("</td>");

				bwf.write("<td>");
				bwf.write(name.split("#")[1]);
				bwf.write("</td>");

				bwf.write("</tr>");
			}

			bwf.write("</table>");
			bwf.write("</html>");

			bwf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void readDBChannelData() {
		if (!Memebot.useMongo()) {
			return;
		}

		Document channelQuery = new Document("_id", this.channel);
		FindIterable<Document> cursor = this.channelCollection.find(channelQuery);

		Document channelData = cursor.first();

		// read data
		if (channelData != null) {
			this.maxFileNameLen = channelData.getInteger("maxfilenamelen", this.maxFileNameLen);
			this.raceBaseURL = (String) channelData.getOrDefault("raceurl", this.raceBaseURL);
			this.fileNameList = (ArrayList<String>) channelData.getOrDefault("fileanmelist", this.fileNameList);
			this.otherLoadedChannels = (ArrayList<String>) channelData.getOrDefault("otherchannels",
					this.otherLoadedChannels);
			this.pointsPerUpdate = (double) channelData.getOrDefault("pointsperupdate", this.pointsPerUpdate);
			this.allowAutogreet = (boolean) channelData.getOrDefault("allowautogreet", this.allowAutogreet);
			this.privateKey = (String) channelData.getOrDefault("privatekey", this.privateKey);
			this.linkTimeout = (int) channelData.getOrDefault("linktimeout", this.linkTimeout);
			this.purgeURLS = (boolean) channelData.getOrDefault("purgelinks", this.purgeURLS);
			this.purgeURLSNewUsers = (boolean) channelData.getOrDefault("purgelinknu", this.purgeURLSNewUsers);
			this.urlRegex = (String) channelData.getOrDefault("urlreges", this.urlRegex);
			this.aliasList = (ArrayList<String>)channelData.getOrDefault("alias", this.aliasList);
			
			Document bultinStringsDoc = (Document) channelData.getOrDefault("builtinstrings", new Document());
			Document autogreetDoc = (Document) channelData.getOrDefault("autogreet", new Document());

			for (String key : bultinStringsDoc.keySet()) {
				this.builtInStrings.put(key, bultinStringsDoc.getString(key));
			}

			for (String key : autogreetDoc.keySet()) {
				this.autogreetList.put(key, autogreetDoc.getString(key));
			}
		}

		// read commands
		MongoCollection<Document> commandCollection = Memebot.db().getCollection(this.channel + "_commands");
		FindIterable<Document> comms = commandCollection.find();
		comms.forEach(new Block<Document>() {

			@Override
			public void apply(Document doc) {
				channelCommands.add(new CommandHandler(channel, doc.getString("command"), null));
			}
		});
	}

	public void writeDBChannelData() {
		if (!Memebot.useMongo()) {
			return;
		}

		log.info("Saving data in db for channel " + this.channel);

		Document channelQuery = new Document("_id", this.channel);

		Document bultinStringsDoc = new Document();

		Document autogreetDoc = new Document();

		for (String key : this.builtInStrings.keySet()) {
			bultinStringsDoc.append(key, this.builtInStrings.get(key));
		}

		for (String key : this.autogreetList.keySet()) {
			autogreetDoc.append(key, this.autogreetList.get(key));
		}

		Document channelData = new Document("_id", this.channel).append("maxfilenamelen", this.maxFileNameLen)
				.append("raceurl", this.raceBaseURL).append("fileanmelist", this.fileNameList)
				.append("otherchannels", this.otherLoadedChannels).append("builtinstrings", bultinStringsDoc)
				.append("autogreet", autogreetDoc).append("pointsperupdate", this.pointsPerUpdate)
				.append("allowautogreet", this.allowAutogreet)
				.append("privatekey", this.privateKey)
				.append("purgelinks", this.purgeURLS)
				.append("purgelinknu", this.purgeURLSNewUsers)
				.append("linktimeout", this.linkTimeout)
				.append("urlreges", this.urlRegex)
				.append("alias", this.aliasList);

		try {
			if (this.channelCollection.findOneAndReplace(channelQuery, channelData) == null) {
				this.channelCollection.insertOne(channelData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String key : this.userList.keySet()) {
			this.userList.get(key).writeDBUserData();
		}

	}

	public void sendMessage(String msg, String channel) {
		if (!this.preventMessageCooldown.canContinue()) {
			return;
		}

		if (this.currentMessageCount >= Memebot.messageLimit()) {
			log.warning("Reached global message limit for 30 seconds. try again later");
			this.preventMessageCooldown.startCooldown();

		}
		this.currentMessageCount++;

		try {
			this.connection.getOutToServer().flush();
			this.connection.getOutToServer()
					.write(new String("PRIVMSG " + this.channel + " :" + msg + "\n").getBytes("UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void update() {
		if (this.messageLimitCooldown.canContinue()) {
			this.messageLimitCooldown.startCooldown();
			this.currentMessageCount = 0;
		}

		if (this.updateCooldown.canContinue()) {
			this.updateCooldown.startCooldown();

			// check if channel is live using twitch api
			try {
				URL url = new URL("https://api.twitch.tv/kraken/streams/" + this.broadcaster);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String dataBuffer = "";
				String data = "";
				while ((dataBuffer = in.readLine()) != null) {
					data = data + dataBuffer;
				}
				in.close();

				JSONParser parser = new JSONParser();
				JSONObject obj = (JSONObject) parser.parse(data);
				Object isOnline = obj.get("stream");

				if (isOnline == null) {
					log.info(String.format("Stream %s is offline", this.channel));
					this.isLive = false;
					this.streamStartTime = -1;
				} else {
					log.info(String.format("Stream %s is live", this.channel));
					if(this.isLive) {
						this.streamStartTime = (int) (System.currentTimeMillis() / 1000L);
					}
					this.isLive = true;
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// get channel info
			try {
				URL url = new URL(this.channelInfoURL);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String dataBuffer = "";
				String data = "";
				while ((dataBuffer = in.readLine()) != null) {
					data = data + dataBuffer;
				}
				in.close();

				JSONParser parser = new JSONParser();
				JSONObject obj = (JSONObject) parser.parse(data);
				this.currentGame = (String) obj.getOrDefault("game", "Not Playing");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// this.saveChannelData();
			this.writeDBChannelData();
			this.writeHTML();

			for (String key : this.userList.keySet()) {
				UserHandler uh = this.userList.get(key);
				uh.update();
				if (this.isLive) {
					uh.setPoints(uh.getPoints() + this.pointsPerUpdate);
				}
				// uh.saveUserData();
				uh.writeDBUserData();
			}

			for (CommandHandler ch : this.channelCommands) {
				ch.update(this);
			}
		}
	}

	public void handleMessage(String rawircmsg) {
		String senderName = "";
		HashMap<String, String> ircTags = new HashMap<String, String>();
		String[] msgContent = null;

		String[] ircmsgBuffer = rawircmsg.split(" ");

		String messageType = "UNDEFINED";
		int i = 0;

		for (i = 0; i < ircmsgBuffer.length; i++) {
			String msg = ircmsgBuffer[i];

			// first off make sure what type it is
			if ((msg.equals("PRIVMSG") || msg.equals("MODE") || msg.equals("PART") || msg.equals("JOIN")
					|| msg.equals("CLEARCHAT")) && messageType.equals("UNDEFINED")) {
				messageType = msg;
			}

			// irc tags
			if (msg.charAt(0) == '@' && i == 0) {
				String[] tagList = msg.split(";");

				for (String tag : tagList) {
					try {
						ircTags.put(tag.split("=")[0], tag.split("=")[1]);
					} catch (ArrayIndexOutOfBoundsException e) {

					}
				}
			} else if (i == 0 || (i == 1 && senderName.isEmpty())) {
				boolean exclaReached = false;
				for (int j = 0; j < msg.length(); j++) {
					if (msg.charAt(j) == '!') {
						exclaReached = true;
						break;
					}
					if (msg.charAt(j) != ':') {
						senderName = senderName + msg.charAt(j);
					}
				}

				if (!exclaReached) {
					senderName = "#internal#";
				}
			}

			if (messageType.equals("PRIVMSG") && i > 3) {
				if (i == 4) {
					msgContent = new String[ircmsgBuffer.length - 4];
					msgContent[i - 4] = msg.substring(1);
				} else {
					msgContent[i - 4] = msg;
				}
			}
		}

		// create user if it does not yet exist
		if (!this.userList.containsKey(senderName) && !senderName.isEmpty()) {
			UserHandler newUser = new UserHandler(senderName, this.channel);
			this.userList.put(senderName, newUser);
		}

		// get sender object
		UserHandler sender = this.userList.get(senderName);

		// this is still old code and needs to be reworked
		if (!messageType.equals("PRIVMSG")) {
			// check other message
			String[] ircmsgList = rawircmsg.split(" ");

			if (ircmsgList[1] == null) {
				return;
			}
			// handle mod status
			if (ircmsgList[1].equals("MODE")) {
				UserHandler user = null;
				if (!this.userList.containsKey(ircmsgList[4])) {
					user = new UserHandler(ircmsgList[4], this.channel);
					this.userList.put(senderName, user);
				} else {
					user = this.userList.get(ircmsgList[4]);
				}
				if (user != null) {
					if (ircmsgList[3].equals("+o")) {
						user.setMod(true);
						if (!user.isBroadcaster()) {
							user.setCommandPower(25);
						}
					} else {
						user.setMod(false);
						user.setCommandPower(10);
					}
				}
			} else if (ircmsgList[1].equals("PART")) {
				if (sender != null) {
					if (this.userList.containsKey(sender.getUsername())) {
						// this.userList.get(sender.getUsername()).saveUserData();
						this.userList.get(sender.getUsername()).writeDBUserData();
						this.userList.remove(sender.getUsername());
					}
				}
			} else if (ircmsgList[1].equals("JOIN")) {
				if (sender != null) {
					//legacy
					if (this.autogreetList.containsKey(sender.getUsername())) {
						if (this.allowAutogreet) {
							this.sendMessage(this.autogreetList.get(sender.getUsername()), this.channel);
						}
					} else {
						if(this.allowAutogreet && !sender.getAutogreet().equals("")) {
							this.sendMessage(sender.getAutogreet(), this.channel);
						}
					}
				}
			} else if (ircmsgList[1].equals("CLEARCHAT")) {
				if (this.userList.containsKey(ircmsgList[3].replace(":", ""))) {
					this.userList.get(ircmsgList[3].replace(":", ""))
							.setTimeouts(this.userList.get(ircmsgList[3].replace(":", "")).getTimeouts() + 1);
					this.userList.get(ircmsgList[3].replace(":", "")).writeDBUserData();
				} else {
					UserHandler uh = new UserHandler(ircmsgList[3].replace(":", ""), this.channel);
					if (!uh.isNewUser()) {
						uh.setTimeouts(uh.getTimeouts() + 1);
						uh.writeDBUserData();
					}
				}
			}
		} else {
			// check irc tags
			if (ircTags.containsKey("user-type")) {
				if (ircTags.get("user-type").equals("mod") && !sender.isBroadcaster()) {
					sender.setMod(true);
					sender.setCommandPower(25);
				} else if (!sender.isBroadcaster()) {
					sender.setMod(false);
					sender.setCommandPower(10);
				}
			}

			// check broadcaster status
			if (sender.getUsername().equalsIgnoreCase(this.broadcaster)) {
				sender.setMod(true);
				sender.setBroadcaster(true);
				sender.setCommandPower(50);
			}

			// check botadmin status
			for (String user : Memebot.botAdmins()) {
				if (user.equalsIgnoreCase(sender.getUsername())) {
					sender.setCommandPower(75);
				}
			}

			String msg = msgContent[0];
			String[] data = Arrays.copyOfRange(msgContent, 0, msgContent.length);

			//purge links
			try {
				for(int x = 0; x < data.length; x++) {
					if(data[x].matches(this.urlRegex)) {
						log.info("Found url in message");
						if(this.purgeURLSNewUsers && sender.isNewUser()) {
							log.info("Puriging " + sender.getUsername() + " for posting a link that matches the regex " + this.urlRegex);
							this.sendMessage("/timeout " + sender.getUsername() + " " + Integer.toString(this.linkTimeout), this.channel);
						} else if(this.purgeURLS) {
							log.info("Puriging " + sender.getUsername() + " for posting a link that matches the regex " + this.urlRegex);
							this.sendMessage("/timeout " + sender.getUsername() + " " + Integer.toString(this.linkTimeout), this.channel);
						}
					}
				}
			} catch(java.util.regex.PatternSyntaxException e) {
				e.printStackTrace();
			}
			
			//check aliases
			try {
				for(String alias : this.aliasList) {
					String[] aliasSplit = alias.split("#");
					if(aliasSplit[0].equals(msg)) {
						ArrayList<String> buffer = new ArrayList<String>();
						for(String str : aliasSplit[1].split("_")) {
							buffer.add(str);
						}
						try {
							for(String str : Arrays.copyOfRange(data, buffer.size() - 1, data.length)) {
								buffer.add(str);
							}
						} catch(java.lang.IllegalArgumentException e) {
							e.printStackTrace();
						}
						data = (String[])buffer.toArray(new String[buffer.size()]);
						msg = data[0];
					}
				}
			} catch(ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			
			// check channel commands
			int p = -1;
			if ((p = this.findCommand(msg)) != -1) {
				if (!this.channelCommands.get(p).isTexttrigger()) {
					this.channelCommands.get(p).execCommand(sender, this, data, userList);
				}
			}

			// check text trigger
			for (CommandHandler ch : this.channelCommands) {
				if (ch.isTexttrigger()) {
					for (String s : msgContent) {
						if (s.equals(ch.getCommand())) {
							ch.execCommand(sender, this, new String[] { "" }, userList);
						}
					}
				}
			}

			// exec other channel's command
			for (ChannelHandler ch : Memebot.joinedChannels()) {
				for (String och : this.otherLoadedChannels) {
					// System.out.println(msg.replace(och.replace("#", "") +
					// ".",
					// ""));
					CharSequence channel = ch.getBroadcaster();
					if (ch.getChannel().equals(och) || ch.getBroadcaster().equals(och)) {
						if ((p = ch.findCommand(msg.replace(och.replace("#", "") + ".", ""))) != -1
								&& msg.contains(channel)) {
							ch.channelCommands.get(p).execCommand(new UserHandler("#readonly#", this.channel), this,
									data, userList);
						}
					}
				}
			}

			// exec internal commands
			for (CommandHandler ch : this.internalCommands) {
				if (ch.getCommand().equals(msg)) {
					ch.execCommand(sender, this, Arrays.copyOfRange(data, 1, data.length), userList);
				}
			}
		}
	}

	@Override
	public void run() {
		while (this.isJoined) {
			String[] ircmsg = { "", "" };
			try {
				ircmsg = this.connection.recvData();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			this.update();

			if (this.getChannel().equalsIgnoreCase(ircmsg[0])) {
				this.handleMessage(ircmsg[1]);
			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void strart() {
		if (t == null) {
			t = new Thread(this, this.channel);
			t.start();
		}
	}

	public int findCommand(String command) {
		return this.findCommand(command, this.channelCommands);
	}

	public int findCommand(String command, ArrayList<CommandHandler> commandList) {
		for (int i = 0; i < commandList.size(); i++) {
			if (commandList.get(i).command().equals(command)) {
				return i;
			}
		}

		return -1;
	}

	public String getChannel() {
		return channel;
	}

	public HashMap<String, UserHandler> getUserList() {
		return userList;
	}

	public void setUserList(HashMap<String, UserHandler> userList) {
		this.userList = userList;
	}

	public boolean isJoined() {
		return isJoined;
	}

	public void setJoined(boolean isJoined) {
		this.isJoined = isJoined;
	}

	public ConnectionHandler getConnection() {
		return connection;
	}

	public void setConnection(ConnectionHandler connection) {
		this.connection = connection;
	}

	public String getBroadcaster() {
		return broadcaster;
	}

	public void setBroadcaster(String broadcaster) {
		this.broadcaster = broadcaster;
	}

	public Cooldown getUpdateCooldown() {
		return updateCooldown;
	}

	public void setUpdateCooldown(Cooldown updateCooldown) {
		this.updateCooldown = updateCooldown;
	}

	public ArrayList<CommandHandler> getChannelCommands() {
		return channelCommands;
	}

	public void setChannelCommands(ArrayList<CommandHandler> channelCommands) {
		this.channelCommands = channelCommands;
	}

	public ArrayList<CommandHandler> getInternalCommands() {
		return internalCommands;
	}

	public void setInternalCommands(ArrayList<CommandHandler> internalCommands) {
		this.internalCommands = internalCommands;
	}

	public String getFollowerNotification() {
		return followerNotification;
	}

	public void setFollowerNotification(String followerNotification) {
		this.followerNotification = followerNotification;
	}

	public String getChannelInfoURL() {
		return channelInfoURL;
	}

	public void setChannelInfoURL(String channelInfoURL) {
		this.channelInfoURL = channelInfoURL;
	}

	public String getChannelFollowersURL() {
		return channelFollowersURL;
	}

	public void setChannelFollowersURL(String channelFollowersURL) {
		this.channelFollowersURL = channelFollowersURL;
	}

	public String getRaceBaseURL() {
		return raceBaseURL;
	}

	public void setRaceBaseURL(String raceBaseURL) {
		this.raceBaseURL = raceBaseURL;
	}

	public String getGreetMessage() {
		return greetMessage;
	}

	public void setGreetMessage(String greetMessage) {
		this.greetMessage = greetMessage;
	}

	public String getCurrentRaceURL() {
		return currentRaceURL;
	}

	public void setCurrentRaceURL(String currentRaceURL) {
		this.currentRaceURL = currentRaceURL;
	}

	public ArrayList<String> getFileNameList() {
		return fileNameList;
	}

	public void setFileNameList(ArrayList<String> fileNameList) {
		this.fileNameList = fileNameList;
	}

	public int getMaxFileNameLen() {
		return maxFileNameLen;
	}

	public void setMaxFileNameLen(int maxFileNameLen) {
		this.maxFileNameLen = maxFileNameLen;
	}

	public HashMap<String, String> getBuiltInStrings() {
		return builtInStrings;
	}

	public void setBuiltInStrings(HashMap<String, String> builtInStrings) {
		this.builtInStrings = builtInStrings;
	}

	public String getChannelPageURL() {
		return channelPageURL;
	}

	public void setChannelPageURL(String channelPageURL) {
		this.channelPageURL = channelPageURL;
	}

	public String getChannelPageBaseURL() {
		return channelPageBaseURL;
	}

	public void setChannelPageBaseURL(String channelPageBaseURL) {
		this.channelPageBaseURL = channelPageBaseURL;
	}

	public String getHtmlDir() {
		return htmlDir;
	}

	public void setHtmlDir(String htmlDir) {
		this.htmlDir = htmlDir;
	}

	public String getYoutubeAPIURL() {
		return youtubeAPIURL;
	}

	public void setYoutubeAPIURL(String youtubeAPIURL) {
		this.youtubeAPIURL = youtubeAPIURL;
	}

	public ArrayList<String> getOtherLoadedChannels() {
		return otherLoadedChannels;
	}

	public void setOtherLoadedChannels(ArrayList<String> otherLoadedChannels) {
		this.otherLoadedChannels = otherLoadedChannels;
	}

	public HashMap<String, String> getAutogreetList() {
		return autogreetList;
	}

	public void setAutogreetList(HashMap<String, String> autogreetList) {
		this.autogreetList = autogreetList;
	}

	public MongoCollection<Document> getChannelCollection() {
		return channelCollection;
	}

	public void setChannelCollection(MongoCollection<Document> channelCollection) {
		this.channelCollection = channelCollection;
	}

	public Thread getT() {
		return t;
	}

	public void setT(Thread t) {
		this.t = t;
	}

	public static Logger getLog() {
		return log;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getCurrentFileName() {
		return currentFileName;
	}

	public void setCurrentFileName(String currentFileName) {
		this.currentFileName = currentFileName;
	}

	public double getPointsPerUpdate() {
		return pointsPerUpdate;
	}

	public void setPointsPerUpdate(double pointsPerUpdate) {
		this.pointsPerUpdate = pointsPerUpdate;
	}

	public boolean isAllowAutogreet() {
		return allowAutogreet;
	}

	public void setAllowAutogreet(boolean allowAutogreetForNonMods) {
		this.allowAutogreet = allowAutogreetForNonMods;
	}

	public boolean isLive() {
		return isLive;
	}

	public void setLive(boolean isLive) {
		this.isLive = isLive;
	}

	public int getCurrentMessageCount() {
		return currentMessageCount;
	}

	public void setCurrentMessageCount(int currentMessageCount) {
		this.currentMessageCount = currentMessageCount;
	}

	public Cooldown getMessageLimitCooldown() {
		return messageLimitCooldown;
	}

	public void setMessageLimitCooldown(Cooldown messageLimitCooldown) {
		this.messageLimitCooldown = messageLimitCooldown;
	}

	public Cooldown getPreventMessageCooldown() {
		return preventMessageCooldown;
	}

	public void setPreventMessageCooldown(Cooldown preventMessageCooldown) {
		this.preventMessageCooldown = preventMessageCooldown;
	}

	public String getCurrentGame() {
		return currentGame;
	}

	public void setCurrentGame(String currentGame) {
		this.currentGame = currentGame;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public SecureRandom getRandom() {
		return random;
	}

	public void setRandom(SecureRandom random) {
		this.random = random;
	}

	public String getApiConnectionIP() {
		return apiConnectionIP;
	}

	public void setApiConnectionIP(String apiConnectionIP) {
		this.apiConnectionIP = apiConnectionIP;
	}

	public int getStreamStartTime() {
		return streamStartTime;
	}

	public void setStreamStartTime(int streamStartTime) {
		this.streamStartTime = streamStartTime;
	}

	public String getUrlRegex() {
		return urlRegex;
	}

	public void setUrlRegex(String urlRegex) {
		this.urlRegex = urlRegex;
	}

	public boolean isPurgeURLS() {
		return purgeURLS;
	}

	public void setPurgeURLS(boolean purgeURLS) {
		this.purgeURLS = purgeURLS;
	}

	public boolean isPurgeURLSNewUsers() {
		return purgeURLSNewUsers;
	}

	public void setPurgeURLSNewUsers(boolean purgeURLSNewUsers) {
		this.purgeURLSNewUsers = purgeURLSNewUsers;
	}

	public int getLinkTimeout() {
		return linkTimeout;
	}

	public void setLinkTimeout(int linkTimeout) {
		this.linkTimeout = linkTimeout;
	}

	public ArrayList<String> getAliasList() {
		return aliasList;
	}

	public void setAliasList(ArrayList<String> aliasList) {
		this.aliasList = aliasList;
	}
}
