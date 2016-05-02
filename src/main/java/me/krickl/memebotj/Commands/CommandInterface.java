package me.krickl.memebotj.Commands;

import me.krickl.memebotj.UserHandler;

/**
 * This file is part of memebotj.
 * Created by unlink on 03/04/16.
 */
public interface CommandInterface {
    void readDB();
    void removeDB();
    void writeDB();
    void overrideDB();
    void initCommand();

    boolean handleCooldown(UserHandler sender);
    boolean startCooldown(UserHandler sender);
    boolean checkCost(UserHandler sender, double cost);
    void commandScript(UserHandler sender, String[] data);
    boolean executeCommand(UserHandler sender, String[] data);

    boolean editCommand(String modType, String newValue, UserHandler sender);

    void update();
}
