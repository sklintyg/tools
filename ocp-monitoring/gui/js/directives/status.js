angular.module('MonitorDirectives')
.directive('monitorservices', ['d3Service', '$interval', '$http', '$compile', function(d3Service, $interval, $http, $compile) {
  return {
    restrict: 'E',
    scope: {
      serviceName: '@',
      alertsize: '@'
    },
    transclude: true,
    templateUrl: 'partials/services.html',
    link: function(scope, element, attrs) {

      scope.statusList = [];
      function fetchStatus() {
        $http.get('/api/status/' + scope.serviceName)
          .success(function(data, status, headers, config) {
            scope.servers = data;
            for (var i = 0; i < data.length; i++) {
              if (!data[i].reachable) continue;
              scope.subservices = data[i].statuses;
              scope.serviceversion = data[i].version;
              scope.checktime = data[i].timestamp;
              break;
            }
          })
          .error(function(data, status, headers, config) {
            console.log('Could not reach server for the status for service ' + scope.serviceName);
          });
      };
      fetchStatus();

      var timer = $interval(fetchStatus, 10000);
      scope.$on('$destroy', function() {
        if (timer) {
          $interval.cancel(timer);
        }
      });
    }
  }
}]);
