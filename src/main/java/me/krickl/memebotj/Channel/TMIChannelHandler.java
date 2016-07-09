package me.krickl.memebotj.Channel;

import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Commands.CommandReference;
import me.krickl.memebotj.Connection.IConnection;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.User.UserHandler;
import me.krickl.memebotj.Utility.Cooldown;
import me.krickl.memebotj.Utility.MessagePackage;
import me.krickl.memebotj.Utility.RNGObject;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * This file is part of memebotj.
 * Created by lukas on 6/16/2016.
 */
public class TMIChannelHandler extends ChannelHandler {
    private Cooldown autoRehostCooldown = new Cooldown(900); // autorehost after the cooldown is over
    private boolean isHosting = false;

    public TMIChannelHandler(String channel, IConnection connection) {
        super(channel, connection);
    }

    @Override
    public void partChannel(String channel) {
        this.connection.sendMessageBytes("PART " + channel + "\n");
        boolean isInList = false;
        ChannelHandler removeThisCH = null;
        for (ChannelHandler ch : Memebot.joinedChannels) {
            if (ch.getChannel().equalsIgnoreCase(channel)) {
                isInList = true;
                removeThisCH = ch;
                break;
            }
        }

        if (isInList && removeThisCH != null) {
            Memebot.joinedChannels.remove(removeThisCH);
            new File(Memebot.memebotDir + "/channels/" + channel).delete();

        }
        this.sendMessage("Leaving channel :(", this.channel, null, false);
        this.isJoined = false;
        this.t.interrupt();
        this.connection.close();
        Memebot.joinedChannels.remove(removeThisCH);
    }

    @Override
    protected void joinChannel(String channel) {
        this.connection.sendMessageBytes("JOIN " + channel + "\n");
        boolean isInList = false;

        for (ChannelHandler ch : Memebot.joinedChannels) {
            if (ch.getChannel().equalsIgnoreCase(channel)) {
                isInList = true;
                break;
            }
        }

        if (!isInList) {
            try {
                new File(Memebot.memebotDir + "/channels/" + channel).createNewFile();
            } catch (IOException e) {
                log.log(Level.WARNING, e.toString());
            }
        }
        this.isJoined = true;
    }

    @Override
    public void sendMessage(String message, String channel, UserHandler sender, boolean whisper, boolean forcechat,
                            boolean allowIgnored) {
        String msg = message;
        if (msg.isEmpty()) {
            return;
        }
        if (!this.preventMessageCooldown.canContinue()) {
            return;
        }
        if (this.silentMode) {
            return;
        }
        if (useDiscord) {
            // todo holy this is hacky
            lastMessage = lastMessage + message + "\n";
            return;
        }

        if (this.currentMessageCount >= me.krickl.memebotj.Memebot.messageLimit) {
            log.log(Level.SEVERE ,"Reached global message limit for 30 seconds. try again later");
            this.preventMessageCooldown.startCooldown();
        }
        // ignore /ignore to avoid people being ignored by the bot /ban glitch with parameters discovered by CatlyMeows
        // todo implement proper fix
        String[] ignoredMessages = new String[]{"/ignore", "/color", ".ignore", ".color", ".unmod", "/unmod",
                "/mod", ".mod", "/ban", ".ban", "/timeout", ".timeout"};
        for (String ignoredStr : ignoredMessages) {
            if (msg.startsWith(ignoredStr) && !allowIgnored) {
                msg = msg.replaceFirst("/", "");
                msg = msg.replaceFirst(".", "");
            }
        }

        this.currentMessageCount += 1;
        //log to file
        System.out.println("<" + channel + ">" + msg);

        if(sender == null) {
            sender = new UserHandler("#internal#", this.getChannel());
        }

        // force outut to both chat and whisper
        if (forcechat && (whisper || useWhisper)) {
            if (sender.getUsername().equals("#readonly#")) {
                this.connection.sendMessage("PRIVMSG " + sender.getChannelOrigin() + " : " + msg + "\n");
            } else {
                this.connection.sendMessage("PRIVMSG " + channel + " :" + msg + "\n");
            }
        }

        if (!whisper && !useWhisper) {
            if (sender.getUsername().equals("#readonly#")) {
                this.connection.sendMessage("PRIVMSG " + sender.getChannelOrigin() + " : " + msg + "\n");
            } else {
                this.connection.sendMessage("PRIVMSG " + channel + " :" + msg + "\n");
            }
        } else {
            if (sender.getUsername().equals("#readonly#")) {
                this.connection.sendMessage("PRIVMSG #jtv :/w " + sender.getUsername() + " <" + sender.getChannelOrigin() + "> " + msg + "\n");
            } else {
                this.connection.sendMessage("PRIVMSG #jtv :/w " + sender.getUsername() + " <" + channel + "> " + msg + "\n");
            }
        }
    }

    @Override
    public void update() {
        super.update();

        // autorehost if channel is offline
        if(!isLive && autoRehostCooldown.canContinue() && !isHosting && enableAutoHost) {
            ArrayList<String> channelsToHost = new ArrayList<>();

            for(ChannelHandler channelHandler : Memebot.joinedChannels) {
                if(!channelHandler.isOpOutOfAutofAutohost() && channelHandler.isLive()) {
                    channelsToHost.add(channelHandler.getBroadcaster());
                }
            }
            if(channelsToHost.size() > 2) {
                SecureRandom random = new SecureRandom();
                String hostTarget = channelsToHost.get(random.nextInt(channelsToHost.size() - 1));

                sendMessage("/host" + hostTarget, channel, userList.get("#internal#"), false);

                sendMessage("Auto-Hosting: " + hostTarget, channel, userList.get("#internal#"), false);

                isHosting = true;
            }
        }
        autoRehostCooldown.startCooldown();
    }

    @Override
    public String handleMessage(String rawmessage) {
        lastMessage = "";

        useWhisper = false;
        MessagePackage msgPackage = connection.handleMessage(rawmessage, this);

        if (msgPackage == null) {
            return lastMessage;
        }
        msgPackage.handleAlias(aliasList);

        // if channel does not match ignore the message
        if (!msgPackage.channel.equals(this.channel) && !msgPackage.channel.equals("#discord#")) {
            // todo send message to the right channel
            return lastMessage;
        }

        if(msgPackage.messageID.equals("host_off")) {
            autoRehostCooldown.startCooldown();
            isHosting = false;
        }

        if (msgPackage.messageType.equals("WHISPER")) {
            useWhisper = true;
            msgPackage.messageType = "PRIVMSG";
        }

        String[] msgContent = msgPackage.messageContent;
        UserHandler sender = msgPackage.sender;
        String msgType = msgPackage.messageType;

        // todo so bad
        if (msgPackage.channel.equals("#discord#")) {
            useDiscord = true;
        }

        if (!msgType.equals("PRIVMSG")) {
            useWhisper = false;
            return lastMessage;
        }

        //log chat content
        String messageString = "";
        for (String msg : msgContent) {
            messageString = messageString + msg + " ";
        }
        System.out.println("<" + channel + ">" + sender.getUsername() + ": " + messageString);

        String msg = msgContent[0];
        String[] data = java.util.Arrays.copyOfRange(msgContent, 0, msgContent.length);
        if (this.purgeURLS) {
            for (String username : Memebot.globalBanList) {
                if (sender.getUsername().equals(username) && !sender.isModerator()) {
                    this.sendMessage("/ban " + sender.getUsername(), channel, null, false);
                }
            }

            for (String message : Memebot.urlBanList) {
                if (rawmessage.contains(message)) {
                    this.sendMessage("/timeout " + sender.getUsername() + " 1", channel, null, false);
                }
            }

            for (String message : Memebot.phraseBanList) {
                if (rawmessage.contains(message)) {
                    this.sendMessage("/timeout " + sender.getUsername() + " 1", channel, null, false);
                }
            }
        }

        CommandHandler ch = null;
        CommandReference cr = null;

        //internal text triggers
        for (String s : msgContent) {
            ch = this.findCommandForString(s, this.internalCommands);

            if (ch != null) {
                if (ch.isTextTrigger()) {
                    ch.executeCommand(sender, new String[]{"", ""});
                }
            }
        }

        //internal commands
        ch = this.findCommandForString(msg, this.internalCommands);
        if (ch != null) {
            if (!ch.isTextTrigger()) {
                ch.executeCommand(sender, java.util.Arrays.copyOfRange(data, 1, data.length));
            }
        }

        //channel commands
        cr = this.findCommandReferneceForString(msg, this.channelCommands);
        if (cr != null) {
            if (!cr.getCH().isTextTrigger()) {
                cr.executeCommand(sender, data);
            }
        }

        //text triggers
        for (String s : msgContent) {
            cr = this.findCommandReferneceForString(s, this.channelCommands);

            if (cr != null) {
                if (cr.getCH().isTextTrigger()) {
                    cr.executeCommand(sender, new String[]{"", ""});
                }
            }
        }

        // other channel's commands
        for (ChannelHandler channelHandler : Memebot.joinedChannels) {
            for (String och : this.otherLoadedChannels) {
                String channel = channelHandler.getBroadcaster();
                if (channelHandler.getChannel().equals(och) || channelHandler.getBroadcaster().equals(och)) {
                    cr = channelHandler.findCommandReferneceForString(msg.replace(och.replace("#", "") + ".", ""),
                            channelHandler.getChannelCommands());
                    if (cr != null && msg.contains(channel)) {
                        cr.executeCommand(readOnlyUser, data);
                    }
                }
            }
        }

        //set user activity
        //sender.setTimeSinceActivity(System.currentTimeMillis())
        useWhisper = false;
        useDiscord = false;

        return lastMessage;
    }
}
