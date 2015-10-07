package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

@Deprecated
public class DeletCommandHandler extends CommandHandler {

	public DeletCommandHandler(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("moderators");
		this.setNeededCommandPower(25);
		this.setHelptext("Syntax: !deletecommand <command>");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			int j = -1;
			if ((j = channelHandler.findCommand(data[0])) != -1) {

				if (!channelHandler.getChannelCommands().get(j).isLocked()) {
					channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("DELCOM_OK").replace("{param1}",
							channelHandler.getChannelCommands().get(j).getCommand()), this.getChannelOrigin());
					channelHandler.getChannelCommands().get(j).removeDBCommand();
					channelHandler.getChannelCommands().remove(j);
				} else {
					channelHandler.sendMessage("This command has been locked by the broadcaster",
							this.getChannelOrigin());
				}

			} else {
				channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("DELCOM_NOT_FOUND"),
						this.getChannelOrigin());
			}
			channelHandler.sendMessage("This command is deprecated! Use !command instead.", this.getChannelOrigin());
		} catch (ArrayIndexOutOfBoundsException e) {
			channelHandler.sendMessage(
					channelHandler.getBuiltInStrings().get("DELCOM_SYNTAX").replace("{param1}", "!medelcom <command>"),
					this.getChannelOrigin());
		}
	}

}
