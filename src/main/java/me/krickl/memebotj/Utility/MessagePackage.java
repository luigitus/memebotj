package me.krickl.memebotj.Utility;

import me.krickl.memebotj.UserHandler;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This file is part of memebotj.
 * Created by unlink on 07/04/16.
 */
public class MessagePackage {
    public String[] messageContent = null;
    public UserHandler sender = null;
    public String messageType = null;
    public String channel = null;

    public MessagePackage(String[] content, UserHandler uh, String type, String channel) {
        messageContent = content;
        sender = uh;
        messageType = type;
        this.channel = channel;
    }

    public void handleAlias(Document aliasDoc) {
        ArrayList<String> tmpArray = new ArrayList<>(Arrays.asList(messageContent));
        for (String obj : aliasDoc.keySet()) {
            if (messageContent[0].equals(obj)) {
                tmpArray.remove(0);
                String[] tmp = aliasDoc.get(obj).toString().split(" ");
                for (int i = tmp.length - 1; i >= 0; i--) {
                    String str = tmp[i];
                    tmpArray.add(0, str);
                }
            }
        }

        messageContent = new String[tmpArray.size()];
        messageContent = tmpArray.toArray(messageContent);
    }
}
