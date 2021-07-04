package com.gluonhq.gamebrowser;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * Contains meta-info to a Game
 */
public class GameHolder {
    
    final int STATUS_LOCAL = 1;
    final int STATUS_REMOTE = 2;
    private String name;
    private int version;
    private int status;
    private URL remoteUrl;
    private String jarFile;

    public static GameHolder parse(String entry) {
        String[] split = entry.split("!");
        String n = split[0];
        int v = Integer.parseInt(split[1]);
        URL url = null;
        try {
            url = new URL(split[2]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return new GameHolder(n, v, url);
    }

    public static GameHolder parseLocal(String entry) {
        String[] split = entry.split("!");
        String n = split[0];
        int v = Integer.parseInt(split[1]);
        URL url = null;
        try {
            url = new URL(split[2]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return new GameHolder(n, v, url);
    }

    public GameHolder(String name, int version, URL url) {
        this.name = name;
        this.version = version;
        this.remoteUrl = url;
    }

    public String getName() {
        return this.name;
    }

    public URL getRemoteUrl() {
        return this.remoteUrl;
    }
}
