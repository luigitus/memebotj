package me.krickl.memebotj.SpeedrunCom;

import me.krickl.memebotj.SpeedrunCom.Model.*;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public interface ISpeedRunCom {
    @GET("users")
    Call<UsersLookup> lookupUser(@Query("lookup") String lookup);

    @GET("users/{userID}")
    Call<UserLookup> getUser(@Path("userID") String userID);

    @GET("games")
    Call<Games> lookupGame(@Query("name") String name, @Query("abbreviation") String abbreviation, @Query("embed") String embed);

    @GET("users/{userID}/personal-bests")
    Call<PBLookup> getPersonalBests(@Path("userID") String userID, @Query("game") String game);

    @GET("leaderboards/{gameID}/category/{category}?top=1")
    Call<WRLookup> getWorldRecord(@Path("gameID") String gameID, @Path("category") String category, @Query("embed") String embed);

    @GET("games/{gameID}/records?top=1")
    Call<WRSLookup> getWorldRecords(@Path("gameID") String gameID, @Query("embed") String embed);
}
