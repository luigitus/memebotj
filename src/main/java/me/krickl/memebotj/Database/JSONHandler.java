package me.krickl.memebotj.Database;

import me.krickl.memebotj.Exceptions.DatabaseReadException;
import org.bson.Document;

import java.util.ArrayList;

/**
 * Created by unlink on 7/9/2016.
 */
public class JSONHandler implements IDatabase {
    @Override
    public boolean readDatabase(String id, String key) throws DatabaseReadException {
        return false;
    }

    @Override
    public boolean readDatabase(String id) throws DatabaseReadException {
        return false;
    }

    @Override
    public boolean writeDatabase(String id, String key) {
        return false;
    }

    @Override
    public boolean writeDatabase(String id) {
        return false;
    }

    @Override
    public boolean removeDatabase(String id) {
        return false;
    }

    @Override
    public void updateDocument(String key, Object value) {

    }

    @Override
    public boolean removeDatabase(String id, String key) {
        return false;
    }

    @Override
    public void removeFromDocument(String key) {

    }

    @Override
    public Object getObject(String key, Object defaultValue) {
        return null;
    }

    @Override
    public ArrayList getDocuments() {
        return null;
    }

    @Override
    public Document getDocument() {
        return null;
    }

    @Override
    public void update() {

    }
}
