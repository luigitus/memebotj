package me.krickl.memebotj.InternalCommands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class SpeedrunCommand extends CommandHandler {

	public SpeedrunCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);

	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			if (data[0].equals("wr")) {
				String game = data[1];
				String category = data[2];

				URL url = new URL(String.format("http://www.speedrun.com/api/v1/leaderboards/%s/category/%s?top=1",
						game, category));

				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String buffer = "";
				StringBuilder response = new StringBuilder();

				while ((buffer = in.readLine()) != null) {
					response.append(buffer);
				}

				in.close();
			}

		} catch (ArrayIndexOutOfBoundsException e) {

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
