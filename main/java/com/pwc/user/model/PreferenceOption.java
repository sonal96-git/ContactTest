package com.pwc.user.model;

import java.util.List;

import com.google.gson.Gson;

/**
 * User Preferences option POGO that provides getters and setters for the properties of the preference page
 */
public class PreferenceOption {
    
    private String name;
    private String title;
    private String i18nTitle;
    private boolean hasChildren;
    private List<PreferenceOption> childrenOption;
    private String path;
    private String category;
    private String secondTierDesign;
    
    /**
     * Instantiates a new preference option.
     */
    public PreferenceOption() {
    }
    
    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the preference option name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Gets the i18n translated title.
     *
     * @return the i18n translated title
     */
    public String getI18nTitle() {
        return i18nTitle;
    }
    
    /**
     * Sets the i18n translated title.
     *
     * @param i18nTitle i18n translated title
     */
    public void setI18nTitle(String i18nTitle) {
        this.i18nTitle = i18nTitle;
    }
    
    /**
     * Checks if the preference option children preference options.
     *
     * @return true, if the preference option has children preference options
     */
    public boolean isHasChildren() {
        return hasChildren;
    }
    
    /**
     * Sets if the preference option children preference options.
     *
     * @param true, if the preference option has children preference options
     */
    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }
    
    /**
     * Gets the preference children options list.
     *
     * @return the children options list
     */
    public List<PreferenceOption> getChildrenOption() {
        return childrenOption;
    }
    
    /**
     * Sets the preference children options list.
     *
     * @param childrenOption the children options list
     */
    public void setChildrenOption(List<PreferenceOption> childrenOption) {
        this.childrenOption = childrenOption;
    }
    
    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Sets the path.
     *
     * @param path the new path
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
    
    /**
     * Gets the second tier design.
     *
     * @return the second tier design
     */
    public String getSecondTierDesign() {
        return secondTierDesign;
    }
    
    /**
     * Sets the second tier design.
     *
     * @param secondTierDesign the new second tier design
     */
    public void setSecondTierDesign(String secondTierDesign) {
        this.secondTierDesign = secondTierDesign;
    }
    
    /**
     * Gets the category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Sets the category.
     *
     * @param category the new category
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
}
