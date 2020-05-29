package com.pwc.chart.impl;

import java.util.ArrayList;
import java.util.Vector;
import com.pwc.chart.type.Chart;
import com.pwc.chart.type.ChartExploded;
import com.pwc.chart.type.ChartTemplate;
import org.apache.commons.lang3.StringEscapeUtils;

public class ChartDataHelper  {


	public static String[][] parse(String htmlContent) {
		Vector<Vector<String>> contents = new Vector<Vector<String>>();
		for(int i=0; i<htmlContent.length(); i++) {
			// Search <tr
			int idxTr = htmlContent.indexOf("<tr", i);
			if (idxTr < 0)
				break;
			i=idxTr;

			Vector<String> row = new Vector<String>();

			// Search </tr
			int idxTrEnd =  htmlContent.indexOf("</tr", i);
			if (idxTrEnd < 0)
				idxTrEnd = htmlContent.length();

			while(i<idxTrEnd) {
				// Search <td
				int idxTd = htmlContent.indexOf("<td", i);
				if (idxTd < 0)
					break;
				if (idxTd > idxTrEnd) {
					i=idxTrEnd;
					break;
				}
				// Search Cell Start
				int idxCell = htmlContent.indexOf(">", idxTd);
				if (idxCell <0)
					break;
				int idxCellEnd = htmlContent.indexOf("</td", idxCell);
				if (idxCellEnd < 0)
					idxCellEnd = htmlContent.length();
				String cellContent = htmlContent.substring(idxCell + 1, idxCellEnd);				
				row.add(cellContent.replaceAll("\\<.*?\\>", ""));
				i=idxCellEnd;
			}

			contents.add(row);
		}
		return getContentsArray(contents);
	}

	public static String[][] getContentsArray(Vector<Vector<String>> chartVector) {
		String[][] contentsArray = new String[chartVector.size()][chartVector.get(0).size()];
		for(int i=0; i<chartVector.size(); i++) {
			for(int j=0; j<chartVector.get(i).size(); j++) {				
				String s = (String) chartVector.get(i).get(j);
				s = StringEscapeUtils.unescapeHtml4(s);
				contentsArray[i][j] = s.equals("\u00A0") ? "0" : s;
			}
		}
		return contentsArray;
	}	

	public static boolean validate(String[][] chartTable)
	{
		for(int i =1; i< chartTable.length;i++){
			for(int j =1; j< chartTable[i].length;j++){
				String value = chartTable[i][j];
				if(value.isEmpty() ) continue;
				if(!tryParseDouble(value) ){
					return false;
				}
			}

		}
		return true;
	}

	static boolean  tryParseDouble(String value)  
	{  		
		try  
		{  
			if (value == null) return false; //To avoid NullPointerException thrown by parseDouble.
			
			boolean b = value.matches("^.*[a-zA-Z]$");
			if (b) return false; //ends with a letter.  This is to prevent, d or f, for double and float data type.
			
			double d = Double.parseDouble(value);
			return true;  
		} catch(NumberFormatException nfe)  
		{  
			return false;  
		} 
	}

	public static String[][] transposeMatrix(String[][] m){
		String[][] temp = new String[m[0].length][m.length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++)
				temp[j][i] = m[i][j];
		return temp;
	}

	public static ArrayList<String>  getCategoriesArrayList(String[][] m){
		ArrayList<String> results = new ArrayList<>();
		for (int i =1;i<m.length;i++) {
			results.add(m[i][0]);
		}
		return results; 
	}

	public static String getCategories(String[][] m) {
		ArrayList<String> results = getCategoriesArrayList(m);
		StringBuffer categories = new StringBuffer();
		for (String cat : results) {
			if (categories.length() > 0)
				categories.append(",");
			categories.append(safeString(cat)); 
		}
		return categories.toString();
	}

	public static String getSeries(String[][] m,String explodedValue,Boolean pie) {
		ChartTemplate template = pie ? new ChartExploded(): new Chart();
		return template.getSeries(m, explodedValue);
	}

	public static String safeString(String formatString)
	{		
		String escapedString = org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript(formatString);
						
		return String.format("'%s'", escapedString);
	}
	
	public static boolean hasEmptyRows(String[][] chartTable)
	{
		for(int i =1; i< chartTable.length;i++){
			if(isRowEmpty(chartTable[i])) return true;
		}
		return false;
	}
	
	public static boolean isRowEmpty(String[] row){
		for(int j =1; j< row.length;j++){
			if( !row[j].isEmpty() ) {
				return false;
			}
		}
		return true;
	}

}


