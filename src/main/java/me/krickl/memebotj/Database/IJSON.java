package me.krickl.memebotj.Database;

import org.json.simple.JSONObject;

/**
 * Created by unlink on 6/6/2016.
 */
public interface IJSON {
    JSONObject toJSONObject();

    String toJSONSString();

    boolean fromJSON(String jsonString);
}
