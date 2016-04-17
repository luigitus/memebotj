package me.krickl.memebotj.Database;


import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.krickl.memebotj.Commands.CommandHandler;
import org.bson.Document;

import java.util.ArrayList;

/**
 * This file is part of memebotj.
 * Created by unlink on 17/04/16.
 * This class will eventually handle all db reads/writes to unify that
 */
public class MongoHandler implements DatabaseInterface {
    private MongoCollection<Document> collection = null;
    Document document = new Document();
    ArrayList<Document> contents = new ArrayList<>();

    public MongoHandler(MongoDatabase db, String collectionName) {
        collection = db.getCollection(collectionName);
    }

    public boolean readDatabase(String id) {
        contents.clear();
        Document query = new Document("_id", id);
        FindIterable cursor = this.collection.find(query);
        document = (Document)cursor.first();

        // todo implement the read - data are stored in contents
        if (document != null) {

        }

        return true;
    }

    //make sure to set document first
    public boolean writeDatabase(String id) {
        Document query = new Document("_id", id);

        try {
            if (this.collection.findOneAndReplace(query, document) == null) {
                this.collection.insertOne(document);
            }
        } catch(Exception e) {
            e.printStackTrace();
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

    public ArrayList<Document> getContents() {
        return contents;
    }

    public void setContents(ArrayList<Document> contents) {
        this.contents = contents;
    }
}
