package me.krickl.memebotj.SpeedrunCom;

import me.krickl.memebotj.SpeedrunCom.Model.Games;
import me.krickl.memebotj.SpeedrunCom.Model.PBLookup;
import me.krickl.memebotj.SpeedrunCom.Model.UserLookup;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public interface SpeedRunCom {
    @GET("users")
    Call<UserLookup> lookupUser(@Query("lookup") String lookup);

    @GET("games")
    Call<Games> lookupGame(@Query("name") String name, @Query("embed") String embed);

    @GET("users/{id}/personal-bests")
    Call<PBLookup> getPersonalBests(@Path("id") String id);
}
