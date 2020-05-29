package com.pwc.impl.filters;

import com.day.cq.commons.Filter;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.pwc.ApplicationConstants;
import com.pwc.workflow.WorkFlowConstants;
import org.apache.sling.api.resource.ValueMap;

/**
 * Implements a custom page filter that checks for invalid,hidden, activated, published page, and pages with no index meta robots.
 */
public class PwCPageFilter implements Filter<Page> {
    private final boolean includeInvalid;
    private final boolean includeHidden;
    private final boolean includeDeactivated;
    private final boolean includeNotPublished;
    private final boolean includeNoIndex;
    private static final String ghostPageResourceType = "pwc/components/page/ghost";
    private static final String ACTIVATED = "Activate";

    public PwCPageFilter() {
        this(false, false, false, false, false);
    }

    /**
     * Creates a custom page filter..
     *
     * @param includeInvalid   {@link String} If true, invalid pages are included.
     * @param includeHidden    {@link String} If true, hidden pages are included.
     * @param includeDeactivated {@link String} If true, not activated pages are included.
     * @param includeNotPublished   {@link String} If true, pages for which "previewstatus" property is not "In Publish" or "activatedInPublish" property is false, are included.
     * @param includeNoIndex   {@link String} If true, pages for which "meta_robots" property does have index of "noindex" are included.
     */
    public PwCPageFilter(boolean includeInvalid, boolean includeHidden, boolean includeDeactivated, boolean includeNotPublished, boolean includeNoIndex) {
        this.includeInvalid = includeInvalid;
        this.includeHidden = includeHidden;
        this.includeDeactivated = includeDeactivated;
        this.includeNotPublished = includeNotPublished;
        this.includeNoIndex = includeNoIndex;
    }

    /**
     * Checks if the given element is included in this filter.
     *
     * @param page {@link Page} the element to check.
     * @return {@link Boolean} true if the element is included; false otherwise.
     */
    @Override
    public boolean includes(Page page) {
        ValueMap valueMap = page.getProperties();
        String pageResourceType = valueMap.get("sling:resourceType",String.class);
        return (this.includeHidden || !page.isHideInNav()) && (this.includeInvalid || page.isValid()) && page.getDeleted() == null
                && (this.includeDeactivated || ACTIVATED.equals(valueMap.get(NameConstants.PN_PAGE_LAST_REPLICATION_ACTION, "")))
                && (this.includeNotPublished || (ApplicationConstants.PUBLISH_STATUS.equals(valueMap.get(ApplicationConstants.PREVIEW_STATUS, "")) || (valueMap.get(WorkFlowConstants.ACTIVATED_IN_PUBLISH, false))))
                && (this.includeNoIndex || valueMap.get(ApplicationConstants.META_ROBOTS, "").indexOf(ApplicationConstants.NO_INDEX) < 0)
                && !pageResourceType.equals(ghostPageResourceType);
    }
}

