# Simplifies local image creation and deploy

IS_NAME=$(shell basename `pwd`)

REPO_NAME=$(shell oc get is | grep $(IS_NAME) | cut -d' ' -f 4 )

USER=$(shell oc whoami)
TOKEN=$(shell oc whoami -t)

all: build

build:
	docker build -t $(REPO_NAME) .

push:
	docker login -u $(USER) -p $(TOKEN) $(REPO_NAME)
	docker push $(REPO_NAME)
