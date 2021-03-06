package com.angelde.jsonblog;

import com.esotericsoftware.jsonbeans.JsonValue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BlogPost implements Comparable<BlogPost> {

    public Blog blog;

    public String content;
    public String content_less;

    public JsonValue values;
    public HashMap<String, String> bases = new HashMap<String, String>();

    public BlogPost() {
    }

    @Override
    public int compareTo(BlogPost post) {
        return Utils.get(post.values, "post.id").asInt() - Utils.get(values, "post.id").asInt(); //if sorting newest-first
    }

    public String buildHTML() {
        //get the full page post base
        String html = bases.get("post");
        if (html == null) {
            html = blog.bases.get("post");
        }

        //replace other existing sections with their bases
        for (Map.Entry<String, String> entry : bases.entrySet()) {
            String name = entry.getKey();
            if (name.equals("main")) {
                continue;
            }
            String base = entry.getValue();
            html = html.replace("<!-- jsonblog." + name + " -->", base);
        }
        for (Map.Entry<String, String> entry : blog.basesPost.entrySet()) {
            String name = entry.getKey();
            if (name.equals("main")) {
                continue;
            }
            String base = entry.getValue();
            html = html.replace("<!-- jsonblog." + name + " -->", base);
        }
        for (Map.Entry<String, String> entry : blog.bases.entrySet()) {
            String name = entry.getKey();
            if (name.equals("main") || name.equals("post.more")) {
                continue;
            }
            String base = entry.getValue();
            html = html.replace("<!-- jsonblog." + name + " -->", base);
        }

        //get the HTML content from the markdown
        String htmlContent;
        try {
            htmlContent = Blog.markdownProcessor.process(content).trim();
        } catch (IOException e) {
            e.printStackTrace();
            htmlContent = "";
        }
        html = html.replace("<!-- jsonblog.post.content -->", htmlContent);

        //replace the description
        JsonValue description = Utils.get(values, "post.description");
        if (description != null) {
            html = html.replace("<!-- jsonblog.post.description -->", description.asString());
        } else if (content_less != null) {
            html = html.replace("<!-- jsonblog.post.description -->",
                    content_less.substring(0, Math.min(content_less.length(), 120)));
        } else {
            html = html.replace("<!-- jsonblog.post.description -->",
                    content.substring(0, Math.min(content.length(), 120)));
        }

        //replace other values
        html = Utils.replace(values, html);
        html = Utils.replace(blog.valuesPost, html);
        html = Utils.replace(blog.values, html);

        return html;
    }

    public String buildHeadlessHTML() {
        //get the headless post base
        String html = bases.get("post.headless");
        if (html == null) {
            html = blog.bases.get("post.headless");
        }

        //add the more link if needed and possible
        if (content_less != null) {
            String basePostMore = bases.get("post.more");
            if (basePostMore == null) {
                basePostMore = blog.bases.get("post.more");
            }
            if (basePostMore != null) {
                html = html.replace("<!-- jsonblog.post.more -->", basePostMore);
            }
        }

        //replace other existing sections with their headless specific bases
        for (Map.Entry<String, String> entry : bases.entrySet()) {
            String name = entry.getKey();
            if (name.equals("main")) {
                continue;
            }
            if (!name.startsWith("post.headless.")) {
                continue;
            }
            name = name.substring(14);
            String base = entry.getValue();
            html = html.replace("<!-- jsonblog." + name + " -->", base);
        }

        for (Map.Entry<String, String> entry : blog.basesPost.entrySet()) {
            String name = entry.getKey();
            if (name.equals("main")) {
                continue;
            }
            if (!name.startsWith("post.headless.")) {
                continue;
            }
            name = name.substring(14);
            String base = entry.getValue();
            html = html.replace("<!-- jsonblog." + name + " -->", base);
        }

        for (Map.Entry<String, String> entry : blog.bases.entrySet()) {
            String name = entry.getKey();
            if (name.equals("main")) {
                continue;
            }
            if (!name.startsWith("post.headless.")) {
                continue;
            }
            name = name.substring(14);
            String base = entry.getValue();
            html = html.replace("<!-- jsonblog." + name + " -->", base);
        }

        //process the markdown into HTML
        String htmlContent;
        try {
            if (content_less == null) {
                htmlContent = Blog.markdownProcessor.process(content).trim();
            } else {
                htmlContent = Blog.markdownProcessor.process(content_less).trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
            htmlContent = "";
        }
        html = html.replace("<!-- jsonblog.post.content -->", htmlContent);

        html = Utils.replace(values, html);

        return html;
    }
}
