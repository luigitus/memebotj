package me.krickl.memebotj.Connection.Discord;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Connection.IConnection;
import me.krickl.memebotj.Connection.Protocols;
import me.krickl.memebotj.Exceptions.LoginException;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.Utility.MessagePackage;

import java.io.IOException;

/**
 * This file is part of memebotj.
 * Created by lukas on 6/13/2016.
 */
public class DiscordConnectionHandler implements IConnection {
    DiscordAPI api;
    public DiscordConnectionHandler(String oauth)  {
        api = Javacord.getApi(oauth, true);
        api.connect(new FutureCallback<DiscordAPI>() {
            @Override
            public void onSuccess(final DiscordAPI api) {
                // do what you want now
            }

            @Override
            public void onFailure(Throwable t) {
                // login failed
                t.printStackTrace();
            }
        });

        api.registerListener(new MessageCreateListener() {
            @Override
            public void onMessageCreate(DiscordAPI api, Message message) {

                boolean tts = true;
                String raw = message.getContent();
                if(raw.startsWith("!tts ")) {
                    tts = false;
                    raw.replace("!tts ", "");
                }

                if(raw.startsWith("!mejoin")) {
                    return;
                }

                // todo make this less hacky and shit
                for(ChannelHandler channelHandler : Memebot.joinedChannels) {
                    System.out.print(message.getChannelReceiver().getServer().getId() + " " + channelHandler.getDiscordChannel() + "\n");
                    if(channelHandler.getDiscordChannel().contains(message.getChannelReceiver().getServer().getId())) {
                        String msg = channelHandler.handleMessage("@badges=;color=#4EBD3A;display-name=#readonly#;emotes=;" +
                                "mod=0;room-id=-1;subscriber=0;turbo=0;user-id=-1;user-type= :#readonly#!#readonly#@#readonly#." +
                                "tmi.twitch.tv PRIVMSG #discord# :" + raw);

                        message.reply(msg);
                    }
                }
            }
        });
    }

    @Override
    public void ping() throws IOException {

    }

    @Override
    public String recvData() throws LoginException {
        return null;
    }

    @Override
    public MessagePackage handleMessage(String rawircmsg, ChannelHandler channelHandler) {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public void sendMessage(String msg) {
    }

    @Override
    public void sendMessageBytes(String msg) {

    }

    @Override
    public String getBotNick() {
        return null;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    public Protocols botMode() {
        return Protocols.DISCORD;
    }
}
