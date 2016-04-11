//FILE SHARED BETWEEN PLATFORMS
angular.module('liferolesApp').controller("roleCtrl",function($scope,$rootScope,$http, $stateParams,TasksAndRoles, $ionicPopup, $state, $ionicPopover){
	$scope.roles = TasksAndRoles.getRoles();
	$scope.$on('dataReLoaded', function () {
		$scope.roles = TasksAndRoles.getRoles();
	});
	$scope.data = {newRoleId:null}
	$scope.popover;
	$ionicPopover.fromTemplateUrl('rolesPopover2.html',{scope:$scope}).then(function(popover) {
	    $scope.popover = popover;
	  });
	$scope.$on('$destroy', function() {
		$scope.popover.remove();
	});
	//PLATFORM SPECIFIC
	if(platform == "m"){
		$scope.role = TasksAndRoles.getRoleCpyById($stateParams.roleId);
	}
	else{
		$scope.role;
		var roleModal;
		$scope.$on('viewRoleDetails', function (event,role,modal) {
			$scope.role=role;
			roleModal=modal;
		});
		$scope.cancel = function(){
			roleModal.hide();
		}
	}
	//
	$scope.updateRole = function(){
		if($scope.role.name == null || (/\s/.test($scope.role.name)))
			return;
		if($stateParams.roleId != null){
			$http.put(host+"/rest/roles/"+platform,$scope.role).then(
				function(){
					TasksAndRoles.updateRole($scope.role);
					if(platform=="m")
						$state.go("roles");
					else
						roleModal.hide();
				},
				function(response){
					$scope.handleErrors(response);
					if(platform=="m")
						$state.go("roles");
					else
						roleModal.hide();
				});
		}
		else{
			$scope.role.user = {id : $rootScope.user.id};
			$http.post(host+"/rest/roles/"+platform,$scope.role).then(
				function(response){
					$scope.role.id = response.data.id;
					TasksAndRoles.addRole($scope.role);
					if(platform=="m")
						$state.go("roles");
					else
						roleModal.hide();
				},
				function(response){
					$rootScope.handleErrors(response);
					if(platform=="m")
						$state.go("roles");
					else
						roleModal.hide();
				}
			);
		}
	};
	$scope.deleteRole = function(){
		if($scope.role.id == null){
			if(platform=="m")
				$state.go("roles");
			else
				roleModal.hide();
		}
		else{
			var popup = {
		    title: 'Role ' + $scope.role.name  +' will be deleted',
		    subTitle: 'What to do with underlying tasks?',
		    scope: $scope,
		    buttons: [
				{
					text: "Move under other role",
					onTap: function(event){
						$scope.popover.show(document.getElementById("popover-beam"));
						var stopListenHidden = $scope.$on('popover.hidden', function(){
							if($scope.data.newRoleId != null){
								$http.delete(host+"/rest/roles/"+platform+"?roleId="+$scope.role.id+"&newRoleId="+$scope.data.newRoleId).then(
									function(){
										TasksAndRoles.moveTasksUnderOtherRole($scope.role.id,$scope.data.newRoleId);
										TasksAndRoles.removeRoleById($scope.role.id);
										$scope.data.newRoleId = null;
										if(platform=="m")
											$state.go("roles");
										else
											roleModal.hide();
									},
									function(response){
										$rootScope.handleErrors(response);
										if(platform=="m")
											$state.go("roles");
										else
											roleModal.hide();
									});
								$scope.data.newRoleId = null;
							}
							stopListenHidden();
						});
					}
				},
				{
					text: 'Delete',
					onTap: function() {
						$http.delete(host+"/rest/roles/"+platform+"?roleId="+$scope.role.id).then(
							function(){
								TasksAndRoles.deleteTasksByRoleId($scope.role.id);
								TasksAndRoles.removeRoleById($scope.role.id);
								if(platform=="m")
									$state.go("roles");
								else
									roleModal.hide();
							},
							function(response){
								$rootScope.handleErrors(response);
								if(platform=="m")
									$state.go("roles");
								else
									roleModal.hide();
							}
						);
					}
		        },
		      { text: "Cancel" }
		    ]
		}
			$ionicPopup.show(popup);
		}
	};
});