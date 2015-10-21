package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.UserHandler

@deprecated
class AliasCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel, command, dbprefix) {
  this.setAccess("broadcaster")
  this.setNeededCommandPower(50)
  this.setHelptext("")

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    try {
      if (data(0) == "add") {
        channelHandler.getAliasList.add(data(1))
        channelHandler.sendMessage("Added alias " + data(1), this.getChannelOrigin())
      } else if (data(0) == "remove") {
        if(channelHandler.getAliasList.contains(data(1))) {
          channelHandler.getAliasList.remove(data(1))
          channelHandler.sendMessage("Removed alias " + data(1), this.getChannelOrigin())
        }

      }
    }
    catch {
      case e: ArrayIndexOutOfBoundsException => {
      }
    }
  }
}