package me.krickl.memebotj

import java.io.{FileNotFoundException, FileReader}
import java.util
import java.util.Properties

/**
  * Created by unlink on 15/12/15.
  */
class Localisation(newLocal: String) {
  var local = newLocal

  val config = new Properties()

  try {
    config.load(new FileReader(f"${Memebot.memebotDir}/locals/$local.properties"))
  } catch {
    case e: FileNotFoundException => e.printStackTrace()
  }

  def localisedStringFor(stringID: String): String = {
    config.getOrDefault(stringID, f"UNKNOWN_STRING_ERR($stringID)").toString
  }

}
