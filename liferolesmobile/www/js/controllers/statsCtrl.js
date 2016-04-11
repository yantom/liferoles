angular.module('liferolesApp').controller("statsCtrl",function($scope,$http,$timeout,$ionicSideMenuDelegate){
	var monthsBeforeToday = 0;
	var date = new Date();
	var chartsData = [];
	var months = ["January","February","March","April","May","June","July","August","September","October","November","December"];
	var viewedMonth = date.getMonth()+1;
	$scope.animate;
	$scope.dataBarRole;
	$scope.dataBarWeek;
	$scope.dataPie;
	$scope.$on('$ionicView.enter', function() {
			$scope.g1.refresh();
			$scope.g2.refresh();
			$scope.g3.refresh();
	});
	$scope.viewedYear = date.getFullYear();
	$scope.viewedMonthName = months[viewedMonth-1];
	$scope.goForward = function(){
		if(monthsBeforeToday == 0)
			return;
		;
		$scope.animate = "slideInRight";
		$timeout(function(){$scope.animate = "";},500);
		monthsBeforeToday--;
		if(viewedMonth != 12)
			viewedMonth++;
		else{
			viewedMonth = 1;
			$scope.viewedYear++;
			}
		$scope.viewedMonthName = months[viewedMonth-1];
		$scope.dataBarRole = chartsData[monthsBeforeToday].barChartItemsRole;
		$scope.dataBarWeek = chartsData[monthsBeforeToday].barChartItemsWeek;
		$scope.dataPie = chartsData[monthsBeforeToday].pieChartItems;
	}
	$scope.goPast = function(){
		$scope.animate = "slideInLeft";
		$timeout(function(){$scope.animate = "";},500);
		monthsBeforeToday++;
		if(viewedMonth != 1)
			viewedMonth--;
		else{
			viewedMonth = 12;
			$scope.viewedYear--;
			}
		$scope.viewedMonthName = months[viewedMonth-1];
		if(chartsData.length<=monthsBeforeToday){
			pushNewChartsData($scope.viewedYear,viewedMonth,"");
		}
		else{
			$scope.dataBarRole = chartsData[monthsBeforeToday].barChartItemsRole;
			$scope.dataBarWeek = chartsData[monthsBeforeToday].barChartItemsWeek;
			$scope.dataPie = chartsData[monthsBeforeToday].pieChartItems;
		}
	}
	var pushNewChartsData = function(year,month,query){
		$http.get(host + "/rest/users/"+platform+"/stats/" + year + "/" + month + query).then(
		function(val){
			if(val.data ==""){
				chartsData.push({barChartItemsRole:[],barChartItemsWeek:[],pieChartItems:[]});}
			else{
				chartsData.push(val.data);
			};
			$scope.dataBarRole = chartsData[monthsBeforeToday].barChartItemsRole;
			$scope.dataBarWeek = chartsData[monthsBeforeToday].barChartItemsWeek;
			$scope.dataPie = chartsData[monthsBeforeToday].pieChartItems;
		},
		function(response){
			$scope.handleErrors(response);
		});
	}
	$scope.optionsBar = {
            chart: {
                type: 'multiBarChart',
                height: 350,
                margin : {
                    top: 0,
                    right: 0,
                    bottom: 40,
                    left: 50
                },
                reduceXTicks : false,
                staggerLabels: true,
                showControls:false,
                duration: 500,
                stacked: true,
                yAxis:{
                	tickFormat:function(d){return d3.format("d")(d);}
                }
            }
        };
	$scope.optionsPie = {
            chart: {
                type: 'pieChart',
                height: 350,
                donut: true,
                x: function(d){return d.key;},
                y: function(d){return d.y;},
                duration: 500,
                labelType:"percent",
                donutRatio:".5",
                yAxis:{
                	tickFormat:function(d){return d3.format(',0f')(d);}
                },
                valueFormat:function(d){return d3.format('d')(d);}
            },
            title:{
            	enable:true,
            	text:'Completed tasks per role',
            	className: "h4"
            }
        };
        var init = function(){
        	pushNewChartsData($scope.viewedYear,viewedMonth,"?last=true");
        }
        init();
		$scope.openMenu = function(){
		$ionicSideMenuDelegate.toggleLeft();
	}
});