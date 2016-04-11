package com.liferoles.rest.JSON;

import java.util.List;

public class BarChartDataItem {
	@Override
	public String toString() {
		return "BarChartDataItem [key=" + key + ", values=" + values + "]";
	}
	private String key;
	private List<BarChartDataValue> values;
	
	public BarChartDataItem(){}
	
	public BarChartDataItem(String key, List<BarChartDataValue> values){
		this.key = key;
		this.values = values;
	}
	
	public List<BarChartDataValue> getValues() {
		return values;
	}
	public void setValues(List<BarChartDataValue> values) {
		this.values = values;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
