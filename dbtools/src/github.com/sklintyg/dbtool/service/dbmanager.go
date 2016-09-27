package service

import (
        "io/ioutil"
        "os/exec"
        "os"
        "bufio"
        "fmt"
        "io"
        "log"
)

func BackupDb(dbFileName string, databaseName string, dbUsername string, dbPassword string) error {

        // open the out file for writing
        outfile, err := os.Create(DumpsDir + "/" + dbFileName)
        if err != nil {
                log.Println("Error opening output file for writing .sql dump result: " + err.Error())
                return err
        }
        defer outfile.Close()

        cmd := exec.Command("mysqldump", "-u", dbUsername, "--password=" + dbPassword, "--databases", databaseName)

        stdoutPipe, err := cmd.StdoutPipe()
        if err != nil {
                log.Println("Error opening stdout pipe for writing .sql dump result: " + err.Error())
                return err
        }

        writer := bufio.NewWriter(outfile)
        defer writer.Flush()
        cmd.Start()
        fmt.Println("Starting taking snapshot of '" + databaseName + "' to file: " + DumpsDir + "/" + dbFileName)
        go io.Copy(writer, stdoutPipe)
        cmd.Wait()

        return nil
}


func RestoreDb(dbName string, dbUsername string, dbPassword string) error {
        log.Println("Start restore of " + dbName)
        data, err := ioutil.ReadFile(DumpsDir + "/" + dbName)

        if err != nil {
                return err
        }

        cmd := exec.Command("mysql", "-u", dbUsername, "--password=" + dbPassword)
        stdInPipe, err := cmd.StdinPipe()

        if err != nil {
                return err
        }

        cmd.Start()

        _, err = stdInPipe.Write(data)
        if err != nil {
                return err
        }
        stdInPipe.Close()
        cmd.Wait()
        log.Println("Restore of " + dbName + " finished OK")
        return nil
}
