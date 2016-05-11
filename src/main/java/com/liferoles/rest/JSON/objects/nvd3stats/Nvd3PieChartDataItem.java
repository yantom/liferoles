package com.liferoles.rest.JSON.objects.nvd3stats;

public class Nvd3PieChartDataItem {
	private String key;
	private int y;
	
	public Nvd3PieChartDataItem(){}
	
	public Nvd3PieChartDataItem(String key, int y){
		this.key=key;
		this.y=y;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
