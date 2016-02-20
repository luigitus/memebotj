package me.krickl.memebotj.Game

import com.mongodb.client.{FindIterable, MongoCollection}
import me.krickl.memebotj.Memebot
import org.bson.Document

/**
  * This file is part of memebotj.
  * Created by unlink on 20/02/16.
  */
class PlayerHandler(var name: String = "") {
  // rpg stats
  var level: Int = 1
  var intelligence: Int = 0
  var dexterity: Int = 0
  var strength: Int = 0
  var charisma: Int = 0
  var wisdom: Int = 0
  // inventory
  var inventory = new Array[String](10)
  var playerClass: String = ""

  var userCollection: MongoCollection[Document] = null

  if (Memebot.useMongo) {
    userCollection = Memebot.db.getCollection("global_rpg")
  }

  readDBUserData()

  def writeDBUserData(): Unit = {
    if (!Memebot.useMongo) {
      return
    }

    val channelQuery = new Document("_id", this.name)

    val channelData = new Document("_id", this.name)
      .append("level", this.level)
      .append("int", this.intelligence)
      .append("wisdom", this.wisdom)
      .append("str", this.strength)
      .append("dex", this.dexterity)
      .append("charisma", this.charisma)
      .append("class", this.playerClass)
      .append("inventory", this.inventory)
    try {
      if (this.userCollection.findOneAndReplace(channelQuery, channelData) == null) {
        this.userCollection.insertOne(channelData)
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  def readDBUserData(): Unit = {
    if (!Memebot.useMongo) {
      return
    }

    val channelQuery = new Document("_id", this.name)
    val cursor: FindIterable[Document] = this.userCollection.find(channelQuery)

    val channelData = cursor.first()

    this.level = channelData.getOrDefault("level", this.level.asInstanceOf[Object]).asInstanceOf[Int]
    this.wisdom = channelData.getOrDefault("wisdom", this.wisdom.asInstanceOf[Object]).asInstanceOf[Int]
    this.dexterity = channelData.getOrDefault("dexterity", this.dexterity.asInstanceOf[Object]).asInstanceOf[Int]
    this.intelligence = channelData.getOrDefault("int", this.intelligence.asInstanceOf[Object]).asInstanceOf[Int]
    this.charisma = channelData.getOrDefault("charisma", this.charisma.asInstanceOf[Object]).asInstanceOf[Int]
    this.strength = channelData.getOrDefault("str", this.strength.asInstanceOf[Object]).asInstanceOf[Int]
    this.inventory = channelData.getOrDefault("level", this.inventory.asInstanceOf[Object]).asInstanceOf[Array[String]]

    // read data
    if (channelData != null) {

    } else {
    }
  }
}
