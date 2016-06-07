package me.krickl.memebotj.Database;

import me.krickl.memebotj.Exceptions.DatabaseReadException;
import org.bson.Document;

import java.util.ArrayList;

/**
 * This file is part of memebotj.
 * Created by unlink on 17/04/16.
 * This will replace all Database code and will be used to unify database reads and writes
 */
public interface DatabaseInterface<T> {

    boolean readDatabase(String id) throws DatabaseReadException;

    boolean writeDatabase(String id);

    boolean removeDatabase(String id);

    void updateDocument(String key, Object value);

    void removeFromDocument(String key);

    Object getObject(String key, Object defaultValue);

    ArrayList<T> getDocuments();
    Document getDocument();
}
