package main

import (
	"fmt"
	"github.com/spf13/viper"
	"os"
	"os/signal"
	"syscall"
	"time"
)

func init() {
	viper.AutomaticEnv()
}

var TOKEN string

var scrapeTargets = make([]string, 0)
var scrapeTargetsContextPath = make(map[string]string)
var configs = make(map[string]AppConfig)

func main() {
	fmt.Println("Starting OCP monitoring")

	handleSigterm(func() {
		fmt.Println("Shutting down...")
	})

	TOKEN = viper.GetString("OCP_AUTH_TOKEN")
	if TOKEN == "" {
		panic("Cannot start application, no OCP_AUTH_TOKEN env var configured")
	}

	parseScrapeTargetsFromEnv()

	go func() {
		for {
			pods, err := GetPods(viper.GetString("OCP_SERVER_URL"), viper.GetString("OCP_PROJECT_NAME"))
			if err != nil {
				fmt.Printf("Error loading pods: %v\n", err.Error())
			} else {
				processPodList(pods)
			}
			time.Sleep(time.Second * 15)
		}
	}()

	// Start web server, blocks...
	router := SetupGin()
	// Blocks here!
	router.Run(":8080")
}

// Handles Ctrl+C or most other means of "controlled" shutdown gracefully. Invokes the supplied func before exiting.
func handleSigterm(handleExit func()) {
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt)
	signal.Notify(c, syscall.SIGTERM)
	go func() {
		<-c
		handleExit()
		os.Exit(1)
	}()
}
