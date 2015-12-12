package me.krickl.memebotj.InternalCommands.ModeratorCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}
//remove if not needed
import scala.collection.JavaConversions._

class EditUserCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("botadmin")

  this.setHelptext("Syntax: !user <alias/removealias> <nickname> || Mod only: !user <power/modalias/modremovealias> <user> <new power/nickname>")

	this.setNeededCommandPower(10)

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
      var uh: UserHandler = null
      if(channelHandler.getUserList.containsKey(data(1).toLowerCase())) {
        uh = channelHandler.getUserList.get(data(1).toLowerCase())
      } else {
        uh = new UserHandler(data(1).toLowerCase(), channelHandler.getChannel)
      }

      if(uh.newUser) {
        channelHandler.sendMessage("This user never joined this channel: " + data(1), this.getChannelOrigin)
        return
      }

			if (data(0) == "power" && CommandHandler.checkPermission(sender.username, 75, channelHandler.userList)) {
				var success = false
				val newCP = java.lang.Integer.parseInt(data(2))

        if ((newCP + uh.autoCommandPower) > sender.commandPower) {
          channelHandler.sendMessage("You cannot set the command power of a user higher than your own",
            this.getChannelOrigin)
          return
        }
        uh.setCustomCommandPower(java.lang.Integer.parseInt(data(2)))
        uh.setCommandPower(uh.autoCommandPower)
        uh.writeDBUserData()
        success = true

				if (success) {
					channelHandler.sendMessage("Changed user power to " + data(2), this.getChannelOrigin)
				}

			} else if (data(0) == "alias") {
        sender.nickname = data(1)
        channelHandler.sendMessage(f"${sender.screenName}: changed their nickname to ${data(2)}", this.getChannelOrigin)
        sender.writeDBUserData()
      } else if(data(0) == "removealias") {
        sender.nickname = ""
        channelHandler.sendMessage(f"${sender.screenName}: removed their nickname", this.getChannelOrigin)
        sender.writeDBUserData()
      } else if (data(0) == "modalias" && CommandHandler.checkPermission(sender.username, 75, channelHandler.userList)) {
        uh.nickname = data(2)
        channelHandler.sendMessage(f"${sender.screenName}: changed ${uh.username}'s nickname to ${data(2)}", this.getChannelOrigin)
        uh.writeDBUserData()
			} else if(data(0) == "modremovealias" && CommandHandler.checkPermission(sender.username, 75, channelHandler.userList)) {
        uh.nickname = ""
        channelHandler.sendMessage(f"${sender.screenName}: removed ${uh.username}'s nickname", this.getChannelOrigin)
        uh.writeDBUserData()
      }
		} catch {
			case e @ (_: ArrayIndexOutOfBoundsException | _: NumberFormatException) => e.printStackTrace()
		}
	}
}
