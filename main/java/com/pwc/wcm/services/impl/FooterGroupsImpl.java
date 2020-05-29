package com.pwc.wcm.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.pwc.wcm.services.FooterGroups;
import com.pwc.wcm.utils.JsonToMapConversion;

@Component(immediate = true, service = { FooterGroups.class }, enabled = true)
public class FooterGroupsImpl implements FooterGroups{
	
	@Reference
	private JsonToMapConversion jsonToMapConversion;
	
	private enum SingleColumn {
		columnone(0),  
		columntwo(1),  
		columnthree(2),
		columnfour(3),
		columnfive(4),
		columnsix(5)
	    ; 

	    private final int levelCode;

	    private SingleColumn(int levelCode) {
	        this.levelCode = levelCode;
	    }

		public int getLevelCode() {
			return levelCode;
		}
	}
	
	private enum DoubleColumn {
		columnonetwo(0),  
		columntwothree(1),  
		columnthreefour(2),
		columnfourfive(3),
		columnfivesix(4)
	    ; 

	    private final int levelCode;

	    private DoubleColumn(int levelCode) {
	        this.levelCode = levelCode;
	    }

		public int getLevelCode() {
			return levelCode;
		}
	}
	
	private List<Map<String, String>> footerGroupList;
	private List<Map<String, String>> stackOnGroupList;
	private int useColumn[] = new int[6];

	@Override
	public List<Map<String, String>> getFooterGroupList(String[] groupList) {
		footerGroupList = new ArrayList<Map<String, String>>();
		stackOnGroupList = new ArrayList<Map<String, String>>();
		for(int i = 0;i < useColumn.length;i++){
			useColumn[i] = 0;
		}
		List<Map<String, String>> propertyMap = jsonToMapConversion.getListFromJson(groupList);
		for(Map<String, String> property : propertyMap){
			addGroupToList(property);			
		}
		addStackOnGroupsToList();
		normaliseFooterGroupList();
		return footerGroupList;
	}
	
	private void addGroupToList(Map<String, String> property){	
		int index;
		if("singlecolumn".equals(property.get("columntype"))){
			if("".equals(property.get("usecolumn"))){
				stackOnGroupList.add(property);
			}else{
				index = SingleColumn.valueOf(property.get("usecolumn")).getLevelCode();
				if(useColumn[index] != -1){
					footerGroupList.add(useColumn[index], property);
					incrementArrayToRight(useColumn,index);
				}	
			}			
		}else if("doublecolumn".equals(property.get("columntype"))){
			index = DoubleColumn.valueOf(property.get("usecolumn")).getLevelCode();
			if(useColumn[index] != -1){
				footerGroupList.add(useColumn[index], property);
				useColumn[index] = -1;
				useColumn[index + 1] = -1;
				incrementArrayToRight(useColumn,index);
			}
		}
	}
	
	private void addStackOnGroupsToList(){
		int index;
		for(Map<String, String> stackOnGroupItem : stackOnGroupList){
			index = SingleColumn.valueOf(stackOnGroupItem.get("stackon")).getLevelCode();
			if(useColumn[index] != -1){
				footerGroupList.add(useColumn[index], stackOnGroupItem);
				incrementArrayToRight(useColumn,index);
			}			
		}
	}
	
	private void normaliseFooterGroupList(){
		if(useColumn[0] == 0)
		{
			footerGroupList.add(0, new HashMap<String, String>());
			incrementArrayToRight(useColumn,0);
		}
		for(int i=1;i<useColumn.length;i++){
			if(useColumn[i] != -1 && useColumn[i] == useColumn[i-1]){
				footerGroupList.add(useColumn[i], new HashMap<String, String>());
				incrementArrayToRight(useColumn,i);
			}
		}
	}
	
	private void incrementArrayToRight(int[] useColumn, int index){
		for(int i=index;i<useColumn.length;i++){
			if(useColumn[i] != -1)
				useColumn[i]++;
		}
	}

}

