package me.krickl.memebotj.InternalCommands;

import com.sun.org.apache.xpath.internal.operations.Bool;
import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class EditChannel extends CommandHandler {

	public EditChannel(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("broadcaster");
		this.setNeededCommandPower(50);
		this.setHelptext("");
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
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
			} else if (data[0].equals("allowautogreet")) {
				channelHandler.setAllowAutogreet(!channelHandler.isAllowAutogreet());
				channelHandler.sendMessage(
						String.format("Autogreet set to %s", Boolean.toString(channelHandler.isAllowAutogreet())),
						this.getChannelOrigin());
			} else if (data[0].equals("maxnamelen")) {
				channelHandler.setMaxFileNameLen(Integer.parseInt(data[1]));
				channelHandler.sendMessage("Changed max filename length to " + data[1], this.getChannelOrigin());
			} else if (data[0].equals("ppi")) {
				channelHandler.setPointsPerUpdate(Double.parseDouble(data[1]));
				channelHandler.sendMessage("Changed max ppi to " + data[1], this.getChannelOrigin());
			} else if (data[0].equals("purgelinks")) {
				channelHandler.setPurgeURLS(Boolean.parseBoolean(data[1]));
			} else if (data[0].equals("purgelinksnu")) {
				channelHandler.setPurgeURLSNewUsers(Boolean.parseBoolean(data[1]));
			} else if (data[0].equals("linkto")) {
				channelHandler.setLinkTimeout(Integer.parseInt(data[1]));
			} else if (data[0].equals("urlregex")) {
				if (data[1].equals("append")) {
					channelHandler.setUrlRegex(channelHandler.getUrlRegex() + data[2]);
				} else {
					channelHandler.setUrlRegex(data[1]);
				}
			} else if(data[0].equals("silent")) {
				channelHandler.setSilentMode(Boolean.parseBoolean(data[1]));
			} else if(data[0].equals("preventspam")) {
                channelHandler.setSpamPrevention(Boolean.parseBoolean(data[1]));
			}else if(data[0].equals("spamtimeout")) {
                channelHandler.setSpamTimeout(Integer.parseInt(data[1]));
            } else {
				if (channelHandler.getBuiltInStrings().containsKey(data[0])) {
					channelHandler.getBuiltInStrings().put(data[0], newEntry);
				} else {
					channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("CHCHANNEL_SYNTAX")
							.replace("{param1}", "!channel <option> <new value>"), this.getChannelOrigin());
				}
			}

			channelHandler.sendMessage("Changed channel settings.", this.getChannelOrigin());
			channelHandler.writeDBChannelData();
		} catch (ArrayIndexOutOfBoundsException e) {
			channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("CHCHANNEL_SYNTAX"),
					this.getChannelOrigin());
		} catch (NumberFormatException e) {

		}
	}
}
