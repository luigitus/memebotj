package me.krickl.memebotj.InternalCommands.UserCommands

import me.krickl.memebotj._
import me.krickl.memebotj.Utility.CommandPower

class RaceCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setHelptext(Memebot.formatText("RACE_SYNTAX", channelOriginHandler, null, this, true, Array()))

	this.enable = false

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		if (data.length >= 1 &&
				CommandHandler.checkPermission(sender, CommandPower.modAbsolute, channelHandler.getUserList)) {
			channelHandler.setCurrentRaceURL(channelHandler.getRaceBaseURL + "/" + channelHandler.getBroadcaster)
			for (i <- data.indices) {
				channelHandler.setCurrentRaceURL(channelHandler.getCurrentRaceURL + "/" + data(i))
			}
			channelHandler.sendMessage(channelHandler.getCurrentRaceURL, this.getChannelOrigin)
		} else {
			channelHandler.sendMessage(channelHandler.getCurrentRaceURL, this.getChannelOrigin)
		}
	}
}
