package com.liferoles.rest.JSON.objects.nvd3stats;

import java.util.List;

public class Nvd3BarChartDataItem {
	
	private String key;
	private List<Nvd3BarChartDataValue> values;
	
	public Nvd3BarChartDataItem(){}
	
	public Nvd3BarChartDataItem(String key, List<Nvd3BarChartDataValue> values){
		this.key = key;
		this.values = values;
	}
	
	public List<Nvd3BarChartDataValue> getValues() {
		return values;
	}
	public void setValues(List<Nvd3BarChartDataValue> values) {
		this.values = values;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	@Override
	public String toString() {
		return "BarChartDataItem [key=" + key + ", values=" + values + "]";
	}
}
