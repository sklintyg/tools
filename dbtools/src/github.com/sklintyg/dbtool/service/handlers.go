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

	cmd := exec.Command("mysql", "-u", "webcert", "--password=webcert")
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
	version, err := ioutil.ReadFile(VersionFile)
	if err != nil {
		log.Println("Unable to read version.txt file with Webcert version from path " + VersionFile)
	}

	// Create a semi-ugly date string without - and :
	var dateStr = strings.Replace(strings.Replace(time.Now().Format(time.RFC3339), ":", "", -1), "-", "", -1)[0:15]

	// open the out file for writing
	outfile, err := os.Create(DumpsDir + "/webcert-" + dateStr + "-" + string(version) + ".sql")
	if err != nil {
		panic(err)
	}
	defer outfile.Close()

	cmd := exec.Command("mysqldump", "-u", "webcert", "--password=webcert", "--databases","webcert")

	stdoutPipe, err := cmd.StdoutPipe()
	if err != nil {
		panic(err)
	}

	writer := bufio.NewWriter(outfile)
	defer writer.Flush()
	cmd.Start()
	fmt.Println("Starting taking snapshot to file: " + DumpsDir + "/webcert-latest-" + string(version) + ".sql")
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
