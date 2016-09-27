package service

import (
	"net/http"

	"io/ioutil"
	"github.com/sklintyg/dbtool/model"
	"encoding/json"
	"strconv"
	"log"
	"github.com/gorilla/mux"
	"os"
	"time"
	"strings"
)

func WebcertVersion(w http.ResponseWriter, r *http.Request) {
	version, err := ioutil.ReadFile(VersionFile)
	if err != nil {
		log.Println("Unable to read version.txt file with Webcert version from path " + VersionFile)
		w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte("Could not read version.txt file from path " + VersionFile))
		return
	}
	var versionMap map[string]string = make(map[string]string)
	versionMap["version"] = string(version)

	w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(versionMap)
}

func List(w http.ResponseWriter, r *http.Request) {
	files, _ := ioutil.ReadDir(DumpsDir)
	snapshots := make([]model.Snapshot, 0, 2)
	for _, f := range files {
		if strings.HasSuffix(f.Name(), ".sql") {
			snapshots = append(snapshots, model.Snapshot{ Name : f.Name(), Created : f.ModTime().String(), Size: strconv.Itoa(int(f.Size()))})
		}
	}
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	w.WriteHeader(http.StatusOK)
	if err := json.NewEncoder(w).Encode(snapshots); err != nil {
		log.Println("Error encoding List response: " + err.Error())
	}
}

func Restore(w http.ResponseWriter, r *http.Request) {
	log.Println("ENTER - Restore")
	vars := mux.Vars(r)
	var snapshotName = vars["snapshotName"]

	if !validateRequest(w, snapshotName) {
		return
	}

	err := RestoreDb(snapshotName, DbUsername, DbPassword)
	if err != nil {
		writeServerError(w, "Error when restoring database: " + err.Error())
		return
	}
	err = RestoreDb("intyg-" + snapshotName + ".intyg", Db2Username, Db2Password)
	if err != nil {
		writeServerError(w, "Error when restoring database: " + err.Error())
		return
	}

	w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(snapshotName + " bytes read into DB"))

}

func Store(w http.ResponseWriter, r *http.Request) {

	// Determine CURRENT webcert version
	versionBytes, err := ioutil.ReadFile(VersionFile)
	var version = strings.Trim(string(versionBytes), "\n")
	if err != nil {
		log.Println("Unable to read version.txt file with Webcert version from path: " + VersionFile)
		return
	}

	// Create a semi-ugly date string without - and :
	var dateStr = strings.Replace(strings.Replace(time.Now().Format(time.RFC3339), ":", "", -1), "-", "", -1)[0:15]
	var wcDbFileName = "webcert-" + dateStr + "-" + version + ".sql"
	var intygDbFileName = "intyg-" + wcDbFileName + ".intyg"

	BackupDb(wcDbFileName, "webcert", DbUsername, DbPassword)
	BackupDb(intygDbFileName, "intyg", Db2Username, Db2Password)

	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	w.WriteHeader(http.StatusOK)
	health := make(map[string]interface{})
	health["result"] = "OK"
	if err := json.NewEncoder(w).Encode(health); err != nil {
		log.Println("Error encoding Store response: " + err.Error())
	}
}

func DeleteSnapshot(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	var snapshotName = vars["snapshotName"]
	if !validateRequest(w, snapshotName) {
		return
	}

	err := os.Remove(DumpsDir + "/" + snapshotName)
	if err != nil {
		log.Println("Failed to delete webcert DB file: " + err.Error())
		writeServerError(w, "Failed to delete webcert DB file: " + err.Error())
		return
	}
	err = os.Remove(DumpsDir + "/intyg-" + snapshotName + ".intyg")
	if err != nil {
		log.Println("Failed to delete intyg DB file: " + err.Error())
	}
	w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte("Snapshot deleted OK"))
}


// - Private scope -

func validateRequest(w http.ResponseWriter, snapshotName string) bool {
	if !validateSnapshotNameSyntax(snapshotName) {
		writeBadRequest(w, "Invalid snapshotName parameter, must not contain any of ../$#_'`´<>!@£%&(){}[]+*\\\"")
		return false
	}
	if snapshotName == "" {
		writeBadRequest(w, "Missing snapshotName parameter")
		return false
	}
	if !strings.HasSuffix(snapshotName, ".sql") {
		writeBadRequest(w, "snapshotName must end in .sql")
		return false
	}
	return true
}

func validateSnapshotNameSyntax(snapshotName string) bool {
	if (strings.ContainsAny(snapshotName, "/$#_'`´<>!@£%&(){}[]+*\\\"")) {
		return false
	}
	if (strings.Contains(snapshotName, "..")) {
		return false
	}
	return true
}

func writeBadRequest(w http.ResponseWriter, msg string) {
	w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
	w.WriteHeader(http.StatusBadRequest)
	w.Write([]byte(msg))
}

func writeServerError(w http.ResponseWriter, msg string) {
	w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
	w.WriteHeader(http.StatusInternalServerError)
	w.Write([]byte(msg))
}
