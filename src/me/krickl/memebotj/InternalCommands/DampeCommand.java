package me.krickl.memebotj.InternalCommands;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class DampeCommand extends CommandHandler {
    //this jackpot will increase every time someone loses to dampe or pays tax

	public DampeCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("Let dampe hate you for only all of your points");
		this.setUserCooldownLen(20);
        this.setListContent(new ArrayList<String>());
        this.setPointCost(0);
		this.setNeededCommandPower(0);
        this.setCmdtype("default");
        this.otherData().put("jackpot", "0");
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		//super.commandScript(sender, channelHandler, data);

        double wage = 20;
		try {
            if(data[0].equals("jackpot")) {
                channelHandler.sendMessage(String.format("Current jackpot: %.2f %s", this.getJackpot(), channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE")), this.getChannelOrigin());
                return;
            } else if(data[0].equals("reset") && CommandHandler.checkPermission(sender.getUsername(), 75, channelHandler.getUserList())) {
                this.setJackpot(0);
                channelHandler.sendMessage("Reset jackpot", this.getChannelOrigin());
                return;
            }
            wage = Double.parseDouble(data[0]);
            if (wage < 20) {
				channelHandler.sendMessage("Sorry the wage can't be less than 20 " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE"), this.getChannelOrigin());
                return;
            }
		} catch(ArrayIndexOutOfBoundsException e) {

		} catch(NumberFormatException e) {
            channelHandler.sendMessage(data[0] + " is not a number", this.getChannelOrigin());
			return;
		}

        if(!this.checkCost(sender, wage, channelHandler)) {
            channelHandler.sendMessage(String.format("Sorry you don't have %f %s.", wage, channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE")), this.getChannelOrigin());
            return;
        }

        if(wage > 500) {
            channelHandler.sendMessage("Sorry the wage can't be more than 500 " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE"), this.getChannelOrigin());
            return;
        }

        sender.setPoints(sender.getPoints() - wage);

		//happy now Luigitus? - removed secure random to make dampe less random
		//SecureRandom ran = new SecureRandom();
        Random ran = new Random();
        int range = 1000;
		int outcome = ran.nextInt(range - (int)wage/4);
        if (outcome <= 3) {
            channelHandler.sendMessage(sender.getUsername() + ": Dampé found " + Double.toString(1000 + this.getJackpot()) + " " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + "! You lucky bastard!", this.getChannelOrigin());
			sender.setPoints(sender.getPoints() + 1000 + this.getJackpot() + wage);
            this.setJackpot(0);
            sender.getUserCommandCooldowns().get(this.getCommand()).startCooldown();
		}
		else if(outcome <= 50) {
            double price = 10 * (Math.sqrt(wage) * 5);

			channelHandler.sendMessage(sender.getUsername() + ": Dampé found " + Double.toString(price) + " " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + "! You lucky bastard!", this.getChannelOrigin());
			sender.setPoints(sender.getPoints() + price + wage);
            sender.getUserCommandCooldowns().get(this.getCommand()).startCooldown();
		} else if(outcome <= 200) {
            double price = 3 * (Math.sqrt(wage) * 5);

			channelHandler.sendMessage(sender.getUsername() + ": Dampé found " + Double.toString(price) + " " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + " and returned your bet! Pretty good!", this.getChannelOrigin());
			sender.setPoints(sender.getPoints() + price + wage);
            sender.getUserCommandCooldowns().get(this.getCommand()).startCooldown();
		} else if(outcome <= 450) {
			channelHandler.sendMessage(sender.getUsername() + ": Dampé is being a dick and returned " + Double.toString(wage/2) + " " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + "!", this.getChannelOrigin());
			sender.setPoints(sender.getPoints() + wage / 2);
            sender.getUserCommandCooldowns().get(this.getCommand()).startCooldown();
            this.setJackpot(this.getJackpot() + wage/2);
		} else {
			channelHandler.sendMessage(sender.getUsername() + ": Dampé spent your " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + " on hookers, booze and crack!", this.getChannelOrigin());
            sender.getUserCommandCooldowns().get(this.getCommand()).startCooldown();
            this.setJackpot(this.getJackpot() + wage);
		}
	}

    public double getJackpot() {
        return Double.parseDouble(this.otherData().get("jackpot"));
    }

    public void setJackpot(double newJackpot) {
        this.otherData().put("jackpot", Double.toString(newJackpot));
    }
}
