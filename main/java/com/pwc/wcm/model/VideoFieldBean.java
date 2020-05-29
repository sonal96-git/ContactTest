package com.pwc.wcm.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class VideoFieldBean {

	private String[] jsonSource;
	private VideoBean[] videoBean;
	
	public String[] getJsonSource() {
		return jsonSource;
	}
	
	public void setJsonSource(String[] jsonSource) {
		
		if (jsonSource == null) {
			this.videoBean = null;
			return;
		}
		
		Gson gson = new GsonBuilder().create();
		VideoBean[] videoBean = new VideoBean[jsonSource.length];
		Integer counter = 0;
		
		for (String json : jsonSource) {
	        VideoBean bean = gson.fromJson(json, VideoBean.class);
	        videoBean[counter] = bean;
	        counter++;
		}
		
        this.videoBean = videoBean;
        
		this.jsonSource = jsonSource;

	}

	public VideoBean[] getVideoBean() {
		return videoBean;
	}

	public void setVideoBean(VideoBean[] links) {
		this.videoBean = links;
	}

	public static void main(String[] args) {

		String[] bob = new String[2];
		bob[0] = "{\"title\":\"video title\",\"url\":\"http://www.youtube.com/asdf\",\"titleLink\":\"http://www.google.com\",\"linkText\":\"This is link text1\",\"thumbnail\":\"/content/pwc/pictures/gogo.jpg\",\"extendedDescription\":\"text text <b>text</b>\"}";
		bob[1] = "{\"title\":\"video title2\",\"url\":\"http://www.youtube.com/1234\",\"titleLink\":\"http://www.yahoo.com\",\"linkText\":\"This is link text2\",\"thumbnail\":\"/content/pwc/pictures/gogo.jpg\",\"extendedDescription\":\"text text <b>text</b> text\"}";
		
		VideoFieldBean vfb = new VideoFieldBean();
		
		vfb.setJsonSource(bob);
		
		VideoBean[] vb = vfb.getVideoBean();
		
		for (VideoBean videoBean : vb) {
			
			System.out.println(videoBean.getTitle());
			System.out.println(videoBean.getUrl());
			System.out.println(videoBean.getTitleLink());
			System.out.println(videoBean.getLinkText());
			System.out.println(videoBean.getThumbnail());
			System.out.println(videoBean.getExtendedDescription());
			
		}
		
	}

}
