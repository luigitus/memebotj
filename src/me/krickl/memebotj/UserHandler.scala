package me.krickl.memebotj

import java.math.BigInteger
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, HashMap}
import java.util.logging.Logger

import org.bson.Document

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection

import scala.beans.BeanProperty

//remove if not needed
import scala.collection.JavaConversions._
import scala.util.control.Breaks._

object UserHandler {
	final var log = Logger.getLogger(UserHandler.getClass.getName)
}

/**
 * This class handles users
 * @author unlink
 *
 */
class UserHandler(usernameNew: String, channelNew: String) {
  @BeanProperty
	var isModerator: Boolean = false
  @BeanProperty
	var isUserBroadcaster: Boolean = false
	//private boolean execCommands = true
  @BeanProperty
	var newUser: Boolean = false
	@BeanProperty
  var nickname = ""
	var _commandPower : Int = 10
	var _autoCommandPower : Int = 10
  @BeanProperty
	var customCommandPower: Int = 0
  @BeanProperty
  var username = usernameNew
  @BeanProperty
	var channelOrigin: String = channelNew
	private var _points: Double = 0.0f

  def points = _points
	// private boolean isJoined = false
  @BeanProperty
	var userCooldown = new Cooldown(0)
  @BeanProperty
	var autogreet: String = ""
  @BeanProperty
	var timeouts: Int = 0
  @BeanProperty
	var userCollection: MongoCollection[Document] = null
  @BeanProperty
	var userCommandCooldowns: java.util.HashMap[String, Cooldown] = new java.util.HashMap[String, Cooldown]()
  @BeanProperty
	var modNote: String = ""
  @BeanProperty
	var random = new SecureRandom()
  @BeanProperty
	var privateKey = new BigInteger(130, random).toString(32)

  @BeanProperty
  var dateJoined = "null"
	@BeanProperty
  var timeStampJoined = System.currentTimeMillis()

  @BeanProperty
  var enableAutogreets = true

  //this will be used for spam prevention
  @BeanProperty
	var lastMessages = Array.ofDim[String](10)

	var timeSinceActivity = System.currentTimeMillis()

  var walletSize: Double = -1

	if (Memebot.useMongo) {
		this.userCollection = Memebot.db.getCollection(this.channelOrigin + "_users")
	}

	readDBUserData()

  if(dateJoined == "null") {
    val sdfDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a")// dd/MM/yyyy
    val cal = Calendar.getInstance()
    this.dateJoined = sdfDate.format(cal.getTime)
  }

	setCommandPower(this._autoCommandPower)

	UserHandler.log.info(String.format("Private key for user %s is %s", this.username, this.privateKey))

	def writeDBUserData(): Unit = {
		if (!Memebot.useMongo) {
			return
		}

		val channelQuery = new Document("_id", this.username)

		val channelData = new Document("_id", this.username).append("pointsf", this._points)
				.append("mod", this.isModerator).append("autogreet", this.autogreet)
				.append("ccommandpower", this.customCommandPower).append("broadcaster", this.isUserBroadcaster)
				.append("timeouts", this.timeouts)
				.append("privatekey", this.privateKey)
        .append("enableautogreet", this.enableAutogreets)
        .append("datejoined", this.dateJoined)
        .append("timeStampJoined", this.timeStampJoined)
        .append("nickname", this.nickname)
        .append("wallet", this.walletSize)

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
			this.isModerator = channelData.getBoolean("mod", this.isModerator)
			this._points = channelData.getOrDefault("pointsf", this._points.asInstanceOf[Object]).asInstanceOf[Double]
			this.autogreet = channelData.getOrDefault("autogreet", this.autogreet).toString
			this.customCommandPower = channelData.getOrDefault("ccommandpower", this.customCommandPower.asInstanceOf[Object]).asInstanceOf[Int]
			this.isUserBroadcaster = channelData.getOrDefault("broadcaster", this.isUserBroadcaster.asInstanceOf[Object]).asInstanceOf[Boolean]
			this.timeouts = channelData.getOrDefault("timeouts", this.timeouts.asInstanceOf[Object]).asInstanceOf[Int]
			this.privateKey = channelData.getOrDefault("privatekey", this.privateKey.asInstanceOf[Object]).asInstanceOf[String]
      this.enableAutogreets = channelData.getOrDefault("enableautogreet", this.enableAutogreets.toString).toString.toBoolean
      this.dateJoined = channelData.getOrDefault("datejoined", this.dateJoined).toString
      this.timeStampJoined = channelData.getOrDefault("timeStampJoined", this.timeStampJoined.asInstanceOf[Object]).asInstanceOf[Long]
      this.nickname = channelData.getOrDefault("nickname", this.nickname.asInstanceOf[Object]).asInstanceOf[String]
      this.walletSize = channelData.getOrDefault("wallet", this.walletSize.asInstanceOf[Object]).asInstanceOf[Double]
		} else {
			this.newUser = true
		}
	}

	def update() = {
	}

  def setPoints(f: Double): Boolean = {
    this.points = f
  }

	def points_=(f: Double): Boolean = {
		for(ch <- Memebot.joinedChannels) {
			if (ch.channel == this.channelOrigin) {
				if(this._points + f > ch.maxPoints || (this._points + f > this.walletSize && this.walletSize > 0)) {
					//this.points = ch.maxPoints
					return false
				}
			}
		}

		this._points = f
    if(this._points < 0) {
      this._points = 0
    }

    true
	}

  def setCommandPower(commandPower: Int) = {
    this.commandPower_=(commandPower)
  }

	def commandPower_=(commandPower: Int) = {
		this._autoCommandPower = commandPower
		this._commandPower = commandPower + this.customCommandPower
	}

  def setAutoCommandPower(autoCommandPower: Int) = {
    this.autoCommandPower_=(autoCommandPower)
  }

	def autoCommandPower_=(autoCommandPower: Int) = {
		this._autoCommandPower = autoCommandPower
	}

  def screenName: String = {
    if(this.nickname.isEmpty) {
      return username
    }
    this.nickname
  }

  def canRemove: Boolean = {
    // remove user after 1 hour (0x36EE80 milliseconds) of inactivity
    val tmpUserList = new util.HashMap[String, UserHandler]()
    tmpUserList.put(this.username, this)
    if ((System.currentTimeMillis() - this.timeSinceActivity) > 0x36EE80 * 0x2 && username != "#internal#" && username != "#readonly#" && !CommandHandler.checkPermission(this.username, 50, tmpUserList)) {
      UserHandler.log.info(f"Removed user ${this.username} for inactivity ${this.timeSinceActivity}")
      return true
    }

    false
  }
}
