package service

import "net/http"

/**
 * Derived from http://thenewstack.io/make-a-restful-json-api-go/
 */

type Route struct {
	Name        string
	Method      string
	Pattern     string
	HandlerFunc http.HandlerFunc
}

type Routes []Route

var routes = Routes{
	Route{
		"List",
		"GET",
		"/snapshots",
		List,
	},
	Route{
		"Restore",
		"GET",
		"/snapshot/{snapshotName}",
		Restore,
	},
	Route{
		"Store",
		"POST",
		"/snapshot",
		Store,
	},
	Route{
		"DeleteSnapshot",
		"DELETE",
		"/snapshot/{snapshotName}",
		DeleteSnapshot,
	},
	Route{
		"WebcertVersion",
		"GET",
		"/webcert/version",
		WebcertVersion,
	},
	Route{
		"Auth",
		"GET",
		"/auth",
		Auth,
	},

}
