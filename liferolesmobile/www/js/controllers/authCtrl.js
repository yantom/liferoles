//FILE IS SHARED BETWEEN PLATFORMS
angular.module(authCtrlModule).controller("authCtrl",function($scope,$injector,$http,$state,$location,$ionicPopover,$rootScope){
	if ($injector.has('TasksAndRoles'))
		var TasksAndRoles = $injector.get('TasksAndRoles');
	if ($injector.has('vcRecaptchaService'))
		var vcRecaptchaService = $injector.get('vcRecaptchaService');
	$scope.data = {email:"",password:"",passwordcheck:"",errMsg:""};
	$scope.authActiveItem = 0;
	$scope.resetData = {sendingMail:false,errMsg:"",mailSent:false};
	var resetData = function(){
		$scope.data = {email:"",password:"",passwordcheck:"",errMsg:""};
	};
	$scope.setActive = function(num){
		resetData();
		$scope.authActiveItem = num;
	};
	$scope.sendResetMail = function(){
		if (!checkIfEmailMatchSimpleRegex($scope.data.email)){
			$scope.resetData.errMsg = "Wrong email format.";
			return;
		}
		$http.get(host+ '/rest/auth/check/' + $scope.data.email).then(function(response){
			if(response.data.response === false){
				$scope.resetData.errMsg = "No such user in database.";;
				return;
			}
			else{
				$scope.resetData.errMsg = "";
				$scope.resetData.sendingMail = true;
				sendPasswordResetCode($scope.data.email);
			}
		},
		function(response){
			$scope.resetData.errMsg = "";
			$scope.handleErrors(response);
		});
	}
	$scope.register= function(){
		$scope.data.errMsg = "";
		if(!checkIfEmailMatchSimpleRegex($scope.data.email)){
			$scope.data.errMsg="Invalid email format.";
			return;
		}
		if($scope.data.password.length < 8){
			$scope.data.errMsg="Password must be at least 8 characters long.";
			return;
		}
		if(!((/.*\d.*/).test($scope.data.password)) || !((/.*[a-zA-Z].*/).test($scope.data.password))){
			$scope.data.errMsg="Password must contain at least one digit and at least one alphabetical character.";
			return;
		};
		if($scope.data.password != $scope.data.passwordcheck){
			$scope.data.errMsg="Passwords don't match.";
			return;
		}
		if(platform == "web"){
			if(vcRecaptchaService.getResponse() === ""){
				$scope.data.errMsg="You must verify that you are not a robot.";
				return;
			}
		}
		$http.get(host+ '/rest/auth/check/' + $scope.data.email).then(function(response){
			if(response.data.response === false){
				createNewUser({
					email:$scope.data.email,
					password:$scope.data.password,
					firstDayOfWeek:0
				});
			}
			else{
				$scope.data.errMsg="User with this email already exists in database.";
			}
		},
		function(response){
			$scope.data.errMsg="";
			$scope.handleErrors(response);
		});
	};
	var checkIfEmailMatchSimpleRegex = function(input){
		var re = /\S+@\S+\.\S+/;
		return re.test(input);
	};
	var sendPasswordResetCode = function(mail){
		$http.post(host + "/rest/auth/getResetCode/" + mail,{}).then(
		function(){
			$scope.resetData.sendingMail=false;
			$scope.resetData.mailSent=true;
		},
		function(response){
			$scope.resetData.sendingMail=false;
			$scope.handleErrors(response);
		});
	}
	var createNewUser = function(user){
		var url;
		if(platform == "m")
			url = host + '/rest/auth/m/reg';
		else
			url = host + '/rest/auth/web/reg?captcha=' + vcRecaptchaService.getResponse();
		$http.post(url, user).then(function(response){
			alert($scope.data.email + " registred, now you can login");
			$scope.setActive(0);
		},function(response){
			$scope.handleErrors(response);});
	}
	var resetPopover1;
	$ionicPopover.fromTemplateUrl('resetPopover1.html',{scope:$scope}).then(function(popover) {
		resetPopover1 = popover;
	});
	$scope.$on('popover.hidden', function() {
		$scope.resetData = {sendingMail:false,errMsg:"",mailSent:false};
	  });
	$scope.showResetPopover1 = function(){
		$scope.data.errMsg="";
		resetPopover1.show(document.getElementById("logo"));
	}
	//PLATFORM SPECIFIC
	if(platform=="m"){
		$scope.login = function(){
			if((!checkIfEmailMatchSimpleRegex($scope.data.email)) || ($scope.data.password.length < 6)){
				$scope.data.errMsg="Wrong email or password";
				$scope.data.password="";
				$scope.data.email="";
				return;
			}
			$http({ 
				url: host + '/rest/auth/m/login',
				skipAuthorization: true,
				method: 'POST',
				data: {
					email:$scope.data.email,
					password:$scope.data.password
				}
			}).then(function(response){
				if(response.data == ""){
					$scope.data.errMsg="Wrong email or password";
					$scope.data.password="";
					$scope.data.email="";
					return;
				}
					$rootScope.user = response.data.user;
					localStorage.setItem("jwt", response.data.token);
					var date = new Date();
			    	if((date.getDay()+6)%7 >= $scope.user.firstDayOfWeek)
			    		date.setTime(date.getTime() - (((date.getDay()+6)%7)-$scope.user.firstDayOfWeek)*86400000);
			    	else
			    		date.setTime(date.getTime() - (((date.getDay()+6)%7)-$scope.user.firstDayOfWeek+7)*86400000);
			    	date.setHours(0, 0, 0, 0);
			    	$rootScope.firstDayOfCurrentWeekDate = date;
			    	TasksAndRoles.init(true);
			    	resetData();
			},function(response){
				$scope.data.errMsg="";
				$scope.handleErrors(response);});
		}
	}
	else{
		//login
		$scope.login = function(){
			if((checkIfEmailMatchSimpleRegex($scope.data.email)) && ($scope.data.password.length >= 6)){
				document.getElementById("login").submit();
				return;
			}
			$scope.data.errMsg = "Wrong email or password.";
			$scope.data.password="";
			$scope.data.email="";
		}
		//reset link
		var userId=null;
		var userEmail;
		var resetCode=null;
		var resetPopover2;
		$ionicPopover.fromTemplateUrl('resetPopover2.html',{scope:$scope}).then(function(popover) {
			resetPopover2 = popover;
		});
		var showResetPopover2 = function(){
			$scope.data.password="";
			$scope.data.passwordcheck="";
			resetPopover2.show(document.getElementById("logo"));
		}
		$scope.reset = function(){
		if(userId == null || resetCode == null || userEmail==null){
			$scope.resetData.errMsg="Link is either wrong or expired. Please repeat reset procedure.";
			return;
		}
		if($scope.data.password.length < 8){
			$scope.resetData.errMsg="Password must be at least 8 characters long.";
			return;
		}
		if(!((/.*\d.*/).test($scope.data.password)) || !((/.*[a-zA-Z].*/).test($scope.data.password))){
			$scope.resetData.errMsg="Password must contain at least one digit and at least one alphabetical character.";
			return;
		};
		if($scope.data.password != $scope.data.passwordcheck){
			$scope.resetData.errMsg="Passwords don't match.";
			return;
		}
		var userWithToken = {user:{id:userId,email:userEmail,password:$scope.data.password},token:resetCode};
		$http.post(host + "/rest/auth/reset",userWithToken).then(
				function(){
					alert("Your password has been changed, you can now log in with new password.");
					$scope.resetData.errMsg="";
					resetPopover2.hide();
				},
				function(response){
					$scope.resetData.errMsg="";
					$scope.handleErrors(response);
				});
	}
		//url checker
		angular.element(document).ready(function () {
			$scope.mobileWeb = false;
			(function(a){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4)))$scope.mobileWeb= true})(navigator.userAgent||navigator.vendor||window.opera);
			if((/.*j_security_check.*/).test($location.path())){
				$location.path('/');
				$scope.data.errMsg = "Wrong email or password.";
			}
			var params = $location.search();
			if(params.reset === true){
				userEmail = params.user;
				userId = params.u;
				resetCode = params.c;
				showResetPopover2();
			}
			//$location.path('/auth.html');
			$scope.$apply();
	    });
	}
	//
});