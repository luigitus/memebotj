package me.krickl.memebotj.InternalCommands.AdminCommands

import me.krickl.memebotj.Memebot

/**
 * Created by unlink on 21/11/15.
 */
class RestartThreadCommand(channel: String, command: String = "null", dbprefix: String = "") extends me.krickl.memebotj.CommandHandler(channel, command, dbprefix) {
  this.setNeededCommandPower(75)

  override def commandScript(sender: me.krickl.memebotj.UserHandler, channelHandler: me.krickl.memebotj.ChannelHandler, data: Array[String]) = {
    channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("RESTART"), channelHandler, sender, this, false, Array()), this.getChannelOrigin)
    channelHandler.writeDBChannelData()
    channelHandler.setIsJoined(false)
  }
}
