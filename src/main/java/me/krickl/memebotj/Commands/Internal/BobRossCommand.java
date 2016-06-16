package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.User.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class BobRossCommand extends CommandHandler {
    public BobRossCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setListContent(new java.util.ArrayList<String>());
        this.setCommandType("list");
        this.setQuotePrefix("");
        this.setQuoteSuffix(" KappaRoss");
        this.setUserCooldownLength(60);
        this.setNeededCooldownBypassPower(CommandPower.adminAbsolute);
        this.setAllowPicksFromList(true);

        this.getListContent().add("There's nothing wrong with having a tree as a friend.");
        this.getListContent().add("The secret to doing anything is believing that you can do it. Anything that you believe you can do strong enough, you can do. Anything. As long as you believe.");
        this.getListContent().add("We don't make mistakes. We just have happy accidents.");
        this.getListContent().add("I think there's an artist hidden at the bottom of every single one of us.");
        this.getListContent().add("You too can paint almighty pictures.");
        this.getListContent().add("No pressure. Just relax and watch it happen.");
        this.getListContent().add("Don’t forget to make all these little things individuals -- all of them special in their own way.");
        this.getListContent().add("Find freedom on this canvas.");
        this.getListContent().add("It’s so important to do something every day that will make you happy.");
        this.getListContent().add("Talent is a pursued interest. Anything that you’re willing to practice, you can do.");
        this.getListContent().add("Make love to the canvas.");
        this.getListContent().add("[Painting] will bring a lot of good thoughts to your heart.");
        this.getListContent().add("We artists are a different breed of people. We're a happy bunch.");
        this.getListContent().add("We want happy paintings. Happy paintings. If you want sad things, watch the news.");
        this.getListContent().add("That's a crooked tree. We'll send him to Washington.");
        this.getListContent().add("Every day is a good day when you paint.");
        this.getListContent().add("I think each of us, sometime in our life, has wanted to paint a picture.");
        this.getListContent().add("We tell people sometimes: We're like drug dealers, come into town and get everybody absolutely addicted to painting. It doesn't take much to get you addicted.");
        this.getListContent().add("They say everything looks better with odd numbers of things. But sometimes I put even numbers -- just to upset the critics.");
        this.getListContent().add("Gotta give him a friend. Like I always say, 'Everyone needs a friend.'");
        this.getListContent().add("See how it fades right into nothing. That's just what you're looking for.");
        this.getListContent().add("If I paint something, I don't want to have to explain what it is.");
        this.getListContent().add("Water's like me. It's lazy. Boy, it always looks for the easiest way to do things.");
        this.getListContent().add("In painting, you have unlimited power. You have the ability to move mountains. You can bend rivers. But when I get home, the only thing I have power over is the garbage.");
        this.getListContent().add("Don’t forget to tell these special people in your life just how special they are to you.");
        this.getListContent().add("Didn’t you know you had that much power? You can move mountains. You can do anything.");
        this.getListContent().add("I like to beat the brush.");
        this.getListContent().add("Just let go -- and fall like a little waterfall.");
        this.getListContent().add("Talk to the tree, make friends with it.");
        this.getListContent().add("I taught my son to paint mountains like these, and guess what? Now he paints the best darn mountains in the industry.");
        this.getListContent().add("I really believe that if you practice enough you could paint the 'Mona Lisa' with a two-inch brush.");
        this.getListContent().add("Be so very light. Be a gentle whisper.");
        this.getListContent().add("Use absolutely no pressure. Just like an angel’s wing.");
        this.getListContent().add("You need the dark in order to show the light.");
        this.getListContent().add("You can do anything you want to do. This is your world.");
        this.getListContent().add("You have to allow the paint to break to make it beautiful.");
        this.getListContent().add("However you think it should be, that’s exactly how it should be.");
        this.getListContent().add("In nature, dead trees are just as normal as live trees.");
        this.getListContent().add("You can have anything you want in the world -- once you help everyone around you get what they want.");
        this.getListContent().add("If you do too much, it’s going to lose its effectiveness.");
        this.getListContent().add("This is happy place; little squirrels live here and play.");
        this.getListContent().add("That’s where the crows will sit. But we’ll have to put an elevator to put them up there because they can’t fly, but they don’t know that, so they still try.");
        this.getListContent().add("Remember how free clouds are. They just lay around in the sky all day long.");
        this.getListContent().add("We don’t really know where this goes -- and I’m not sure we really care.");
        this.getListContent().add("If we’re going to have animals around we all have to be concerned about them and take care of them.");
        this.getListContent().add("You can do anything here -- the only prerequisite is that it makes you happy.");
        this.getListContent().add("Go out on a limb -- that’s where the fruit is.");
        this.getListContent().add("Isn’t it fantastic that you can change your mind and create all these happy things?");
        this.getListContent().add("Anytime you learn, you gain.");
        this.getListContent().add("It’s life. It’s interesting. It’s fun.");
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {

    }
}
