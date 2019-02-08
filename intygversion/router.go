package main

import (
	"github.com/gin-gonic/contrib/static"
	"github.com/gin-gonic/gin"
)

// SetupGin starts the GIN http router/muxer at port 8080.
func SetupGin() {
	router := gin.Default()

	// Not used, can serve static files on / from a future SPA in the /gui folder
	router.Use(static.Serve("/", static.LocalFile("./gui", true)))

	// Defines our single /api/versions endpoint
	api := router.Group("/api")
	{
		api.GET("/versions", GetVersions)
	}

	// Blocks here!
	router.Run(":8080")
}
