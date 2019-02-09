package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"net/http"
	"strconv"
)

// GetCount is the HTTP handler that returns time series data for the specified "app"
func GetCounter(c *gin.Context) {
	app := c.Param("app")
	requestedNumberOfEntries, _ := strconv.Atoi(c.Query("requestedNumberOfEntries"))
	if requestedNumberOfEntries == 0 {
		requestedNumberOfEntries = 360
	}

	userCount := counters[app]
	if userCount == nil {
		handleErr(fmt.Errorf("no data for requested entry"), c)
		return
	}

	total := len(userCount)
	if total == 0 {
		c.JSON(http.StatusOK, []UserCount{})
		return
	}

	if total < requestedNumberOfEntries {
		requestedNumberOfEntries = total
	}
	startIndex := total - requestedNumberOfEntries
	c.JSON(http.StatusOK, userCount[startIndex:])
}

// GetApplicationInfo is the HTTP handler for returning Application statuses for the specified "app"
func GetApplicationInfo(c *gin.Context) {
	app := c.Param("app")
	statuses := appStatuses[app]
	if statuses == nil {
		handleErr(fmt.Errorf("no data for requested entry"), c)
		return
	}
	c.JSON(http.StatusOK, statuses)
}

// GetConfig returns the global config, i.e. what our env vars have specified
func GetConfig(c *gin.Context) {
	c.JSON(http.StatusOK, configs)
}

func handleErr(err error, c *gin.Context) bool {
	if err != nil {
		fmt.Printf("Error serving request: %v\n", err.Error())
		c.JSON(http.StatusInternalServerError, gin.H{
			"message": err.Error(),
		})
		return true
	}
	return false
}
