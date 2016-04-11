package com.liferoles.rest.JSON;

public class PieChartDataItem {
	private String key;
	private int y;
	
	public PieChartDataItem(){}
	
	public PieChartDataItem(String key, int y){
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
