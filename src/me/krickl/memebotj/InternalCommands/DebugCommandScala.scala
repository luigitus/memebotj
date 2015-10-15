package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj._

class DebugCommandScala(channel: String, command: String, dbprefix: String) extends CommandHandler(channel, command, dbprefix) {
  this.setNeededCommandPower(75)
  
  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) = {
    channelHandler.sendMessage("Hello This is scala", this.getChannelOrigin)
  }
}