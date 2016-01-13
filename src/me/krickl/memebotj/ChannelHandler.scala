package me.krickl.memebotj

import java.io._
import java.math.BigInteger
import java.net.{HttpURLConnection, MalformedURLException, URL, URLEncoder}
import java.security.SecureRandom
import java.util
import java.util.logging.Logger
import java.util.{ArrayList, Arrays, HashMap}

import com.mongodb.Block
import com.mongodb.client.MongoCollection
import me.krickl.memebotj.InternalCommands.AdminCommands._
import me.krickl.memebotj.InternalCommands.FunCommands._
import me.krickl.memebotj.InternalCommands.ModeratorCommands._
import me.krickl.memebotj.InternalCommands.UserCommands._
import me.krickl.memebotj.InternalCommands._
import org.bson.Document
import org.json.simple.JSONObject
import org.json.simple.parser.{JSONParser, ParseException}

import scala.beans.{BeanProperty, BooleanBeanProperty}
import scala.util.control.Breaks._
//remove if not needed
import scala.collection.JavaConversions._

object ChannelHandler {

  @BeanProperty
  val log = Logger.getLogger(classOf[ChannelHandler].getName)
}

class ChannelHandler(@BeanProperty var channel: String, @BeanProperty var connection: ConnectionHandler)
  extends Runnable {

  @BeanProperty
  var broadcaster: String = this.channel.replace("#", "")
  @BeanProperty
  var userList: java.util.HashMap[String, UserHandler] = new java.util.HashMap[String, UserHandler]()
  @BeanProperty
  var updateCooldown: Cooldown = new Cooldown(600)
  @BeanProperty
  var channelCommands: java.util.ArrayList[CommandHandler] = new java.util.ArrayList[CommandHandler]()
  @BeanProperty
  var internalCommands: java.util.ArrayList[CommandHandler] = new java.util.ArrayList[CommandHandler]()
  @BeanProperty
  var followerNotification: String = ""
  @BeanProperty
  var channelInfoURL: String = "https://api.twitch.tv/kraken/channels/" + this.broadcaster
  @BeanProperty
  var channelFollowersURL: String = channelInfoURL + "/follows/?limit="
  @BeanProperty
  var raceBaseURL: String = "http://kadgar.net/live"
  @BeanProperty
  var greetMessage: String = "Hello I'm {botnick} {version} the dankest irc bot ever RitzMitz"
  @BeanProperty
  var currentRaceURL: String = ""
  @BeanProperty
  var fileNameList: java.util.ArrayList[String] = new java.util.ArrayList[String]()
  //@BeanProperty
  //var aliasList: ArrayList[String] = new ArrayList[String]()

  @BeanProperty
  var maxFileNameLen = 8
  @BeanProperty
  var currentFileName: String = ""
  @BeanProperty
  var streamStartTime: Long = 0
  //@BeanProperty @Deprecated
  //var builtInStrings: java.util.HashMap[String, String] = new java.util.HashMap[String, String]()

  var local = "engb"
  @BeanProperty
  var channelPageURL: String = Memebot.webBaseURL + this.broadcaster + "/index.html"
  @BeanProperty
  var channelPageBaseURL: String = Memebot.webBaseURL + this.broadcaster
  @BeanProperty
  var htmlDir: String = Memebot.htmlDir + "/" + this.broadcaster
  @BeanProperty
  var youtubeAPIURL: String = "https://www.googleapis.com/youtube/v3/videos?id={videoid}&part=contentDetails&key=" +
    Memebot.youtubeAPIKey
  @BeanProperty
  var otherLoadedChannels: java.util.ArrayList[String] = new java.util.ArrayList[String]()
  @BeanProperty
  var channelCollection: MongoCollection[Document] = _
  @BeanProperty
  var pointsPerUpdate: Double = 1.0f
  @BeanProperty
  var t: Thread = _
  @BeanProperty
  var isJoined: Boolean = true
  @BooleanBeanProperty
  var allowAutogreet: Boolean = true
  @BeanProperty
  var isLive: Boolean = false
  @BeanProperty
  var currentMessageCount: Int = 0
  @BeanProperty
  var messageLimitCooldown: Cooldown = new Cooldown(30)
  @BeanProperty
  var preventMessageCooldown: Cooldown = new Cooldown(30)
  @BeanProperty
  var currentGame: String = "Not Playing"
  @BeanProperty
  var random: SecureRandom = new SecureRandom()
  @BeanProperty
  var privateKey: String = new BigInteger(130, random).toString(32)
  @BeanProperty
  var urlRegex: String = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
  @BooleanBeanProperty
  var purgeURLS: Boolean = false
  @BooleanBeanProperty
  var purgeURLSNewUsers: Boolean = false
  var givePointsWhenOffline = false

  ChannelHandler.getLog.info("Joining channel " + this.channel)
  @BeanProperty
  var linkTimeout: Int = 1

  @BeanProperty
  val broadcasterHandler = new UserHandler(this.broadcaster, this.channel)
  @BeanProperty
  val htmlDirF = new File(this.htmlDir)
  @BeanProperty
  val issueCommand = new CommandHandler(this.channel, "!issue", "#internal#")
  @BeanProperty
  val readOnlyUser = new UserHandler("#readonly#", this.channel)
  var allowGreetMessage = false

  var maxPoints: Double = 100000.0f

  //val mrDestructoidCommand = new CommandHandler(this.channel, "!noamidnightonthethirdday", "#internal#")

  var currencyName = "points"
  var currencyEmote = "points"
  var followAnnouncement = ""
  var maxScreenNameLen = 15

  broadcasterHandler.isUserBroadcaster = true

  broadcasterHandler.setIsModerator(true)

  this.userList.put(this.broadcaster, broadcasterHandler)

  @BeanProperty
  var silentMode = false

  if (!htmlDirF.exists()) {
    htmlDirF.mkdirs()
  }

  this.joinChannel(this.channel)

  if (Memebot.useMongo) {
    this.channelCollection = Memebot.db.getCollection(this.channel)
  }

  this.readDBChannelData()

  var localisation = new Localisation(this.local)

  this.internalCommands.add(new AboutCommand(this.channel, "!about", "#internal#"))

  this.internalCommands.add(new AutogreetCommand(this.channel, "!autogreet", "#internal#"))

  this.internalCommands.add(new EditChannel(this.channel, "!channel", "#internal#"))

  this.internalCommands.add(new CommandList(this.channel, "!commands", "#internal#"))

  this.internalCommands.add(new HelpCommand(this.channel, "!help", "#internal#"))

  this.internalCommands.add(new HugCommand(this.channel, "!mehug", "#internal#"))

  this.internalCommands.add(new JoinCommand(this.channel, "!mejoin", "#internal#"))

  this.internalCommands.add(new PartCommand(this.channel, "!mepart", "#internal#"))

  this.internalCommands.add(new PointsCommand(this.channel, "!points", "#internal#"))

  this.internalCommands.add(new QuitCommand(this.channel, "!mequit", "#internal#"))

  this.internalCommands.add(new RaceCommand(this.channel, "!race", "#internal#"))

  this.internalCommands.add(new SaveCommand(this.channel, "!mesave", "#internal#"))

  this.internalCommands.add(new WhoisCommand(this.channel, "!whois", "#internal#"))

  this.internalCommands.add(new FilenameCommand(this.channel, "!name", "#internal#"))

  this.internalCommands.add(new SpeedrunCommand(this.channel, "!run", "#internal#"))

  this.internalCommands.add(new EditUserCommand(this.channel, "!user", "#internal#"))

  this.internalCommands.add(new SendMessageCommand(this.channel, "!sm", "#internal#"))

  this.internalCommands.add(new DampeCommand(this.channel, "!dampe", "#internal#"))

  this.internalCommands.add(new DebugCommand(this.channel, "!debug", "#debug#"))

  this.internalCommands.add(new PyramidCommand(this.channel, "!pyramid", "#internal#"))

  this.internalCommands.add(new CommandManager(this.channel, "!command", "#internal#"))

  this.internalCommands.add(new ChannelInfoCommand(this.channel, "!ci", "#internal#"))

  this.internalCommands.add(new UptimeCommand(this.channel, "!uptime", "#internal#"))

  this.internalCommands.add(new BobRossCommand(this.channel, "!bobross", "#internal#"))

  this.internalCommands.add(new BKTWVEAAAVBMOFSRCCommand(this.channel, "!BKTWVEAAAVBMOFSRC", "#internal#"))

  this.internalCommands.add(new SimonsQuestCommand(this.channel, "!simonsquest", "#internal#"))

  this.internalCommands.add(new RestartThreadCommand(this.channel, "!restartt", "#internal#"))

  this.internalCommands.add(new CategoryGeneratorCommand(this.channel, "!category", "#internal#"))

  @BeanProperty
  var spamPrevention = false

  issueCommand.editCommand("output", "Having issues? Write a bugreport at https://github.com/unlink2/memebotj/issues",
    new UserHandler("#internal#", this.channel), userList)

  this.internalCommands.add(issueCommand)

  val annoyingDog = new CommandHandler(this.channel, "AnnoyingZ", "#internal#")
  annoyingDog.texttrigger = true
  annoyingDog.setUnformattedOutput("http://annoying.dog ")
  annoyingDog.cooldown = new Cooldown(6000)
  this.internalCommands.add(annoyingDog)

  @BeanProperty
  var spamTimeout = -1

  /* mrDestructoidCommand.editCommand("output", "MrDestructoid Midnight Raid MrDestructoid", new UserHandler("#internal#",
    this.channel), userList)

  mrDestructoidCommand.setExcludeFromCommandList(true)

  this.internalCommands.add(mrDestructoidCommand)*/

  if(this.allowGreetMessage) {
    this.sendMessage(Memebot.formatText(this.greetMessage, this, readOnlyUser, null), this.channel)
  }

  ChannelHandler.getLog.info(String.format("Private key for channel %s is %s", this.channel, this.privateKey))

  def partChannel(channel: String) {
    try {
      this.connection.sendMessageBytes("PART " + channel + "\n")
    } catch {
      case e: IOException => e.printStackTrace()
    }
    var isInList = false
    var removeThisCH: ChannelHandler = null
    breakable {
      for (ch <- Memebot.joinedChannels if ch.getChannel.equalsIgnoreCase(channel)) {
        isInList = true
        removeThisCH = ch
        break()
      }
    }
    if (isInList && removeThisCH != null) {
      Memebot.joinedChannels.remove(removeThisCH)
      try {
        val bw = new BufferedWriter(new FileWriter(Memebot.channelConfig))
        for (ch <- Memebot.joinedChannels) {
          bw.write(ch.getChannel + "\n")
        }
        bw.close()
      } catch {
        case e: IOException => e.printStackTrace()
      }
    }
    this.sendMessage("Leaving channel :(", this.channel)
    this.isJoined = false
    this.t.interrupt()
    this.connection.close()
    Memebot.joinedChannels.remove(removeThisCH)
  }

  override def run() {
    //let update loop run independently
    if(Memebot.useUpdateThread) {
      val updateThread = new Thread {
        override def run(): Unit = {
          while (isJoined) {
            update()
            Thread.sleep(100)
          }
        }
      }
      updateThread.start()
    }

    while (this.isJoined) {
      var ircmsg = Array("", "")
      try {
        ircmsg = this.connection.recvData()
      } catch {
        case e: IOException => e.printStackTrace()
      }
      //this.update()
      if (this.getChannel.equalsIgnoreCase(ircmsg(0))) {
        this.handleMessage(ircmsg(1))
      }
      try {
        Thread.sleep(50)
      } catch {
        case e: InterruptedException => e.printStackTrace()
      }

      if(!Memebot.useUpdateThread) {
        this.update()
      }
    }
  }

  def update() {
    if (this.messageLimitCooldown.canContinue) {
      this.messageLimitCooldown.startCooldown()
      this.currentMessageCount = 0
    }
    if (this.updateCooldown.canContinue) {
      this.updateCooldown.startCooldown()

      //twitch update
      try {
        if(Memebot.isTwitchBot) {
          val data = Memebot.readHttpRequest("https://api.twitch.tv/kraken/streams/" + this.broadcaster)
          val parser = new JSONParser()
          val obj = parser.parse(data).asInstanceOf[JSONObject]
          val isOnline = obj.get("stream")
          if (isOnline == null) {
            ChannelHandler.getLog.info(String.format("Stream %s is offline", this.channel))
            this.isLive = false
            this.streamStartTime = -1
          } else {
            ChannelHandler.getLog.info(String.format("Stream %s is live", this.channel))
            if (this.isLive) {
              this.streamStartTime = System.currentTimeMillis()
            }
            this.isLive = true
          }
        } else {
          this.isLive = true
          this.streamStartTime = -1
        }
      } catch {
        case e: MalformedURLException => e.printStackTrace()
        case e: IOException => e.printStackTrace()
        case e: ParseException => e.printStackTrace()
      }

      //get game
      try {
        if(Memebot.isTwitchBot) {
          val data = Memebot.readHttpRequest(this.channelInfoURL)

          val parser = new JSONParser()
          val obj = parser.parse(data).asInstanceOf[JSONObject]
          this.currentGame = obj.get("game").asInstanceOf[String]
          if (this.currentGame == null) {
            this.currentGame = "Not Playing"
          }
        } else {
          this.currentGame = ""
        }
      } catch {
        case e: MalformedURLException => e.printStackTrace()
        case e: IOException => e.printStackTrace()
        case e: ParseException => e.printStackTrace()
      }

      //get follower list


      this.writeDBChannelData()
      this.writeHTML()

      val removeUsers = new java.util.ArrayList[String]()
      val it = this.userList.keySet.iterator()
      while (it.hasNext) {
        val key = it.next()
        val uh = this.userList.get(key)
        uh.update(this)
        if (this.isLive || this.givePointsWhenOffline) {
          uh.setPoints(uh.points + this.pointsPerUpdate * 10)
        }
        uh.writeDBUserData()

        if(uh.canRemove) {
          removeUsers.add(key)
        }
      }
      for(user <- removeUsers) {
        this.userList.remove(user)
      }

      for (ch <- this.channelCommands) {
        ch.update(this)
      }
    }
  }

  def writeHTML() {
    if (!Memebot.useWeb) {
      return
    }
    try {
      val bw = new BufferedWriter(new FileWriter(this.htmlDir + "/index.html"))
      bw.write("<head><link rel=\"stylesheet\" type=\"text/css\" href=\"../style.css\"></head>")
      bw.write("<html>")
      bw.write("<h1>Index for " + this.broadcaster + "</h1>")
      bw.write("<table style=\"width:100%\">")
      bw.write("<tr>")
      bw.write("<td>")
      bw.write("Command")
      bw.write("</td>")
      bw.write("<td>")
      bw.write("Help")
      bw.write("</td>")
      bw.write("<td>")
      bw.write("Output")
      bw.write("</td>")
      bw.write("<td>")
      bw.write("Access")
      bw.write("</td>")
      bw.write("<td>")
      bw.write("Command Type")
      bw.write("</td>")
      bw.write("</tr>")
      for (ch <- this.internalCommands) {
        if (ch.excludeFromCommandList) {
          //continue
        }
        bw.write("<tr>")
        bw.write("<td>")
        if (ch.getCmdtype == "list") {
          bw.write("<a href=\"" + this.channelPageBaseURL + "/" + URLEncoder.encode(ch.command, "UTF-8") +
            ".html\">" +
            ch.getCommand +
            "</a>")
          val bwq = new BufferedWriter(new FileWriter(this.htmlDir + "/" + URLEncoder.encode(ch.command, "UTF-8") + ".html"))
          bwq.write("<head><link rel=\"stylesheet\" type=\"text/css\" href=\"../style.css\"></head>")
          bwq.write("<html>")
          bwq.write("<h1>")
          bwq.write(ch.command)
          bwq.write("</h1>")
          bwq.write("<table style=\"width:100%\">")
          bwq.write("<tr>")
          bwq.write("<td>")
          bwq.write("#")
          bwq.write("</td>")
          bwq.write("<td>")
          bwq.write("Content")
          bwq.write("</td>")
          bwq.write("</tr>")
          var c = 0
          for (item <- ch.getListContent) {
            bwq.write("<tr>")
            bwq.write("<td>")
            bwq.write(java.lang.Integer.toString(c))
            bwq.write("</td>")
            bwq.write("<td>")
            bwq.write(item)
            bwq.write("</td>")
            bwq.write("</tr>")
            c += 1
          }
          bwq.write("</table>")
          bwq.write("</html>")
          bwq.close()
        } else {
          bw.write(ch.getCommand)
        }
        bw.write("</td>")
        bw.write("<td>")
        bw.write(ch.getHelptext)
        bw.write("</td>")
        bw.write("<td>")
        bw.write(ch.getUnformattedOutput)
        bw.write("</td>")
        bw.write("<td>")
        bw.write(java.lang.Integer.toString(ch.getNeededCommandPower))
        bw.write("</td>")
        bw.write("<td>")
        bw.write(ch.getCmdtype)
        bw.write("</td>")
        bw.write("</tr>")
      }
      for (ch <- this.channelCommands) {
        if (ch.getExcludeFromCommandList) {
          //continue
        }
        bw.write("<tr>")
        bw.write("<td>")
        if (ch.getCmdtype == "list") {
          bw.write("<a href=\"" + this.channelPageBaseURL + "/" + URLEncoder.encode(ch.getCommand, "UTF-8") +
            ".html\">" +
            ch.getCommand +
            "</a>")
          val bwq = new BufferedWriter(new FileWriter(this.htmlDir + "/" + ch.getCommand + ".html"))
          bwq.write("<head><link rel=\"stylesheet\" type=\"text/css\" href=\"../style.css\"></head>")
          bwq.write("<html>")
          bwq.write("<h1>")
          bwq.write(ch.getCommand)
          bwq.write("</h1>")
          bwq.write("<table style=\"width:100%\">")
          bwq.write("<tr>")
          bwq.write("<td>")
          bwq.write("#")
          bwq.write("</td>")
          bwq.write("<td>")
          bwq.write("Content")
          bwq.write("</td>")
          bwq.write("</tr>")
          var c = 0
          for (item <- ch.getListContent) {
            bwq.write("<tr>")
            bwq.write("<td>")
            bwq.write(java.lang.Integer.toString(c))
            bwq.write("</td>")
            bwq.write("<td>")
            bwq.write(item)
            bwq.write("</td>")
            bwq.write("</tr>")
            c += 1
          }
          bwq.write("</table>")
          bwq.write("</html>")
          bwq.close()
        } else {
          bw.write(ch.getCommand)
        }
        bw.write("</td>")
        bw.write("<td>")
        bw.write(ch.getHelptext)
        bw.write("</td>")
        bw.write("<td>")
        bw.write(ch.getUnformattedOutput)
        bw.write("</td>")
        bw.write("<td>")
        bw.write(java.lang.Integer.toString(ch.getNeededCommandPower))
        bw.write("</td>")
        bw.write("<td>")
        bw.write(ch.getCmdtype)
        bw.write("</td>")
        bw.write("</tr>")
      }
      bw.write("</table>")
      bw.write("</html>")
      bw.close()
      val bwf = new BufferedWriter(new FileWriter(this.htmlDir + "/filenames.html"))
      bwf.write("<head><link rel=\"stylesheet\" type=\"text/css\" href=\"../style.css\"></head>")
      bwf.write("<html>")
      bwf.write("<h1>")
      bwf.write("Filenames")
      bwf.write("</h1>")
      bwf.write("<table style=\"width:100%\">")
      bwf.write("<tr>")
      bwf.write("<td>")
      bwf.write("Filename")
      bwf.write("</td>")
      bwf.write("<td>")
      bwf.write("Suggested by")
      bwf.write("</td>")
      bwf.write("</tr>")
      for (name <- this.fileNameList) {
        bwf.write("<tr>")
        bwf.write("<td>")
        bwf.write(name.split("#")(0))
        bwf.write("</td>")
        bwf.write("<td>")
        bwf.write(name.split("#")(1))
        bwf.write("</td>")
        bwf.write("</tr>")
      }
      bwf.write("</table>")
      bwf.write("</html>")
      bwf.close()
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }

  def writeDBChannelData() {
    if (!Memebot.useMongo) {
      return
    }
    ChannelHandler.getLog.info("Saving data in db for channel " + this.channel)
    val channelQuery = new Document("_id", this.channel)

    val channelData = new Document("_id", this.channel).append("maxfilenamelen", this.maxFileNameLen)
      .append("raceurl", this.raceBaseURL)
      .append("fileanmelist", this.fileNameList)
      .append("otherchannels", this.otherLoadedChannels)
      .append("pointsperupdate", this.pointsPerUpdate)
      .append("allowautogreet", this.allowAutogreet)
      .append("privatekey", this.privateKey)
      .append("purgelinks", this.purgeURLS)
      .append("purgelinknu", this.purgeURLSNewUsers)
      .append("linktimeout", this.linkTimeout)
      .append("urlreges", this.urlRegex)
      .append("silent", this.silentMode)
      .append("preventspam", this.spamPrevention)
      .append("spamtimeout", this.spamTimeout)
      .append("pointswhenoffline", this.givePointsWhenOffline)
      .append("allowgreetmessage", this.allowGreetMessage)
      .append("maxpoints", this.maxPoints)
      .append("local", this.local)
      .append("currname", this.currencyName)
      .append("curremote", this.currencyEmote)
      .append("followannouncement", this.followAnnouncement)
      .append("maxscreennamelen", this.maxScreenNameLen)
    try {
      if (this.channelCollection.findOneAndReplace(channelQuery, channelData) ==
        null) {
        this.channelCollection.insertOne(channelData)
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }
    for (key <- this.userList.keySet) {
      this.userList.get(key).writeDBUserData()
    }
  }

  def handleMessage(rawircmsg: String) {
    var senderName = ""
    val ircTags = new java.util.HashMap[String, String]()
    var msgContent: Array[String] = null
    val ircmsgBuffer = rawircmsg.split(" ")
    var messageType = "UNDEFINED"
    var i = 0
    i = 0
    while (i < ircmsgBuffer.length) {
      val msg = ircmsgBuffer(i)
      if ((msg == "PRIVMSG" || msg == "MODE" || msg == "PART" || msg == "JOIN" ||
        msg == "CLEARCHAT") &&
        messageType == "UNDEFINED") {
        messageType = msg
      }
      if (msg.charAt(0) == '@' && i == 0) {
        val tagList = msg.split(";")
        for (tag <- tagList) {
          try {
            ircTags.put(tag.split("=")(0), tag.split("=")(1))
          } catch {
            case e: ArrayIndexOutOfBoundsException =>
          }
        }
      } else if (i == 0 || (i == 1 && senderName.isEmpty)) {
        var exclaReached = false
        breakable {
          for (j <- 0 until msg.length) {
            if (msg.charAt(j) == '!') {
              exclaReached = true
              break
            }
            if (msg.charAt(j) != ':') {
              senderName = senderName + msg.charAt(j)
            }
          }
        }
        if (!exclaReached) {
          senderName = "#internal#"
        }
      }
      if (messageType == "PRIVMSG" && i > 3) {
        if (i == 4) {
          msgContent = Array.ofDim[String](ircmsgBuffer.length - 4)
          msgContent(i - 4) = msg.substring(1)
        } else {
          msgContent(i - 4) = msg
        }
      }
      i += 1
    }
    if (!this.userList.containsKey(senderName) && !senderName.isEmpty) {
      val newUser = new UserHandler(senderName, this.channel)
      this.userList.put(senderName, newUser)
    }
    val sender = this.userList.get(senderName)
    if (messageType != "PRIVMSG") {
      val ircmsgList = rawircmsg.split(" ")
      if (ircmsgList(1) == null) {
        return
      }
      if (ircmsgList(1) == "MODE") {
        var user: UserHandler = null
        if (!this.userList.containsKey(ircmsgList(4))) {
          user = new UserHandler(ircmsgList(4), this.channel)
          this.userList.put(senderName, user)
        } else {
          user = this.userList.get(ircmsgList(4))
        }
        if (user != null) {
          if (ircmsgList(3) == "+o") {
            user.isModerator = true
            if (!user.isUserBroadcaster) {
              user.setCommandPower(CommandPower.modAbsolute)
            }
          } else {
            user.isModerator = false
            user.setCommandPower(CommandPower.viewer)
          }
        }
      } else if (ircmsgList(1) == "PART") {
        if (sender != null) {
          if (this.userList.containsKey(sender.getUsername)) {
            this.userList.get(sender.getUsername).writeDBUserData()
            this.userList.remove(sender.getUsername)
          }
        }
      } else if (ircmsgList(1) == "JOIN") {
        if (sender != null) {
          if (this.allowAutogreet && sender.getAutogreet != "") {
            this.sendMessage(sender.getAutogreet, this.channel)
          }
        }
      } else if (ircmsgList(1) == "CLEARCHAT") {
        try {
          if (this.userList.containsKey(ircmsgList(3).replace(":", ""))) {
            this.userList.get(ircmsgList(3).replace(":", "")).setTimeouts(this.userList.get(ircmsgList(3).replace(":",
              "")).getTimeouts +
              1)
            this.userList.get(ircmsgList(3).replace(":", "")).writeDBUserData()
          } else {
            val uh = new UserHandler(ircmsgList(3).replace(":", ""), this.channel)
            if (!uh.newUser) {
              uh.setTimeouts(uh.getTimeouts + 1)
              uh.writeDBUserData()
            }
          }
        } catch {
          case e: ArrayIndexOutOfBoundsException => e.printStackTrace()
        }
      }
    } else {
      if (ircTags.containsKey("user-type")) {
        if (ircTags.get("user-type") == "mod" && !sender.isUserBroadcaster) {
          sender.setIsModerator(true)
          sender.setCommandPower(CommandPower.modAbsolute)
        } else if (!sender.isUserBroadcaster) {
          sender.setIsModerator(false)
          sender.setCommandPower(CommandPower.viewerAbsolute)
        }
      } else {
        sender.setIsModerator(false)
        sender.setCommandPower(CommandPower.viewerAbsolute)
      }
      if (sender.getUsername.equalsIgnoreCase(this.broadcaster)) {
        sender.setIsModerator(true)
        sender.isUserBroadcaster = true
        sender.setCommandPower(CommandPower.broadcasterAbsolute)
      }
      for (user <- Memebot.botAdmins) {
        if (user.equalsIgnoreCase(sender.getUsername)) {
          sender.setCommandPower(CommandPower.adminAbsolute)
        }
      }
      val msg = msgContent(0)
      val data = java.util.Arrays.copyOfRange(msgContent, 0, msgContent.length)
      try {
        for (x <- 0 until data.length if data(x).matches(this.urlRegex)) {
          ChannelHandler.getLog.info("Found url in message")
          if (this.purgeURLSNewUsers && sender.newUser) {
            ChannelHandler.getLog.info("Puriging " + sender.getUsername + " for posting a link that matches the regex " +
              this.urlRegex)
            this.sendMessage("/timeout " + sender.getUsername + " " + java.lang.Integer.toString(this.linkTimeout),
              this.channel)
          } else if (this.purgeURLS) {
            ChannelHandler.getLog.info("Puriging " + sender.getUsername + " for posting a link that matches the regex " +
              this.urlRegex)
            this.sendMessage("/timeout " + sender.getUsername + " " + java.lang.Integer.toString(this.linkTimeout),
              this.channel)
          }
        }
      } catch {
        case e: java.util.regex.PatternSyntaxException => e.printStackTrace()
      }

      //channel commands
      var p = this.findCommand(msg)
      if (p != -1) {
        if (!this.channelCommands.get(p).getTexttrigger) {
          this.channelCommands.get(p).executeCommand(sender, this, data, userList)
        }
      }

      //text triggers
      for (s <- msgContent) {
        p = this.findCommand(s)

        if (p != -1) {
          val ch = this.channelCommands.get(p)
          if (ch.texttrigger) {
            ch.executeCommand(sender, this, Array(""), userList)
          }
        }
      }

      //other channel's commands
      for (ch <- Memebot.joinedChannels; och <- this.otherLoadedChannels) {
        val channel = ch.getBroadcaster
        if (ch.getChannel == och || ch.getBroadcaster == och) {
          p = ch.findCommand(msg.replace(och.replace("#", "") + ".", ""))
          if (p != -1 && msg.contains(channel)) {
            ch.getChannelCommands.get(p).executeCommand(readOnlyUser, this, data, userList)
          }
        }
      }

      //internal commands
      p = this.findCommand(msg, this.internalCommands)
      if (p != -1) {
        val ch = this.internalCommands.get(p)
        ch.executeCommand(sender, this, java.util.Arrays.copyOfRange(data, 1, data.length), userList)
      }

      //set user activity
      sender.timeSinceActivity = System.currentTimeMillis()
    }
  }

  def sendMessage(msg: String, channel: String = this.channel, sender: UserHandler = new UserHandler("#internal#", this.channel)) {
    if (!this.preventMessageCooldown.canContinue) {
      return
    }
    if (this.silentMode) {
      return
    }
    if (this.currentMessageCount >= Memebot.messageLimit) {
      ChannelHandler.getLog.warning("Reached global message limit for 30 seconds. try again later")
      this.preventMessageCooldown.startCooldown()
    }
    this.currentMessageCount += 1
    try {
      if (sender.getUsername != "#readonly#") {
        this.connection.sendMessage(new String("PRIVMSG " + this.channel + " :" + msg + "\n"))
      } else {
        this.connection.sendMessage(new String("PRIVMSG " + this.channel + " : " + msg + "\n"))
      }
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }

  def findCommand(command: String): Int = {
    this.findCommand(command, this.channelCommands)
  }

  def findCommand(command: String, commandList: java.util.ArrayList[CommandHandler]): Int = {
    for (index <- 0 to commandList.size() - 1) {
      val cmd = commandList.get(index)
      if (cmd.getCommand == command) {
        return index
      }

      if (!cmd.getCaseSensitive && cmd.command.toLowerCase() == command.toLowerCase()) {
        return index
      }
    }
    //(0 until commandList.size).find(commandList.get(_).command == command).getOrElse(-1)
    -1
  }

  def start() {
    if (t == null) {
      t = new Thread(this, this.channel)
      t.start()
    }
  }

  private def joinChannel(channel: String) {
    try {
      this.connection.sendMessageBytes("JOIN " + channel + "\n")
    } catch {
      case e: IOException => e.printStackTrace()
    }
    var isInList = false
    breakable {
      for (ch <- Memebot.joinedChannels if ch.getChannel.equalsIgnoreCase(channel)) {
        isInList = true
        break
      }
    }
    if (!isInList) {
      Memebot.joinedChannels.add(this)
      try {
        val bw = new BufferedWriter(new FileWriter(Memebot.channelConfig))
        for (ch <- Memebot.joinedChannels) {
          bw.write(ch.getChannel + "\n")
        }
        bw.close()
      } catch {
        case e: IOException => e.printStackTrace()
      }
    }
    this.isJoined = true
  }

  private def readDBChannelData() {
    if (!Memebot.useMongo) {
      return
    }
    val channelQuery = new Document("_id", this.channel)
    val cursor = this.channelCollection.find(channelQuery)
    val channelData = cursor.first()
    if (channelData != null) {
      this.maxFileNameLen = channelData.getInteger("maxfilenamelen", this.maxFileNameLen)
      this.raceBaseURL = channelData.getOrDefault("raceurl", this.raceBaseURL).asInstanceOf[String]
      this.fileNameList = channelData.getOrDefault("fileanmelist", this.fileNameList).asInstanceOf[java.util.ArrayList[String]]
      this.otherLoadedChannels = channelData.getOrDefault("otherchannels", this.otherLoadedChannels).asInstanceOf[java.util.ArrayList[String]]
      this.pointsPerUpdate = channelData.getOrDefault("pointsperupdate", this.pointsPerUpdate.toString).toString.toDouble
      this.allowAutogreet = channelData.getOrDefault("allowautogreet", this.allowAutogreet.toString).toString.toBoolean
      this.privateKey = channelData.getOrDefault("privatekey", this.privateKey).asInstanceOf[String]
      this.linkTimeout = channelData.getOrDefault("linktimeout", this.linkTimeout.toString).toString.toInt
      this.purgeURLS = channelData.getOrDefault("purgelinks", this.purgeURLS.toString).toString.toBoolean
      this.purgeURLSNewUsers = channelData.getOrDefault("purgelinknu", this.purgeURLSNewUsers.toString).toString.toBoolean
      this.urlRegex = channelData.getOrDefault("urlreges", this.urlRegex).asInstanceOf[String]
      //val bultinStringsDoc = channelData.getOrDefault("builtinstrings", new Document()).asInstanceOf[Document]
      this.silentMode = channelData.getOrDefault("silent", this.silentMode.toString).toString.toBoolean
      this.spamPrevention = channelData.getOrDefault("preventspam", this.spamPrevention.toString).toString.toBoolean
      this.spamTimeout = channelData.getOrDefault("spamtimeout", this.spamTimeout.toString).toString.toInt
      this.givePointsWhenOffline = channelData.getOrDefault("pointswhenoffline", this.givePointsWhenOffline.asInstanceOf[Object]).asInstanceOf[Boolean]
      this.allowGreetMessage = channelData.getOrDefault("allowgreetmessage", this.givePointsWhenOffline.asInstanceOf[Object]).asInstanceOf[Boolean]
      this.maxPoints = channelData.getOrDefault("maxpoints", this.maxPoints.asInstanceOf[Object]).asInstanceOf[Double]
      this.local = channelData.getOrDefault("local", this.local.asInstanceOf[Object]).asInstanceOf[String]
      this.currencyName = channelData.getOrDefault("currname", this.currencyName.asInstanceOf[Object]).toString
      this.currencyEmote = channelData.getOrDefault("curremote", this.currencyEmote.asInstanceOf[Object]).toString
      this.followAnnouncement = channelData.getOrDefault("followannouncement", this.followAnnouncement.asInstanceOf[Object]).toString
      this.maxScreenNameLen = channelData.getOrDefault("maxscreennamelen", this.maxScreenNameLen.asInstanceOf[Object]).asInstanceOf[Int]
    }
    val commandCollection = Memebot.db.getCollection(this.channel + "_commands")
    val comms = commandCollection.find()
    comms.forEach(new Block[Document]() {

      override def apply(doc: Document) {
        channelCommands.add(new CommandHandler(channel, doc.getString("command"), null))
      }
    })
  }
}

