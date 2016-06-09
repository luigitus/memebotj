package me.krickl.memebotj;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Commands.CommandRefernce;
import me.krickl.memebotj.Connection.ConnectionInterface;
import me.krickl.memebotj.Connection.IRCConnectionHandler;
import me.krickl.memebotj.SpeedrunCom.SpeedRunComAPI;
import me.krickl.memebotj.Twitch.TwitchAPI;
import me.krickl.memebotj.Utility.BuildInfo;
import me.krickl.memebotj.Utility.Localisation;
import me.krickl.memebotj.Web.WebHandler;
import org.json.simple.JSONObject;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * This file is part of memebotj.
 * Created by unlink on 04/04/16.
 */
//todo rewrite main class
public class Memebot {
    public static final int messageLimit = 19; // message limit per 30 seconds
    public static Logger log = Logger.getLogger(Memebot.class.getName());
    public static String ircServer = "irc.twitch.tv";
    public static int ircport = 6667;
    public static String mongoHost = "localhost";
    public static int mongoPort = 27017;
    public static String mongoDBName = "memebot";
    public static String home = System.getProperty("user.home");
    public static String memebotDir = "./config";
    public static String htmlDir = "";
    public static String configFile = memebotDir + "/memebot.cfg";
    public static String channelConfig = memebotDir + "/channels.cfg";
    public static String botNick = null;
    public static String botPassword = null;
    public static String clientID = null;
    public static int clientIDValidated = 1;
    public static String clientSecret = null;
    public static List<String> botAdmins = new ArrayList<String>();
    public static String mongoUser = "";
    public static String mongoPassword = "";
    public static boolean useMongoAuth = false;
    public static int pid = 0;
    public static ArrayList<String> channels = new java.util.ArrayList<String>();
    public static ArrayList<String> channelsPrivate = new java.util.ArrayList<String>();
    public static boolean isTwitchBot = true;
    public static boolean jokeMode = false;
    // ConnectionHandler connection = null
    public static List<ChannelHandler> joinedChannels = new ArrayList<ChannelHandler>();
    public static boolean useMongo = true;
    // boolean updateToMongo = false
    public static MongoClient mongoClient = null;
    public static MongoDatabase dbPrivate = null;
    public static MongoDatabase db = null;
    public static int webPort = 4567;

    public static TwitchAPI twitchAPI = null;
    public static SpeedRunComAPI speedRunComAPI = null;

    //public static MongoCollection<Document> internalCollection = null;
    public static String webBaseURL = "";

    public static boolean useWeb = true;

    public static boolean isBotMode = true;

    public static String mainChannel = "#getmemebot"; // this is the home channel of the bot - in this channel people can adopt the bot

    public static boolean debug = false;

    public static boolean useUpdateThread = true;

    public static ArrayList<String> urlBanList = new java.util.ArrayList<String>();

    public static ArrayList<String> globalBanList = new java.util.ArrayList<String>();

    public static ArrayList<String> phraseBanList = new ArrayList<String>();

    public static Localisation defaultLocal = new Localisation("engb");

    public static boolean cliInput = false;

    public static boolean isRunning = true;

    public static String connectionMode = "irc";

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.contains("home=")) {
                Memebot.home = arg.replaceAll("home=", "");
                Memebot.memebotDir = Memebot.home + "/config";
                Memebot.configFile = Memebot.memebotDir + "/memebot.cfg";
                Memebot.channelConfig = Memebot.memebotDir + "/channels.cfg";
                log.info("Set home directory to " + Memebot.home);
            }
        }

        setupDirs();
        readConfig();
        shutDownHook();
        setupDB();
        WebHandler.webHandler();
        setupConnection();
        mainLoop();
    }

    public static void setupDirs() {
        // initial setup
        new File(memebotDir).mkdir();
        new File(memebotDir + "/channels").mkdir();
        new File(memebotDir + "/locals").mkdir();
        new File(memebotDir + "/logs").mkdir();

        BuildInfo.loadBuildInfo();
    }

    public static void setupDB() {
        if (Memebot.isBotMode) {
            // set up database
            if (Memebot.useMongo) {
                if (Memebot.useMongoAuth) {
                    MongoClientURI authuri = new MongoClientURI(String.format("mongodb://%s:%s@%s/?authSource=%s",
                            Memebot.mongoUser, Memebot.mongoPassword, Memebot.mongoHost, Memebot.mongoDBName));
                    Memebot.mongoClient = new MongoClient(authuri);
                } else {
                    Memebot.mongoClient = new MongoClient(Memebot.mongoHost, Memebot.mongoPort);
                }
                Memebot.dbPrivate = Memebot.mongoClient.getDatabase(Memebot.mongoDBName);
                Memebot.db = Memebot.mongoClient.getDatabase(Memebot.mongoDBName + "_public");
                //Memebot.internalCollection = Memebot.db.getCollection("#internal#");
            } else {
                new File(Memebot.memebotDir + "/channeldata").mkdirs();
            }

            try {
                channels = Memebot.listDirectory(new File(memebotDir + "/channels/"), 1);
                //private channels on private database
                channelsPrivate = (java.util.ArrayList<String>)
                        Files.readAllLines(Paths.get(Memebot.channelConfig), Charset.defaultCharset());

                urlBanList = (java.util.ArrayList<String>)
                        Files.readAllLines(Paths.get(Memebot.memebotDir + "/urlblacklist.cfg"),
                        Charset.defaultCharset());

                phraseBanList = (java.util.ArrayList<String>)
                        Files.readAllLines(Paths.get(Memebot.memebotDir + "/phrasebanlist.cfg"),
                        Charset.defaultCharset());

                globalBanList = (java.util.ArrayList<String>)
                        Files.readAllLines(Paths.get(Memebot.memebotDir + "/globalbanlist.cfg"),
                        Charset.defaultCharset());

            } catch (IOException e) {
                log.warning(e.toString());
            }
        }
    }

    public static void setupConnection() {
        // setup connection

        // join channels
        Iterator it = Memebot.channels.iterator();
        while (it.hasNext()) {
            String channel = (String) it.next();
            Memebot.joinChannel(channel);
        }

        twitchAPI = new TwitchAPI();
        twitchAPI.start();
        speedRunComAPI = new SpeedRunComAPI();
        speedRunComAPI.start();
    }

    public static void mainLoop() {
        //auto rejoin if a thread crashes
        while (true) {
            for (int i = 0; i < Memebot.joinedChannels.size(); i++) {
                ChannelHandler ch = Memebot.joinedChannels.get(i);
                if (!ch.getT().isAlive()) {
                    String channel = ch.getChannel();
                    Memebot.joinedChannels.remove(i);
                    Memebot.joinChannel(channel);
                }
            }

            if (!twitchAPI.getT().isAlive()) {
                twitchAPI = new TwitchAPI();
                twitchAPI.start();
            }

            if (!speedRunComAPI.getT().isAlive()) {
                speedRunComAPI = new SpeedRunComAPI();
                speedRunComAPI.start();
            }

            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                log.warning(e.toString());
            }
        }
    }

    public static void joinChannel(String channel) {
        try {
            File login = new File(Memebot.memebotDir + "/" + channel.replace("\n\r", "") + ".login");
            ConnectionInterface connectionInterface = new IRCConnectionHandler(Memebot.ircServer,
                    Memebot.ircport, Memebot.botNick, Memebot.botPassword);
            if (login.exists()) {
                ArrayList<String> loginInfo = (ArrayList<String>)
                        Files.readAllLines(Paths.get(Memebot.memebotDir + "/" + channel.replace("\n\r", "") + ".login"));

                Memebot.log.info("Found login file for channel " + channel);

                connectionInterface = new IRCConnectionHandler(Memebot.ircServer, Memebot.ircport,
                        loginInfo.get(0).replace("\n", ""), loginInfo.get(1).replace("\n", ""));

                ChannelHandler newChannel = new ChannelHandler(channel.replace("\n\r", ""), connectionInterface);
                newChannel.start();
                joinedChannels.add(newChannel);
            } else {
                ChannelHandler newChannel = new ChannelHandler(channel.replace("\n\r", ""), connectionInterface);
                newChannel.start();
                joinedChannels.add(newChannel);
            }
        } catch (IOException e) {
            log.warning(e.toString());
        }
    }

    public static void readConfig() {
        // read config
        Properties config = new Properties();
        try {
            config.load(new FileReader(Memebot.configFile));
        } catch (FileNotFoundException e) {
            try {
                new File(Memebot.configFile).createNewFile();
                // save properties
            } catch (IOException e1) {
                log.warning(e.toString());
            }

            log.warning(e.toString());
        } catch (IOException e) {
            log.warning(e.toString());
        }

        // read botadmin file
        Memebot.botAdmins.add("#internal#");
        try {
            botAdmins = Files.readAllLines(Paths.get(Memebot.memebotDir + "/botadmins.cfg"));
            botAdmins.add("#internal#");
        } catch (IOException e) {
            log.warning(e.toString());
        }

        Memebot.ircServer = config.getProperty("ircserver", Memebot.ircServer);
        Memebot.ircport = Integer.parseInt(config.getProperty("ircport", Integer.toString(Memebot.ircport)));
        Memebot.mongoHost = config.getProperty("mongohost", Memebot.mongoHost);
        Memebot.mongoPort = Integer.parseInt(config.getProperty("mongoport", Integer.toString(Memebot.mongoPort)));
        Memebot.mongoDBName = config.getProperty("mongodbname", Memebot.mongoDBName);
        Memebot.botNick = config.getProperty("botnick", Memebot.botNick);
        Memebot.botPassword = config.getProperty("botpassword", Memebot.botPassword);
        Memebot.clientID = config.getProperty("clientid", Memebot.clientID);
        Memebot.clientSecret = config.getProperty("clientsecret", Memebot.clientSecret);
        Memebot.htmlDir = config.getProperty("htmldir", Memebot.htmlDir);
        Memebot.mongoUser = config.getProperty("mongouser", Memebot.mongoUser);
        Memebot.mongoPassword = config.getProperty("mongopassword", Memebot.mongoPassword);
        Memebot.useMongoAuth = Boolean.parseBoolean(config.getProperty("mongoauth",
                Boolean.toString(Memebot.useMongoAuth)));
        Memebot.webBaseURL = config.getProperty("weburl", Memebot.webBaseURL);
        Memebot.useWeb = Boolean.parseBoolean(config.getProperty("useweb", Boolean.toString(Memebot.useWeb)));
        Memebot.useMongo = Boolean.parseBoolean(config.getProperty("usemongo", Boolean.toString(Memebot.useMongo)));
        Memebot.isTwitchBot = Boolean.parseBoolean(config.getProperty("istwitchbot",
                Boolean.toString(Memebot.isTwitchBot)));
        Memebot.mainChannel = config.getProperty("mainchannel", Memebot.mainChannel);
        Memebot.debug = Boolean.parseBoolean(config.getProperty("debug", Boolean.toString(Memebot.debug)));
        Memebot.useUpdateThread = Boolean.parseBoolean(config.getProperty("updatethread",
                Boolean.toString(Memebot.useUpdateThread)));
        Memebot.jokeMode = Boolean.parseBoolean(config.getProperty("joke", Boolean.toString(Memebot.jokeMode)));
        Memebot.webPort = Integer.parseInt(config.getProperty("webport", Integer.toString(Memebot.webPort)));
        Memebot.cliInput = Boolean.parseBoolean(config.getProperty("climode", Boolean.toString(Memebot.cliInput)));
        Memebot.connectionMode = config.getProperty("connectionmode", Memebot.connectionMode);

        if (!Memebot.debug) {
            try {
                File f = new File(Memebot.memebotDir + "/logs/#bot#.log");
                if (!f.exists())
                    f.createNewFile();
                PrintStream writer = new PrintStream(new FileOutputStream(f), true);
                System.setOut(writer);
                System.setErr(writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void shutDownHook() {
        if (Memebot.isBotMode) {
            // shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    Memebot.log.warning("Process received SIGTERM...");
                    saveAll();
                }
            });
        }

        // get pid and write to file
        File f = new File(memebotDir + "/pid");
        BufferedWriter bw = null;
        try {
            Memebot.log.info("PID: " + ManagementFactory.getRuntimeMXBean().getName());
            bw = new BufferedWriter(new FileWriter(f));
            bw.write(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
            bw.close();
        } catch (IOException e) {
            log.info(e.toString());
        }
    }

    public static void saveAll() {
        Iterator it = Memebot.joinedChannels.iterator();
        while (it.hasNext()) {
            ChannelHandler ch = (ChannelHandler) it.next();
            ch.writeDB();
            for (CommandRefernce commandHandler : ch.getChannelCommands()) {
                commandHandler.writeDB();
            }

            for (CommandHandler commandHandler : ch.getInternalCommands()) {
                commandHandler.writeDB();
            }

            for (String userHandler : ch.getUserList().keySet()) {
                ch.getUserList().get(userHandler).writeDB();
            }
            ch.setJoined(false);
        }
    }

    public static String[] parseParameters(String toParse) {
        String[] parsedTemp = toParse.replace("{", "").replace("}", "").split(";");
        String[] parsed = new String[parsedTemp.length];

        for(int i = 0; i < parsedTemp.length; i++) {
            if(i != 0 && i != 1) {
                parsed[i] = parsedTemp[i];
            } else if(i == 1) {
                parsed[i] = "{" + parsedTemp[i] + "}";
            } else {
                parsed[i] = toParse;
            }
        }


        return parsed;
    }

    public static String formatText(String fo, ChannelHandler channelHandler, UserHandler sender,
                                    CommandHandler commandHandler, boolean local, String[] params, String alternativeText) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd"); // dd/MM/yyyy
        Calendar cal = Calendar.getInstance();
        String strDate = sdfDate.format(cal.getTime());
        String formattedOutput = fo;

        SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss a");
        Calendar calTime = Calendar.getInstance();
        String strTime = sdfTime.format(calTime.getTime());

        SecureRandom ran = new SecureRandom();

        boolean containsNone = false;
        if (formattedOutput.contains("{none}")) {
            containsNone = true;
        }

        if (local && channelHandler != null) {
            formattedOutput = channelHandler.getLocalisation().localisedStringFor(fo);
        }

        if (sender != null) {
            formattedOutput = formattedOutput.replace("{sender}", sender.screenName());
            formattedOutput = formattedOutput.replace("{senderusername}", sender.getUsername());
            formattedOutput = formattedOutput.replace("{points}", String.format("%.2f", sender.getPoints()));
            formattedOutput = formattedOutput.replace("{debugsender}", sender.toString());
            int newRan = new Double(sender.getPoints()).intValue();
            if (newRan > 0) {
                formattedOutput = formattedOutput.replace("{randompoints}",
                        Integer.toString(Math.abs(ran.nextInt(newRan))));
            } else {
                formattedOutput = formattedOutput.replace("{randompoints}",
                        Integer.toString(Math.abs(ran.nextInt(newRan + 1))));
            }
        }
        if (commandHandler != null) {
            formattedOutput = formattedOutput.replace("{counter}", Integer.toString(commandHandler.getCounter()));
            formattedOutput = formattedOutput.replace("{debugch}", commandHandler.toString());
            formattedOutput = formattedOutput.replace("{execcount}", Integer.toString(commandHandler.getExecCounter()));
        }
        if (channelHandler != null) {
            formattedOutput = formattedOutput.replace("{broadcaster}", channelHandler.getBroadcaster());
            formattedOutput = formattedOutput.replace("{channelweb}", channelHandler.getChannelPageBaseURL());
            if (channelHandler.getCurrentGame() != null) {
                formattedOutput = formattedOutput.replace("{game}", channelHandler.getCurrentGame());
            }
            if (channelHandler.getStreamTitle() != null) {
                formattedOutput = formattedOutput.replace("{title}", channelHandler.getStreamTitle());
            }
            formattedOutput = formattedOutput.replace("{curremote}",
                    channelHandler.getCurrencyEmote());
            formattedOutput = formattedOutput.replace("{currname}",
                    channelHandler.getCurrencyName());
            formattedOutput = formattedOutput.replace("{botnick}", channelHandler.getConnection().getBotNick());

            // random user as parameter
            List<String> keys = new ArrayList<String>(channelHandler.getUserList().keySet());
            UserHandler randomUH = channelHandler.getUserList().getOrDefault(keys.get(ran.nextInt(keys.size())), new UserHandler("#internal#", channelHandler.getChannel(), "#internal#"));
            formattedOutput = formattedOutput.replace("{randomuser}", randomUH.getUsername());

            // random USSR as parameter - returns your favourite communist leader
            String[] keysUSSR = {"Vladimir Lenin", "Joseph Stalin", "Georgy Malenkov", "Nikita Khrushchev",
                    "Leonid Brezhnev", "Yuri Andropov", "Konstantin Chernenko", "Mikhail Gorbachev",
                    "Gennady Yanayev", "The man who arranges the blocks which continue to fall from up above"};
            String randomUSSRString = keysUSSR[ran.nextInt(keysUSSR.length)];
            formattedOutput = formattedOutput.replace("{randomUSSR}", randomUSSRString);

            // todo add this later
            /*// command output as parameter
            for(CommandHandler ch : channelHandler.getChannelCommands()) {
                if(formattedOutput.contains("{" + ch.getCommandName() + "}")) {
                    ch.executeCommand(sender, new String[]{});
                    formattedOutput = formattedOutput.replace("{" + ch.getCommandName() + "}", ch.getLastOutput());
                }
            }

            // internal command output as parameter
            for(CommandHandler ch : channelHandler.getInternalCommands()) {
                if(formattedOutput.contains("{" + ch.getCommandName() + "}")) {
                    ch.executeCommand(sender, new String[]{});
                    formattedOutput = formattedOutput.replace("{" + ch.getCommandName() + "}", ch.getLastOutput());
                }
            }*/
        }

        formattedOutput = formattedOutput.replace("{version}", BuildInfo.version);
        formattedOutput = formattedOutput.replace("{developer}", BuildInfo.dev);
        formattedOutput = formattedOutput.replace("{appname}", BuildInfo.appName);
        formattedOutput = formattedOutput.replace("{appname}", BuildInfo.buildNumber);
        formattedOutput = formattedOutput.replace("{builddate}", BuildInfo.timeStamp);
        formattedOutput = formattedOutput.replace("{date}", strDate);
        formattedOutput = formattedOutput.replace("{time}", strTime);
        formattedOutput = formattedOutput.replace("{space}", " ");
        formattedOutput = formattedOutput.replace("{none}", "");
        formattedOutput = formattedOutput.replace("{random}", Integer.toString(Math.abs(ran.nextInt())));
        formattedOutput = formattedOutput.replace("NO_OUTPUT_ERR()", "");

        String paramN = "";

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                String str = params[i];
                String original = formattedOutput;
                if (str != null) {
                    formattedOutput = formattedOutput.replace(String.format("{param%d}", i + 1), str);

                    if (formattedOutput.equals(original) && !formattedOutput.contains("{paramN}")) {
                        formattedOutput = formattedOutput + " " + str;
                    } else if(formattedOutput.equals(original)) {
                        paramN = paramN + str + " ";
                    }
                } else if (alternativeText != null) {
                    formattedOutput = alternativeText;
                }
            }
        }

        // insert paramN
        formattedOutput = formattedOutput.replace("{paramN}", paramN);

        if (formattedOutput.isEmpty() && !containsNone && Memebot.debug) {
            formattedOutput = "";//"NO_OUTPUT_ERR()";
        }

        return formattedOutput;
    }

    /***
     * Reads from URL
     * @param urlString The URL
     * @return String of content
     * @deprecated
     */
    public static String urlRequest(String urlString) {
        return urlRequest(urlString, 5000, false, "GET", "");
    }

    public static String urlRequest(String urlString, int timeout, boolean appendLineFeed) {
        return urlRequest(urlString, timeout, appendLineFeed, "GET", "");
    }

    public static String urlRequest(String urlString, int timeout, boolean appendLineFeed, String requestMethod,
                                    String urlParameters) {
        URL url;
        HttpURLConnection connection = null;
        byte[] sendData = urlParameters.getBytes(StandardCharsets.UTF_8);
        int sendLen = sendData.length;
        String data = "";
        BufferedReader in;
        try {
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setRequestMethod(requestMethod);
            if (requestMethod.equals("POST") || requestMethod.equals("PUT")) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", Integer.toString(sendLen));
            }
            connection.setRequestProperty("charset", "utf-8");
            connection.setUseCaches(false);

            if (requestMethod.equals("POST") || requestMethod.equals("PUT")) {
                try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                    wr.write(sendData);
                    wr.flush();
                    wr.close();
                }
            }

            boolean isError = connection.getResponseCode() >= 400;

            if (!isError) {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            String line;
            while ((line = in.readLine()) != null) {
                if (appendLineFeed) {
                    data = data + line + "\n";
                } else {
                    data = data + line;
                }
            }

            in.close();
        } catch (IOException e) {
            log.warning(e.toString());
            data = e.toString();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return data;
    }

    public static byte[] urlRequestBytes(String urlString, int timeout) {
        return urlRequestBytes(urlString, timeout, "GET", "");
    }

    public static byte[] urlRequestBytes(String urlString, int timeout, String requestMethod, String urlParameters) {
        URL url;
        HttpURLConnection connection = null;
        byte[] data = new byte[1024];
        byte[] sendData = urlParameters.getBytes(StandardCharsets.UTF_8);
        int sendLen = sendData.length;
        InputStream in;
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        try {
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setRequestMethod(requestMethod);
            if (requestMethod.equals("POST") || requestMethod.equals("PUT")) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", Integer.toString(sendLen));
            }
            connection.setRequestProperty("charset", "utf-8");
            connection.setUseCaches(false);

            if (requestMethod.equals("POST") || requestMethod.equals("PUT")) {
                try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                    wr.write(sendData);
                    wr.flush();
                    wr.close();
                }
            }

            boolean isError = connection.getResponseCode() >= 400;

            if (!isError) {
                in = connection.getInputStream();
            } else {
                in = connection.getErrorStream();
            }

            int bytesRead = 0;

            while ((bytesRead = in.read(data)) != -1) {
                bao.write(data, 0, bytesRead);
            }

            in.close();
            bao.close();
        } catch (IOException e) {
            log.warning(e.toString());
            return e.toString().getBytes();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return bao.toByteArray();
    }

    /***
     * @param dir
     * @param mode 1 = list all files, 2 = list all directories, 0 = list both
     * @return
     */
    public static ArrayList<String> listDirectory(File dir, int mode) {
        ArrayList<String> dirList = new ArrayList<>();

        File listFiles[] = dir.listFiles();
        if (listFiles == null) {
            return dirList;
        }
        for (final File entry : listFiles) {
            if (entry.isDirectory()) {
                if (mode == 0 || mode == 2) {
                    dirList.add(entry.getName());
                }
            } else {
                if (mode == 0 || mode == 1) {
                    dirList.add(entry.getName());
                }
            }
        }

        return dirList;
    }

    public static JSONObject toJSONObject() {
        JSONObject wrapper = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("channels", Memebot.webBaseURL + "/api/channels");
        jsonObject.put("_id", BuildInfo.appName);
        jsonObject.put("version", BuildInfo.version);
        jsonObject.put("dev", BuildInfo.dev);

        wrapper.put("data", jsonObject);
        wrapper.put("links" ,getLinks(Memebot.webBaseURL + "/api", null, null, null));

        return wrapper;
    }

    public static JSONObject getLinks(String self, String parent, String next, String previous) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("self", self);
        jsonObject.put("parent", parent);
        jsonObject.put("next", next);
        jsonObject.put("previous", previous);

        return jsonObject;
    }

    public static String toJSONString() {
        return toJSONObject().toJSONString();
    }
}
