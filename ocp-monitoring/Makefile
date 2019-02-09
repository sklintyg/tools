GOFILES = $(shell find . -name '*.go' -not -path './vendor/*')
GOPACKAGES = $(shell go list ./...  | grep -v /vendor/)
TEST_RESULTS=test-results

default: format build test vet

format:
	go fmt

vet:
	go vet ./...

build:
	export GO111MODULE=on
	go build -o ocpmonitoring

release:
	mkdir -p dist
	export GO111MODULE=on;export GOOS=linux;export GOARCH=amd64;export CGO_ENABLED=0;go build -o dist/ocpmonitoring-linux-amd64
	export GOOS=darwin
	go build -o dist/ocpmonitoring-darwin-amd64
	docker build -t sklintyg/ocpmonitoring:latest .

deploy:
	docker push sklintyg/ocpmonitoring:latest
	oc import-image sklintyg/ocpmonitoring:latest --confirm

reconfigure:
	oc delete secret ocpmonitoring-demo-secret-envvar
	oc create -f envvar-secret.yaml

test:
	mkdir -p ${TEST_RESULTS}
	@go test -coverprofile=${TEST_RESULTS}/unittest.out -v $(GOPACKAGES)
	@go tool cover -html=${TEST_RESULTS}/unittest.out -o ${TEST_RESULTS}/unittest-coverage.html
	rm -f ${TEST_RESULTS}/unittest.out

run: build
	./dist/ocpmonitoring-darwin-amd64
