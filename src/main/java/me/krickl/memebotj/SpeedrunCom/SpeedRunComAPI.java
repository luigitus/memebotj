package me.krickl.memebotj.SpeedrunCom;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.SpeedrunCom.Model.Game;
import me.krickl.memebotj.SpeedrunCom.Model.Games;
import me.krickl.memebotj.SpeedrunCom.Model.UserObject;
import me.krickl.memebotj.SpeedrunCom.Model.UsersLookup;
import me.krickl.memebotj.Utility.BuildInfo;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class SpeedRunComAPI {
    private ChannelHandler channelHandler;
    private UserObject user;
    private Game game;

    private SpeedRunCom service;

    public SpeedRunComAPI(ChannelHandler ch) {
        this.channelHandler = ch;
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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.speedrun.com/api/v1/")
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(SpeedRunCom.class);
    }

    public void update() {
        if (user == null) {
            updateUser();
        }
        updateGame();
    }

    public void updateUser() {
        Call<UsersLookup> userLookup = service.lookupUser(channelHandler.getBroadcaster());
        try {
            user = userLookup.execute().body().getData().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateGame() {
        String currentGame = channelHandler.getCurrentGame();
        if (game == null) {
            Call<Games> gameLookup = service.lookupGame(currentGame, "categories");
            try {
                game = gameLookup.execute().body().getData().get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if (!game.getName().equals(currentGame)) {
            Call<Games> gameLookup = service.lookupGame(currentGame, "categories");
            try {
                game = gameLookup.execute().body().getData().get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public SpeedRunCom getService() {
        return service;
    }

    public Game getGame() {
        return game;
    }

    public UserObject getUser() {
        return user;
    }
}
