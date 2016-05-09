package me.krickl.memebotj.Utility;

/**
 * This class contains constants for both absolute and relative command power values. Use these values instead of absolute
 * values to ensure maintainability
 * This file is part of memebotj.
 * Created by unlink on 07/04/16.
 */
public class CommandPower {
    public static final int viewerAbsolute = 10;
    public static final int viewer = viewerAbsolute;
    public static final int modAbsolute = 25;
    public static final int mod = modAbsolute - viewerAbsolute;
    public static final int broadcasterAbsolute = 50;
    public static final int broadcaster = broadcasterAbsolute - viewerAbsolute;
    public static final int botModAbsolute = 75;
    public static final int botMod = botModAbsolute - viewerAbsolute;
    public static final int adminAbsolute = 100;
    public static final int admin = adminAbsolute - viewerAbsolute;
}
