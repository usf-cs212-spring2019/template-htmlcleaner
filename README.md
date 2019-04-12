HTML Cleaner
=================================================

Eventually, we want to be able to crawl the web and parse web pages into plain text words. As such, we must be able to find the links embedded in the HTML code of a web page and then remove all of the HTML code from that web page.

This homework is broken into the following classes:

  - `HttpsFetcher.java`: This is a general HTTP fetcher, and is the same as the lecture code.

  - `HtmlFetcher.java`: This is a variant of HTTP fetcher specifically for HTML content. There are 3 methods here you must implement.

  - `HtmlCleaner.java`: This is a class dedicated to pulling out links from HTML and then stripping any remaining HTML tags leaving only the raw text. There are 5 methods here you must implement.
  

Requirements
-------------------------------------------------

The official name of this homework is `HtmlCleaner`. This should be the name you use for your Eclipse Java project and the name you use when running the homework test script.

See the [Homework Guides](https://usf-cs212-spring2019.github.io/guides/homework.html) for additional details on homework requirements and submission.

Background
-------------------------------------------------

We do not explicitly cover HTML in this class. However, there are many helpful resources on the web for more about this simple markup language. Some resources include:

* [Mozilla Developer Network](https://developer.mozilla.org/en-US/docs/Web/HTML)
* [W3C Markup Validation](http://validator.w3.org/)

You will need to be familiar with the anchor tag `<a>` for this assignment. This is the tag used to create links on web pages. For example:

```html
<a href="http://www.cs.usfca.edu/">USF CS</a>
```

The above code will generate the link <a href="http://www.cs.usfca.edu/">USF CS</a>, where the link text is `USF CS` and the link destination is `http://www.cs.usfca.edu/`. The link will always be placed in the `href` attribute of the `a` tag, but not all `a` tags will have this attribute. For example, this is a valid `a` tag without the `href` attribute:

```html
<a name="home" class="bookmark">Home</a>
```

And, the `href` attribute may appear in other tags. For example, this is a valid `link` tag to include a style sheet:

```html
<link rel="stylesheet" type="text/css" href="style.css">
```

The majority of URLs on webpages are relative (i.e. specified relative to the current webpage URL). You will need to convert those relative URLs into an absolute URL. For this, you may use the `java.net.URL` class. For example, consider the following:

```java
URL base = new URL("http://www.cs.usfca.edu/~sjengle/cs212/");
URL absolute = new URL(base, "../index.html");

// outputs http://www.cs.usfca.edu/~sjengle/index.html
System.out.println(absolute);
```

This works even if the provided string was already absolute. For example:

```java
URL base = new URL("http://www.cs.usfca.edu/~sjengle/cs212/");
URL absolute = new URL(base, "http://www.example.com/");

// outputs http://www.example.com/
System.out.println(absolute);
```

Because of this, you do not need to test if a link was relative or absolute. You can simply always use the above code.
