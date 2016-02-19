package me.krickl.memebotj.InternalCommands.ModeratorCommands

import me.krickl.memebotj.Utility.CommandPower
import me.krickl.memebotj._

//remove if not needed

class EditUserCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
  command, dbprefix) {

  this.setHelptext(Memebot.formatText("EDIT_USER_SYNTAX", channelOriginHandler, null, this, true, Array()))

  this.setNeededCommandPower(CommandPower.viewer)

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    var uh: UserHandler = null

    try {
      if (channelHandler.getUserList.containsKey(data(1).toLowerCase())) {
        uh = channelHandler.getUserList.get(data(1).toLowerCase())
      } else {
        uh = new UserHandler(data(1).toLowerCase(), channelHandler.getChannel)
      }

      if (uh.newUser && CommandHandler.checkPermission(sender, CommandPower.adminAbsolute, channelHandler.userList)) {
        channelHandler.sendMessage(Memebot.formatText("EDIT_USER_NEVER_JOINED", channelHandler, sender, this, true, Array(data(1))), this.getChannelOrigin)
        return
      }
    } catch {
      case e: ArrayIndexOutOfBoundsException =>
        e.printStackTrace()
    }
    try {
      if (data(0) == "power" && CommandHandler.checkPermission(sender, CommandPower.adminAbsolute, channelHandler.userList)) {
        var success = false
        val newCP = java.lang.Integer.parseInt(data(2))

        if ((newCP + uh._autoCommandPower) > sender._commandPower) {
          channelHandler.sendMessage(Memebot.formatText("EDIT_USER_FAILED_UP", channelHandler, sender, this, true, Array()), this.getChannelOrigin)
          return
        }
        uh.setCustomCommandPower(java.lang.Integer.parseInt(data(2)))
        uh.setCommandPower(uh._autoCommandPower)
        uh.writeDBUserData()
        success = true

        if (success) {
          channelHandler.sendMessage(Memebot.formatText("EDIT_USER_UP", channelHandler, sender, this, true, Array(data(2))), this.getChannelOrigin)
        }

      } else if (data(0) == "alias") {
        if (data(1).length > channelHandler.maxScreenNameLen) {
          channelHandler.sendMessage(Memebot.formatText("EDIT_ALIAS_FAIL", channelHandler, sender, this, true, Array(sender.screenName, channelHandler.maxScreenNameLen.toString)), this.getChannelOrigin)
        } else {
          sender.nickname = data(1)
          channelHandler.sendMessage(Memebot.formatText("EDIT_ALIAS", channelHandler, sender, this, true, Array(sender.screenName, data(2))), this.getChannelOrigin)
          sender.writeDBUserData()
        }
      } else if (data(0) == "removealias") {
        sender.nickname = ""
        channelHandler.sendMessage(Memebot.formatText("REMOVE_ALIAS", channelHandler, sender, this, true, Array(sender.screenName)), this.getChannelOrigin)
        sender.writeDBUserData()
      } else if (data(0) == "modalias" && CommandHandler.checkPermission(sender, CommandPower.adminAbsolute, channelHandler.userList)) {
        uh.nickname = data(2)
        channelHandler.sendMessage(Memebot.formatText("MOD_EDIT_ALIAS", channelHandler, sender, this, true, Array(sender.screenName, uh.username, data(2))), this.getChannelOrigin)
        uh.writeDBUserData()
      } else if (data(0) == "modremovealias" && CommandHandler.checkPermission(sender, CommandPower.adminAbsolute, channelHandler.userList)) {
        uh.nickname = ""
        channelHandler.sendMessage(Memebot.formatText("MOD_REMOVE_ALIAS", channelHandler, sender, this, true, Array(sender.screenName, uh.username)), this.getChannelOrigin)
        uh.writeDBUserData()
      }
    } catch {
      case e@(_: ArrayIndexOutOfBoundsException | _: NumberFormatException) => e.printStackTrace()
    }
  }
}
