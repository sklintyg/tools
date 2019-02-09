package main

import (
	"encoding/json"
	"github.com/spf13/viper"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"
)

import . "github.com/smartystreets/goconvey/convey"

var expectedJSON = `{"rehabstod-demo":{"id":"rehabstod-demo","name":"Rehabstöd"},"webcert-demo":{"id":"webcert-demo","name":"Webcert"}}`

func TestGetCounter(t *testing.T) {

	testdata := make([]UserCount, 0)
	for a := 0; a < 1000; a++ {
		testdata = append(testdata, UserCount{Count: a, Server: "localhost", TimeStamp: time.Now().Format("2006-01-02 15:04")})
	}
	counters["webcert-demo"] = testdata
	counters["empty-demo"] = make([]UserCount, 0)
	router := SetupGin()
	Convey("Given that the router have been created", t, func() {

		Convey("When /api/counters/unknown-demo is called", func() {

			w := performRequest(router, "GET", "/api/counters/unknown-demo")
			Convey("Then we should get an error back", func() {
				So(w.Code, ShouldEqual, 500)
				So(w.Header()["Content-Type"][0], ShouldEqual, "application/json; charset=utf-8")
			})
		})

		Convey("When /api/counters/empty-demo is called", func() {

			w := performRequest(router, "GET", "/api/counters/empty-demo")
			Convey("Then we should get an error back", func() {
				actualData := unmarshalUserCount(w)

				So(w.Code, ShouldEqual, 200)
				So(w.Header()["Content-Type"][0], ShouldEqual, "application/json; charset=utf-8")
				So(len(actualData), ShouldEqual, 0)
			})
		})

		Convey("When /api/counters/webcert-demo is called without requestedNumberOfEntries", func() {

			w := performRequest(router, "GET", "/api/counters/webcert-demo")
			Convey("Then we should have a config", func() {
				actualData := unmarshalUserCount(w)

				So(w.Code, ShouldEqual, 200)
				So(w.Header()["Content-Type"][0], ShouldEqual, "application/json; charset=utf-8")
				So(len(actualData), ShouldEqual, 360)
			})
		})

		Convey("When /api/counters/webcert-demo is called with requestedNumberOfEntries", func() {

			w := performRequest(router, "GET", "/api/counters/webcert-demo?requestedNumberOfEntries=800")
			Convey("Then we should get back exactly that amount", func() {
				actualData := unmarshalUserCount(w)

				So(w.Code, ShouldEqual, 200)
				So(w.Header()["Content-Type"][0], ShouldEqual, "application/json; charset=utf-8")
				So(len(actualData), ShouldEqual, 800)
			})
		})

		Convey("When /api/counters/webcert-demo is called with more requestedNumberOfEntries than available", func() {

			w := performRequest(router, "GET", "/api/counters/webcert-demo?requestedNumberOfEntries=1800")
			Convey("Then we should get back 1000", func() {
				actualData := unmarshalUserCount(w)

				So(w.Code, ShouldEqual, 200)
				So(w.Header()["Content-Type"][0], ShouldEqual, "application/json; charset=utf-8")
				So(len(actualData), ShouldEqual, 1000)
			})
		})
	})
}

func TestGetConfig(t *testing.T) {
	router := SetupGin()

	Convey("Given that the router have been created", t, func() {

		Convey("When /api/config is called", func() {

			// Sets up the env
			setupEnv()

			w := performRequest(router, "GET", "/api/config")

			Convey("Then we should have a config", func() {
				So(w.Code, ShouldEqual, 200)
				So(w.Header()["Content-Type"][0], ShouldEqual, "application/json; charset=utf-8")
				So(w.Body.String(), ShouldEqual, expectedJSON)
			})
		})
	})
}

func TestGetApplicationInfo(t *testing.T) {
	router := SetupGin()

	setupEnv()

	statuses := make([]ApplicationData, 0)
	appStatuses["webcert-demo"] = append(statuses, ApplicationData{Application: "Webcert"})

	Convey("Given that the router have been created", t, func() {

		Convey("When /api/status/unknown-demo is called without any data present", func() {

			w := performRequest(router, "GET", "/api/status/unknown-demo")

			Convey("Then we should have a response", func() {
				So(w.Code, ShouldEqual, 500)
				So(w.Header()["Content-Type"][0], ShouldEqual, "application/json; charset=utf-8")
				So(w.Body.String(), ShouldContainSubstring, "no data for requested entry")
			})
		})

		Convey("When /api/status/webcert-demo is called with data present", func() {

			w := performRequest(router, "GET", "/api/status/webcert-demo")

			Convey("Then we should have a response", func() {
				So(w.Code, ShouldEqual, 200)
				So(w.Header()["Content-Type"][0], ShouldEqual, "application/json; charset=utf-8")
				var resp []ApplicationData
				_ = json.Unmarshal(w.Body.Bytes(), &resp)
				So(resp[0].Application, ShouldEqual, "Webcert")

			})
		})
	})
}

func setupEnv() {
	// Sets up the env
	viper.Set("OCP_SCRAPE_TARGETS", "webcert-demo,rehabstod-demo")
	viper.Set("OCP_SCRAPE_TARGETS_LABELS", "webcert-demo=Webcert,rehabstod-demo=Rehabstöd")
	parseScrapeTargetsFromEnv()
}

func performRequest(r http.Handler, method, path string) *httptest.ResponseRecorder {
	req, _ := http.NewRequest(method, path, nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)
	return w
}

func unmarshalUserCount(w *httptest.ResponseRecorder) []UserCount {
	var actualData []UserCount
	json.Unmarshal(w.Body.Bytes(), &actualData)
	return actualData
}
