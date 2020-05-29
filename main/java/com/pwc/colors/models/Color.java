package com.pwc.colors.models;

public class Color {

    private String name;
    private String value;
    private String hexColor;
    private Boolean baseColor;

    public Color(){}
    public Color(String name, String value ,String hexColor,Boolean baseColor){
        this.name = name;
        this.value = value;
        this.hexColor = hexColor;
        this.baseColor =baseColor;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getHexColor() {
        return hexColor;
    }
    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public Boolean getBaseColor() {
        return baseColor;
    }
    public void setBaseColor(Boolean baseColor) {
        this.baseColor = baseColor;
    }
}
