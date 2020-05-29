package com.pwc.chart.type;

import java.util.HashMap;
import java.util.Map;

import com.pwc.chart.impl.ChartDataHelper;
import com.pwc.chart.impl.MapFormat;

public class ChartScatter {
	
	public static String getSerieDataScatter(int colIndex,String[][] m)
	{
		StringBuffer data = new StringBuffer();
		for (int rowIndex = 1  /*skip first*/; rowIndex < m.length; rowIndex++)
		{
			if (data.length() > 0)
				data.append(",");


			String cellData1 = m[rowIndex][colIndex];
			String cellData2 = m[rowIndex][colIndex+1];

			Map<String, String> map = new HashMap<String, String>();
			map.put("CellData1", (cellData1 == null || cellData1.isEmpty())?"null":cellData1);
			map.put("CellData2", (cellData2 == null || cellData2.isEmpty())?"null":cellData2);

			data.append(MapFormat.format("[{CellData1},{CellData2}]",map)); 
		}

		return String.format("[%s]", data);
	}

	public static String getSeriesScatter(String[][] m,String markerSymbol)
	{

		StringBuffer series = new StringBuffer();
		try{
			for (int i = 0; i < m[0].length; i = i + 2 /*voy de dos en dos (porque el GetSerieData toma su fila y la siguiente*/)
			{

				String serie = "{name: {Name}, data: {Data} {AditionalSerieConfiguration} }";
				String serieName = m[0][i];
				String serieData = getSerieDataScatter(i,m);

				Map<String, String> map = new HashMap<String, String>();
				map.put("Name", ChartDataHelper.safeString(serieName));
				map.put("Data", serieData);
				map.put("AditionalSerieConfiguration", getAditionalSerieConfiguration(i/2,markerSymbol));

				serie = MapFormat.format(serie,map);

				if (series.length() > 0)
					series.append(",");
				series.append(serie);
			}
		}catch(Exception e){}

		return series.toString();
	}
	
	public static String getAditionalSerieConfiguration(int index,String markerSymbols)
	{
		if (markerSymbols == null || markerSymbols.isEmpty())
			return "";

		String[] splitMarker = markerSymbols.split(",");
		if (splitMarker.length == 0)
			return "";

		if (splitMarker.length - 1 >= index)
		{
			return String.format(", marker: { symbol: '%s' }", splitMarker[index].trim());
		}

		return "";
	}
}
