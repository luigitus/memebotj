package me.krickl.memebotj.InternalCommands.FunCommands

import java.security.SecureRandom

import me.krickl.memebotj.{Cooldown, UserHandler, ChannelHandler, CommandHandler}

import scala.util.Random

/**
  * Created by unlink on 29/12/15.
  */
class CategoryGeneratorCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
  command, dbprefix) {

  override def beforeDBLoad(channelHandler: ChannelHandler = null): Unit = {

  }

  override def overrideDBData(channelHandler: ChannelHandler = null): Unit = {
    this.cooldown = new Cooldown(60)
    this.neededCommandPower = 25

    this.listContent.clear()
    this.listContent.add("any%")
    this.listContent.add("oob")
    this.listContent.add("no")
    this.listContent.add("im")
    this.listContent.add("ww")
    this.listContent.add("im/ww")
    this.listContent.add("isg")
    this.listContent.add("lanky kong")
    this.listContent.add("only")
    this.listContent.add("trials")
    this.listContent.add("100%")
    this.listContent.add("masks")
    this.listContent.add("all")
    this.listContent.add("chests")
    this.listContent.add("rupee")
    this.listContent.add("bomb chu")
    this.listContent.add("bomb")
    this.listContent.add("goron race")
    this.listContent.add("bottle")
    this.listContent.add("bertha")
    this.listContent.add("VCheater")
    this.listContent.add("max")
    this.listContent.add("first cycle")
    this.listContent.add("ben%")
    this.listContent.add("dank%")
    this.listContent.add("pink%")
    this.listContent.add("fly barrel")
    this.listContent.add("beetle race")
    this.listContent.add("skip")
    this.listContent.add("z button")
    this.listContent.add("x button")
    this.listContent.add("y button")
    this.listContent.add("only")
    this.listContent.add("cs")
    this.listContent.add("skip")
    this.listContent.add("low%")
    this.listContent.add("arcade")
    this.listContent.add("nintendo coin")
    this.listContent.add("rare coin")
    this.listContent.add("ganonless")
    this.listContent.add("3 heart challange")
    this.listContent.add("ice arrow")
    this.listContent.add("swordless")
    this.listContent.add("shieldless")
    this.listContent.add("no doors")
    this.listContent.add("bottle on b")
    this.listContent.add("fire")
    this.listContent.add("FrankerZ race")
    this.listContent.add("no reset")
    this.listContent.add("challange")
    this.listContent.add("max%")
    this.listContent.add("101")
    this.listContent.add("102")
    this.listContent.add("103")
    this.listContent.add("no reset")
    this.listContent.add("no save and quit")
    this.listContent.add("deku nuts")
    this.listContent.add("diddy baloons")
    this.listContent.add("ique")

  }

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]): Unit = {
    var lenght = 3
    var category = ""
    try {
      lenght = data(0).toInt
    } catch {
      case e: ArrayIndexOutOfBoundsException =>
        e.printStackTrace()
      case e: NumberFormatException =>
        e.printStackTrace()
    }

    for(i <- 0 until lenght) {
      val ran = new SecureRandom()
      category = category + " " + this.listContent.get(ran.nextInt(this.listContent.size()))
    }

    channelHandler.sendMessage(category)
  }
}
