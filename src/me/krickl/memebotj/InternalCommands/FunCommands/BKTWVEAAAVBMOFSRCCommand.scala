package me.krickl.memebotj.InternalCommands.FunCommands

import me.krickl.memebotj.InternalCommands.UserCommands.SpeedrunCommand

/**
 * Created by unlink on 16/11/15.
 */
class BKTWVEAAAVBMOFSRCCommand(channel: String, command: String = "null", dbprefix: String = "") extends SpeedrunCommand(channel, command, dbprefix) {
  this.setQuotePrefix("The best known time with video evidence as approved and verified by members of the speed running community")
  this.enable = false

  override def commandScript(sender: me.krickl.memebotj.UserHandler, channelHandler: me.krickl.memebotj.ChannelHandler, data: Array[String]) = {
    super.commandScript(sender, channelHandler, data)
  }
}
