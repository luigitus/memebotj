package me.krickl.memebotj.Database;


import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.krickl.memebotj.Exceptions.DatabaseReadException;
import me.krickl.memebotj.Memebot;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This file is part of memebotj.
 * Created by unlink on 17/04/16.
 * This class will eventually handle all db reads/writes to unify that
 */
public class MongoHandler implements DatabaseInterface<Document> {
    public static Logger log = Logger.getLogger(MongoHandler.class.getName());
    private MongoCollection<Document> collection = null;
    Document document = new Document();

    public MongoHandler(MongoDatabase db, String collectionName) {
        collection = db.getCollection(collectionName);
    }

    public boolean readDatabase(String id) throws DatabaseReadException {
        Document query = new Document("_id", id);
        FindIterable cursor = this.collection.find(query);
        document = (Document)cursor.first();

        log.info("Reading db for id " + id);

        // todo implement the read - data is stored in contents
        if (document == null) {
            document = new Document();
            throw new DatabaseReadException("Document cannot be null - Consider setting default values and writing to database");
        }

        return true;
    }

    //make sure to set document first
    public boolean writeDatabase(String id) {
        Document query = new Document("_id", id);

        log.info("Writing db for id " + id);

        try {
            if (this.collection.findOneAndReplace(query, document) == null) {
                this.collection.insertOne(document);
            }
        } catch(Exception e) {
            log.warning(e.toString());
        }
        return true;
    }

    public void updateDocument(String key, Object object) {
        if(document == null) {
            log.info("Warning: Document is null");
            document = new Document();
        }
        if(document.containsKey(key)) {
            document.replace(key, object);
        } else {
            document.append(key, object);
        }
    }

    public Object getObject(String key, Object defaultValue) {
        return document.getOrDefault(key, defaultValue);
    }

    public void removeFromDocument(String key) {
        if(document.containsKey(key)) {
            document.remove(key);
        }
    }

    public boolean removeDatabase(String id) {
        log.info("Removing db for id " + id);
        try {
            if(document != null) {
                this.collection.deleteOne(document);
            }
        } catch(java.lang.IllegalArgumentException e) {
            log.warning(e.toString());
        }

        return true;
    }

    public ArrayList<Document> getDocuments() {
        FindIterable data = collection.find();
        ArrayList<Document> ret = new ArrayList<>();

        data.forEach(new Block<Document>(){

            @Override
            public void apply(final Document doc) {
                ret.add(doc);
            }
        });

        return ret;
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
