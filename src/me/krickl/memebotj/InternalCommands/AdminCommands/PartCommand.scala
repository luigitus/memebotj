package me.krickl.memebotj.InternalCommands.AdminCommands

import me.krickl.memebotj.{Memebot, ChannelHandler, CommandHandler, UserHandler}

class PartCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("botadmin")

	this.setNeededCommandPower(50)

	this.setHelptext(Memebot.formatText("PART_SYNTAX", channelOriginHandler, null, this, true, Array()))

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		channelHandler.partChannel(this.getChannelOrigin)
		channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("JOIN"), channelHandler, sender, this, false, Array(sender.getUsername)), this.getChannelOrigin)
	}
}
