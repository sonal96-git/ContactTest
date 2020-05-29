package com.pwc.topic.redirect.model;

import java.util.Map;
/**
 * Model representing Topic resource in Topic Sites Reference Data.
 */
public class Topic {
    private String path;
    private String name;
    private Map<String, Territory> territories;
    
    public Topic() {
    }
    
    public Topic(String path, String name, Map<String, Territory> territories) {
        this.path = path;
        this.name = name;
        this.territories = territories;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Map<String, Territory> getTerritories() {
        return territories;
    }
    
    public void setTerritories(Map<String, Territory> territories) {
        this.territories = territories;
    }
    
    @Override
    public String toString() {
        return "Topic Site Path: " + path + ", Name: " + name + ", Territories: " + territories;
    }
}
