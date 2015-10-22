package me.krickl.memebotj.InternalCommands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class SpeedrunCommand extends CommandHandler {

    ArrayList<String> runnerList = new ArrayList<String>();

	public SpeedrunCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
        this.setQuotePrefix("");
		this.listContent().add("WR is 4:20:69 by {runner}");
        this.listContent().add("WR is 69 rupees by {runner}");
        this.listContent().add("WR is not getting Dampé Heart Piece at all by {runner}");
        this.listContent().add("WR is 400 resets by {runner}");
        this.listContent().add("WR is sub Bob by {runner}");
        this.listContent().add("WR is sub MrDestructoid by {runner}");
        this.listContent().add("WR is 69 HMS resets by {runner}");
        this.listContent().add("WR is having WR in ben% by {runner}");
        this.listContent().add("WR is 20 hours at lullaby skip by {runner}");
        this.listContent().add("WR is 200% more leg shakes by {runner}");
        this.listContent().add("WR is -20 seonds by {runner}");
        this.listContent().add("WR is done on emulator by {runner}");
        this.listContent().add("WR is free by {runner}");
        this.listContent().add("WR is sub 5 by {runner}");
        this.listContent().add("WR is 5 antlers by {runner}");
        this.listContent().add("WR is 2 Gigabytes of porn by {runner}");


        this.runnerList.add("zfg1");
        this.runnerList.add("TrevPerson");
        this.runnerList.add("MikamiHero");
        this.runnerList.add("SageTodd");
        this.runnerList.add("Runnerguy");
        this.runnerList.add("CosmoWright");
        this.runnerList.add("Testrunner");
        this.runnerList.add("Midnightonthethirdday");
        this.runnerList.add("Misttrusting");
        this.runnerList.add("SampleName_");
        this.runnerList.add("Hitokage220");
        this.runnerList.add("Kiwikiller");
        this.runnerList.add("Aaron Stevens");
        this.runnerList.add("Delightfulmoose");
        this.runnerList.add("Major Ass");
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
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
            e.printStackTrace();

            Random ran = new Random();
            int wrPick = ran.nextInt(this.listContent().size());
            int runnerPick = ran.nextInt(this.runnerList.size());

            channelHandler.sendMessage(this.listContent().get(wrPick).replace("{runner}", this.runnerList.get(runnerPick)), this.getChannelOrigin());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
