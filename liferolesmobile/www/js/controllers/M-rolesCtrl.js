angular.module('liferolesApp').controller("rolesCtrl",function($scope,TasksAndRoles,$ionicSideMenuDelegate,$rootScope){
	$scope.roles = TasksAndRoles.getRoles();
	$rootScope.$on('dataReLoaded', function () {
		$scope.roles = TasksAndRoles.getRoles();
	});
	$scope.openMenu = function(){
		$ionicSideMenuDelegate.toggleLeft();
	}
	});