package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;

public class CommandList extends CommandHandler {

	public CommandList(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("Displays a command list");
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		if(Memebot.useWeb()) {
			channelHandler.sendMessage("Commands: " + channelHandler.getChannelPageBaseURL()
						+ "/index.html || Use !help for further information || Documentation: https://github.com/unlink2/memebotj/blob/master/README.md",
						this.getChannelOrigin());
		} else {
			int index = 0;
			String output = "";
			try {
				index = Integer.parseInt(data[0]);
			} catch(ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}

			for(int i = index * 10; i < channelHandler.getChannelCommands().size(); i++) {
				if(i > index * 10 + 10) {
					break;
				}
				output = output + " || " + channelHandler.getChannelCommands().get(i).getCommand();
			}

			channelHandler.sendMessage("Commands: " + output, this.getChannelOrigin());
		}
	}
}
