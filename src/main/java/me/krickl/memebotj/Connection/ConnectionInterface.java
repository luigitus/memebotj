package me.krickl.memebotj.Connection;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Exceptions.LoginException;
import me.krickl.memebotj.Utility.MessagePackage;

import java.io.IOException;

/**
 * This file is part of memebotj.
 * Created by unlink on 09/04/16.
 */
public interface ConnectionInterface {
    void ping() throws IOException;

    String recvData() throws LoginException;

    MessagePackage handleMessage(String rawircmsg, ChannelHandler channelHandler);

    void close();

    void sendMessage(String msg);

    void sendMessageBytes(String msg);

    String getBotNick();

    boolean canReceive();
}
