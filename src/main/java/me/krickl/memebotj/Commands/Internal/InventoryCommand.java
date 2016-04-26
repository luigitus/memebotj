package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Inventory.Buff;
import me.krickl.memebotj.Inventory.Inventory;
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
        setWhisper(true);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            if (data[0].equals("info")) {
                getChannelHandler().sendMessage(Memebot.formatText(new Item(data[1], 0).getDescription(), getChannelHandler(), sender, this, false, new String[]{}, ""), getChannelHandler().getChannel(), sender, isWhisper());
            } else if (data[0].equals("add") && checkPermissions(sender, CommandPower.adminAbsolute, CommandPower.adminAbsolute)) {
                sender.getUserInventory().addItem(data[1], Integer.parseInt(data[2]));
            } else if (data[0].equals("use")) {
                int amount = 1;
                if (data.length >= 3) {
                    amount = Integer.parseInt(data[2]);
                }
                int i = sender.getUserInventory().hasItems(data[1], amount);
                if (i != -1) {
                    Inventory inventory = sender.getUserInventory();
                    Item item = inventory.getItems().get(i);
                    item.useItem(amount);
                    inventory.addBuff(item);

                    getChannelHandler().sendMessage(Memebot.formatText(data[1].toUpperCase(), getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
                } else {
                    getChannelHandler().sendMessage(Memebot.formatText("NO_ITEM_FAIL", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
                }
            } else if (data[0].equals("sell")) {
                if (Integer.parseInt(data[2]) > 0) {
                    int i = sender.getUserInventory().hasItems(data[1], Integer.parseInt(data[2]));
                    if (i != -1) {
                        Item item = sender.getUserInventory().getItems().get(i);
                        sender.setPoints(sender.getPoints() + item.getValue() * Integer.parseInt(data[2]));
                        sender.getUserInventory().getItems().get(i).setAmount(sender.getUserInventory().getItems().get(i).getAmount() - Integer.parseInt(data[2]));
                        getChannelHandler().sendMessage(Memebot.formatText("SELL_OK", getChannelHandler(), sender, this, true, new String[]{Integer.toString(Integer.parseInt(data[2]) * item.getValue())}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
                    } else {
                        getChannelHandler().sendMessage(Memebot.formatText("SELL_FAIL", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
                    }
                }
            } else if (data[0].equals("buy")) {
                // todo implement shop
            } else if(data[0].equals("value")) {
                getChannelHandler().sendMessage(Memebot.formatText(String.format("%s: %d", data[1], new Item(data[1], 0).getValue()), getChannelHandler(), sender, this, false, new String[]{}, ""), getChannelHandler().getChannel(), sender, isWhisper());
            } else if(data[0].equals("stats")) {
                getChannelHandler().sendMessage(sender.getUserInventory().toString(), this.getChannelHandler().getChannel(), sender, isWhisper());
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
