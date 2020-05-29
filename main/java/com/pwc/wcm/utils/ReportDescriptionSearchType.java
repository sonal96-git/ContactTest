package com.pwc.wcm.utils;

import com.google.gson.annotations.SerializedName;

public enum ReportDescriptionSearchType {

	@SerializedName("and")
	AND,

	@SerializedName("or")
	OR,

	@SerializedName("not")
	NOT;
}
