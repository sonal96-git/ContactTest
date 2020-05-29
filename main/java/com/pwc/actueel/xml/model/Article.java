package com.pwc.actueel.xml.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.pwc.actueel.xml.adapter.CategoryListAdapter;
import com.pwc.actueel.xml.adapter.DateAdapter;

/**
 * Models the 'Article' element of the RSSFeed response XML.
 */
public class Article {
    private String title;
    private String link;
    private String pubDate;
    private String description;
    private String image;

    private List<String> categories;

    public Article() {
    }

    public Article(final String title, final String link, final String pubDate, final String description,
            final List<String> categories, final String image) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.categories = categories;
        this.image = image;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @XmlElement(name = "title")
    public String getTitle() {
        return title;
    }

    public void setLink(final String link) {
        this.link = link;
    }

    @XmlElement(name = "link", type = String.class)
    public String getLink() {
        return link;
    }

    public void setPubDate(final String pubDate) {
        this.pubDate = pubDate;
    }

    @XmlElement(name = "pubdate")
    @XmlJavaTypeAdapter(DateAdapter.class)
    public String getPubDate() {
        return pubDate;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @XmlElement(name = "description")
    public String getDescription() {
        return description;
    }

    public void setImage(final String image) {
        this.image = image;
    }

    @XmlElement(name = "image")
    public String getImage() {
        return image;
    }

    public void setCategories(final List<String> categories) {
        this.categories = categories;
    }

    @XmlJavaTypeAdapter(CategoryListAdapter.class)
    @XmlElement(name = "category-names")
    public List<String> getCategories() {
        return categories;
    }

    @Override
    public String toString() {
        
        return "title: " + title + "link: " + link + "pubDate: " + pubDate + "description: " + description
                + "categories: " + categories + "image: " + image;
    }
    
}
