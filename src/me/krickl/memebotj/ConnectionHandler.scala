package me.krickl.memebotj

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import java.net.UnknownHostException
import java.util.Scanner
import java.util.logging.Logger
import scala.util.control.Breaks._
import scala.collection.JavaConversions._

object ConnectionHandler {
  final var log: Logger = Logger.getLogger(ConnectionHandler.getClass.getName)
}

class ConnectionHandler(serverNew: String, portNew: Int, botNickNew: String, passwordNew: String) {
	val server: String = serverNew
	val botNick: String = botNickNew
	val password: String = passwordNew
	val port: Int = portNew
	var ircSocket: Socket = _
	var inFromServer: BufferedReader = _
	var outToServer: DataOutputStream = _
  var debugMode: Boolean = false

	try {
		this.ircSocket = new Socket(server, port)
	} catch {
	  case e: UnknownHostException =>
		  e.printStackTrace()
	}
	if(ircSocket != null) {
		this.inFromServer = new BufferedReader(new InputStreamReader(this.ircSocket.getInputStream, "UTF-8"))

	  this.outToServer = new DataOutputStream(this.ircSocket.getOutputStream)

		ConnectionHandler.log.info(f"Connectiong to server $server with username $botNick on $port\n")

		this.outToServer.writeBytes("PASS " + this.password + "\n")
		this.outToServer.writeBytes("NICK " + this.botNick + "\n")
    this.sendMessageBytes("CAP REQ :twitch.tv/membership\n")
    this.sendMessageBytes("CAP REQ :twitch.tv/commands\n")
    this.sendMessageBytes("CAP REQ :twitch.tv/tags\n")

	} else {
		debugMode = true
	}

	@throws[IOException]()
	def ping() {
		this.outToServer.writeBytes("PING :PONG\n")
	}

	@throws[IOException]()
  def recvData(): Array[String] = {
	  var ircmsg = ""
		if(this.debugMode) {
			val input = new Scanner(System.in)
			ircmsg = input.nextLine()
		} else {
			ircmsg = this.inFromServer.readLine().replace("\n", "").replace("\r", "")
		}
		var channel = ""
		val hashIndex = ircmsg.indexOf(" #")

		if (hashIndex > 0) {
			breakable { for(i <- hashIndex + 1 to ircmsg.length() - 1) {
				if (ircmsg.charAt(i) != ' ') {
					channel = channel + ircmsg.charAt(i)
				} else {
					break()
				}
			} }
		}

		ConnectionHandler.log.info("<" + channel + "> " + ircmsg)

		if (ircmsg.contains("PING :")) {
			this.ping()
		}

		val returnArray: Array[String] = Array(channel, ircmsg)
		returnArray
	}

  /***
    * This method handles the parsing of IRC messages. It is part of the connectionhandler to make
    * memebot adaptable to new protocols in the future
    * @param rawircmsg
    * @param channelHandler
    * @return
    */
	def handleMessage(rawircmsg: String, channelHandler: ChannelHandler): MessagePackage = {
    var senderName = ""
    val ircTags = new java.util.HashMap[String, String]()
    var msgContent: Array[String] = null
    val ircmsgBuffer = rawircmsg.split(" ")
    var messageType = "UNDEFINED"
    var i = 0
    i = 0
    while (i < ircmsgBuffer.length) {
      val msg = ircmsgBuffer(i)
      if ((msg == "PRIVMSG" || msg == "MODE" || msg == "PART" || msg == "JOIN" || msg == "CLEARCHAT") && messageType == "UNDEFINED") {
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
    if(!channelHandler.userList.containsKey(senderName) && !senderName.isEmpty) {
      val newUser = new UserHandler(senderName, channelHandler.channel)
      channelHandler.userList.put(senderName, newUser)
    }

    val sender = channelHandler.userList.get(senderName)
    //todo make sure sender is not removed
    sender.shouldBeRemoved = false

    if (messageType != "PRIVMSG") {
      val ircmsgList = rawircmsg.split(" ")
      if (ircmsgList(1) == null) {
        return null
      }
      if (ircmsgList(1) == "MODE") {
        var user: UserHandler = null
        if (!channelHandler.userList.containsKey(ircmsgList(4))) {
          user = new UserHandler(ircmsgList(4), channelHandler.channel)
          channelHandler.userList.put(senderName, user)
        } else {
          user = channelHandler.userList.get(ircmsgList(4))
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
          //todo mark user for removal 5 minutes after part message
          if (channelHandler.userList.containsKey(sender.getUsername)) {
            //this.userList.get(sender.getUsername).writeDBUserData()
            //this.userList.remove(sender.getUsername)
            sender.shouldBeRemoved = true
            sender.removeCooldown = new Cooldown(300)
          }
        }
      } else if (ircmsgList(1) == "JOIN") {
        if (sender != null) {
          //send autogreet if the channel allows autogreets
          if (channelHandler.allowAutogreet && sender.getAutogreet != "") {
            sender.sendAutogreet(channelHandler)
          }
        }
      } else if (ircmsgList(1) == "CLEARCHAT") {
        try {
          if (channelHandler.userList.containsKey(ircmsgList(3).replace(":", ""))) {
            channelHandler.userList.get(ircmsgList(3).replace(":", "")).setTimeouts(channelHandler.userList.get(ircmsgList(3).replace(":", "")).getTimeouts + 1)
            channelHandler.userList.get(ircmsgList(3).replace(":", "")).writeDBUserData()
          } else {
            val uh = new UserHandler(ircmsgList(3).replace(":", ""), channelHandler.channel)
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
      if (sender.getUsername.equalsIgnoreCase(channelHandler.broadcaster)) {
        sender.setIsModerator(true)
        sender.isUserBroadcaster = true
        sender.setCommandPower(CommandPower.broadcasterAbsolute)
      }
      for (user <- Memebot.botAdmins) {
        if (user.equalsIgnoreCase(sender.getUsername)) {
          sender.setCommandPower(CommandPower.adminAbsolute)
        }
      }
    }

    new MessagePackage(msgContent, sender, messageType)
	}

	def close() = {
		try {
			this.outToServer.close()
			this.inFromServer.close()
			this.ircSocket.close()
		} catch {
			case  e: IOException =>
				e.printStackTrace()
		}
	}

	def sendMessage(msg: String): Unit = {
    outToServer.flush()
    outToServer.write(msg.getBytes("UTF-8"))
  }

  def sendMessageBytes(msg: String): Unit = {
    outToServer.flush()
    outToServer.writeBytes(msg)
  }
}
