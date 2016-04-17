package me.krickl.memebotj.Database;

import java.util.ArrayList;

/**
 * This file is part of memebotj.
 * Created by unlink on 17/04/16.
 * This will replace all Database code and will be used to unify database reads and writes
 */
public interface DatabaseInterface {
    boolean readDatabase(String id);

    boolean writeDatabase(String id);
}
