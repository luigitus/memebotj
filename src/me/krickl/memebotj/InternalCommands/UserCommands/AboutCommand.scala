package me.krickl.memebotj.InternalCommands.UserCommands

import me.krickl.memebotj.{BuildInfo, ChannelHandler, CommandHandler, UserHandler}

class AboutCommand(channel: String,command: String, dbprefix: String) extends CommandHandler(channel, command, dbprefix) {
  this.setHelptext("")

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) = {
		channelHandler.sendMessage("memebotj version " + BuildInfo.version
				+ ". Developed by " + BuildInfo.dev, this.getChannelOrigin)
		channelHandler.sendMessage("Fork me RitzMitz : https://github.com/unlink2/memebotj",
				this.getChannelOrigin)
	}
}
