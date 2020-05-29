package com.pwc.model.components.bottom_kick;

import javax.annotation.PostConstruct;
import javax.jcr.Property;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.pwc.wcm.utils.I18nPwC;

@Model(adaptables = { SlingHttpServletRequest.class })
public class BottomKickModel {

	private String title;
	private String scrollPosition;
	private String hideText;
	private boolean enableOnlinePar;
	private boolean enableContactsPar;
	private boolean enableListPar;
	private String alignment;
	private Boolean enableContactsTwoThird;
	private boolean disableInheritance;
	private String disableInheritanceText;
	private boolean cancelInheritance;
	private String cancelInheritanceText;

	@Self
	private SlingHttpServletRequest request;

	private Resource resource;

	@PostConstruct
	protected void init() {
		resource = request.getResource();
		HierarchyNodeInheritanceValueMap hierarchyNodeInheritanceValueMap = new com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap(resource);
		I18nPwC i18nPwC = new I18nPwC(request, resource);
		hideText = i18nPwC.getPwC("Bottom_Kick_Hide_Text");
		title = hierarchyNodeInheritanceValueMap.getInherited("title", "Get in touch");
		scrollPosition = hierarchyNodeInheritanceValueMap.getInherited("scrollPosition", StringUtils.EMPTY);
		cancelInheritance = hierarchyNodeInheritanceValueMap.getInherited("cancelInheritance", false);
		disableInheritance= hierarchyNodeInheritanceValueMap.get("disableInheritance", false);
		boolean getDialogProperty = cancelInheritance || disableInheritance;
		enableOnlinePar = getDialogProperty ? hierarchyNodeInheritanceValueMap.get("enableOnlinePar",false) : hierarchyNodeInheritanceValueMap.getInherited("enableOnlinePar", false);
		enableContactsPar = getDialogProperty ? hierarchyNodeInheritanceValueMap.get("enableContactsPar",false) : hierarchyNodeInheritanceValueMap.getInherited("enableContactsPar", false);
		enableListPar = getDialogProperty ? hierarchyNodeInheritanceValueMap.get("enableListPar",false) : hierarchyNodeInheritanceValueMap.getInherited("enableListPar", false);
		alignment = hierarchyNodeInheritanceValueMap.getInherited("alignment", StringUtils.EMPTY);
		enableContactsTwoThird = getDialogProperty ? hierarchyNodeInheritanceValueMap.get("contactsTwoThird",false) : hierarchyNodeInheritanceValueMap.getInherited("contactsTwoThird",false);
		disableInheritanceText = i18nPwC.getPwC("Bottom_Kick_Disable_Inheritance");
		cancelInheritanceText = i18nPwC.getPwC("Bottom_Kick_Cancel_Inheritance");
	}

	public String getTitle() {
		return title;
	}

	public String getScrollPosition() {
		return scrollPosition;
	}

	public String getHideText() {
		return hideText;
	}

	public boolean isEnableOnlinePar() {
		return enableOnlinePar;
	}

	public boolean isEnableContactsPar() {
		return enableContactsPar;
	}

	public boolean isEnableListPar() {
		return enableListPar;
	}

	public String getAlignment() {
		return alignment;
	}

	public Boolean getEnableContactsTwoThird() {
		return enableContactsTwoThird;
	}

	public boolean isCancelInheritance() {
		return cancelInheritance;
	}

	public boolean isDisableInheritance() {
		return disableInheritance;
	}

	public String getDisableInheritanceText() { return disableInheritanceText; }

	public String getCancelInheritanceText() { return cancelInheritanceText; }
}
