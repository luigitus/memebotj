package me.krickl.memebotj.Connection.Discord;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Connection.ConnectionInterface;
import me.krickl.memebotj.Exceptions.LoginException;
import me.krickl.memebotj.Utility.MessagePackage;

import java.io.IOException;

/**
 * This file is part of memebotj.
 * Created by lukas on 6/13/2016.
 */
public class DiscordConnectionHandler implements ConnectionInterface {
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
                if(message.getContent().equals("!hello")) {
                    message.reply("world");
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
    public String botMode() {
        return "discord";
    }
}
