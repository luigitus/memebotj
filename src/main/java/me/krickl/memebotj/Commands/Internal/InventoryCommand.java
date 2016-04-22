package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Inventory.Item;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

/**
 * This file is part of memebotj.
 * Created by unlink on 22/04/16.
 */
public class InventoryCommand extends CommandHandler {
    public InventoryCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void initCommand() {
    }

    @Override
    public void overrideDB() {
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            if(data[0].equals("info")) {
                getChannelHandler().sendMessage(new Item(data[1], 0).getDescription(), getChannelHandler().getChannel(), sender, isWhisper());
            } else if(data[0].equals("add") && checkPermissions(sender, CommandPower.adminAbsolute, CommandPower.adminAbsolute)) {
                sender.getUserInventory().addItem(data[1], Integer.parseInt(data[2]));
            } else if(data[0].equals("use")) {
                int i = sender.getUserInventory().hasItems(data[1], Integer.parseInt(data[2]));
                if(i != -1) {
                    sender.getUserInventory().getItems().get(i).setUsing(sender.getUserInventory().getItems().get(i).getUsing() + Integer.parseInt(data[2]));
                    getChannelHandler().sendMessage(Memebot.formatText(data[1].toUpperCase(), getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
                } else {
                    getChannelHandler().sendMessage(Memebot.formatText(data[1].toUpperCase() + "_FAIL", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
                }
            }
        } catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
            String msg = "";
            for(Item i : sender.getUserInventory().getItems()) {
                msg = msg + i.toString() + " - ";
            }
            getChannelHandler().sendMessage(msg, getChannelHandler().getChannel(), sender, isWhisper());
            log.info(e.toString());
        }
    }
}
