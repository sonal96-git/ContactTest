package com.pwc.model;

public class DropDownEntity {
	private String text;
	private String label;
	private String value;
	
	public DropDownEntity(String text, String label, String value){
		this.text = text;
		this.label = label;
		this.value = value;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
