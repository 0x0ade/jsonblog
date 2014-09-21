package com.angelde.jsonblog;

import com.esotericsoftware.jsonbeans.JsonValue;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Utils {

    private Utils() {
    }

    public final static String name = "JsonBlog";
    public final static String version = "0.1";
    public final static int versionNumber = 0;

    public static boolean log = false;

    public static void main(String[] args) {
        System.out.println(name+" "+version+" ("+versionNumber+")");
        log = true;

        String config;
        if (args.length == 1) {
            config = args[0];
        } else {
            System.out.println("Defaulting to config.json ...");
            config = "./config.json";
        }

        buildAuto(config);
    }

    public static Blog buildAuto(String config) {
        Blog blog = new Blog();

        blog.loadConfigFrom(config);
        blog.loadEntries();

        JsonValue output = blog.values.get("output");
        if (output != null) {
            saveString(blog.buildHTML(), output.asString());
        }

        for (BlogPost post : blog.posts) {
            JsonValue outputPost = post.values.get("output.post");
            if (outputPost != null) {
                saveString(post.buildHTML(), outputPost.asString());
            } else {
                outputPost = blog.values.get("output.posts");
                if (outputPost != null) {
                    saveString(post.buildHTML(), outputPost.asString() + "/" + post.values.getInt("post.id") + ".html");
                }
            }
        }
        return blog;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                //silently
            }
        }
    }

    public static String platformPath(String path) {
        return path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
    }

    public static String htmlEscape(String html) {
        String htmlEscaped = "";
        for (int i = 0; i < html.length(); i++) {
            char c = html.charAt(i);
            if (c == '\n') {
                htmlEscaped += "<br>";
            } else if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                htmlEscaped += "&#" + ((int) c) + ';';
            } else {
                htmlEscaped += c;
            }
        }
        return htmlEscaped;
    }

    public static void saveString(String string, String path) {
        path = platformPath(path);

        File file = new File(path);
        file.getParentFile().mkdirs();

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(string.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeSilently(bos);
            closeSilently(fos);
        }
    }

    public static String readAsString(String path) {
        path = platformPath(path);

        byte[] bbuf;

        try {
            bbuf = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        try {
            return new String(bbuf, "UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
