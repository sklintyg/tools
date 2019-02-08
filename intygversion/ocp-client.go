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

func init() {
	var transport http.RoundTripper = &http.Transport{
		DisableKeepAlives: true,
		TLSClientConfig:   &tls.Config{InsecureSkipVerify: true},
	}
	client.Transport = transport
}

// GetTags uses the OCP REST API to get all tags for the given project/stream
func GetTags(serverUrl string, projectName string, streamName string) ([]string, error) {

	url := fmt.Sprintf("%s/apis/image.openshift.io/v1/namespaces/%s/imagestreams/%s", serverUrl, projectName, streamName)

	req, _ := http.NewRequest("GET", url, nil)
	req.Header.Add("Authorization", "Bearer "+viper.GetString("OCP_AUTH_TOKEN"))
	resp, err := client.Do(req)
	if err != nil {
		fmt.Printf("Error calling API: %v\n", err.Error())
		return nil, fmt.Errorf("Call to API returned error: %v\n", err.Error())
	}
	if resp.StatusCode == 200 {
		body, _ := ioutil.ReadAll(resp.Body)
		ocpPodResponse := &ImageStreamResponse{}
		err := json.Unmarshal(body, &ocpPodResponse)
		if err != nil {
			return nil, fmt.Errorf("Error parsing returned Pods JSON: %v\n", err.Error())
		} else {
			tags := make([]string, 0)
			for _, tag := range ocpPodResponse.Status.Tags {
				tags = append(tags, tag.Tag)
			}
			return tags, nil
		}
	} else {
		fmt.Printf("HTTP %v calling API %v\n", resp.StatusCode, url)
		return nil, fmt.Errorf("HTTP status was %v", resp.StatusCode)
	}
}
