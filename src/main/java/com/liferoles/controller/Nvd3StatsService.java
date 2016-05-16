package com.liferoles.controller;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import com.liferoles.rest.JSON.objects.nvd3stats.Nvd3BarChartDataItem;
import com.liferoles.rest.JSON.objects.nvd3stats.Nvd3BarChartDataValue;
import com.liferoles.rest.JSON.objects.nvd3stats.Nvd3ChartsData;
import com.liferoles.rest.JSON.objects.nvd3stats.Nvd3PieChartDataItem;

/**
 * Class which extends StatsService with method getJsonStatsData which returns
 * object which is utilized by Angular-nvD3 charts library in the frontend
 * controller.
 * 
 * @author Honzator
 *
 */
@LocalBean
@Stateless
public class Nvd3StatsService extends StatsService<Nvd3ChartsData> {
	
	@SuppressWarnings("unchecked")
	@Override
	public Nvd3ChartsData getJsonStatsData(int year, int month, boolean currentMonth, Long userId) {
		StatsData sd = getStatsData(year, month, currentMonth, userId);
		if (sd == null)
			return null;
		Nvd3ChartsData chartsData = new Nvd3ChartsData();
		List<Nvd3PieChartDataItem> pieChartData = new ArrayList<>();
		List<Nvd3BarChartDataItem> roleBarChartData = new ArrayList<>();
		List<Nvd3BarChartDataItem> weekBarChartData = new ArrayList<>();
		
		Nvd3BarChartDataItem[] roleBarChartDataItems = new Nvd3BarChartDataItem[4];
		Nvd3BarChartDataItem[] weekBarChartDataItems = new Nvd3BarChartDataItem[4];
		List<Nvd3BarChartDataValue>[] roleBarChartDataValues = (ArrayList<Nvd3BarChartDataValue>[]) new List[4];
		List<Nvd3BarChartDataValue>[] weekBarChartDataValues = (ArrayList<Nvd3BarChartDataValue>[]) new List[4];
		
		String[] indexes = {"Earlier than planned","Day D","Within 3 days","Postponed"};

		//fill data for pie chart
		for (String key : sd.getCountOfTasksPerRoleData().keySet()) {
			pieChartData.add(new Nvd3PieChartDataItem(key, sd.getCountOfTasksPerRoleData().get(key)));
		}
		chartsData.setPieChartItems(pieChartData);
		
		//fill data for roles and weeks charts
		for(int i =0;i<4;i++){
			
			for (String key : sd.getCountOfTasksPerRoleAndEffeciencyData()[i].keySet()) {
				roleBarChartDataValues[i].add(new Nvd3BarChartDataValue(key, sd.getCountOfTasksPerRoleAndEffeciencyData()[i].get(key)));
			}
			roleBarChartDataItems[i].setKey(indexes[i]);
			roleBarChartDataItems[i].setValues(roleBarChartDataValues[i]);
			roleBarChartData.add(roleBarChartDataItems[i]);
			
			for (String key : sd.getCountOfTasksPerWeekAndEffeciencyData()[i].keySet()) {
				weekBarChartDataValues[0].add(new Nvd3BarChartDataValue(key, sd.getCountOfTasksPerWeekAndEffeciencyData()[i].get(key)));
			}
			weekBarChartDataItems[i].setKey(indexes[i]);
			weekBarChartDataItems[i].setValues(weekBarChartDataValues[i]);
			weekBarChartData.add(weekBarChartDataItems[i]);
		}

		chartsData.setBarChartItemsRole(roleBarChartData);
		chartsData.setBarChartItemsWeek(weekBarChartData);

		return chartsData;
	}
}
