package me.krickl.memebotj.Utility

import java.io.{IOException, InputStream}
import java.util.Properties

/***
 * This class contains Build Information
 * @author unlink
 *
 */
object BuildInfo {

	var appName: String = "no_appname_available"
	var version: String = "no_version_available"
	var dev: String = "no_dev_available"
	var buildNumber: String = "no_build_available"
  var timeStamp: String = "no_timestamp_available"

	def loadBuildInfo() = {
	  val is: InputStream = BuildInfo.getClass.getResourceAsStream("/buildinfo.properties")
		val buildInfo: Properties = new Properties()
		try {
			if(is != null) {
				buildInfo.load(is)
				is.close()
			}
		} catch {
		  case e: IOException => {
			  e.printStackTrace()
		  }
		}

		BuildInfo.version = buildInfo.getOrDefault("version", BuildInfo.version).toString
		BuildInfo.appName =  buildInfo.getOrDefault("appname", BuildInfo.appName).toString
		BuildInfo.dev =  buildInfo.getOrDefault("developer", BuildInfo.dev).toString
		BuildInfo.buildNumber =  buildInfo.getOrDefault("build", BuildInfo.buildNumber).toString
		BuildInfo.timeStamp =  buildInfo.getOrDefault("buildtime", BuildInfo.timeStamp).toString
	}
}
