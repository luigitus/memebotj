package me.krickl.memebotj.api

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.net.UnknownHostException
import java.util.logging.Logger
import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.Memebot
import APIConnectionHandler._

import scala.collection.mutable._

//remove if not needed
import scala.collection.JavaConversions._

object APIConnectionHandler {

	private val log = Logger.getLogger(classOf[APIConnectionHandler].getName)
}

class APIConnectionHandler(port: Int) extends Runnable {

	private var socket: DatagramSocket = null

	private var t: Thread = _

	private val runapi: Boolean = true

	try {
		socket = new DatagramSocket(port)
	} catch {
		case e: SocketException => e.printStackTrace()
	}

	def sendData(data: String, ip: String, port: Int, channel: ChannelHandler) {
		var dataBytes = Array.ofDim[Byte](1024)
    var IPAddress: InetAddress = null
    try {
      IPAddress = InetAddress.getByName(ip)
    } catch {
      case e: UnknownHostException => IPAddress = InetAddress.getLocalHost
    }
		try {
			dataBytes = data.getBytes
			val packet = new DatagramPacket(dataBytes, dataBytes.length, IPAddress, port)
			socket.send(packet)
		} catch {
			case e: IOException => e.printStackTrace()
		}
		log.info("<API> IP: " + ip + ":" + java.lang.Integer.toString(port) + " Channel:  " + channel + ">>" + data)
	}

	def receiveData(): Array[String] = {
		val data = Array.ofDim[Byte](1024)
		val dataReturn = Array.ofDim[String](3)
		val packet = new DatagramPacket(data, data.length)
		try {
			socket.receive(packet)
			dataReturn(0) = new String(packet.getData)
			dataReturn(1) = packet.getAddress.toString
			dataReturn(2) = java.lang.Integer.toString(packet.getPort)
		} catch {
			case e: IOException => e.printStackTrace()
		}
		log.info("<API> IP: " + dataReturn(1) + ":" + dataReturn(2) + ">>" + dataReturn(0))
		dataReturn
	}

	override def run() {
		log.info("API running")
		while (this.runapi) {
			val data = this.receiveData()
			val message = data(0)
			val ip = data(1)
			val port = java.lang.Integer.parseInt(data(2))
			val buffer = message.split("::")
      val listedMessage = new HashMap[String, String]()
			for (i <- 0 until buffer.length) {
        try {
          val msg: String = buffer(i).split("=")(0)
          val data: String = buffer(i).split("=")(1)
          listedMessage.put(msg, data)
        } catch {
          case e: java.lang.ArrayIndexOutOfBoundsException => e.printStackTrace()
        }
			}
			var success = false
      try {
        if (listedMessage.get("request").get == "handshake") {
          if (listedMessage.get("pkey").get == Memebot.apiMasterKey) {
            this.sendData("pkey=apisource::sender=apisource::request=hello::message=Access Granted", ip, port, null)
            success = true
          }
          for (ch <- Memebot.joinedChannels if ch.getPrivateKey == listedMessage.get("pkey").toString) {
            ch.setApiConnectionIP(ip)
            this.sendData("pkey=apisource::sender=apisource::request=hello::message=Access Granted", ip, port, ch)
            success = true
          }
        } else if (listedMessage.get("request").get == "ping") {
          this.sendData("pkey=apisource::sender=apisource::request=ping::message=Pong", ip, port, null)
          success = true
        } else if (listedMessage.get("request").get == "commands") {
          var channelCommands = ""
          var internalCommands = ""
          for (ch <- Memebot.joinedChannels) {
            if (ch.getChannel == listedMessage.get("channel").get) {
              for (cmd <- ch.getChannelCommands) {
                channelCommands = channelCommands + cmd.getCommand() + ";"
              }

              for (cmd <- ch.getInternalCommands) {
                internalCommands = internalCommands + cmd.getCommand() + ";"
              }
            }
          }
          this.sendData("pkey=apisource::sender=apisource::request=commands::message=Commands::channelcmds=" + channelCommands + "::internalcmds=" + internalCommands, ip, port, null)
          success = true
        }
      } catch {
        case e: java.util.NoSuchElementException => this.sendData("pkey=apisource::sender=apisource::request=invalid", ip, port, null)
      }
			if (!success) {
				this.sendData("pkey=apisource::sender=apisource::request=invalid::message=Connection Failed", ip, port, null)
			}
		}
	}

	def start() {
		if (t == null) {
			t = new Thread(this, "api.thread")
			t.start()
		}
	}
}
