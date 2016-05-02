package me.krickl.memebotj.Twitch;

import me.krickl.memebotj.Twitch.Model.Channel;
import me.krickl.memebotj.Twitch.Model.Streams;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public interface TwitchKraken {
    @GET("streams/{channel}")
    Call<Streams> getStream(@Path("channel") String channel);
    @GET("channels/{channel}")
    Call<Channel> getChannel(@Path("channel") String channel);
}
