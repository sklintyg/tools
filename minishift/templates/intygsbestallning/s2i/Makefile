# Utility to create and delete build configurations for intygsbestallning

GIT_URL=https://github.com/sklintyg/ib-backend.git
GIT_BRANCH=develop
PROCESS=oc process buildtemplate-webapp -p APP_NAME=$(APP_NAME) -p GIT_URL=$(GIT_URL) -p GIT_BRANCH=$(GIT_BRANCH)

all: apply

apply:
	$(PROCESS) | oc $@ -f -

delete:
	$(PROCESS) | oc $@ -f -

list:
	$(PROCESS) -o yaml

build:
	oc start-build $(APP_NAME)-artifact --follow --wait
	oc start-build $(APP_NAME) --follow --wait
