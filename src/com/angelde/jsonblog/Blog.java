package com.angelde.jsonblog;

import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.JsonReader;
import com.esotericsoftware.jsonbeans.JsonValue;
import org.markdown4j.Markdown4jProcessor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Blog {

    public static JsonReader jsonReader = new JsonReader();
    public static Markdown4jProcessor markdownProcessor = new Markdown4jProcessor();

    public JsonValue values;
    public HashMap<String, String> bases = new HashMap<String, String>();

    public JsonValue valuesPost;
    public HashMap<String, String> basesPost = new HashMap<String, String>();

    public ArrayList<BlogPost> posts = new ArrayList<BlogPost>();

    public Blog() {
    }

    public void loadConfigFrom(String path) {
        path = Utils.platformPath(path);

        FileReader fr = null;
        try {
            //read the main config file
            fr = new FileReader(path);
            values = jsonReader.parse(fr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //create stub configuration anyway
            values = new JsonValue(JsonValue.ValueType.object);
        } finally {
            Utils.closeSilently(fr);
        }

        fr = null;
        try {
            //read the extended post config file
            fr = new FileReader(path.substring(0, path.length()-4)+"post.json");
            valuesPost = jsonReader.parse(fr);
        } catch (FileNotFoundException e) {
            //don't print the error as a missing config.post.json is possible
            //create stub configuration anyway
            valuesPost = new JsonValue(JsonValue.ValueType.object);
        } finally {
            Utils.closeSilently(fr);
        }

        //read main imports
        JsonValue valuesImports = values.get("imports");
        if (valuesImports != null) {
            for (JsonValue value = valuesImports.child(); value != null; value = value.next()) {
                if (!value.isString()) {
                    continue;
                }
                String importPath = value.asString();
                FileReader ifr = null;
                try {
                    ifr = new FileReader(importPath);
                    JsonValue valuesImported = jsonReader.parse(ifr);
                    //adding the first child should be sufficient as the other children are the first children's next children.
                    values.addChild(valuesImported.child());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    Utils.closeSilently(ifr);
                }
            }
        }
        values.remove("imports");

        //read main bases
        JsonValue valuesBases = values.get("bases");
        if (valuesBases != null) {
            for (JsonValue value = valuesBases.child(); value != null; value = value.next()) {
                String name = value.name();
                String base = value.asString();
                if (base.length() > 0) {
                    bases.put(name, Utils.readAsString(value.asString()));
                }
            }
        }

        //read extended post imports
        JsonValue valuesImportsPost = valuesPost.get("imports");
        if (valuesImportsPost != null) {
            for (JsonValue value = valuesImportsPost.child(); value != null; value = value.next()) {
                if (!value.isString()) {
                    continue;
                }
                String importPath = value.asString();
                FileReader ifr = null;
                try {
                    ifr = new FileReader(importPath);
                    JsonValue valuesImported = jsonReader.parse(ifr);
                    valuesPost.addChild(valuesImported.child());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    Utils.closeSilently(ifr);
                }
            }
        }
        valuesPost.remove("imports");

        //read extended post bases
        JsonValue valuesBasesPost = valuesPost.get("bases");
        if (valuesBasesPost != null) {
            for (JsonValue value = valuesBasesPost.child(); value != null; value = value.next()) {
                String name = value.name();
                String base = value.asString();
                if (base.length() > 0) {
                    basesPost.put(name, Utils.readAsString(value.asString()));
                }
            }
        }
    }

    public void loadEntries() {
        loadEntriesFrom(values.getString("entries"));
    }

    public void loadEntriesFrom(String path) {
        path = Utils.platformPath(path);
        File dir = new File(path);

        if (!dir.isDirectory()) {
            //simply don't load any entries
            return;
        }

        for (File file : dir.listFiles()) {
            //ignore non-markdown files
            if (!file.getName().toLowerCase().endsWith(".md")) {
                continue;
            }

            BlogPost post = new BlogPost();
            post.blog = this;

            FileReader fr = null;
            try {
                //read the post-specific config file
                fr = new FileReader(file.getPath().substring(0, file.getPath().length()-2)+"json");
                post.values = jsonReader.parse(fr);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                post.values = new JsonValue(JsonValue.ValueType.object);
            } finally {
                Utils.closeSilently(fr);
            }

            //read the post-specific imports
            JsonValue valuesImports = post.values.get("imports");
            if (valuesImports != null) {
                for (JsonValue value = valuesImports.child(); value != null; value = value.next()) {
                    if (!value.isString()) {
                        continue;
                    }
                    String importPath = value.asString();
                    FileReader ifr = null;
                    try {
                        ifr = new FileReader(importPath);
                        JsonValue valuesImported = jsonReader.parse(ifr);
                        post.values.addChild(valuesImported.child());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        Utils.closeSilently(ifr);
                    }
                }
            }
            post.values.remove("imports");

            //read the post-specific bases
            JsonValue valuesBases = post.values.get("bases");
            if (valuesBases != null) {
                for (JsonValue value = valuesBases.child(); value != null; value = value.next()) {
                    String name = value.name();
                    String base = value.asString();
                    if (base.length() > 0) {
                        post.bases.put(name, Utils.readAsString(value.asString()));
                    }
                }
            }

            //read the content and split if a [MORE]-tag exists
            String content = Utils.readAsString(file.getPath());

            int indexofMore = content.indexOf("[MORE]");

            if (indexofMore < 0) {
                post.content = content.substring(0).trim();
            } else {
                post.content_less = content.substring(0, indexofMore).trim();
                post.content = post.content_less + "\n" + content.substring(indexofMore+6).trim();
            }

            posts.add(post);
        }
    }

    public String buildHTML() {
        String html = bases.get("main");
        String htmlPosts = "";

        //replace the bases
        for (Map.Entry<String, String> entry : bases.entrySet()) {
            String name = entry.getKey();
            if (name.equals("main")) {
                continue;
            }
            String base = entry.getValue();
            html = html.replace("<!-- jsonblog." + name + " -->", base);
        }

        //create post content
        Collections.sort(posts);
        for (BlogPost post : posts) {
            htmlPosts += post.buildHeadlessHTML();
        }

        //replace the posts section with the posts
        html = html.replace("<!-- jsonblog.posts -->", htmlPosts);

        //replace any further unset values with the main ones
        html = Utils.replace(values, html);

        return html;
    }

}
