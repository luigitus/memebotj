package me.krickl.memebotj.Web;

import com.mongodb.util.JSON;
import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Database.MongoHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.Document;
import org.json.simple.JSONObject;
import spark.ModelAndView;
import spark.Request;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;


/**
 * This file is part of memebotweb.
 * Created by unlink on 12/04/16.
 */
public class WebHandler {
    public static void webHandler() {
        externalStaticFileLocation("./public");
        port(Memebot.webPort);

        get("/channels", (req, res) -> {
            return "Coming soon(tm)";
        });

        get("/commands/:channel", (req, res) -> {
            String loginUserName = "Guest";
            String channel = "#" + req.params(":channel");
            ChannelHandler channelHandler = getChannelForName(channel);

            if (channelHandler != null) {
                Collections.sort(channelHandler.getChannelCommands());
            }

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);
            model.put("uh", getLoginUserHandler(req, req.cookie("login_name"), channel));

            return new ModelAndView(model, "web/channel.vm");
        }, new VelocityTemplateEngine());

        get("/commands/list/internals/:channel", (req, res) -> {
            String channel = "#" + req.params(":channel");
            ChannelHandler channelHandler = getChannelForName(channel);
            if (channelHandler != null) {
                Collections.sort(channelHandler.getInternalCommands());
            }

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);
            model.put("uh", getLoginUserHandler(req, req.cookie("login_name"), channel));

            return new ModelAndView(model, "web/internals.vm");
        }, new VelocityTemplateEngine());

        get("/commands/:channel/:command", (req, res) -> {
            String channel = "#" + req.params(":channel");
            String command = req.params(":command");
            ChannelHandler channelHandler = getChannelForName(channel);
            int i = -1;
            if (channelHandler != null) {
                i = channelHandler.findCommand(command);
            }
            CommandHandler commandHandler = null;
            if (i != -1) {
                commandHandler = channelHandler.getChannelCommands().get(i);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("command", commandHandler);
            model.put("web", Memebot.webBaseURL);
            model.put("uh", getLoginUserHandler(req, req.cookie("login_name"), channel));

            return new ModelAndView(model, "web/command.vm");
        }, new VelocityTemplateEngine());

        get("/commands/internals/:channel/:command", (req, res) -> {
            String channel = "#" + req.params(":channel");
            String command = req.params(":command");

            ChannelHandler channelHandler = getChannelForName(channel);
            int i = -1;
            if (channelHandler != null) {
                i = channelHandler.findCommand(command, channelHandler.getInternalCommands(), 1);
            }
            CommandHandler commandHandler = null;
            if (i != -1) {
                commandHandler = channelHandler.getInternalCommands().get(i);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("command", commandHandler);
            model.put("web", Memebot.webBaseURL);
            model.put("uh", getLoginUserHandler(req, req.cookie("login_name"), channel));

            return new ModelAndView(model, "web/command.vm");
        }, new VelocityTemplateEngine());

        get("/songs/:channel/player", (req, res) -> {
            String channel = "#" + req.params(":channel");

            ChannelHandler channelHandler = getChannelForName(channel);

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);
            model.put("uh", getLoginUserHandler(req, req.cookie("login_name"), channel));

            return new ModelAndView(model, "web/songrequest.vm");
        }, new VelocityTemplateEngine());

        get("/filesnames/:channel/:page", (req, res) -> {
            String channel = "#" + req.params(":channel");
            int page = Integer.parseInt(req.params(":page"));
            String next = Integer.toString(page + 1);
            String previous = Integer.toString(page - 1);

            ChannelHandler channelHandler = getChannelForName(channel);

            ArrayList<String> displayList = new ArrayList<String>();

            if (channelHandler != null) {
                // decide what name to list
                for (int i = page * 10; i < channelHandler.getFileNameList().size(); i++) {
                    if (i <= page * 10 + 25) {
                        displayList.add(channelHandler.getFileNameList().get(i));
                    }
                }
            }

            Map<String, Object> model = new HashMap<>();

            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);
            model.put("next", next);
            model.put("previous", previous);
            model.put("names", displayList);
            model.put("uh", getLoginUserHandler(req, req.cookie("login_name"), channel));

            return new ModelAndView(model, "web/filenames.vm");
        }, new VelocityTemplateEngine());

        get("/users/listnames/:channel/:page", (req, res) -> {
            String channel = "#" + req.params(":channel");
            int page = Integer.parseInt(req.params(":page"));
            String next = Integer.toString(page + 1);
            String previous = Integer.toString(page - 1);

            ChannelHandler channelHandler = getChannelForName(channel);

            if (channelHandler != null) {
                //Collections.sort(channelHandler.getUserList());
            }

            Map<String, Object> model = new HashMap<>();
            MongoHandler mh = null;
            if (Memebot.channelsPrivate.contains(channel)) {
                mh = new MongoHandler(Memebot.dbPrivate, channel + "_users");
            } else {
                mh = new MongoHandler(Memebot.db, channel + "_users");
            }

            ArrayList<String> userList = new ArrayList<String>();
            int counter = 0;
            for (Document doc : mh.getDocuments()) {
                userList.add(doc.getOrDefault("_id", "#error#").toString());
            }

            Collections.sort(userList);

            ArrayList<String> displayList = new ArrayList<String>();

            // decide what user to list
            for (int i = page * 10; i < userList.size(); i++) {
                if (i <= page * 10 + 25) {
                    displayList.add(userList.get(i));
                }
            }

            model.put("channel", channelHandler);
            model.put("userlist", displayList);
            model.put("web", Memebot.webBaseURL);
            model.put("next", next);
            model.put("previous", previous);
            model.put("uh", getLoginUserHandler(req, req.cookie("login_name"), channel));

            return new ModelAndView(model, "web/userlist.vm");
        }, new VelocityTemplateEngine());

        get("/users/user/:channel/:user", (req, res) -> {
            String channel = "#" + req.params(":channel");
            String user = req.params(":user");
            ChannelHandler channelHandler = getChannelForName(channel);
            UserHandler userHandler = new UserHandler(user, channel);

            Map<String, Object> model = new HashMap<>();

            model.put("channel", channelHandler);
            model.put("user", userHandler);
            model.put("web", Memebot.webBaseURL);
            model.put("uh", getLoginUserHandler(req, req.cookie("login_name"), channel));

            return new ModelAndView(model, "web/userdetail.vm");
        }, new VelocityTemplateEngine());

        get("/login/:channel", (req, res) -> {
            String channel = "#" + req.params(":channel");
            ChannelHandler channelHandler = getChannelForName(channel);

            Map<String, Object> model = new HashMap<>();

            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);
            model.put("uh", getLoginUserHandler(req, req.cookie("login_name"), channel));

            return new ModelAndView(model, "web/login.vm");
        }, new VelocityTemplateEngine());

        get("/badbrowser", (req, res) -> {
            String channel = "#" + req.params(":channel");
            ChannelHandler channelHandler = getChannelForName(channel);

            Map<String, Object> model = new HashMap<>();

            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);
            model.put("uh", getLoginUserHandler(req, req.cookie("login_name"), channel));

            return new ModelAndView(model, "web/badbrowser.vm");
        }, new VelocityTemplateEngine());

        post("/login/:channel/process", (req, res) -> {
            String channel = "#" + req.params(":channel");
            UserHandler user = new UserHandler(java.net.URLDecoder.decode(req.queryParams("username"), "UTF-8"), channel);
            String oauth = java.net.URLDecoder.decode(req.queryParams("password"), "UTF-8");
            ChannelHandler channelHandler = getChannelForName(channel);

            String response = "Failed to login!";

            if (oauth.equals(user.getOauth())) {
                response = "Login OK";
                res.cookie("/", "login_name", user.getUsername(), 604800, false);
                res.cookie("/", "login_oauth", sha1HexString(user.getOauth()), 604800, false);
                res.cookie("/", "login_apikey", user.getAPIKey(), 604800, false);
            }

            Map<String, Object> model = new HashMap<>();

            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);
            model.put("login", response);
            model.put("uh", getLoginUserHandler(req, req.cookie("login_name"), channel));

            return new ModelAndView(model, "web/process.vm");
        }, new VelocityTemplateEngine());

        post("/login", (req, res) -> {
            JSONObject loginObject = new JSONObject();

            String channel = "#" + req.params(":channel");
            UserHandler user = new UserHandler(java.net.URLDecoder.decode(req.queryParams("username"), "UTF-8"), channel);
            String oauth = java.net.URLDecoder.decode(req.queryParams("password"), "UTF-8");
            ChannelHandler channelHandler = getChannelForName(channel);

            loginObject.put("_status", "Login Failed");
            loginObject.put("_self", Memebot.webBaseURL + "/login");

            if (oauth.equals(user.getOauth())) {
                loginObject.put("_status", "Login OK");
                res.cookie("/", "login_name", user.getUsername(), 604800, false);
                res.cookie("/", "login_oauth", sha1HexString(user.getOauth()), 604800, false);
                res.cookie("/", "login_apikey", user.getAPIKey(), 604800, false);
            }

            return loginObject.toJSONString();
        });


        get("/api", (req, res) -> {
            res.type("application/json");

            return Memebot.toJSONString();
        });

        get("/api/channels", (req, res) -> {
            res.type("application/json");
            JSONObject wrapper = new JSONObject();
            JSONObject channelsObject = new JSONObject();

            for (ChannelHandler channelHandler : Memebot.joinedChannels) {
                channelsObject.put(channelHandler.getChannel(), Memebot.webBaseURL + "/api/channels/" + channelHandler.getBroadcaster());
            }

            channelsObject.put("_id", null);
            wrapper.put("data", channelsObject);
            wrapper.put("links", Memebot.getLinks(Memebot.webBaseURL + "/api/channels", Memebot.webBaseURL + "/api", null
            , null));
            return wrapper.toJSONString();
        });

        get("/api/channels/:channel", (req, res) -> {
            res.type("application/json");
            ChannelHandler channelHandler = getChannelForName("#" + req.params(":channel"));
            return channelHandler.toJSONSString();
        });

        get("/api/filenames/:channel", (req, res) -> {
            res.type("application/json");
            ChannelHandler channelHandler = getChannelForName("#" + req.params(":channel"));
            return channelHandler.filenamesToJSON();
        });

        get("/api/users/:channel", (req, res) -> {
            res.type("application/json");
            String channel = "#" + req.params(":channel");

            ChannelHandler channelHandler = getChannelForName(channel);

            MongoHandler mh = null;
            if (Memebot.channelsPrivate.contains(channel)) {
                mh = new MongoHandler(Memebot.dbPrivate, channel + "_users");
            } else {
                mh = new MongoHandler(Memebot.db, channel + "_users");
            }

            ArrayList<String> userList = new ArrayList<String>();
            int counter = 0;
            for (Document doc : mh.getDocuments()) {
                userList.add(doc.getOrDefault("_id", "#error#").toString());
            }

            JSONObject usersObject = new JSONObject();

            for (String user : userList) {
                usersObject.put(user, Memebot.webBaseURL + "/api/users/" + channel.replace("#", "") + "/" + user);
            }

            return usersObject.toJSONString();
        });

        get("/api/users/:channel/:user", (req, res) -> {
            res.type("application/json");
            UserHandler userHandler = new UserHandler(req.params(":user"), "#" + req.params(":channel"));
            if(userHandler.isNewUser()) {
                res.status(404);
                return "{}";
            }
            return userHandler.toJSONString();
        });

        get("/api/commands/:channel", (req, res) -> {
            res.type("application/json");
            JSONObject wrapper = new JSONObject();
            JSONObject commandsObject = new JSONObject();
            ChannelHandler channelHandler = getChannelForName("#" + req.params(":channel"));
            for (CommandHandler commandHandler : channelHandler.getChannelCommands()) {
                commandsObject.put(commandHandler.getCommandName(), Memebot.webBaseURL + "/api/commands/" + channelHandler.getBroadcaster() + "/" + commandHandler.getCommandName());
            }
            commandsObject.put("_id", channelHandler.getChannel());

            wrapper.put("data", commandsObject);
            wrapper.put("links", Memebot.getLinks(Memebot.webBaseURL + "/api/commands/" + channelHandler.getBroadcaster(),
                    Memebot.webBaseURL + "/api/channels/" + channelHandler.getBroadcaster(), null, null));

            return wrapper.toJSONString();
        });

        get("/api/commands/:channel/:command", (req, res) -> {
            res.type("application/json");
            ChannelHandler channelHandler = getChannelForName("#" + req.params(":channel"));
            CommandHandler commandHandler = channelHandler.findCommandForString(req.params(":command"), channelHandler.getChannelCommands());
            if (commandHandler != null) {
                JSONObject jsonObject = commandHandler.toJSONObject();
                return jsonObject.toJSONString();
            }
            res.status(404);
            return "{}";
        });

        get("/api/internals/:channel", (req, res) -> {
            res.type("application/json");
            JSONObject wrapper = new JSONObject();
            JSONObject commandsObject = new JSONObject();
            ChannelHandler channelHandler = getChannelForName("#" + req.params(":channel"));
            for (CommandHandler commandHandler : channelHandler.getInternalCommands()) {
                commandsObject.put(commandHandler.getCommandName(), Memebot.webBaseURL + "/api/internals/" + channelHandler.getBroadcaster() + "/" + commandHandler.getCommandName());
            }
            commandsObject.put("_id", channelHandler.getChannel());

            wrapper.put("data", commandsObject);
            wrapper.put("links", Memebot.getLinks(Memebot.webBaseURL + "/api/internals/" + channelHandler.getBroadcaster(),
                    Memebot.webBaseURL + "/api/channels/" + channelHandler.getBroadcaster(), null, null));

            return wrapper.toJSONString();
        });

        get("/api/internals/:channel/:command", (req, res) -> {
            res.type("application/json");
            ChannelHandler channelHandler = getChannelForName("#" + req.params(":channel"));
            CommandHandler commandHandler = channelHandler.findCommandForString(req.params(":command"), channelHandler.getInternalCommands());
            if (commandHandler != null) {
                JSONObject jsonObject = commandHandler.toJSONObject();
                return jsonObject.toJSONString();
            }
            res.status(404);
            return "{}";
        });

        HelpWeb.helpWeb();
    }


    public static boolean checkLogin(Request req, String username, String channel) {
        String storedName = req.cookie("login_name");
        String storedOauth = req.cookie("login_oauth");
        UserHandler user = new UserHandler(username, channel);

        if (storedName == null || storedOauth == null) {
            return false;
        }

        return user.getUsername().equals(storedName) && sha1HexString(user.getOauth()).equals(storedOauth);

    }

    public static UserHandler getLoginUserHandler(Request req, String username, String channel) {

        if (checkLogin(req, username, channel)) {
            return getUserHandlerForName(channel, username);
        }

        return new UserHandler("#readonly#", channel);
    }

    public static UserHandler getUserHandlerForName(String channel, String username) {
        ChannelHandler channelHandler = getChannelForName(channel);
        if (channelHandler.getUserList().containsKey(username)) {
            return channelHandler.getUserList().get(username);
        }
        UserHandler userHandler = new UserHandler(username, channel);

        channelHandler.getUserList().put(username, userHandler);
        return userHandler;
    }


    public static ChannelHandler getChannelForName(String channel) {
        ChannelHandler channelHandler = null;
        for (ChannelHandler ch : Memebot.joinedChannels) {
            if (ch.getChannel().equals(channel)) {
                channelHandler = ch;
            }
        }
        return channelHandler;
    }

    public static String sha1HexString(String toDigest) {
        return DigestUtils.sha1Hex(toDigest);
    }
}