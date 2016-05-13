package me.krickl.memebotj.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This file is part of memebotj.
 * Created by unlink on 07/04/16.
 */
public class BuildInfo {
    public static String appName = "no_appname_available";
    public static String version = "no_version_available";
    public static String dev = "no_dev_available";
    public static String buildNumber = "no_build_available";
    public static String timeStamp = "no_timestamp_available";

    public static void loadBuildInfo() {
        InputStream is = BuildInfo.class.getResourceAsStream("/buildinfo.properties");
        Properties buildInfo = new Properties();
        try {
            if (is != null) {
                buildInfo.load(is);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        BuildInfo.version = buildInfo.getOrDefault("version", BuildInfo.version).toString();
        BuildInfo.appName = buildInfo.getOrDefault("appname", BuildInfo.appName).toString();
        BuildInfo.dev = buildInfo.getOrDefault("developer", BuildInfo.dev).toString();
        BuildInfo.buildNumber = buildInfo.getOrDefault("build", BuildInfo.buildNumber).toString();
        BuildInfo.timeStamp = buildInfo.getOrDefault("buildtime", BuildInfo.timeStamp).toString();
    }
}
