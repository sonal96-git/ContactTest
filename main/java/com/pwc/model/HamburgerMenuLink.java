package com.pwc.model;

import java.util.ArrayList;
import java.util.List;

public class HamburgerMenuLink {
	
	String url;
	String text;
	String targetType;
	List<HamburgerMenuLink> children;

	
	public HamburgerMenuLink(){
		
	}
	
	public HamburgerMenuLink(String url,String text,String targetType,List children) {
		this.url=url;
		this.text=text;
		this.targetType=targetType;
		this.children=children;
	}
	
	public String getUrl() {
		return url;
	}
	public String getText() {
		return text;
	}
	public String getTargetType() {
		return targetType;
	}
	public List getChildren() {
		return children;
	}
}