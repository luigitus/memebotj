package me.krickl.memebotj.InternalCommands.UserCommands

import java.util.Random

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}
//remove if not needed


class FilenameCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
  command, dbprefix) {

  this.setHelptext("Syntax: !name <filename> (100 points/name) || !name get || !name current")

  //this.setListregex("/^[一-龠ぁ-ゔァ-ヴーa-zA-Z0-9_,.-々〆〤]{1,8}$/u")

  this.setPointCost(0)

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    try {
      if (data(0) == "get") {
        if (CommandHandler.checkPermission(sender.getUsername, this.getNeededBroadcasterCommandPower,
          channelHandler.getUserList)) {
          val rand = new Random()
          val index = rand.nextInt(channelHandler.getFileNameList.size - 1)
          channelHandler.setCurrentFileName(channelHandler.getFileNameList.get(index))
          channelHandler.getFileNameList.remove(index)
          channelHandler.sendMessage("Filename: " + channelHandler.getCurrentFileName.split("#")(0) +
            " suggested by " +
            channelHandler.getCurrentFileName.split("#")(1), this.getChannelOrigin)
          return
        }
      } else if (data(0) == "current") {
        channelHandler.sendMessage("Filename: " + channelHandler.getCurrentFileName.split("#")(0) +
          " suggested by " +
          channelHandler.getCurrentFileName.split("#")(1), this.getChannelOrigin)
        return
      } else if (data(0) == "list") {
        channelHandler.sendMessage(s"${channelHandler.getChannelPageBaseURL}/filenames.html", this.channelOrigin)
        return
      } else if (data(0) == "remove" &&
        CommandHandler.checkPermission(sender.getUsername, this.getNeededBroadcasterCommandPower, channelHandler.getUserList)) {
        var counter = 0
        var i = channelHandler.getFileNameList.size - 1
        while (i >= 0) {
          val name = channelHandler.getFileNameList.get(i).split("#")(0)
          if (data(1) == name) {
            channelHandler.getFileNameList.remove(i)
            counter += 1
          }
          i -= 1
        }
        channelHandler.sendMessage(f"Removed $counter names", this.getChannelOrigin)
        return
      }
      var i: Int = 0
      try {
        i = java.lang.Integer.parseInt(data(1))
      } catch {
        case e @ (_: ArrayIndexOutOfBoundsException | _: NumberFormatException) =>
          channelHandler.sendMessage(this.helptext, this.channelOrigin)
          return
      }
      var success = false
      if (data(0).length <= channelHandler.getMaxFileNameLen) {
        if (!this.checkCost(sender, 100.0d * i, channelHandler)) {
          channelHandler.sendMessage(f"${sender.screenName}: Sorry, you don't have ${100f * i} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}",
            this.getChannelOrigin)
        } else {
          if(i > 1)
            channelHandler.sendMessage(f"${sender.screenName} added name ${data(0)} $i times", this.getChannelOrigin)
          else
            channelHandler.sendMessage(f"${sender.screenName} added name ${data(0)}", this.getChannelOrigin)
          sender.setPoints(sender.points - (100 * i))
          success = true
        }
      } else {
        channelHandler.sendMessage(f"${sender.screenName}: The filename cannot be longer than ${channelHandler.maxFileNameLen} characters.", this.getChannelOrigin)
      }
      for (c <- 0 until i if success) {
        channelHandler.getFileNameList.add(data(0) + "#" + sender.getUsername)
      }
      channelHandler.writeDBChannelData()
    } catch {
      case e: ArrayIndexOutOfBoundsException =>
        channelHandler.sendMessage(this.helptext, this.getChannelOrigin)
        e.printStackTrace()
      case e: java.lang.IllegalArgumentException => e.printStackTrace()
    }
  }
}
