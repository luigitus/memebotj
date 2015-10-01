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
	public static final String appName = "memebot";
	public static final String version = "no_version_available";
	public static final String dev = "Lukas Krickl";
	public static String buildNumber = "no_build_available";
	public static final String timeStamp = "no_timestamp_available";
	
	public static void loadBuildInfo() {
		InputStream is = BuildInfo.class.getResourceAsStream("buildinfo.properties");
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
		
		buildInfo.getOrDefault("version", BuildInfo.version);
		buildInfo.getOrDefault("appname", BuildInfo.appName);
		buildInfo.getOrDefault("developer", BuildInfo.dev);
		buildInfo.getOrDefault("build", BuildInfo.buildNumber);
		buildInfo.getOrDefault("buildtime", BuildInfo.timeStamp);
	}
}
