package com.angelde.jsonblog;

import com.esotericsoftware.jsonbeans.JsonValue;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Utils {

    private Utils() {
    }

    public final static String name = "JsonBlog";
    public final static String version = "0.3";
    public final static int versionNumber = 2;

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

        JsonValue output = get(blog.values, "output.main");
        if (output != null) {
            saveString(blog.buildHTML(), output.asString());
        }

        for (BlogPost post : blog.posts) {
            JsonValue outputPost = get(post.values, "output.posts");
            if (outputPost != null) {
                saveString(post.buildHTML(), outputPost.asString());
            } else {
                outputPost = get(blog.values, "output.posts");
                if (outputPost != null) {
                    saveString(post.buildHTML(), outputPost.asString() + "/" + get(post.values, "post.id").asInt() + ".html");
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

    public static JsonValue get(JsonValue values, String name) {
        if (values == null) {
            return null;
        }

        JsonValue value = values.get(name);
        if (value != null) {
            return value;
        }

        if (!name.contains(".")) {
            return null;
        }

        String nameFirst = name.substring(0, name.indexOf("."));
        value = values.get(nameFirst);
        if (name.length() != nameFirst.length()) {
            return get(value, name.substring(nameFirst.length()+1, name.length()));
        }

        return value;
    }

    public static String replace(JsonValue values, String html) {
        return replace(values, html, "");
    }

    public static String replace(JsonValue values, String html, String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        for (JsonValue value = values.child(); value != null; value = value.next()) {
            if (value.isObject()) {
                html = replace(value, html, (prefix.isEmpty()?"":(prefix+"."))+value.name());
                continue;
            }

            html = html.replace("<!-- jsonblog."+(prefix.isEmpty()?"":(prefix+"."))+value.name()+" -->", htmlEscape(value.asString()));
        }
        return html;
    }


}
