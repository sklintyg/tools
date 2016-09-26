package service

import (
        "net/http"
        "log"
        "github.com/sklintyg/dbtool/model"
)

var DumpsDir string
var VersionFile string
var DbUsername string
var DbPassword string

func StartWebServer(prefs model.Prefs) {

        DumpsDir = prefs.SnapshotsDir
        VersionFile = prefs.VersionFile

        port := prefs.Port

        r := NewRouter(prefs)
        s := http.StripPrefix("/static/", http.FileServer(http.Dir("./static/")))
        r.PathPrefix("/static/").Handler(s)
        http.Handle("/", r)
        http.Handle("/dbtool", r)

        err := http.ListenAndServe(":"+ port, nil)

        log.Println("Starting HTTP service at " + port)

        if err != nil {
                log.Println("An error occured starting HTTP listener at port " + port)
                log.Println("Error: " + err.Error())
        }
}
