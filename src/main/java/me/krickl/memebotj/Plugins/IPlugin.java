package me.krickl.memebotj.Plugins;

/**
 * Created by unlink on 7/13/2016.
 */
public class IPlugin implements Runnable {
    private Thread t = null;

    public void start() {
        if (t == null) {
            t = new Thread(this, this.getClass().getName());
            t.start();
        }
    }

    public void run() {

    }

    public Thread getT() {
        return t;
    }
}
