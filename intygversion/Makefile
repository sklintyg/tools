default: format build vet

format:
	go fmt

vet:
	go vet ./...

build:
	mkdir -p dist
	export GO111MODULE=on;go build -o dist/intygversion-darwin-amd64

release:
	mkdir -p dist
	export GO111MODULE=on;export GOOS=linux;export GOARCH=amd64;export CGO_ENABLED=0;go build -o dist/intygversion-linux-amd64
	export GO111MODULE=on;export GOOS=windows;export GOARCH=amd64;export CGO_ENABLED=0;go build -o dist/intygversion-windows-amd64
	export GO111MODULE=on;export GOOS=darwin;export GOARCH=amd64;export CGO_ENABLED=0;go build -o dist/intygversion-darwin-amd64
