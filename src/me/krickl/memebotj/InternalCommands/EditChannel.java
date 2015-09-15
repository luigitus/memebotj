package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class EditChannel extends CommandHandler {

	public EditChannel(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("broadcaster");
		this.setHelptext("");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			String newEntry = "";
			for (int x = 1; x < data.length; x++) {
				newEntry = newEntry + " " + data[x];
			}

			if (data[0].equals("race")) {
				channelHandler.setRaceBaseURL(data[1]);
			} else if (data[0].equals("otherch")) {
				if (data[1].equals("add")) {
					channelHandler.getOtherLoadedChannels().add(data[2]);
				} else if (data[1].equals("remove")) {
					int index = -1;
					for (int x = 0; x < channelHandler.getOtherLoadedChannels().size(); x++) {
						if (channelHandler.getOtherLoadedChannels().get(x).equals(data[2])) {
							index = x;
						}
					}
					if (index >= 0) {
						channelHandler.getOtherLoadedChannels().remove(index);
					}
				}
			} else {
				if (channelHandler.getBuiltInStrings().containsKey(data[0])) {
					channelHandler.getBuiltInStrings().put(data[0], newEntry);
				} else {
					channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("CHCHANNEL_SYNTAX")
							.replace("{param1}", "coming soon"), this.getChannelOrigin());
				}
			}

			channelHandler.sendMessage("Changed channel settings.", this.getChannelOrigin());
		} catch (ArrayIndexOutOfBoundsException e) {
			channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("CHCHANNEL_SYNTAX"),
					this.getChannelOrigin());
		}
	}
}
