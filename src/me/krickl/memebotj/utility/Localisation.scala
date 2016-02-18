package me.krickl.memebotj.Utility

import java.io.{FileNotFoundException, InputStreamReader}
import java.util.Properties

/**
  * Created by unlink on 15/12/15.
  */
class Localisation(newLocal: String) {
  var local = newLocal

  val config = new Properties()

  try {
    val localsURL = getClass.getResourceAsStream(f"/local/$local.properties")

    if(localsURL != null) {
      val reader = new InputStreamReader(localsURL, "UTF-8")
      config.load(reader)
      reader.close()
    }
  } catch {
    case e: FileNotFoundException => e.printStackTrace()
  }

  def localisedStringFor(stringID: String): String = {
    config.getOrDefault(stringID, f"UNKNOWN_STRING_ERR($stringID)").toString
  }

}
