//FILE SHARED BETWEEN PLATFORMS
angular.module('liferolesApp').controller("tasksCtrl",function($scope,TasksAndRoles, $interval,$ionicPopover,$http,$state,$timeout,$rootScope,$ionicScrollDelegate,$ionicModal,$ionicSideMenuDelegate,$ionicPlatform ){
	//PLATFORM SPECIFIC
	if(platform=="m"){
		$scope.days = ["Mon","Tue","Wed","Thu","Fri","Sat","Sun"];
		$scope.openMenu = function(){
			$ionicSideMenuDelegate.toggleLeft();
		}
		var pausedDate = null;
		$ionicPlatform.on("resume", function(){
			if((Date.now())>(pausedDate + 600000))
				$scope.refresh();
		});
		
		$ionicPlatform.on("pause", function(){
			pausedDate = Date.now();
		});
	}
	else{
		$scope.days = ["Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"];
		var taskModal;
		$ionicModal.fromTemplateUrl('task.html', {}).then(function(modal) {
		      taskModal = modal;
		});
		//refresh data every 5 minutes
		$interval(function() {
            $scope.refresh();
          }, 300000);
	}
	//
	//
	$scope.roles;
	var futureTasks;
	var weeks;
	var backlog;
	$scope.tasksCounts = [{count:0},{count:0},{count:0},{count:0},{count:0},{count:0},{count:0},{count:0},{count:0}];
	$scope.viewedTasks;
	$scope.label;
	//0 stands for next week, 1 for this week, 2 for last week etc.
	//-1 means future tasks, backlog is not indicated by current index but by daysmenuactiveitem == -2
	$scope.currentIndex;
	//-2 is backlog, -1 week summary, 0-6 days of week
	$scope.daysMenuActiveItem;
	$scope.daysMenuActiveItemDate;
	$scope.animateWeek = null;
	$scope.animateTitle = false;
	$scope.popover;
	$ionicPopover.fromTemplateUrl('rolesPopover.html',{scope:$scope}).then(function(popover) {$scope.popover = popover;});
	$scope.dragData = {taskId : null};
	$scope.data={newTaskName:null,newTaskRole:null,inputReadOnly:false,filterRole:null};

	var setData = function(){
		$scope.roles = TasksAndRoles.getRoles();
		//$scope.currentIndex = 1;
		futureTasks = TasksAndRoles.getfutureTasks();
		weeks = TasksAndRoles.getWeeks();
		backlog = TasksAndRoles.getBacklog();
		//
		if($scope.daysMenuActiveItem == -2)
			$scope.viewedTasks = backlog;
		else if($scope.currentIndex == -1)
			$scope.viewedTasks = futureTasks;
		else{
			$scope.viewedTasks = weeks[$scope.currentIndex];
			$scope.label = weeks[$scope.currentIndex].label;
		}
		//
		//$scope.viewedTasks = weeks[$scope.currentIndex];
		//$scope.label = weeks[$scope.currentIndex].label;
		//$scope.setActive(-1);
		computeTasksCounts();
	}
	
	$scope.setActive = function(num){
		if(num == -2 && $scope.daysMenuActiveItem != -2)
			$scope.viewedTasks = backlog;
		else if($scope.currentIndex == -1)
			$scope.viewedTasks = futureTasks;
		else if(num != -2 && $scope.daysMenuActiveItem == -2){
			$scope.viewedTasks = weeks[$scope.currentIndex];
			$scope.label = weeks[$scope.currentIndex].label;
		}
		$scope.data.filterRole = null;
		$scope.daysMenuActiveItem = num;
		$scope.daysMenuActiveItemDate = computeDateByDaysMenuItem(num);
		checkIfTaskCanBeCreated();
		if(platform == "m")
			$ionicScrollDelegate.$getByHandle('tasks').scrollTop(true);
	}

	$scope.refresh = function(){
		TasksAndRoles.init();
	}
	$scope.moveToBacklog = function(){
		if (confirm("Move all old, uncompleted tasks to backlog? (last week and older)")){
			$http.post(host + "/rest/tasks/"+platform+"/backlog/" + $scope.firstDayOfCurrentWeekDate.getFullYear() + "/" + ($scope.firstDayOfCurrentWeekDate.getMonth()+1) +"/"+ $scope.firstDayOfCurrentWeekDate.getDate(),{}).then(
					function(){
						TasksAndRoles.moveTasksToBacklog();
					},
					function(response){
						$scope.handleErrors(response);
					});
		}
	}
	$scope.tasksOrder = function(task){
		if(task.finished == true){
			if(task.date ==null)
				return 1;
			if(task.time == null)
				return 100000000000+parseInt(task.date.year+task.date.month+task.date.day+"0000");
			else
				return 100000000000+parseInt(task.date.year+task.date.month+task.date.day+task.time.hours+task.time.minutes);
		}
		if(task.date ==null)
			return 0;
		if(task.time == null)
			return parseInt(task.date.year+task.date.month+task.date.day+"0000");
		else
			return parseInt(task.date.year+task.date.month+task.date.day+task.time.hours+task.time.minutes);
	}
	$scope.$on('draggable:end',function(){
		$scope.dragData.taskId = null;
		$ionicScrollDelegate.freezeScroll(false);
	});
	$scope.$on('draggable:start',function(ev,el){
		$scope.dragData.taskId = el.data.id;
		$ionicScrollDelegate.freezeScroll(true);
	});
	$scope.moveTask = function($data,itemIndex){
		var newTaskDate = getParsedDate(computeDateByDaysMenuItem(itemIndex));
		updateTaskDate(angular.copy($data),newTaskDate);
	}
	var updateTaskDate = function(task,newDate){
		if(task.firstDate == null && newDate != null)
			task.firstDate = angular.copy(newDate);
		task.date = newDate;
		var factoryIndex;
		if($scope.daysMenuActiveItem == -2)
			factoryIndex = -2;
		else
			factoryIndex = $scope.currentIndex;
		$http.put(host+"/rest/tasks/"+platform+"/"+task.id,task).then(
			function(response){
				TasksAndRoles.updateTask(task,factoryIndex);
				computeTasksCounts();
			},function(response){
				$scope.handleErrors(response);
			});
	};
	$scope.viewDetails = function(task){
		var factoryIndex;
		if($scope.daysMenuActiveItem == -2)
			factoryIndex = -2;
		else
			factoryIndex = $scope.currentIndex;
		//PLATFORM SPECIFIC
		if(platform=="m"){
			$state.go("task",({
				taskId: task.id,
				factoryIndex: factoryIndex
				}));
		}
		else{
			taskModal.show().then(function(){
		    	$rootScope.$broadcast('viewTaskDetails',angular.copy(task),factoryIndex,taskModal);
		    });
		}
		//
	}
	var computeTasksCounts = function(){
		$scope.tasksCounts[0].count = backlog.tasks.length;
		if($scope.currentIndex == -1){
			$scope.tasksCounts[1].count = futureTasks.tasks.length;
			return;
		}
		$scope.tasksCounts[1].count=weeks[$scope.currentIndex].tasks.length;;
		var date = computeFirstDayOfDisplayedWeek();
		var initDateMs = date.getTime();
		var dayNumber;
		for(i=0;i<7;i++){
			date.setTime(initDateMs + i*86400000);
			dayNumber = date.getDate();
			$scope.tasksCounts[i+2].count = 0;
			for(j=0;j<weeks[$scope.currentIndex].tasks.length;j++){
				if(dayNumber == weeks[$scope.currentIndex].tasks[j].date.day)
					$scope.tasksCounts[i+2].count++;
			}
		}
	}
	$rootScope.$on('updateCounts',function(){
		computeTasksCounts();
	});
	$scope.goForward = function(){
		if($scope.currentIndex == -1 || $scope.dragData.taskId !=null)
			return;
		$scope.animateWeek = "right";
		$scope.animateTitle=true;
		$timeout(function(){
			$scope.animateWeek = null;
			$scope.animateTitle=false;
		},1000);
		$scope.currentIndex--;
		if($scope.currentIndex == -1){
			$scope.viewedTasks = futureTasks;
			$scope.label = "Future";
			}
		else{
			$scope.viewedTasks = weeks[$scope.currentIndex];
			$scope.label = weeks[$scope.currentIndex].label;
		}
		$scope.setActive(-1);
		computeTasksCounts();
	}
	$scope.goPast = function(){
		if($scope.dragData.taskId !=null)
			return;
		$scope.animateWeek = "left";
		$scope.animateTitle=true;
		$timeout(function(){
			$scope.animateWeek = null;
			$scope.animateTitle=false;
		},1000);
		$scope.currentIndex++;
		if($scope.currentIndex == weeks.length){
				var date = computeFirstDayOfDisplayedWeek();
				var dateTo = new Date(date.getTime() + 6*86400000);
				$http.get(host + "/rest/tasks/week/"+platform+"/" + date.getFullYear() +"/" + (date.getMonth()+1) +"/"+ date.getDate()).then(
				function(response){
					weeks.push({
						label: getTwoDigitString(date.getDate()) + "-"+ getTwoDigitString(date.getMonth()+1) + "-"+ date.getFullYear().toString().substr(2,2) + " <> " + getTwoDigitString(dateTo.getDate()) + "-"+ getTwoDigitString(dateTo.getMonth()+1) + "-"+ dateTo.getFullYear().toString().substr(2,2),
						tasks:response.data
					});
					$scope.viewedTasks = weeks[$scope.currentIndex];
					$scope.label = weeks[$scope.currentIndex].label;
					computeTasksCounts();
				},
				function(response){
					$scope.handleErrors(response);
				});
			}
		else{
			$scope.viewedTasks = weeks[$scope.currentIndex];
			$scope.label = weeks[$scope.currentIndex].label;
			computeTasksCounts();
		}
		$scope.setActive(-1);
	}
	
	$scope.checkTask = function(task){
		$scope.animatedTaskId = task.id;
		$timeout(function(){
				$scope.animatedTaskId = null;
				task.finished = !task.finished;
				$http.put(host+"/rest/tasks/"+platform+"/"+task.id,task).then(
			function(){},
			function(response){
				task.finished = !task.finished;
				var tmp = $scope.viewedTasks.tasks;
				$scope.viewedTasks.tasks = [];
				$timeout(function(){
					$scope.viewedTasks.tasks = tmp;
				},1);
				$scope.handleErrors(response);
			});
		},1000);	
	}
	
	$scope.createNewTask = function($event){
		if($scope.data.newTaskName == null || (!(/\S/.test($scope.data.newTaskName)))){
			$scope.data.newTaskName=null;
			return;
		}
		if($scope.roles.length == 0){
			$scope.data.newTaskName=null;
			alert("There are no roles. You must create some role first.");
			return;
		}
		var task = {
				firstDate:null,
				name:$scope.data.newTaskName,
				note:null,
				important: false,
				time:null,
				date: getParsedDate($scope.daysMenuActiveItemDate),
				finished: false
				}
		if(task.date != null)
			task.firstDate = angular.copy(task.date);
		if($scope.daysMenuActiveItem == -1 && $scope.currentIndex < 2){
			task.name = angular.copy($scope.data.newTaskName);
			$scope.data.newTaskName=null;
			//PLATFORM SPECIFIC
			if(platform=="m"){
			$state.go("task",({
				taskId: task.name,
				factoryIndex: null
				}));
			}
			else{
				taskModal.show().then(function(){
			    	$rootScope.$broadcast('viewTaskDetails',task,null,taskModal);
			    });
			}
			//
			return;
		}
		var stopListenHidden = $scope.$on('popover.hidden', function(){
			if($scope.data.newTaskRole != null){
				task.role = $scope.data.newTaskRole;
				$http.post(host+"/rest/tasks/"+platform,task).then(
					function(response){
						task.id = response.data.id;
						if(task.date == null)
							TasksAndRoles.addTask(task,-2);
						else{
							TasksAndRoles.addTask(task,$scope.currentIndex);
						}
						computeTasksCounts();
					},
					function(response){
						$scope.handleErrors(response);
					});
				$scope.data.newTaskRole = null;
			}
			$scope.data.newTaskName = null;
			stopListenHidden();
		});
		//if there is more then one role show popover, else just call hide function to trigger handler
		if($scope.roles.length > 1){
			$scope.popover.show($event);
		}
		else{
			$scope.data.newTaskRole = $scope.roles[0];
			$scope.popover.hide();
		}
	}
	
	var checkIfTaskCanBeCreated = function(){
		if($scope.currentIndex < 1 || $scope.daysMenuActiveItem == -2 || ($scope.daysMenuActiveItem == -1 && $scope.currentIndex==1)){
			$scope.data.inputReadOnly = false;
			return;
		}
		if($scope.currentIndex > 1){
			$scope.data.inputReadOnly = true;
			return;
		}
		var today = new Date();
		today.setHours(0, 0, 0, 0);
		if(today>$scope.daysMenuActiveItemDate)
			$scope.data.inputReadOnly = true;
		else
			$scope.data.inputReadOnly = false;
	}
	
	var computeDateByDaysMenuItem = function(itemIndex){
		//return null for backlog or week menu options (those tabs can't have date assigned)
		if(itemIndex == -2 || itemIndex == -1)
			return null;
		var date = new Date();
		var firstDayOfDisplayedWeek = computeFirstDayOfDisplayedWeek();
		date.setTime(firstDayOfDisplayedWeek.getTime() + itemIndex * 86400000);
		return date;
	}
	//computes the first day of the week according to index, return null for future tasks
	var computeFirstDayOfDisplayedWeek = function(){
		if($scope.currentIndex == -1)
			return null;
		return new Date($scope.firstDayOfCurrentWeekDate.getTime() - ($scope.currentIndex-1)*7 *86400000);
	}

	var getTwoDigitString = function(number){
	    return number > 9 ? "" + number: "0" + number;
	};
	
	var getParsedDate = function(date){
		if(!(Object.prototype.toString.call(date) === '[object Date]'))
			return null;
		return{
			year:date.getFullYear().toString(),
			month:getTwoDigitString(date.getMonth()+1),
			day:getTwoDigitString(date.getDate())
		};
	};
	
	$rootScope.$on('dataReLoaded', function () {
		setData();
		
	});
	var init = function(){
		$scope.currentIndex = 1;
		$scope.setActive(-1);
		setData();
	};
	init();
});