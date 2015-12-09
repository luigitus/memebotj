package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.UserHandler

class UptimeCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel: String, command: String, dbprefix: String) {
  this.setNeededCommandPower(75)

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    val currentUpTimeSeconds: Int = channelHandler.getStreamStartTime - (System.currentTimeMillis / 1000L).toInt
    channelHandler.sendMessage(f"Uptime: ${currentUpTimeSeconds}", this.getChannelOrigin)
  }
}