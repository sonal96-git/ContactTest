package com.pwc.chart.type;

import java.util.HashMap;
import java.util.Map;

import com.pwc.chart.impl.ChartDataHelper;
import com.pwc.chart.impl.MapFormat;

public abstract class ChartTemplate {

	public String getSeries(String[][] m,String explodedValue) {
		StringBuffer series = new StringBuffer();
		for (int i = 1 /*skip first*/; i < m[0].length; i++)
		{
			String serie = "{name: {Name}, data: {Data} {AditionalSerieConfiguration} }";
			String serieName = m[0][i];
			String serieData = getSerieData(m,i,explodedValue);

			Map<String, String> map = new HashMap<String, String>();
			map.put("Name", ChartDataHelper.safeString(serieName));
			map.put("Data", serieData);
			map.put("AditionalSerieConfiguration", "");

			serie = MapFormat.format(serie,map);

			if (series.length()>0)
				series.append(",");
			series.append(serie);
		}

		return series.toString();
	}
	
	public abstract String getSerieData(String[][] m,int pos,String explodedValue);
	

}
