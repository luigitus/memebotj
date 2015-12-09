package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.UserHandler

class QuitCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("botadmin")

	this.setNeededCommandPower(75)

	this.setUnformattedOutput("You have been terminated MrDestructoid")

	this.setHelptext("Quits the bot")

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		System.exit(0)
	}
}
