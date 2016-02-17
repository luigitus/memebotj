package me.krickl.memebotj.InternalCommands.ModeratorCommands

import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.{Paths, Files}

import me.krickl.memebotj._
import me.krickl.memebotj.Utility.CommandPower

class ChannelInfoCommand(channel: String, command: String, dbprefix: String)
		extends CommandHandler(channel, command, dbprefix) {

	this.setNeededCommandPower(CommandPower.adminAbsolute)

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
      if(data(0) == "reload") {
        Memebot.urlBanList = Files.readAllLines(Paths.get(Memebot.memebotDir + "/urlblacklist.cfg"),
          Charset.defaultCharset()).asInstanceOf[java.util.ArrayList[String]]

        Memebot.phraseBanList = Files.readAllLines(Paths.get(Memebot.memebotDir + "/phrasebanlist.cfg"),
          Charset.defaultCharset()).asInstanceOf[java.util.ArrayList[String]]

        Memebot.globalBanList = Files.readAllLines(Paths.get(Memebot.memebotDir + "/globalbanlist.cfg"),
          Charset.defaultCharset()).asInstanceOf[java.util.ArrayList[String]]

        channelHandler.sendMessage(f"Reloaded global ban lists. There are ${Memebot.urlBanList.size()} banned urls, ${Memebot.phraseBanList.size()} banned phrases and ${Memebot.globalBanList.size()} blacklisted users.")

      }
    } catch {
      case e: ArrayIndexOutOfBoundsException =>
        channelHandler.sendMessage(f"Is live: ${channelHandler.isLive} || Points per update ${channelHandler.getPointsPerUpdate} || Game: ${channelHandler.currentGame} || ${channelHandler.followAnnouncement}", this.getChannelOrigin)
      case e: IOException =>
        e.printStackTrace()
        channelHandler.sendMessage(e.toString)
    }
	}
}
