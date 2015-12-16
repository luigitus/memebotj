package me.krickl.memebotj.InternalCommands.ModeratorCommands

import me.krickl.memebotj.{Memebot, ChannelHandler, CommandHandler, UserHandler}
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

      if(uh.newUser && CommandHandler.checkPermission(sender.username, 75, channelHandler.userList)) {
        channelHandler.sendMessage(Memebot.formatText("EDIT_USER_NEVER_JOINED", channelHandler, sender, this, true, Array(data(1))), this.getChannelOrigin)
        return
      }

			if (data(0) == "power" && CommandHandler.checkPermission(sender.username, 75, channelHandler.userList)) {
				var success = false
				val newCP = java.lang.Integer.parseInt(data(2))

        if ((newCP + uh._autoCommandPower) > sender._commandPower) {
          channelHandler.sendMessage(Memebot.formatText("EDIT_USER_FAILED_UP", channelHandler, sender, this, true, Array()), this.getChannelOrigin)
          return
        }
        uh.setCustomCommandPower(java.lang.Integer.parseInt(data(2)))
        uh.setCommandPower(uh._autoCommandPower)
        uh.writeDBUserData()
        success = true

				if (success) {
					channelHandler.sendMessage(Memebot.formatText("EDIT_USER_UP", channelHandler, sender, this, true, Array(data(2))), this.getChannelOrigin)
				}

			} else if (data(0) == "alias") {
        sender.nickname = data(1)
        channelHandler.sendMessage(Memebot.formatText("EDIT_ALIAS", channelHandler, sender, this, true, Array(sender.screenName, data(2))), this.getChannelOrigin)
        sender.writeDBUserData()
      } else if(data(0) == "removealias") {
        sender.nickname = ""
        channelHandler.sendMessage(Memebot.formatText("REMOVE_ALIAS", channelHandler, sender, this, true, Array(sender.screenName)), this.getChannelOrigin)
        sender.writeDBUserData()
      } else if (data(0) == "modalias" && CommandHandler.checkPermission(sender.username, 75, channelHandler.userList)) {
        uh.nickname = data(2)
        channelHandler.sendMessage(Memebot.formatText("MOD_EDIT_ALIAS", channelHandler, sender, this, true, Array(sender.screenName, uh.username, data(2))), this.getChannelOrigin)
        uh.writeDBUserData()
			} else if(data(0) == "modremovealias" && CommandHandler.checkPermission(sender.username, 75, channelHandler.userList)) {
        uh.nickname = ""
        channelHandler.sendMessage(Memebot.formatText("MOD_REMOVE_ALIAS", channelHandler, sender, this, true, Array(sender.screenName, uh.username)), this.getChannelOrigin)
        uh.writeDBUserData()
      }
		} catch {
			case e @ (_: ArrayIndexOutOfBoundsException | _: NumberFormatException) => e.printStackTrace()
		}
	}
}
