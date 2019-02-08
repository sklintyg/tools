# Intyg version finder
Uses the OpenShift REST interface to scan a defined set of image streams for their latest MAJOR.MINOR builds.

Example output:

    {
      "intygstjanst-test-verified": [
        "3.8.0.187",
        "3.6.0.447",
        "3.7.0.188"
      ],
      "logsender-test-verified": [
        "6.2.0.197",
        "6.3.0.182",
        "6.1.0.446"
      ],
      "minaintyg-test-verified": [
        "3.7.0.444",
        "3.8.0.447",
        "3.9.0.180"
      ],
      "privatlakarportal-test-verified": [
        "1.8.0.76",
        "1.9.0.55",
        "2.0.0.32"
      ],
      "rehabstod-test-verified": [
        "1.7.0.170",
        "1.8.0.174",
        "1.9.0.85"
      ],
      "statistik-test-verified": [
        "7.1.0.438",
        "7.2.0.42"
      ],
      "webcert-test-verified": [
        "6.2.0.326",
        "6.3.0.228",
        "6.1.0.788"
      ]
    }
    
### Running
First, you need to establish your VPN connection to the test cluster and then log into your openshift cluster using: 

    oc login <URL to test cluster>

Then the following environment variables needs to be set:

    export OCP_AUTH_TOKEN=$(oc whoami -t)
    export OCP_SERVER_URL=https://portal-test1.ind-ocp.sth.basefarm.net
    export OCP_PROJECT_NAME=dintyg
    export OCP_IMAGE_STREAMS=webcert-test-verified,intygstjanst-test-verified,rehabstod-test-verified,privatlakarportal-test-verified,logsender-test-verified,minaintyg-test-verified,statistik-test-verified

### Without Go SDK
There are pre-built binaries for Linux, OS X and Windows in the dist/ folder.

    cd dist
    ./

### With Go SDK installed

Start by running either of these two:

    go run *.go
    
or

    make run
    
Start up and GET _http://localhost:8080/api/versions_ using web browser, curl or similar.

Or use curl with some nice formatting:

    curl http://localhost:8080/api/versions | jq '.'
 
## Building etc
This little util is built in Golang since I had code from other projects around for talking to the OCP REST API.

To build the binaries or running directly from source you need Go 1.11 or later installed since the util uses "go modules" introduced in Go 1.11.

For convenience, there's a Makefile to easily produce the binaries:

    make release 
    
## TODOs

Wrap the linux binary in a Docker container and deploy it in "dintyg" with an OCP_AUTH_TOKEN from a "service account", then the output can be available at any time.