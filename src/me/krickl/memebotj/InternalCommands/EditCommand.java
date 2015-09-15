package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class EditCommand extends CommandHandler {

	public EditCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("moderators");
		this.setHelptext("Syntax !chmod <command> <name|access|param|cooldown|message|helptext|cmdtype> <new value>");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			int j = -1;
			if ((j = channelHandler.findCommand(data[0])) != -1) {
				String newValue = data[2];
				for (int x = 3; x < data.length; x++) {
					newValue = newValue + " " + data[x];
				}

				if (channelHandler.getChannelCommands().get(j).editCommand(data[1], newValue, sender,
						channelHandler.getUserList())) {
					channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("EDITCOMMAND_OK").replace("{param1}", data[0]).replace("{param2}", data[1]).replace("{param3}", newValue),
							this.getChannelOrigin());
				} else {
					channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("EDITCOMMAND_FAIL"),
							this.getChannelOrigin());
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			channelHandler.sendMessage(
					channelHandler.getBuiltInStrings().get("CHMOD_SYNTAX").replace("{param1}",
							"!chmod <command> <name|access|param|cooldown|message|helptext|cmdtype> <new value>"),
					this.getChannelOrigin());
		}
	}

}
