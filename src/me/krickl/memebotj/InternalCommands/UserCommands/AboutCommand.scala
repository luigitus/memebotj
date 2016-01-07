package me.krickl.memebotj.InternalCommands.UserCommands

import me.krickl.memebotj._

class AboutCommand(channel: String,command: String, dbprefix: String) extends CommandHandler(channel, command, dbprefix) {
  this.setHelptext("")
  this.setUserCooldownLen(600)

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) = {
		channelHandler.sendMessage(BuildInfo.appName + " version " + BuildInfo.version + ". Developed by " + BuildInfo.dev, this.getChannelOrigin)
		channelHandler.sendMessage("Fork me RitzMitz : https://github.com/unlink2/memebotj")
    if(Memebot.isTwitchBot) {
      channelHandler.sendMessage(f"Get me here: http://www.twitch.tv/${Memebot.mainChannel.replace("#", "")}/chat - just type !mejoin in chat! :)", this.getChannelOrigin)
    } else {
      channelHandler.sendMessage(f"Get me here: ${Memebot.mainChannel} - just type !mejoin in chat! :)", this.getChannelOrigin)
    }
	}
}
