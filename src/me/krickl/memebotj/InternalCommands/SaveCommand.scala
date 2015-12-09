package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.Memebot
import me.krickl.memebotj.UserHandler
//remove if not needed
import scala.collection.JavaConversions._

class SaveCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("botadmin")

	this.setNeededCommandPower(75)

	this.setHelptext("Saves everything to database")

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		channelHandler.sendMessage("Saving all the things Keepo", this.getChannelOrigin)
		for (ch <- Memebot.joinedChannels) {
			ch.writeDBChannelData()
		}
	}
}
