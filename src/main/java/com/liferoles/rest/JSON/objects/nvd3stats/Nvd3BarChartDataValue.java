package com.liferoles.rest.JSON.objects.nvd3stats;

public class Nvd3BarChartDataValue {
	@Override
	public String toString() {
		return "BarChartDataValue [x=" + x + ", y=" + y + "]";
	}
	private String x;
	private int y;
	
	public Nvd3BarChartDataValue(){}
	
	public Nvd3BarChartDataValue(String x, int y){
		this.x=x;
		this.y=y;
	}
	
	public String getX() {
		return x;
	}
	public void setX(String x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
}
