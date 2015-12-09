package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.Memebot
import me.krickl.memebotj.UserHandler
//remove if not needed
import scala.collection.JavaConversions._

class SendMessageCommand(channel: String, command: String, dbprefix: String)
		extends CommandHandler(channel, command, dbprefix) {

	this.setAccess("botadmin")

	this.setNeededCommandPower(75)

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		var msg = ""
		for (s <- data) {
			msg = msg + " " + s
		}
		for (ch <- Memebot.joinedChannels) {
			ch.sendMessage(msg, ch.channel)
		}
	}
}
