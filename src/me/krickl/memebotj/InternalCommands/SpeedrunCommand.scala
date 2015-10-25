package me.krickl.memebotj.InternalCommands

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.{URLEncoder, HttpURLConnection, MalformedURLException, URL}
import java.util.ArrayList
import java.util.Random
import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.UserHandler
import org.json.simple.{JSONArray, JSONObject}
import org.json.simple.parser.JSONParser

//remove if not needed
import scala.collection.JavaConversions._

class SpeedrunCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
    command, dbprefix) {

    var runnerList: ArrayList[String] = new ArrayList[String]()
    this.setListContent(new ArrayList[String]())
    this.setQuotePrefix("")
    this.setUserCooldownLen(60)
    this.setNeededCooldownBypassPower(75)

    this.listContent.add("WR is 4:20:69 by {runner}")

    this.listContent.add("WR is 69 rupees by {runner}")

    this.listContent.add("WR is not getting Dampé Heart Piece at all by {runner}")

    this.listContent.add("WR is 400 resets by {runner}")

    this.listContent.add("WR is sub Bob by {runner}")

    this.listContent.add("WR is sub MrDestructoid by {runner}")

    this.listContent.add("WR is 69 HMS resets by {runner}")

    this.listContent.add("WR is having WR in ben% by {runner}")

    this.listContent.add("WR is 20 hours at lullaby skip by {runner}")

    this.listContent.add("WR is 200% more leg shakes by {runner}")

    this.listContent.add("WR is -20 seonds by {runner}")

    this.listContent.add("WR is done on emulator by {runner}")

    this.listContent.add("WR is free by {runner}")

    this.listContent.add("WR is sub 5 by {runner}")

    this.listContent.add("WR is 5 antlers by {runner}")

    this.listContent.add("WR is 2 Gigabytes of porn by {runner}")

    this.listContent.add("WR is 90% downtime of Teamspeak by {runner}")

    this.runnerList.add("zfg1")

    this.runnerList.add("TrevPerson")

    this.runnerList.add("MikamiHero")

    this.runnerList.add("SageTodd")

    this.runnerList.add("Runnerguy")

    this.runnerList.add("CosmoWright")

    this.runnerList.add("Testrunner")

    this.runnerList.add("Midnightonthethirdday")

    this.runnerList.add("Misttrusting")

    this.runnerList.add("SampleName_")

    this.runnerList.add("Hitokage220")

    this.runnerList.add("Kiwikiller")

    this.runnerList.add("Aaron Stevens")

    this.runnerList.add("Delightfulmoose")

    this.runnerList.add("Major Ass")

    this.runnerList.add("f")

    this.runnerList.add("ClintStevens")

    this.runnerList.add("KrimtonZ")

    this.runnerList.add("FrankerZ")

    this.runnerList.add("LilZ")

    this.runnerList.add("FrankerZ")

    this.runnerList.add("OVH")

    this.runnerList.add("LZM")

    this.runnerList.add("Fezehim")
    
    this.runnerList.add("Ur Mom")
    
    this.runnerList.add("{runner}")

    override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
        try {
          var urlGame = new URL("http://www.speedrun.com/api/v1/games?name=" + URLEncoder.encode(channelHandler.currentGame, "UTF-8") + "&max=1")
          var connection = urlGame.openConnection().asInstanceOf[HttpURLConnection]
          connection.setRequestMethod("GET")
          var in = new BufferedReader(new InputStreamReader(connection.getInputStream))
          var response = Stream.continually(in.readLine()).takeWhile(_ != null).mkString("\n")
          val category = "Any1"
          val parser = new JSONParser()
          var obj: JSONObject = parser.parse(response).asInstanceOf[JSONObject]

          channelHandler.sendMessage(obj.get("data").asInstanceOf[JSONArray].get(0).asInstanceOf[JSONObject].get("names").toString, this.getChannelOrigin())
          in.close()

          val game = obj.get("data").asInstanceOf[JSONArray].get(0).asInstanceOf[JSONObject].get("abbreviation")

          var urlCategory =

          //todo add category lookup
          if (data(0) == "wr") {
            val urlWR = new URL(f"http://www.speedrun.com/api/v1/leaderboards/${game}/category/${category}?top=1")
            connection = urlWR.openConnection().asInstanceOf[HttpURLConnection]
            in = new BufferedReader(new InputStreamReader(connection.getInputStream))
            response = Stream.continually(in.readLine()).takeWhile(_ != null).mkString("\n")
            obj = parser.parse(response).asInstanceOf[JSONObject]
            in.close()
          }
        } catch {
            case e: ArrayIndexOutOfBoundsException => {
                e.printStackTrace()
                val ran = new Random()
                val wrPick = ran.nextInt(this.listContent.size)
                val runnerPick = ran.nextInt(this.runnerList.size)
                channelHandler.sendMessage(this.listContent.get(wrPick).replace("{runner}", this.runnerList.get(runnerPick)),
                    this.getChannelOrigin)
            }
            case e: MalformedURLException => e.printStackTrace()
            case e: IOException => e.printStackTrace()
        }
    }
}
