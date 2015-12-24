package me.krickl.memebotj.InternalCommands.ModeratorCommands

import me.krickl.memebotj._

class CommandManager(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setNeededCommandPower(0)

	this.setHelptext("Syntax: !command add/remove/edit <command> <param1> ...")

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
			if (data(0) == "add" && CommandHandler.checkPermission(sender.getUsername, CommandPower.modAbsolute, channelHandler.getUserList)) {
				val newCommand = new CommandHandler(this.getChannelOrigin, "null", null)
				if (channelHandler.findCommand(data(1)) == -1) {
					newCommand.editCommand("name", data(1), new UserHandler("#internal#", "#internal#"), channelHandler.getUserList)
					newCommand.editCommand("access", "viewers", new UserHandler("#internal#", "#internal#"), channelHandler.getUserList)
					var output = data(2)
					for (i <- 3 until data.length) {
						output = output + " " + data(i)
					}
					newCommand.editCommand("output", output, new UserHandler("#internal#", "#internal#"), channelHandler.getUserList)
					channelHandler.sendMessage("Command " + newCommand.getCommand + " created.", this.getChannelOrigin)
					channelHandler.getChannelCommands.add(newCommand)
				} else {
					channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("COMMAND_EXISTS"), channelHandler, sender, this, true, Array()), this.getChannelOrigin)
				}
			} else if (data(0) == "remove" &&
					CommandHandler.checkPermission(sender.getUsername, CommandPower.modAbsolute, channelHandler.getUserList)) {
				val j = channelHandler.findCommand(data(1))
				if (j != -1) {
					if (!channelHandler.getChannelCommands.get(j).getLocked) {
						channelHandler.sendMessage(Memebot.formatText("DELCOM_OK", channelHandler, sender, this, true, Array(channelHandler.getChannelCommands.get(j).getCommand)), this.getChannelOrigin)

						channelHandler.getChannelCommands.get(j).removeDBCommand()
						channelHandler.getChannelCommands.remove(j)
					} else {
						channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("DELCOM_LOCKED"), channelHandler, sender, this, true, Array()), this.getChannelOrigin)
					}
				} else {
					channelHandler.sendMessage(Memebot.formatText("DELCOM_NOT_FOUND", channelHandler, sender, this, true), this.getChannelOrigin)
				}
			} else if (data(0) == "edit" &&
					CommandHandler.checkPermission(sender.getUsername, CommandPower.modAbsolute, channelHandler.getUserList)) {
				val j = channelHandler.findCommand(data(1))
				if (j != -1) {
					var newValue = data(3)
					for (x <- 4 until data.length) {
						newValue = newValue + " " + data(x)
					}
					if (channelHandler.getChannelCommands.get(j).editCommand(data(2), newValue, sender, channelHandler.getUserList)) {
						channelHandler.sendMessage(Memebot.formatText("EDITCOMMAND_OK", channelHandler, sender, this, true, Array(data(1), data(2), newValue)), this.getChannelOrigin)
					} else {
						channelHandler.sendMessage(Memebot.formatText("EDITCOMMAND_FAIL", channelHandler, sender, this, true), this.getChannelOrigin)
					}
				}
			} else if(data(0) == "toggleinternal" && CommandHandler.checkPermission(sender.getUsername, CommandPower.broadcasterAbsolute, channelHandler.getUserList)) {
        val i = channelHandler.findCommand(data(1), channelHandler.internalCommands)
        if(i != -1) {
          val ch = channelHandler.getInternalCommands.get(i)
          if(ch.command == this.command) {
            channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("COMMAND_DISABLE_FAILED"), channelHandler, sender, this, false, Array(sender.screenName)), this.getChannelOrigin)
          } else {
            ch.setEnable(!ch.enable)
            channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("COMMAND_TOGGLE"), channelHandler, sender, this, false, Array(sender.screenName, data(1), f"${ch.enable}")), this.getChannelOrigin)
          }
        }
      } else if (data(0) == "info") {
				var j = channelHandler.findCommand(data(1))
				if (j != -1) {
					channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("COMMAND_TIMES_EXECUTED"), channelHandler, sender, this, false, Array(f"${channelHandler.getChannelCommands.get(j).getExecCounter}", f"${channelHandler.getChannelCommands.get(j).getNeededCommandPower}", f"${channelHandler.getChannelCommands.get(j).getNeededCommandPower + CommandPower.mod}", f"${channelHandler.getChannelCommands.get(j).getNeededCommandPower + CommandPower.broadcaster}", f"${channelHandler.getChannelCommands.get(j).getNeededCommandPower + CommandPower.admin}")), this.channelOrigin)
				}

        j = channelHandler.findCommand(data(1), channelHandler.internalCommands)
        if(j!= -1) {
          channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("COMMAND_TIMES_EXECUTED"), channelHandler, sender, this, false, Array(f"${channelHandler.getInternalCommands.get(j).getExecCounter}", f"${channelHandler.getInternalCommands.get(j).getNeededCommandPower}", f"${channelHandler.getInternalCommands.get(j).getNeededCommandPower + CommandPower.mod}", f"${channelHandler.getInternalCommands.get(j).getNeededCommandPower + CommandPower.broadcaster}", f"${channelHandler.getInternalCommands.get(j).getNeededCommandPower + CommandPower.admin}")), this.channelOrigin)
        }
			}
		} catch {
			case e: ArrayIndexOutOfBoundsException => channelHandler.sendMessage(this.helptext, this.getChannelOrigin)
		}
	}
}
