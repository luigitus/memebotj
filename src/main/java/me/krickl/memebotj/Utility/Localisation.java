package me.krickl.memebotj.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * This file is part of memebotj.
 * Created by unlink on 07/04/16.
 */
public class Localisation {
    String local = "";

    Properties config = new Properties();

    public Localisation(String local) {
        this.local = local;
        try {
            InputStream localsURL = getClass().getResourceAsStream("/local/" + local + ".properties");

            if (localsURL != null) {
                InputStreamReader reader = new InputStreamReader(localsURL, "UTF-8");
                config.load(reader);
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String localisedStringFor(String stringID) {
        return config.getOrDefault(stringID, "UNKNOWN_STRING_ERR(" + stringID + ")").toString();
    }
}
