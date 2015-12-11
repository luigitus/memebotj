package me.krickl.memebotj.InternalCommands.UserCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, Memebot, UserHandler}

class CommandList(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
  command, dbprefix) {

  this.setHelptext("Displays a command list")

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    try {
      if (Memebot.useWeb) {
        channelHandler.sendMessage("Commands: " + channelHandler.getChannelPageBaseURL +
          "/index.html || Use !help for further information", this.getChannelOrigin)
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
      case e: NumberFormatException => {
        channelHandler.sendMessage("That is not an integer 4Head", this.getChannelOrigin)
        e.printStackTrace()
      }
    }
  }
}
