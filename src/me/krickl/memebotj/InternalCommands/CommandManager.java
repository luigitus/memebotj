package me.krickl.memebotj.InternalCommands;

import java.nio.channels.CompletionHandler;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;


public class CommandManager extends CommandHandler {

	public CommandManager(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setNeededCommandPower(0);
		this.setHelptext("Syntax: !command add/remove/edit <command> <param1> ...");
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			if(data[0].equals("add") && CommandHandler.checkPermission(sender.getUsername(), 25, channelHandler.getUserList())) {
				CommandHandler newCommand = new CommandHandler(this.getChannelOrigin(), "null", null);
				if (channelHandler.findCommand(data[1]) == -1) {
					newCommand.editCommand("name", data[1], new UserHandler("#internal#", "#internal#"),
							channelHandler.getUserList());
					newCommand.editCommand("access", "viewers", new UserHandler("#internal#", "#internal#"),
							channelHandler.getUserList());
					String output = data[2];

					for (int i = 3; i < data.length; i++) {
						output = output + " " + data[i];
					}

					newCommand.editCommand("output", output, new UserHandler("#internal#", "#internal#"),
							channelHandler.getUserList());
					channelHandler.sendMessage("Command " + newCommand.getCommand() + " created.", this.getChannelOrigin());

					channelHandler.getChannelCommands().add(newCommand);
				} else {
					channelHandler.sendMessage("This command already exists", this.getChannelOrigin());
				}
			} else if(data[0].equals("remove") && CommandHandler.checkPermission(sender.getUsername(), 25, channelHandler.getUserList())) {
				int j = -1;
				if ((j = channelHandler.findCommand(data[1])) != -1) {

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
			} else if(data[0].equals("edit") && CommandHandler.checkPermission(sender.getUsername(), 25, channelHandler.getUserList())) {
				int j = -1;
				if ((j = channelHandler.findCommand(data[1])) != -1) {
					String newValue = data[3];
					for (int x = 4; x < data.length; x++) {
						newValue = newValue + " " + data[x];
					}

					if (channelHandler.getChannelCommands().get(j).editCommand(data[2], newValue, sender, channelHandler.getUserList())) {
						channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("EDITCOMMAND_OK").replace("{param1}", data[1]).replace("{param2}", data[2]).replace("{param3}", newValue),
								this.getChannelOrigin());
					} else {
						channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("EDITCOMMAND_FAIL"),
								this.getChannelOrigin());
					}
				}
			} else if(data[0].equals("info")) {
                int j = -1;
                if ((j = channelHandler.findCommand(data[1])) != -1) {
                    channelHandler.sendMessage("Times executed: " + Integer.toString(channelHandler.getChannelCommands().get(j).getExecCounter()), this.channelOrigin());
                }
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("COMMANDMANAGER_SYNTAX").replace("{param1}",
					"!command add/remove/edit <command> <param1> ..."), this.getChannelOrigin());
		}
	}
}
