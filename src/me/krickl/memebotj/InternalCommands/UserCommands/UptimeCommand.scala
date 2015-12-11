package me.krickl.memebotj.InternalCommands.UserCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}

class UptimeCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel: String, command: String, dbprefix: String) {
  this.setNeededCommandPower(75)

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    val currentUpTimeSeconds: Int = channelHandler.getStreamStartTime - (System.currentTimeMillis / 1000L).toInt
    channelHandler.sendMessage(f"Uptime: ${currentUpTimeSeconds}", this.getChannelOrigin)
  }
}