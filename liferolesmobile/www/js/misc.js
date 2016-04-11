//FILE IS SHARED BETWEEN PLATFORMS
angular.module('liferolesApp').filter('TasksDateFilter',function(){
	return function(tasks,date,indexOfActiveItem,arrayWithCounts){
		if(date == null){
			arrayWithCounts[indexOfActiveItem+2].count = tasks.length;
			return tasks;
		}
		var out = [];
		for(i = 0;i<tasks.length;i++){
			if(date.getDate() == tasks[i].date.day)
				out.push(tasks[i]);
		}
		arrayWithCounts[indexOfActiveItem+2].count = out.length;
		return out;
	}
});
angular.module('liferolesApp').filter('TasksRoleFilter',function(){
	return function(tasks,role){
		var out = [];
		if(role == null){
			return tasks;
		}
		for(i=0;i<tasks.length;i++){
			if(tasks[i].role.id == role.id)
				out.push(tasks[i]);
		}
		return out;
	}
});
angular.module("liferolesApp").directive('autofocus', ['$timeout', function($timeout) {
  return {
    restrict: 'A',
    link : function($scope, $element) {
      $timeout(function() {
        $element[0].focus();
      });
    }
  }
}]);
function textAreaAdjust(o) {
    o.style.height = "0px";
    o.style.height = o.scrollHeight+"px";
};