#JsonBlog
##Reads .json configuration files and .md posts; creates .html / .xml / ... files from any given base files.
######This project was created as highly customizable way to create blogs on GitHub Pages. It is advised to take a look at other solutions anyway.

JsonBlog is just another small project I wrote to learn how to create static websites in the most unperformant way.
It uses JsonBeans by EsotericSoftware to process the JSON configuration files and Markdown4J to process the Markdown post files.

Usage:
        java -jar jsonblog.jar
Or:
        java -jar jsonblog.jar config.json

Example jsonblog setup: [AngelDE98's Website on GitHub](https://github.com/AngelDE98/AngelDE98.github.io)
Example result: [AngelDE98's Website on GitHub Pages](http://angelde98.github.io)

Explaination:
All paths in the configuration .json files are relative to the directory the above command was launched in.
The config.json contains most of the main configuration of the blog, input files and output files.
A config.post.json (or in case of abc.json, abc.post.json) may exist to define the configuration of a full-page blog post.
The input posts (entries) must end with the .md file extension and be in the same directory. They also must have an .json configuration file with the same filename (f.e. abc.md and abc.json).
The post specific configuration file may define the blog post, title, other variables.
The post specific configuration file must define the post ID as the output file is stored at the post output directory > id.html by default.
Alternatively, the post specific configuration files can have post specific output paths.

All HTML / XML comments in the following format:
                <!-- jsonblog.value -->
are replaced by their respective values. For example, if the following line exists in the highest-priority config.json ...
                "post.title": "Hello, World!"
..., all occurences of ...
                <!-- jsonblog.post.title -->
... in the base file, no matter where, will be replaced with the value.

It is advised to look at the example jsonblog setup and it's output (in the same repository) listed above.
