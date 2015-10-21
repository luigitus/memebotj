package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.CommandHandler

class DebugCommand(channel: String, command: String = "null", dbprefix: String = "") extends me.krickl.memebotj.CommandHandler(channel, command, dbprefix) {
  this.setNeededCommandPower(75)

  override def commandScript(sender: me.krickl.memebotj.UserHandler, channelHandler: me.krickl.memebotj.ChannelHandler, data: Array[String]) = {
    channelHandler.sendMessage("Hello This is scala", this.getChannelOrigin)
  }
}