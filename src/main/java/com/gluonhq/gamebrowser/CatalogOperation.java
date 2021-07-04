/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gluonhq.gamebrowser;

import com.almasb.fxgl.app.GameApplication;
import com.gluonhq.attach.storage.StorageService;
import java.io.File;
import java.io.IOException;


import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static javafx.application.Application.STYLESHEET_MODENA;

/**
 *
 * @author johan
 */
public class CatalogOperation {

    private final StorageService storageService;
    private final File root;
    
    public CatalogOperation() {
        this.storageService = StorageService.create().get();
        root = new File(this.storageService.getPrivateStorage().get(), ".gamebrowser");
        Path path = Path.of(root.getAbsolutePath(), "localcatalog.txt");
    if (!path.toFile().exists()) {
        try {
            Files.write(path,"".getBytes());
            path.toFile().createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }

    /**
     * Retrieve the catalog from the server and store it in remotecatalog.txt
     * @throws IOException in case something goes wrong. 
     */
    void fetchRemoteCatalog() throws IOException {
        Path path = Path.of(root.getAbsolutePath(), "remotecatalog.txt");
        String[] entries;
        URI u = URI.create("https://download2.gluonhq.com/games/catalog.txt");
        try ( InputStream in = u.toURL().openStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }



    List<GameHolder> getInstalledGames() throws IOException {
        Path path = Path.of(root.getAbsolutePath(), "localcatalog.txt");
        List<String> entries = Files.readAllLines(path);
        List<GameHolder> answer = new LinkedList<>();
        for (String entry : entries) {
            answer.add(GameHolder.parseLocal(entry));
        }
        return answer;
    }

    List<GameHolder> getLocalAvailableGames() throws IOException {
        Path path = Path.of(root.getAbsolutePath(), "localcatalog.txt");
        List<String> entries = Files.readAllLines(path);
        List<GameHolder> answer = new LinkedList<>();
        for (String entry : entries) {
            answer.add(GameHolder.parseLocal(entry));
        }
        return answer;
    }

    List<GameHolder> getRemoteAvailableGames() throws IOException {
        fetchRemoteCatalog();
        List<GameHolder> answer = new LinkedList<>();
        Path path = Path.of(root.getAbsolutePath(), "remotecatalog.txt");
        List<String> strings = Files.readAllLines(path);
        for (String entry : strings) {
            GameHolder holder = GameHolder.parse(entry);
            answer.add(holder);
        }
        return answer;
    }

    public List<GameHolder> getRemoteOnlyGames() throws IOException {
        List<GameHolder> candidates = getRemoteAvailableGames();
        Map<String, GameHolder> map = new HashMap<>();
        for (GameHolder g: candidates) map.put(g.getName(), g);
        List<GameHolder> local = getLocalAvailableGames();
        for (GameHolder exists: local) {
            if (map.containsKey(exists.getName())) {
                map.remove(exists.getName());
            }
        }
        System.err.println("remoteonly = "+map);
        return new LinkedList(map.values());
    }

    void fetchGame(GameHolder gh) throws IOException {
        String dest = gh.getName();
        URL url = gh.getRemoteUrl();
        Path path = Path.of(root.getAbsolutePath(), "games", dest+".jar");
        try {
            InputStream in = url.openStream();
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            in.close();
            String entry = dest+"!1!"+url.toString()+"\n";
            Path lc = Path.of(root.getAbsolutePath(), "localcatalog.txt");
            Files.write(lc, entry.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

    }

    List<GameApplication> getRegisteredGames() {
        List<GameApplication> games = new LinkedList<>();
        File gameDir = Path.of(root.getAbsolutePath(), "/games").toFile();
        gameDir.mkdirs();
        File[] gameFiles = gameDir.listFiles(f -> f.getAbsolutePath().endsWith(".jar"));
        System.err.println("We have "+ gameFiles.length+" jars");
        URL[] urls = new URL[gameFiles.length];
        for (int i = 0; i < gameFiles.length; i++) {
            try {
                urls[i] = gameFiles[i].toURI().toURL();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        URLClassLoader ucl = new URLClassLoader(urls);
        ServiceLoader<GameApplication> loader = ServiceLoader.load(GameApplication.class, ucl);
        Stream<ServiceLoader.Provider<GameApplication>> stream = loader.stream();
        stream.forEach(s -> {
            System.err.println("s = "+s);
        });
        Iterator<GameApplication> it = loader.iterator();
        while (it.hasNext()) {
            System.err.println("We have a game");
            GameApplication game = it.next();
            games.add(game);
        }
        return games;
    }
}
