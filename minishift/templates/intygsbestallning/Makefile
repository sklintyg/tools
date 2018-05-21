# Deplpy utilitiy

GIT_URL=https://github.com/sklintyg/ib-backend.git
GIT_BRANCH=develop
BUILD=oc process buildtemplate-webapp -p APP_NAME=$(APP_NAME) -p GIT_URL=$(GIT_URL) -p GIT_BRANCH=$(GIT_BRANCH)
DEPLOY=oc process deploytemplate-webapp -p APP_NAME=$(APP_NAME) -p IMAGE=$(IMAGE) -p STAGE=$(STAGE)

deploy:
	$(DEPLOY) | oc apply -f -

build:
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

