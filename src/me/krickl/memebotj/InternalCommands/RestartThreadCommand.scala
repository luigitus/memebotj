package me.krickl.memebotj.InternalCommands

/**
 * Created by unlink on 21/11/15.
 */
class RestartThreadCommand(channel: String, command: String = "null", dbprefix: String = "") extends me.krickl.memebotj.CommandHandler(channel, command, dbprefix) {
  this.setNeededCommandPower(75)

  override def commandScript(sender: me.krickl.memebotj.UserHandler, channelHandler: me.krickl.memebotj.ChannelHandler, data: Array[String]) = {
    channelHandler.sendMessage("Restarting this channels thread now. Please note that this might take up to a minute MrDestructoid", this.getChannelOrigin)
    channelHandler.writeDBChannelData()
    channelHandler.setJoined(false)
  }
}
