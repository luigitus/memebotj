package me.krickl.memebotj.InternalCommands.AdminCommands

class DebugCommand(channel: String, command: String = "null", dbprefix: String = "") extends me.krickl.memebotj.CommandHandler(channel, command, dbprefix) {
  this.setNeededCommandPower(75)


  override def commandScript(sender: me.krickl.memebotj.UserHandler, channelHandler: me.krickl.memebotj.ChannelHandler, data: Array[String]) = {
    channelHandler.sendMessage("Congraturations. This glitch is happy end and you have prooven the justive of our coding! You are great debugger !!!", this.getChannelOrigin)
  }
}