package com.pwc.user.services.impl;



import com.pwc.user.services.SFMCURLConfigService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(immediate = true, service = { SFMCURLConfigService.class }, enabled = true,
        property = {
                Constants.SERVICE_DESCRIPTION + "= Provides methods to access the path of user preference page hosted at SFMC" })
@Designate(ocd = SFMCURLConfigServiceImpl.Config.class)
public class SFMCURLConfigServiceImpl implements SFMCURLConfigService {

    public static final String AKAMAI_PREFERENCE_PAGE_PATH_PROPERTY = "akamai.preference.path";
    public static final String AKAMAI_PREFERENCE_PAGE_PATH_VALUE = "https://sfmc.esi.pwc.com/test_subcntr?ee=";

    private String akamaiPrefrencePagePath;


    @ObjectClassDefinition(name = "PwC SFMC Preference Pages Path Configuration Service",
            description = "Provides methods to access the User Account Pages Content Path")
    @interface Config {
        @AttributeDefinition(name = "User Preference Page Path",
                description = "Provides methods to access the path of user preference page hosted at SFMC",
                type = AttributeType.STRING)
        public String akamai_preference_path() default AKAMAI_PREFERENCE_PAGE_PATH_VALUE;
    }

    @Activate
    @Modified
    protected final void activate(final SFMCURLConfigServiceImpl.Config properties) throws Exception {
        akamaiPrefrencePagePath = properties.akamai_preference_path();
    }

    @Override
    public String getAkamaiPreferencePagePath() {
        return akamaiPrefrencePagePath;
    }
}
