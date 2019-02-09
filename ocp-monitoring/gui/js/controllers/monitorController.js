angular.module('MonitorControllers')
.controller('MonitorController', ['$scope', '$http', '$location', '$interval',
  function($scope, $http, $location, $interval) {
    // Set the maximum amount of failed retries
    $scope.threshold = 10;
    // Set the time between retries
    var retryTime = 30000;

    // $scope.webcert = {
    //   doneLoading: false,
    //   fail: 0,
    //   name: 'webcert-demo'
    // };
    // $scope.minaintyg = {
    //   doneLoading: false,
    //   fail: 0,
    //   name: 'minaintyg-demo'
    // };
    // $scope.rehabstod = {
    //   doneLoading: false,
    //   fail: 0,
    //   name: 'rehabstod-demo'
    // };
    // $scope.intygstjanst = {
    //   doneLoading: false,
    //   fail: 0,
    //   name: 'intygstjanst-demo'
    // };
    // $scope.privatlakarportal = {
    //   doneLoading: false,
    //   fail: 0,
    //   name: 'privatlakarportal-demo'
    // };
    // $scope.statistik = {
    //   doneLoading: false,
    //   fail: 0,
    //   name: 'statistik-demo'
    // };


    // $scope.webcert.timer = $interval(function() {
    //   checkIfDone($scope.webcert)
    // }, retryTime);
    // $scope.minaintyg.timer = $interval(function() {
    //   checkIfDone($scope.minaintyg)
    // }, retryTime);
    // $scope.rehabstod.timer = $interval(function() {
    //   checkIfDone($scope.rehabstod)
    // }, retryTime);
    // $scope.intygstjanst.timer = $interval(function() {
    //   checkIfDone($scope.intygstjanst)
    // }, retryTime);
    // $scope.privatlakarportal.timer = $interval(function() {
    //   checkIfDone($scope.privatlakarportal)
    // }, retryTime);
    // $scope.statistik.timer = $interval(function() {
    //   checkIfDone($scope.statistik)
    // }, retryTime);

    // checkIfDone($scope.webcert);
    // checkIfDone($scope.minaintyg);
    // checkIfDone($scope.rehabstod);
    // checkIfDone($scope.intygstjanst);
    // checkIfDone($scope.privatlakarportal);
    // checkIfDone($scope.statistik);

    function loadConfig() {
        $http.get('/api/config')
            .success(function(data) {
                $scope.configs = data;
                angular.forEach($scope.configs, function(config) {

                    config.doneLoading = false;
                    config.fail = 0;
                    config.timer = $interval(function() {
                        checkIfDone(config)
                    }, retryTime);

                    checkIfDone(config);
                });


            })
    }

    function checkIfDone(service) {
      $http.get('/api/counters/' + service.id)
        .success(function(data) {
          if (data.length != 0) {
            service.doneLoading = true;
            $interval.cancel(service.timer);
          }
          else {
            service.fail++;
            if (service.fail > $scope.threshold) {
              $interval.cancel(service.timer);
            }
          }
        })
      .error(function() {
        service.fail++;
        if (service.fail > $scope.threshold) {
          $interval.cancel(service.timer);
        }
      });
    }

    $scope.$on('$destroy', function() {
      if (timer) {
        $interval.cancel(timer);
      }
      angular.forEach($scope.configs, function(config) {
          if (config.timer) {
              $interval.cancel(config.timer);
          }
      });
    });

    loadConfig();
}]);
