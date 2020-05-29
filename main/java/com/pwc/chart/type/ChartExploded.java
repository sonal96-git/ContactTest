package com.pwc.chart.type;

import java.util.HashMap;
import java.util.Map;

import com.pwc.chart.impl.ChartDataHelper;
import com.pwc.chart.impl.MapFormat;

public class ChartExploded extends ChartTemplate{

	@Override
	public String getSerieData(String[][] m, int pos,String explodedValue) {
		StringBuffer data = new StringBuffer();
		String obj = "{ name: {Name} , y: {Value} {Slice} }";

		for (int rowIndex = 1  /*skip first*/;  rowIndex < m.length; rowIndex++)
		{
			if (data.length() > 0)
				data.append(",");

			String name = m[rowIndex][0];

			Map<String, String> map = new HashMap<String, String>();
			map.put("Name", ChartDataHelper.safeString(name));
			map.put("Value", m[rowIndex][1]);
			map.put("Slice", (explodedValue.isEmpty() || !explodedValue.equals(name))? "":", sliced: true");

			data.append(MapFormat.format(obj,map));
		}

		return String.format("[%s]", data);
	}

}
