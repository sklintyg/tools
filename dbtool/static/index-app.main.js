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

function validateNewName(str) {
    const regex = /^[a-zA-Z0-9-.]{0,64}\.sql$/g;
    return regex.test(str);
}

angular.module('dbtoolIndexApp')
    .controller('IndexController', ['$scope', '$http', '$location', function($scope, $http, $location) {
        'use strict';

        $scope.versionMessage = '';
        $scope.statusMessage = '';
        $scope.isDisabled = false;

        $scope.currentlyEditing = '';
        $scope.form = {
            name: ''
        };

        var urlPrefix = '/dbtool';

        if ($location.host() === 'localhost') {
            urlPrefix = '';
        }

        $scope.rename = function(snapshot) {
            $scope.currentlyEditing = snapshot.name;
            $scope.form.name = snapshot.name;
        };

        $scope.savename = function() {

            // Name hasn't changed. Just cancel.
            if ($scope.currentlyEditing === $scope.form.name) {
                $scope.currentlyEditing = '';
                return;
            }

            // Validate
            if (!validateNewName($scope.form.name)) {
                alert("Namnet innehåller otillåtna tecken. Endast a-z A-Z 0-9 - och . är tillåtna. Filändelsen måste vara .sql");
                return;
            }

            for (var a = 0; a < $scope.snapshots.length; a++) {
                var snapshot = $scope.snapshots[a];
                if (snapshot.name === $scope.form.name) {
                    alert("Det finns redan en snapshot som heter " + $scope.form.name + ". Välj ett annat namn.");
                    return;
                }
            }

            // Do PUT to update name
            var putBody = {
                name: $scope.form.name
            };
            $scope.isDisabled = true;
            $http({
                method: 'PUT',
                url: urlPrefix + '/snapshot/' + $scope.currentlyEditing,
                data: putBody
            }).then(function successCallback(response) {
                $scope.listSnapshots();
                $scope.statusMessage = "Snapshot omdöpt!";
                $scope.isDisabled = false;
                $scope.currentlyEditing = '';
            });
        };

        $scope.abortNameChange = function() {
            $scope.currentlyEditing = '';
        };

        $scope.listSnapshots = function() {
            $http({
                method: 'GET',
                url: urlPrefix + '/snapshots'
            }).then(function successCallback(response) {
                $scope.snapshots = response.data;
                $scope.getVersion();
            });
        };

        $scope.restoreSnapshot = function(snapshot) {
            console.log("ENTER - restoreSnapshot");
            $scope.isDisabled = true;
            if (window.confirm('Är du verkligen helt säker på att du vill återställa databasen till denna snapshot?')) {
                $http({
                    method: 'GET',
                    url: urlPrefix + '/snapshot/' + snapshot.name
                }).then(function successCallback(response) {
                    $scope.listSnapshots();
                    $scope.statusMessage = "Databas återställd till " + snapshot.name;
                    $scope.isDisabled = false;
                });
            }
            $scope.isDisabled = false;

        };

        $scope.deleteSnapshot = function(snapshot) {
            if (window.confirm('Är du verkligen helt säker på att du vill radera denna snapshot?')) {
                $scope.isDisabled = true;
                $http({
                    method: 'DELETE',
                    url: urlPrefix + '/snapshot/' + snapshot.name
                }).then(function successCallback(response) {
                    $scope.listSnapshots();
                    $scope.statusMessage = "Snapshot raderad!";
                    $scope.isDisabled = false;
                });
            }
            $scope.isDisabled = false;
        };

        $scope.createSnapshot = function() {
            $scope.isDisabled = true;
            $http({
                method: 'POST',
                url: urlPrefix + '/snapshot'
            }).then(function successCallback(response) {
                $scope.listSnapshots();
                $scope.statusMessage = "Snapshot skapad!";
                $scope.isDisabled = false;
            });
            $scope.isDisabled = false;
        };

        $scope.getVersion = function() {
            $http({
                method: 'GET',
                url: urlPrefix + '/webcert/version'
            }).then(function successCallback(response) {
                $scope.versionMessage = response.data.version;
            });
        };

        $scope.format = function(dateStr) {
            var d = moment(dateStr, ["YYYY-MM-DD HH:mm:ss", "YYYY-MM-DD HH:mm:ss"]);
            return d.format("YYYY-MM-DD HH:mm:ss");
        }

        $scope.listSnapshots();

    }]);
