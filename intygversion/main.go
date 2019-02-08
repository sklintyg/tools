package main

import (
	"fmt"
	"github.com/spf13/viper"
	"os"
	"os/signal"
	"syscall"
)

var TOKEN string

func init() {
	viper.AutomaticEnv()
}

func main() {
	fmt.Println("Starting OCP monitoring")

	handleSigterm(func() {
		fmt.Println("Shutting down...")
	})

	TOKEN = viper.GetString("OCP_AUTH_TOKEN")
	if TOKEN == "" {
		panic("Cannot start application, no OCP_AUTH_TOKEN env var configured")
	}

	// Start web server, blocks...
	SetupGin()
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
