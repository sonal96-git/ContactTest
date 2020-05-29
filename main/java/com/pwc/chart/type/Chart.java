package com.pwc.chart.type;


public class Chart extends ChartTemplate{

	public String getSerieData(String[][] m, int colIndex,String explodedValue )
	{
		StringBuffer data = new StringBuffer();
		for (int rowIndex = 1  /*skip first*/; rowIndex < m.length; rowIndex++)
		{
			if (data.length() > 0)
				data.append(",");
			String cellData = m[rowIndex][colIndex];

			data.append(cellData == null || cellData.isEmpty() ? "null" : cellData); // if first cell is empty replace value for null so Highcharts works correctly
		}

		return String.format("[%s]", data);
	}
}
