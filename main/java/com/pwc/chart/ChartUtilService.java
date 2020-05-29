package com.pwc.chart;

import java.io.IOException;

import org.apache.sling.api.resource.Resource;
import org.json.JSONException;

/** Service Util class for creating PwC charts
 * 
 * @author PwC Uy
 */
public interface ChartUtilService {
	
	public void setChartResource(Resource chartResource);	
	public String getSeries();
	public String getCategories();
	public String getStringProperty(String propertyName);
	public boolean getBoolProperty(String propertyName);
	public Integer getNumberProperty(String propertyName) ;
	public boolean isPropertyEnabled(String propertyName);
	public String numberFormat(String value);
	public String injectFormatter( String formatter, boolean isAxis);
	public String getLegend();
	public String getColor() throws JSONException, IOException;
	public String getChartType();
	public String getSerieStacking();
	public String getInnerSize();

}
