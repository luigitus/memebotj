package me.krickl.memebotj.InternalCommands.ModeratorCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}

class WhoisCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setHelptext("Syntax: !whois <name>")

	this.setNeededCommandPower(25)

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
			val user = data(0).toLowerCase()
			var uh: UserHandler = null
			uh = if (channelHandler.getUserList.containsKey(user)) channelHandler.getUserList.get(user) else new UserHandler(user,
				this.getChannelOrigin)
			var isCat = false
			if (user.contains("cat")) {
				isCat = true
			}
			channelHandler.sendMessage(uh.getUsername + " || Broadcaster: " + java.lang.Boolean.toString(uh.isUserBroadcaster) +
					" || Mod: " +
					java.lang.Boolean.toString(uh.isModerator) +
					" || Command Power: " +
					java.lang.Integer.toString(uh.commandPower) +
					" || Timeouts: " +
					java.lang.Integer.toString(uh.getTimeouts) +
					" || Is known user: " +
					java.lang.Boolean.toString(!uh.newUser) +
					" || Date joined: " +
					uh.getDateJoined +
					" || Is user a cat: " +
					java.lang.Boolean.toString(isCat), this.getChannelOrigin)
		} catch {
			case e: ArrayIndexOutOfBoundsException => e.printStackTrace()
		}
	}
}
