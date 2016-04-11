//FILE SHARED BETWEEN PLATFORMS
angular.module('liferolesApp').controller("settingsCtrl",function($scope,$http,$ionicPopover,$ionicPopup,TasksAndRoles,$window,$ionicSideMenuDelegate){
	$scope.passwdData = {password:"",newPassword:"",newPassword2:"",errMsg:"",editingEmail:false}
	$scope.emailData = {password:"",newEmail:"",errMsg:"",editingPassword :false}
	$scope.viewData = {email:$scope.user.email,newFirstDay:null}
	$scope.popover;
	$ionicPopover.fromTemplateUrl('daysPopover.html',{scope:$scope}).then(function(popover) {
	    $scope.popover = popover;
	  });
	$scope.hideEmailEdit = function(){
		$scope.emailData = {password:"",newEmail:"",errMsg:"",editingPassword :false}
	}
	$scope.hidePasswdEdit = function(){
		$scope.passwdData = {password:"",newPassword:"",newPassword2:"",errMsg:"",editingEmail:false}
	}
	//PLATFORM SPECIFIC
	if(platform=="m"){
		$scope.logout = function(){
			localStorage.removeItem("jwt");
			$timeout(function(){
				$ionicHistory.clearHistory();
				$ionicHistory.clearCache();
				}, 500);
			$state.go("auth");
		};
	}
	else{
		var areYouSurePopupSpec = {
				title: 'Are you sure that you want logout all your mobile devices?',
				scope: $scope,
				buttons: [
				{text: 'OK',
				onTap: function(){
					$http.put(host +  "/rest/users/web/blocktokens",{}).then(
					function(){},
					function(response){
						$scope.handleErrors(response);
					});
				}
				},
				{text: 'Cancel'}
				]
			};
		$scope.blockTokens = function(){
			$ionicPopup.show(areYouSurePopupSpec);
		};
		$scope.logout = function(){
			$http.get(host + "/rest/users/web/logout").then(function(){
				$window.location.href = host;
			},
			function(response){
				$scope.handleErrors(response);
			});
		};
	}
	//
	$scope.changeEmail = function(){
		if(!((/\S+@\S+\.\S+/).test($scope.emailData.newEmail))){
			$scope.emailData.errMsg="Wrong email format.";
			return
		}
		if($scope.emailData.password.length <8){
			$scope.emailData.errMsg="Wrong password";
			return
		}
		if (!(confirm("Please confirm your new email:\n"+$scope.emailData.newEmail)))
			return;
		$http.post(host + "/rest/users/"+platform+"/checkPassword",{password:$scope.emailData.password}).then(
			function(response){
				if (response.data.response == true){
					$http.put(host + "/rest/users/"+platform,{email:$scope.emailData.newEmail}).then(
					function(){
						$scope.user.email = $scope.emailData.newEmail;
						$scope.viewData.email = $scope.emailData.newEmail;
						$scope.hideEmailEdit();
					},
					function(response){
						$scope.handleErrors(response);
						$scope.hideEmailEdit();
					});
				}
				else{
					$scope.emailData.errMsg="Wrong password";
				}
			},
			function(response){
					$scope.handleErrors(response);
					$scope.hideEmailEdit();
			});		
	}
	
	$scope.changePassword = function(){
		if($scope.passwdData.newPassword != $scope.passwdData.newPassword2){
			$scope.passwdData.errMsg="Passwords dont match.";
			return
		}
		if($scope.passwdData.newPassword.length <8){
			$scope.passwdData.errMsg="New password must be at least 8 characters long.";
			return
		}
		if(!((/.*\d.*/).test($scope.passwdData.newPassword))){
			$scope.passwdData.errMsg="New password must contain at least one digit.";
			return;
		};
		if($scope.passwdData.password.length <8){
			$scope.passwdData.errMsg="Wrong password";
			return
		}
		$http.post(host + "/rest/users/"+platform+"/checkPassword",{password:$scope.passwdData.password}).then(
			function(response){
				if(response.data.response == true){
					$http.put(host + "/rest/users/"+platform,{password:$scope.passwdData.newPassword}).then(
					function(){
						$scope.hidePasswdEdit();
					},
					function(response){
						$scope.handleErrors(response);
						$scope.hidePasswdEdit();
					});
				}
				else{
					$scope.passwdData.errMsg="Wrong password";
				}
			},
			function(response){
				$scope.handleErrors(response);
				$scope.hidePasswdEdit();
			});
	}
	
	$scope.changeFirstDay = function(){
		$scope.popover.show(document.getElementById("popover-beam"));
		var stopListenHidden = $scope.$on('popover.hidden', function(){
			if($scope.viewData.newFirstDay != null && $scope.viewData.newFirstDay != $scope.user.firstDayOfWeek){
				$http.put(host + "/rest/users/"+platform,{firstDayOfWeek:$scope.viewData.newFirstDay}).then(
				function(){
					$scope.user.firstDayOfWeek = $scope.viewData.newFirstDay;
					var date = new Date();
			    	if((date.getDay()+6)%7 >= $scope.user.firstDayOfWeek)
			    		date.setTime(date.getTime() - (((date.getDay()+6)%7)-$scope.user.firstDayOfWeek)*86400000);
			    	else
			    		date.setTime(date.getTime() - (((date.getDay()+6)%7)-$scope.user.firstDayOfWeek+7)*86400000);
			    	date.setHours(0, 0, 0, 0);
			    	$scope.firstDayOfCurrentWeekDate = date;
					TasksAndRoles.init();
					$scope.viewData.newFirstDay = null;
				},
				function(response){
					$scope.handleErrors(response);
					$scope.viewData.newFirstDay = null;
				});
			}
			stopListenHidden();
		});
	}
	
	$scope.leaveSettings = function(){
		$scope.passwdData = {password:"",newPassword:"",newPassword2:"",errMsg:"",editingEmail:false};
		$scope.emailData = {password:"",newEmail:"",errMsg:"",editingPassword :false};
	}
	$scope.openMenu = function(){
		$ionicSideMenuDelegate.toggleLeft();
	}
});