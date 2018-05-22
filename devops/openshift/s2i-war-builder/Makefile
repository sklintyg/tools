# Creates docker image

NAME=$(shell basename `pwd`)
TEMPLATE=buildtemplate-image
PROCESS=oc process $(TEMPLATE) -p NAME=$(NAME) -p SOURCE="`cat Dockerfile`"

all: build

# build image
build: apply
	oc start-build $(NAME) --from-dir=./ --follow

# apply config
apply:
	$(PROCESS) | oc $@ -f -

# delete config
delete:
	$(PROCESS) | oc $@ -f -

