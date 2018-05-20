# Deplpy utilitiy

#APP_NAME=$(shell cd ..; basename `pwd`)

PROCESS=oc process deploytemplate-webapp -p APP_NAME=$(APP_NAME) -p IMAGE=$(IMAGE_NAME)

apply:
	$(PROCESS) | oc $@ -f -

	
prepare:
	oc create configmap $(APP_NAME)-config --from-file=config/
	oc create secret generic $(APP_NAME)-env --from-file=env/ --type=Opaque

clean:
	oc delete configmap $(APP_NAME)-config
	oc delete secret $(APP_NAME)-env

