angular.module('liferolesApp').controller("infoCtrl",function($scope,$ionicSideMenuDelegate){
	$scope.openMenu = function(){
		$ionicSideMenuDelegate.toggleLeft();
	}
});