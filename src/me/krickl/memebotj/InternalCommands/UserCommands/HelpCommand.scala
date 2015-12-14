package me.krickl.memebotj.InternalCommands.UserCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}
//remove if not needed
import scala.collection.JavaConversions._

class HelpCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setHelptext("Displays help for other commands")

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
			val j = channelHandler.findCommand(data(0))
			if (j != -1) {
				channelHandler.sendMessage(channelHandler.getChannelCommands.get(j).getHelptext, this.getChannelOrigin)
				return
			}
			for (ch <- channelHandler.getInternalCommands if ch.getCommand == data(0)) {
				if(ch.getHelptext == "null") {
          channelHandler.sendMessage("No help text available!", this.getChannelOrigin)
        }
        else {
          channelHandler.sendMessage(ch.getHelptext, this.getChannelOrigin)
        }
				return
			}
			channelHandler.sendMessage(channelHandler.getBuiltInStrings.get("HELP_NOT_FOUND"), this.getChannelOrigin)
		} catch {
			case e: ArrayIndexOutOfBoundsException => channelHandler.sendMessage(channelHandler.getBuiltInStrings.get("HELP_SYNTAX")
					.replace("{param1}", "!help <command>"), this.getChannelOrigin)
		}
	}
}
