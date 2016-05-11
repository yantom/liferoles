angular.module('liferolesApp').controller("pmCtrl",function($scope,$http,$timeout,$ionicSideMenuDelegate){
	if(platform == 'm'){
		$scope.openMenu = function(){
		$ionicSideMenuDelegate.toggleLeft();
	}
	}
	$scope.oldPM;
	$scope.$on('$ionicView.enter', function() {
		textAreaAdjust(document.getElementById("txtarea"));
		$scope.oldPM = $scope.user.personalMission;
	});
	$scope.$on('$ionicView.leave', function() {
		if($scope.oldPM != $scope.user.personalMission)
			$scope.update();
	});
	$scope.update= function(){
		$http.put(host + "/rest/users/"+platform,$scope.user).then(
			function(){
			},
			function(response){
				$scope.user.personalMission = $scope.oldPM;
				$scope.handleErrors(response);
				$scope.viewData.newFirstDay = null;
			});
	};
});