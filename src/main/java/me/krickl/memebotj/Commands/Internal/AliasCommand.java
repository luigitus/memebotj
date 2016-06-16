package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.User.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

/**
 * Created by unlink on 6/5/2016.
 */
public class AliasCommand extends CommandHandler {

    public AliasCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setNeededCommandPower(CommandPower.adminAbsolute);
        this.setHelptext(Memebot.formatText("ALIAS_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            if (data[0].equals("add")) {
                String aliasName = data[1];
                String original = "";

                for (int i = 2; i < data.length; i++) {
                    original = original + data[i] + " ";
                }

                getChannelHandler().getAliasList().put(aliasName, original);
                String message = Memebot.formatText("ALIAS_ADDED", getChannelHandler(), sender, this, true, new String[]{}, "");
                getChannelHandler().sendMessage(message, getChannelHandler().getChannel(), sender, false);
            } else if (data[0].equals("remove")) {
                if (getChannelHandler().getAliasList().containsKey(data[1])) {
                    getChannelHandler().getAliasList().remove(data[1]);
                }
                String message = Memebot.formatText("ALIAS_REMOVED", getChannelHandler(), sender, this, true, new String[]{}, "");
                getChannelHandler().sendMessage(message, getChannelHandler().getChannel(), sender, false);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            String message = Memebot.formatText("ALIAS_ERROR", getChannelHandler(), sender, this, true, new String[]{}, "");
            getChannelHandler().sendMessage(message, getChannelHandler().getChannel(), sender, false);
        }
    }
}
