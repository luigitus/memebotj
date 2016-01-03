package me.krickl.memebotj.InternalCommands.UserCommands

import java.text.SimpleDateFormat
import java.util.Date

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}

class UptimeCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel: String, command: String, dbprefix: String) {
  this.setNeededCommandPower(75)

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    val currentUpTimeSeconds = System.currentTimeMillis - channelHandler.getStreamStartTime
    val date = new Date(currentUpTimeSeconds)
    val formattedTime = new SimpleDateFormat("HH:mm:ss").format(date)
    channelHandler.sendMessage(f"Uptime: $formattedTime", this.getChannelOrigin)
  }
}