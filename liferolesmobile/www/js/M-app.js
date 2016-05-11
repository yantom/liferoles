//MOBILE PLATFORM
angular.module('liferolesApp', ['ngCordova','ionic','ngResource','nvd3','angular-jwt','ngDraggable']);
angular.module('liferolesApp').config(function($stateProvider, $urlRouterProvider,$ionicConfigProvider,$httpProvider, jwtInterceptorProvider) {
	$stateProvider.state('auth', {
        templateUrl : 'auth.html',
        controller: 'authCtrl'
    });
	$stateProvider.state('pm', {
        templateUrl : 'pm.html',
        controller : 'pmCtrl'
    });
	$stateProvider.state('stats', {
            templateUrl : 'stats.html',
            controller : 'statsCtrl'
        });
        $stateProvider.state('tasks', {
            templateUrl : 'tasks.html',
			controller : 'tasksCtrl'
        });
        $stateProvider.state('roles',{
        	templateUrl : 'roles.html',
        	controller : 'rolesCtrl'
        });
        $stateProvider.state('task', {
        	cache: false,
            templateUrl : 'task.html',
            controller : 'taskCtrl',
            params: {taskId:null,factoryIndex:null}
        });
        $stateProvider.state('role', {
        	cache:false,
            templateUrl : 'role.html',
            controller : 'roleCtrl',
            params: {roleId:null}
        });
        $stateProvider.state('info', {
            templateUrl : 'info.html',
			controller : 'infoCtrl'
        });
        $stateProvider.state('settings', {
            templateUrl : 'settings.html',
            controller : 'settingsCtrl'
        });
        $ionicConfigProvider.views.forwardCache(true);
		//$ionicConfigProvider.scrolling.jsScrolling(false);
        jwtInterceptorProvider.tokenGetter = function(){return localStorage.getItem('jwt');};
        $httpProvider.interceptors.push('jwtInterceptor');
});
angular.module('liferolesApp').run(function($rootScope,$http,$state,TasksAndRoles,$timeout,$ionicHistory,$ionicPlatform) {
    $rootScope.isAndroid = ionic.Platform.isAndroid();
	$rootScope.noInternet = false;
	$ionicPlatform.on("offline", function(){
		$rootScope.noInternet = true;
	});
	$ionicPlatform.on("online", function(){
		$rootScope.noInternet = false;
	});
    $rootScope.handleErrors = function(response){
		switch(response.status){
			case 403:
				localStorage.removeItem("jwt");
				$timeout(function(){
							$ionicHistory.clearHistory();
							$ionicHistory.clearCache();
							}, 500);
				$state.go("auth");
				break;
			case 500:
				alert("Error occured on the server side. Please try to refresh your tasks and roles by clicking refresh button in settings. If the problem persists, please report it to liferoles.app@gmail.com.");
				break;
			default:
				alert("Internet connection problem");
		}
	}
	if(localStorage.getItem("jwt") == null){
    	$state.go("auth");
    	return;
    }
    $http.get(host + '/rest/users/m').then(
	function(response){
    	$rootScope.user = response.data;
    	var date = new Date();
    	if((date.getDay()+6)%7 >= $rootScope.user.firstDayOfWeek)
    		date.setTime(date.getTime() - (((date.getDay()+6)%7)-$rootScope.user.firstDayOfWeek)*86400000);
    	else
    		date.setTime(date.getTime() - (((date.getDay()+6)%7)-$rootScope.user.firstDayOfWeek+7)*86400000);
    	date.setHours(0, 0, 0, 0);
    	$rootScope.firstDayOfCurrentWeekDate = date;
		TasksAndRoles.init(true);
    },
	function(response){
    	$rootScope.handleErrors(response);
    });
});

angular.module('liferolesApp').controller("rolesCtrl",function($scope,TasksAndRoles,$ionicSideMenuDelegate,$rootScope){
	$scope.roles = TasksAndRoles.getRoles();
	$rootScope.$on('dataReLoaded', function () {
		$scope.roles = TasksAndRoles.getRoles();
	});
	$scope.openMenu = function(){
		$ionicSideMenuDelegate.toggleLeft();
	}
	});

angular.module('liferolesApp').controller("infoCtrl",function($scope,$ionicSideMenuDelegate){
	$scope.openMenu = function(){
		$ionicSideMenuDelegate.toggleLeft();
	}
});
