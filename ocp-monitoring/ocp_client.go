package main

import (
	"crypto/tls"
	"fmt"
	"github.com/spf13/viper"
	"io/ioutil"
	"k8s.io/apimachinery/pkg/util/json"
	"net/http"
)

var client = &http.Client{}

// init configures our underlying transport so it accepts https without trusted certs
func init() {
	var transport http.RoundTripper = &http.Transport{
		DisableKeepAlives: true,
		TLSClientConfig:   &tls.Config{InsecureSkipVerify: true},
	}
	client.Transport = transport
}

// GetPods calls the OCP REST API given the supplied serverUrl and string and returns all running pods
func GetPods(serverUrl string, projectName string) ([]PodItem, error) {

	url := fmt.Sprintf("%s/api/v1/namespaces/%s/pods", serverUrl, projectName)

	req, _ := http.NewRequest("GET", url, nil)
	req.Header.Add("Authorization", "Bearer "+viper.GetString("OCP_AUTH_TOKEN"))
	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("Call to API returned error: %v\n", err.Error())
	}
	if resp.StatusCode == 200 {
		body, _ := ioutil.ReadAll(resp.Body)
		ocpPodResponse := &OcpPod{}
		err := json.Unmarshal(body, &ocpPodResponse)
		if err != nil {
			return nil, fmt.Errorf("Error parsing returned Pods JSON: %v\n", err.Error())
		} else {
			return ocpPodResponse.Items, nil
		}
	} else {
		return nil, fmt.Errorf("HTTP status was %v", resp.StatusCode)
	}
}
