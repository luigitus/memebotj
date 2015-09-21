package me.krickl.memebotj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

import org.bson.Document;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

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

	private MongoCollection<Document> commandCollection;

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
		if (!this.cooldown.canContinue() || (!sender.getUserCooldown().canContinue() && !sender.isMod())) {
			return "cooldown";
		}

		if (!CommandHandler.checkPermission(sender.getUsername(), this.neededCommandPower, userList)) {
			return "denied";
		}
		if (sender.getPoints() < this.pointCost && !CommandHandler.checkPermission(sender.getUsername(), this.neededBotAdminCommandPower, userList) && this.pointCost > 0) {
			channelHandler.sendMessage(String.format("Sorry, you don't have %f points", this.pointCost) , this.channelOrigin);
			return "cost";
		}

		sender.setPoints(sender.getPoints() - this.pointCost);
		
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
				} else {
					try {
						formattedOutput = this.quotePrefix.replace("{number}", data[1])
								+ this.listContent.get(Integer.parseInt(data[1])) + this.quoteSuffix.replace("{number}", data[1]);
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
					formattedOutput = this.quotePrefix.replace("{number}", Integer.toString(i)) + this.listContent.get(i)
							+ this.quoteSuffix.replace("{number}", Integer.toString(i));
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

		formattedOutput = formattedOutput.replace("{sender}", sender.getUsername());
		formattedOutput = formattedOutput.replace("{counter}", Integer.toString(this.counter));
		formattedOutput = formattedOutput.replace("{points}", Double.toString(sender.getPoints()));
		formattedOutput = formattedOutput.replace("{debugsender}", sender.toString());
		formattedOutput = formattedOutput.replace("{debugch}", this.toString());
		formattedOutput = formattedOutput.replace("{channelweb}", channelHandler.getChannelPageURL());
		formattedOutput = formattedOutput.replace("{version}", BuildInfo.version);
		formattedOutput = formattedOutput.replace("{developer}", BuildInfo.dev);
		formattedOutput = formattedOutput.replace("{appname}", BuildInfo.appName);

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

		//write changes to db
		if(!sender.getUsername().equals("#readonly#")) {
			this.writeDBCommand();
		}
		
		return "OK";
	}

	public void update(ChannelHandler ch) {
		if (this.cmdtype.equals("timer")) {
			this.execCommand(new UserHandler("#internal#", this.channelOrigin), ch, new String[] {}, ch.getUserList());
		}
	}

	public boolean editCommand(String modType, String newValue, UserHandler sender, HashMap<String, UserHandler> userList) {
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
		} else if (modType.equals("lock") && CommandHandler.checkPermission(sender.getUsername(), this.neededBroadcasterCommandPower, userList) ) {
			this.locked = Boolean.parseBoolean(newValue);
			success = true;
		} else if (modType.equals("texttrigger")) {
			this.texttrigger = Boolean.parseBoolean(newValue);
			success = true;
		} else if (modType.equals("modpower")) {
			this.neededModCommandPower = Integer.parseInt(newValue);
		} else if (modType.equals("viewerpower")) {
			this.neededCommandPower = Integer.parseInt(newValue);
		} else if (modType.equals("broadcasterpower")) {
			this.neededBroadcasterCommandPower = Integer.parseInt(newValue);
		} else if (modType.equals("botadminpower")) {
			this.neededBotAdminCommandPower = Integer.parseInt(newValue);
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
				.append("counter", this.counter).append("listcontent", this.listContent)
				.append("locked", this.locked)
				.append("texttrigger", this.texttrigger)
				.append("viewerpower", this.neededCommandPower)
				.append("modpower", this.neededModCommandPower)
				.append("broadcasterpower", this.neededBroadcasterCommandPower)
				.append("botadminpower", this.neededBotAdminCommandPower);

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
			this.pointCost = (double)channelData.getOrDefault("costf", this.pointCost);
			this.counter = channelData.getInteger("counter", this.counter);
			this.listContent = (ArrayList<String>) channelData.getOrDefault("listcontent", this.listContent);
			this.locked = (boolean)channelData.getOrDefault("locked", this.locked);
			this.texttrigger = (boolean)channelData.getOrDefault("texttrigger", this.texttrigger);
			this.neededCommandPower = (int)channelData.getOrDefault("viewerpower", this.neededCommandPower);
			this.neededModCommandPower = (int)channelData.getOrDefault("modpower", this.neededModCommandPower);
			this.neededBroadcasterCommandPower = (int)channelData.getOrDefault("broadcasterpower", this.neededBroadcasterCommandPower);
			this.neededBotAdminCommandPower = (int)channelData.getOrDefault("botadminpower", this.neededBotAdminCommandPower);
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

		if ( reqPermLevel <= userList.get(sender).getCommandPower()) {
			return true;
		}
		
		return false;
	}
	
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
	
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {

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

}
