package me.krickl.memebotj.Database;


import com.mongodb.Block;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.krickl.memebotj.Exceptions.DatabaseReadException;
import me.krickl.memebotj.Log.MLogger;
import me.krickl.memebotj.Memebot;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This file is part of memebotj.
 * Created by unlink on 17/04/16.
 * This class handles all database reads and writes
 */
public class MongoHandler implements IDatabase<Document> {
    public static MLogger log = MLogger.createLogger(MongoHandler.class.getName());
    public static WriteConcern safeWriteConcern = new WriteConcern(Memebot.writeConcernLevel);
    Document document = new Document();
    private MongoCollection<Document> collection = null;

    private HashMap<String, String> outstandingWrites = new HashMap<>();

    public MongoHandler(MongoDatabase db, String collectionName) {
        collection = db.getCollection(collectionName).withWriteConcern(safeWriteConcern);
    }

    public boolean readDatabase(String id) throws DatabaseReadException {
        return readDatabase(id, "_id");
    }

    public boolean readDatabase(String id, String key) throws DatabaseReadException {
        Document query = new Document(key, id);
        FindIterable cursor = this.collection.find(query);
        document = (Document) cursor.first();

        log.log("Reading db for id: " + id + " key: " + key);

        // todo implement the read - data is stored in contents
        if (document == null) {
            document = new Document();
            throw new DatabaseReadException("Document cannot be null - Consider setting default values and writing to database." +
                    "Created empty document.");
        }

        return true;
    }

    public boolean writeDatabase(String id) {
        return writeDatabase(id, "_id");
    }

    //make sure to set document first
    public boolean writeDatabase(String id, String key) {
        Document query = new Document(key, id);

        log.log("Writing db for id: " + id + " key: " + key);

        try {
            if (this.collection.findOneAndReplace(query, document) == null) {
                this.collection.insertOne(document);
            }
        } catch (Exception e) {
            log.log(e.toString());

            outstandingWrites.put(id, key);

            return false;
        }
        return true;
    }

    public void updateDocument(String key, Object object) {
        if (document == null) {
            log.log("Warning: Document is null! Created empty document!");
            document = new Document();
        }
        if (document.containsKey(key)) {
            document.replace(key, object);
        } else {
            document.append(key, object);
        }
    }

    public Object getObject(String key, Object defaultValue) {
        return document.getOrDefault(key, defaultValue);
    }

    public void removeFromDocument(String key) {
        if (document.containsKey(key)) {
            document.remove(key);
        }
    }

    public boolean removeDatabase(String id) {
        return removeDatabase(id, "_id");
    }

    public boolean removeDatabase(String id, String key) {
        log.log("Removing db for id " + id);
        try {
            if (document != null) {
                this.collection.deleteOne(document);
            }
        } catch (java.lang.IllegalArgumentException e) {
            log.log(e.toString());

            return false;
        }

        return true;
    }

    public ArrayList<Document> getDocuments() {
        FindIterable data = collection.find();
        ArrayList<Document> ret = new ArrayList<>();

        data.forEach(new Block<Document>() {

            @Override
            public void apply(final Document doc) {
                ret.add(doc);
            }
        });

        return ret;
    }

    @Override
    public void update() {
        // check every outstanding write and re-try
        Set<String> keySet = outstandingWrites.keySet();
        for(String k : keySet) {
            // the write will automatically be added again in case of a failure
            writeDatabase(k, outstandingWrites.get(k));
            // remove last outstanding write
            outstandingWrites.remove(k);
        }
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    public void setCollection(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
