package service

import (
        "net/http"
        "strings"
        "encoding/base64"
        "github.com/sklintyg/dbtool/model"
)

var username string
var password string

func BasicAuth(inner http.Handler, name string, prefs model.Prefs) http.Handler {

        username = prefs.Username
        password = prefs.Password

        return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
                if checkAuth(w, r) {
                        inner.ServeHTTP(w, r)
                        return
                }

                w.Header().Set("WWW-Authenticate", `Basic realm="MY REALM"`)
                w.WriteHeader(401)
                w.Write([]byte("401 Unauthorized\n"))
        })
}

func checkAuth(w http.ResponseWriter, r *http.Request) bool {
        s := strings.SplitN(r.Header.Get("Authorization"), " ", 2)
        if len(s) != 2 { return false }

        b, err := base64.StdEncoding.DecodeString(s[1])
        if err != nil { return false }

        pair := strings.SplitN(string(b), ":", 2)
        if len(pair) != 2 { return false }

        return pair[0] == username && pair[1] == password
}
