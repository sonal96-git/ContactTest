package com.pwc.colors.servlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.pwc.colors.models.ColorsBean;
import com.pwc.colors.utils.ColorsHelper;
import com.pwc.util.ExceptionLogger;

@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= Get colors combination",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,		
		"sling.servlet.paths=" + "/bin/colors"
})
public class ColorsCombinationServlet extends SlingAllMethodsServlet  {


    private static final long serialVersionUID = 1L;
    private final String SPLIT_CHAR = "-";
    private ColorsBean colorsBean;
    private ColorsHelper colorsH;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        JSONObject finalJsonResponse = new JSONObject();

        Resource resource = request.getResource();
        PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
        Page page = pageManager.getContainingPage(resource);

        colorsH = new ColorsHelper();
        colorsBean = new ColorsBean(page,request);

        colorsH.setColorsBean(colorsBean);

        String family = request.getParameter("family");
        String[] familyArray = family.split(SPLIT_CHAR);

        int numPerm = getNumPerm(familyArray.length);

        Map<String,String> permutation = new LinkedHashMap<String,String>();
        int length = familyArray.length;
        permute(familyArray,numPerm,length-1,length-2,permutation);

        JSONArray optionsArray = getCombinationArray(permutation);

        try {
            finalJsonResponse.put("combo", optionsArray);
        } catch (JSONException e) {
            ExceptionLogger.logException(e);
        }
        String finalJsonResponseString = finalJsonResponse.toString();
        response.getWriter().println(finalJsonResponseString);
    }
    private int getNumPerm(int num) {
        if(num == 0) {
            return 1;
        } else {
            int numPerm = num * getNumPerm(num-1);
            return numPerm;
        }
    }

    private JSONArray getCombinationArray(Map<String,String> combination) {

    	String colors = "";
        JSONArray optionsArray = new JSONArray();
        Iterator it = combination.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            colors = GetHexColor(pair.getKey().toString());
            optionsArray.put(pair.getKey()+","+pair.getValue()+","+ colors);
            it.remove(); // avoids a ConcurrentModificationException
        }
        return optionsArray;
    }

    private void permute(String[] arr, int numPerm, int swap1,int swap2,Map<String,String> perm) {

        if (numPerm > 0) {

            StringBuilder familyComb = new StringBuilder();
            for (int i =0; i < arr.length; i++) {
                familyComb.append(arr[i]);
                if(i+1 < arr.length) familyComb.append("-");
            }

            perm.put(familyComb.toString(),getText(familyComb.toString()));

            String color = arr[swap1];
            arr[swap1] = arr[swap2];
            arr[swap2] = color;

            if( (swap2-1) >= 0) {
                permute(arr, numPerm-1, swap1, swap2 - 1, perm);
            } else if(swap2 == 0 ) {
                permute(arr, numPerm-1, swap1, swap2+1, perm);
            }

        }
    }
    private String getText(String familyValue){

        String[] familyArray = familyValue.split(SPLIT_CHAR);
        StringBuilder familyText = new StringBuilder();

        for(int i = 0; i < familyArray.length; i++){
            familyText.append(colorsH.getColor(familyArray[i]).getName());
            if(i+1 < familyArray.length) familyText.append(" / ");
        }

        return familyText.toString();
    }
    
    private String GetHexColor(String combination)
    {
    	String hexColor = "";
    	String[] colors = combination.split(SPLIT_CHAR);
    	StringBuilder resultCode = new StringBuilder();    	
    	
    	for(int i = 0;i < colors.length;i++)
    	{
    		hexColor = colorsH.getColor(colors[i]).getHexColor();
    		resultCode.append(hexColor);
    		if(i+1 < colors.length)
    			resultCode.append(",");
    	}
    	
    	return resultCode.toString();
    }

}

