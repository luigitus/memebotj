package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;
import me.krickl.memebotj.Utility.Cooldown;

import java.security.SecureRandom;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class DampeCommand extends CommandHandler {
    public DampeCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setHelptext(Memebot.formatText("DAMPE_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));

        this.setCooldownLength(0);
        this.setCooldown(new Cooldown(getCooldownLength()));
        this.setUserCooldownLength(90);
        this.setNeededCommandPower(10);

        this.setUnformattedOutput("");
        this.setCommandScript("");
        //special case for mikami currently hard coded
        if(this.getChannelHandler().getChannel().equals("mikamihero")) {
            this.setUserCooldownLength(300);
        }

        this.setListContent(new java.util.ArrayList<String>());

        this.setCost(0);
        this.setWhisper(false);

        this.setNeededCommandPower(CommandPower.viewerAbsolute);

        this.setCommandType("default");

        this.setCheckDefaultCooldown(true);

        if (!this.getOtherData().containsKey("jackpot")) {
            this.getOtherData().put("jackpot", "0");
        }

        if (!this.getOtherData().containsKey("winner")) {
            this.getOtherData().put("winner", "null");
        }
    }

    @Override
    public void initCommand() {
        this.getOtherData().put("minbet", "50.0");
        this.getOtherData().put("jackpotchance2", "3");
    }

    @Override
    public boolean executeCommand(UserHandler sender, String[] data) {
        if(!this.isEnabled()) {
            return false;
        }
        data = formatData(sender, data);

        commandScript(sender, data);
        // write changes to db
        if (!sender.getUsername().equals("#readonly#")) {
            this.writeDB();
        }

        return true;
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        double wage = 50;
        try {
            wage = Double.parseDouble(getOtherData().get("minbet")); //50.0f;
        } catch(NumberFormatException e) {
            log.warning(e.toString());
            getOtherData().put("minbet", "50");
        }
        try {
            if (data[0].equals("jackpot")) {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_JACKPOT", getChannelHandler(), sender, this, true, new String[]{String.format("%.2f", this.getJackpot()), getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel());
                return;
            } else if (data[0].equals("set") && checkPermissions(sender, CommandPower.adminAbsolute, CommandPower.adminAbsolute)) {
                this.setJackpot(Double.parseDouble(data[1]));
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_SETJACKPOT", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
                return;
            } else if (data[0].equals("winner")) {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_WINNER", getChannelHandler(), sender, this, true, new String[]{this.getOtherData().get("winner")}, ""), this.getChannelHandler().getChannel());
                return;
            } else if(data[0].equals("edit") && checkPermissions(sender, CommandPower.broadcasterAbsolute, CommandPower.broadcasterAbsolute)) {
                try {
                    if (data[1].equals("jackpotchance")) {
                        Integer.parseInt(data[2]);
                        getOtherData().put("jackpotchance2", data[2]);
                        getChannelHandler().sendMessage(Memebot.formatText("DAMPE_JACKPOT_EDIT_OK", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
                    } else if (data[1].equals("minbet")) {
                        wage = Double.parseDouble(getOtherData().get("minbet"));
                        getOtherData().put("minbet", data[2]);
                        getChannelHandler().sendMessage(Memebot.formatText("DAMPE_MINBET_EDIT_OK", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
                    }
                } catch(NumberFormatException e) {
                    log.warning(e.toString());
                }
                return;
            }

            wage = Double.parseDouble(data[0]);
            if (wage < Double.parseDouble(getOtherData().get("minbet"))) {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_WAGE_FAIL_MIN", getChannelHandler(), sender, this, true, new String[]{String.format("%.2f", getChannelHandler().getPointsPerUpdate() * 50), getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel());
                return;
            }
        } catch(NumberFormatException e) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_NFE", getChannelHandler(), sender, this, true, new String[]{data[0]}, ""), this.getChannelHandler().getChannel());
            return;
        } catch(ArrayIndexOutOfBoundsException e) {

        }

        if(!this.checkCost(sender, wage)) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_OUT_OF_MONEY", getChannelHandler(), sender, this, true, new String[]{sender.screenName(), String.format("%.2f", wage), getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel());
            return;
        }
        if (wage > getChannelHandler().getMaxPoints() / 10) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_WAGE_FAIL_MAX", getChannelHandler(), sender, this, true, new String[]{String.format("%.2f", getChannelHandler().getMaxPoints() / 10)}, ""), this.getChannelHandler().getChannel());
            return;
        }

        if(this.handleCooldown(sender)) {
            return;
        }

        sender.setPoints(sender.getPoints() - wage);
        SecureRandom ran = new SecureRandom();
        int range = 1000;
        int outcome = ran.nextInt(range); //- wage.toInt / 4)
        int offlineModifier = 0;
        int jackpotChance = 3;

        try {
            jackpotChance = Integer.parseInt(getOtherData().get("jackpotchance2"));
        } catch(NumberFormatException e) {
            log.warning(e.toString());
            getOtherData().put("jackpotchance2", "3");
        }

        if (!getChannelHandler().isLive()) {
            offlineModifier = 5;
        }

        if(sender.getUserInventory().hasBuff("dekustick")) {
            jackpotChance = jackpotChance + 2;
        }

        //outcomes of dampe
        if (outcome <= jackpotChance && getChannelHandler().isLive()) {
            if (sender.setPoints(sender.getPoints() + this.getJackpot() + wage)) {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_JACKPOT_WON", getChannelHandler(), sender, this, true, new String[]{sender.screenName(), String.format("%.2f", this.getJackpot()), getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel());
                this.setJackpot(0);
                this.getOtherData().put("winner", sender.getUsername());
                sender.setJackpotWins(sender.getJackpotWins() + 1);
            } else {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_JACKPOT_WON_WALLET_FULL", getChannelHandler(), sender, this, true, new String[]{sender.screenName(), String.format("%.2f" ,this.getJackpot()), getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
                this.setJackpot(0);
            }
        } else if (outcome <= (40 - offlineModifier)) {
            double price = 10 * (Math.sqrt(wage) * 5) + ran.nextInt((int)wage);
            if (sender.setPoints(sender.getPoints() + price + wage)) {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_WON_1", getChannelHandler(), sender, this, true, new String[]{sender.screenName(), String.format("%.2f" ,price), getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            } else {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_WON_1_WALLET_FULL", getChannelHandler(), sender, this, true, new String[]{sender.screenName(), String.format("%.2f", price), getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
                this.setJackpot(this.getJackpot() + price);
            }
        } else if (outcome <= (180 - offlineModifier)) {
            double price = 3 * (Math.sqrt(wage) * 5) + ran.nextInt((int)wage);
            if (sender.setPoints(sender.getPoints() + price + wage)) {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_WON_2", getChannelHandler(), sender, this, true, new String[]{sender.screenName(), String.format("%.2f", price), getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            } else {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_WON_2_WALLET_FULL", getChannelHandler(), sender, this, true, new String[]{sender.screenName(), String.format("%.2f", price), getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
                this.setJackpot(this.getJackpot() + price);
            }
        } else if (outcome <= 450) {
            if (sender.setPoints(sender.getPoints() + wage / 2)) {
                this.setJackpot(this.getJackpot() + wage / 2);
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_LOST_1", getChannelHandler(), sender, this, true, new String[]{String.format("%.2f", wage / 2)}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            } else {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_LOST_1_WALLET_FULL", getChannelHandler(), sender, this, true, new String[]{String.format("%.2f", wage / 2)}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
                this.setJackpot(this.getJackpot() + wage);
            }

            this.setJackpot(this.getJackpot() + wage / 2);
        } else if (outcome <= 650) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_LOST_2", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            this.setJackpot(this.getJackpot() + wage);
        } else if (outcome <= 750) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_LOST_3", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            sender.getUserInventory().addItem("heartpiece", 1);
            this.setJackpot(this.getJackpot() + wage);
        } else if (outcome <= 850) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_LOST_4", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            this.setJackpot(this.getJackpot() + wage);
        } else if (outcome <= 900) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_LOST_6", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            sender.setPoints(sender.getPoints() - 10);
            this.setJackpot(this.getJackpot() + wage + 10);
        } else if(outcome <= 910) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_SHITTY", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            sender.setPoints(sender.getPoints() - 20);
            this.setJackpot(this.getJackpot() + wage + 10);
        } else {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_LOST_5", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            this.setJackpot(this.getJackpot() + wage);
        }

        this.setSuccess(true);

        this.startCooldown(sender);
    }

    public double getJackpot() {
        return Double.parseDouble(this.getOtherData().get("jackpot"));
    }

    public void setJackpot(double newJackpot) {
        this.getOtherData().put("jackpot", Double.toString(newJackpot));
    }
}
