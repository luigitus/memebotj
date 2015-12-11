package me.krickl.memebotj.InternalCommands.FunCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}

 class HugCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel, command, dbprefix) {
	this.setHelptext("Give people a hug :D")


	override protected def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) = {
		try {
		  channelHandler.sendMessage(String.format("%s hugs %s. HOW CUTE!", sender.username, data(0)), this.getChannelOrigin)
		} catch {
		  case e: ArrayIndexOutOfBoundsException => {
			  channelHandler.sendMessage(sender.username + " hugs nobody. How pathetic!", this.getChannelOrigin)
		  }
		}
	}

}
