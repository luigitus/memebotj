package me.krickl.memebotj.InternalCommands.AdminCommands

import me.krickl.memebotj.Utility.CommandPower
import me.krickl.memebotj._

class QuitCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
  command, dbprefix) {

  this.setNeededCommandPower(CommandPower.adminAbsolute)

  this.setUnformattedOutput(Memebot.formatText("QUIT", channelOriginHandler, null, this, true, Array()))

  this.setHelptext(Memebot.formatText("QUIT_SYNTAX", channelOriginHandler, null, this, true, Array()))

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    System.exit(0)
  }
}
