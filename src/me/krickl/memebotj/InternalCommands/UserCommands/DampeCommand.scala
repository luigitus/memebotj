package me.krickl.memebotj.InternalCommands.UserCommands

import java.util.Random

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}

class DampeCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
  command, dbprefix) {

  this.setHelptext("Let Dampé hate you for only all of your points")

  this.setUserCooldownLen(40)

  this.setListContent(new java.util.ArrayList[String]())

  this.setPointCost(0)

  this.setNeededCommandPower(0)

  this.setCmdtype("default")

  if(!this.otherData.containsKey("jackpot")) {
    this.otherData.put("jackpot", "0")
  }

  override def overrideDBData(): Unit = {

  }

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    var wage: Double = 20.0f
    try {
      if (data(0) == "jackpot") {
        channelHandler.sendMessage(f"Current jackpot: ${"%.2f".format(this.getJackpot)} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}", this.getChannelOrigin)
        return
      } else if (data(0) == "reset" &&
        CommandHandler.checkPermission(sender.getUsername, 75, channelHandler.getUserList)) {
        this.setJackpot(0)
        channelHandler.sendMessage("Reset jackpot", this.getChannelOrigin)
        return
      }
      wage = data(0).toDouble
      if (wage < 20) {
        channelHandler.sendMessage("Sorry the wage can't be less than 20 " +
          channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE"), this.getChannelOrigin)
        return
      }
    } catch {
      case e: ArrayIndexOutOfBoundsException =>
      case e: NumberFormatException =>
        channelHandler.sendMessage(data(0) + " is not a number", this.getChannelOrigin)
        return
    }
    if (!this.checkCost(sender, wage, channelHandler)) {
      channelHandler.sendMessage(f"${sender.screenName}: Sorry you don't have ${"%.2f".format(wage)} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}.", this.getChannelOrigin)
      return
    }
    if (wage > 10000) {
      channelHandler.sendMessage(s"${sender.screenName}: Sorry the wage can't be more than 10000 " +
        channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE"), this.getChannelOrigin)
      return
    }
    sender.setPoints(sender.points - wage)
    val ran = new Random()
    val range = 1000
    val outcome = ran.nextInt(range) //- wage.toInt / 4)
    if (outcome <= 3) {
      val price = 10 * (Math.sqrt(wage) * 5)
      if(sender.setPoints(sender.points + price + this.getJackpot + wage)) {
        channelHandler.sendMessage(f"${sender.screenName}: Dampé found ${"%.2f".format(price + this.getJackpot)} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}! Damn you're good!", this.getChannelOrigin)
        this.setJackpot(0)
      } else {
        channelHandler.sendMessage(f"${sender.screenName}: Dampé found ${"%.2f".format(price + this.getJackpot)} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}! But your wallet is full so you decide to put it back! Kappa", this.getChannelOrigin)
        this.setJackpot(this.getJackpot + price)
      }
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()

    } else if (outcome <= 50) {
      val price = 10 * (Math.sqrt(wage) * 5)
      if(sender.setPoints(sender.points + price + wage)) {
        channelHandler.sendMessage(f"${sender.screenName}: Dampé found ${"%.2f".format(price)} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}! You lucky bastard!", this.getChannelOrigin)
      } else {
        channelHandler.sendMessage(f"${sender.screenName}: Dampé found ${"%.2f".format(price)} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}! But your wallet is full so you decide to put it back! Kappa", this.getChannelOrigin)
        this.setJackpot(this.getJackpot + price)
      }
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()
    } else if (outcome <= 200) {
      val price = 3 * (Math.sqrt(wage) * 5)
      if(sender.setPoints(sender.points + price + wage)) {
        channelHandler.sendMessage(f"${sender.screenName}: Dampé found ${"%.2f".format(price)} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")} and returned your bet! Pretty good!", this.getChannelOrigin)
      } else {
        channelHandler.sendMessage(f"${sender.screenName}: Dampé found ${"%.2f".format(price)} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}! But your wallet is full so you decide to put it back! Kappa", this.getChannelOrigin)
        this.setJackpot(this.getJackpot + price)
      }
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()
    } else if (outcome <= 450) {
      if(sender.setPoints(sender.points + wage / 2)) {
        channelHandler.sendMessage(f"${sender.screenName}: Dampé is being a dick and returned ${"%.2f".format(wage / 2)} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}!", this.getChannelOrigin)
      } else {
        channelHandler.sendMessage(f"${sender.screenName}: Dampé, being the dick he is, would have returned ${"%.2f".format(wage / 2)} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}, but your wallet is too full and you had to put it back!", this.getChannelOrigin)
        this.setJackpot(this.getJackpot + wage / 2)
      }
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()
      this.setJackpot(this.getJackpot + wage / 2)
    } else if(outcome <= 650) {
      channelHandler.sendMessage(sender.screenName + ": Dampé offered to return your " + channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE") + ", but in order to lighten your wallet you decide to give it to him. Kappa", this.getChannelOrigin)
      sender.getUserCommandCooldowns.get(this.getCommand).startCooldown()
      this.setJackpot(this.getJackpot + wage)
    } else {
      channelHandler.sendMessage(sender.screenName + ": Dampé spent your " + channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE") + " on hookers, booze and crack!", this.getChannelOrigin)
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
