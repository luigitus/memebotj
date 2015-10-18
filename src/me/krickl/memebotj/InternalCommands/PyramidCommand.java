package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class PyramidCommand extends CommandHandler {

	public PyramidCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setNeededCommandPower(75);
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			String message = data[0];
			int size = Integer.parseInt(data[1]);

			for(int i = 0; i < size; i++) {
				channelHandler.sendMessage(message, this.getChannelOrigin());
				message = message + " " + data[0];
				Thread.sleep(1000);
			}

			for(int i = size; i >= 0; i--) {
				channelHandler.sendMessage(message, this.getChannelOrigin());
				message = message.substring(0, message.length() - data[0].length() - 1);
				Thread.sleep(1000);
			}

		} catch(ArrayIndexOutOfBoundsException e) {

		} catch(NumberFormatException e) {

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(StringIndexOutOfBoundsException e) {

		}
	}

}
