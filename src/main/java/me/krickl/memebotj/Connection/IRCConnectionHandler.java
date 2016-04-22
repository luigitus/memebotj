package me.krickl.memebotj.Connection;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Exceptions.LoginException;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;
import me.krickl.memebotj.Utility.Cooldown;
import me.krickl.memebotj.Utility.MessagePackage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * This file is part of memebotj.
 * Created by unlink on 09/04/16.
 */
public class IRCConnectionHandler implements ConnectionInterface {
    String server = "";
    String botNick = "";
    String password = "";
    int port = -1;
    Socket ircSocket  = null;
    BufferedReader inFromServer = null;
    DataOutputStream outToServer = null;
    boolean debugMode = false;

    public IRCConnectionHandler(String serverNew, int portNew, String botNickNew, String passwordNew) {
        server = serverNew;
        botNick = botNickNew;
        password = passwordNew;
        port = portNew;

        try {
            this.ircSocket = new Socket(server, port);
        } catch(IOException e) {
            e.printStackTrace();
        }
        try {
            if (ircSocket != null) {
                this.inFromServer = new BufferedReader(new InputStreamReader(this.ircSocket.getInputStream(), "UTF-8"));

                this.outToServer = new DataOutputStream(this.ircSocket.getOutputStream());

                this.outToServer.writeBytes("PASS " + this.password + "\n");
                this.outToServer.writeBytes("NICK " + this.botNick + "\n");
                this.sendMessageBytes("CAP REQ :twitch.tv/membership\n");
                this.sendMessageBytes("CAP REQ :twitch.tv/commands\n");
                this.sendMessageBytes("CAP REQ :twitch.tv/tags\n");

            } else {
                debugMode = true;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void ping() throws IOException {
        this.outToServer.writeBytes("PING :PONG\n");
    }

    public String recvData() throws LoginException {
        String ircmsg = "";
        if(inFromServer == null) {
            throw new LoginException("No input from server found");
        }
        try {
            if (this.debugMode) {
                Scanner input = new Scanner(System.in);
                ircmsg = input.nextLine();
            } else {
                ircmsg = this.inFromServer.readLine().replace("\n", "").replace("\r", "");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        /*String channel = "";
        int hashIndex = ircmsg.indexOf(" #");

        if (hashIndex > 0) {
            for (int i = hashIndex + 1; i < ircmsg.length(); i++) {
                if (ircmsg.charAt(i) != ' ') {
                    channel = channel + ircmsg.charAt(i);
                } else {
                    break;
                }
            }
        }

        System.out.println("<" + channel + "> " + ircmsg);*/

        if (ircmsg.contains("PING :")) {
            try {
                this.ping();
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else if (ircmsg.contains(":tmi.twitch.tv NOTICE * :Error logging in")) {
            throw new LoginException("Error logging in");
        }

        return ircmsg;
    }

    public MessagePackage handleMessage(String rawircmsg, ChannelHandler channelHandler) {
        String senderName = "";
        HashMap<String, String> ircTags = new HashMap<String, String>();
        String[] msgContent = {""};
        String[] ircmsgBuffer = rawircmsg.split(" ");
        String messageType = "UNDEFINED";
        int i = 0;

        String channel = "";

        //get channel name
        // todo do this better
        int hashIndex = rawircmsg.indexOf(" #");

        if (hashIndex > 0) {
            for (i = hashIndex + 1; i < rawircmsg.length(); i++) {
                if (rawircmsg.charAt(i) != ' ') {
                    channel = channel + rawircmsg.charAt(i);
                } else {
                    break;
                }
            }
        }

        System.out.println("<" + channel + ">" + rawircmsg);


        i = 0;
        // handle message
        while (i < ircmsgBuffer.length) {
            String msg = ircmsgBuffer[i];
            if ((msg.equals("PRIVMSG") || msg.equals("WHISPER") || msg.equals("MODE") || msg.equals("PART") || msg.equals("JOIN") || msg.equals("CLEARCHAT")) && messageType.equals("UNDEFINED")) {
                messageType = msg;
            }
            if (msg.charAt(0) == '@' && i == 0) {
                String[] tagList = msg.split(";");
                for (String tag : tagList) {
                    try {
                        ircTags.put(tag.split("=")[0], tag.split("=")[1]);
                    } catch(ArrayIndexOutOfBoundsException e) {
                        //e.printStackTrace();
                    }
                }
            } else if (i == 0 || (i == 1 && senderName.isEmpty())) {
                boolean exclaReached = false;
                for (int j = 0; j <  msg.length(); j++) {
                    if (msg.charAt(j) == '!') {
                        exclaReached = true;
                        break;
                    }
                    if (msg.charAt(j) != ':') {
                        senderName = senderName + msg.charAt(j);
                    }
                }
                if (!exclaReached) {
                    senderName = "#internal#";
                }
            }
            if ((messageType.equals("PRIVMSG") || messageType.equals("WHISPER")) && i > 3) {
                if (i == 4) {
                    msgContent = new String[ircmsgBuffer.length - 4];
                    msgContent[i - 4] = msg.substring(1);
                } else {
                    msgContent[i - 4] = msg;
                }
            }
            i += 1;
        }
        if (!channelHandler.getUserList().containsKey(senderName) && !senderName.isEmpty()) {
            UserHandler newUser = new UserHandler(senderName, channelHandler.getChannel());
            channelHandler.getUserList().put(senderName, newUser);
        }

        UserHandler sender = channelHandler.getUserList().get(senderName);
        //todo make sure sender is not removed
        sender.setShouldBeRemoved(false);

        if (!messageType.equals("PRIVMSG") && !messageType.equals("WHISPER")) {
            String[] ircmsgList = rawircmsg.split(" ");
            if (ircmsgList[1] == null) {
                return null;
            }
            if (ircmsgList[1].equals("MODE")) {
                UserHandler user = null;
                if (!channelHandler.getUserList().containsKey(ircmsgList[4])) {
                    user = new UserHandler(ircmsgList[4], channelHandler.getChannel());
                    channelHandler.getUserList().put(senderName, user);
                } else {
                    user = channelHandler.getUserList().get(ircmsgList[4]);
                }
                if (user != null) {
                    if (ircmsgList[3].equals("+o")) {
                        user.setModerator(true);
                        if (!user.isUserBroadcaster()) {
                            user.setCommandPower(CommandPower.modAbsolute);
                        }
                    } else {
                        user.setModerator(false);
                        user.setCommandPower(CommandPower.viewer);
                    }
                }
            } else if (ircmsgList[1].equals("PART")) {
                if (sender != null) {
                    //todo mark user for removal 5 minutes after part message
                    if (channelHandler.getUserList().containsKey(sender.getUsername())) {
                        //this.userList.get(sender.getUsername).writeDBUserData()
                        //this.userList.remove(sender.getUsername)
                        sender.setShouldBeRemoved(true);
                        sender.setRemoveCooldown(new Cooldown(300));
                    }
                }
            } else if (ircmsgList[1].equals("JOIN")) {
                if (sender != null) {
                    //send autogreet if the channel allows autogreets
                    if (channelHandler.isAllowAutogreet() && !sender.getAutogreet().equals("")) {
                        sender.sendAutogreet(channelHandler);
                    }
                }
            } else if (ircmsgList[1].equals("CLEARCHAT")) {
                try {
                    if (channelHandler.getUserList().containsKey(ircmsgList[3].replace(":", ""))) {
                        channelHandler.getUserList().get(ircmsgList[3].replace(":", "")).setTimeouts(channelHandler.getUserList().get(ircmsgList[3].replace(":", "")).getTimeouts() + 1);
                        channelHandler.getUserList().get(ircmsgList[3].replace(":", "")).writeDB();
                    } else {
                        UserHandler uh = new UserHandler(ircmsgList[3].replace(":", ""), channelHandler.getChannel());
                        if (!uh.isNewUser()) {
                            uh.setTimeouts(uh.getTimeouts() + 1);
                            uh.writeDB();
                        }
                    }
                } catch(ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if(ircTags.containsKey("display-name")) {
                sender.setNickname(ircTags.get("display-name"));
            }
            if (ircTags.containsKey("user-type")) {
                if (ircTags.get("user-type").equals("mod") && !sender.isUserBroadcaster()) {
                    sender.setModerator(true);
                    sender.setCommandPower(CommandPower.modAbsolute);
                } else if (!sender.isUserBroadcaster()) {
                    sender.setModerator(false);
                    sender.setCommandPower(CommandPower.viewerAbsolute);
                }
            } else {
                sender.setModerator(false);
                sender.setCommandPower(CommandPower.viewerAbsolute);
            }
            if (sender.getUsername().equalsIgnoreCase(channelHandler.getBroadcaster())) {
                sender.setModerator(true);
                sender.setUserBroadcaster(true);
                sender.setCommandPower(CommandPower.broadcasterAbsolute);
            }
            for (String user : Memebot.botAdmins) {
                if (user.equalsIgnoreCase(sender.getUsername())) {
                    sender.setCommandPower(CommandPower.adminAbsolute);
                }
            }
        }

        if(messageType.equals("WHISPER")) {
            try {
                messageType = "WHISPER";
                channel = msgContent[0];
                msgContent = Arrays.copyOfRange(msgContent, 1, msgContent.length);
            } catch(ArrayIndexOutOfBoundsException e) {

            }
        }

        return new MessagePackage(msgContent, sender, messageType, channel);
    }

    public void close() {
        try {
            this.outToServer.close();
            this.inFromServer.close();
            this.ircSocket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        System.out.println(msg);
        if(outToServer == null) {
            return;
        }
        try {
            outToServer.flush();
            outToServer.write(msg.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageBytes(String msg) {
        System.out.println(msg);
        if(outToServer == null) {
            return;
        }
        try {
            outToServer.flush();
            outToServer.writeBytes(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getBotNick() {
        return botNick;
    }

    public void setBotNick(String botNick) {
        this.botNick = botNick;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Socket getIrcSocket() {
        return ircSocket;
    }

    public void setIrcSocket(Socket ircSocket) {
        this.ircSocket = ircSocket;
    }

    public BufferedReader getInFromServer() {
        return inFromServer;
    }

    public void setInFromServer(BufferedReader inFromServer) {
        this.inFromServer = inFromServer;
    }

    public DataOutputStream getOutToServer() {
        return outToServer;
    }

    public void setOutToServer(DataOutputStream outToServer) {
        this.outToServer = outToServer;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
}
