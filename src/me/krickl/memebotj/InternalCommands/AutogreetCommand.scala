package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.UserHandler


class AutogreetCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("moderators")

	this.setNeededCommandPower(0)

	this.setHelptext("Syntax: !autogreet <add/remove/get> <name>")

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
			val nameToModify = data(1).toLowerCase()
			var message = ""
			var user: UserHandler = null
			user = if (channelHandler.getUserList.containsKey(nameToModify)) channelHandler.getUserList.get(nameToModify) else new UserHandler(nameToModify,
				this.getChannelOrigin)
			if ((data(0) == "add" || data(0) == "edit") &&
					CommandHandler.checkPermission(sender.getUsername, 25, channelHandler.getUserList)) {
				var newValue = data(2)
				for (x <- 3 until data.length) {
					newValue = newValue + " " + data(x)
				}
				if (!user.newUser) {
					user.setAutogreet(newValue)
					user.writeDBUserData()
					message = "Autogreet added"
				} else {
					message = "Autogreet was not added because this user never entered this chatroom"
				}
			} else if (data(0) == "remove" &&
					CommandHandler.checkPermission(sender.getUsername, 25, channelHandler.getUserList)) {
				if (!user.newUser) {
					user.setAutogreet("")
					user.writeDBUserData()
					message = "Autogreet removed"
				}
			} else if (data(0) == "get") {
				if (!user.newUser) {
					channelHandler.sendMessage("Autogreet for " + user.getUsername + ": " + user.getAutogreet,
						this.getChannelOrigin)
				}
			} else if (data(0) == "toggle") {
				sender.setEnableAutogreets(!sender.getEnableAutogreets)
				channelHandler.sendMessage(sender.getUsername + " autogreet enabled: " +
						java.lang.Boolean.toString(sender.enableAutogreets), this.getChannelOrigin)
			}
			channelHandler.sendMessage(message, this.getChannelOrigin)
		} catch {
			case e: ArrayIndexOutOfBoundsException => e.printStackTrace()
		}
	}
}
