angular.module('MonitorDirectives')
.directive('chart', ['d3Service', '$interval', '$http', function(d3Service, $interval, $http) {
  return {
    restrict: 'E',
    scope: {
      chartname: '@',
      chartwidth: '@',
      chartheight: '@'
    },
    link: function(scope, element, attrs) {

      // Once d3 is loaded we can start drawing the chart
      d3Service.d3().then(function(d3) {
        var margin = {top: 10, right: 20, bottom: 20, left: 50},
        width = (scope.chartwidth ? scope.chartwidth : 600) - margin.left - margin.right,
        height = (scope.chartheight ? scope.chartheight :  200) - margin.top - margin.bottom;

        var parseDate = d3.time.format("%Y-%m-%d %H:%M").parse;

        var x = d3.time.scale()
          .range([0, width]);

        var y = d3.scale.linear()
          .range([height, 0]);

        var color_picker = d3.scale.category20c();

        var xAxis = d3.svg.axis()
          .scale(x)
          .ticks(5)
          .orient("bottom")
          .tickFormat(d3.time.format("%H:%M"));

        var yAxis = d3.svg.axis()
          .scale(y)
          .orient("left");

        var stack = d3.layout.stack()
          .offset("zero")
          .values(function(d) { return d.values; })
          .x(function(d) { return d.timeStamp; })
          .y(function(d) { return d.count; });

        var nest = d3.nest()
          .key(function(d) { return d.server; });

        var area = d3.svg.area()
          .x(function(d) { return x(d.timeStamp); })
          .y0(function(d) { return y(d.y0); })
          .y1(function(d) { return y(d.y0 + d.y); });

        // We create a div with the id of the chartname to refer to this
        // specific chart in case of several charts on page
        element.html("<div id='" + scope.chartname + "'></div>");

        var svg = d3.select("#" + scope.chartname).append("svg")
          .attr("width", width + margin.left + margin.right)
          .attr("height", height + margin.top + margin.bottom)
          .append("g")
          .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        function init(data) {

          data.forEach(function(d) {
            d.timeStamp = parseDate(d.timeStamp);
            d.count = d.count;
          });

          var layers = stack(nest.entries(data));
          x.domain(d3.extent(data, function(d) { return d.timeStamp; }));
          y.domain([0, d3.max(data, function(d) { return d.y + d.y0; })]);

          svg.selectAll(".layer")
            .data(layers)
            .enter().append("path")
            .attr("class", "layer")
            .attr("d", function(d) { return area(d.values); })
            .style("fill", function(d, i) { return color_picker(i); });

          svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "rotate(90)")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis);

          svg.append("g")
            .attr("class", "y axis")
            .call(yAxis);

          svg.append("g").append("text")
            .attr("class", "headline")
            .attr("text-anchor", "middle")
            .attr("transform", "translate("+(width/2 - 15)+"," + (height/2 - 15) + ")")
            .text(function(d) {

              return layers.map(function(val) {
                return val.values[0].count;
              }).reduce(function(prev, curr) {
                return curr;
              }, 0);
            });

          var timer = $interval(function() {fetchData(updateChart);}, 10000);
          scope.$on('$destroy', function() {
            if (timer) {
              $interval.cancel(timer);
            }
          });
        }

        // Updates the data in the chart
        function updateChart(data) {
          if (data.length == 0)
                return;

          data.forEach(function(d) {
            d.timeStamp = parseDate(d.timeStamp);
            d.count = d.count;
          });
          var layers = stack(nest.entries(data));
          x.domain(d3.extent(data, function(d) { return d.timeStamp; }));
          y.domain([0, d3.max(data, function(d) { return d.y0 + d.y; })]);

          var svg = d3.select("#" + scope.chartname);

          svg.select(".headline")
            .transition()
            .duration(750)
            .text(function(d) {
              return layers.map(function(val) {
                return val.values[0].count;
              }).reduce(function(prev, curr) {
                return curr;
              }, 0);
            });
          svg
            .selectAll("path")
            .data(layers)
            .transition()
            .duration(750)
            .attr("d", function(d) { return area(d.values); })
            .style("fill", function(d, i) { return color_picker(i); });
          svg.select(".x.axis")
            .transition()
            .duration(750)
            .call(xAxis);
          svg.select(".y.axis")
            .transition()
            .duration(750)
            .call(yAxis);
        }
        function fetchData(func) {
          $http.get('/api/counters/' + scope.chartname + '?entries=360').
            success(function(data, status, headers, config) {
              func(data);
            }).
          error(function(data, status, headers, config) {
            console.log('Could not fetch the number of logged in users from the server for service ' + scope.chartname);
          });
        }
        fetchData(init);
      });
    }
  }
}]);
