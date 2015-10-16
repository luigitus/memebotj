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

object ConnectionHandler {
  final var log: Logger = Logger.getLogger(ConnectionHandler.getClass.getName())
}

class ConnectionHandler(serverNew: String, portNew: Int, botNickNew: String, passwordNew: String) {
	private var server: String = serverNew
	private var botNick: String = botNickNew
	private var password: String = passwordNew
	private var port: Int = portNew
	private var ircSocket: Socket = _
	private var inFromServer: BufferedReader = _
	private var outToServer: DataOutputStream = _
  var debugMode: Boolean = false 

	try {
		this.ircSocket = new Socket(server, port)
	} catch {
	  case e: UnknownHostException => {
		  e.printStackTrace()
	  }
	}
	if(ircSocket != null) {
		this.inFromServer = new BufferedReader(new InputStreamReader(this.ircSocket.getInputStream(), "UTF-8"))
	
	  this.outToServer = new DataOutputStream(this.ircSocket.getOutputStream())
	
		ConnectionHandler.log.info(f"Connectiong to server $server with username $botNick on $port\n")
	
		this.outToServer.writeBytes("PASS " + this.password + "\n")
		this.outToServer.writeBytes("NICK " + this.botNick + "\n")
	} else {
		debugMode = true
	}
	  
	@throws[IOException]()
	def ping() {
	  ConnectionHandler.log.info("Responding to ping request!")

		this.outToServer.writeBytes("PING :PONG\n")
	}

	@throws[IOException]()
  def recvData(): Array[String] = {
	  var ircmsg = ""
		if(this.debugMode) {
			var input = new Scanner(System.in)
			ircmsg = input.nextLine()
		} else {
			ircmsg = this.inFromServer.readLine().replace("\n", "").replace("\r", "")
		}
		var channel = ""
		var hashIndex = ircmsg.indexOf(" #")

		if (hashIndex > 0) {
			breakable { for(i <- hashIndex + 1 to ircmsg.length() - 1) {
				if (ircmsg.charAt(i) != ' ') {
					channel = channel + ircmsg.charAt(i)
				} else {
					break
				}
			} }
		}

		ConnectionHandler.log.info("<" + channel + "> " + ircmsg)

		if (ircmsg.contains("PING :")) {
			this.ping()
		}

		var returnArray: Array[String] = Array(channel, ircmsg)
		return returnArray
	}

	def close() = {
		try {
			this.outToServer.close()
			this.inFromServer.close()
			this.ircSocket.close()
		} catch {
			// TODO Auto-generated catch block
			case  e: IOException => {
				e.printStackTrace()
			}
		}
	}

  def getBotNick(): String = {
		return botNick
	}

	def getInFromServer(): BufferedReader = {
		return inFromServer
	}

	def getOutToServer(): DataOutputStream = {
		return outToServer
	}
}
