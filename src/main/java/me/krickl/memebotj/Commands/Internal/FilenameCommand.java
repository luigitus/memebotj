package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

import java.util.Random;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class FilenameCommand extends CommandHandler {
    private double namecost = 40;

    public FilenameCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
        this.readDB();
    }

    @Override
    public void initCommand() {
    }

    @Override
    public void overrideDB() {
        this.setHelptext("Syntax: !name <filename> || !name get || !name current");

        //this.setListregex("/^[一-龠ぁ-ゔァ-ヴーa-zA-Z0-9_,.-々〆〤]{1,8}$/u");

        this.setCost(0);
        this.setUserCooldownLength(20);
        this.setUses(2);
    }

    @Override
    public void readDB() {
        if(!Memebot.useMongo) {return;}
        super.readDB();

        namecost = (double)mongoHandler.getObject("namecost", 40);
    }

    @Override
    public void setDB() {
        super.setDB();

        mongoHandler.updateDocument("namecost", namecost);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            // count how many names a user has in the list
            int amountOfNamesInList = 0;

            for(String name : getChannelHandler().getFileNameList()) {
                if (name.contains(String.format("%s#%s", data[0], sender.getUsername()))) {
                    amountOfNamesInList = amountOfNamesInList + 1;
                }
            }

            if (data.length >= 1) {
                if (data[0].equals("get")) {
                    if (checkPermissions(sender, CommandPower.broadcasterAbsolute, CommandPower.broadcasterAbsolute)) {
                        int amount = 1;

                        try {
                            amount = Integer.parseInt(data[1]);
                        } catch(ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }

                        for (int i = 0; i < amount; i++) {
                            Random rand = new Random();
                            int index = rand.nextInt(getChannelHandler().getFileNameList().size() - 1);
                            getChannelHandler().setCurrentFileName(getChannelHandler().getFileNameList().get(index));
                            getChannelHandler().getFileNameList().remove(index);
                            getChannelHandler().sendMessage(Memebot.formatText("NAME_PICK", getChannelHandler(), sender, this, true, new String[]{getChannelHandler().getCurrentFileName().split("#")[0], getChannelHandler().getCurrentFileName().split("#")[1]}, ""), this.getChannelHandler().getChannel());
                        }
                        return;
                    } else {
                        getChannelHandler().sendMessage(Memebot.formatText("NAME_GET_ERROR", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
                        return;
                    }
                } else if (data[0].equals("current")) {
                    getChannelHandler().sendMessage(Memebot.formatText("NAME_PICK", getChannelHandler(), sender, this, true, new String[]{getChannelHandler().getCurrentFileName().split("#")[0], getChannelHandler().getCurrentFileName().split("#")[1]}, ""), this.getChannelHandler().getChannel());
                    return;
                } else if (data[0].equals("list")) {
                    getChannelHandler().sendMessage(String.format("%s/filesnames/%s/0", Memebot.webBaseURL, getChannelHandler().getBroadcaster()), this.getChannelHandler().getChannel());
                    return;
                } else if (data[0].equals("return") && checkPermissions(sender, CommandPower.broadcasterAbsolute, CommandPower.broadcasterAbsolute)) {
                    getChannelHandler().getFileNameList().add(getChannelHandler().getCurrentFileName());
                    getChannelHandler().sendMessage(Memebot.formatText("NAME_RETURN", getChannelHandler(), sender, this, true, new String[]{Integer.toString(getCounter())}, ""), this.getChannelHandler().getChannel());
                    return;
                } else if (data[0].equals("remove") && checkPermissions(sender, CommandPower.broadcasterAbsolute, CommandPower.broadcasterAbsolute)) {
                    int counter = 0;
                    int i = getChannelHandler().getFileNameList().size() - 1;
                    while (i >= 0) {
                        String name = getChannelHandler().getFileNameList().get(i).split("#")[0];
                        if (data[1].equals(name)) {
                            getChannelHandler().getFileNameList().remove(i);
                            counter += 1;
                        }
                        i -= 1;
                    }
                    getChannelHandler().sendMessage(Memebot.formatText("NAME_REMOVE", getChannelHandler(), sender, this, true, new String[]{Integer.toString(counter)}, ""), this.getChannelHandler().getChannel());
                    return;
                } else if (data[0].equals("count")) {
                    getChannelHandler().sendMessage(Memebot.formatText("NAME_COUNT", getChannelHandler(), sender, this, true, new String[]{Integer.toString(getChannelHandler().getFileNameList().size())}, ""), this.getChannelHandler().getChannel());
                    return;
                } else if (data[0].equals("cost")) {
                    getChannelHandler().sendMessage(Memebot.formatText("NAME_COST", getChannelHandler(), sender, this, true, new String[]{Double.toString(namecost)}, ""), this.getChannelHandler().getChannel());
                    return;
                } else if(data[0].equals("edit") && checkPermissions(sender, CommandPower.broadcasterAbsolute, CommandPower.broadcasterAbsolute)) {
                    if(data[1].equals("cost")) {
                        try {
                            //check if number is integer
                            namecost = Integer.parseInt(data[2]);
                            getChannelHandler().sendMessage(Memebot.formatText("NAME_EDIT_COST_OK", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
                        } catch(NumberFormatException e) {
                            log.info(e.toString());
                        }
                    }
                    return;
                }
            }

            int i = 1;
            if (data.length >= 2) {
                try {
                    i = java.lang.Integer.parseInt(data[1]);
                } catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    getChannelHandler().sendMessage(this.getHelptext(), this.getChannelHandler().getChannel());
                    return;
                }
            }
            boolean success = false;
            if (data[0].length() <= getChannelHandler().getMaxFileNameLen()) {
                if (amountOfNamesInList + i > getChannelHandler().getMaxAmountOfNameInList()) {
                    getChannelHandler().sendMessage(Memebot.formatText("NAME_ADD_TOO_MANY", getChannelHandler(), sender, this, true, new String[]{Integer.toString(getChannelHandler().getMaxAmountOfNameInList()), Integer.toString(amountOfNamesInList)}, ""), this.getChannelHandler().getChannel());
                    success = false;
                }
                else if (!this.checkCost(sender, namecost * i + getChannelHandler().getPointsPerUpdate())) {
                    getChannelHandler().sendMessage(Memebot.formatText("NAME_NOT_ENOUGH_MONEY", getChannelHandler(), sender, this, true, new String[]{Double.toString(namecost * i)}, ""), this.getChannelHandler().getChannel());
                } else {
                    if (i > 1) {
                        getChannelHandler().sendMessage(Memebot.formatText("NAME_ADD_MANY", getChannelHandler(), sender, this, true, new String[]{data[0], data[1]}, ""), this.getChannelHandler().getChannel());
                    }
                    else {
                        getChannelHandler().sendMessage(Memebot.formatText("NAME_ADD", getChannelHandler(), sender, this, true, new String[]{data[0]}, ""), this.getChannelHandler().getChannel());
                    }
                    sender.setPoints(sender.getPoints() - (namecost * i));
                    success = true;
                }
            } else {
                getChannelHandler().sendMessage(Memebot.formatText("NAME_ADD_TOO_LONG", getChannelHandler(), sender, this, true, new String[]{Integer.toString(getChannelHandler().getMaxFileNameLen())}, ""), this.getChannelHandler().getChannel());
            }
            for (int c = 0; c < i; c++) {
                if(success) {
                    getChannelHandler().getFileNameList().add(data[0].replace("#", "") + "#" + sender.getUsername());
                }
            }
            getChannelHandler().writeDB();
        } catch (ArrayIndexOutOfBoundsException e) {
            getChannelHandler().sendMessage(this.getHelptext(), this.getChannelHandler().getChannel());
            e.printStackTrace();
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
            getChannelHandler().sendMessage(Memebot.formatText("NAME_EMPTY", getChannelHandler(), sender, this, true, new String[]{}, ""));
        }
    }
}
