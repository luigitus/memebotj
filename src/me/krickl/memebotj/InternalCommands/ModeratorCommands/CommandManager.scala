package me.krickl.memebotj.InternalCommands.ModeratorCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}

class CommandManager(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setNeededCommandPower(0)

	this.setHelptext("Syntax: !command add/remove/edit <command> <param1> ...")

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
			if (data(0) == "add" &&
					CommandHandler.checkPermission(sender.getUsername, 25, channelHandler.getUserList)) {
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
					channelHandler.sendMessage("This command already exists", this.getChannelOrigin)
				}
			} else if (data(0) == "remove" &&
					CommandHandler.checkPermission(sender.getUsername, 25, channelHandler.getUserList)) {
				val j = channelHandler.findCommand(data(1))
				if (j != -1) {
					if (!channelHandler.getChannelCommands.get(j).getLocked) {
						channelHandler.sendMessage(channelHandler.getBuiltInStrings.get("DELCOM_OK").replace("{param1}",
							channelHandler.getChannelCommands.get(j).getCommand), this.getChannelOrigin)
						channelHandler.getChannelCommands.get(j).removeDBCommand()
						channelHandler.getChannelCommands.remove(j)
					} else {
						channelHandler.sendMessage("This command has been locked by the broadcaster", this.getChannelOrigin)
					}
				} else {
					channelHandler.sendMessage(channelHandler.getBuiltInStrings.get("DELCOM_NOT_FOUND"), this.getChannelOrigin)
				}
			} else if (data(0) == "edit" &&
					CommandHandler.checkPermission(sender.getUsername, 25, channelHandler.getUserList)) {
				val j = channelHandler.findCommand(data(1))
				if (j != -1) {
					var newValue = data(3)
					for (x <- 4 until data.length) {
						newValue = newValue + " " + data(x)
					}
					if (channelHandler.getChannelCommands.get(j).editCommand(data(2), newValue, sender, channelHandler.getUserList)) {
						channelHandler.sendMessage(channelHandler.getBuiltInStrings.get("EDITCOMMAND_OK")
								.replace("{param1}", data(1))
								.replace("{param2}", data(2))
								.replace("{param3}", newValue), this.getChannelOrigin)
					} else {
						channelHandler.sendMessage(channelHandler.getBuiltInStrings.get("EDITCOMMAND_FAIL"), this.getChannelOrigin)
					}
				}
			} else if(data(0) == "toggleinternal" && CommandHandler.checkPermission(sender.getUsername, 50, channelHandler.getUserList)) {
        val i = channelHandler.findCommand(data(1), channelHandler.internalCommands)
        if(i != -1) {
          val ch = channelHandler.getInternalCommands.get(i)
          if(ch.command == this.command) {
            channelHandler.sendMessage(f"${sender.screenName}: You cannot disable the command manager!", this.getChannelOrigin)
          } else {
            ch.setEnable(!ch.enable)
            channelHandler.sendMessage(f"${sender.screenName}: Toggled internal command ${data(1)}: ${ch.enable}", this.getChannelOrigin)
          }
        }
      } else if (data(0) == "info") {
				val j = channelHandler.findCommand(data(1))
				if (j != -1) {
					channelHandler.sendMessage(f"Times executed: ${channelHandler.getChannelCommands.get(j).getExecCounter.toString}", this.channelOrigin)
				}
			}
		} catch {
			case e: ArrayIndexOutOfBoundsException => channelHandler.sendMessage(channelHandler.getBuiltInStrings.get("COMMANDMANAGER_SYNTAX")
					.replace("{param1}", "!command add/remove/edit <command> <param1> ..."), this.getChannelOrigin)
		}
	}
}
