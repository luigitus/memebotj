package me.krickl.memebotj.InternalCommands.ModeratorCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}

class ChannelInfoCommand(channel: String, command: String, dbprefix: String)
		extends CommandHandler(channel, command, dbprefix) {

	this.setAccess("moderators")

	this.setNeededCommandPower(25)

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		channelHandler.sendMessage(f"Is live: ${channelHandler.isLive} || Points per update ${channelHandler.getPointsPerUpdate} || purge regex: ${channelHandler.getUrlRegex}", this.getChannelOrigin)
	}
}
