package com.pwc.wcm.taglibs;

import com.day.cq.wcm.api.Page;
import com.pwc.wcm.model.List;

@SuppressWarnings("serial")
public class ListTagLib extends BaseTagLib {

	@Override
	protected int startTag() {
    	List<Page> list = new List<Page>(request, Page.class);
    	setPageAttribute("list", list);
		return EVAL_PAGE;
	}

}
