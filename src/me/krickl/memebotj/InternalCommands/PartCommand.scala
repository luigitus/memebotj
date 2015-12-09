package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.UserHandler

class PartCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("botadmin")

	this.setNeededCommandPower(75)

	this.setHelptext("Syntax: !mepart")

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		channelHandler.partChannel(this.getChannelOrigin)
	}
}
