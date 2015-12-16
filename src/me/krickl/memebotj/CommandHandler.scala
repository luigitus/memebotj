package me.krickl.memebotj

import java.io.{BufferedReader, InputStreamReader}
import java.net.{HttpURLConnection, URL, URLEncoder}
import java.text.SimpleDateFormat
import java.util
import java.util.logging.Logger
import java.util.{ArrayList, Calendar, HashMap, Random}

import com.mongodb.client.{FindIterable, MongoCollection}
import org.bson.Document
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

import scala.beans.BeanProperty

//remove if not needed
import scala.collection.JavaConversions._

object CommandHandler {
  final val log = Logger.getLogger(CommandHandler.getClass.getName)

  def checkPermission(sender: String, reqPermLevel: Int, userList: java.util.HashMap[String, UserHandler]): Boolean = {
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
      if (reqPermLevel <= userList.get(sender)._commandPower) {
        return true
      }
    } else if (sender.equals("#readonly#")) {
      if (reqPermLevel <= 10) {
        return true
      }
    }

    false
  }
}

/** *
  * This class is the base class for all commands.
  *
  * @author unlink
  *
  */
class CommandHandler(channel: String, commandName: String = "null", dbprefix: String = null) {

  @BeanProperty
  var channelOrigin: String = channel
  @BeanProperty
  var command: String = commandName
  @BeanProperty
  var param: Int = 0
  @BeanProperty
  var pointCost: Double = 0
  @BeanProperty
  var cooldown = new Cooldown(2)
  @BeanProperty
  var access: String = "viewers"
  @BeanProperty
  var helptext: String = "null"
  @BeanProperty
  var cmdtype = "default"
  @BeanProperty
  var listContent = new java.util.ArrayList[String]()
  @BeanProperty
  var unformattedOutput = "null"
  @BeanProperty
  var quotePrefix = "#{number}: "
  @BeanProperty
  var quoteSuffix = ""
  @BeanProperty
  var quoteModAccess = "moderators"
  @BeanProperty
  var counter: Int = 0
  @BeanProperty
  var aliases = new java.util.ArrayList[String]()
  @BeanProperty
  var locked = false
  @BeanProperty
  var texttrigger = false

  @BeanProperty
  var neededCommandPower = 10
  @BeanProperty
  var neededModCommandPower = 25
  @BeanProperty
  var neededBroadcasterCommandPower = 50
  @BeanProperty
  var neededBotAdminCommandPower = 75
  @BeanProperty
  var neededCooldownBypassPower = 50
  @BeanProperty
  var neededAddPower = 25
  @BeanProperty
  var allowPicksFromList = true
  @BeanProperty
  var removeFromListOnPickIfMod = false

  @BeanProperty
  var userCooldownLen = 0

  @BeanProperty
  var appendToQuoteString = ""

  @BeanProperty
  var excludeFromCommandList = false
  @BeanProperty
  var enable = true
  @BeanProperty
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

  @BeanProperty
  var commandCollection: MongoCollection[Document] = null
  @BeanProperty
  var commandScript: String = ""

  if (Memebot.useMongo) {
    if (dbprefix == null) {
      this.commandCollection = Memebot.db.getCollection(this.channelOrigin + "_commands")
    } else {
      this.commandCollection = Memebot.db.getCollection(dbprefix + this.channelOrigin + "_commands")
    }
  }

  this.readDBCommand()
  this.overrideDBData()

  def update(ch: ChannelHandler) {
    if (this.cmdtype.equals("timer") && ch.isLive) {
      val newArray = new Array[String](0)
      this.executeCommand(new UserHandler("#internal#", this.channelOrigin), ch, newArray, ch.userList)
    }
  }

  def executeCommand(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String], userList: java.util.HashMap[String, UserHandler]): String = {
    this.success = true

    if (this.overrideHandleMessage) {
      return "override"
    }

    if (!enable) {
      return "disabled"
    }

    // check global cooldown
    if ((!this.cooldown.canContinue || !sender.getUserCooldown.canContinue) && !CommandHandler.checkPermission(sender.getUsername, this.neededCooldownBypassPower, userList)) {
      return "cooldown"
    }

    // check user cooldown
    if (!sender.getUserCommandCooldowns.containsKey(this.command)) {
      sender.getUserCommandCooldowns.put(this.command, new Cooldown(this.userCooldownLen))
    } else {
      if (sender.getUserCommandCooldowns.get(this.command).getCooldownLen != this.userCooldownLen) {
        sender.getUserCommandCooldowns.get(this.command).setCooldownLen(this.userCooldownLen)
      }
    }

    if (!sender.getUserCommandCooldowns.get(this.command).canContinue
      && !CommandHandler.checkPermission(sender.getUsername, this.neededCooldownBypassPower, userList)) {
      return "usercooldown"
    }

    if (!CommandHandler.checkPermission(sender.getUsername, this.neededCommandPower, userList)) {
      return "denied"
    }
    if (!this.checkCost(sender, this.pointCost, channelHandler)) {
      channelHandler.sendMessage(f"Sorry, you don't have ${this.pointCost.toFloat} ${channelHandler.currencyEmote}", this.channelOrigin)
      return "cost"
    }

    var formattedOutput = this.unformattedOutput
    val counterStart = 1
    //var success = true

    if (this.cmdtype.equals("list")) {
      try {
        if (data(1).equals("add") && CommandHandler.checkPermission(sender.getUsername, this.neededAddPower, userList)) {
          var newEntry = ""
          for (i <- 2 to data.length - 1) {
            newEntry = newEntry + " " + data(i)
          }
          if (!newEntry.isEmpty) {
            this.listContent.add(newEntry + " " + Memebot.formatText(this.appendToQuoteString, channelHandler, sender, this))
            formattedOutput = "Added "
            this.success = false
          } else {
            formattedOutput = "Not added"
            this.success = false
          }
        } else if (data(1).equals("remove") && CommandHandler.checkPermission(sender.getUsername, this.neededModCommandPower, userList)) {
          try {
            this.listContent.remove(Integer.parseInt(data(2)))
            formattedOutput = "Removed"
            this.success = false
          } catch {
            case e: IndexOutOfBoundsException =>
              formattedOutput = e.toString
          }
        } else if (data(1).equals("edit") && CommandHandler.checkPermission(sender.getUsername, this.neededModCommandPower, userList)) {
          var newEntry = ""
          for (i <- 3 to data.length - 1) {
            newEntry = newEntry + " " + data(i)
          }

          this.listContent.set(Integer.parseInt(data(2)), newEntry)
          formattedOutput = "Edited"
          this.success = false
        } else if (data(1).equals("list")) {
          formattedOutput = "List: " + channelHandler.getChannelPageBaseURL + "/" + URLEncoder.encode(this.command, "UTF-8") + ".html"
          this.success = false

        } else if (data(1).equals("clear") && CommandHandler.checkPermission(sender.getUsername, 75, channelHandler.getUserList)) {
          formattedOutput = "Cleared list. This cannot be undone."
          this.listContent.clear()
          this.success = true

        } else if (data(1).equals("import_json")) {
          formattedOutput = "Could not import json"
          // todo fix later
          try {
            val url = new URL(data(2))
            val connection = url.openConnection().asInstanceOf[HttpURLConnection]
            val in = new BufferedReader(new InputStreamReader(connection.getInputStream))
            var dataBuffer = ""
            dataBuffer = Stream.continually(in.readLine()).takeWhile(_ != null).mkString("\n")
            in.close()
            this.success = true
            val parser = new JSONParser()
            val obj = parser.parse(dataBuffer).asInstanceOf[JSONObject]
          } catch {
            case e: IndexOutOfBoundsException => e.printStackTrace()
          }

        } else if (allowPicksFromList) {
          try {
            formattedOutput = this.quotePrefix.replace("{number}", data(1)) + this.listContent.get(Integer.parseInt(data(1))) + this.quoteSuffix.replace("{number}", data(1))
            if (this.removeFromListOnPickIfMod && CommandHandler.checkPermission(sender.getUsername, this.neededBroadcasterCommandPower, userList)) {
              this.listContent.remove(Integer.parseInt(data(1)))
            }
          } catch {
            case e: NumberFormatException =>
              formattedOutput = "That's not an integer"
            case e: IndexOutOfBoundsException =>
              formattedOutput = "Index out of bounds: Size " + Integer.toString(this.listContent.size())
          }
        } else {
          this.success = false
        }
      } catch {
        case e: ArrayIndexOutOfBoundsException =>
          try {
            val rand = new Random()
            val i = rand.nextInt(this.listContent.size())
            formattedOutput = this.quotePrefix.replace("{number}", Integer.toString(i)) + this.listContent.get(i) + this.quoteSuffix.replace("{number}", Integer.toString(i))

            if (this.removeFromListOnPickIfMod && CommandHandler.checkPermission(sender.getUsername, this.neededBroadcasterCommandPower, userList)) {
              this.listContent.remove(i)
            }
          } catch {
            case e: IllegalArgumentException =>
              e.printStackTrace()
          } finally {
            // just ignore it
          }

      }
    } else if (this.cmdtype.equals("counter")) {
      var modifier = 1
      try {
        modifier = Integer.parseInt(data(2))
      } catch {
        case e: ArrayIndexOutOfBoundsException =>
          e.printStackTrace()
        case e: NumberFormatException => e.printStackTrace()
      }

      try {
        if (data(1).equals("add")
          && CommandHandler.checkPermission(sender.getUsername, this.neededModCommandPower, userList)) {
          counter = counter + modifier
        } else if (data(1).equals("sub")
          && CommandHandler.checkPermission(sender.getUsername, this.neededModCommandPower, userList)) {
          counter = counter - modifier
        } else if (data(1).equals("set")
          && CommandHandler.checkPermission(sender.getUsername, this.neededModCommandPower, userList)) {
          counter = modifier
        }
      } catch {
        case e: ArrayIndexOutOfBoundsException =>
          e.printStackTrace()
        case e: NumberFormatException => e.printStackTrace()
      }
    }

    formattedOutput = Memebot.formatText(formattedOutput, channelHandler, sender, this)

    try {
      for (i <- counterStart to this.param) {
        formattedOutput = formattedOutput.replace("{param" + Integer.toString(i) + "}", data(i))
      }
      if (!formattedOutput.equals("null")) {
        channelHandler.sendMessage(formattedOutput, this.channelOrigin, sender)
      }
    } catch {
      case e: ArrayIndexOutOfBoundsException =>
        if (!this.helptext.equals("null")) {
          channelHandler.sendMessage(this.helptext, this.channelOrigin)
        }
        return "usage"
    }

    this.commandScript(sender, channelHandler, data)

    // write changes to db
    if (!sender.getUsername.equals("#readonly#")) {
      this.writeDBCommand()
    }

    this.execCounter = this.execCounter + 1

    "OK"
  }

  protected def checkCost(sender: UserHandler, cost: Double, ch: ChannelHandler): Boolean = {
    if (sender.points >= cost || CommandHandler.checkPermission(sender.getUsername, this.neededBotAdminCommandPower, ch.getUserList)) {
      return true
    }

    if (cost <= 0) {
      return true
    }
    false
  }

  protected def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) = {
    if (this.success) {
      this.cooldown.startCooldown()
      sender.getUserCooldown.startCooldown()
      sender.getUserCommandCooldowns.get(this.command).startCooldown()
      sender.setPoints(sender.points - this.pointCost)
    }
  }

  /***
    * This method will always be called after the Database has ben read
    * and can be used to override data saved in the Database.
    * This should be used instead of constructors for child classes
    */
  protected def overrideDBData(channelHandler: ChannelHandler = null): Unit = {

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
    * @param modType Modification type
    * @param newValue New Value as String
    * @param sender Sender Object
    * @param userList List of all Users
    * @return
    */
  def editCommand(modType: String, newValue: String, sender: UserHandler, userList: java.util.HashMap[String, UserHandler]): Boolean = {
    if (!CommandHandler.checkPermission(sender.getUsername, this.neededModCommandPower, userList)) {
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
        if (newValue == "{none}") {
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
      } else if (modType.equals("lock") && CommandHandler.checkPermission(sender.getUsername, this.neededBroadcasterCommandPower, userList)) {
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
      } else if (modType.equals("script") && CommandHandler.checkPermission(sender.getUsername, 75, userList)) {
        this.commandScript = newValue
        success = true
      } else if (modType.equals("enable")) {
        this.enable = newValue.toBoolean
        success = true
      } else if (modType.equals("allowpick")) {
        allowPicksFromList = newValue.toBoolean
        success = true
      } else if (modType.equals("cooldownbypasspower")) {
        this.neededCooldownBypassPower = Integer.parseInt(newValue)
        success = true
      } else if (modType.equals("neededAddPower")) {
        this.neededAddPower = Integer.parseInt(newValue)
        success = true
      } else if (modType.equals("autoremove")) {
        this.removeFromListOnPickIfMod = newValue.toBoolean
        success = true
      } else if (modType == "appendtoquote") {
        this.appendToQuoteString = newValue
        success = true
      } else if (modType == "overridehandlemessage") {
        this.overrideHandleMessage = newValue.toBoolean
        success = true
      } else if (modType == "listregex") {
        this.listregex = newValue
        success = true
      } else if (modType == "case") {
        this.caseSensitive = newValue.toBoolean
        success = true
      }
    } catch {
      case e: NumberFormatException =>
        CommandHandler.log.warning(String.format("Screw you Luigitus: %s", e.toString))
    }
    this.writeDBCommand()


    success
  }

  def writeDBCommand() {
    if (!Memebot.useMongo) {
      return
    }

    CommandHandler.log.info(String.format("Writing data for command %s to db", this.command))

    val channelQuery = new Document("_id", this.command)

    val otherDataDocument = new Document()
    for (key <- this.otherData.keySet()) {
      otherDataDocument.append(key, this.otherData.get(key))
    }

    val channelData = new Document("_id", this.command).append("command", this.command)
      .append("cooldown", new Integer(this.cooldown.getCooldownLen)).append("access", this.access)
      .append("helptext", this.helptext).append("param", new Integer(this.param))
      .append("cmdtype", this.cmdtype).append("output", this.unformattedOutput)
      .append("qsuffix", this.quoteSuffix).append("qprefix", this.quotePrefix)
      .append("qmodaccess", this.quoteModAccess).append("costf", this.pointCost)
      .append("counter", this.counter).append("listcontent", this.listContent).append("locked", this.locked)
      .append("texttrigger", this.texttrigger).append("viewerpower", this.neededCommandPower)
      .append("modpower", this.neededModCommandPower)
      .append("broadcasterpower", this.neededBroadcasterCommandPower)
      .append("botadminpower", this.neededBotAdminCommandPower).append("usercooldown", this.userCooldownLen)
      .append("script", this.commandScript)
      .append("enable", this.enable)
      .append("cooldownbypasspower", this.neededCooldownBypassPower)
      .append("allowpick", this.allowPicksFromList)
      .append("addpower", this.neededAddPower)
      .append("autoremove", this.removeFromListOnPickIfMod)
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
      case e: Exception =>
        e.printStackTrace()
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
      case e: java.lang.IllegalArgumentException =>
        e.printStackTrace()
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
      this.listContent = channelData.getOrDefault("listcontent", this.listContent).asInstanceOf[java.util.ArrayList[String]]
      this.locked = channelData.getOrDefault("locked", this.locked.toString).toString.toBoolean
      this.texttrigger = channelData.getOrDefault("texttrigger", this.texttrigger.toString).toString.toBoolean
      this.neededCommandPower = channelData.getOrDefault("viewerpower", this.neededCommandPower.toString).toString.toInt
      this.neededModCommandPower = channelData.getOrDefault("modpower", this.neededModCommandPower.toString).toString.toInt
      this.neededBroadcasterCommandPower = channelData.getOrDefault("broadcasterpower", this.neededBroadcasterCommandPower.toString).toString.toInt
      this.neededBotAdminCommandPower = channelData.getOrDefault("botadminpower", this.neededBotAdminCommandPower.toString).toString.toInt
      this.userCooldownLen = channelData.getOrDefault("usercooldown", this.userCooldownLen.toString).toString.toInt
      this.commandScript = channelData.getOrDefault("script", this.commandScript.toString).toString
      this.enable = channelData.getOrDefault("enable", this.enable.toString).toString.toBoolean
      this.neededCooldownBypassPower = channelData.getOrDefault("cooldownbypass", this.neededCooldownBypassPower.toString).toString.toInt
      this.allowPicksFromList = channelData.getOrDefault("allowpick", this.allowPicksFromList.toString).toString.toBoolean
      this.neededAddPower = channelData.getOrDefault("addpower", this.neededAddPower.toString).toString.toInt
      this.removeFromListOnPickIfMod = channelData.getOrDefault("autoremove", this.removeFromListOnPickIfMod.toString).toString.toBoolean
      this.appendToQuoteString = channelData.getOrDefault("appendtoquote", this.appendToQuoteString).toString
      this.overrideHandleMessage = channelData.getOrDefault("overridehandlemessage", this.overrideHandleMessage.toString).toString.toBoolean
      this.execCounter = channelData.getOrDefault("execcounter", this.execCounter.toString).toString.toInt
      this.listregex = channelData.getOrDefault("listregex", this.listregex).toString
      this.caseSensitive = channelData.getOrDefault("case", this.caseSensitive.toString).toString.toBoolean
      //other data are used to store data that are used for internal commands
      val otherDataDocument = channelData.getOrDefault("otherdata", new Document()).asInstanceOf[Document]

      for (key <- otherDataDocument.keySet()) {
        this.otherData.put(key, otherDataDocument.getString(key))
      }
    }
  }

}
