package me.krickl.memebotj

import java.io.{Reader, InputStreamReader, FileNotFoundException, FileReader}
import java.util
import java.util.Properties

/**
  * Created by unlink on 15/12/15.
  */
class Localisation(newLocal: String) {
  var local = newLocal

  val config = new Properties()

  try {
    val reader = new InputStreamReader(getClass.getResourceAsStream(f"/local/$local.properties"), "UTF-8")
    config.load(reader)
    reader.close()
  } catch {
    case e: FileNotFoundException => e.printStackTrace()
    case e: NullPointerException => e.printStackTrace() // needs to be caught cause it can be reloaded by a user
  }

  def localisedStringFor(stringID: String): String = {
    config.getOrDefault(stringID, f"UNKNOWN_STRING_ERR($stringID)").toString
  }

}
