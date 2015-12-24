package me.krickl.memebotj.InternalCommands.AdminCommands

import me.krickl.memebotj._

class PartCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setNeededCommandPower(CommandPower.broadcasterAbsolute)

	this.setHelptext(Memebot.formatText("PART_SYNTAX", channelOriginHandler, null, this, true, Array()))

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		channelHandler.partChannel(this.getChannelOrigin)
		channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("JOIN"), channelHandler, sender, this, false, Array(sender.getUsername)), this.getChannelOrigin)
	}
}
