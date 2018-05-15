# Simplifies local image creation and deploy, requires oc CLI and make
# To be able to push an existing image stream is expected

IS_NAME=$(shell basename `pwd`)
REPO_NAME=$(shell oc get is | grep $(IS_NAME) | cut -d' ' -f 4 )
USER=$(shell oc whoami)
TOKEN=$(shell oc whoami -t)
SRC=imagestream-s2i-war-builder.yaml

all: build

# build container
build:
ifeq ($(REPO_NAME),)
	@echo "Warning: no image stream exists. Create one with make install"
endif
	docker build -t $(IS_NAME) .

# requires an existing image stream (repo), see install
push: update build
	@echo $(TOKEN) | docker login -u $(USER) --password-stdin $(REPO_NAME)
	docker tag $(IS_NAME) $(REPO_NAME)
	docker push $(REPO_NAME)

# install image stream
install: $(SRC)
	oc create -f $<

#  update stream
update: $(SRC)
	oc replace -f $<

