angular.module('liferolesApp').factory('TasksAndRoles',function($rootScope,$http,$state,$q){
	var backlog={
			label:"Backlog",
			tasks:[]
	};
	var futureTasks={
			label:"Future",
			tasks:[]
	}
	var rolesList;
	var weeksList = [
{
	label:"Next week",
	tasks:[]
},{
	label:"This week",
	tasks:[]
},{
	label:"Last week",
	tasks:[]
}];
	var init = function(first){
		var firstDayOfLastWeekDate = new Date($rootScope.firstDayOfCurrentWeekDate.getTime() - 7*86400000);
		$q.all([
			$http.get(host + '/rest/tasks/'+platform+'/' + firstDayOfLastWeekDate.getFullYear() + "/" + (firstDayOfLastWeekDate.getMonth()+1) + "/" + firstDayOfLastWeekDate.getDate()).then(
			function(response){
				weeksList[0].tasks =response.data[2];
				weeksList[1].tasks = response.data[3];
				weeksList[2].tasks = response.data[4];
				backlog.tasks=response.data[0];
				futureTasks.tasks=response.data[1];
			}),
			$http.get(host + '/rest/roles/'+platform).then(
			function(response){
				rolesList=response.data;
			})
		]).then(
		function(){
			$rootScope.$broadcast('dataReLoaded');
			if(first){
					if(rolesList.length == 0)
						$state.go("info");
					else
						$state.go("tasks");
			}
		},
		function(){
			$rootScope.handleErrors(response);
		});
	};
	var getWeeks = function() {
		 return weeksList;
	 };
	 var getBacklog = function(){
		 return backlog;
	 }
	 var getfutureTasks = function(){
		 return futureTasks;
	 }
	 
	//index 0 = weeks[0] = next week, index 1 = weeks[1] = this week.. -1 = future, -2 = backlog 
	 var getAffectedArrayByIndex = function(index){
		 switch(index){
			case -2 : 
				return backlog.tasks;
			case -1 :
				return futureTasks.tasks;
			default:
				return weeksList[index].tasks;
			}
	 }
	 
	 var getArrayIndexByTaskDate = function(taskDateObj){
		 if(taskDateObj == null)
			 return -2;
		 var date = new Date(taskDateObj.year,parseInt(taskDateObj.month)-1,taskDateObj.day);
		 var daysAfterFirstDayOfWeek = (date.getTime() - $rootScope.firstDayOfCurrentWeekDate.getTime())/86400000;
		 if (daysAfterFirstDayOfWeek > 13)
			 return -1;
		 if(daysAfterFirstDayOfWeek > 6)
			 return 0;
		 if(daysAfterFirstDayOfWeek > -1)
			 return 1;
		 return Math.abs(~~((daysAfterFirstDayOfWeek+1)/7))+2;
	 }
//
	var addTask = function(task,index) {
			if(index == null)
				getAffectedArrayByIndex(getArrayIndexByTaskDate(task.date)).push(task);
			else
				getAffectedArrayByIndex(index).push(task);
	};
	 
	 var removeTaskById = function(taskId,index) {
		 var affectedArray = getAffectedArrayByIndex(index);
		 for(var i = 0; i< affectedArray.length; i++){
			 if (affectedArray[i].id == taskId){
				 affectedArray.splice(i, 1);
				 break;
			 }
		 }
	 };
	 var updateTask = function(task,index){
		 var newArrayIndex = getArrayIndexByTaskDate(task.date);
		 var oldArray = getAffectedArrayByIndex(index);
		 if(newArrayIndex == index){
			 for(var i = 0; i< oldArray.length; i++){
				 if (oldArray[i].id == task.id){
					 oldArray[i] = task;
					 break;
				 }
			 }
		 }
		 else{
			 for(var i = 0; i< oldArray.length; i++){
				 if (oldArray[i].id == task.id){
					 oldArray.splice(i, 1);
					 break;
				 }
			 }
			 (getAffectedArrayByIndex(newArrayIndex)).push(task);
		 }
	 };
	 
	 
	 var getTaskCpyById = function(id,index){
		 var affectedArray = getAffectedArrayByIndex(index);
		 for(var i = 0; i< affectedArray.length; i++){
			 if (affectedArray[i].id == id){
				 return angular.copy(affectedArray[i]);
			 }
		 } 
	 };
	 
	 var moveTasksToBacklog = function(){
		 for(var j = 2;j<weeksList.length;j++){
			 for(var i = 0;i<(weeksList[j]).tasks.length;i++){
				 if(weeksList[j].tasks[i].finished == false){
					 weeksList[j].tasks[i].date = null;
					 weeksList[j].tasks[i].time = null;
					 backlog.tasks.push(weeksList[j].tasks[i]);
					 weeksList[j].tasks.splice(i, 1);
					 i--;
				 }
			 }
		 }
	 }
	 
	 var moveTasksUnderOtherRole = function(deletedRoleId,newRoleId){
		 var uns = getRoleById(newRoleId);
		 for(var i = 0; i< backlog.tasks.length; i++){
			 if (backlog.tasks[i].role.id == deletedRoleId){
				 backlog.tasks[i].role = uns;
			 }
		 }
		 for(var i = 0; i< futureTasks.tasks.length; i++){
			 if (futureTasks.tasks[i].role.id == deletedRoleId){
				 futureTasks.tasks[i].role = uns;
			 }
		 }
		 for(var j = 0; j<weeksList.length; j++){
			 for(var i = 0;i<(weeksList[j]).tasks.length;i++){
				 if (weeksList[j].tasks[i].role.id == deletedRoleId){
					 weeksList[j].tasks[i].role = uns;
			 	} 
		 	}
		 }
	 };
	 var deleteTasksByRoleId = function(roleId){
		 for(var i = 0; i< backlog.tasks.length; i++){
			 if (backlog.tasks[i].role.id == roleId){
				 backlog.tasks.splice(i, 1);
			 }
		 }
		 for(var i = 0; i< futureTasks.tasks.length; i++){
			 if (futureTasks.tasks[i].role.id == roleId){
				 futureTasks.tasks.splice(i, 1);
			 }
		 }
		 for(var j = 0; j<weeksList.length; j++){
			 for(var i = 0;i<(weeksList[j]).tasks.length;i++){
				 if (weeksList[j].tasks[i].role.id == roleId){
					 weeksList[j].tasks.splice(i, 1);
					 i--;
			 	} 
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
			 for(var i = 0; i< backlog.tasks.length; i++){
				 if (backlog.tasks[i].role.id == role.id){
					 backlog.tasks[i].role=role;
				 }
			 }
			 for(var i = 0; i< futureTasks.tasks.length; i++){
				 if (futureTasks.tasks[i].role.id == role.id){
					 futureTasks.tasks[i].role=role;
				 }
			 }
			 for(var j = 0; j<weeksList.length; j++){
				 for(var i = 0;i<(weeksList[j]).tasks.length;i++){
					 if (weeksList[j].tasks[i].role.id == role.id){
						 weeksList[j].tasks[i].role=role;
				 	} 
			 	}
			 }
		 };
		 var getRoleCpyById = function(id){
			 if(id == null)
				 return {
							name:null,
							goals:[],
							id:null,
							user:{id:null}
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
					 console.log(rolesList[i].name);
					 console.log(rolesList[i].id);
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
		 init:init,
		 getWeeks:getWeeks,
		 getBacklog:getBacklog,
		 getfutureTasks:getfutureTasks,
		 addTask:addTask,
		 removeTaskById:removeTaskById,
		 updateTask:updateTask,
		 getTaskCpyById:getTaskCpyById,
		 moveTasksUnderOtherRole:moveTasksUnderOtherRole,
		 moveTasksToBacklog:moveTasksToBacklog,
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