package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class AddCommandHandler extends CommandHandler {

	public AddCommandHandler(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("moderators");
		this.setHelptext("Syntax: !addcommand <command> <output>");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			CommandHandler newCommand = new CommandHandler(this.getChannelOrigin());
			if (channelHandler.findCommand(data[0]) == -1) {
				newCommand.editCommand("name", data[0], new UserHandler("#internal#", "#internal#"),
						channelHandler.getUserList());
				newCommand.editCommand("access", "viewers", new UserHandler("#internal#", "#internal#"),
						channelHandler.getUserList());
				String output = "";

				for (int i = 1; i < data.length; i++) {
					output = output + " " + data[i];
				}

				newCommand.editCommand("output", output, new UserHandler("#internal#", "#internal#"),
						channelHandler.getUserList());
				channelHandler.sendMessage("Command " + newCommand.getCommand() + " created.", this.getChannelOrigin());

				channelHandler.getChannelCommands().add(newCommand);
			} else {
				channelHandler.sendMessage("This command already exists", this.getChannelOrigin());
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("ADDCOM_SYNTAX").replace("{param1}",
					"!meaddcom <command> <text>"), this.getChannelOrigin());
		}
	}

}
