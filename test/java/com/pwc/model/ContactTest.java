package com.pwc.model;


import com.pwc.wcm.services.CountryTerritoryMapperService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith({ AemContextExtension.class})
@RunWith(MockitoJUnitRunner.class)
public class ContactTest {

    private final AemContext ctx = new AemContext();

    public Contact contact;

    @Mock
    private CountryTerritoryMapperService countryTerritoryMapperService;



    @BeforeEach
    public void setUp() throws Exception {
        ctx.addModelsForClasses(Contact.class);
        ctx.load().json("/com/pwc/model/ContactTest.json", "/content");
       // countryTerritoryMapperService=new CountryTerritoryMapperServiceImpl();
        ctx.registerService(CountryTerritoryMapperService.class,countryTerritoryMapperService);
        Map<String,Territory> testMap = new HashMap<>();
        Territory territory = new Territory();
        territory.setTerritoryName("Estonia");
        territory.setTerritoryI18nKey("Territory_Name_EE");
        testMap.put("ee",territory);
        Mockito.when(countryTerritoryMapperService.getTerritoryCodeToTerritoryMap()).thenReturn(testMap);
    }

    @Test
    public void testGetImageReference()
    {
        final String expected="/content/dam/core-components-examples/library/sample-assets/lava-rock-formation.jpg";

        Resource resource=ctx.resourceResolver().getResource("/content/contact");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getImageReference();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetProfileName()
    {
        final String expected="image.jpg";

        Resource resource=ctx.resourceResolver().getResource("/content/contact");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getProfileName();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetAlternativeLinkText()
    {
        final String expected="some text";

        Resource resource=ctx.resourceResolver().getResource("/content/contact");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getAlternativeLinkText();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetAlternativeLink()
    {
        final String expected="/content/pwc/ee/en/home.html";

        Resource resource=ctx.resourceResolver().getResource("/content/contact");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getAlternativeLink();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetContactBox_ProfileType()
    {
        final String expected="contactBox";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getProfileType();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetGlobalTitle()
    {
        final String expected="E.164";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

         contact= resource.adaptTo(Contact.class);


        String actual = contact.getGlobalTitle();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetFirstName()
    {
        final String expected="Sonal";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getFirstName();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetLastName()
    {
        final String expected="Gupta";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getLastName();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetLocalTitle()
    {
        final String expected="E.124";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getLocaltitle();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetLinkedInUrl()
    {
        final String expected="https://www.linkedin.com/in/sonal-gupta-b92746147/";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getLinkedInUrl();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetEmail()
    {
        final String expected="janedoe12@gmail.com";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getEmail();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetCountry()
    {
        final String expected="EE";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getCountry();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetOffice()
    {
        final String expected="ttn";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getOffice();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetProvince()
    {
        final String expected="UP";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getProvince();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetTelephoneNumber()
    {
        final String expected="9450816811";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getTelephone();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetTerritory()
    {
        final String expected="ee";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getTerritory();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetFaxNumber()
    {
        final String expected="212-555-1234";

        Resource resource=ctx.resourceResolver().getResource("/content/contactBox");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getFax();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetContactBoxCard_ProfileType()
    {
        final String expected="contactBoxCard";

        Resource resource=ctx.resourceResolver().getResource("/content/contactCard");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getProfileType();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetTwitterUrl()
    {
        final String expected="https://www.twitter.com/in/jane-doe/";

        Resource resource=ctx.resourceResolver().getResource("/content/contactCard");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getTwitter();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetDesignation()
    {
        final String expected="software developer";

        Resource resource=ctx.resourceResolver().getResource("/content/contactCard");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getDesignation();

        assertEquals(expected, actual);
    }
    @Test
    public void testGetExperience()
    {
        final String expected="2 years";

        Resource resource=ctx.resourceResolver().getResource("/content/contactCard");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getExperience();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetSpecialty()
    {
        final String expected="Good Programmer";

        Resource resource=ctx.resourceResolver().getResource("/content/contactCard");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getSpecialty();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetContactBoxCardProfile_ProfileType()
    {
        final String expected="contactBoxCardProfile";

        Resource resource=ctx.resourceResolver().getResource("/content/contactProfile");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getProfileType();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetEducation()
    {
        final String expected="MCA";

        Resource resource=ctx.resourceResolver().getResource("/content/contactProfile");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getEducation();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetAreaOfInterest()
    {
        final String expected="AEM technology";

        Resource resource=ctx.resourceResolver().getResource("/content/contactProfile");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getInterest();

        assertEquals(expected, actual);
    }


    @Test
    public void testGetBodyContent()
    {
        final String expected="I am hardworking";

        Resource resource=ctx.resourceResolver().getResource("/content/contactProfile");

        contact= resource.adaptTo(Contact.class);


        String actual = contact.getBodyContent();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetTerritoryOrLegalEntitiesI18nKey_WhenTerritoryIsNotBlank()
    {
        final String expected="Territory_Name_EE";

        Resource resource=ctx.resourceResolver().getResource("/content/TerritoryOrLegalEntityI18nKey-With-Territory");
        contact= resource.adaptTo(Contact.class);
        String actual = contact.getTerritoryOrLegalEntityI18nKey();
        assertEquals(expected, actual);
    }

}

