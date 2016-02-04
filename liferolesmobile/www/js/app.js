var app = angular.module('liferolesApp', ['ngCordova','ionic','ngResource']);

app.run(function($rootScope,$resource) {
    $rootScope.isAndroid = ionic.Platform.isAndroid();
    $rootScope.hostAddress = "https://localhost:8443/liferoles/rest/";
    $rootScope.user = $resource($rootScope.hostAddress + 'users/').get({userId:1});
});

app.config(function($stateProvider, $urlRouterProvider,$ionicConfigProvider) {
	$stateProvider.state('stats', {
        	url: '/stats',
            templateUrl : 'stats.html',
            controller : 'statsCtrl'
        });
        $stateProvider.state('tasks', {
        	url: '/tasks/',
            templateUrl : 'tasks.html',
			controller : 'tasksCtrl'
        });
        $stateProvider.state('roles',{
        	url: '/roles/',
        	templateUrl: 'roles.html',
        	controller : 'rolesCtrl'
        });
        $stateProvider.state('task', {
        	cache: false,
        	url: '/task',
            templateUrl : 'task.html',
            controller : 'taskCtrl',
            params: {taskId:null}
        });
        $stateProvider.state('role', {
        	cache:false,
        	url: '/role',
            templateUrl : 'role.html',
            controller : 'roleCtrl',
            params: {roleId:null}
        });
        $stateProvider.state('menu', {
        	url: '/menu',
            templateUrl : 'menu.html'
        });
        $stateProvider.state('pm', {
        	url: '/pm/',
            templateUrl : 'pm.html',
            controller : 'pmCtrl'
        });
        $stateProvider.state('info', {
        	url: '/info',
            templateUrl : 'info.html'
        });
		$stateProvider.state('auth', {
			url: '/auth',
            templateUrl : 'auth.html',
			controller : 'authCtrl',
        });
		$stateProvider.state('reg', {
			url: '/reg',
            templateUrl : 'reg.html',
			controller : 'authCtrl',
        });
        $urlRouterProvider.otherwise('/menu');
        $ionicConfigProvider.views.forwardCache(true);
});

app.filter('TasksDateFilter',function(){
	return function(tasks,date){
		var out = [];
		if(date == null){
			for(i = 0; i<tasks.length; i++){
				if(tasks[i].date == null){
					out.push(tasks[i]);
					}
			}
			return out;
		}
		
		if(!(Object.prototype.toString.call(date) === '[object Date]')){
			var taskDate = new Date();
			for(i = 0; i<tasks.length; i++){
				if(tasks[i].date == null)
					continue;
				taskDate.setFullYear(tasks[i].date.year,tasks[i].date.month-1,tasks[i].date.day);
				if(taskDate < date.dateTo && taskDate > date.dateFrom){
					out.push(tasks[i]);
				}
			}
			return out;
		}
		
		for(i = 0;i<tasks.length;i++){
			if(tasks[i].date == null)
				continue;
			if(date.getFullYear() == tasks[i].date.year && date.getMonth() == tasks[i].date.month-1 && date.getDate() == tasks[i].date.day)
				out.push(tasks[i]);
		}
		return out;
	}
});

app.filter('TasksRoleFilter',function(){
	return function(tasks,role){
		var out = [];
		
		if(role == null){
			return tasks;
		}
		
		for(i=0;i<tasks.length;i++){
			if(tasks[i].role.id == role.id)
				out.push(tasks[i]);
		}
		return out;
	}
});

app.filter('TasksOrder',function(){
	return function(tasks){
		tasks.sort(function(a, b) {
			if(a.finished == true && b.finished == false)
				return 1;
			if(a.finished == false && b.finished == true)
				return -1;
			if(a.date == null)
				return -1;
			else if(b.date == null)
				return 1;
			var res = parseInt(a.date.year+a.date.month+a.date.day) - parseInt(b.date.year+b.date.month+b.date.day);
			if(res != 0)
				return res;
			if(a.time == null)
				return -1;
			else if(b.time == null)
				return 1;
			return parseInt(a.time.hours+a.time.minutes) - parseInt(b.time.hours+b.time.minutes);
		})
		return tasks;
	}
});
app.factory('TaskFactory',function($resource,$rootScope){
	return $resource($rootScope.hostAddress + 'tasks/:userId',{},{'update':{method:'PUT'}});
});
app.factory('RoleFactory',function($resource,$rootScope){
	return $resource($rootScope.hostAddress + 'roles/:userId',{},{'update':{method:'PUT'}});
});
app.factory('UserFactory',function($resource,$rootScope){
	return $resource($rootScope.hostAddress + 'users/',{},{'update':{method:'PUT'}});
});
app.factory('AuthFactory',function($resource,$rootScope){
	return $resource($rootScope.hostAddress + 'auth/:userId',{},{'update':{method:'PUT'}});
});

app.factory('TasksAndRoles',function($resource,$rootScope,TaskFactory,RoleFactory){
	
	var tasksList = TaskFactory.query({userId:1},
			function(dbResult){
		if(dbResult != null)
			$rootScope.$broadcast('tasksLoaded');
		else
			alert("problem occured on the server side, please report problem at: URL");},
			function(){alert("internet connection problem");});
	
	var rolesList = RoleFactory.query({userId:1},
			function(dbResult){
		if(dbResult == null)
			alert("problem occured on the server side, please report problem at: URL");},
			function(){alert("internet connection problem");});	
	
	
	var getTasks = function() {
		 return tasksList;
	 };
	var addTask = function(task) {
	      tasksList.push(task);
	 };
	 var removeTaskById = function(taskId) {
		 for(var i = 0; i< tasksList.length; i++){
			 if (tasksList[i].id == taskId){
				 tasksList.splice(i, 1);
				 break;
			 }
		 }
	 };
	 var updateTask = function(task){
		 for(var i = 0; i< tasksList.length; i++){
			 if (tasksList[i].id == task.id){
				 tasksList[i] = task;
				 break;
			 }
		 }
	 };
	 var getTaskById = function(id){
		 for(var i = 0; i< tasksList.length; i++){
			 if (tasksList[i].id == id){
				 return tasksList[i];
			 }
		 } 
	 };
	 var getTaskCpyById = function(id){
		 for(var i = 0; i< tasksList.length; i++){
			 if (tasksList[i].id == id){
				 return angular.copy(tasksList[i]);
			 }
		 } 
	 };
	 var moveTasksUnderOtherRole = function(deletedRoleId,newRoleId){
		 var uns = getRoleById(newRoleId);
		 for(var i = 0; i< tasksList.length; i++){
			 if (tasksList[i].role.id == deletedRoleId){
				 tasksList[i].role = uns;
			 }
		 }
	 };
	 var deleteTasksByRoleId = function(roleId){
		 for(var i = 0; i< tasksList.length; i++){
			 if (tasksList[i].role.id == roleId){
				 tasksList.splice(i, 1);
			 }
		 }
	 }
	 
		var getRoles = function() {
			 return rolesList;
		 };
		var getRolesCount = function(){
			return rolesList.length;
		}
		var addRole = function(role) {
		      rolesList.push(role);
		 };
		 var removeRoleById = function(roleId) {
			 for(var i = 0; i< rolesList.length; i++){
				 if (rolesList[i].id == roleId){
					 rolesList.splice(i, 1);
					 break;
				 }
			 }
		 };
		 var updateRole = function(role){
			 for(var i = 0; i< rolesList.length; i++){
				 if (rolesList[i].id == role.id){
					 rolesList[i]=role;
					 break;
				 }
			 }
			 for(var i = 0; i<tasksList.length;i++){
				 if(tasksList[i].role.id == role.id){
					 tasksList[i].role=role;
				 }
			 }
		 };
		 var getRoleCpyById = function(id){
			 if(id == null)
				 return {
							name:null,
							roleGoal:null,
							id:null,
						}
			 for(var i = 0; i< rolesList.length; i++){
				 if (rolesList[i].id == id){
					 return angular.copy(rolesList[i]);
				 }
			 } 
		 };
		 var getRoleById = function(id){
			 for(var i = 0; i< rolesList.length; i++){
				 if (rolesList[i].id == id){
					 return rolesList[i];
				 }
			 } 
		 };
		 var getRoleByName = function(roleName){
			 for(var i = 0; i< rolesList.length; i++){
				 if (rolesList[i].name == roleName){
					 return rolesList[i];
				 }
			 }
		 };
	 return {
		 getTasks:getTasks,
		 addTask:addTask,
		 removeTaskById:removeTaskById,
		 updateTask:updateTask,
		 getTaskById:getTaskById,
		 getTaskCpyById:getTaskCpyById,
		 moveTasksUnderOtherRole:moveTasksUnderOtherRole,
		 deleteTasksByRoleId:deleteTasksByRoleId,
		 
		 getRoles:getRoles,
	    	getRolesCount:getRolesCount,
	    	addRole:addRole,
	    	removeRoleById:removeRoleById,
	    	updateRole:updateRole,
	    	getRoleCpyById:getRoleCpyById,
	    	getRoleById:getRoleById,
	    	getRoleByName:getRoleByName
	      }
});

app.controller("rolesCtrl",function($scope,TasksAndRoles){
	$scope.roles = TasksAndRoles.getRoles();
});

app.controller("statsCtrl",function($scope){
});

app.controller("pmCtrl",function($scope, $rootScope, TasksAndRoles){
	$scope.user = $rootScope.user;
	$scope.roles = TasksAndRoles.getRoles();
});

app.controller("tasksCtrl",function($scope, TasksAndRoles,$ionicSlideBoxDelegate,$ionicPopover,TaskFactory,$state){
	$scope.daysMenuActiveItem=0;
	$scope.daysMenuActiveItemDate =null;
	$scope.data={newTaskName:null,newTaskRole:null,inputReadOnly:false, filterRole:null};
	$scope.popover;
	$scope.tasks = TasksAndRoles.getTasks();
	$scope.roles = TasksAndRoles.getRoles();
	var popoverTemplate = "<ion-popover-view style='margin:0'><ion-content style='padding-top:20px; padding-bottom:20px;'><ion-list><ion-item ng-repeat='role in roles' ng-click='data.newTaskRole = role;popover.hide();'>{{role.name}}</ion-item></ion-list></ion-content></ion-popover-view>";
	$scope.popover = $ionicPopover.fromTemplate(popoverTemplate,
				{
			scope:$scope
				});
	$scope.$on('tasksLoaded',function(){$scope.setActive(0)});
	$scope.setActive = function(num){
		$scope.data.filterRole = null;
		$scope.daysMenuActiveItem = num;
		$scope.daysMenuActiveItemDate = computeDateByDaysMenuActiveItem();
		checkIfTaskCanBeCreated();
	}
	$scope.viewDetails = function(task){
		if(task.finshed == true)
			return;
		$state.go("task",({taskId: task.id}));
	}
	
	$scope.checkTask = function(task){
		TaskFactory.update(task,
				function(dbResult){
			if(!dbResult){
				task.finished = !task.finished;
				alert("problem occurred on the server side, please report problem at: URL");
				}
			},
			function(){
				task.finished = !task.finished;
				alert("internet connection problem");
				}
	);}
	
	$scope.createNewTask = function(){
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

		$scope.popover.show((document.getElementsByClassName("tasks-create"))[$ionicSlideBoxDelegate.currentIndex()]);

		var stopListenHidden = $scope.$on('popover.hidden', function(){
			if($scope.data.newTaskRole != null){
			task.role = $scope.data.newTaskRole;
			TaskFactory.save(task,
					function(dbResult){
				task.id = dbResult.id;
				if(task.id!=null){
					TasksAndRoles.addTask(task);
					if($scope.daysMenuActiveItem == 0)
						alert("The task was created in backlog.");
				}
				else
					alert("problem occured on the server side, please report problem at: URL");},
				function(){alert("internet connection problem");});
				$scope.data.newTaskName=null;
				}
			$scope.data.newTaskName = null;
			stopListenHidden();
		});
	$scope.$on('$destroy', function() {
    $scope.popover.remove();
	});
	}
	
	var checkIfTaskCanBeCreated = function(){
		var todayNumber = ((((new Date()).getDay()+6)%7)+1);
		if(($ionicSlideBoxDelegate.currentIndex() == 0) && ($scope.daysMenuActiveItem > 0 && $scope.daysMenuActiveItem < todayNumber)){
			$scope.data.inputReadOnly = true;
		}
		else{
			$scope.data.inputReadOnly=false;
		}
	}
	
	var computeDateByDaysMenuActiveItem = function(){
		if($ionicSlideBoxDelegate.currentIndex() == 2)
			return null;
		var date = new Date();
		var mondayOfPlanedWeek = computeMondayOfTheWeek();
		switch ($scope.daysMenuActiveItem) {
		case 1:
			date.setTime(mondayOfPlanedWeek.getTime());
			return date;
		case 2:
			date.setTime(mondayOfPlanedWeek.getTime() + 86400000);
			return date;
		case 3:
			date.setTime(mondayOfPlanedWeek.getTime() + 2 * 86400000);
			return date;
		case 4:
			date.setTime(mondayOfPlanedWeek.getTime() + 3 * 86400000);
			return date;
		case 5:
			date.setTime(mondayOfPlanedWeek.getTime() + 4 * 86400000);
			return date;
		case 6:
			date.setTime(mondayOfPlanedWeek.getTime() + 5 * 86400000);
			return date;
		case 7:
			date.setTime(mondayOfPlanedWeek.getTime() + 6 * 86400000);
			return date;
		case 0:
			date.setTime(mondayOfPlanedWeek.getTime());
			dateFrom = new Date(date.setTime(mondayOfPlanedWeek.getTime()));
			dateTo = new Date(date.setTime(mondayOfPlanedWeek.getTime() + 6 * 86400000 + 86399999));
			var interval = {dateFrom:dateFrom,dateTo:dateTo};
			return interval;
		default:
			return null;
		}
	}
	
	var computeMondayOfTheWeek = function(){
		var date = new Date();
		switch ($ionicSlideBoxDelegate.currentIndex()) {
		case 0:
			date.setTime(date.getTime() - ((date.getDay()+6)%7) * 86400000);
			date.setHours(0,0,0,0);
			return date;
		case 1:
			date.setTime(date.getTime() + (7 - (date.getDay()+6)%7) * 86400000);
			date.setHours(0,0,0,0);
			return date;
		default:
			return null;
		}
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
	
});

app.controller("taskCtrl",function($scope, $stateParams, TasksAndRoles, TaskFactory){
	$scope.roles = TasksAndRoles.getRoles();
	$scope.task = TasksAndRoles.getTaskCpyById($stateParams.taskId);
	
	$scope.updateTask = function(){
		if($scope.task.firstDate == null && $scope.task.date != null)
			$scope.task.firstDate = angular.copy($scope.task.date);
		TaskFactory.update($scope.task,
				function(dbResult){
			if(dbResult.response)
				TasksAndRoles.updateTask($scope.task);
			else
				alert("problem occured on the server side, please report problem at: URL");
			},
			function(){alert("internet connection problem");});
	};
	
	$scope.deleteTask = function(){
		TaskFactory.remove({taskId: $scope.task.id},
			function(dbResult){
			if(dbResult.response)
				TasksAndRoles.removeTaskById($scope.task.id);
			else
				alert("problem occured on the server side, please report problem at: URL");
			},
			function(){alert("internet connection problem");});
		}
});

app.controller("roleCtrl",function($scope, $stateParams,TasksAndRoles, $ionicPopup, $state,$rootScope,$rootScope, RoleFactory, $ionicPopover){
	$scope.role = TasksAndRoles.getRoleCpyById($stateParams.roleId);
	$scope.roles = TasksAndRoles.getRoles();
	$scope.data = {newRoleId:null}
	var popoverTemplate = "<ion-popover-view style='margin:0'><ion-content style='padding-top:20px; padding-bottom:20px;'><ion-list><ion-item ng-repeat='rolefromlist in roles' ng-if='rolefromlist.id != role.id' ng-click='data.newRoleId = rolefromlist.id;popover.hide();'>{{rolefromlist.name}}</ion-item></ion-list></ion-content></ion-popover-view>";
	$scope.popover = $ionicPopover.fromTemplate(popoverTemplate,{scope:$scope});
	$scope.$on('$destroy', function() {
		$scope.popover.remove();
	});
	
	$scope.updateRole = function(){
		if($stateParams.roleId != null){
			RoleFactory.update($scope.role,
				function(dbResult){
			if(dbResult.response)
				TasksAndRoles.updateRole($scope.role);
			else
				alert("problem occured on the server side, please report problem at: URL");
			},
			function(){alert("internet connection problem");});
		}
		else{
			$scope.role.user = {
				id : $rootScope.user.id
			}
			RoleFactory.save($scope.role,
					function(dbResult){
				$scope.role.id = dbResult.id;
				if($scope.role.id!=null){
					TasksAndRoles.addRole($scope.role);
				}
				else
					alert("problem occured on the server side, please report problem at: URL");},
					function(){alert("internet connection problem");});
		}
	};
	
	$scope.deleteRole = function(){
		if($scope.role.id == null)
			$state.go("roles");
		else{
		$ionicPopup.show({
		    title: 'Role ' + $scope.role.name  +' will be deleted',
		    subTitle: 'What to do with underlying tasks?',
		    scope: $scope,
		    buttons: [
		      { text: "Move under other role",
		    	onTap: function(event){
					$scope.popover.show(event);

		var stopListenHidden = $scope.$on('popover.hidden', function(){
			if($scope.data.newRoleId != null){
				RoleFactory.remove({roleId: $scope.role.id, newRoleId: $scope.data.newRoleId},
				function(dbResult){
							if(dbResult.response){
								TasksAndRoles.moveTasksUnderOtherRole($scope.role.id,$scope.data.newRoleId);
							TasksAndRoles.removeRoleById($scope.role.id);
							$scope.data.newRoleId = null;
							}
							else
								alert("problem occured on the server side, please report problem at: URL");
							$scope.data.newRoleId = null;
						},
						function(){alert("internet connection problem");
						$scope.data.newRoleId = null;
						});
						$state.go("roles");
			}
			stopListenHidden();
		});
		    		
		    	}  
		      },
		      {
		        text: 'Delete',
		        onTap: function() {
					RoleFactory.remove({roleId: $scope.role.id},
						function(dbResult){
							if(dbResult.response){
								TasksAndRoles.deleteTasksByRoleId($scope.role.id);
							TasksAndRoles.removeRoleById($scope.role.id);}
							else
								alert("problem occurred on the server side, please report problem at: URL");
						},
						function(){alert("internet connection problem");});
						$state.go("roles");
				}
		        },
		      { text: "Cancel" }
		    ]
		});
		}
	};

});


app.controller('dateCtrl',function($scope,$cordovaDatePicker,$rootScope){
	$scope.clearTime = function(){
		$scope.task.time = null;
	};
	$scope.clearDate = function(){
		$scope.task.date = null;
		$scope.clearTime();
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
	 if($rootScope.isAndroid){
		 timeOptions['minDate']=Date.parse(new Date());
		 dateOptions['minDate']=Date.parse(new Date());
	 }else{
		 timeOptions['minDate']=new Date();
		 dateOptions['minDate']=new Date();
	 }
	 $scope.openTimePicker = function(){
		 $cordovaDatePicker.show(timeOptions).then(function(date){
			 $scope.task.time = {hours:date.getHours().toString(),minutes:date.getMinutes().toString()};
	    });
	 }
	 $scope.openDatePicker = function(){
		 $cordovaDatePicker.show(dateOptions).then(function(date){
	        $scope.task.date = {year:date.getFullYear().toString(),month:(date.getMonth()+1).toString(),day:date.getDate().toString()};
	        $scope.task.time = null;
	    });
	 }
});

app.controller("authCtrl",function($scope, UserFactory, AuthFactory, $ionicPopup){
	$scope.data = {email:"",emailcheck:"",password:"",passwordcheck:"",errMsg:"",passwordStrong:true,language:"EN"};
	$scope.authActiveItem = 0;
	
	$scope.checkPasswordComplexity = function(){
		
		var lowercaseRegex = /.*[a-z].*/;
		var uppercaseRegex = /.*[A-Z].*/;
		var specialCharRegex = /.*[^a-zA-Z0-9].*/;
		var numberRegex = /.*\d.*/;
		if($scope.data.password.length < 8)
			return false;
		if(!specialCharRegex.test($scope.data.password))
			return false;
		if(!numberRegex.test($scope.data.password))
			return false;
		if(!uppercaseRegex.test($scope.data.password))
			return false;
		if(!lowercaseRegex.test($scope.data.password))
			return false;
		return true;
	};
	
	var resetData = function(){
		$scope.data = {email:"",emailcheck:"",password:"",passwordcheck:"",errMsg:"",passwordStrong:true,language:"EN"};
	};
	
	$scope.setActive = function(num){
		resetData();
		$scope.authActiveItem = num;
	};
	
	$scope.register= function(){
		$scope.data.errMsg = "";
		if(!checkIfEmailMatchSimpleRegex()){
			$scope.data.errMsg="Invalid email format.";
			return;
		}
		if($scope.data.password.length < 6){
			$scope.data.errMsg="Password must be at least 6 characters long.";
			return;
		}
		if($scope.data.password != $scope.data.passwordcheck){
			$scope.data.errMsg="Passwords don't match.";
			return;
		}
		if($scope.data.email != $scope.data.emailcheck){
			$scope.data.errMsg="Emails don't match.";
			return;
		}
		
		getUserByMail($scope.data.email).then(function(result){
			if(result == null){
				createNewUser({
					language:$scope.data.language,
					email:$scope.data.email,
					password:$scope.data.password
				});
			}
			else{
				alert("User with this email already exists in database.");
			}
		},
		function(){
			alert("Problem occurred on the server side, please report problem at: URL");
		});
	};
	var clearString = function(string){
		string="";
	}
	var getUserByMail = function(mail){
		return new Promise(function(resolve,reject){
			UserFactory.get({userMail:mail},
			function(result){
			if (result.id === undefined)
				resolve(null);
			else
				resolve(result);
		},
		function(){
		reject("Internet connection problem.");
		}
		);});
	};
	
	var checkIfEmailMatchSimpleRegex = function(){
		var re = /\S+@\S+\.\S+/;
		return re.test($scope.data.email);
	};
	
	var sendPasswordResetLink = function(mail,id){
		AuthFactory.save({userId:id,userMail:mail},{},
		function(result){
			if (result.response == true)
				alert("Password reset link was send to your email.");
			else
				alert("Problem occurred on the server side, please report problem at: URL");
		},
		function(){
			alert("Internet connection problem.");
		}
		);
	}
	
	var createNewUser = function(user){
		UserFactory.save(user,function(dbResult){
			if(dbResult.id == null){
				$scope.data.errMsg="problem occurred on the server side, please report problem at: URL";
			}
			else{
				alert("You have been registered, now you can login.");
				$scope.setActive(0);
				
		}},function(){$scope.data.errMsg = "internet connection problem";});
	}
	
	var forgotPasswordPopupSpec = {
			template: '<input type="email" ng-model="data.email">',
			title: 'Enter your email',
			scope: $scope,
	  
			buttons: [
			{text: 'Cancel',
			onTap: function(e){$scope.data.email = null;}
			},
			{text: 'OK',
				onTap: function(e) {
				if (!checkIfEmailMatchSimpleRegex($scope.data.email)){
					alert("Invalid email format.");
					e.preventDefault();
					return;
				}
				getUserByMail($scope.data.email).then(
					function(result){
						if(result!=null){
						sendPasswordResetLink($scope.data.email,result.id);
						$scope.data.email = null;}
						else{
							$scope.data.email = null;
							alert("No such user in database.");
						}
					},
					function(errMessage){
						$scope.data.email = null;
						alert(errMessage);
					}
				);			
				}
			}
			]
		};
	
	$scope.showForgotPasswordPopup = function(){
		var popup = $ionicPopup.show(forgotPasswordPopupSpec);
	};
	
	
});

function textAreaAdjust(o) {
    o.style.height = "0px";
    o.style.height = o.scrollHeight+"px";
};