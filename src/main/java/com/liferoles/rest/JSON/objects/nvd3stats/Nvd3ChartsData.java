package com.liferoles.rest.JSON.objects.nvd3stats;

import java.util.List;

public class Nvd3ChartsData {
	private List<Nvd3PieChartDataItem> pieChartItems;
	private List<Nvd3BarChartDataItem> barChartItemsRole;
	private List<Nvd3BarChartDataItem> barChartItemsWeek;
	
	public List<Nvd3PieChartDataItem> getPieChartItems() {
		return pieChartItems;
	}
	public void setPieChartItems(List<Nvd3PieChartDataItem> pieChartItems) {
		this.pieChartItems = pieChartItems;
	}
	public List<Nvd3BarChartDataItem> getBarChartItemsRole() {
		return barChartItemsRole;
	}
	public void setBarChartItemsRole(List<Nvd3BarChartDataItem> barChartItemsRole) {
		this.barChartItemsRole = barChartItemsRole;
	}
	public List<Nvd3BarChartDataItem> getBarChartItemsWeek() {
		return barChartItemsWeek;
	}
	public void setBarChartItemsWeek(List<Nvd3BarChartDataItem> barChartItemsWeek) {
		this.barChartItemsWeek = barChartItemsWeek;
	}
}
