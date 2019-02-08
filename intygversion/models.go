package main

import "time"

// ImageStreamResponse defines a struct matching the JSON output for image streams from OCP. Generated from a sample JSON response
type ImageStreamResponse struct {
	Kind       string `json:"kind"`
	APIVersion string `json:"apiVersion"`
	Metadata   struct {
		Name              string    `json:"name"`
		Namespace         string    `json:"namespace"`
		SelfLink          string    `json:"selfLink"`
		UID               string    `json:"uid"`
		ResourceVersion   string    `json:"resourceVersion"`
		Generation        int       `json:"generation"`
		CreationTimestamp time.Time `json:"creationTimestamp"`
		Labels            struct {
			App   string `json:"app"`
			Stage string `json:"stage"`
		} `json:"labels"`
		Annotations struct {
			KubectlKubernetesIoLastAppliedConfiguration string    `json:"kubectl.kubernetes.io/last-applied-configuration"`
			OpenshiftIoImageDockerRepositoryCheck       time.Time `json:"openshift.io/image.dockerRepositoryCheck"`
		} `json:"annotations"`
	} `json:"metadata"`
	Spec struct {
		LookupPolicy struct {
			Local bool `json:"local"`
		} `json:"lookupPolicy"`
		Tags []struct {
			Name        string      `json:"name"`
			Annotations interface{} `json:"annotations"`
			From        struct {
				Kind string `json:"kind"`
				Name string `json:"name"`
			} `json:"from"`
			Generation   int `json:"generation"`
			ImportPolicy struct {
			} `json:"importPolicy"`
			ReferencePolicy struct {
				Type string `json:"type"`
			} `json:"referencePolicy"`
		} `json:"tags"`
	} `json:"spec"`
	Status struct {
		DockerImageRepository string `json:"dockerImageRepository"`
		Tags                  []struct {
			Tag   string `json:"tag"`
			Items []struct {
				Created              time.Time `json:"created"`
				DockerImageReference string    `json:"dockerImageReference"`
				Image                string    `json:"image"`
				Generation           int       `json:"generation"`
			} `json:"items"`
		} `json:"tags"`
	} `json:"status"`
}
