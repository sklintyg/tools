package main

import (
	"github.com/spf13/viper"
	"gopkg.in/h2non/gock.v1"
	"testing"
)
import . "github.com/smartystreets/goconvey/convey"

var podItemJson = `{"items":[{
    "metadata": {
    "labels": {
        "application": "activemq"
    }
},
    "status": {
        "podIP": "10.1.2.3"
    }
}]}`

func TestGetPods(t *testing.T) {
	var url = "http://localhost:8080/api/v1/namespaces/pintyg/pods"
	gock.InterceptClient(client)
	gock.New(url).
		MatchHeader("Authorization", "Bearer <token>").Reply(200).BodyString(podItemJson)

	gock.New(url).
		MatchHeader("Authorization", "Bearer ").Reply(403)

	defer gock.Off()

	Convey("Given", t, func() {

		Convey("When calling GetPods", func() {
			viper.Set("OCP_AUTH_TOKEN", "<token>")

			items, err := GetPods("http://localhost:8080", "pintyg")

			Convey("Then we should get a response", func() {
				So(err, ShouldBeNil)
				So(len(items), ShouldEqual, 1)
			})
		})

		Convey("When calling GetPods without authorization header", func() {
			viper.Set("OCP_AUTH_TOKEN", "")
			items, err := GetPods("http://localhost:8080", "pintyg")

			Convey("Then we should get a response", func() {
				So(err, ShouldNotBeNil)
				So(len(items), ShouldEqual, 0)
			})
		})
	})
}
