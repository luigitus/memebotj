package me.krickl.memebotj.Web;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Database.MongoHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import org.bson.Document;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.ArrayList;
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
            String channel = "#" + req.params(":channel");
            ChannelHandler channelHandler = null;
            for(ChannelHandler ch : Memebot.joinedChannels) {
                if(ch.getChannel().equals(channel)) {
                    channelHandler = ch;
                }
            }

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "channel.vm");
        }, new VelocityTemplateEngine());

        get("/commands/list/internals/:channel", (req, res) -> {
            String channel = "#" + req.params(":channel");
            ChannelHandler channelHandler = null;
            for(ChannelHandler ch : Memebot.joinedChannels) {
                if(ch.getChannel().equals(channel)) {
                    channelHandler = ch;
                }
            }

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "internals.vm");
        }, new VelocityTemplateEngine());

        get("/commands/:channel/:command", (req, res) -> {
            String channel = "#" + req.params(":channel");
            String command = req.params(":command");
            ChannelHandler channelHandler = null;
            for(ChannelHandler ch : Memebot.joinedChannels) {
                if(ch.getChannel().equals(channel)) {
                    channelHandler = ch;
                }
            }
            int i = -1;
            if(channelHandler != null) {
                i = channelHandler.findCommand(command);
            }
            CommandHandler commandHandler = null;
            if(i != -1) {
                commandHandler = channelHandler.getChannelCommands().get(i);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("command", commandHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "command.vm");
        }, new VelocityTemplateEngine());

        get("/commands/internals/:channel/:command", (req, res) -> {
            String channel = "#" + req.params(":channel");
            String command = req.params(":command");

            ChannelHandler channelHandler = null;
            for(ChannelHandler ch : Memebot.joinedChannels) {
                if(ch.getChannel().equals(channel)) {
                    channelHandler = ch;
                }
            }
            int i = -1;
            if(channelHandler != null) {
                i = channelHandler.findCommand(command, channelHandler.getInternalCommands(), 1);
            }
            CommandHandler commandHandler = null;
            if(i != -1) {
                commandHandler = channelHandler.getInternalCommands().get(i);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("command", commandHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "internalcommand.vm");
        }, new VelocityTemplateEngine());

        get("/songs/:channel/player", (req, res) -> {
            String channel = "#" + req.params(":channel");

            ChannelHandler channelHandler = null;
            for(ChannelHandler ch : Memebot.joinedChannels) {
                if(ch.getChannel().equals(channel)) {
                    channelHandler = ch;
                }
            }

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "songrequest.vm");
        }, new VelocityTemplateEngine());

        get("/filesnames/:channel", (req, res) -> {
            String channel = "#" + req.params(":channel");

            ChannelHandler channelHandler = null;
            for(ChannelHandler ch : Memebot.joinedChannels) {
                if(ch.getChannel().equals(channel)) {
                    channelHandler = ch;
                }
            }

            Map<String, Object> model = new HashMap<>();

            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "filenames.vm");
        }, new VelocityTemplateEngine());

        get("/users/listnames/:channel", (req, res) -> {
            String channel = "#" + req.params(":channel");

            ChannelHandler channelHandler = null;
            for(ChannelHandler ch : Memebot.joinedChannels) {
                if(ch.getChannel().equals(channel)) {
                    channelHandler = ch;
                }
            }

            Map<String, Object> model = new HashMap<>();
            MongoHandler mh = null;
            if(Memebot.channelsPrivate.contains(channel)) {
                mh = new MongoHandler(Memebot.dbPrivate, channel + "_users");
            } else {
                mh = new MongoHandler(Memebot.db, channel + "_users");
            }

            ArrayList<String> userList = new ArrayList<String>();
            for(Document doc : mh.getDocuments()) {
                userList.add(doc.getOrDefault("_id", "#error#").toString());
            }

            model.put("channel", channelHandler);
            model.put("userlist", userList);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "userlist.vm");
        }, new VelocityTemplateEngine());

        get("/users/listnames/:channel/:user", (req, res) -> {
            String channel = "#" + req.params(":channel");
            String user = req.params(":user");
            ChannelHandler channelHandler = null;
            for(ChannelHandler ch : Memebot.joinedChannels) {
                if(ch.getChannel().equals(channel)) {
                    channelHandler = ch;
                }
            }
            UserHandler userHandler = new UserHandler(user, channel);

            Map<String, Object> model = new HashMap<>();

            model.put("channel", channelHandler);
            model.put("user", userHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "userdetail.vm");
        }, new VelocityTemplateEngine());
    }
}
