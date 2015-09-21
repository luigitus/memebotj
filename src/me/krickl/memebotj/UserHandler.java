package me.krickl.memebotj;

import org.bson.Document;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class UserHandler {
	private boolean isMod = false;
	private boolean isBroadcaster = false;
	private boolean execCommands = true;
	private boolean newUser = false;
	private int commandPower = 10;
	private String username = "";
	private String channelOrigin = "";
	private double points = 0;
	//private boolean isJoined = false;
	private Cooldown userCooldown = new Cooldown(0);
	private String autogreet = "";
	private int timeouts = 0;
	private MongoCollection<Document> userCollection;

	public UserHandler(String username, String channel) {
		this.username = username;
		this.channelOrigin = channel;
		if (Memebot.useMongo) {
			this.userCollection = Memebot.db.getCollection(this.channelOrigin + "_users");
		}
		this.commandPower = 10;
		this.isMod = false;
		this.isBroadcaster = false;
		// this.loadUserData();
		this.readDBUserData();
	}

	public void writeDBUserData() {
		if (!Memebot.useMongo) {
			return;
		}

		// System.out.printf("Saving data in db for channel %s\n",
		// this.command);

		Document channelQuery = new Document("_id", this.username);

		Document channelData = new Document("_id", this.username).append("pointsf", this.points)
				.append("mod", this.isMod).append("autogreet", this.autogreet)
				.append("execcommands", this.execCommands)
				.append("commandpower", this.commandPower)
				.append("broadcaster", this.isBroadcaster)
				.append("timeouts", this.timeouts);

		try {
			if (this.userCollection.findOneAndReplace(channelQuery, channelData) == null) {
				this.userCollection.insertOne(channelData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readDBUserData() {
		if (!Memebot.useMongo) {
			return;
		}

		Document channelQuery = new Document("_id", this.username);
		FindIterable<Document> cursor = this.userCollection.find(channelQuery);

		Document channelData = cursor.first();

		// read data
		if (channelData != null) {
			this.isMod = channelData.getBoolean("vip", this.isMod);
			this.points = (double)channelData.getOrDefault("pointsf", this.points);
			this.autogreet = channelData.getOrDefault("autogreet", this.autogreet).toString();
			this.execCommands = (boolean)channelData.getOrDefault("execcommands", this.execCommands);
			this.commandPower = (int)channelData.getOrDefault("commandpower", this.commandPower);
			this.isBroadcaster = (boolean)channelData.getOrDefault("broadcaster", this.isBroadcaster);
			this.timeouts = (int)channelData.getOrDefault("timeouts", this.timeouts);
		} else {
			this.newUser = true;
		}
	}

	public void update() {
	}

	public boolean isMod() {
		return isMod;
	}

	public void setMod(boolean isMod) {
		this.isMod = isMod;
	}

	public boolean isBroadcaster() {
		return isBroadcaster;
	}

	public void setBroadcaster(boolean isBroadcaster) {
		this.isBroadcaster = isBroadcaster;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getChannelOrigin() {
		return channelOrigin;
	}

	public void setChannelOrigin(String channelOrigin) {
		this.channelOrigin = channelOrigin;
	}

	public double getPoints() {
		return points;
	}

	public void setPoints(double f) {
		this.points = f;
	}

	public Cooldown getUserCooldown() {
		return userCooldown;
	}

	public void setUserCooldown(Cooldown userCooldown) {
		this.userCooldown = userCooldown;
	}

	public String getAutogreet() {
		return autogreet;
	}

	public void setAutogreet(String autogreet) {
		this.autogreet = autogreet;
	}

	public boolean isExecCommands() {
		return execCommands;
	}

	public void setExecCommands(boolean execCommands) {
		this.execCommands = execCommands;
	}

	public MongoCollection<Document> getUserCollection() {
		return userCollection;
	}

	public void setUserCollection(MongoCollection<Document> userCollection) {
		this.userCollection = userCollection;
	}

	public boolean isNewUser() {
		return newUser;
	}

	public void setNewUser(boolean newUser) {
		this.newUser = newUser;
	}

	public int getCommandPower() {
		return commandPower;
	}

	public void setCommandPower(int commandPower) {
		this.commandPower = commandPower;
	}

	public int getTimeouts() {
		return timeouts;
	}

	public void setTimeouts(int timeouts) {
		this.timeouts = timeouts;
	}

}
