package com.pwc.wcm.utils;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import com.day.cq.commons.ListInfoProvider;
import com.day.cq.wcm.api.Page;
import com.pwc.workflow.WorkFlowConstants;

/**
 * Extending Siteadmin to add a column "Preview Status" for each page row loaded in siteadmin.
 *
 * @author jayati
 */
@Component(immediate = true, service = ListInfoProvider.class)
public class PagePreviewStatusSiteadmin implements ListInfoProvider {

    @Override
	public void updateListGlobalInfo(SlingHttpServletRequest request, JSONObject info, Resource resource) throws JSONException {
    }

    /**
     * Adds "previewstatus" key to "info" for siteadmin by picking up value from jcr:content node of the page.
     * @param request {@link SlingHttpServletRequest} carries request object for current load of siteadmin
     * @param info {@link JSONObject} pages.json info object for each page
     * @param resource {@link Resource} resource usually Page or Asset whose info needs to be populated
     */
    @Override
	public void updateListItemInfo(SlingHttpServletRequest request, JSONObject info, Resource resource) throws JSONException {
        Page pwc_page = resource.adaptTo(Page.class);
        String status_text = "";
            if(pwc_page!=null){
                status_text = pwc_page.getProperties().get(WorkFlowConstants.PREVIEW_STATUS_LABEL)!=null?pwc_page.getProperties().get(WorkFlowConstants.PREVIEW_STATUS_LABEL).toString():"";
            }
        info.put(WorkFlowConstants.PREVIEW_STATUS_LABEL,status_text );
        }
}
