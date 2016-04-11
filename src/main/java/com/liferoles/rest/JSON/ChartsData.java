package com.liferoles.rest.JSON;

import java.util.List;

public class ChartsData {
	private List<PieChartDataItem> pieChartItems;
	private List<BarChartDataItem> barChartItemsRole;
	private List<BarChartDataItem> barChartItemsWeek;
	
	public List<PieChartDataItem> getPieChartItems() {
		return pieChartItems;
	}
	public void setPieChartItems(List<PieChartDataItem> pieChartItems) {
		this.pieChartItems = pieChartItems;
	}
	public List<BarChartDataItem> getBarChartItemsRole() {
		return barChartItemsRole;
	}
	public void setBarChartItemsRole(List<BarChartDataItem> barChartItemsRole) {
		this.barChartItemsRole = barChartItemsRole;
	}
	public List<BarChartDataItem> getBarChartItemsWeek() {
		return barChartItemsWeek;
	}
	public void setBarChartItemsWeek(List<BarChartDataItem> barChartItemsWeek) {
		this.barChartItemsWeek = barChartItemsWeek;
	}
}
