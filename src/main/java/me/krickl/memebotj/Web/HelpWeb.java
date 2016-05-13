package me.krickl.memebotj.Web;

import me.krickl.memebotj.Memebot;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

/**
 * This file is part of memebotj.
 * Created by unlink on 03/05/16.
 */
public class HelpWeb {
    public static void helpWeb() {
        get("/help", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "web/help/help.vm");
        }, new VelocityTemplateEngine());

        get("/help/:page", (req, res) -> {
            String page = req.params(":page");
            Map<String, Object> model = new HashMap<>();

            model.put("web", Memebot.webBaseURL);

            return new ModelAndView(model, "web/help/" + page + ".vm");
        }, new VelocityTemplateEngine());
    }
}
