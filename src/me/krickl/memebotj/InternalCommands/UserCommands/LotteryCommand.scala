package me.krickl.memebotj.InternalCommands.UserCommands

import me.krickl.memebotj.Utility.CommandPower
import me.krickl.memebotj._

import scala.util.Random

/**
  * This file is part of memebotj.
  * Created by unlink on 17/02/16.
  */
class LotteryCommand(channel: String, commandName: String = "null", dbprefix: String = null)
  extends CommandHandler(channel, commandName, dbprefix) {
  /** *
    * This method will always be called before the database load and can be used to init a class
    *
    * @param channelHandler
    */
  override def beforeDBLoad(channelHandler: ChannelHandler = null): Unit = {
  }

  /** *
    * This method will always be called after the Database has ben read
    * and can be used to override data saved in the Database.
    * This should be used instead of constructors for child classes
    */
  override def overrideDBData(channelHandler: ChannelHandler = null): Unit = {
    this.setHelptext(Memebot.formatText("LOTTERY_SYNTAX", channelHandler, null, this, true, Array()))
    this.otherData.clear()
  }

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) = {
    if (CommandHandler.hasNeededParameters(data, 1) != -1) {

      if (CommandHandler.checkPermission(sender, CommandPower.mod, channelHandler.userList)) {
        if (data(0) == "start") {
          //check if started
          if (otherData.containsKey("#started#")) {
            channelHandler.sendMessage(Memebot.formatText("LOTTERY_START_ERROR", channelHandler, sender, this, true, Array()))
          } else {
            channelHandler.sendMessage(Memebot.formatText("LOTTERY_START", channelHandler, sender, this, true, Array()))
            otherData.clear()
            listContent.clear()
            otherData.put("#started#", "0")
          }
        } else if (data(0) == "end") {
          if (otherData.containsKey("#started#") && listContent.size() > 0) {
            val ran = new Random()

            val winner = new UserHandler(listContent.get(ran.nextInt(listContent.size())), this.channel)
            otherData.clear()
            listContent.clear()
            // todo number format exception
            winner.setPoints(otherData.get("#started#").toInt)
          } else {
            channelHandler.sendMessage(Memebot.formatText("LOTTERY_END_ERROR", channelHandler, sender, this, true, Array()))
          }
        }
      }

      if (data(0) == "enter") {
        if (!listContent.contains(sender.username)) {
          //is lottery started
          if (!otherData.containsKey("#started#")) {
            channelHandler.sendMessage(Memebot.formatText("LOTTERY_ENTER_NOT_STARTED", channelHandler, sender, this, true, Array()))
          } else {
            channelHandler.sendMessage(Memebot.formatText("LOTTERY_ENTER", channelHandler, sender, this, true, Array()))
            if (this.checkCost(sender, 20, channelHandler)) {
              listContent.add(sender.username)
              sender.setPoints(sender.points - 20)
              //set points
              val newPrice: Int = otherData.get("#started#").toInt + 20
              // todo weird error
              //otherData.put("#started#", newPrice.toString)
            } else {
              channelHandler.sendMessage(Memebot.formatText("LOTTERY_ENTER_ERROR_NO_POINTS", channelHandler, sender, this, true, Array("20")))
            }
          }
        } else {
          channelHandler.sendMessage(Memebot.formatText("LOTTERY_ENTER_ERROR_JOINED", channelHandler, sender, this, true, Array()))
        }
      }
    } else {
      channelHandler.sendMessage(Memebot.formatText("LOTTERY_SYNTAX", channelHandler, null, this, true, Array()))
    }
  }
}
