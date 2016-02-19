package me.krickl.memebotj.Services.Twitch

import java.io.IOException
import java.net.MalformedURLException

import me.krickl.memebotj.{ChannelHandler, Memebot}
import org.json.simple.JSONObject
import org.json.simple.parser.{JSONParser, ParseException}

/**
  * This file is part of memebotj.
  * Created by unlink on 17/02/16.
  */
class ChannelAPI(channel: String) {
  def update(channelHandler: ChannelHandler): Unit = {
    //twitch update
    try {
      if (Memebot.isTwitchBot) {
        val data = Memebot.readHttpRequest("https://api.twitch.tv/kraken/streams/" + this.channel)
        val parser = new JSONParser()
        val obj = parser.parse(data).asInstanceOf[JSONObject]
        val isOnline = obj.get("stream")
        if (isOnline == null) {
          ChannelHandler.getLog.info(String.format("Stream %s is offline", this.channel))
          channelHandler.isLive = false
          channelHandler.streamStartTime = -1
        } else {
          ChannelHandler.getLog.info(String.format("Stream %s is live", this.channel))
          if (channelHandler.isLive) {
            channelHandler.streamStartTime = System.currentTimeMillis()
          }
          channelHandler.isLive = true
        }
      } else {
        channelHandler.isLive = true
        channelHandler.streamStartTime = -1
      }
    } catch {
      case e: MalformedURLException => e.printStackTrace()
      case e: IOException => e.printStackTrace()
      case e: ParseException => e.printStackTrace()
    }

    //get game
    try {
      if (Memebot.isTwitchBot) {
        val data = Memebot.readHttpRequest("https://api.twitch.tv/kraken/channels/" + this.channel)

        val parser = new JSONParser()
        val obj = parser.parse(data).asInstanceOf[JSONObject]
        channelHandler.currentGame = obj.get("game").asInstanceOf[String]
        if (channelHandler.currentGame == null) {
          channelHandler.currentGame = "Not Playing"
        }
      } else {
        channelHandler.currentGame = ""
      }
    } catch {
      case e: MalformedURLException => e.printStackTrace()
      case e: IOException => e.printStackTrace()
      case e: ParseException => e.printStackTrace()
    }
  }
}
