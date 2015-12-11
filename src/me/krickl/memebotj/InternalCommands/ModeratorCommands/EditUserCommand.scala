package me.krickl.memebotj.InternalCommands.ModeratorCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}
//remove if not needed
import scala.collection.JavaConversions._

class EditUserCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("botadmin")

	this.setNeededCommandPower(75)

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		try {
      var uh: UserHandler = null
      if(channelHandler.getUserList.containsKey(data(1).toLowerCase())) {
        uh = channelHandler.getUserList.get(data(1).toLowerCase())
      } else {
        uh = new UserHandler(data(1).toLowerCase(), channelHandler.getChannel)
      }

      if(uh.newUser) {
        channelHandler.sendMessage("This user never joined this channel: " + data(1), this.getChannelOrigin)
        return
      }

			if (data(0) == "power") {
				var success = false
				val newCP = java.lang.Integer.parseInt(data(2))

        if ((newCP + uh.autoCommandPower) > sender.commandPower) {
          channelHandler.sendMessage("You cannot set the command power of a user higher than your own",
            this.getChannelOrigin)
          return
        }
        uh.setCustomCommandPower(java.lang.Integer.parseInt(data(2)))
        uh.setCommandPower(uh.autoCommandPower)
        uh.writeDBUserData()
        success = true

				if (success) {
					channelHandler.sendMessage("Changed user power to " + data(2), this.getChannelOrigin)
				}

			} else if (data(0) == "alias") {
        uh.nickname = data(2)
        channelHandler.sendMessage(f"${sender.screenName}: changed ${uh.username}'s nickname to ${data(2)}", this.getChannelOrigin)
        uh.writeDBUserData()
			} else if(data(0) == "removealias") {
        uh.nickname = ""
        channelHandler.sendMessage(f"${sender.screenName}: removed ${uh.username}'s nickname", this.getChannelOrigin)
        uh.writeDBUserData()
      }
		} catch {
			case e @ (_: ArrayIndexOutOfBoundsException | _: NumberFormatException) => e.printStackTrace()
		}
	}
}
