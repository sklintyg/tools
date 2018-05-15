# Simplifies local image creation and deploy
# To be able to push an appropriate image stream is expected to exists

IS_NAME=$(shell basename `pwd`)
REPO_NAME=$(shell oc get is | grep $(IS_NAME) | cut -d' ' -f 4 )
USER=$(shell oc whoami)
TOKEN=$(shell oc whoami -t)
SRC=imagestream-s2i-war-builder.yaml

all: build

# build container
build:
	docker build -t $(IS_NAME) .

# requires an existing image stream (repo), see install
push: update build
	docker login -u $(USER) -p $(TOKEN) $(REPO_NAME)
	docker tag $(IS_NAME) $(REPO_NAME)
	docker push $(REPO_NAME)

# install image stream
install: $(SRC)
	oc create -f $<

#  update stream
update: $(SRC)
	oc replace -f $<

