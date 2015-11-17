package me.krickl.memebotj

import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util
import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap
import java.util.Random
import java.util.logging.Logger

import org.bson.Document

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection

import scala.beans.BeanProperty

//remove if not needed
import scala.collection.JavaConversions._

object CommandHandler {
	final val log = Logger.getLogger(CommandHandler.getClass.getName)

	def checkPermission(sender: String, reqPermLevel: Int, userList: HashMap[String, UserHandler]): Boolean = {
		val it = Memebot.botAdmins.iterator()
		while (it.hasNext) {
			val user = it.next()
			if (sender.equals(user)) {
				return true
			}
		}

		if (!userList.containsKey(sender) && !sender.equals("#readonly#")) {
			return false
		}

		if (userList.containsKey(sender)) {
			if (reqPermLevel <= userList.get(sender).getCommandPower()) {
				return true
			}
		} else if (sender.equals("#readonly#")) {
			if (reqPermLevel <= 10) {
				return true
			}
		}

		return false
	}
}

/***
 * This class is the base class for all commands.
 *
 * @author unlink
 *
 */
class CommandHandler(channel: String, commandName: String = "null", dbprefix: String = null) {

	var channelOrigin: String = channel
	var command: String = commandName
	var param: Int = 0
	var pointCost: Double = 0
	var cooldown = new Cooldown(2)
	var access: String = "viewers"
	var helptext: String = "null"
	var cmdtype = "default"
	var listContent = new ArrayList[String]()
	var unformattedOutput = "null"
	var quotePrefix = "#{number}: "
	var quoteSuffix = ""
	var quoteModAccess = "moderators"
	var counter: Int = 0
	var aliases = new ArrayList[String]()
	var locked = false
	var texttrigger = false

	var neededCommandPower = 10
	var neededModCommandPower = 25
	var neededBroadcasterCommandPower = 50
	var neededBotAdminCommandPower = 75
	var neededCooldownBypassPower = 50
	var neededAddPower = 25
	var allowPicksFromList = true
	var removeFromListOnPickIfMod = false

	var userCooldownLen = 0
	var appendGameToQuote = false
	var appendDateToQuote = false
	var appendSenderToQuote = false
	var appendToQuoteString = ""

	var excludeFromCommandList = false
	var enable = true
	var overrideHandleMessage = false

  @BeanProperty
  var caseSensitive = true

	@BeanProperty
	var execCounter = 0

	@BeanProperty
	var listregex: String = ""

  @BeanProperty
  var success = false

	@BeanProperty
  var otherData = new util.HashMap[String, String]()

	private var commandCollection: MongoCollection[Document] = null
	private var commandScript: String = ""

	if (Memebot.useMongo) {
		if (dbprefix == null) {
			this.commandCollection = Memebot.db.getCollection(this.channelOrigin + "_commands")
		} else {
			this.commandCollection = Memebot.db.getCollection(dbprefix + this.channelOrigin + "_commands")
		}
	}

	this.readDBCommand()
	
	def executeCommand(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String], userList: HashMap[String, UserHandler]): String = {
    this.success = true


    if(this.overrideHandleMessage) {
      return "override"
    }

		if(!enable) {
			return "disabled"
		}

		// check global cooldown
		if ((!this.cooldown.canContinue() || !sender.getUserCooldown().canContinue()) && !CommandHandler.checkPermission(sender.getUsername(), this.neededCooldownBypassPower, userList) ) {
			return "cooldown"
		}

		// check user cooldown
		if (!sender.getUserCommandCooldowns().containsKey(this.command)) {
			sender.getUserCommandCooldowns().put(this.command, new Cooldown(this.userCooldownLen))
		} else {
			if (sender.getUserCommandCooldowns().get(this.command).getCooldownLen() != this.userCooldownLen) {
				sender.getUserCommandCooldowns().get(this.command).setCooldownLen(this.userCooldownLen)
			}
		}

		if (!sender.getUserCommandCooldowns().get(this.command).canContinue()
				&& !CommandHandler.checkPermission(sender.getUsername(), this.neededCooldownBypassPower, userList)) {
			return "usercooldown"
		}

		if (!CommandHandler.checkPermission(sender.getUsername(), this.neededCommandPower, userList)) {
			return "denied"
		}
		if(!this.checkCost(sender, this.pointCost, channelHandler)){
			channelHandler.sendMessage(f"Sorry, you don't have ${this.pointCost.toFloat} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}", this.channelOrigin)
			return "cost"
		}

    val sdfDate = new SimpleDateFormat("yyyy-MM-dd")// dd/MM/yyyy
    val cal = Calendar.getInstance()
    val strDate = sdfDate.format(cal.getTime)

		var formattedOutput = this.unformattedOutput
    val counterStart = 1
		//var success = true

		if (this.cmdtype.equals("list")) {
			try {
				if (data(1).equals("add") && CommandHandler.checkPermission(sender.getUsername(), this.neededAddPower, userList)) {
					var newEntry = ""
					for (i <- 2 to data.length - 1) {
						newEntry = newEntry + " " + data(i)
					}
					if (!newEntry.isEmpty)
					{
						if (this.appendDateToQuote) {
							newEntry = newEntry + " <" + strDate + ">"
						}
						if (this.appendGameToQuote) {
							newEntry = newEntry + " <" + channelHandler.getCurrentGame + ">"
						}

						this.listContent.add(newEntry + " " + this.formatText(this.appendToQuoteString, channelHandler, sender))
						formattedOutput = "Added "
					} else {
						formattedOutput = "Not added"
						this.success = false
					}
				} else if (data(1).equals("remove") && CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
					try {
						this.listContent.remove(Integer.parseInt(data(2)))
						formattedOutput = "Removed"
					} catch {
						case e: IndexOutOfBoundsException => {
							formattedOutput = e.toString
						}
					}
				} else if (data(1).equals("edit") && CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
					var newEntry = ""
					for(i <- 3 to data.length - 1) {
						newEntry = newEntry + " " + data(i)
					}

					this.listContent.set(Integer.parseInt(data(2)), newEntry)
					formattedOutput = "Edited"
				} else if (data(1).equals("list")) {
					formattedOutput = "List: " + channelHandler.getChannelPageBaseURL + "/" + URLEncoder.encode(this.command, "UTF-8") + ".html"
					this.success = false
				} else if(allowPicksFromList) {
					try {
						formattedOutput = this.quotePrefix.replace("{number}", data(1)) + this.listContent.get(Integer.parseInt(data(1))) + this.quoteSuffix.replace("{number}", data(1))
						if(this.removeFromListOnPickIfMod && CommandHandler.checkPermission(sender.getUsername(), this.getNeededBroadcasterCommandPower(), userList)) {
							this.listContent.remove(Integer.parseInt(data(1)))
						}
					} catch {
						case e: NumberFormatException => {
							formattedOutput = "That's not an integer"
						}
						case e: IndexOutOfBoundsException => {
							formattedOutput = "Index out of bounds: Size " + Integer.toString(this.listContent.size())
						}
					}
				} else {
					this.success= false
				}
			} catch {
				case e: ArrayIndexOutOfBoundsException => {
					try {
						val rand = new Random()
						val i = rand.nextInt(this.listContent.size())
						formattedOutput = this.quotePrefix.replace("{number}", Integer.toString(i)) + this.listContent.get(i) + this.quoteSuffix.replace("{number}", Integer.toString(i))

						if(this.removeFromListOnPickIfMod && CommandHandler.checkPermission(sender.getUsername(), this.getNeededBroadcasterCommandPower(), userList)) {
							this.listContent.remove(i)
						}
					} catch {
            case e: IllegalArgumentException => {
              e.printStackTrace()
            }
          } finally {
						// just ignore it
					}
				}
			}
		} else if (this.cmdtype.equals("counter")) {
			var modifier = 1
			try {
				modifier = Integer.parseInt(data(2))
			} catch {
        case e: ArrayIndexOutOfBoundsException => {
          e.printStackTrace()
        }
      }

			try {
				if (data(1).equals("add")
						&& CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
					counter = counter + modifier
				} else if (data(1).equals("sub")
						&& CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
					counter = counter - modifier
				} else if (data(1).equals("set")
						&& CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
					counter = modifier
				}
			} catch {
        case e: ArrayIndexOutOfBoundsException => {
          e.printStackTrace()
        }
      }
		}

		formattedOutput = this.formatText(formattedOutput, channelHandler, sender)

		try {
			for(i <- counterStart to this.param) {
				formattedOutput = formattedOutput.replace("{param" + Integer.toString(i) + "}", data(i))
			}
			if (!formattedOutput.equals("null")) {
				channelHandler.sendMessage(formattedOutput, this.channelOrigin, sender)
			}
		} catch {
			case e: ArrayIndexOutOfBoundsException => {
				if (!this.helptext.equals("null")) {
					channelHandler.sendMessage(this.helptext, this.channelOrigin)
				}
				return "usage"
			}
		}

		this.commandScript(sender, channelHandler, data)

		// write changes to db
		if (!sender.getUsername().equals("#readonly#")) {
			this.writeDBCommand()
		}

		// send information to api
		if(!channelHandler.getApiConnectionIP.equals("")) {
			Memebot.apiConnection.sendData("pkey=apisourcesender=" + this.command + "request=commandmessage=Command executed", channelHandler.getApiConnectionIP(), Memebot.apiport, channelHandler)
		}

    this.execCounter = this.execCounter + 1

		return "OK"
	}

	def update(ch: ChannelHandler) {
		if (this.cmdtype.equals("timer") && ch.isLive) {
			val newArray = new Array[String](0)
			this.executeCommand(new UserHandler("#internal#", this.channelOrigin), ch, newArray, ch.getUserList())
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
	def editCommand(modType: String, newValue: String, sender: UserHandler, userList: HashMap[String, UserHandler]): Boolean = {
		if (!CommandHandler.checkPermission(sender.getUsername(), this.neededModCommandPower, userList)) {
			return false
		}

    success = false
		try {
			if (modType.equals("name")) {
        this.removeDBCommand()
				this.command = newValue
        this.writeDBCommand()
				success = true
			} else if (modType.equals("param")) {
				this.param = Integer.parseInt(newValue)
				success = true
			} else if (modType.equals("helptext")) {
				this.helptext = newValue
				success = true
			} else if (modType.equals("access")) {
				this.access = newValue
				success = true
			} else if (modType.equals("output")) {
        if(newValue == "{none}") {
          this.unformattedOutput = ""
        } else {
          this.unformattedOutput = newValue
        }
				success = true
			} else if (modType.equals("cooldown")) {
				this.cooldown = new Cooldown(Integer.parseInt(newValue))
				success = true
			} else if (modType.equals("cmdtype")) {
				this.cmdtype = newValue
				success = true
			} else if (modType.equals("qsuffix")) {
				this.quoteSuffix = newValue
				success = true
			} else if (modType.equals("qprefix")) {
				this.quotePrefix = newValue
				success = true
			} else if (modType.equals("qmodaccess")) {
				this.quoteModAccess = newValue
				success = true
			} else if (modType.equals("cost")) {
				this.pointCost = newValue.toDouble
				success = true
			} else if (modType.equals("lock") && CommandHandler.checkPermission(sender.getUsername(), this.neededBroadcasterCommandPower, userList)) {
				this.locked = newValue.toBoolean
				success = true
			} else if (modType.equals("texttrigger")) {
				this.texttrigger = newValue.toBoolean
				success = true
			} else if (modType.equals("modpower")) {
				this.neededModCommandPower = Integer.parseInt(newValue)
				success = true
			} else if (modType.equals("viewerpower")) {
				this.neededCommandPower = Integer.parseInt(newValue)
				success = true
			} else if (modType.equals("broadcasterpower")) {
				this.neededBroadcasterCommandPower = Integer.parseInt(newValue)
				success = true
			} else if (modType.equals("botadminpower")) {
				this.neededBotAdminCommandPower = Integer.parseInt(newValue)
				success = true
			} else if (modType.equals("usercooldown")) {
				this.userCooldownLen = Integer.parseInt(newValue)
				success = true
			} else if (modType.equals("appenddate")) {
				this.appendDateToQuote = newValue.toBoolean
				success = true
			} else if (modType.equals("appendgame")) {
				this.appendGameToQuote = newValue.toBoolean
				success = true
			} else if (modType.equals("script") && CommandHandler.checkPermission(sender.getUsername(), 75, userList)) {
				this.commandScript = newValue
				success = true
			} else if (modType.equals("enable")) {
				this.enable = newValue.toBoolean
				success = true
			} else if(modType.equals("allowpick")) {
				allowPicksFromList = newValue.toBoolean
				success = true
			} else if(modType.equals("cooldownbypasspower")) {
				this.neededCooldownBypassPower = Integer.parseInt(newValue)
				success = true
			} else if(modType.equals("neededAddPower")) {
				this.neededAddPower = Integer.parseInt(newValue)
				success = true
			} else if(modType.equals("autoremove")) {
				this.removeFromListOnPickIfMod = newValue.toBoolean
				success = true
			} else if(modType.equals("appendsender")) {
				this.appendSenderToQuote = true
				success = true
			} else if(modType == "appendtoquote") {
				this.appendToQuoteString = newValue
				success = true
			} else if(modType == "overridehandlemessage") {
				this.overrideHandleMessage = newValue.toBoolean
				success = true
			} else if(modType == "listregex") {
        this.listregex = newValue
        success = true
      } else if(modType == "case") {
        this.caseSensitive = newValue.toBoolean
        success = true
      }
		} catch {
			case e: NumberFormatException => {
				CommandHandler.log.warning(String.format("Screw you Luigitus: %s", e.toString))
			}
		}
		this.writeDBCommand()

		return success
	}

	def writeDBCommand() {
		if (!Memebot.useMongo) {
			return
		}

		// System.out.printf("Saving data in db for channel %s\n",
		// this.command)
		CommandHandler.log.info(String.format("Writing data for command %s to db", this.command))

    val channelQuery = new Document("_id", this.command)

    val otherDataDocument = new Document()
    for (key <- this.otherData.keySet()) {
      otherDataDocument.append(key, this.otherData.get(key))
    }

    val channelData = new Document("_id", this.command).append("command", this.command)
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
				.append("script", this.commandScript)
				.append("enable", this.enable)
				.append("cooldownbypasspower", this.neededCooldownBypassPower)
				.append("allowpick", this.allowPicksFromList)
				.append("addpower", this.neededAddPower)
				.append("autoremove", this.removeFromListOnPickIfMod)
				.append("appendsender", this.appendSenderToQuote)
				.append("appendtoquote", this.appendToQuoteString)
				.append("overridehandlemessage", this.overrideHandleMessage)
        .append("execcounter", this.execCounter)
        .append("listregex", this.listregex)
        .append("case", this.caseSensitive)
        .append("otherdata", otherDataDocument)

		try {
			if (this.commandCollection.findOneAndReplace(channelQuery, channelData) == null) {
				this.commandCollection.insertOne(channelData)
			}
		} catch {
			case e: Exception => {
				e.printStackTrace()
			}
		}
	}

	def removeDBCommand() {
		if (!Memebot.useMongo) {
			return
		}
		try {
      val channelQuery = new Document("_id", this.command)
      val cursor: FindIterable[Document] = this.commandCollection.find(channelQuery)

      val channelData = cursor.first()
      this.commandCollection.deleteOne(channelData)
    } catch {
      case e: java.lang.IllegalArgumentException => {
        e.printStackTrace()
      }
    }
	}

	def readDBCommand() {
		if (!Memebot.useMongo) {
			return
		}

    val channelQuery = new Document("_id", this.command)
    val cursor = this.commandCollection.find(channelQuery)

    val channelData = cursor.first()

		// read data
		if (channelData != null) {
			this.command = channelData.getOrDefault("command", this.command).toString
			this.cooldown = new Cooldown(channelData.getInteger("cooldown", 2))
			this.access = channelData.getOrDefault("access", this.access).toString
			this.helptext = channelData.getOrDefault("helptext", this.helptext).toString
			this.param = channelData.getInteger("param", this.param)
			this.cmdtype = channelData.getOrDefault("cmdtype", this.cmdtype).toString
			this.unformattedOutput = channelData.getOrDefault("output", this.unformattedOutput).toString
			this.quoteSuffix = channelData.getOrDefault("qsuffix", this.quoteSuffix).toString
			this.quotePrefix = channelData.getOrDefault("qprefix", this.quotePrefix).toString
			this.quoteModAccess = channelData.getOrDefault("qmodaccess", this.quoteModAccess).toString
			this.pointCost = channelData.getOrDefault("costf", this.pointCost.toString).toString.toDouble
			this.counter = channelData.getInteger("counter", this.counter)
			this.listContent = channelData.getOrDefault("listcontent", this.listContent).asInstanceOf[ArrayList[String]]
			this.locked = channelData.getOrDefault("locked", this.locked.toString).toString.toBoolean
			this.texttrigger = channelData.getOrDefault("texttrigger", this.texttrigger.toString).toString.toBoolean
			this.neededCommandPower = channelData.getOrDefault("viewerpower", this.neededCommandPower.toString).toString.toInt
			this.neededModCommandPower = channelData.getOrDefault("modpower", this.neededModCommandPower.toString).toString.toInt
			this.neededBroadcasterCommandPower = channelData.getOrDefault("broadcasterpower", this.neededBroadcasterCommandPower.toString()).toString().toInt
			this.neededBotAdminCommandPower = channelData.getOrDefault("botadminpower", this.neededBotAdminCommandPower.toString()).toString().toInt
			this.userCooldownLen = channelData.getOrDefault("usercooldown", this.userCooldownLen.toString()).toString().toInt
			this.appendDateToQuote = channelData.getOrDefault("appendgame", this.appendDateToQuote.toString()).toString().toBoolean
			this.appendGameToQuote = channelData.getOrDefault("appenddate", this.appendGameToQuote.toString()).toString().toBoolean
			this.commandScript = channelData.getOrDefault("script", this.commandScript.toString()).toString()
			this.enable = channelData.getOrDefault("enable", this.enable.toString()).toString().toBoolean
			this.neededCooldownBypassPower = channelData.getOrDefault("cooldownbypass", this.neededCooldownBypassPower.toString()).toString().toInt
			this.allowPicksFromList = channelData.getOrDefault("allowpick", this.allowPicksFromList.toString()).toString().toBoolean
			this.neededAddPower = channelData.getOrDefault("addpower", this.neededAddPower.toString()).toString().toInt
			this.removeFromListOnPickIfMod = channelData.getOrDefault("autoremove", this.removeFromListOnPickIfMod.toString()).toString().toBoolean
			this.appendSenderToQuote = channelData.getOrDefault("appendsender", this.appendSenderToQuote.toString).toString().toBoolean
			this.appendToQuoteString = channelData.getOrDefault("appendtoquote", this.appendToQuoteString).toString
			this.overrideHandleMessage = channelData.getOrDefault("overridehandlemessage", this.overrideHandleMessage.toString).toString.toBoolean
      this.execCounter = channelData.getOrDefault("execcounter", this.execCounter.toString).toString.toInt
      this.listregex = channelData.getOrDefault("listregex", this.listregex).toString
      this.caseSensitive = channelData.getOrDefault("case", this.caseSensitive.toString).toString.toBoolean
      //otherdata are used to store data that are used for internal commands
      val otherDataDocument = channelData.getOrDefault("otherdata", new Document()).asInstanceOf[Document]

      for (key <- otherDataDocument.keySet()) {
        this.otherData.put(key, otherDataDocument.getString(key))
      }
		}
	}

  @Deprecated
	def formatText(fo: String, channelHandler: ChannelHandler, sender: UserHandler): String = {
    return Memebot.formatText(fo, channelHandler, sender, this)
	}

	protected def checkCost(sender: UserHandler, cost: Double, ch: ChannelHandler): Boolean = {
		if (sender.getPoints() >= cost || CommandHandler.checkPermission(sender.getUsername(), this.neededBotAdminCommandPower, ch.getUserList)) {
			return true
		}

    if(cost <= 0) {
      return true
    }
		return false
	}

	protected def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) = {
    if(this.success) {
      this.cooldown.startCooldown()
      sender.getUserCooldown().startCooldown()
      sender.getUserCommandCooldowns().get(this.command).startCooldown()
      sender.setPoints(sender.getPoints() - this.pointCost)
    }
	}

	def getChannelOrigin(): String = {
		return channelOrigin
	}

	def setChannelOrigin(channelOrigin: String) {
		this.channelOrigin = channelOrigin
	}

	def getCommand(): String = {
		return command
	}

	def setCommand(command: String) = {
		this.command = command
	}

	def getParam(): Int = {
		return param
	}

	def setParam(param: Int) = {
		this.param = param
	}

	def getCooldown(): Cooldown = {
		return cooldown
	}

	def setCooldown(cooldown: Cooldown) = {
		this.cooldown = cooldown
	}

	def getAccess(): String = {
		return access
	}

	def setAccess(access: String) = {
		this.access = access
	}

	def getHelptext(): String = {
		return helptext
	}

	def setHelptext(helptext: String) = {
		this.helptext = helptext
	}

	def getCmdtype(): String = {
		return cmdtype
	}

	def setCmdtype(cmdtype: String) = {
		this.cmdtype = cmdtype
	}

	def getListContent(): ArrayList[String] = {
		return listContent
	}

	def setListContent(listContent: ArrayList[String]) = {
		this.listContent = listContent
	}

	def getUnformattedOutput(): String = {
		return unformattedOutput
	}

	def setUnformattedOutput(unformattedOutput: String) = {
		this.unformattedOutput = unformattedOutput
	}

	def getPointCost(): Double = {
		return pointCost
	}

	def setPointCost(pointCost: Double) = {
		this.pointCost = pointCost
	}

	def getQuotePrefix(): String = {
		return quotePrefix
	}

	def setQuotePrefix(quotePrefix: String) = {
		this.quotePrefix = quotePrefix
	}

	def getQuoteSuffix(): String = {
		return quoteSuffix
	}

	def setQuoteSuffix(quoteSuffix: String) = {
		this.quoteSuffix = quoteSuffix
	}

	def getQuoteModAccess(): String = {
		return quoteModAccess
	}

	def setQuoteModAccess(quoteModAccess: String) = {
		this.quoteModAccess = quoteModAccess
	}

	def isLocked(): Boolean = {
		return locked
	}

	def setLocked(locked: Boolean) = {
		this.locked = locked
	}

	def getCounter(): Int = {
		return counter
	}

	def setCounter(counter: Int) = {
		this.counter = counter
	}

	def getAliases(): ArrayList[String] = {
		return aliases
	}

	def setAliases(aliases: ArrayList[String]) = {
		this.aliases = aliases
	}

	def isTexttrigger(): Boolean = {
		return texttrigger
	}

	def setTexttrigger(texttrigger: Boolean) = {
		this.texttrigger = texttrigger
	}

	def getCommandCollection(): MongoCollection[Document] = {
		return commandCollection
	}

	def setCommandCollection(commandCollection: MongoCollection[Document]) = {
		this.commandCollection = commandCollection
	}

	def getNeededCommandPower(): Int = {
		return neededCommandPower
	}

	def setNeededCommandPower(neededCommandPower: Int) = {
		this.neededCommandPower = neededCommandPower
	}

	def getNeededModCommandPower(): Int = {
		return neededModCommandPower
	}

	def setNeededModCommandPower(neededModCommandPower: Int) = {
		this.neededModCommandPower = neededModCommandPower
	}

	def getNeededBroadcasterCommandPower(): Int = {
		return neededBroadcasterCommandPower
	}

	def setNeededBroadcasterCommandPower(neededBroadcasterCommandPower: Int) = {
		this.neededBroadcasterCommandPower = neededBroadcasterCommandPower
	}

	def getNeededBotAdminCommandPower(): Int = {
		return neededBotAdminCommandPower
	}

	def setNeededBotAdminCommandPower(neededBotAdminCommandPower: Int) = {
		this.neededBotAdminCommandPower = neededBotAdminCommandPower
	}

	def getUserCooldownLen(): Int = {
		return userCooldownLen
	}

	def setUserCooldownLen(userCooldownLen: Int) = {
		this.userCooldownLen = userCooldownLen
	}

	def isAppendGameToQuote(): Boolean = {
		return appendGameToQuote
	}

	def setAppendGameToQuote(appendGameToQuote: Boolean) = {
		this.appendGameToQuote = appendGameToQuote
	}

	def isAppendDateToQuote(): Boolean = {
		return appendDateToQuote
	}

	def setAppendDateToQuote(appendDateToQuote: Boolean) {
		this.appendDateToQuote = appendDateToQuote
	}

	def isExcludeFromCommandList(): Boolean = {
		return excludeFromCommandList
	}

	def setExcludeFromCommandList(excludeFromCommandList: Boolean) = {
		this.excludeFromCommandList = excludeFromCommandList
	}

	def getNeededCooldownBypassPower(): Int = {
		return neededCooldownBypassPower
	}

	def setNeededCooldownBypassPower(neededCooldownBypassPower: Int) = {
		this.neededCooldownBypassPower = neededCooldownBypassPower
	}

	def getNeededAddPower(): Int = {
		return neededAddPower
	}

	def setNeededAddPower(neededAddPower: Int) = {
		this.neededAddPower = neededAddPower
	}

	def isAllowPicksFromList(): Boolean = {
		return allowPicksFromList
	}

	def setAllowPicksFromList(allowPicksFromList: Boolean) = {
		this.allowPicksFromList = allowPicksFromList
	}

	def isRemoveFromListOnPickIfMod(): Boolean = {
		return removeFromListOnPickIfMod
	}

	def setRemoveFromListOnPickIfMod(removeFromListOnPickIfMod: Boolean) = {
		this.removeFromListOnPickIfMod = removeFromListOnPickIfMod
	}

	def isEnable(): Boolean = {
		return enable
	}

	def setEnable(enable: Boolean) = {
		this.enable = enable
	}

	def getCommandScript(): String = {
		return commandScript
	}

	def setCommandScript(commandScript: String) = {
		this.commandScript = commandScript
	}

}
