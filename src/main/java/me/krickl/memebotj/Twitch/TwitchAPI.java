package me.krickl.memebotj.Twitch;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.Twitch.Model.Channel;
import me.krickl.memebotj.Twitch.Model.Stream;
import me.krickl.memebotj.Twitch.Model.Streams;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class TwitchAPI {
    private ChannelHandler channelHandler;
    private TwitchKraken service;

    public TwitchAPI(ChannelHandler ch) {
        this.channelHandler = ch;
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
       /* httpClient.addInterceptor(new Interceptor() {
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
        */
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.twitch.tv/kraken/")
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(TwitchKraken.class);
    }

    public void update() {
        try {
            if (Memebot.isTwitchBot) {
                Call<Streams> streamCall = service.getStream(channelHandler.getBroadcaster());
                Stream stream = streamCall.execute().body().getStream();
                if (stream == null) {
                    channelHandler.setLive(false);
                    Call<Channel> channelCall = service.getChannel(channelHandler.getBroadcaster());
                    Channel channel = channelCall.execute().body();
                    parseChannel(channel);
                } else {
                    channelHandler.setLive(true);
                    // When the stream is live, we get the channel data too! One API call less
                    parseChannel(stream.getChannel());
                }
            } else {
                channelHandler.setLive(true);
                channelHandler.setCurrentGame("");
                channelHandler.setStreamTitle("");
            }
        } catch (IOException e) {
            channelHandler.setCurrentGame("");
            channelHandler.setStreamTitle("");
        }
    }

    private void parseChannel(Channel channel) {
        channelHandler.setStreamTitle(channel.getStatus());
        channelHandler.setCurrentGame(channel.getGame());
        if (channelHandler.getCurrentGame() == null) {
            channelHandler.setCurrentGame("Not Playing");
        }
        if (channelHandler.getStreamTitle() == null) {
            channelHandler.setStreamTitle("");
        }
    }

}
