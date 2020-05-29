package com.pwc.wcm.model;

import java.util.Iterator;
import java.util.List;

import com.day.cq.wcm.api.Page;

public class PageTree {

	private Page currentPage;
	private List<PageTree> childrenTree;
	private List<Page> childrenIter;

	public Page getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(Page currentPage) {
		this.currentPage = currentPage;
	}

	public List<Page> getChildrenIter() {
		return childrenIter;
	}
	public void setChildrenIter(List<Page> childrenIter) {
		this.childrenIter = childrenIter;
	}
	public List<PageTree> getChildrenTree() {
		return childrenTree;
	}
	public void setChildrenTree(List<PageTree> childrenTree) {
		this.childrenTree = childrenTree;
	}
}
