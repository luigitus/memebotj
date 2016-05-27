package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;
import me.krickl.memebotj.Utility.Cooldown;
import me.krickl.memebotj.Utility.DampeOutcome;
import sun.plugin.dom.exception.InvalidStateException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class DampeCommand extends CommandHandler {
    private double jackpot = 0.0;
    private int jackpotchance = 3;
    private double minbet = 50.0;
    private String winner = "";

    public DampeCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
        this.readDB();
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
        if (this.getChannelHandler().getChannel().equals("#mikamihero")) {
            this.setUserCooldownLength(300);
        }

        this.setListContent(new java.util.ArrayList<String>());

        this.setCost(0);
        this.setWhisper(false);

        this.setNeededCommandPower(CommandPower.viewerAbsolute);

        this.setCommandType("default");

        this.setCheckDefaultCooldown(true);
    }

    @Override
    public void initCommand() {
    }

    @Override
    public boolean executeCommand(UserHandler sender, String[] data) {
        if (!this.isEnabled()) {
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
    public void setDB() {
        super.setDB();
        mongoHandler.updateDocument("jackpot", this.jackpot);
        mongoHandler.updateDocument("jackpotchance", this.jackpotchance);
        mongoHandler.updateDocument("minbet", this.minbet);
        mongoHandler.updateDocument("winner", this.winner);
    }

    @Override
    public void readDB() {
        if (!Memebot.useMongo) {
            return;
        }

        super.readDB();

        this.setJackpot((double) mongoHandler.getObject("jackpot", jackpot));
        this.jackpotchance = (int) mongoHandler.getObject("jackpotchance", jackpotchance);
        this.minbet = (double) mongoHandler.getObject("minbet", minbet);
        if (mongoHandler.getObject("winner", winner) != null) {
            this.winner = mongoHandler.getObject("winner", winner).toString();
        }
    }


    @Override
    public boolean editCommand(String modType, String newValue, UserHandler sender) {
        if (!checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
        }

        setSuccess(super.editCommand(modType, newValue, sender));

        try {
            if (modType.equals("jackpotchance")) {
                jackpotchance = Integer.parseInt(newValue);
                setSuccess(true);
            } else if (modType.equals("minbet")) {
                minbet = Double.parseDouble(newValue);
                setSuccess(true);
            }
        } catch (NumberFormatException e) {
            log.warning(e.toString());
        }

        this.writeDB();

        return isSuccess();
    }


    @Override
    public void commandScript(UserHandler sender, String[] data) {
        double wage = minbet;

        try {
            if (data[0].equals("jackpot")) {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_JACKPOT", getChannelHandler(), sender, this, true, new String[]{String.format("%.2f", this.getJackpot()), getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel());
                return;
            } else if (data[0].equals("set") && checkPermissions(sender, CommandPower.adminAbsolute, CommandPower.adminAbsolute)) {
                this.setJackpot(Double.parseDouble(data[1]));
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_SETJACKPOT", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
                return;
            } else if (data[0].equals("winner")) {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_WINNER", getChannelHandler(), sender, this, true, new String[]{winner}, ""), this.getChannelHandler().getChannel());
                return;
            }

            wage = Double.parseDouble(data[0]);
            if (wage < minbet) {
                getChannelHandler().sendMessage(Memebot.formatText("DAMPE_WAGE_FAIL_MIN", getChannelHandler(), sender, this, true, new String[]{String.format("%.2f", getChannelHandler().getPointsPerUpdate() * 50), getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel());
                return;
            }
        } catch (NumberFormatException e) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_NFE", getChannelHandler(), sender, this, true, new String[]{data[0]}, ""), this.getChannelHandler().getChannel());
            return;
        } catch (ArrayIndexOutOfBoundsException e) {

        }

        if (!this.checkCost(sender, wage)) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_OUT_OF_MONEY", getChannelHandler(), sender, this, true, new String[]{sender.screenName(), String.format("%.2f", wage), getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel());
            return;
        }
        if (wage > getChannelHandler().getMaxPoints() / 10) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_WAGE_FAIL_MAX", getChannelHandler(), sender, this, true, new String[]{String.format("%.2f", getChannelHandler().getMaxPoints() / 10)}, ""), this.getChannelHandler().getChannel());
            return;
        }

        if (this.handleCooldown(sender)) {
            return;
        }

        sender.setPoints(sender.getPoints() - wage);
        SecureRandom ran = new SecureRandom();
        int range = 1000;
        int outcome = ran.nextInt(range); //- wage.toInt / 4)
        int offlineModifier = 0;

        if (!getChannelHandler().isLive()) {
            offlineModifier = 5;
        }

        /*if(sender.getUserInventory().hasBuff("dekustick")) {
            jackpotchance = jackpotchance + 2;
        }*/

        // see what outcomes are possible with first roll

        // list of possible outcomes
        DampeOutcome[] dampeOutcomes = {new DampeOutcome("jackpot", 0, jackpotchance),
        new DampeOutcome("win1", 1, 90 - offlineModifier), new DampeOutcome("win2", 2, 380 - offlineModifier),
        new DampeOutcome("half", 3, 650),
        new DampeOutcome("loss", 4, 910), new DampeOutcome("rupoor1", 5, 950), new DampeOutcome("rupoor2", 6, range)};

        ArrayList<DampeOutcome> possibleOutComes = new ArrayList<>();

        for(DampeOutcome dampeOutcomeT : dampeOutcomes) {
            if(outcome <= dampeOutcomeT.getChance()) {
                possibleOutComes.add(dampeOutcomeT);
            }
        }

        try {
            outcome = ran.nextInt(possibleOutComes.size());
        } catch (Exception e) {
            outcome = 0;
        }

        DampeOutcome dampeOutcome = possibleOutComes.get(outcome);

        // process the outcomes
        if(dampeOutcome.getId() == 0 && getChannelHandler().isLive()) {
            sender.setPoints(sender.getPoints() + this.getJackpot() + wage);
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_JACKPOT_WON", getChannelHandler(), sender, this,
                    true, new String[]{sender.screenName(), String.format("%.2f", this.getJackpot()),
                            getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel());

            this.setJackpot(0);
            this.winner = sender.getUsername();
            sender.setJackpotWins(sender.getJackpotWins() + 1);
        } else if(dampeOutcome.getId() == 1) {
            double price = 10 * (Math.sqrt(wage) * 5) + ran.nextInt((int) wage);
            sender.setPoints(sender.getPoints() + price + wage);
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_WON_1", getChannelHandler(), sender, this, true,
                    new String[]{sender.screenName(), String.format("%.2f", price), getChannelHandler().getCurrencyEmote()}, ""),
                    this.getChannelHandler().getChannel(), sender, isWhisper());

        } else if(dampeOutcome.getId() == 2) {
            double price = 3 * (Math.sqrt(wage) * 5) + ran.nextInt((int) wage);
            sender.setPoints(sender.getPoints() + price + wage);
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_WON_2", getChannelHandler(), sender, this, true,
                    new String[]{sender.screenName(), String.format("%.2f", price),
                            getChannelHandler().getCurrencyEmote()}, ""), this.getChannelHandler().getChannel(),
                    sender, isWhisper());

        } else if(dampeOutcome.getId() == 3) {
            sender.setPoints(sender.getPoints() + wage / 2);
            this.setJackpot(this.getJackpot() + wage / 2);
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_LOST_1", getChannelHandler(), sender, this, true,
                    new String[]{String.format("%.2f", wage / 2)}, ""), this.getChannelHandler().getChannel(), sender,
                    isWhisper());

        } else if(dampeOutcome.getId() == 4) {
            int lossText = ran.nextInt(4);
            if(lossText < 2) {
                lossText = 2;
            }

            String lossString = String.format("DAMPE_LOST_%d", lossText);
            getChannelHandler().sendMessage(Memebot.formatText(lossString, getChannelHandler(), sender,
                    this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            this.setJackpot(this.getJackpot() + wage);

        } else if(dampeOutcome.getId() == 5) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_RUPOOR", getChannelHandler(), sender,
                    this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            sender.setPoints(sender.getPoints() - 10);
            this.setJackpot(this.getJackpot() + wage + 10);
        } else if(dampeOutcome.getId() == 6) {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_SHITTY", getChannelHandler(),
                    sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            sender.setPoints(sender.getPoints() - 20);
            this.setJackpot(this.getJackpot() + wage + 20);
        } else {
            getChannelHandler().sendMessage(Memebot.formatText("DAMPE_DEFAULT", getChannelHandler(),
                    sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            this.setJackpot(this.getJackpot() + wage);
        }

        this.setSuccess(true);

        this.startCooldown(sender);
    }

    public double getJackpot() {
        return jackpot;
    }

    public void setJackpot(double newJackpot) {
        jackpot = newJackpot;
    }
}
