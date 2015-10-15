package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.UserHandler

 class HugCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel, command, dbprefix) {
	this.setHelptext("Give people a hug :D")


	override protected def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) = {
		try {
		  channelHandler.sendMessage(String.format("%s hugs %s. HOW CUTE!", sender.toString(), data(0)), this.getChannelOrigin)
		} catch {
		  case e: ArrayIndexOutOfBoundsException => { 
			  channelHandler.sendMessage(sender.toString() + " hugs nobody. How pathetic!", this.getChannelOrigin())
		  }
		}
	}

}
