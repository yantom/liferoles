var app = angular.module('liferolesAuth', ['ngCordova','ionic','ngResource']);

app.run(function($rootScope) {
    $rootScope.hostAddress = "https://localhost:8443/liferoles/rest/";
});

app.config(function($locationProvider){
	$locationProvider.html5Mode({
		  enabled: true,
		  requireBase: false
		});
})

app.controller("authCtrl",function($scope, $ionicPopup,$rootScope,$http,$location){
	
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
		
		checkIfUserExistsInDB($scope.data.email).then(function(result){
			if(result === false){
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
		function(err){
			alert(err);
		});
	};

	var checkIfUserExistsInDB = function(mail){
		return new Promise(function(resolve,reject){
			$http.get($rootScope.hostAddress + 'check/' + $scope.data.email).then(function(result){
				if(result.data.response !== undefined)
					resolve(result.data.response);
				else
					reject("Problem occurred on the server side, please report problem at: URL");
			},
			function(){
				reject("Internet connection problem.");
			})
		})
	}
	
	var checkIfEmailMatchSimpleRegex = function(){
		var re = /\S+@\S+\.\S+/;
		return re.test($scope.data.email);
	};
	
	var sendPasswordResetLink = function(mail){
		$http.post($rootScope.hostAddress + "reset/" + mail,{}).then(
		function(result){
			if (result.data.response === true)
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
		$http.post($rootScope.hostAddress + 'reg', user).then(function(dbResult){
			if(dbResult.data.id == null){
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
				checkIfUserExistsInDB($scope.data.email).then(
					function(result){
						if(result === true){
						sendPasswordResetLink($scope.data.email);
						$scope.data.email = null;}
						else{
							$scope.data.email = null;
							alert("No such user in database.");
						}
					},
					function(err){
						$scope.data.email = null;
						alert(err);
					}
				);			
				}
			}
			]
		};
	
	$scope.login = function(){
		if((checkIfEmailMatchSimpleRegex()) && ($scope.data.password.length >= 6)){
			document.getElementById("login").submit();
			return;
		}
		$scope.data.errMsg = "Wrong email or password.";
		$scope.data.password="";
		scope.data.email="";
	}
	
	$scope.showForgotPasswordPopup = function(){
		var popup = $ionicPopup.show(forgotPasswordPopupSpec);
	};
	
	
	angular.element(document).ready(function () {
		var re =  /.*j_security_check.*/;
		if(re.test($location.path())){
			$scope.data.errMsg = "Wrong email or password.";
			$scope.$apply();
		}
		
    });
});