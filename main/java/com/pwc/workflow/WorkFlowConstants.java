package com.pwc.workflow;

public class WorkFlowConstants {

	public static final String TYPE_JCR_PATH  =  "JCR_PATH";
	public static final String DAM_ASSET  =  "dam:Asset";
	public static final String CONTENT_ELEMENT = "jcr:content";
	public static final String SRC_ELEMENT = "pwcSourceURL";
	public static final String TEMPLATE_ELEMENT = "cq:template";
	public static final String SYNDICATION_FLAG_ELEMENT = "pwcSyndicationFlag";
	public static final String SYNC_ELEMENT = "pwcStayinSync";
	
	public static final String URL_REGISTRY_PROP = "pwcAllDestinationURL";
	public static final String SYNDICATION_OPEN = "Open";
	public static final String SYNDICATION_RESTRICTED = "Restricted";
	public static final String SYNDICATION_LOCKED = "Locked";
	public static final String INVALID_INPUT = "invalid";
	public static final String USERNAME_REGISTRY = "pwcUserList";
	public static final String USER_EMAIL_REGISTRY = "pwcUserEmailList";
	public static final String SYNDICATION_DONE_MANUALLY = "manual";
	
	public static final String NUMBER_OF_REGISTERED_USERES = "registeredUserNum";
	public static final String NUMBER_OF_REGISTERED_USERES_COUNTER = "registeredUserNumCounter";
	public static final String PARTICIPANT = "pwcParticipant";
	public static final String NOTIFICATION_WORKFLOW_MODEL =  "/conf/global/settings/workflow/models/pwc-notification-workflow/jcr:content/model";
	public static final String SYNDICATION_WORKFLOW_MODEL =  "/conf/global/settings/workflow/models/pwc-syndication-workflow/jcr:content/model";
	public static final String EXPIRY_WORKFLOW_MODEL =  "/conf/global/settings/workflow/models/PwC-Expiry-Workflow/jcr:content/model";
	
	public static final String STAY_IN_SYNC_CHECK_VALUE = "on";
	public static final String EMAIL_TEMPLATE = "/etc/notification/email/pwcEmailTemplate/emailtemplate.txt";
	public static final String NOTIFICATION_GROUP = "pwc-temp-notification-group";
	
	public static final String CONTACTFORM_REFNO="referenceNumber";
	public static final String CONTACTFORM_PARAM_SUBJECT="subject";
	public static final String CONTACTFORM_VALUE_SUBJECT="Customer enquiry from pwc.com";
	public static final String CONTACTFORM_FORMNAME="PwC Contact Us Form";
	public static final String CONTACTFORM_FORM="formName";
	public static final String CONTACTFORM_CONTACTDECIDER="contact";
	public static final String CONTACTFORM_SENDEMAILTO="sendEmailTo";
	
	public final static String PROPERTY_FORMPATH = "formPath";
	public final static String PROPERTY_FORMID = "formId";
	
	public final static String FORM_STATUS = "status";
	public final static String STATUS_SUBMITTED = "Submitted";
	public final static String STATUS_OBSCENE = "Potential Obscene Language";
	public final static String STATUS_PROCESSING="Ready for Processing";
	public final static String STATUS_PROCESSED="Processed";
	public static final String STATUS_FAILED ="Delivery Failed";
	public static final String STATUS_RETURN = "return";
	public static final String PAGE_EXPIRATION_CHECK = "pwcPageExpirationCheck";
	public static final String DESCRIPTION_ELEMENT = "jcr:description";
	public static final String SYNDICATION_DESCRIPTION = "syndicated";
	public static final String LAST_PUBLISHED_VERSION = "pwcLastPublishedVersion";
	public static final String LAST_PUBLISHED_VERSION_ID = "pwcLastPublishedRevisionId";
	public static final String PARTICIPANT_AUTHORS = "pwcAuthors";
	public static final String PARTICIPANT_AUTHORS_AND_APPROVERS = "pwcAuthorsAndApprovers";
	public static final String PARTICIPANT_APPROVERS = "pwcApprovers";
	public static final String ASSETS_TO_ACTIVATE = "pwcPageAssetsToBeActivated";
	public static final String HAS_VERSION_BEEN_RESTORED = "isVersionRestored";
	
	public static final String PREVIEW_STATUS_LABEL = "previewstatus";
	public static  final String EMAIL_WHITELIST_PATH = "/content/pwc/global/referencedata/whitelistedEmails";
	public static  final String BANNED_WORDS_PATH = "/content/pwc/global/referencedata/bannedWords";
	public static  final String VISITOR_EMAIL = "user_email";
	public static  final String ACTIVATED_IN_PUBLISH = "activatedInPublish";

}
