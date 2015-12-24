package me.krickl.memebotj.InternalCommands.UserCommands

import java.security.SecureRandom
import java.util.Random

import me.krickl.memebotj._

class DampeCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
  command, dbprefix) {

  this.setHelptext(Memebot.formatText("DAMPE_SYNTAX", channelOriginHandler, null, this, true, Array()))

  this.setUserCooldownLen(40)

  this.setListContent(new java.util.ArrayList[String]())

  this.setPointCost(0)

  this.setNeededCommandPower(10)

  this.setCmdtype("default")

  if(!this.otherData.containsKey("jackpot")) {
    this.otherData.put("jackpot", "0")
  }

  override def overrideDBData(channelHandler: ChannelHandler = null): Unit = {

  }

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    var wage: Double = 50.0f
    try {
      if (data(0) == "jackpot") {
        channelHandler.sendMessage(Memebot.formatText("DAMPE_JACKPOT", channelHandler, sender, this, true, Array("%.2f".format(this.getJackpot), channelHandler.currencyEmote)), this.getChannelOrigin)
        return
      } else if (data(0) == "set" &&
        CommandHandler.checkPermission(sender.getUsername, CommandPower.adminAbsolute, channelHandler.getUserList)) {
        this.setJackpot(data(1).toDouble)
        channelHandler.sendMessage(Memebot.formatText("DAMPE_SETJACKPOT", channelHandler, sender, this, true, Array()), this.getChannelOrigin)
        return
      }
      wage = data(0).toDouble
      if (wage < channelHandler.pointsPerUpdate * 50) {
        channelHandler.sendMessage(Memebot.formatText("DAMPE_WAGE_FAIL_MIN", channelHandler, sender, this, true, Array("%.2f".format(channelHandler.pointsPerUpdate * 50), channelHandler.currencyEmote)), this.getChannelOrigin)
        return
      }
    } catch {
      case e: ArrayIndexOutOfBoundsException =>
      case e: NumberFormatException =>
        channelHandler.sendMessage(Memebot.formatText("DAMPE_NFE", channelHandler, sender, this, true, Array(data(0))) , this.getChannelOrigin)
        return
    }
    if (!this.checkCost(sender, wage, channelHandler)) {
      channelHandler.sendMessage(Memebot.formatText("DAMPE_OUT_OF_MONEY", channelHandler, sender, this, true, Array(sender.screenName, "%.2f".format(wage), channelHandler.currencyEmote)), this.getChannelOrigin)
      return
    }
    if (wage > channelHandler.maxPoints / 10) {
      channelHandler.sendMessage(Memebot.formatText("DAMPE_WAGE_FAIL_MAX", channelHandler, sender, this, true, Array("%.2f".format(channelHandler.maxPoints / 10))), this.getChannelOrigin)
      return
    }
    sender.setPoints(sender.points - wage)
    val ran = new SecureRandom()
    val range = 1000
    val outcome = ran.nextInt(range) //- wage.toInt / 4)
    if (outcome <= 3) {
      val price = 10 * (Math.sqrt(wage) * 5)
      if(sender.setPoints(sender.points + price + this.getJackpot + wage)) {
        channelHandler.sendMessage(Memebot.formatText("DAMPE_JACKPOT_WON", channelHandler, sender, this, true, Array(sender.screenName, "%.2f".format(price + this.getJackpot), channelHandler.currencyEmote)), this.getChannelOrigin)
        this.setJackpot(0)
      } else {
        channelHandler.sendMessage(Memebot.formatText("DAMPE_JACKPOT_WON_WALLET_FULL", channelHandler, sender, this, true, Array(sender.screenName, "%.2f".format(price + this.getJackpot), channelHandler.currencyEmote)), this.getChannelOrigin)
        this.setJackpot(this.getJackpot + price)
      }
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()

    } else if (outcome <= 50) {
      val price = 10 * (Math.sqrt(wage) * 5)
      if(sender.setPoints(sender.points + price + wage)) {
        channelHandler.sendMessage(Memebot.formatText("DAMPE_WON_1", channelHandler, sender, this, true, Array(sender.screenName, "%.2f".format(price), channelHandler.currencyEmote)), this.getChannelOrigin)
      } else {
        channelHandler.sendMessage(Memebot.formatText("DAMPE_WON_1_WALLET_FULL", channelHandler, sender, this, true, Array(sender.screenName, "%.2f".format(price), channelHandler.currencyEmote)), this.getChannelOrigin)
        this.setJackpot(this.getJackpot + price)
      }
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()
    } else if (outcome <= 200) {
      val price = 3 * (Math.sqrt(wage) * 5)
      if(sender.setPoints(sender.points + price + wage)) {
        channelHandler.sendMessage(Memebot.formatText("DAMPE_WON_2", channelHandler, sender, this, true, Array(sender.screenName, "%.2f".format(price), channelHandler.currencyEmote)), this.getChannelOrigin)
      } else {
        channelHandler.sendMessage(Memebot.formatText("DAMPE_WON_2_WALLET_FULL", channelHandler, sender, this, true, Array(sender.screenName, "%.2f".format(price), channelHandler.currencyEmote)), this.getChannelOrigin)
        this.setJackpot(this.getJackpot + price)
      }
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()
    } else if (outcome <= 450) {
      if(sender.setPoints(sender.points + wage / 2)) {
        channelHandler.sendMessage(Memebot.formatText("DAMPE_LOST_1", channelHandler, sender, this, true, Array("%.2f".format(wage / 2))), this.getChannelOrigin)
      } else {
        channelHandler.sendMessage(Memebot.formatText("DAMPE_LOST_1_WALLET_FULL", channelHandler, sender, this, true, Array("%.2f".format(wage / 2))), this.getChannelOrigin)
        this.setJackpot(this.getJackpot + wage / 2)
      }
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()
      this.setJackpot(this.getJackpot + wage / 2)
    } else if(outcome <= 650) {
      channelHandler.sendMessage(Memebot.formatText("DAMPE_LOST_2", channelHandler, sender, this, true, Array()), this.getChannelOrigin)
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()
      this.setJackpot(this.getJackpot + wage)
    } else if(outcome <= 750) {
      channelHandler.sendMessage(Memebot.formatText("DAMPE_LOST_3", channelHandler, sender, this, true, Array()), this.getChannelOrigin)
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()
      this.setJackpot(this.getJackpot + wage)
    } else if(outcome <= 850) {
      channelHandler.sendMessage(Memebot.formatText("DAMPE_LOST_4", channelHandler, sender, this, true, Array()), this.getChannelOrigin)
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()
      this.setJackpot(this.getJackpot + wage)
    } else {
      channelHandler.sendMessage(Memebot.formatText("DAMPE_LOST_5", channelHandler, sender, this, true, Array()), this.getChannelOrigin)
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()
      this.setJackpot(this.getJackpot + wage)
    }
  }

  def getJackpot: Double = {
    this.otherData.get("jackpot").toDouble
  }

  def setJackpot(newJackpot: Double) {
    this.otherData.put("jackpot", newJackpot.toString)
  }
}
