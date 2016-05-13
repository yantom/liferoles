package com.liferoles.controller;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
	
	@Override
	public Nvd3ChartsData getJsonStatsData(int year, int month, boolean currentMonth, Long userId) {
		StatsData sd = getStatsData(year, month, currentMonth, userId);
		if (sd == null)
			return null;
		Nvd3ChartsData chartsData = new Nvd3ChartsData();
		List<Nvd3PieChartDataItem> pieChartDataList = new ArrayList<>();
		List<Nvd3BarChartDataItem> roleBarCharDataList = new ArrayList<>();
		List<Nvd3BarChartDataItem> weekBarCharDataList = new ArrayList<>();
		
		Nvd3BarChartDataItem roleBarCharDataItemList0 = new Nvd3BarChartDataItem();
		Nvd3BarChartDataItem weekBarCharDataItemList0 = new Nvd3BarChartDataItem();
		Nvd3BarChartDataItem roleBarCharDataItemList1 = new Nvd3BarChartDataItem();
		Nvd3BarChartDataItem weekBarCharDataItemList1 = new Nvd3BarChartDataItem();
		Nvd3BarChartDataItem roleBarCharDataItemList2 = new Nvd3BarChartDataItem();
		Nvd3BarChartDataItem weekBarCharDataItemList2 = new Nvd3BarChartDataItem();
		Nvd3BarChartDataItem roleBarCharDataItemList3 = new Nvd3BarChartDataItem();
		Nvd3BarChartDataItem weekBarCharDataItemList3 = new Nvd3BarChartDataItem();

		List<Nvd3BarChartDataValue> roleBarCharDataValueList0 = new ArrayList<>();
		List<Nvd3BarChartDataValue> weekBarCharDataValueList0 = new ArrayList<>();
		List<Nvd3BarChartDataValue> roleBarCharDataValueList1 = new ArrayList<>();
		List<Nvd3BarChartDataValue> weekBarCharDataValueList1 = new ArrayList<>();
		List<Nvd3BarChartDataValue> roleBarCharDataValueList2 = new ArrayList<>();
		List<Nvd3BarChartDataValue> weekBarCharDataValueList2 = new ArrayList<>();
		List<Nvd3BarChartDataValue> roleBarCharDataValueList3 = new ArrayList<>();
		List<Nvd3BarChartDataValue> weekBarCharDataValueList3 = new ArrayList<>();

		for (String key : sd.getCountOfTasksPerRoleData().keySet()) {
			pieChartDataList.add(new Nvd3PieChartDataItem(key, sd.getCountOfTasksPerRoleData().get(key)));
		}
		chartsData.setPieChartItems(pieChartDataList);
		

		for (String key : sd.getCountOfTasksPerRoleAndEffeciencyData()[0].keySet()) {
			roleBarCharDataValueList0
					.add(new Nvd3BarChartDataValue(key, sd.getCountOfTasksPerRoleAndEffeciencyData()[0].get(key)));
		}
		roleBarCharDataItemList0.setKey("Earlier than planned");
		roleBarCharDataItemList0.setValues(roleBarCharDataValueList0);
		roleBarCharDataList.add(roleBarCharDataItemList0);

		for (String key : sd.getCountOfTasksPerRoleAndEffeciencyData()[1].keySet()) {
			roleBarCharDataValueList1
					.add(new Nvd3BarChartDataValue(key, sd.getCountOfTasksPerRoleAndEffeciencyData()[1].get(key)));
		}
		roleBarCharDataItemList1.setKey("Day D");
		roleBarCharDataItemList1.setValues(roleBarCharDataValueList1);
		roleBarCharDataList.add(roleBarCharDataItemList1);

		for (String key : sd.getCountOfTasksPerRoleAndEffeciencyData()[2].keySet()) {
			roleBarCharDataValueList2
					.add(new Nvd3BarChartDataValue(key, sd.getCountOfTasksPerRoleAndEffeciencyData()[2].get(key)));
		}
		roleBarCharDataItemList2.setKey("Within 3 days");
		roleBarCharDataItemList2.setValues(roleBarCharDataValueList2);
		roleBarCharDataList.add(roleBarCharDataItemList2);

		for (String key : sd.getCountOfTasksPerRoleAndEffeciencyData()[3].keySet()) {
			roleBarCharDataValueList3
					.add(new Nvd3BarChartDataValue(key, sd.getCountOfTasksPerRoleAndEffeciencyData()[3].get(key)));
		}
		roleBarCharDataItemList3.setKey("Postponed");
		roleBarCharDataItemList3.setValues(roleBarCharDataValueList3);
		roleBarCharDataList.add(roleBarCharDataItemList3);

		chartsData.setBarChartItemsRole(roleBarCharDataList);

		for (String key : sd.getCountOfTasksPerWeekAndEffeciencyData()[0].keySet()) {
			weekBarCharDataValueList0
					.add(new Nvd3BarChartDataValue(key, sd.getCountOfTasksPerWeekAndEffeciencyData()[0].get(key)));
		}
		weekBarCharDataItemList0.setKey("Earlier than planned");
		weekBarCharDataItemList0.setValues(weekBarCharDataValueList0);
		weekBarCharDataList.add(weekBarCharDataItemList0);

		for (String key : sd.getCountOfTasksPerWeekAndEffeciencyData()[1].keySet()) {
			weekBarCharDataValueList1
					.add(new Nvd3BarChartDataValue(key, sd.getCountOfTasksPerWeekAndEffeciencyData()[1].get(key)));
		}
		weekBarCharDataItemList1.setKey("Day D");
		weekBarCharDataItemList1.setValues(weekBarCharDataValueList1);
		weekBarCharDataList.add(weekBarCharDataItemList1);

		for (String key : sd.getCountOfTasksPerWeekAndEffeciencyData()[2].keySet()) {
			weekBarCharDataValueList2
					.add(new Nvd3BarChartDataValue(key, sd.getCountOfTasksPerWeekAndEffeciencyData()[2].get(key)));
		}
		weekBarCharDataItemList2.setKey("Within 3 days");
		weekBarCharDataItemList2.setValues(weekBarCharDataValueList2);
		weekBarCharDataList.add(weekBarCharDataItemList2);

		for (String key : sd.getCountOfTasksPerWeekAndEffeciencyData()[3].keySet()) {
			weekBarCharDataValueList3
					.add(new Nvd3BarChartDataValue(key, sd.getCountOfTasksPerWeekAndEffeciencyData()[3].get(key)));
		}
		weekBarCharDataItemList3.setKey("Postponed");
		weekBarCharDataItemList3.setValues(weekBarCharDataValueList3);
		weekBarCharDataList.add(weekBarCharDataItemList3);

		chartsData.setBarChartItemsWeek(weekBarCharDataList);

		return chartsData;
	}
}
