package main

import (
	"fmt"
	"io"
	"sort"
	"strconv"
	"strings"
)
import "github.com/prometheus/common/expfmt"

func parseMetrics(metrics io.Reader) []AppStatus {

	parser := expfmt.TextParser{}
	families, err := parser.TextToMetricFamilies(metrics)
	if err != nil {
		fmt.Printf("Error parsing metrics: %v\n", err.Error())
	}

	statuses := make([]AppStatus, 0)

	for k, v := range families {
		if strings.HasPrefix(k, "health_") {

			appStatus := AppStatus{}
			appStatus.ServiceName = formatServiceName(k)

			val := *v.GetMetric()[0].Gauge.Value
			appStatus.Severity = int(val)
			appStatus.Statuscode = strconv.Itoa(int(val))

			if strings.HasSuffix(k, "_normal") {
				appStatus.Type = "NORMAL"
			} else {
				appStatus.Type = "VALUE"
			}

			statuses = append(statuses, appStatus)
		}
	}
	sort.Slice(statuses, func(i, j int) bool {
		return statuses[i].ServiceName < statuses[j].ServiceName
	})
	return statuses
}

// formatServiceName makes sure the name of a health metric is prettied
func formatServiceName(name string) string {
	name = strings.TrimPrefix(name, "health_")
	name = strings.TrimSuffix(name, "_value")
	name = strings.TrimSuffix(name, "_normal")
	return name
}
