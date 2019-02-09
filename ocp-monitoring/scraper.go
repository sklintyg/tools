package main

import (
	"fmt"
	"github.com/spf13/viper"
	"net/http"
	"strings"
	"time"
)

// counters is global storage for user counts per application
var counters = make(map[string][]UserCount)

// appStatuses is global storage for individual metrics per application
var appStatuses = make(map[string][]ApplicationData)

// parseScrapeTargetsFromEnv reads scrape targets and labels from env vars and transforms them into the app config
func parseScrapeTargetsFromEnv() {
	tgts := viper.GetString("OCP_SCRAPE_TARGETS")
	labels := viper.GetString("OCP_SCRAPE_TARGETS_LABELS")
	parseLabels(labels)
	if tgts == "" {
		panic("No scrape targets configured, have you set environment variable OCP_SCRAPE_TARGETS with a comma-separated list of scrape targets?")
	}

	items := strings.Split(tgts, ",")
	for _, item := range items {
		scrapeTargets = append(scrapeTargets, strings.ToLower(item))

		// Check for label, if not present use tgt name as label
		if _, ok := configs[item]; !ok {
			configs[item] = AppConfig{Id: item, Name: item}
			fmt.Printf("Warn, no label was configured in OCP_SCRAPE_TARGETS_LABELS for target %v\n", item)
		}
	}

	// Extra context paths per tgt
	contextPaths := viper.GetString("OCP_SCRAPE_TARGETS_CONTEXT_PATH")

	if contextPaths != "" {
		fmt.Printf("OCP_SCRAPE_TARGETS_CONTEXT_PATH was specified with value %v\n", contextPaths)
		parts := strings.Split(contextPaths, ",")
		for _, part := range parts {
			if !strings.Contains(part, "=") {
				panic("Invalid OCP_SCRAPE_TARGETS_CONTEXT_PATH entry, must be of key=value format.")
			}
			sep := strings.Index(part, "=")
			key := part[:sep]
			val := part[sep+1:]
			if !strings.HasPrefix(val, "/") {
				panic("Invalid context-path entry, must begin with a forward slash")
			}
			scrapeTargetsContextPath[key] = val
			fmt.Printf("Registered context path %v for app %v\n", val, key)
		}
	}
}

func parseLabels(labels string) {
	if labels == "" {
		return
	}
	labelArr := strings.Split(labels, ",")
	for _, labelStr := range labelArr {
		if !strings.Contains(labelStr, "=") {
			panic("Invalid label, no = detected in OCP_SCRAPE_TARGETS_LABELS variable")
		}
		sep := strings.Index(labelStr, "=")
		key := labelStr[:sep]
		val := labelStr[sep+1:]
		configs[key] = AppConfig{Id: key, Name: val}
		fmt.Printf("Registered application label %v for %v\n", val, key)
	}
}

// processPodList scrapes all pods /metrics endpoint and updates the data structures holding instance info
func processPodList(items []PodItem) {

	// Reset appStatuses
	appStatuses = make(map[string][]ApplicationData)

	for _, podItem := range items {
		var app = podItem.Metadata.Labels.App
		if contains(app) {
			// This is a scrape target.
			fmt.Printf("Adding data for podItem: %v\n", app)

			// First, check if we have initialized a counter and appInfo entry
			if counters[app] == nil {
				userCounts := make([]UserCount, 0)
				counters[app] = userCounts
			}

			if appStatuses[app] == nil {
				appData := make([]ApplicationData, 0)
				appStatuses[app] = appData
			}

			// Note that we assume all services are running on port 8080 here...
			url := "http://" + podItem.Status.PodIP + ":8080" + scrapeTargetsContextPath[app] + "/metrics"
			fmt.Println("Scraping target: " + url)
			resp, err := http.Get(url)
			if err != nil {
				fmt.Printf("Call to scrape metrics at %v failed, message: %v\n", url, err.Error())
				break
			}
			if resp.StatusCode == 200 {
				metrics := parseMetrics(resp.Body)

				// AppData
				appData := ApplicationData{Server: podItem.Status.PodIP, Application: app, Reachable: true, Responsetime: 100, Timestamp: time.Now().Format("2006-01-02 15:04"), Version: "version-xx", Statuses: metrics}
				appStatuses[app] = append(appStatuses[app], appData)

				// UserCount
				addedUserCount := false
				for _, v := range metrics {
					// Ugh, hard-coded check for "logged_in_users"
					if strings.HasPrefix(v.ServiceName, "logged_in_users") {
						addedUserCount = addUserCount(v.Severity, podItem, app)
						break
					}
				}
				if !addedUserCount {
					addUserCount(0, podItem, app)
				}
				fmt.Printf("Successfully scraped %v for app %v\n", url, app)
			} else {
				fmt.Printf("Call to scrape metrics at %v failed with HTTP status %v\n", url, resp.Status)
			}
		}
	}
}

func addUserCount(number int, podItem PodItem, app string) bool {
	// Note the use of the weird date formatting Go uses...
	userCount := UserCount{TimeStamp: time.Now().Format("2006-01-02 15:04"), Count: number, Server: podItem.Status.PodIP}

	counters[app] = append(counters[app], userCount)

	// Store a maxiumum of 1024 data points per service
	if len(counters[app]) > 1024 {
		// Delete oldest using ugly go trick of creating a new one every time...
		// Delete the 512 oldest entries so this shit doesn't have to be performed every tick.
		tmp := make([]UserCount, len(counters[app])-512)
		copy(tmp, counters[app][512:])
		counters[app] = tmp
	}
	return true
}

func contains(tgt string) bool {
	for _, scrapeTgt := range scrapeTargets {
		if tgt == scrapeTgt {
			return true
		}
	}
	return false
}
