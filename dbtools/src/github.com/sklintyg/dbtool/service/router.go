package service

import (
	"net/http"
	"github.com/sklintyg/dbtool/model"
	"github.com/gorilla/mux"
)

/**
 * From http://thenewstack.io/make-a-restful-json-api-go/
 */
func NewRouter(prefs model.Prefs) *mux.Router {

	router := mux.NewRouter().StrictSlash(true)
	for _, route := range routes {
		var handler http.Handler

		handler = route.HandlerFunc
		handler = Logger(handler, route.Name)
		handler = BasicAuth(handler, route.Name, prefs)

		router.
			Methods(route.Method).
			Path(route.Pattern).
			Name(route.Name).
			Handler(handler)
	}
	return router
}
