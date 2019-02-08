package main

import (
	"fmt"
	"github.com/Masterminds/semver"
	"github.com/gin-gonic/gin"
	"github.com/spf13/viper"
	"net/http"
	"strconv"
	"strings"
)

// GetVersions is the HTTP handler function that calls the OCP REST API and assembles the response with versions per app
func GetVersions(c *gin.Context) {

	results := make(map[string][]string)
	tgts := viper.GetString("OCP_IMAGE_STREAMS")
	if tgts == "" {
		panic("No OCP_IMAGE_STREAMS defined, nothing to query")
	}
	streams := strings.Split(tgts, ",")

	for _, stream := range streams {

		var verMap = make(map[string]int)
		versions, _ := GetTags(viper.GetString("OCP_SERVER_URL"), viper.GetString("OCP_PROJECT_NAME"), stream)

		for _, version := range versions {
			parseVersion(version, verMap)
		}

		// Append to results
		results[stream] = make([]string, 0)
		for k, v := range verMap {
			results[stream] = append(results[stream], k+"."+strconv.Itoa(v))
		}
	}

	c.JSON(http.StatusOK, results)
}

func parseVersion(version string, verMap map[string]int) error {

	// To parse into semver, remove the build number first...
	lastDot := strings.LastIndex(version, ".")
	if lastDot <= 0 {
		return fmt.Errorf("unable to parse " + version + " as semver")
	}
	semVer := version[:strings.LastIndex(version, ".")]

	parsed, err := semver.NewVersion(semVer)
	if err != nil {
		fmt.Printf("unable to parse %v as semver: %v\n", version, err.Error())
		return err
	}

	buildNr := version[strings.LastIndex(version, ".")+1:]
	buildNrNumeric, err := strconv.Atoi(buildNr)
	if err != nil {
		return fmt.Errorf("unable to parse build nr, not a proper intyg version")
	}

	// Check if semVer is in map
	if verMap[parsed.String()] == 0 {
		verMap[parsed.String()] = buildNrNumeric
	}

	// Update if build number is higher than the stored one
	if buildNrNumeric > verMap[parsed.String()] {
		verMap[parsed.String()] = buildNrNumeric
	}

	return nil
}
