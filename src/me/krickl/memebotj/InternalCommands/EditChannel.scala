package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.UserHandler
//remove if not needed

class EditChannel(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("broadcaster")

	this.setNeededCommandPower(50)

	this.setHelptext("")

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
			var newEntry = ""
			for (x <- 1 until data.length) {
				newEntry = newEntry + " " + data(x)
			}
			if (data(0) == "race") {
				channelHandler.setRaceBaseURL(data(1))
			} else if (data(0) == "otherch") {
				if (data(1) == "add") {
					channelHandler.getOtherLoadedChannels.add(data(2))
				} else if (data(1) == "remove") {
					var index = -1
					for (x <- 0 until channelHandler.getOtherLoadedChannels.size if channelHandler.getOtherLoadedChannels.get(x) == data(2)) {
						index = x
					}
					if (index >= 0) {
						channelHandler.getOtherLoadedChannels.remove(index)
					}
				}
			} else if (data(0) == "allowautogreet") {
				channelHandler.setAllowAutogreet(!channelHandler.isAllowAutogreet)
				channelHandler.sendMessage(String.format("Autogreet set to %s", java.lang.Boolean.toString(channelHandler.isAllowAutogreet)),
					this.getChannelOrigin)
			} else if (data(0) == "maxnamelen") {
				channelHandler.setMaxFileNameLen(java.lang.Integer.parseInt(data(1)))
				channelHandler.sendMessage("Changed max filename length to " + data(1), this.getChannelOrigin)
			} else if (data(0) == "ppi") {
				channelHandler.setPointsPerUpdate(java.lang.Double.parseDouble(data(1)))
				channelHandler.sendMessage("Changed max ppi to " + data(1), this.getChannelOrigin)
			} else if (data(0) == "purgelinks") {
				channelHandler.setPurgeURLS(java.lang.Boolean.parseBoolean(data(1)))
			} else if (data(0) == "purgelinksnu") {
				channelHandler.setPurgeURLSNewUsers(java.lang.Boolean.parseBoolean(data(1)))
			} else if (data(0) == "linkto") {
				channelHandler.setLinkTimeout(java.lang.Integer.parseInt(data(1)))
			} else if (data(0) == "urlregex") {
				if (data(1) == "append") {
					channelHandler.setUrlRegex(channelHandler.getUrlRegex + data(2))
				} else {
					channelHandler.setUrlRegex(data(1))
				}
			} else if (data(0) == "silent") {
				channelHandler.setSilentMode(java.lang.Boolean.parseBoolean(data(1)))
			} else if (data(0) == "preventspam") {
				channelHandler.setSpamPrevention(java.lang.Boolean.parseBoolean(data(1)))
			} else if (data(0) == "spamtimeout") {
				channelHandler.setSpamTimeout(java.lang.Integer.parseInt(data(1)))
			} else {
				if (channelHandler.getBuiltInStrings.containsKey(data(0))) {
					channelHandler.getBuiltInStrings.put(data(0), newEntry)
				} else {
					channelHandler.sendMessage(channelHandler.getBuiltInStrings.get("CHCHANNEL_SYNTAX")
							.replace("{param1}", "!channel <option> <new value>"), this.getChannelOrigin)
				}
			}
			channelHandler.sendMessage("Changed channel settings.", this.getChannelOrigin)
			channelHandler.writeDBChannelData()
		} catch {
			case e: ArrayIndexOutOfBoundsException => channelHandler.sendMessage(channelHandler.getBuiltInStrings.get("CHCHANNEL_SYNTAX"),
				this.getChannelOrigin)
			case e: NumberFormatException =>
		}
	}
}
