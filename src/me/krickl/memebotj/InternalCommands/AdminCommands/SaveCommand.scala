package me.krickl.memebotj.InternalCommands.AdminCommands

import me.krickl.memebotj._
import me.krickl.memebotj.Utility.CommandPower

//remove if not needed
import scala.collection.JavaConversions._

class SaveCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setNeededCommandPower(CommandPower.adminAbsolute)

	this.setHelptext(Memebot.formatText("SAVE_SYNTAX", channelOriginHandler, null, this, true, Array()))

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("SAVE"), channelHandler, sender, this, false, Array()), this.getChannelOrigin)
		for (ch <- Memebot.joinedChannels) {
			ch.writeDBChannelData()
		}
	}
}
