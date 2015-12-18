package me.krickl.memebotj.InternalCommands.UserCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, Memebot, UserHandler}

class CommandList(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
  command, dbprefix) {

  this.setHelptext(Memebot.formatText("COMMAND_LIST_SYNTAX", channelOriginHandler, null, this, true, Array()))

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    try {
      if (Memebot.useWeb) {
        channelHandler.sendMessage(Memebot.formatText("COMMAND_LIST", channelHandler, sender, this, true, Array(channelHandler.getChannelPageBaseURL + "/index.html")), this.getChannelOrigin)
      } else {
        var index = 0
        var output = ""
        try {
          index = java.lang.Integer.parseInt(data(0))
        } catch {
          case e: ArrayIndexOutOfBoundsException => e.printStackTrace()
          case e: NumberFormatException => e.printStackTrace()
        }
        for (i <- index * 10 until channelHandler.getChannelCommands.size) {
          if (i > index * 10 + 10) {
            //break
          }
          output = output + " || " +
            channelHandler.getChannelCommands.get(i).getCommand
        }
        channelHandler.sendMessage("Commands: " + output, this.getChannelOrigin)
      }
    } catch {
      case e: NumberFormatException =>
        channelHandler.sendMessage(Memebot.formatText("COMMAND_LIST_ERROR", channelHandler, sender, this, true, Array()), this.getChannelOrigin)
        e.printStackTrace()
    }
  }
}
