package com.pwc.chart.impl;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONException;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;

import com.pwc.chart.ChartUtilService;
import com.pwc.chart.type.ChartScatter;

/** Util class for creating PwC charts
 * 
 * @author PwC Uy
 */
@Component(service = ChartUtilService.class)
public class ChartUtilImpl implements ChartUtilService {
	
	private ValueMap chartProperties; 

	@Override
	public void setChartResource(Resource chartResource) {
		this.chartProperties = chartResource.adaptTo(ValueMap.class);
	}

	@Override
	public String getStringProperty(String propertyName) {
		return  "'" + chartProperties.get(propertyName, "") + "'";
	}
	@Override
	public boolean getBoolProperty(String propertyName)  {
		return  Boolean.parseBoolean(chartProperties.get(propertyName, "false"));
	}
	@Override
	public Integer getNumberProperty(String propertyName)  {
		Integer propertyValue = null;
		if (chartProperties.containsKey(propertyName))
		{
			try
			{
				propertyValue = Integer.parseInt(chartProperties.get(propertyName,"0"));
			}
			catch(NumberFormatException nfe){}
		}
		return  propertyValue;
	}

	@Override
	public boolean isPropertyEnabled(String propertyName) {
		return  chartProperties.containsKey(propertyName);
	}

	@Override
	public String numberFormat(String value){

		String text = "Highcharts.numberFormat({Number}, {Decimals}, {DecimalPoint}, {ThousandsSep})";
		Map<String, String> map = new HashMap<String, String>();
		map.put("Number", value);
		String decimals  = (this.getNumberProperty("chartoptions/decimals")!=null) ? this.getNumberProperty("chartoptions/decimals").toString() : "0";		
		map.put("Decimals", decimals );
		map.put("DecimalPoint", this.getStringProperty("chartoptions/decimalPoint"));
		map.put("ThousandsSep", this.getStringProperty("chartoptions/thousandsSep"));

		return MapFormat.format(text, map);
	}

	@Override
	public String injectFormatter( String formatter, boolean isAxis)
	{
		if(formatter == null || formatter.equals("''")){
			return "this.value";
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("Percentage", concatenate(numberFormat("this.percentage")));
		map.put("PointName", concatenate("this.point.name"));
		map.put("SeriesName", concatenate("this.series.name"));
		map.put("Total", concatenate(numberFormat("this.total")));
		map.put("Value", isAxis ? concatenate(numberFormat("this.value")) : concatenate(numberFormat("this.y")));
		map.put("Category", concatenate("this.point.category"));

		return MapFormat.format(formatter,map);
	}

	@Override
	public String getLegend()
    {
        if (this.getNumberProperty("chartoptions/legendLocation") == 0)
            return "enabled: false";

        String legendCommon = "enabled: true, backgroundColor: '#FFFFFF', shadow: false";

        String left = ",layout: 'vertical', align: 'left',   verticalAlign: 'middle'";

        String right = ",layout: 'vertical',align: 'right',verticalAlign: 'middle'";

        String top = ",layout: 'horizontal', align: 'center', verticalAlign: 'top'";

        String bottom = ",layout: 'horizontal',align: 'center', verticalAlign: 'bottom'";

        String result = "";
        switch (this.getNumberProperty("chartoptions/legendLocation"))
        {
            case 1:
                result = top;
                break;
            case 2:
                result = right;
                break;
            case 3:
                result = bottom;
                break;
            case 4:
                result = left;
                break;
           
        }

        return legendCommon + result;
    }
	
	@Override
	public String getColor() throws JSONException, IOException{
		    return getJsonColor(chartProperties.get("chartoptions/colorCombo", "burgundy").toLowerCase() +".json");
	}
	
	@Override
	public String getChartType() {
		String type = "bar";
		switch (chartProperties.get("charttype/type", "bar"))
        {
            case "area_clustered":            	
            case "area_stacked":
            case "area_percent":
            	type = (this.getBoolProperty("chartadvanced/smooth")?  "areaspline" :  "area") ;
            	break;
            case "bar_clustered":            	
            case "bar_stacked":
            case "bar_percent":
            	type = "bar";
            	break;
            case "column_clustered":            	
            case "column_stacked":
            case "column_percent":
            	type = "column";
            	break;
            case "line":
            	type = (this.getBoolProperty("chartadvanced/smooth")?  "spline" :  "line") ;
            	break;
            case "doughnut":            	
            case "doughnut_exploded":
            case "pie":
            case "pie_exploded":
            	type = "pie";
            	break;
            case "scatter":
            	type = "scatter";
            	break;
        }
		return type;
	
	}
	@Override
	public String getSerieStacking() {
		String chartType = chartProperties.get("charttype/type", "bar");
		if(chartType.indexOf("stacked") > -1){
			return "'stacked'";
		};
		if(chartType.indexOf("percent") > -1){
			return "'percent'";
		};
		return null;
	}
	
	@Override
	public String getInnerSize() {
		String size = (this.isPropertyEnabled("piedoughnutoptions/innerSize") ) ? chartProperties.get("piedoughnutoptions/innerSize", "0"):"70";
		String chartType = chartProperties.get("charttype/type", "bar");
		String innerSize = "{}"; 
		if(chartType.indexOf("doughnut") > -1 ) innerSize = "{  series: [{	innerSize: '"+size+"%'  }]	}";
		//TODO ESTO DEBE IR EN ADICIONAL VALUES Y YO ME ACUERDO (LALO)
		return innerSize;
	}
	
	@Override
	public String getSeries(){
		String series;
		switch (chartProperties.get("charttype/type", "bar"))
		{		
		case "pie":
		case "doughnut":
			series =ChartDataHelper.getSeries(ChartDataHelper.parse(this.getStringProperty("datainput/tableData")),"",true);
			break;
		case "pie_exploded":		
		case "doughnut_exploded":	
			series =ChartDataHelper.getSeries(ChartDataHelper.parse(this.getStringProperty("datainput/tableData")),chartProperties.get("exploded/categories", ""),true);
			break;
		case "scatter":
			series = ChartScatter.getSeriesScatter(ChartDataHelper.parse(this.getStringProperty("datainput/tableData")),chartProperties.get("scatteroptions/markedSymbols", ""));
			break;
		default:
			if (this.isPropertyEnabled("chartoptions/switchRowsToColumns") ) {
				series =  ChartDataHelper.getSeries(ChartDataHelper.transposeMatrix(ChartDataHelper.parse(this.getStringProperty("datainput/tableData"))),"",false);
			}else{
				String[][] strParsed = ChartDataHelper.parse(this.getStringProperty("datainput/tableData"));
				series =  ChartDataHelper.getSeries(strParsed,"",false);
			};
		}
		return series;
	}
	
	@Override
	public String getCategories(){
		String categories;
		if (this.isPropertyEnabled("chartoptions/switchRowsToColumns") ) {
			categories = ChartDataHelper.getCategories(ChartDataHelper.transposeMatrix(ChartDataHelper.parse(this.getStringProperty("datainput/tableData"))));
		}else{
			categories = ChartDataHelper.getCategories(ChartDataHelper.parse(this.getStringProperty("datainput/tableData")));
		}
		return categories;
	}
	
	private String getJsonColor(String fileName) throws JSONException, IOException{
		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		URL url = bundle.getResource(fileName);
		return IOUtils.toString(url.openConnection().getInputStream(), "UTF-8");
		
	}
	
	private  String concatenate(String value)
	{
		return String.format("'+%s+'", value);
	}
	
}
