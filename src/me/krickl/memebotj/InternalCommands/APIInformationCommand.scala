package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.UserHandler

class APIInformationCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel, command, dbprefix) {
  this.setNeededCommandPower(75)

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    channelHandler.sendMessage("Current api connection: " + channelHandler.getApiConnectionIP + " || Private Key for channel: " + channelHandler.getPrivateKey + " || Private key for sender: " + sender.getPrivateKey, this.getChannelOrigin)
  }
}