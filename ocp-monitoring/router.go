package main

import (
	"github.com/gin-gonic/contrib/static"
	"github.com/gin-gonic/gin"
)

// SetupGin stats the HTTP server that serves both the files for the GUI as well as the REST API
func SetupGin() *gin.Engine {
	router := gin.Default()

	router.Use(static.Serve("/", static.LocalFile("./gui", true)))

	api := router.Group("/api")
	{
		api.GET("/config", GetConfig)
		api.GET("/counters/:app", GetCounter)
		api.GET("/status/:app", GetApplicationInfo)
	}

	return router
}
