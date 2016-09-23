/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//Register module
angular.module('dbtoolIndexApp', [

]);

angular.module('dbtoolIndexApp')
    .controller('IndexController', ['$scope', '$http', function($scope, $http) {
        'use strict';

        $scope.statusMessage = '';
        $scope.isDisabled = false;

        $scope.listSnapshots = function() {
            $http({
                method: 'GET',
                url: '/snapshots'
            }).then(function successCallback(response) {
                $scope.snapshots = response.data;
            });
        };

        $scope.restoreSnapshot = function(snapshot) {
            console.log("ENTER - restoreSnapshot");
            $scope.isDisabled = true;
            if (window.confirm('Är du verkligen helt säker på att du vill återställa databasen till denna snapshot?')) {
                $http({
                    method: 'GET',
                    url: '/snapshot/' + snapshot.name
                }).then(function successCallback(response) {
                    $scope.listSnapshots();
                    $scope.statusMessage = "Databas återställd till " + snapshot.name;
                    $scope.isDisabled = false;
                });
            }

        };

        $scope.deleteSnapshot = function(snapshot) {
            if (window.confirm('Är du verkligen helt säker på att du vill radera denna snapshot?')) {
                $scope.isDisabled = true;
                $http({
                    method: 'DELETE',
                    url: '/snapshot/' + snapshot.name
                }).then(function successCallback(response) {
                    $scope.listSnapshots();
                    $scope.statusMessage = "Snapshot raderad!";
                    $scope.isDisabled = false;
                });
            }
        };

        $scope.createSnapshot = function() {
            $scope.isDisabled = true;
            $http({
                method: 'POST',
                url: '/snapshot'
            }).then(function successCallback(response) {
                $scope.listSnapshots();
                $scope.statusMessage = "Snapshot skapad!";
                $scope.isDisabled = false;
            });
        };

        $scope.listSnapshots();

    }]);
