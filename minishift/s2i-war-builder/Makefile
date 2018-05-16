# Simplifies local image creation and deploy.
# Requires oc, docker, make and the openshift imagestream (reoo)

IS_NAME=$(shell basename `pwd`)
REPO_NAME=$(shell oc get is | grep $(IS_NAME) | awk '{ print $$2}' )
USER=$(shell oc whoami)
TOKEN=$(shell oc whoami -t)
SRC=imagestream-s2i-war-builder.yaml

all: build

# build container
build:
	docker build -t $(IS_NAME) .

# requires an existing image stream (repo), see install
push: update build
	@echo Login to docker repository $(REPO_NAME)
	@echo $(TOKEN) | docker login -u $(USER) --password-stdin $(REPO_NAME)
	docker tag $(IS_NAME) $(REPO_NAME)
	docker push $(REPO_NAME)

# install image stream
install: $(SRC)
	oc create -f $<

# update image stream
update: $(SRC)
	oc replace -f $<

