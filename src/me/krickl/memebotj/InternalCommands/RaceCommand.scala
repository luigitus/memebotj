package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.UserHandler

class RaceCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("moderators")

	this.setHelptext("Syntax: !race <channel1> <channel2> <channel3> ... || !race")

	this.enable = false

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		if (data.length >= 1 &&
				CommandHandler.checkPermission(sender.getUsername, this.getNeededModCommandPower, channelHandler.getUserList)) {
			channelHandler.setCurrentRaceURL(channelHandler.getRaceBaseURL + "/" + channelHandler.getBroadcaster)
			for (i <- 0 until data.length) {
				channelHandler.setCurrentRaceURL(channelHandler.getCurrentRaceURL + "/" + data(i))
			}
			channelHandler.sendMessage(channelHandler.getCurrentRaceURL, this.getChannelOrigin)
		} else {
			channelHandler.sendMessage(channelHandler.getCurrentRaceURL, this.getChannelOrigin)
		}
	}
}
