package me.krickl.memebotj.Log;

/**
 * Created by unlink on 7/10/2016.
 */
public enum LogLevels {
    WARNING("WARNING"),
    ERROR("ERROR"),
    CRITICAL("CRITICAL"),
    DEBUG("DEBUG"),
    INFO("INFO");

    private final String value;

    private LogLevels(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
