package me.krickl.memebotj;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

import org.bson.Document;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

/***
 * This class is the base class for all commands.
 * 
 * @author unlink
 *
 */
public class CommandHandler {
	private static final Logger log = Logger.getLogger(CommandHandler.class.getName());

	String channelOrigin = null;
	String command = "null";
	int param = 0;
	double pointCost = 0;
	Cooldown cooldown = new Cooldown(2);
	String access = "viewers";
	String helptext = "null";
	String cmdtype = "default";
	ArrayList<String> listContent = new ArrayList<String>();
	String unformattedOutput = "null";
	String quotePrefix = "#{number}: ";
	String quoteSuffix = "";
	String quoteModAccess = "moderators";
	int counter = 0;
	ArrayList<String> aliases = new ArrayList<String>();
	boolean locked = false;
	boolean texttrigger = false;

	int neededCommandPower = 10;
	int neededModCommandPower = 25;
	int neededBroadcasterCommandPower = 50;
	int neededBotAdminCommandPower = 75;

	int userCooldownLen = 0;
	boolean appendGameToQuote = false;
	boolean appendDateToQuote = false;

	boolean excludeFromCommandList = false;

	private MongoCollection<Document> commandCollection;
	private String commandScript = "";

	public CommandHandler(String channel) {
		this(channel, "null");
	}

	public CommandHandler(String channel, String command) {
		this(channel, command, null);
	}

	public CommandHandler(String channel, String command, String dbprefix) {
		this.channelOrigin = channel;
		this.command = command;

		if (Memebot.useMongo) {
			if (dbprefix == null) {
				this.commandCollection = Memebot.db.getCollection(this.channelOrigin + "_commands");
			} else {
				this.commandCollection = Memebot.db.getCollection(dbprefix + this.channelOrigin + "_commands");
			}
		}

		this.readDBCommand();
	}

	public String execCommand(UserHandler sender, ChannelHandler channelHandler, String[] data,
			HashMap<String, UserHandler> userList) {

		// check global cooldown
		if (!this.cooldown.canContinue() || (!sender.getUserCooldown().canContinue() && !sender.isMod())) {
			return "cooldown";
		}

		// check user cooldown
		if (!sender.getUserCommandCooldowns().containsKey(this.command)) {
			sender.getUserCommandCooldowns().put(this.command, new Cooldown(this.userCooldownLen));
		} else {
			if (sender.getUserCommandCooldowns().get(this.command).getCooldownLen() != this.userCooldownLen) {
				sender.getUserCommandCooldowns().get(this.command).setCooldownLen(this.userCooldownLen);
			}
		}

		if (!sender.getUserCommandCooldowns().get(this.command).canContinue()
				&& !CommandHandler.checkPermission(sender.getUsername(), 75, userList)) {
			return "usercooldown";
		}

		if (!CommandHandler.checkPermission(sender.getUsername(), this.neededCommandPower, userList)) {
			return "denied";
		}
		if(this.checkCost(sender, this.pointCost, channelHandler)){
			channelHandler.sendMessage(String.format("Sorry, you don't have %.2f %s", (float) this.pointCost,
					channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE")), this.channelOrigin);
			return "cost";
		}

		sender.setPoints(sender.getPoints() - this.pointCost);

		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");// dd/MM/yyyy
		Calendar cal = Calendar.getInstance();
		String strDate = sdfDate.format(cal.getTime());

		String formattedOutput = this.unformattedOutput;
		int counterStart = 1;

		if (this.cmdtype.equals("list")) {
			try {
				if (data[1].equals("add")
						&& CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
					String newEntry = "";
					for (int i = 2; i < data.length; i++) {
						newEntry = newEntry + " " + data[i];
					}
					if (this.appendDateToQuote) {
						newEntry = newEntry + " <" + strDate + ">";
					}
					if (this.appendGameToQuote) {
						newEntry = newEntry + " <" + channelHandler.getCurrentGame() + ">";
					}

					this.listContent.add(newEntry);
					formattedOutput = "Added.";
				} else if (data[1].equals("remove")
						&& CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
					try {
						this.listContent.remove(Integer.parseInt(data[2]));
						formattedOutput = "Removed.";
					} catch (IndexOutOfBoundsException e) {
						formattedOutput = e.toString();
					}
				} else if (data[1].equals("edit")
						&& CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
					String newEntry = "";
					for (int i = 3; i < data.length; i++) {
						newEntry = newEntry + " " + data[i];
					}

					this.listContent.set(Integer.parseInt(data[2]), newEntry);
					formattedOutput = "Edited";
				} else if (data[1].equals("list")) {
					formattedOutput = "List: " + channelHandler.getChannelPageBaseURL() + "/" + this.command;
				} else {
					try {
						formattedOutput = this.quotePrefix.replace("{number}", data[1])
								+ this.listContent.get(Integer.parseInt(data[1]))
								+ this.quoteSuffix.replace("{number}", data[1]);
					} catch (NumberFormatException e) {
						formattedOutput = "That's not an integer";
					} catch (IndexOutOfBoundsException e) {
						formattedOutput = "Index out of bounds: Size " + Integer.toString(this.listContent.size());
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				try {
					Random rand = new Random();
					int i = rand.nextInt(this.listContent.size());
					formattedOutput = this.quotePrefix.replace("{number}", Integer.toString(i))
							+ this.listContent.get(i) + this.quoteSuffix.replace("{number}", Integer.toString(i));
				} catch (IllegalArgumentException e2) {
					// just ignore it
				}
			}
		} else if (this.cmdtype.equals("counter")) {
			int modifier = 1;
			try {
				modifier = Integer.parseInt(data[2]);
			} catch (ArrayIndexOutOfBoundsException e) {

			} catch (NumberFormatException e) {

			}

			try {
				if (data[1].equals("add")
						&& CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
					counter = counter + modifier;
				} else if (data[1].equals("sub")
						&& CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
					counter = counter - modifier;
				} else if (data[1].equals("set")
						&& CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
					counter = modifier;
				}
			} catch (ArrayIndexOutOfBoundsException e) {

			}
		}

		this.cooldown.startCooldown();
		sender.getUserCooldown().startCooldown();
		sender.getUserCommandCooldowns().get(this.command).startCooldown();

		formattedOutput = this.formatText(formattedOutput, channelHandler, sender);

		try {
			for (int i = counterStart; i <= this.param; i++) {
				formattedOutput = formattedOutput.replace("{param" + Integer.toString(i) + "}", data[i]);
			}
			if (!formattedOutput.equals("null")) {
				channelHandler.sendMessage(formattedOutput, this.channelOrigin);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			if (!this.helptext.equals("null")) {
				channelHandler.sendMessage(this.helptext, this.channelOrigin);
			}
			return "usage";
		}

		this.commandScript(sender, channelHandler, data);

		// write changes to db
		if (!sender.getUsername().equals("#readonly#")) {
			this.writeDBCommand();
		}

		return "OK";
	}

	public void update(ChannelHandler ch) {
		if (this.cmdtype.equals("timer") && ch.isLive()) {
			this.execCommand(new UserHandler("#internal#", this.channelOrigin), ch, new String[] {}, ch.getUserList());
		}
	}

	
	/**
	 * This function is used to set new command variables from chat.
	 * The following variables can be set:
	 * name
	 * param
	 * helptext
	 * access
	 * output
	 * cooldown
	 * cmdtype
	 * qsuffix
	 * qprefix
	 * qmodaccess
	 * cost
	 * lock
	 * texttrigger
	 * modpower
	 * viewerpower
	 * broadcasterpower
	 * botadminpower
	 * usercooldown
	 * appenddate
	 * appendgame
	 * 
	 * @param modType
	 * @param newValue
	 * @param sender
	 * @param userList
	 * @return
	 */
	public boolean editCommand(String modType, String newValue, UserHandler sender,
			HashMap<String, UserHandler> userList) {
		if (!CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
			return false;
		}

		boolean success = false;

		if (modType.equals("name")) {
			this.command = newValue;
			success = true;
		} else if (modType.equals("param")) {
			this.param = Integer.parseInt(newValue);
			success = true;
		} else if (modType.equals("helptext")) {
			this.helptext = newValue;
			success = true;
		} else if (modType.equals("access")) {
			this.access = newValue;
			success = true;
		} else if (modType.equals("output")) {
			this.unformattedOutput = newValue;
			success = true;
		} else if (modType.equals("cooldown")) {
			this.cooldown = new Cooldown(Integer.parseInt(newValue));
			success = true;
		} else if (modType.equals("cmdtype")) {
			this.cmdtype = newValue;
			success = true;
		} else if (modType.equals("qsuffix")) {
			this.quoteSuffix = newValue;
			success = true;
		} else if (modType.equals("qprefix")) {
			this.quotePrefix = newValue;
			success = true;
		} else if (modType.equals("qmodaccess")) {
			this.quoteModAccess = newValue;
			success = true;
		} else if (modType.equals("cost")) {
			this.pointCost = Double.parseDouble(newValue);
			success = true;
		} else if (modType.equals("lock")
				&& CommandHandler.checkPermission(sender.getUsername(), this.neededBroadcasterCommandPower, userList)) {
			this.locked = Boolean.parseBoolean(newValue);
			success = true;
		} else if (modType.equals("texttrigger")) {
			this.texttrigger = Boolean.parseBoolean(newValue);
			success = true;
		} else if (modType.equals("modpower")) {
			this.neededModCommandPower = Integer.parseInt(newValue);
			success = true;
		} else if (modType.equals("viewerpower")) {
			this.neededCommandPower = Integer.parseInt(newValue);
			success = true;
		} else if (modType.equals("broadcasterpower")) {
			this.neededBroadcasterCommandPower = Integer.parseInt(newValue);
			success = true;
		} else if (modType.equals("botadminpower")) {
			this.neededBotAdminCommandPower = Integer.parseInt(newValue);
			success = true;
		} else if (modType.equals("usercooldown")) {
			this.userCooldownLen = Integer.parseInt(newValue);
			success = true;
		} else if (modType.equals("appenddate")) {
			this.appendDateToQuote = Boolean.parseBoolean(newValue);
			success = true;
		} else if (modType.equals("appendgame")) {
			this.appendGameToQuote = Boolean.parseBoolean(newValue);
			success = true;
		} else if (modType.equals("script") && CommandHandler.checkPermission(sender.getUsername(), 75, userList)) {
			this.commandScript = newValue;
			success = true;
		}

		this.writeDBCommand();

		return success;
	}

	public void writeDBCommand() {
		if (!Memebot.useMongo) {
			return;
		}

		// System.out.printf("Saving data in db for channel %s\n",
		// this.command);
		log.info(String.format("Writing data for command %s to db", this.command));

		Document channelQuery = new Document("_id", this.command);

		Document channelData = new Document("_id", this.command).append("command", this.command)
				.append("cooldown", new Integer(this.cooldown.getCooldownLen())).append("access", this.access)
				.append("helptext", this.helptext).append("param", new Integer(this.param))
				.append("cmdtype", this.cmdtype).append("output", this.unformattedOutput)
				.append("qsuffix", this.quoteSuffix).append("qprefix", this.quotePrefix)
				.append("qmodaccess", this.quoteModAccess).append("costf", this.pointCost)
				.append("counter", this.counter).append("listcontent", this.listContent).append("locked", this.locked)
				.append("texttrigger", this.texttrigger).append("viewerpower", this.neededCommandPower)
				.append("modpower", this.neededModCommandPower)
				.append("broadcasterpower", this.neededBroadcasterCommandPower)
				.append("botadminpower", this.neededBotAdminCommandPower).append("usercooldown", this.userCooldownLen)
				.append("appendgame", this.appendGameToQuote).append("appenddate", this.appendDateToQuote)
				.append("script", this.commandScript);

		try {
			if (this.commandCollection.findOneAndReplace(channelQuery, channelData) == null) {
				this.commandCollection.insertOne(channelData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeDBCommand() {
		if (!Memebot.useMongo) {
			return;
		}

		Document channelQuery = new Document("_id", this.command);
		FindIterable<Document> cursor = this.commandCollection.find(channelQuery);

		Document channelData = cursor.first();
		this.commandCollection.deleteOne(channelData);
	}

	@SuppressWarnings("unchecked")
	public void readDBCommand() {
		if (!Memebot.useMongo) {
			return;
		}

		Document channelQuery = new Document("_id", this.command);
		FindIterable<Document> cursor = this.commandCollection.find(channelQuery);

		Document channelData = cursor.first();

		// read data
		if (channelData != null) {
			this.command = (String) channelData.getOrDefault("command", this.command);
			this.cooldown = new Cooldown(channelData.getInteger("cooldown", 2));
			this.access = (String) channelData.getOrDefault("access", this.access);
			this.helptext = (String) channelData.getOrDefault("helptext", this.helptext);
			this.param = channelData.getInteger("param", this.param);
			this.cmdtype = (String) channelData.getOrDefault("cmdtype", this.cmdtype);
			this.unformattedOutput = (String) channelData.getOrDefault("output", this.unformattedOutput);
			this.quoteSuffix = (String) channelData.getOrDefault("qsuffix", this.quoteSuffix);
			this.quotePrefix = (String) channelData.getOrDefault("qprefix", this.quotePrefix);
			this.quoteModAccess = (String) channelData.getOrDefault("qmodaccess", this.quoteModAccess);
			this.pointCost = (double) channelData.getOrDefault("costf", this.pointCost);
			this.counter = channelData.getInteger("counter", this.counter);
			this.listContent = (ArrayList<String>) channelData.getOrDefault("listcontent", this.listContent);
			this.locked = (boolean) channelData.getOrDefault("locked", this.locked);
			this.texttrigger = (boolean) channelData.getOrDefault("texttrigger", this.texttrigger);
			this.neededCommandPower = (int) channelData.getOrDefault("viewerpower", this.neededCommandPower);
			this.neededModCommandPower = (int) channelData.getOrDefault("modpower", this.neededModCommandPower);
			this.neededBroadcasterCommandPower = (int) channelData.getOrDefault("broadcasterpower",
					this.neededBroadcasterCommandPower);
			this.neededBotAdminCommandPower = (int) channelData.getOrDefault("botadminpower",
					this.neededBotAdminCommandPower);
			this.userCooldownLen = (int) channelData.getOrDefault("usercooldown", this.userCooldownLen);
			this.appendDateToQuote = (boolean) channelData.getOrDefault("appendgame", this.appendDateToQuote);
			this.appendGameToQuote = (boolean) channelData.getOrDefault("appenddate", this.appendGameToQuote);
			this.commandScript = (String) channelData.getOrDefault("script", this.commandScript);
		}
	}

	public static boolean checkPermission(String sender, int reqPermLevel, HashMap<String, UserHandler> userList) {
		for (String user : Memebot.botAdmins) {
			if (sender.equals(user)) {
				return true;
			}
		}

		if (!userList.containsKey(sender) && !sender.equals("#readonly#")) {
			return false;
		}

		if (userList.containsKey(sender)) {
			if (reqPermLevel <= userList.get(sender).getCommandPower()) {
				return true;
			}
		} else if (sender.equals("#readonly#")) {
			if (reqPermLevel <= 10) {
				return true;
			}
		}

		return false;
	}

	/***
	 * @deprecated This function is deprecated use {@link #checkPermission(String, int, HashMap)} instead.
	 * @param sender
	 * @param reqPermLevel
	 * @param userList
	 * @return
	 */
	@Deprecated
	public static boolean checkPermission(String sender, String reqPermLevel, HashMap<String, UserHandler> userList) {
		for (String user : Memebot.botAdmins) {
			if (sender.equals(user)) {
				return true;
			}
		}

		if (!userList.containsKey(sender) && !sender.equals("#readonly#")) {
			return false;
		}

		if (reqPermLevel.equals("moderators") && userList.get(sender).isMod()) {
			return true;
		} else if (reqPermLevel.equals("broadcaster") && userList.get(sender).isBroadcaster()) {
			return true;
		} else if (reqPermLevel.equals("viewers")) {
			return true;
		}

		return false;
	}
	
	/***
	 * This method formats text for output.
	 * The following parameters can be passed in:
	 * {sender}
	 * {counter}
	 * {debugsender}
	 * {debugch}
	 * {channelweb}
	 * {version}
	 * {appname}
	 * {date}
	 * {game}
	 * {curremote}
	 * {currname}
	 * @param formattedOutput
	 * @param channelHandler
	 * @param sender
	 * @return
	 */
	public String formatText(String formattedOutput, ChannelHandler channelHandler, UserHandler sender) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");// dd/MM/yyyy
		Calendar cal = Calendar.getInstance();
		String strDate = sdfDate.format(cal.getTime());
		
		formattedOutput = formattedOutput.replace("{sender}", sender.getUsername());
		formattedOutput = formattedOutput.replace("{counter}", Integer.toString(this.counter));
		formattedOutput = formattedOutput.replace("{points}", Double.toString(sender.getPoints()));
		formattedOutput = formattedOutput.replace("{debugsender}", sender.toString());
		formattedOutput = formattedOutput.replace("{debugch}", this.toString());
		formattedOutput = formattedOutput.replace("{channelweb}", channelHandler.getChannelPageURL());
		formattedOutput = formattedOutput.replace("{version}", BuildInfo.version);
		formattedOutput = formattedOutput.replace("{developer}", BuildInfo.dev);
		formattedOutput = formattedOutput.replace("{appname}", BuildInfo.appName);
		formattedOutput = formattedOutput.replace("{date}", strDate);
		formattedOutput = formattedOutput.replace("{game}", channelHandler.getCurrentGame());
		formattedOutput = formattedOutput.replace("{curremote}",
				channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE"));
		formattedOutput = formattedOutput.replace("{currname}",
				channelHandler.getBuiltInStrings().get("CURRENCY_NAME"));
		return formattedOutput;
	}
	
	protected boolean checkCost(UserHandler sender, double cost, ChannelHandler ch) {
		if (sender.getPoints() < this.pointCost
				&& !CommandHandler.checkPermission(sender.getUsername(), this.neededBotAdminCommandPower, ch.getUserList())
				&& this.pointCost > 0) {
			return true;
		}
		return false;
	}

	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		byte [] deocdedScriptBytes = Base64.getDecoder().decode(this.commandScript);
		
		log.info("Command script is: " + new String(deocdedScriptBytes));
	}

	public String getChannelOrigin() {
		return channelOrigin;
	}

	public void setChannelOrigin(String channelOrigin) {
		this.channelOrigin = channelOrigin;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public int getParam() {
		return param;
	}

	public void setParam(int param) {
		this.param = param;
	}

	public Cooldown getCooldown() {
		return cooldown;
	}

	public void setCooldown(Cooldown cooldown) {
		this.cooldown = cooldown;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getHelptext() {
		return helptext;
	}

	public void setHelptext(String helptext) {
		this.helptext = helptext;
	}

	public String getCmdtype() {
		return cmdtype;
	}

	public void setCmdtype(String cmdtype) {
		this.cmdtype = cmdtype;
	}

	public ArrayList<String> getListContent() {
		return listContent;
	}

	public void setListContent(ArrayList<String> listContent) {
		this.listContent = listContent;
	}

	public String getUnformattedOutput() {
		return unformattedOutput;
	}

	public void setUnformattedOutput(String unformattedOutput) {
		this.unformattedOutput = unformattedOutput;
	}

	public double getPointCost() {
		return pointCost;
	}

	public void setPointCost(double pointCost) {
		this.pointCost = pointCost;
	}

	public String getQuotePrefix() {
		return quotePrefix;
	}

	public void setQuotePrefix(String quotePrefix) {
		this.quotePrefix = quotePrefix;
	}

	public String getQuoteSuffix() {
		return quoteSuffix;
	}

	public void setQuoteSuffix(String quoteSuffix) {
		this.quoteSuffix = quoteSuffix;
	}

	public String getQuoteModAccess() {
		return quoteModAccess;
	}

	public void setQuoteModAccess(String quoteModAccess) {
		this.quoteModAccess = quoteModAccess;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public ArrayList<String> getAliases() {
		return aliases;
	}

	public void setAliases(ArrayList<String> aliases) {
		this.aliases = aliases;
	}

	public boolean isTexttrigger() {
		return texttrigger;
	}

	public void setTexttrigger(boolean texttrigger) {
		this.texttrigger = texttrigger;
	}

	public MongoCollection<Document> getCommandCollection() {
		return commandCollection;
	}

	public void setCommandCollection(MongoCollection<Document> commandCollection) {
		this.commandCollection = commandCollection;
	}

	public int getNeededCommandPower() {
		return neededCommandPower;
	}

	public void setNeededCommandPower(int neededCommandPower) {
		this.neededCommandPower = neededCommandPower;
	}

	public int getNeededModCommandPower() {
		return neededModCommandPower;
	}

	public void setNeededModCommandPower(int neededModCommandPower) {
		this.neededModCommandPower = neededModCommandPower;
	}

	public int getNeededBroadcasterCommandPower() {
		return neededBroadcasterCommandPower;
	}

	public void setNeededBroadcasterCommandPower(int neededBroadcasterCommandPower) {
		this.neededBroadcasterCommandPower = neededBroadcasterCommandPower;
	}

	public int getNeededBotAdminCommandPower() {
		return neededBotAdminCommandPower;
	}

	public void setNeededBotAdminCommandPower(int neededBotAdminCommandPower) {
		this.neededBotAdminCommandPower = neededBotAdminCommandPower;
	}

	public int getUserCooldownLen() {
		return userCooldownLen;
	}

	public void setUserCooldownLen(int userCooldownLen) {
		this.userCooldownLen = userCooldownLen;
	}

	public boolean isAppendGameToQuote() {
		return appendGameToQuote;
	}

	public void setAppendGameToQuote(boolean appendGameToQuote) {
		this.appendGameToQuote = appendGameToQuote;
	}

	public boolean isAppendDateToQuote() {
		return appendDateToQuote;
	}

	public void setAppendDateToQuote(boolean appendDateToQuote) {
		this.appendDateToQuote = appendDateToQuote;
	}

	public boolean isExcludeFromCommandList() {
		return excludeFromCommandList;
	}

	public void setExcludeFromCommandList(boolean excludeFromCommandList) {
		this.excludeFromCommandList = excludeFromCommandList;
	}

	public static Logger getLog() {
		return log;
	}

}
