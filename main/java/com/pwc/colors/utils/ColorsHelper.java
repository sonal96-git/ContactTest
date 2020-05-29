package com.pwc.colors.utils;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONException;
import org.json.JSONObject;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.pwc.BrandSimplificationConfigService;
import com.pwc.colors.models.Color;
import com.pwc.colors.models.ColorsBean;
import com.pwc.util.ExceptionLogger;


public class ColorsHelper extends WCMUsePojo {

    private ColorsBean colorsBean;

    private final String SPLIT_CHAR = "-|\\s";
    private final String COLOR_FAMILY = "colorFamily";
    private final String COLORS_ENABLED = "colorsEnabled";
    private final String STANDARD_SPACING = "standardSpacing";
    private final String COLOR_FAMILY_COMBINATION = "colorFamilyCombination";
    private final String COLOR_INHERITED = "Inherited";

    private final String PROPERTY    = "data";
    private final String COLOR_NAME  = "name";
    private final String COLOR_VALUE = "value";
    private final String HEX_COLOR   = "hexColor";
    private final String BASE_COLOR  = "baseColor";

    private final String COLORS         = "/colors/";
    private final String CSS_CLASSES    = "/colorsutils/cssclasses";
    private final String DEFAULT_FAMILY = "/colorsutils/defaultfamily";
    private final String COLORS_DATA    = "/etc/designs/pwc/fallbackcomponentstyles/longform-base/cq:styles";

    BrandSimplificationConfigService brandSimplificationConfigService;


    @Override
    public void activate() throws Exception {
        colorsBean = new ColorsBean(getCurrentPage(),getRequest());
        brandSimplificationConfigService = getSlingScriptHelper()
                .getService(BrandSimplificationConfigService.class);
    }

    public boolean getIsColorEnabled() {

        ValueMap pageProp = colorsBean.getCurrentPage().getProperties();
        boolean enable = pageProp.get(COLORS_ENABLED) != null ? pageProp.get(COLORS_ENABLED).toString().equals("true"):false;
        return enable;
    }

    public boolean getIsPaddingEnabled() {

        ValueMap pageProp = colorsBean.getCurrentPage().getProperties();
        boolean enable = pageProp.get(STANDARD_SPACING) != null ? pageProp.get(STANDARD_SPACING).toString().equals("true"):false;
        return enable;
    }

    public Color getPrimaryColor() {
        return getFamilyColor(0);
    }
    public Color getSecondaryColor() {
        return getFamilyColor(1);
    }
    public Color getTertiaryColor() {
        return getFamilyColor(2);
    }
    public Color getColor(String color) {

        String hexColor = "";
        String colorName = "";
        String colorValue = "";
        Boolean baseColor = true;

        try {

            Resource res = colorsBean.getRequest().getResourceResolver().getResource(COLORS_DATA+COLORS+color);

            if(res == null ) return null;

            String data = res.getValueMap().get(PROPERTY).toString();
            JSONObject jsonObj = new JSONObject(data);

            hexColor   = jsonObj.get(HEX_COLOR).toString();
            colorName  = jsonObj.get(COLOR_NAME).toString();
            baseColor  = Boolean.valueOf(jsonObj.get(BASE_COLOR).toString());
            colorValue = jsonObj.get(COLOR_VALUE).toString();

        } catch (JSONException e) {
            ExceptionLogger.logException(e);
        }

        Color colorObject = new Color(colorName,colorValue,hexColor,baseColor);
        return colorObject;
    }

    public String getColorClasses() {
    		//Brand Simplification switch
    		String BSFamily = getBrandSimplificationEnabled() ? COLOR_FAMILY : COLOR_FAMILY_COMBINATION;

        Resource resColors = colorsBean.getRequest().getResourceResolver().getResource(COLORS_DATA+DEFAULT_FAMILY);
        String defaultValue = resColors.getValueMap().get(PROPERTY).toString();

        HierarchyNodeInheritanceValueMap iPageProp = new HierarchyNodeInheritanceValueMap(colorsBean.getCurrentPage().getContentResource());
        String colorFamilyComb = iPageProp.getInherited(BSFamily, defaultValue);
        // Default family color(red-burgundy-maroon) will be selected as familyComb, if value of colorFamilyComb is "Inherited".
        String familyComb = colorFamilyComb.equals(COLOR_INHERITED) ? defaultValue : colorFamilyComb;
        String[] familyArray = familyComb.split(SPLIT_CHAR);

        Resource resClasses = colorsBean.getRequest().getResourceResolver().getResource(COLORS_DATA+CSS_CLASSES);

        String[] data = (String[])resClasses.getValueMap().get(PROPERTY);

        StringBuilder cssText = new StringBuilder();
        for(int i =0; i< data.length;i++){
            cssText.append(data[i]+familyArray[getBrandSimplificationEnabled() ? 0 : i].toLowerCase());
            if(i+1 < data.length) cssText.append(" ");
        }

        return cssText.toString();

    }

    private Color getFamilyColor(int colorType){

        String familyComb;

        String colorName = "";

        Resource res = colorsBean.getRequest().getResourceResolver().getResource(COLORS_DATA+DEFAULT_FAMILY);
        String defaultValue = res.getValueMap().get(PROPERTY).toString();

        ValueMap pageProp = colorsBean.getCurrentPage().getProperties();
        HierarchyNodeInheritanceValueMap iPageProp = new HierarchyNodeInheritanceValueMap(colorsBean.getCurrentPage().getContentResource());

        String family =  pageProp.get(COLOR_FAMILY)!= null ? pageProp.get(COLOR_FAMILY).toString() : null ;

        /* if "Inherited" option was selected, "colorFamily" came as null.
         if so, we get the inherited properties, otherwise we get the current page properties */
        familyComb = family == null ?
                iPageProp.getInherited(COLOR_FAMILY_COMBINATION,defaultValue) :
                (pageProp.get(COLOR_FAMILY_COMBINATION) != null ?pageProp.get(COLOR_FAMILY_COMBINATION).toString():defaultValue);

        String[] familyArray = familyComb.split(SPLIT_CHAR);

        colorName = familyArray[colorType].toLowerCase();

        return getColor(colorName.toLowerCase());
    }

    public ColorsBean getColorsBean() {
        return colorsBean;
    }

    public void setColorsBean(ColorsBean controllerBean) {
        colorsBean = controllerBean;
    }

    public boolean getBrandSimplificationEnabled() {
        return brandSimplificationConfigService.isBrandSimplificationEnabled(getRequest());
    }
}
