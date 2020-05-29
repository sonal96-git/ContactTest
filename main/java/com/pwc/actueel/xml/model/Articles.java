package com.pwc.actueel.xml.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Channel' element of the response XML. Wraps a List of all Articles.
 */
@XmlRootElement(name = "channel")
public class Articles implements Channel {
    
    private List<Article> articles;
    
    public Articles() {
    }

    public Articles(final List<Article> articles) {
        this.articles = articles;
    }
    
    @XmlElement(name = "article")
    public List<Article> getArticles() {
        return articles;
    }
    
    public void setArticles(final List<Article> articles) {
        this.articles = articles;
    }
}
