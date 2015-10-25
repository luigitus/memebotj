package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.BuildInfo
import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.Memebot
import me.krickl.memebotj.UserHandler

class AboutCommand(channel: String,command: String, dbprefix: String) extends CommandHandler(channel, command, dbprefix) {
  this.setHelptext("");

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) = {
		channelHandler.sendMessage("memebotj version " + BuildInfo.version + " build " + BuildInfo.buildNumber
				+ ". Developed by " + BuildInfo.dev, this.getChannelOrigin())
		channelHandler.sendMessage("Fork me RitzMitz : https://github.com/unlink2/memebotj",
				this.getChannelOrigin())
	}
}
