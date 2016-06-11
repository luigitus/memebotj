package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.ChatColours;
import me.krickl.memebotj.Utility.CommandPower;
import me.krickl.memebotj.Utility.Localisation;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class EditChannelCommand extends CommandHandler {
    public EditChannelCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setNeededCommandPower(CommandPower.broadcasterAbsolute);

        this.setHelptext("");
    }

    @Override
    public void commandScript(UserHandler sender, String data[]) {
        try {
            String newEntry = "";
            for (int x = 1; x < data.length; x++) {
                newEntry = newEntry + data[x] + " ";
            }

            if (data[0].equals("race")) {
                getChannelHandler().setRaceBaseURL(data[1]);
            } else if (data[0].equals("otherchannel")) {
                if (data[1].equals("add")) {
                    getChannelHandler().getOtherLoadedChannels().add(data[2].toLowerCase());
                } else if (data[1].equals("remove")) {
                    int index = -1;
                    for (int x = 0; x < getChannelHandler().getOtherLoadedChannels().size(); x++) {
                        if (getChannelHandler().getOtherLoadedChannels().get(x).toLowerCase().equals(data[2].toLowerCase())) {
                            index = x;
                        }
                    }
                    if (index >= 0) {
                        getChannelHandler().getOtherLoadedChannels().remove(index);
                    }
                } else if (data[1].equals("clear")) {
                    getChannelHandler().getOtherLoadedChannels().clear();
                }
            } else if (data[0].equals("allowautogreet")) {
                getChannelHandler().setAllowAutogreet(!getChannelHandler().isAllowAutogreet());
                getChannelHandler().sendMessage(String.format("Autogreet set to %s", java.lang.Boolean.toString(getChannelHandler().isAllowAutogreet())), getChannelHandler().getChannel());
            } else if (data[0].equals("maxnamelen")) {
                getChannelHandler().setMaxFileNameLen(java.lang.Short.parseShort(data[1]));
                getChannelHandler().sendMessage("Changed max filename length to " + data[1], getChannelHandler().getChannel());
            } else if (data[0].equals("ppi")) {
                getChannelHandler().setPointsPerUpdate(java.lang.Double.parseDouble(data[1]));
                getChannelHandler().sendMessage("Changed max ppi to " + data[1], getChannelHandler().getChannel());
            } else if (data[0].equals("purgelinks")) {
                getChannelHandler().setPurgeURLS(java.lang.Boolean.parseBoolean(data[1]));
            } else if (data[0].equals("linkto")) {
                getChannelHandler().setLinkTimeout(java.lang.Integer.parseInt(data[1]));
            } else if (data[0].equals("silent")) {
                getChannelHandler().setSilentMode(java.lang.Boolean.parseBoolean(data[1]));
            } else if (data[0].equals("allowgreet")) {
                getChannelHandler().setAllowAutogreet(Boolean.parseBoolean(data[1]));
            } else if (data[0].equals("maxpoints")) {
                getChannelHandler().setMaxPoints(Double.parseDouble(data[1]));
            } else if (data[0].equals("local")) {
                getChannelHandler().setLocal(data[1]);
                getChannelHandler().setLocalisation(new Localisation(getChannelHandler().getLocal()));
            } else if (data[0].equals("curremote")) {
                getChannelHandler().setCurrencyEmote(data[1]);
            } else if (data[0].equals("currname")) {
                getChannelHandler().setCurrencyEmote(data[1]);
            } else if (data[0].equals("tax")) {
                getChannelHandler().setPointsTax(Double.parseDouble(data[1]));
            } else if (data[0].equals("maxname")) {
                getChannelHandler().setMaxAmountOfNameInList(Integer.parseInt(data[1]));
            } else if (data[0].equals("background")) {
                getChannelHandler().setBgImage(data[1]);
            } else if (data[0].equals("itemDrops")) {
                getChannelHandler().setItemDrops(data[1]);
            } else if(data[0].equals("colour")) {
                if(!getChannelHandler().getConnection().getBotNick().equals(Memebot.botNick)
                        || checkPermissions(sender, CommandPower.botModAbsolute, CommandPower.botModAbsolute)) {
                    ChatColours.setColour(getChannelHandler(), sender, data[1]);
                }
            } else if(data[0].equals("colourrotation")) {
                getChannelHandler().setUseRotatingColours(Boolean.parseBoolean(data[1]));
            } else if(data[0].equals("setoverride")) {
                getChannelHandler().setOverrideChannelInformation(Boolean.parseBoolean(data[1]));
            } else if(data[0].equals("overridetitle")) {
                getChannelHandler().setStreamTitle(newEntry);
            } else if(data[0].equals("overridegame")) {
                getChannelHandler().setCurrentGame(newEntry);
            } else if(data[0].equals("setlive")) {
                getChannelHandler().setLive(Boolean.parseBoolean(data[1]));
            }

            getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("EDIT_CHANNEL_OK"), getChannelHandler(), sender, this, false, new String[]{sender.getUsername(), data[0], data[1]}, getChannelHandler().getChannel()));
            getChannelHandler().writeDB();
        } catch (ArrayIndexOutOfBoundsException e) {
            getChannelHandler().sendMessage(Memebot.formatText("CHCHANNEL_SYNTAX", getChannelHandler(), sender, this, true, new String[]{}, ""), getChannelHandler().getChannel());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
