# Deplpy utilitiy

APP_NAME=$(shell cd ..; basename $$(pwd))

GIT_URL=$(shell git config --get remote.origin.url)
BUILD_VERSION=$(shell git describe --tags --dirty=.dirty)
GIT_REF=$(shell git rev-parse --abbrev-ref HEAD)


BUILD=oc process buildtemplate-webapp -p APP_NAME=$(APP_NAME) -p GIT_URL=$(GIT_URL) -p GIT_REF=$(GIT_REF)
DEPLOY=oc process deploytemplate-webapp -p APP_NAME=$(APP_NAME) -p IMAGE=$(IMAGE) -p STAGE=$(STAGE)

deploy:
	$(eval IMAGE := $(shell oc get is | grep "^${APP_NAME}[ ]" | grep "$BUILD_VERSION" | awk '{ print $$2 }'))
	$(DEPLOY) | oc apply -f -

build:
	$(eval IMAGE=$(oc get is | grep "^${APP_NAME}[ ]" | grep "$BUILD_VERSION" | awk '{ print $2 }'))
	$(BUILD) | oc apply -f -
	oc start-build $(APP_NAME)-artifact --follow --wait
	oc start-build $(APP_NAME) --follow --wait

install-config:
	oc create configmap $(APP_NAME)-config --from-file=config/$(STAGE)/
	oc create secret generic $(APP_NAME)-env --from-file=env/$(STAGE)/ --type=Opaque

clean-build:
	$(BUILD) | oc delete -f -

clean-config:
	oc delete configmap $(APP_NAME)-config
	oc delete secret $(APP_NAME)-env

assemble-resources:
	@echo $(GIR_URL)
	@echo $(APP_NAME)

