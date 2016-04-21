package me.krickl.memebotj.web;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.krickl.memebotj.Memebot;
import org.bson.Document;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static spark.Spark.*;


/**
 * This file is part of memebotweb.
 * Created by unlink on 12/04/16.
 */
public class WebHandler {
    public static void webHandler() {
        externalStaticFileLocation("./public");

        get("/channels", (req, res) -> {
            return "Coming soon(tm)";
        });

        get("/commands/:channel", (req, res) -> {
            String channel = "#" + req.params(":channel");
            WebChannelHandler channelHandler = new WebChannelHandler(channel);

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "channel.vm");
        }, new VelocityTemplateEngine());

        get("/commands/list/internals/:channel", (req, res) -> {
            String channel = "#" + req.params(":channel");
            WebChannelHandler channelHandler = new WebChannelHandler(channel);

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "internals.vm");
        }, new VelocityTemplateEngine());

        get("/commands/:channel/:command", (req, res) -> {
            String channel = "#" + req.params(":channel");
            String command = req.params(":command");
            WebChannelHandler channelHandler = new WebChannelHandler(channel);
            WebCommandHandler commandHandler = new WebCommandHandler(channelHandler.getChannel(), command, null);

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("command", commandHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "command.vm");
        }, new VelocityTemplateEngine());

        get("/commands/internals/:channel/:command", (req, res) -> {
            String channel = "#" + req.params(":channel");
            String command = req.params(":command");
            WebChannelHandler channelHandler = new WebChannelHandler(channel);
            WebCommandHandler commandHandler = new WebCommandHandler(channelHandler.getChannel(), command, "#internal#");

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("command", commandHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "internalcommand.vm");
        }, new VelocityTemplateEngine());

        get("/commands/songs/:channel/player", (req, res) -> {
            String channel = "#" + req.params(":channel");
            String command = req.params(":command");
            WebChannelHandler channelHandler = new WebChannelHandler(channel);

            Map<String, Object> model = new HashMap<>();
            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "songrequest.vm");
        }, new VelocityTemplateEngine());

        get("/commands/files/names/:channel", (req, res) -> {
            String channel = "#" + req.params(":channel");
            WebChannelHandler channelHandler = new WebChannelHandler(channel);

            Map<String, Object> model = new HashMap<>();

            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "filenames.vm");
        }, new VelocityTemplateEngine());

        get("/commands/users/list/names/:channel", (req, res) -> {
            String channel = "#" + req.params(":channel");
            WebChannelHandler channelHandler = new WebChannelHandler(channel);

            Map<String, Object> model = new HashMap<>();

            model.put("channel", channelHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "userlist.vm");
        }, new VelocityTemplateEngine());

        get("/commands/users/list/names/:channel/:user", (req, res) -> {
            String channel = "#" + req.params(":channel");
            String user = req.params(":user");
            WebChannelHandler channelHandler = new WebChannelHandler(channel);
            WebUserHandler userHandler = new WebUserHandler(user, channel);

            Map<String, Object> model = new HashMap<>();

            model.put("channel", channelHandler);
            model.put("user", userHandler);
            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "userdetail.vm");
        }, new VelocityTemplateEngine());
    }
}
