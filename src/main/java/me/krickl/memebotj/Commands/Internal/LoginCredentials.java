package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

import java.io.*;

/**
 * This file is part of memebotj.
 * Created by unlink on 20/04/16.
 */
public class LoginCredentials extends CommandHandler {
    public LoginCredentials(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setNeededCommandPower(CommandPower.adminAbsolute);

        this.setHelptext(Memebot.formatText("LOGIN_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));
        this.setFormatData(false);
        setEnabled(true);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            try {
                PrintWriter fop = new PrintWriter(new File(Memebot.memebotDir + "/" + "#" + sender.getUsername() + ".login"));
                fop.println(data[0] + "\n" + data[1]);
                fop.close();
                getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("LOGIN"), getChannelHandler(), sender, this, false, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            } catch(IOException e) {
                log.warning(e.toString());
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            log.info(e.toString());
        }
    }
}
