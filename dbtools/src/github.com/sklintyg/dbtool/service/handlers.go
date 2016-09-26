package service

import (
	"fmt"

	"net/http"

	"io/ioutil"
	"github.com/sklintyg/dbtool/model"
	"encoding/json"
	"strconv"
	"log"
	"github.com/gorilla/mux"
	"os/exec"
	"os"
	"bufio"
	"io"
	"time"
	"strings"
)

func Auth(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
	w.WriteHeader(http.StatusOK)
}

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
		// TODO filter out any non .sql files
		snapshots = append(snapshots, model.Snapshot{ Name : f.Name(), Created : f.ModTime().String()})
	}
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	w.WriteHeader(http.StatusOK)
	if err := json.NewEncoder(w).Encode(snapshots); err != nil {
		panic(err)
	}
}

func Restore(w http.ResponseWriter, r *http.Request) {
	log.Println("ENTER - Restore")
	vars := mux.Vars(r)
	var snapshotName = vars["snapshotName"]
	if !validateSnapshotName(snapshotName) {
		w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
		w.WriteHeader(http.StatusBadRequest)
		w.Write([]byte("Invalid snapshotName parameter, must not contain any of ../$#_'`´<>!@£%&(){}[]+*\\\""))
		return
	}
	if snapshotName == "" {
		w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
		w.WriteHeader(http.StatusBadRequest)
		w.Write([]byte("Missing snapshotName parameter"))
		return
	}

	data, err := ioutil.ReadFile(DumpsDir + "/" + snapshotName)

	if err != nil {
		panic(err)
	}

	cmd := exec.Command("mysql", "-u", DbUsername, "--password=" + DbPassword)
	stdInPipe, err := cmd.StdinPipe()

	if err != nil {
		panic(err)
	}

	cmd.Start()

	len, err := stdInPipe.Write(data)
	if err != nil {
		panic(err)
	}
	stdInPipe.Close()
	cmd.Wait()

	w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(strconv.Itoa(len) + " bytes read into DB"))

}

func Store(w http.ResponseWriter, r *http.Request) {

	// Determine CURRENT webcert version
	versionBytes, err := ioutil.ReadFile(VersionFile)
	var version = strings.Trim(string(versionBytes), "\n")
	if err != nil {
		log.Println("Unable to read version.txt file with Webcert version from path " + VersionFile)
	}

	// Create a semi-ugly date string without - and :
	var dateStr = strings.Replace(strings.Replace(time.Now().Format(time.RFC3339), ":", "", -1), "-", "", -1)[0:15]

	// open the out file for writing
	outfile, err := os.Create(DumpsDir + "/webcert-" + dateStr + "-" + version + ".sql")
	if err != nil {
		panic(err)
	}
	defer outfile.Close()

	cmd := exec.Command("mysqldump", "-u", DbUsername, "--password=" + DbPassword, "--databases","webcert")

	stdoutPipe, err := cmd.StdoutPipe()
	if err != nil {
		panic(err)
	}

	writer := bufio.NewWriter(outfile)
	defer writer.Flush()
	cmd.Start()
	fmt.Println("Starting taking snapshot to file: " + DumpsDir + "/webcert-latest-" + version + ".sql")
	go io.Copy(writer, stdoutPipe)
	cmd.Wait()
	fmt.Println("Backup finished!")

	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	w.WriteHeader(http.StatusOK)
	health := make(map[string]interface{})
	health["result"] = "OK"
	if err := json.NewEncoder(w).Encode(health); err != nil {
		panic(err)
	}
}

func DeleteSnapshot(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	var snapshotName = vars["snapshotName"]
	if !validateSnapshotName(snapshotName) {
		w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
		w.WriteHeader(http.StatusBadRequest)
		w.Write([]byte("Invalid snapshotName parameter, must not contain any of ../$#_'`´<>!@£%&(){}[]+*\\\""))
		return
	}
	if snapshotName == "" {
		w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
		w.WriteHeader(http.StatusBadRequest)
		w.Write([]byte("Missing snapshotName parameter"))
		return
	}

	err := os.Remove(DumpsDir + "/" + snapshotName)
	if err != nil {
		log.Fatal(err)
	}
	w.Header().Set("Content-Type", "text/plain; charset=UTF-8")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte("Snapshot deleted OK"))
}

func validateSnapshotName(snapshotName string) bool {
	if (strings.ContainsAny(snapshotName, "/$#_'`´<>!@£%&(){}[]+*\\\"")) {
		return false
	}
	if (strings.Contains(snapshotName, "..")) {
		return false
	}
	return true
}
