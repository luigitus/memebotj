package me.krickl.memebotj.InternalCommands;

import java.util.Random;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class FilenameCommand extends CommandHandler {

	public FilenameCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("Syntax: !name <filename> (100 points/name) || !name get || !name current");
        this.setListregex("/^[一-龠ぁ-ゔァ-ヴーa-zA-Z0-9_,.-々〆〤]{1,8}$/u"); //todo not matching - make sure default value is only set once when the default works
        this.setPointCost(0);
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			if (data[0].equals("get")) {
				if (CommandHandler.checkPermission(sender.getUsername(), this.getNeededBroadcasterCommandPower(),
						channelHandler.getUserList())) {
					Random rand = new Random();
					int index = rand.nextInt(channelHandler.getFileNameList().size() - 1);
					channelHandler.setCurrentFileName(channelHandler.getFileNameList().get(index));

					// TODO if name is too long reroll - this allows to have
					// names of more than one length in the list
					// TODO try getting a good name a few times. if it fails
					// return a name that is longer than max len

					channelHandler.getFileNameList().remove(index);
					channelHandler.sendMessage("Filename: " + channelHandler.getCurrentFileName().split("#")[0] + " suggested by " + channelHandler.getCurrentFileName().split("#")[1],
                            this.getChannelOrigin());

					return;
				}
			} else if (data[0].equals("current")) {
				channelHandler.sendMessage("Filename: " + channelHandler.getCurrentFileName().split("#")[0]
						+ " suggested by " + channelHandler.getCurrentFileName().split("#")[1],
						this.getChannelOrigin());
				return;
			} else if(data[0].equals("list")) {
                channelHandler.sendMessage(channelHandler.getChannelPageBaseURL() + "/filenames.html", this.channelOrigin());
                return;
            }
            int i = 1;
            try {
                i = Integer.parseInt(data[1]);
            } catch(ArrayIndexOutOfBoundsException e) {
                i = 1;
            } catch (NumberFormatException e) {
                i = 1;
            }
            boolean success = false;
            //todo make regex match
            if (data[0].matches(this.listregex()) || data[0].length() <= channelHandler.getMaxFileNameLen()) {
                if (!this.checkCost(sender, 100.0d * (i + 1), channelHandler)) {
                    channelHandler.sendMessage(String.format("Sorry, you don't have %.2f %s", 100f * (i + 1),
                            channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE")), this.getChannelOrigin());
                } else {
                    channelHandler.sendMessage(String.format("%s added name %s %s times", sender.getUsername(), data[0], Integer.toString(i)), this.getChannelOrigin());

                    sender.setPoints(sender.getPoints() - (100 * (i + 1)));
                    success = true;
                }
            } else {
                channelHandler.sendMessage("The filename does not match: " + this.getListregex(), this.getChannelOrigin());
            }

            for(int c = 0; c < i; c++) {
                if (success) {
                    channelHandler.getFileNameList().add(data[0] + "#" + sender.getUsername());
                }
            }

			channelHandler.writeDBChannelData();
		} catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
		} catch (java.lang.IllegalArgumentException e) {
            e.printStackTrace();
		}
	}

}
