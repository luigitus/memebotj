package me.krickl.memebotj.Twitch;

import me.krickl.memebotj.Twitch.Model.Channel;
import me.krickl.memebotj.Twitch.Model.FriendRequests;
import me.krickl.memebotj.Twitch.Model.KrakenRoot;
import me.krickl.memebotj.Twitch.Model.Streams;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public interface TwitchKraken {
    @GET("kraken/")
    Call<KrakenRoot> getRoot(@Header("Authorization") String OAuthToken);

    @GET("kraken/streams/{channel}")
    Call<Streams> getStream(@Path("channel") String channel);

    @GET("kraken/channels/{channel}")
    Call<Channel> getChannel(@Path("channel") String channel);

    @GET("kraken/users/{user}/friends/requests?limit=100")
    Call<FriendRequests> getFriendRequests(@Path("user") String user, @Header("Authorization") String OAuthToken);

    @PUT("kraken/users/{user}/friends/{target}")
    Call<Void> acceptFriendRequest(@Path("user") String user, @Path("target") String target, @Header("Authorization") String OAuthToken);
}
