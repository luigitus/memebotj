package me.krickl.memebotj;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/***
 * This calls contains Build Information
 * @author unlink
 *
 */
public class BuildInfo {
	public static String appName = "memebot";
	public static String version = "no_version_available";
	public static String dev = "Lukas Krickl";
	public static String buildNumber = "no_build_available";
	public static String timeStamp = "no_timestamp_available";
	
	public static void loadBuildInfo() {
		InputStream is = BuildInfo.class.getResourceAsStream("/buildinfo.properties");
		Properties buildInfo = new Properties();
		try {
			if(is != null) {
				buildInfo.load(is);
				is.close();
			} 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BuildInfo.version = (String) buildInfo.getOrDefault("version", BuildInfo.version);
		BuildInfo.appName = (String) buildInfo.getOrDefault("appname", BuildInfo.appName);
		BuildInfo.dev = (String) buildInfo.getOrDefault("developer", BuildInfo.dev);
		BuildInfo.buildNumber = (String) buildInfo.getOrDefault("build", BuildInfo.buildNumber);
		BuildInfo.timeStamp = (String) buildInfo.getOrDefault("buildtime", BuildInfo.timeStamp);
	}
}
