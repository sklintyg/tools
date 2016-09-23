package main

import (
    "fmt"
    "sync"
    "log"
    "github.com/sklintyg/dbtool/model"
    "github.com/sklintyg/dbtool/service"
    "os"
    "io/ioutil"
    "gopkg.in/yaml.v2"
)

var DEFAULT_VERSION_FILE = "/opt/inera/tomcat7/webapps/version.txt"
var DEFAULT_DUMPS_DIR = "/opt/inera/dbtool/dumps"

func main() {
    fmt.Printf("Starting Webcert DB tool...\n")

    dir, _ := os.Getwd()
    dat, err := ioutil.ReadFile(dir + "/preferences.yml")
    if (err != nil) {
        log.Fatalln("Cannot start DB tool, preferences.yml is missing from " + dir)
    }
    var prefs model.Prefs
    yaml.Unmarshal([]byte(dat), &prefs)

    version, err := ioutil.ReadFile(prefs.VersionFile)
    if err != nil {
        log.Fatal("Unable to read version.txt file with Webcert version from path " + prefs.VersionFile)
    }

    fmt.Printf("Webcert version at DB tool startup is " + string(version) + "\n")

    go service.StartWebServer(prefs)

    // Block...
    wg := sync.WaitGroup{}                       // Use a WaitGroup to block main() exit
    wg.Add(1)
    wg.Wait()
}

