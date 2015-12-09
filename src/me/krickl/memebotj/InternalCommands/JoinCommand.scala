package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.Memebot
import me.krickl.memebotj.UserHandler

class JoinCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("botadmin")

	this.setNeededCommandPower(75)

	this.setHelptext("Syntax: !mejoin <channel>")

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
			Memebot.joinChannel("#" + data(0))
			channelHandler.sendMessage("Joined channel " + data(0), this.getChannelOrigin)
		} catch {
			case e: ArrayIndexOutOfBoundsException =>
		}
	}
}
