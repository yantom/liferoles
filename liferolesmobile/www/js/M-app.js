//MOBILE PLATFORM
angular.module('liferolesApp', ['ngCordova','ionic','ngResource','nvd3','angular-jwt','ngDraggable']);
angular.module('liferolesApp').config(function($stateProvider, $urlRouterProvider,$ionicConfigProvider,$httpProvider, jwtInterceptorProvider) {
	$stateProvider.state('menu', {
    	url: '/menu',
        templateUrl : 'menu.html'
    });
	$stateProvider.state('auth', {
    	url: '/auth',
        templateUrl : 'auth.html',
        controller: 'authCtrl'
    });
	$stateProvider.state('stats', {
        	url: '/stats',
            templateUrl : 'stats.html',
            controller : 'statsCtrl'
        });
        $stateProvider.state('tasks', {
        	url: '/tasks',
            templateUrl : 'tasks.html',
			controller : 'tasksCtrl'
        });
        $stateProvider.state('roles',{
        	url: '/roles',
        	templateUrl: 'roles.html',
        	controller : 'rolesCtrl'
        });
        $stateProvider.state('task', {
        	cache: false,
        	url: '/task',
            templateUrl : 'task.html',
            controller : 'taskCtrl',
            params: {taskId:null,factoryIndex:null}
        });
        $stateProvider.state('role', {
        	cache:false,
        	url: '/role',
            templateUrl : 'role.html',
            controller : 'roleCtrl',
            params: {roleId:null}
        });
        $stateProvider.state('info', {
        	url: '/info',
            templateUrl : 'info.html',
			controller : 'infoCtrl'
        });
        $stateProvider.state('settings', {
        	url: '/settings',
            templateUrl : 'settings.html',
            controller : 'settingsCtrl'
        });
        $ionicConfigProvider.views.forwardCache(true);
        jwtInterceptorProvider.tokenGetter = function(){return localStorage.getItem('jwt');};
        $httpProvider.interceptors.push('jwtInterceptor');
});
angular.module('liferolesApp').run(function($rootScope,$http,$state,TasksAndRoles,$timeout,$ionicHistory) {
    $rootScope.isAndroid = ionic.Platform.isAndroid();
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
				alert("Error on server side, please report error at liferoles.app@gmail.com");
				break;
			case 409:
				alert("Data conflict. It looks like you are trying to access data which you already deleted on another device or in another browser session. Refresh your data by refresh button in Tasks view to get actual data.");
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

angular.module('liferolesApp').controller("rolesCtrl",function($scope,TasksAndRoles,$ionicSideMenuDelegate){
	$scope.roles = TasksAndRoles.getRoles();
	$scope.$on('dataReLoaded', function () {
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
