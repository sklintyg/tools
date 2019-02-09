package main

import (
	"encoding/json"
	"fmt"
	"github.com/spf13/viper"
	"gopkg.in/h2non/gock.v1"
	"testing"
)
import . "github.com/smartystreets/goconvey/convey"

var podStr = `{
  "metadata": {
    "labels": {
      "app": "myapp"
    }
  },
  "status": {
    "podIP": "10.1.2.3"
  }
}`

func TestTargetParser(t *testing.T) {

	Convey("Given env vars", t, func() {
		viper.Set("OCP_SCRAPE_TARGETS", "intygstjanst-demo,webcert-demo")
		viper.Set("OCP_SCRAPE_TARGETS_CONTEXT_PATH", "intygstjanst-demo=/inera-certificate")

		Convey("When parsed", func() {
			parseScrapeTargetsFromEnv()
			Convey("Then context paths should have been populated", func() {
				So(len(scrapeTargetsContextPath), ShouldEqual, 1)
				So(scrapeTargetsContextPath["intygstjanst-demo"], ShouldEqual, "/inera-certificate")
			})
		})
	})
}

func TestScraper(t *testing.T) {

	defer gock.Off()
	gock.New("http://10.1.2.3:8080").
		Get("/metrics").
		Reply(200).
		BodyString(metrics)

	scrapeTargets = append(scrapeTargets, "myapp")

	Convey("Given", t, func() {
		podList := make([]PodItem, 0)

		podItem := &PodItem{}
		err := json.Unmarshal([]byte(podStr), &podItem)
		if err != nil {
			fmt.Printf("Cannot create PodItem from test JSON:  %v\n", err.Error())
		}

		podList = append(podList, *podItem)

		Convey("When", func() {

			processPodList(podList)

			Convey("Then", func() {
				applicationData := appStatuses["myapp"]
				userCount := counters["myapp"]
				So(applicationData, ShouldNotBeNil)
				So(userCount, ShouldNotBeNil)
				So(len(userCount), ShouldEqual, 1)
				So(userCount[0].Count, ShouldEqual, 5)
			})
		})
	})
}
