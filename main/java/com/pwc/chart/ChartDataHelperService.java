package com.pwc.chart;

public interface ChartDataHelperService {
	public String[][] parse(String htmlContent);
	public String validate(String[][] chartTable);
}
