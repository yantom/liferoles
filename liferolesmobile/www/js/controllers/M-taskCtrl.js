//MOBILE PLATFORM
angular.module('liferolesApp').controller("taskCtrl",function($scope,$rootScope, $state, $http, $stateParams, TasksAndRoles){
	$scope.roles = TasksAndRoles.getRoles();
	$scope.$on('dataReLoaded', function () {
		$scope.roles = TasksAndRoles.getRoles();
	});
	if($stateParams.factoryIndex == null){
		$scope.task = {
				id:null,
				firstDate:null,
				name:$stateParams.taskId,
				note:null,
				important: false,
				time:null,
				date: null,
				finished: false
				};
	}
	else
		$scope.task = TasksAndRoles.getTaskCpyById($stateParams.taskId,$stateParams.factoryIndex);
	
	$scope.updateTask = function(){
		if($scope.task.name == null || (!(/\S/.test($scope.task.name))))
			return;
		if($scope.task.role == null){
			alert("you must choose role for your task");
			return;
		}
		if($scope.task.firstDate == null && $scope.task.date != null)
			$scope.task.firstDate = angular.copy($scope.task.date);
		if($stateParams.factoryIndex == null){
			$http.post(host+"/rest/tasks/"+platform,$scope.task).then(
				function(response){
					$scope.task.id = response.data.id;
					TasksAndRoles.addTask($scope.task,null);
					$rootScope.$broadcast('updateCounts');
					$state.go('tasks');
				},
				function(response){
					$rootScope.handleErrors(response);
					$state.go('tasks');
				});
		}
		else{
			$http.put(host+"/rest/tasks/"+platform+"/"+$scope.task.id,$scope.task).then(
				function(){
					TasksAndRoles.updateTask($scope.task,$stateParams.factoryIndex);
					$rootScope.$broadcast('updateCounts');
					$state.go('tasks');
				},
				function(response){
					$rootScope.handleErrors(response);
					$state.go('tasks');
				});
		}
	};
	
	$scope.deleteTask = function(){
		$http.delete(host+"/rest/tasks/"+platform+"/"+$scope.task.id).then(
			function(){
				TasksAndRoles.removeTaskById($scope.task.id,$stateParams.factoryIndex);
				$rootScope.$broadcast('updateCounts');
			},
			function(response){
					$rootScope.handleErrors(response);
			});
	}
});

angular.module('liferolesApp').controller('dateCtrl',function($scope,$cordovaDatePicker){
	$scope.clearTime = function(){
		$scope.task.time = null;
	};
	$scope.clearDate = function(){
		$scope.task.date = null;
	};
	 var dateOptions = {
			    date: new Date(),
			    mode: 'date',
			    allowOldDates: false,
			    allowFutureDates: true,
			  };
	 var timeOptions = {
			    date: new Date(),
			    mode: 'time',
			    allowOldDates: false,
			    allowFutureDates: true,
			    is24Hour: true
			  };
	 $scope.openTimePicker = function(){
		 $cordovaDatePicker.show(timeOptions).then(function(date){
			 $scope.task.time = {hours:getTwoDigitString(date.getHours()),minutes:getTwoDigitString(date.getMinutes())};
	    });
	 }
	 $scope.openDatePicker = function(){
		 $cordovaDatePicker.show(dateOptions).then(function(date){
	        $scope.task.date = {year:getTwoDigitString(date.getFullYear()),month:getTwoDigitString(date.getMonth()+1),day:getTwoDigitString(date.getDate())};
	    });
	 }
	 var getTwoDigitString = function(number){
		    return number > 9 ? "" + number: "0" + number;
	};
});