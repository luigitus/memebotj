package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.User.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class PointsCommand extends CommandHandler {
    public PointsCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setHelptext(Memebot.formatText("POINTS_SYNTAX", this.getChannelHandler(), null, this, true, new String[]{}, ""));
        this.setUserCooldownLength(40);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        this.setSuccess(false);
        if (getChannelHandler().getUserList().containsKey(sender.getUsername().toLowerCase())) {
            if (data.length < 1) {
                getChannelHandler().sendMessage(String.format("%s: %.2f %s", sender.screenName(),
                        getChannelHandler().getUserList().get(sender.getUsername()).getPoints(),
                        getChannelHandler().getCurrencyEmote()), getChannelHandler().getChannel(), sender, isWhisper());
            } else {
                try {
                    UserHandler target = null;
                    if (getChannelHandler().getUserList().containsKey(data[1].toLowerCase())) {
                        target = getChannelHandler().getUserList().get(data[1].toLowerCase());
                    } else {
                        target = new UserHandler(data[1], this.getChannelHandler().getChannel());
                        if (target.isNewUser()) {
                            target = null;
                        }
                    }
                    if (target != null && checkPermissions(sender, CommandPower.adminAbsolute, CommandPower.adminAbsolute)) {
                        double number = java.lang.Double.parseDouble(data[2]);
                        if (data[0].equals("add")) {
                            target.setPoints(target.getPoints() + number);
                            this.setSuccess(true);
                        } else if (data[0].equals("sub")) {
                            target.setPoints(target.getPoints() - number);
                            this.setSuccess(true);
                        } else if (data[0].equals("set")) {
                            target.setPoints(number);
                            this.setSuccess(true);
                        }
                        if (this.isSuccess()) {
                            getChannelHandler().sendMessage(Memebot.formatText("POINTS_MOD_NEW_TOTAL",
                                    getChannelHandler(), sender, this, true, new String[]{target.screenName(),
                                            String.format("%.2f", target.getPoints())}, this.getChannelHandler().getChannel()),
                                    getChannelHandler().getChannel(), sender, isWhisper());
                        }
                    }
                    if (target != null && !this.isSuccess()) {
                        double number = java.lang.Double.parseDouble(data[2]);
                        if (number < 1) {
                            return;
                        }
                        double tax = getChannelHandler().getPointsTax(); // tax is currently 0
                        if (data[0].equals("send")) {
                            if (this.checkCost(sender, number + tax)) {
                                if (getChannelHandler().getPointsTax() <= 0) {
                                    sender.setPoints(sender.getPoints() - number);
                                } else {
                                    sender.setPoints(sender.getPoints() - (number + number / tax));
                                }
                                target.setPoints(target.getPoints() + number);
                                getChannelHandler().sendMessage(
                                        Memebot.formatText("POINTS_SEND", getChannelHandler(), sender, this, true,
                                                new String[]{String.format("%.2f", number), target.screenName(),
                                                        String.format("%.2f", tax)}, getChannelHandler().getChannel()),
                                        getChannelHandler().getChannel(), sender, isWhisper());
                            } else {
                                getChannelHandler().sendMessage(
                                        Memebot.formatText("POINTS_SEND_FAIL", getChannelHandler(),
                                                sender, this, true, new String[]{String.format("%.2f", number + tax)}, ""),
                                        this.getChannelHandler().getChannel(), sender, isWhisper());
                            }
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    CommandHandler.log.log("This exception is an illusion and is a trap devised by satan!" + e.toString());
                    getChannelHandler().sendMessage(Memebot.formatText("POINTS_ERROR_NFE", getChannelHandler(),
                            sender, this, true, new String[]{"NumberFormatException"}, ""), getChannelHandler().getChannel(),
                            sender, isWhisper());
                }
            }
        }
    }
}
