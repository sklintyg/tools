# Creates build docker images (base java8 and java11)

NAME=$(shell basename `pwd`)
TEMPLATE=buildtemplate-image
PROCESS=oc process $(TEMPLATE) -p NAME=$(NAME) -p SOURCE="`cat Dockerfile`"
PROCESS11=oc process $(TEMPLATE) -p NAME=$(NAME)-java11 -p SOURCE="`cat Dockerfile.java11`"


all: build

# build image
build: apply
	oc start-build $(NAME) --from-dir=./ --follow
	oc start-build $(NAME)-java11 --from-dir=./ --follow

# apply config
apply:
	$(PROCESS) | oc $@ -f -
	$(PROCESS11) | oc $@ -f -

# delete config
delete:
	$(PROCESS) | oc $@ -f -
	$(PROCESS11) | oc $@ -f -

