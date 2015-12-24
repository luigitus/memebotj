package me.krickl.memebotj.InternalCommands.AdminCommands

import me.krickl.memebotj._
//remove if not needed
import scala.collection.JavaConversions._

class SendMessageCommand(channel: String, command: String, dbprefix: String)
		extends CommandHandler(channel, command, dbprefix) {

	this.setNeededCommandPower(CommandPower.adminAbsolute)

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
