package com.pwc.model;

public class SelectOption {

    private String text;
    
    private String value;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SelectOption(String text, String value) {
        super();
        this.text = text;
        this.value = value;
    }

    @Override
    public String toString() {
        return "SelectOption [text=" + text + ", value=" + value + "]";
    }
    
}
