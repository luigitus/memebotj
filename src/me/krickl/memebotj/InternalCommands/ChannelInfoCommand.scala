package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.UserHandler

class ChannelInfoCommand(channel: String, command: String, dbprefix: String)
		extends CommandHandler(channel, command, dbprefix) {

	this.setAccess("moderators")

	this.setNeededCommandPower(25)

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		channelHandler.sendMessage(f"Is live: ${channelHandler.isLive} || Points per update ${channelHandler.getPointsPerUpdate} || purge regex: ${channelHandler.getUrlRegex}", this.getChannelOrigin)
	}
}
