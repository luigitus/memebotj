package me.krickl.memebotj.InternalCommands.AdminCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}

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
