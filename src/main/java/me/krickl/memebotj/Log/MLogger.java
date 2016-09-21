package me.krickl.memebotj.Log;

import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.Utility.Cooldown;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.FormatFlagsConversionMismatchException;
import java.util.Properties;

/**
 * Created by unlink on 7/10/2016.
 */
public class MLogger {
    private static LogLevels level = LogLevels.DEBUG;
    public static ArrayList<String> allLogs = new ArrayList<String>();
    public static Cooldown emailInterval = new Cooldown(36000);
    private PrintWriter writer;
    private String id;

    public MLogger(String id) {
        this.id = id;
        try {
            File f = new File("./config/logs/" + id + ".log");
            if(!f.exists()) {
                f.createNewFile();
            }
            writer = new PrintWriter(f);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static MLogger createLogger(String id) {
        return new MLogger(id);
    }

    public void log(String message) {
        log(message, LogLevels.INFO);
    }

    public void log(String message, LogLevels level) {
        try {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd"); // dd/MM/yyyy
            Calendar cal = Calendar.getInstance();
            String strDate = sdfDate.format(cal.getTime());

            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss a");
            Calendar calTime = Calendar.getInstance();
            String strTime = sdfTime.format(calTime.getTime());

            String formattedMessage = "<null>";
            try {
                formattedMessage = String.format("%s <%s>%s %s >> %s\n", level.toString(), id, strDate, strTime, message);
                writer.printf(formattedMessage);

                if (Memebot.debug) {
                    System.out.printf(formattedMessage);
                }
            } catch(FormatFlagsConversionMismatchException e) {

            }

            allLogs.add(formattedMessage);
        } catch(java.util.UnknownFormatConversionException e) {

        }
    }
}
