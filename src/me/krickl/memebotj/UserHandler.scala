package me.krickl.memebotj

import java.math.BigInteger
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.{Calendar, HashMap}
import java.util.logging.Logger

import org.bson.Document

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection

import scala.beans.BeanProperty

object UserHandler {
	final var log = Logger.getLogger(UserHandler.getClass.getName())
}

/**
 * This class handles users
 * @author unlink
 *
 */
class UserHandler(usernameNew: String, channelNew: String) {
	private var isModerator: Boolean = false
	private var isUserBroadcaster: Boolean = false
	//private boolean execCommands = true
	private var newUser: Boolean = false
	private var commandPower: Int = 10
	private var autoCommandPower: Int = 10
	private var customCommandPower: Int = 0
	private var username = usernameNew
	private var channelOrigin: String = channelNew
	private var points: Double = 0
	// private boolean isJoined = false
	private var userCooldown = new Cooldown(0)
	private var autogreet: String = ""
	private var timeouts: Int = 0
	private var userCollection: MongoCollection[Document] = null
	private var userCommandCooldowns: HashMap[String, Cooldown] = new HashMap[String, Cooldown]()
	private var modNote: String = ""
	private var random = new SecureRandom()
	private var privateKey = new BigInteger(130, random).toString(32)

  @BeanProperty
  var dateJoined = "null"
  var timeStampJoined = System.currentTimeMillis()

  @BeanProperty
  var enableAutogreets = true

	if (Memebot.useMongo) {
		this.userCollection = Memebot.db.getCollection(this.channelOrigin + "_users")
	}

	// this.loadUserData()
	readDBUserData()

  if(dateJoined == "null") {
    val sdfDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a")// dd/MM/yyyy
    val cal = Calendar.getInstance()
    this.dateJoined = sdfDate.format(cal.getTime)
  }

	setCommandPower(this.autoCommandPower)

	UserHandler.log.info(String.format("Private key for user %s is %s", this.username, this.privateKey))

	def writeDBUserData(): Unit = {
		if (!Memebot.useMongo) {
			return
		}

		// System.out.printf("Saving data in db for channel %s\n",
		// this.command)

		val channelQuery = new Document("_id", this.username)

		val channelData = new Document("_id", this.username).append("pointsf", this.points)
				.append("mod", this.isModerator).append("autogreet", this.autogreet)
				.append("ccommandpower", this.customCommandPower).append("broadcaster", this.isUserBroadcaster)
				.append("timeouts", this.timeouts)
				.append("privatekey", this.privateKey)
        .append("enableautogreet", this.enableAutogreets)
        .append("datejoined", this.dateJoined)
        .append("timeStampJoined", this.timeStampJoined)

		try {
			if (this.userCollection.findOneAndReplace(channelQuery, channelData) == null) {
				this.userCollection.insertOne(channelData)
			}
		} catch {
			case e: Exception =>
			e.printStackTrace()
		}
	}

	def readDBUserData(): Unit = {
		if (!Memebot.useMongo) {
			return
		}

		val channelQuery = new Document("_id", this.username)
		val cursor: FindIterable[Document] = this.userCollection.find(channelQuery)

		val channelData = cursor.first()

		// read data
		if (channelData != null) {
			this.isModerator = channelData.getBoolean("vip", this.isModerator)
			this.points = channelData.getOrDefault("pointsf", this.points.asInstanceOf[Object]).asInstanceOf[Double]
			this.autogreet = channelData.getOrDefault("autogreet", this.autogreet).toString()
			this.customCommandPower = channelData.getOrDefault("ccommandpower", this.customCommandPower.asInstanceOf[Object]).asInstanceOf[Int]
			this.isUserBroadcaster = channelData.getOrDefault("broadcaster", this.isBroadcaster.asInstanceOf[Object]).asInstanceOf[Boolean]
			this.timeouts = channelData.getOrDefault("timeouts", this.timeouts.asInstanceOf[Object]).asInstanceOf[Int]
			this.privateKey = channelData.getOrDefault("privatekey", this.privateKey.asInstanceOf[Object]).asInstanceOf[String]
      this.enableAutogreets = channelData.getOrDefault("enableautogreet", this.enableAutogreets.toString).toString.toBoolean
      this.dateJoined = channelData.getOrDefault("datejoined", this.dateJoined).toString
      this.timeStampJoined = channelData.getOrDefault("timeStampJoined", this.timeStampJoined.asInstanceOf[Object]).asInstanceOf[Long]
		} else {
			this.newUser = true
		}
	}

	def update() = {
	}

	def isMod(): Boolean = {
		return isModerator
	}

	def setMod(isMod: Boolean) {
		this.isModerator = isMod
	}

	def isBroadcaster(): Boolean = {
		return this.isUserBroadcaster
	}

	def setBroadcaster(isBroadcaster: Boolean) = {
		this.isUserBroadcaster = isBroadcaster
	}

	def getUsername(): String = {
		return username
	}

	def setUsername(username: String) = {
		this.username = username
	}

	def getChannelOrigin(): String = {
		return channelOrigin
	}

	def setChannelOrigin(channelOrigin: String) = {
		this.channelOrigin = channelOrigin
	}

	def getPoints(): Double = {
		return points
	}

	def setPoints(f: Double) {
		this.points = f
    if(this.points < 0) {
      this.points = 0
    }
	}

	def getUserCooldown(): Cooldown = {
		return userCooldown
	}

	def setUserCooldown(userCooldown: Cooldown) = {
		this.userCooldown = userCooldown
	}

	def getAutogreet(): String = {
		return autogreet
	}

	def setAutogreet(autogreet: String) = {
		this.autogreet = autogreet
	}

	def getUserCollection(): MongoCollection[Document] = {
		return userCollection
	}

	def setUserCollection(userCollection: MongoCollection[Document]) = {
		this.userCollection = userCollection
	}

	def isNewUser(): Boolean = {
		return newUser
	}

	def setNewUser(newUser: Boolean) = {
		this.newUser = newUser
	}

	def getCommandPower(): Int = {
		return commandPower
	}

	def setCommandPower(commandPower: Int) = {
		this.autoCommandPower = commandPower
		this.commandPower = commandPower + this.customCommandPower
	}

	def getTimeouts(): Int = {
		return timeouts
	}

	def setTimeouts(timeouts: Int) = {
		this.timeouts = timeouts
	}

	def getCustomCommandPower(): Int = {
		return customCommandPower
	}

	def setCustomCommandPower(customCommandPower: Int) = {
		this.customCommandPower = customCommandPower
	}

	def getAutoCommandPower(): Int = {
		return autoCommandPower
	}

	def setAutoCommandPower(autoCommandPower: Int) = {
		this.autoCommandPower = autoCommandPower
	}

	def getUserCommandCooldowns(): HashMap[String, Cooldown] = {
		return userCommandCooldowns
	}

	def setUserCommandCooldowns(userCooldowns: HashMap[String, Cooldown]) = {
		this.userCommandCooldowns = userCooldowns
	}

	def getModNote(): String = {
		return modNote
	}

	def setModNote(modNote: String) = {
		this.modNote = modNote
	}

	def getPrivateKey(): String = {
		return privateKey
	}

	def setPrivateKey(privateKey: String) = {
		this.privateKey = privateKey
	}

	def getRandom(): SecureRandom = {
		return random
	}

	def setRandom(random: SecureRandom) = {
		this.random = random
	}
}
