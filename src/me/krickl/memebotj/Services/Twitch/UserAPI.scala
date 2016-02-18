package me.krickl.memebotj.Services.Twitch

import me.krickl.memebotj.{ChannelHandler, Memebot, UserHandler}
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

/**
  * This file is part of memebotj.
  * Created by unlink on 17/02/16.
  */
class UserAPI(user: String) {
  def update(userHanlder: UserHandler, channelHandler: ChannelHandler = null): Unit = {
    //todo this causes issues
    if (Memebot.isTwitchBot && !userHanlder.hasFollowed && userHanlder.username != "#internal#" && userHanlder.username != "#readonly#" && Memebot.debug) {
      val data = Memebot.readHttpRequest(f"https://api.twitch.tv/kraken/users/${userHanlder.username}/follows/channels/${userHanlder.channelOrigin.replace("#", "")}", 1000)
      val parser = new JSONParser()
      val obj = parser.parse(data).asInstanceOf[JSONObject]
      val status = obj.get("status")
      if (status == null) {

        if (!userHanlder.hasFollowed && channelHandler != null) {
          channelHandler.sendMessage(Memebot.formatText(channelHandler.followAnnouncement, channelHandler, userHanlder))
        }

        userHanlder.isFollowing = true
        userHanlder.hasFollowed = true
      } else if (status.toString == "404") {
        userHanlder.isFollowing = false
      }
    }
  }
}
