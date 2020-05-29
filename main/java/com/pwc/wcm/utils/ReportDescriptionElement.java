/*
 * Copyright 1997-2008 Day Management AG
 * Barfuesserplatz 6, 4001 Basel, Switzerland
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Day Management AG, ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Day.
 */
package com.pwc.wcm.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;


	public class ReportDescriptionElement {

		@SerializedName("id")
		private String id;

		@SerializedName("classification")
		private String classification;

		@SerializedName("top")
		private Integer top;

		@SerializedName("startingWith")
		private Integer startingWith;


		@SerializedName("selected")
		private List<String> selected;

		@SerializedName("parentID")
		private String parentID;

		@SerializedName("checkpoints")
		private List<String> checkpoints;

		@SerializedName("pattern")
		private List<List<String>> pattern;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getClassification() {
			return classification;
		}

		public void setClassification(String classification) {
			this.classification = classification;
		}

		public Integer getTop() {
			return top;
		}

		public void setTop(Integer top) {
			this.top = top;
		}

		public Integer getStartingWith() {
			return startingWith;
		}

		public void setStartingWith(Integer startingWith) {
			this.startingWith = startingWith;
		}


		public List<String> getSelected() {
			return selected;
		}

		public void setSelected(List<String> selected) {
			this.selected = selected;
		}

		public String getParentID() {
			return parentID;
		}

		public void setParentID(String parentID) {
			this.parentID = parentID;
		}

		public List<String> getCheckpoints() {
			return checkpoints;
		}

		public void setCheckpoints(List<String> checkpoints) {
			this.checkpoints = checkpoints;
		}

		public List<List<String>> getPattern() {
			return pattern;
		}

		public void setPattern(List<List<String>> pattern) {
			this.pattern = pattern;
		}

	}
	

