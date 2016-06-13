package me.krickl.memebotj.SpeedrunCom;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.SpeedrunCom.Model.Games;
import me.krickl.memebotj.SpeedrunCom.Model.UserObject;
import me.krickl.memebotj.SpeedrunCom.Model.UsersLookup;
import me.krickl.memebotj.Utility.BuildInfo;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class SpeedRunComAPI implements Runnable {
    private Thread t = null;
    private int updateCycleMinuets = 10;

    private SpeedRunCom service;

    public SpeedRunComAPI() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("User-Agent", BuildInfo.appName + "/" + BuildInfo.version)
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        });
        if (Memebot.debug) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            httpClient.addInterceptor(logging);
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.speedrun.com/api/v1/")
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(SpeedRunCom.class);
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, "SpeedRunComAPI");
            t.start();
        }
    }

    @Override
    public void run() {
        while (Memebot.twitchAPI != null && !Memebot.twitchAPI.isCycleDone()) {
            try {
                Memebot.log.info("SpeedRunComAPI: Waiting for first completed cycle.");
                Thread.sleep(15000);
            } catch (InterruptedException ignored) {
            }
        }
        while (Memebot.isRunning) {
            for (int i = 0; i < Memebot.joinedChannels.size(); i++) {
                update(Memebot.joinedChannels.get(i));
                if (!(i + 1 == Memebot.joinedChannels.size())) {
                    try {
                        Memebot.log.info("SpeedRunComAPI: Request executed for " + Memebot.joinedChannels.get(i).getChannel()
                                + ", pausing before continuing.");
                        if (!Memebot.debug) {
                            Thread.sleep(5000); // 5 second pause
                        }
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    Memebot.log.info("SpeedRunComAPI: Request executed for " + Memebot.joinedChannels.get(i).getChannel()
                            + ", Update cycle completed. Next update is scheduled in " + updateCycleMinuets + " minuets.");
                }
            }
            try {
                Thread.sleep(updateCycleMinuets * 60000); // Continue this loop every updateCycleMinuets minuets
            } catch (InterruptedException ignored) {
            }
        }

        System.out.println("Thread end");
    }

    public void update(ChannelHandler channelHandler) {
        if (channelHandler.getUser() == null) {
            updateUser(channelHandler);
        }
        updateGame(channelHandler);
    }

    public void updateUser(ChannelHandler ch) {
        Call<UsersLookup> userLookup = service.lookupUser(ch.getBroadcaster());
        try {
            ArrayList<UserObject> users = userLookup.execute().body().getData();
            if (!users.isEmpty()) {
                ch.setUser(users.get(0));
            }
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public void updateGame(ChannelHandler ch) {
        String currentGame = ch.getCurrentGame();
        if (!currentGame.equals("Not Playing") && !currentGame.equals("")) {
            if (ch.getGame() == null) {
                Call<Games> gameLookup = service.lookupGame(currentGame, "categories");
                try {
                    ch.setGame(gameLookup.execute().body().getData().get(0));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            if (!ch.getGame().getName().equals(currentGame)) {
                Call<Games> gameLookup = service.lookupGame(currentGame, "categories");
                try {
                    ch.setGame(gameLookup.execute().body().getData().get(0));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            ch.setGame(null);
        }
    }

    public SpeedRunCom getService() {
        return service;
    }

    public Thread getT() {
        return t;
    }
}
