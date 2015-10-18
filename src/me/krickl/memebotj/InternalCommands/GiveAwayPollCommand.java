package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class GiveAwayPollCommand extends CommandHandler {
	public GiveAwayPollCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("Coming Soon (tm)");
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			if(CommandHandler.checkPermission(sender.getUsername(), 50, channelHandler.getUserList())) {
				if(data[0].equals("add")) {
					this.getListContent().add(data[1] + "#0");
				} else if(data[0].equals("reset")) {
					this.getListContent().clear();
				} else if(data[0].equals("remove")) {
					this.getListContent().remove(Integer.parseInt(data[1]));
				}
			}

			if(data[0].equals("vote")) {
				int voteFor = Integer.parseInt(data[1]);
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			//TODO ca
			e.printStackTrace();
		}

	}

}
