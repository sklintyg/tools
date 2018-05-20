# Deplpy utilitiy

PROCESS=oc process deploytemplate-webapp -p APP_NAME=$(APP_NAME) -p IMAGE=$(IMAGE_NAME) -p STAGE=$(STAGE)

apply:
	$(PROCESS) | oc $@ -f -

prepare:
	oc create configmap $(APP_NAME)-$(STAGE)-config --from-file=config/$(STAGE)/
	oc create secret generic $(APP_NAME)-$(STAGE)-env --from-file=env/$(STAGE)/ --type=Opaque

clean:
	oc delete configmap $(APP_NAME)-$(STAGE)-config
	oc delete secret $(APP_NAME)-$(STAGE)-env

