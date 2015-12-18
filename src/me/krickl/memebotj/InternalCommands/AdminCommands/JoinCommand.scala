package me.krickl.memebotj.InternalCommands.AdminCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, Memebot, UserHandler}
import scala.collection.JavaConversions._

class JoinCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("botadmin")

	this.setNeededCommandPower(10)

	this.setHelptext(Memebot.formatText("JOIN_SYNTAX", channelOriginHandler, null, this, true, Array()))

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
			if(CommandHandler.checkPermission(sender.getUsername, 75, channelHandler.userList) && data.length > 1) {
        Memebot.joinChannel("#" + data(0))
        channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("JOIN"), channelHandler, sender, this, false, Array(data(0))), this.getChannelOrigin)
      } else if(channelHandler.channel == Memebot.mainChannel) {
        for(channel <- Memebot.joinedChannels) {
          if(channel.channel == "#" + sender.getUsername) {
            channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("JOIN_FAIL"), channelHandler, sender, this, false, Array(sender.getUsername)), this.getChannelOrigin)
            return
          }
        }
        Memebot.joinChannel("#" + sender.getUsername)
        channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("JOIN"), channelHandler, sender, this, false, Array(sender.getUsername)), this.getChannelOrigin)
      }
		} catch {
			case e: ArrayIndexOutOfBoundsException =>
		}
	}
}
