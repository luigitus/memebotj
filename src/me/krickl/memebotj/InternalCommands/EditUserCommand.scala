package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.Memebot
import me.krickl.memebotj.UserHandler
//remove if not needed
import scala.collection.JavaConversions._

class EditUserCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("botadmin")

	this.setNeededCommandPower(75)

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
			if (data(0) == "power") {
				var success = false
				val newCP = java.lang.Integer.parseInt(data(2))
				for (key <- channelHandler.getUserList.keySet) {
					val uh = channelHandler.getUserList.get(key)
					if (uh.getUsername == data(1)) {
						if ((newCP + uh.autoCommandPower) > sender.commandPower) {
							channelHandler.sendMessage("You cannot set the command power of a user higher than yours",
								this.getChannelOrigin)
							return
						}
						uh.setCustomCommandPower(java.lang.Integer.parseInt(data(2)))
						uh.setCommandPower(uh.autoCommandPower)
						uh.writeDBUserData()
						success = true
					}
				}
				if (!success) {
					val uh = new UserHandler(data(1), channelHandler.getChannel)
					if (!uh.newUser) {
						if ((newCP + uh.autoCommandPower) > sender.commandPower) {
							channelHandler.sendMessage("You cannot set the command power of a user higher than yours",
								this.getChannelOrigin)
							return
						}
						uh.setCustomCommandPower(java.lang.Integer.parseInt(data(2)))
						uh.setCommandPower(uh.autoCommandPower)
						uh.writeDBUserData()
						success = true
					}
				}
				if (success) {
					channelHandler.sendMessage("Changed user power to " + data(2), this.getChannelOrigin)
				} else {
					channelHandler.sendMessage("This user never joined this channel: " + data(1), this.getChannelOrigin)
				}
			} else if (data(0) == "note") {
			}
		} catch {
			case e @ (_: ArrayIndexOutOfBoundsException | _: NumberFormatException) => e.printStackTrace()
		}
	}
}
