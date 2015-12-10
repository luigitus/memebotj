package me.krickl.memebotj.InternalCommands.FunCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}

class PyramidCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setNeededCommandPower(75)

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
			var message = data(0)
			val size = java.lang.Integer.parseInt(data(1))
			for (i <- 0 until size) {
				channelHandler.sendMessage(message, this.getChannelOrigin)
				message = message + " " + data(0)
				Thread.sleep(1000)
			}
			var i = size
			while (i >= 0) {
				channelHandler.sendMessage(message, this.getChannelOrigin)
				message = message.substring(0, message.length - data(0).length - 1)
				Thread.sleep(1000)
				i -= 1
			}
		} catch {
			case e: ArrayIndexOutOfBoundsException =>
			case e: NumberFormatException =>
			case e: InterruptedException => e.printStackTrace()
			case e: StringIndexOutOfBoundsException =>
		}
	}
}
