# Intygstjänster - OpenShift Container Platform (OCP) 

For Intygstjänster 2018 the main objective is to run all applications on a OpenShift cluster. This document provides work-in-progress thoughts and findings on the 
topic.

## OCP Web Apps and Dev Pipelines

### Architectural pre-requisites

* Use parameterized and shared OCP templates for all Web Apps
* Replace ansible for provisioning with base docker images and application controlled software components only
* Rely on shared conventions for
  * Build and test folder structure
  * Configuration, credentials and resources
  * Logging
  * Backing services
  * Monitoring and health-checks

### Housekeeping

Build and test processes generates a lot of artifacts such as images, pods/containers, reports and logs. Therefore it's necessary to have some fundamental house keeping functions.

* Builds have properties to control history limits, also see `successfulBuildsHistoryLimit` and `failedBuildsHistoryLimit`. Normally are these set to a value in the range `2..4`.
* A dedicated housekeeping pipeline cleans:
  * Completed pods/containers (> 1 day)
  * Latest images from image streams (only keep a history of 20 develop builds per stream)
  * Jenkins reports and build logs (only keep a history of 20 days) 

Release images are kept forever. 

Housekeeping functions are executed as a pipeline, see `pipeline-houskeeping.yaml`, nightly. Also see `jobtemplate.yaml` for how to trigger the housekeeping pipeline with a `CronJob`  running `curl`.

**Note:** _For the time being all images and main versions of develop builds are defined within the pipeline definition, see `def images = [ 'image': "'X.Y.Z', ...]` list in `pipeline-houskeeping.yaml`. This means that when a new release branch is created the version numbers have to be bumnped and the housekeeping pipeline needs to be updated, accordingly._

Also see scripts `clean-istags.sh` and `clean-pods.sh`.

The build configuration (housekeeping) must be installed or updated in the OCP project of question:

```
$ oc [ create | replace ] -f pipeline-houskeeping.yaml
```

### Nightly Demo Deploy Pipeline

In the demo OCP project a specific pipeline is used to perform nightly deploys of the latest builds. The pipeline definition, see `pipeline-demo-deploy.yaml` and the `def apps = [ 'name': 'git-url' ]` list.

Prior to add an application to the `apps` list a valid deployment definition has to be created with `${APP_NAME}-secret-envvar` and `${APP_NAME}-certifikat`. All other required configuration artifacts are for each redeploy refreshed from source code and the `~/devops/openshift/demo` folder.

It's assumed a demo application ends with suffix `-demo` and that it's deployment config refers to verified image streams from project `dintyg` tagged with `latest`. 

The build configuration (nightly-deploy) must be installed or updated in the OCP project of question:

```
$ oc [ create | replace ] -f pipeline-demo-deploy.yaml
```

### Webhook Proxy for GitHub

To be able to accept webhooks from GitHub and trigger builds a proxy is needed.
 
The proxy is [gitwebhookproxy](https://github.com/stakater/GitWebhookProxy) with the image from docker hub [https://hub.docker.com/r/stakater/gitwebhookproxy/](https://hub.docker.com/r/stakater/gitwebhookproxy/).

A deployment config, service and route has to be configured, and the following environment variables are set in the deployment config:

| Name | Value |
| ---- | ----- |
| LISTEN | :8080 |
| UPSTREAMURL | https://portal-test1.ind-ocp.sth.basefarm.net |
| ALLOWEDPATHS | |
| PROVIDER | github |
| SECRET | _from git-webhook-secret with key WebhookSecretKey_ |

_Note: ALLOWEDPATHS shall be empty, and the SECRET value is from a secret with name git-webhook-secret and the key WebhookSecretKey_

### Web App OCP Templates

Dev pipelines for Web Apps are realized with OCP Templates and the following templates exists:

* buildtemplate-image.yaml - Docker Image Builder
* buildtemplate-webapp-binary.yaml - Web App Builder (gradle, java, tomcat)
* buildtemplate-bootapp-binary.yaml - Spring App Builder (gradle, java 8 and 11)
* deploytemplate-webapp.yaml - Web App Deployer
* testrunnertemplate-pod.yaml - Pod Test Runner (gradle, java)
* pipelinetemplate-build-webapp.yaml - Web App Test Pipeline. Depends the templates above
* jobtemplate.yaml - Runs scheduled scripts. 

Each template must be installed or updated in the OCP project of question:

```
$ oc [ create | replace ] -f <template-file>
```
 
#### Base images

Base images for artifact builds, tomcat apps, spring boot and a special for srs exists. These are uploaded to BaseFarm nexus server `docker.drift.inera.se/intyg` due to stability problems with OpenShift image streams.

They are created with `make` and the uploaded to BaseFarm nexus with the buildtemplate-image, see dintyg and build configs such as:

* springboot-base-nexus
* tomcat-base-nexus
* srs-base-nexus
* s2i-war-builder-nexus
* s2i-war-builder-java11-nexus

The tags indicates main version and currently are 8 and 11 used to indicate java versions and 9 for tomcat version. Tagging is done manually in OpenShift `oc tag <is>:latest <is>:8` before pushing to BaseFarm nexus. 

 
#### Template Docker Image Builder 

For creating docker images (ImageStream) using plain Dockerfiles. Normally the docker layer of the OCP cluster is unreachable for a developer.

**Name:** buildtemplate-image  

**Parameters:** 

| Parameter | Required | Description |
| --------- | -------- | ----------- |
| NAME      | Yes      | The docker image name, ex: `s2i-war-builder` |
| STAGE     |          | The stage label, default is `test` |        
| SOURCE    | Yes      | The content of the Dockerfile |
| BUILD_VERSION |      | The docker image version tag, default is `latest` |

**Outputs:**

A Docker image according to the source spec, and if necessary an new ImageStream.

**Example:**

```
$ oc process buildtemplate-image -p NAME=s2i-war-builder \
	-p SOURCE="$(cat Dockerfile)" | oc apply -f -
```

#### Deprecated: Template Web App Builder

_Replaced by Template Web App Binary Builder_ 

For building Web Apps (WAR) with OCP S2I and gradle-wrapper (java). A two-step process first building a WAR artifact and then a tomcat docker image running the WAR.

**Name:** buildtemplate-webapp  

**Parameters:** 

| Parameter | Required | Description |
| --------- | -------- | ----------- |
| APP_NAME  | Yes         | The Web App name, ex: `ib-backend` |
| STAGE     |             | The stage label, default is `test` |        
| ARTIFACT\_IMAGE\_SUFFIX | | The suffix of the artifact ImageStream, default is `artifact` |
| GIT_URL   | Yes         | URL to the git repository to clone |
| GIT_REF   |             | The ref to build, default is branch `develop` |
| BUILDER_IMAGE |         | The builder image and tag to use, default is `s2i-war-builder:latest` |
| COMMON_VERSION |        | The common-version to use, default is `3.7.0.+` | 
| INFRA_VERSION |         | The infra-version to use, default is `3.7.0.+` | 
| BUILD_VERSION |         | The build version for the Web App, default is `1.0-OPENSHIFT` |
| CONTEXT_PATH |          | The Web App context path, default is `ROOT`. _Please note: this setting is translated to the base-name of the Web App WAR file and not a path as such._ |


**Conventions:**

* gradle wrapper (gradlew) is used to build the Web App
* build output WAR placed shall be placed in sub-folder `web/build/libs`

**Outputs:**

* S2I docker image (artifact ImageStream) containing the checked out source and built WAR file, scripts to run tests and a build information file named `build.info`. 
* Web App runtime image (runtime ImageStream) with tomcat and the build WAR file.
* Verified Web App ImageStream, used by the pipeline if a runtime image passes all tests.

**Example:**

```
$ oc process buildtemplate-webapp -p APP_NAME=ib-backend-dev  \
	-p GIT_URL=https://github.com/sklintyg/ib-backend.git \
	-p STAGE=dev | oc apply -f -
```

#### Deprecated: Template Web App Binary Builder


For building Web Apps (WAR) with OCP S2I and gradle-wrapper (java). A two-step docker build process first building a WAR artifact and then a tomcat docker image running the WAR.

**Name:** buildtemplate-webapp-binary

**Parameters:** 

| Parameter | Required | Description |
| --------- | -------- | ----------- |
| APP_NAME  | Yes         | The Web App name, ex: `ib-backend` |
| STAGE     |             | The stage label, default is `test` |        
| ARTIFACT\_IMAGE\_SUFFIX | | The suffix of the artifact ImageStream, default is `artifact` |
| GIT_URL   | Yes         | URL to the git repository to clone |
| GIT_REF   |             | The ref to build, default is branch `develop` |
| COMMON_VERSION |        | The common-version to use, default is `3.7.0.+` | 
| INFRA_VERSION |         | The infra-version to use, default is `3.7.0.+` | 
| BUILD_VERSION |         | The build version for the Web App, default is `1.0-OPENSHIFT` |
| GRADLE_USER_HOME |      | Gradle stuff (.gradle locatiomn), default is `/tmp/src` |
| E_UID |                 | Effective user id of build process, default is `1000130000` |
| CONTEXT_PATH |          | The Web App context path, default is `ROOT`. _Please note: this setting is translated to the base-name of the Web App WAR file and not a path as such._ |


**Conventions:**

* gradle wrapper (gradlew) is used to build the Web App
* build output WAR placed shall be placed in sub-folder `web/build/libs`
* Binary input from compiled and unit tested source `--from-archive=...`

**Outputs:**

* S2I docker image (artifact ImageStream) containing the checked out source and built WAR file, scripts to run tests. 
* Web App runtime image (runtime ImageStream) with tomcat and the build WAR file.
* Verified Web App ImageStream, used by the pipeline if a runtime image passes all tests.

**Example:**

```
$ oc process buildtemplate-webapp-binary -p APP_NAME=ib-backend  \
	-p GIT_URL=https://github.com/sklintyg/ib-backend.git \
	-p STAGE=dev | oc apply -f -
```

#### Template Web App Deployer 

For deploying Web Apps with service and route endpoints.

**Name:** deploytemplate-webapp  

**Parameters:** 

| Parameter | Required | Description |
| --------- | -------- | ----------- |
| APP_NAME      | Yes         | The Web App name, ex: `ib-backend` |
| STAGE         |             | The stage label, default is `test` |        
| IMAGE         | Yes         | The image and tag to deploy |
| DATABASE_NAME | Yes         | Existing database to connect to. _Please note: overrides environment variable for database to use, and if not applicable any value might be used_   |
| HEALTH_URI    |             | The path (URI) to the health check service, default is `/`|

**Conventions:**

* The environment variable `DATABASE_NAME` specifies the database to use.
* A config map exists with name `${APP_NAME}-config` and will be mounted to `/opt/${APP_NAME}/config`
* A secret exists with name `${APP_NAME}-env` and will mounted to `/opt/${APP_NAME}/env`
* Web App listens on HTTP port `8080`
* The health probe uses HTTP `GET` to access the health check service
* JVM Memory settings and credentials has to be fine-tuned for each unique deployment 

**Outputs:**

* Rolls out a deployment with 1 replica (pod)
* A service with name `${APP_NAME}`
* A route terminating TLS (HTTPS) with name `${APP_NAME}-route`

**Example:**

```
$ oc process deploytemplate-webapp \
	-p APP_NAME=intygstjanst-test \
	-p STAGE=test \
	-p IMAGE=docker-registry.default.svc:5000/dintyg/intygstjanst-test-verified:latest \
	-p DATABASE_NAME=intygstjanst_test \
	-p HEALTH_URI=/inera-certificate | oc apply -f - 
```

#### Template Pod Test Runner 

For running external client test suites from a test pipeline.

**Name:** testrunnertemplate-pod

**Parameters:** 

| Parameter | Required | Description |
| --------- | -------- | ----------- |
| APP_NAME      | Yes         | The Web App name, ex: `ib-backend` |
| STAGE         |             | The stage label, default is `test` |        
| TARGET_URL    | Yes         | The target URL of the service to test |
| ACTUATOR_URL  |             | The target URL of the actuator on the service to test |
| JOB_NAME      | Yes         | The pipeline job name for traceability |
| BUILD_VERSION | Yes         | The Web App build version |
| CALLBACK_URL  | Yes         | The callback URL to post wither `SUCCESS` or `FAILURE` when all tests have finished |
| BUILD_TAG     | Yes         | The actual pipeline build tag |
| IMAGE         | Yes         | The image to run tests. _For the time being tests are only carried out by the Web App artifact image_ |
| TESTS         | Yes         | A comma separated list of named test(s) to run |

**Conventions:**

* The test script is the default entry-point of the docker image.
* The pipeline waits until a callback HTTP call is received with a text/plan body containing `SUCCESS` upon success, otherwise it's a failure. 
* A PVC named `test-report` exists, and is mounted to volume `/mnt/reports`.
* Jenkins has a mount path `/var/lib/jenkins/reports` for the same `test-report` PVC.
* Test reports (HTML files only) are copied to `/mnt/reports`.

**Outputs:**

* Runs the application artifact image and the specified tests, 1 replica (pod)


#### Deprecated: Template Web App Test Pipeline

_This pipeline has been replaced by an end-to-end Build Pipeline, se below._

For building and testing Web App docker images.

**Name:** pipelinetemplate-test-webapp

**Parameters:** 

| Parameter | Required | Description |
| --------- | -------- | ----------- |
| APP_NAME                | Yes         | The Web App name, ex: `ib-backend` |
| STAGE                   |             | The stage label, default is `test` |        
| BUILD_TEMPLATE          |             | Name of the build template, default is: `buildtemplate-webapp` |
| DEPLOY_TEMPLATE         |             | Name of the deploy template, default is: `deploytemplate-webapp` |
| TESTRUNNER_TEMPLATE     |             | Name of the testrunner template, default is: `testrunnertemplate-pod` |
| ARTIFACT\_IMAGE\_SUFFIX |             | The suffix of the artifact ImageStream, default is `artifact` |
| SECRET                  | Yes         | The secret for triggering the pipeline |
| CONTEXT_PATH            |             | The Web App context path, default is `ROOT`. _Please note: this setting is translated to the base-name of the Web App WAR file and not a path as such._ |
| HEALTH_URI              |             | The path (URI) to the health check service, default is `/`|
| BACKING_SERVICES        |             | A comma separated list of backing services to start (each needs to have a verified image and working config (secret, configmap). Ex: `BACKING_SERVICES="intygstjanst-test,minaintyg-test"` | 
| TESTS                   | Yes         | A comma separated list of  test to run `restAssuredTest`, `protractorTest` and `fitnesseTest`. For no tests shall a dash `-` be specified. |

**Conventions:**

* Required backing services such as mysql, activemq and redis are up and running, and mysql is expected to run in the same OCP project.
* Web App configuration with config map and secret must exist prior to run the pipeline.
* Application database users must exists and have admin privileges in the actual database.
* A randomly named database is created for each test run, i.e. the application must honor the `DATABASE_NAME` environment variable. 
* The web hook trigger must contain the following environment variables `gitRef`, `gitUrl`, `buildVersion` and `infraVersion`. And may also specify `commonVersion` and `backingServices` (overriding `BACKING_SERVCIES`), also see below.
* Secret for envvars and certifikat are expected to already exist in environment as well as configuration settings for any backing services.    

**Outputs:**

* Upon a successful run an image reference is created in the ImageStream `${APP_NAME}-verified` and tagged with the `${buildVersion}` from trigger as well as with the `latest` tag.
* Updated configmap (config), secret (env) and configmap-envvars for the app.
* Test Reports are registered to Jenkins, requires plugin, see publishHTML.  

**Example:**

```
$ oc process pipelinetemplate-test-webapp \
	-p APP_NAME=statistik-test \
	-p STAGE=test \
	-p SECRET=nosecret  \
	-p TESTS="protractorTest,fitnesseTest" | oc apply -f -
```

**Trigger**

The pipeline trigger contains meta-data about the build, environment variables are passed in the `env` section as an array of name and value properties..

Example trigger for a release build of WebCert.

_***Note:*** To be able to distinguish between release and development builds a release flag is used. The main difference is that release builds never are tagged as `latest`. The release value must be a string and it's just to omit this flag for develop builds._ 

```json
{
  "git": {
    "uri": "https://github.com/sklintyg/webcert.git"
  "env": [
    {
      "name": "gitRef",
      "value": "v6.2.0.101"
    },
    {
      "name": "gitUrl",
      "value": "https://github.com/sklintyg/webcert.git"
    },
    {
      "name": "buildVersion",
      "value": "6.2.0.101"
    },
    {
      "name": "infraVersion",
      "value": "3.8.0.+"
    },
    {
      "name": "commonVersion",
      "value": "3.8.0.+"
    },
    {
    	"name": "backingServices",
    	"value": "intygstjanst-test:3.6.0.435"
    },
    {
    	"name": "release",
    	"value": "true" 
    }
  ]
}
```

Trigger properties:

| Name | Required | Description |
| ---- | -------- | ----------- |
| gitUrl | Yes | Repository URL |
| gitRef | Yes | Branch, tag or commit ref to build |
| buildVersion | Yes | The build version |
| infraVersion | Yes | The infra dependency version, ex latest 3.8.0 `3.8.0.+` |
| commonVersion | No | The common dependency version if any exists, otherwise it shall be omitted |
| backingServices | No | Overrides `BACKING_SERVICES`, see above, for the pipeline. A comma separated list of `<name>[:<tag>]` pairs. Ex: `intygstjanst-test:3.7.0.53`. Default tag is `latest`. Typically used for release branches with dependencies to specific versions. |
| release | No | A string value `"true"`or `"false"` indicating if this is a release build. |

**CI-Jenkins Integration**

Checkout files `TriggerOCBuildJenkinsfile` and `Jenkinsfile` in the root of each application. `TriggerOCBuildJenkinsfile` assembles the trigger content and the last step of the pipeline defined in `Jenkinsfile` fires the trigger with parameters.

#### Template Web App Build Pipeline

For end-to-end building and testing Web App docker images.

**Name:** pipelinetemplate-build-webapp

**Parameters:** 

| Parameter | Required | Description |
| --------- | -------- | ----------- |
| APP_NAME                | Yes         | The Web App name, ex: `ib-backend` |
| GIT_URL                 | Yes         | URL to git repository | 
| GIT_CI_BRANCH           | Yes         | Branch in git repository | 
| STAGE                   |             | The stage label, default is `test` |        
| BUILD_TEMPLATE          |             | Name of the build template, default is: `buildtemplate-webapp` |
| DEPLOY_TEMPLATE         |             | Name of the deploy template, default is: `deploytemplate-webapp` |
| TESTRUNNER_TEMPLATE     |             | Name of the testrunner template, default is: `testrunnertemplate-pod` |
| ARTIFACT\_IMAGE\_SUFFIX |             | The suffix of the artifact ImageStream, default is `artifact` |
| CONTEXT_PATH            |             | The Web App context path, default is `ROOT`. _Please note: this setting is translated to the base-name of the Web App WAR file and not a path as such._ |
| HEALTH_URI              |             | The path (URI) to the health check service, default is `/`|
| BUILD_TOOL              |             | The tool to build binaries with default is `shgradle` |
| TEST_PORT               |             | Test TCP port to use. Default is `8081` | 
| RELEASE                 |             | Defines if this i a release build. Default is `false` | 

**Conventions:**

* Required backing services such as mysql, activemq and redis are up and running, and mysql is expected to run in the same OCP project.
* Web App configuration with config map and secret must exist prior to run the pipeline.
* Application database users must exists and have admin privileges in the actual database.
* A randomly named database is created for each test run, i.e. the application must honor the `DATABASE_NAME` environment variable. 
* The file `build-info.json` with versions, build arguments, tests etc is required in the source root folder 
* Secret for envvars and certifikat are expected to already exist in environment as well as configuration settings for any backing services.

**Outputs:**

* Upon a successful run an image reference is created in the ImageStream `${APP_NAME}-verified` and tagged with the `${buildVersion}` from trigger as well as with the `latest` tag.
* Updated configmap (config), secret (env) and configmap-envvars for the app.
* Test Reports are registered to Jenkins, requires plugin, see publishHTML.
* The source is tagged with version and build-number.
* Release images and additionally artifacts are uploaded to nexus.

**For all applications except Intygstjanst and Intygsadmin this example can be used to process the template:**

```
$  oc process pipelinetemplate-build-webapp -p APP_NAME=logsender-test -p GIT_URL=https://github.com/sklintyg/logsender.git -p GIT_CI_BRANCH=release/2020-1 | oc apply  -f -
```

**Intygstjanst:**

```
$  oc process pipelinetemplate-build-webapp -p APP_NAME=intygstjanst-test -p GIT_URL=https://github.com/sklintyg/intygstjanst.git -p GIT_CI_BRANCH=<branch> -p CONTEXT_PATH="inera-certificate" -p HEALTH_URI="/inera-certificate/services" | oc apply  -f -
```

**Intygsadmin:**

```
$  oc process pipelinetemplate-build-webapp -p APP_NAME=intygsadmin-test -p GIT_URL=https://github.com/sklintyg/intygsadmin.git -p GIT_CI_BRANCH=<branch> -p BUILD_TEMPLATE=buildtemplate-bootapp-binary -p BUILD_TOOL=shgradle11 -p HEALTH_URI=/version.html -p TEST_PORT=8080 | oc apply  -f -
```

**Trigger**

Connect GitHub Webhook to pipeline. See more about gitwebhookproxy above. 

**CI-Jenkins Integration**

Checkout the file `build-info.json` in the root of each application. 

Example:

```
{
  "appVersion": "6.4.1",
  "commonVersion": "3.10.1.+",
  "infraVersion": "3.10.1.+",
  "buildArgs": "--refresh-dependencies clean build camelTest testReport sonarqube -PcodeQuality -PcodeCoverage -DgruntColors=false",
  "tests": [ "restAssuredTest", "protractorTest" ],
  "uploadArchives": true,
  "backingService": "intygstjanst-test:3.9.1.+"
}
```

_A build-number substituted with a plus-sign (+) indicates the latest build_

#### Template Scheduled Jobs 

Typically used to trigger a pipeline like the `housekeeping` pipeline etc.

**Name:** jobtemplate

**Parameters:** 

| Parameter | Required | Description |
| --------- | -------- | ----------- |
| JOB_NAME      | Yes         | The name, ex: `housekeeping-trigger` |
| SCRIPT        | Yes         | The script to execute, currently are `bash` and `curl` available.  |        
| SCHEDULE      |             | Cron expression default is `0 3 * * *`, i.e. 3AM each night|

**Conventions:**

* Runs the script as is. 

**Outputs:**

* Depends on the script (script content is logged).


## OpenShift anatomy
Openshift is a PaaS (Platform as a Service) built on top of the CaaS (Container orchestration as a Service) Kubernetes (K8S). OpenShift and K8S introduces a number of abstractions on top of traditional containers to provide orchestration, routing, resilience, configuration etc.
 
![img2](docs/openshift-hierarchy.png)

- A cluster is backed by one or more **nodes**. A _node_ is a physical or virtualized OS running a OpenShift master or worker. As developers, we should _never_ have to concern ourselves with actual nodes, this is something NMT or BF will provide and configure for us.
- A cluster always backs at least one **project**. A _project_ is typically declared for a number of related applications and their services in a given environment and provides the basis for multi-tenancy in OpenShift. In intygstjänster, we'll typically declare a _project_ for an environment running our applications such as:
    - test
    - demo
     - perf
     - qa
     - staging
     - prod
- Inside a _project_ we can deploy our applications as declared by a **deployment configuration**, typically defining a _container image_ for the application we want to deploy, number of replicas, ports to expose etc.
- A launched _deployment configuration_ will create a **pod** that may run one or more **containers** for the given _container image_.
- If we want our container(s) within a _pod_ to be visible _within_ the cluster, we need to define a **service** that provides an internal DNS name so our applications can talk to each other. The _service_ abstraction provides internal load-balancing, e.g. if we have one _pod_ running three instances of the _intygstjanst_ container image, requests for the logical service name "intygstjanst" will be load-balanced automatically amongst the three instances.
- If we want a _service_ (for example Webcert) to be accessible from outside of the cluster, we need to declare a **route** connecting the service to the outside world, including a port mapping and possibly an external hostname.

### Editing egress network policy
You may need to open egress for accessing github or similar. If your user has the correct authorization:

    oc edit egressnetworkpolicy
    

## Minishift on Mac instructions
This folder contains some work-in-progress instructions and notes on how to get OpenShift Origin running using "minishift" on a local virtualbox virtual machine.

Also includes some YAML files for setting up ActiveMQ and MySQL for intyg use.

### Installing
##### 1. Install using homebrew:

    $ brew cask install minishift

##### 2. Update using homebrew

    $ brew cask install --force minishift
  
##### 3. Start with virtualbox:

    $ minishift start --disk-size=40G --vm-driver=virtualbox
    -- Starting local OpenShift cluster using 'virtualbox' hypervisor ...
    -- Minishift VM will be configured with ...
       Memory:    2 GB
       vCPUs :    2
       Disk size: 40 GB
     ........
     OpenShift server started.
     
     The server is accessible via web console at:
         https://192.168.99.100:8443
     
     You are logged in as:
         User:     developer
         Password: <any value>
     
     To login as administrator:
         oc login -u system:admin
         
##### 4. Setup first project

1.) Open web browser at https://192.168.99.100:8443/console/ _(you may need to change your IP)_

2.) Log in user system:admin

3.) Create your first project

Name it _intygstjanster-test_

         
##### 5. Fix oc command
Back to the command-line       
         
    $ minishift oc-env
    export PATH="/Users/yourusername/.minishift/cache/oc/v3.6.0:$PATH"
    
    OR
    
    > eval $(minishift oc-env)
    
Run that export:

    > export PATH="/Users/yourusername/.minishift/cache/oc/v3.6.0:$PATH"

##### 6. Log in to oc

    $ oc login -u system:admin
    Logged into "https://192.168.99.100:8443" as "system:admin" using existing credentials.
    Using project "myproject".
 
##### 7. Change project and grant root so we can run stuff like mysql and activemq    
    $ oc project intygstjanster-test
    $ oc adm policy add-scc-to-user anyuid -z default

##### 8. Fix Docker path
It's very convenient to use the minishift's docker engine for building images locally. Run this:

    $ eval $(minishift docker-env)    

##### 9. Stopping minishift
Why would you want to do that? Well, just in case:

    $ minishift stop
         
### A note on supporting services
Please note that running MySQL and ActiveMQ as containers with mounted storage is _not_ suitable for production usages. Most users of container orchestrators runs their Database and Messaging backends on dedicated hardware outside the cluster - just like BF does for us currently.

From the point of view of our applications, this shouldn't matter since all we need to know about is URLs and possible user credentials. For dev, test, demo purposes it's fine running these services inside the cluster.


### Installing the Spring Boot version of Logsender using CLI

There's a container image with a Spring Boot version of our "logsender" application on docker hub: _eriklupander/logsender-boot_.

It's configured to use two mounted resources:

- /config/application.yaml - Config map. This resource overrides some of the defaults of the _application.yaml_ baked into the .jar artifact.
- /opt/inera/logsender-konfiguration - Secret. This resource contains a _credentials.properties_ file as well as _keystore.jks_ and _truststore.jks_.

Use the following sequence of commands to deploy a functional pod with logsender:

    $ oc create -f templates/logsender-boot/configmap-logsender.yaml
    $ oc create -f templates/logsender-boot/secrets-logsender.yaml
    $ oc create -f templates/logsender-boot/deploymentconfig-logsender.yaml

There are files for _service_ and _route_ but those aren't needed.

### Installing our applications using CLI
Since we don't have any container images (yet) we need to build our applications before we can deploy them into OpenShift.

Each application has a _s2i_ and _deploy_ folder with templates for building and deploying an application respectively.

##### Pre-requisits

If not already done so, you have to set up the build image first. The s2i-war-builder is the base image we use to build each application.

Make sure the image builder template has been installed.

	$  oc create -f tools/devops/openshift/buildtemplate-image.yaml

Go to the `tools/devops/openshift/s2i-war-builder` folder and build the builder image.
	
	$ make build

...or with plain oc

	$ oc process buildtemplate-image -p NAME=s2i-war-builder -p SOURCE="$(cat Dockerfile)" | oc apply -f -
	$ oc start-build s2i-war-builder --from-dir=./ --follow

Use the GUI to confirm that the image was successfully imported.

	{project_name} => Builds => Images
	
Make sure that the generic build template has been installed:
	
	$ oc create -c tools/devops/openshift/buildtemplate-war.yaml 

#### Building

**Create build config**

	$ oc process buildtemplate-war \
		-p APP_NAME=intygsbestallning-pl \
		-p GIT_URL=https://github.com/sklintyg/ib-backend.git \
		-p GIT_BRANCH=develop | oc apply -f -
	

**Build**
	
	# build artifact
	$ oc start-build {app_name}-artifact
	
	# build runtime
	$ oc start-build {app_name}
	

Goto the UI to watch the build process

	{project_name} => Builds => Images => {app_name}-N-build]

#### Secret and Config Map

An application deployment config expects one secret with the name `{app_name}-env` and one config map with the name `{app_name}-config`

Create secret for certificates, credentials, resources, etc.

    $ oc create secret generic {app_name}-env --from-file {config-directory} --type=Opaque

Create config map with text files:
	
	$ oc create configmap {app_name}-config --from-file {config-directory}
	
This last command might not be applicable to all application. It depends if tha application uses a database and/or message broker or any other external resource.
 
#### Deploying

	$ oc process deploytemplate-webapp -p APP_NAME="{app_name}" -p IMAGE="{image}:{build_version}" -p STAGE={stage} | oc apply -f -

Goto the UI and watch the deploy process

	{project_name} => Applications => Pods => {app_name-N-xyz12}
	

## Running all .yaml files in directory

Run all `.yaml` files in a given directory:

    $ oc create -R=true -f={directory}

## REST examples

#### Set some ENV-vars
    $ oc login -u system -p admin
    $ TOKEN=$(oc whoami -t)
    $ ENDPOINT=$(oc config current-context | cut -d/ -f2 | tr - .)
    $ NAMESPACE=$(oc config current-context | cut -d/ -f1)

#### Call service

    $ curl -k \
        -H "Authorization: Bearer $TOKEN" \
        -H 'Accept: application/json' \
        https://$ENDPOINT/api/v1/namespaces/$NAMESPACE/pods

## Using script webapp-tool.sh to C(R)UD an application

The `webapp-tool.sh` is a generic script to bootstrap an application from openshift templates. It's designed to be executed from the source tree of the application to deploy, 
and assigns default values for:

* APP_NAME - from git repository name
* GIT_URL - from git
* GIT_REF - current git ref
* STAGE - default is test
* BUILD_VERSION - default is the actual tag (see git describe --tags)

Usage:

	# Run from remote source
	$ curl -s https://raw.githubusercontent.com/sklintyg/tools/develop/devops/openshift/webapp-tool.sh | bash /dev/stdin -h
	$ usage: webapp-tool.sh [ -bcdhr ] [ -n <app_name> ] [ -m <build_version> ] [ -t <git_ref> ] [ -s <stage> ]
		  -b: do build
		  -c: do config
		  -d: do deploy
		  -h: prints usage options
		  -n <app_name>: set application name (default is git project name)
		  -m <build_version>: set build version (default is git tag)
		  -r: remove config, build or deploy  (in combination with other flags)
		  -s <stage>: stage name (default is test)
		  -t <git_ref>: build from git ref (default is current)
	

## 2-Step Source to Image (S2I) Build Strategy 

To isolate images for building the application from images to run the application a two-step build process is applied. The first step is a normal S2I _Source_ type build resulting in an image with built artifacts and the second step extracts the artifacts from the first step and performs a _Docker_ type build.


#### Base Images

The build image for step one is based on a plain `openshift/base-centos7` with Java 8 and custom s2i scripts added, and the runtime image is currently based on a Red Hat JBoss Tomcat Server  `registry.access.redhat.com/jboss-webserver-3/webserver30-tomcat8-openshift`.

###### build assemble script (step 1)
	
	#!/bin/bash
	
	# Global S2I variable setup
	# Use openshift env vars
	#source $(dirname "$0")/s2i-setup
	
	# Source code provided to directory
	S2I_SOURCE_DIR=${S2I_SOURCE_DIR-"/tmp/src"} <-- Note: S2I downloads code to /tmp/src 
	
	# Resulting WAR files will be copied to this location
	S2I_ARTIFACTS_DIR=${S2I_ARTIFACTS_DIR-"/tmp/artifacts"}
	mkdir -p $S2I_ARTIFACTS_DIR
	
	# Source artifact
	SOURCE_ARTIFACTS_DIR=$S2I_SOURCE_DIR/web/build/libs <-- Note: Gradle is expected to output the WAR file at this location
	
	# Target artifact WAR
	TARGET_WAR=$S2I_ARTIFACTS_DIR/ROOT.war
	
	# Setup build nev
	export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS \
	-DbuildVersion=$BUILD_VERSION  \
	-DinfraVersion=$INFRA_VERSION \
	-DcommonVersion=$COMMON_VERSION \
	-Dfile.encoding=UTF-8"
	
	# create build info
	create_build_info() {  <-- Note: Used to save build info to a file
	    SHA256=$(sha256sum $S2I_ARTIFACTS_DIR/ROOT.war | awk '{ print $1 }')
	    TIMESTAMP=$(date --iso-8601='seconds')
	cat << EOF > $S2I_ARTIFACTS_DIR/build.info
	TIMESTAMP=$TIMESTAMP
	ARTIFACT=$S2I_ARTIFACTS_DIR/ROOT.war
	ARTIFACT_SHA256=$SHA256
	ARTIFACT_FROM=$WAR
	SOURCE=$OPENSHIFT_BUILD_SOURCE
	BRANCH=$OPENSHIFT_BUILD_REFERENCE
	COMMIT=$OPENSHIFT_BUILD_COMMIT
	OPENSHIFT_BUILD_NAME=$OPENSHIFT_BUILD_NAME
	OPENSHIFT_PROJECT=$OPENSHIFT_BUILD_NAMESPACE
	BUILDER_IMAGE=${BUILDER_IMAGE}
	EOF
	}
	
	# Start build using gradle.
	if [ -f "$S2I_SOURCE_DIR/build.gradle" ]; then
	    GRADLE_ARGS=${GRADLE_ARGS:-"--no-daemon build"}
	
	    echo "---> Building application from source with gradle wrapper GRADLE_ARGS=$GRADLE_ARGS"
	    (cd $S2I_SOURCE_DIR; ./gradlew $GRADLE_ARGS)
	    ERR=$?
	    if [ $ERR -ne 0 ]; then
	        echo "Aborting due to error code $ERR from gradle build"
	        exit $ERR
	    fi
	
	    # Copy built file into tomcat webapps
	    WAR=$(find ${SOURCE_ARTIFACTS_DIR} -name \*.war)
	    if [ -f "$WAR" ]; then
	        echo "---> Move output artifact $WAR to $TARGET_WAR"
	        mv $WAR $TARGET_WAR
	        ERR=$?
	        if [ $ERR -ne 0 ]; then
	            echo "Unable to move output artifact, exit code: $ERR"
	            exit $ERR
	        fi
	    else
	        echo "---> Output WAR file $WAR could not be found in directory $SOURCE_ARTIFACTS_DIR"
	        exit 1
	    fi
	
	    echo "---> Create build info"
	    create_build_info
	else
	    echo "---> No such gradle.build file in directory $S2I_SOURCE_DIR"
	    exit 1
	fi
	
	# cleanup
	echo "---> Cleaning up"
	rm -rf $S2I_SOURCE_DIR $HOME/.gradle $HOME/.m2
	
	echo "build done."


### Generic openshift template for building WAR applications

As long as it's about to build a typical intyg WAR packaged application there exists a build template which can be used to generate:

* Build configuration to build the artifact and runtime image.
* ImageStreams to store images.


The build template name is `buildtemplate-war` and the definition file is `templates/openshift/buildtemplate-war.yaml`:

	# create template
	$ oc create -f buildtemplate-war.yaml

Example of use to generate or update build configurations for the application `intygsbestallning`:

	# apply for application
	$ oc process buildtemplate-war -p APP_NAME=intygsbestallning -p GIT_URL=https://github.com/sklintyg/ib-backend.git | oc apply -f - 

Now we have a build configuration and can trigger a build by starting step 1 (intygsbestallning-artifact).

	# build images for intygsbestallning
	$ oc start-build intygsbestallning-artifact
	
_Step 2 is automatically started on a successful completion of step 1_
	

The `APP_NAME` and `GIT_URL` parameters are mandatory, the following parameters might be used to customize the configuration:
	
	* APP_NAME - name of application, mandatory
	* STAGE - stage environment default is dev
	* ARTIFACT_IMAGE_SUFFIX - suffix of artifact container default value is artifact
	* GIT_URL - source repository, mandatory
	* GIT_REF - source tag/branch/ref default is develop
	* GENERIC_SECRET - secret default is intygtestar
	* BUILDER_IMAGE - S2I image to build, default is s2i-war-builder:latest
	* COMMON_VERSION - version of common to use
	* INFRA_VERSION - version of infra to use
	* BUILD_VERSION - build version label 

## Creating the S2I image

Go to the `tools/devops/openshift/s2i-war-builder` folder.
A Makefile exists to simplify creation of image and openshift image stream. You are expected to be logged in to openshift.
 
 	# create image stream, requires the `buildtemplate-image` to be installed
 	$ make build
 	
If you would like to dive into details, please checkout:

* S2I - [https://github.com/openshift/source-to-image/releases/tag/v1.1.8](https://github.com/openshift/source-to-image/releases/tag/v1.1.8)
* DIY - [https://blog.openshift.com/create-s2i-builder-image](https://blog.openshift.com/create-s2i-builder-image)


# Debugging a running container

Sometimes its necessary to troubleshoot stuff in a running container. Luckily, openshift and "oc" provides built-in port-forwarding so it's enough for us to add the necessary JVM args, expose our selected port (such as 5005) using a service and then forward that port from our local host computer.

### Steps

##### 1. Prepend the JVM args for debugging to our APP_JVM_ENV environment variable.

    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -D..... -D...... 
  

##### 2. Add debug port to service
Make sure the service of the container we want to debug has a service that exposes port 5005 or similar.

    spec:
      ports:
      - name: 8080-tcp
        port: 8080
        protocol: TCP
        targetPort: 8080
      - name: 5005-tcp             <-- HERE!!
        port: 5005
        protocol: TCP
        targetPort: 5005

##### 3. Replace / Redeploy deployment config, service etc.
Note that the service must be deleted and then recreated. Mina Intyg as example.

    $ oc replace -f deploymentconfig-minaintyg.yaml
    $ oc delete -f service-minaintyg.yaml
    $ oc create -f service-minaintyg.yaml 

##### 4. Run oc pods to get pod name
We need to know the name of the pod to port-forward to

    $ oc get pods
    NAME                    READY     STATUS      RESTARTS   AGE
    activemq-1-wdwhj        1/1       Running     3          4d
    minaintyg-12-vbs3m      1/1       Running     0          5m    

##### 5. Set up port-forwarding
 Use _oc port-forward [podnamn] [lokal port]:[remote port]_, e.g:
  
    $ oc port-forward minaintyg-12-vbs3m 5005:5005
    Forwarding from 127.0.0.1:5005 -> 5005
    Forwarding from [::1]:5005 -> 5005

##### 6. Start debug-session
Start debugging against localhost:5005 or similar using IDEA, Eclipse, NetBeans or whatever...

# BF Sandbox access

##### OC client
for interacting with BF OpenShift make sure you use the `oc` client application provided by Red Hat, and not the Origin one:

1. https://access.redhat.com/downloads/content/290
   - requires Red Hat account
2. https://mirror.openshift.com/pub/openshift-v3/clients/3.7.23/
   - open Red Hat trusted source, requires no account/registration

We currently use version 3.7.23. Always make sure your client is up to date with the server, if in doubt type `oc version`. (edited)

Download and unpack somewhere, the tar.gz is unpacked into the pure oc binary.

To login to the sandbox, 

    $ oc login https://portal-ocpsbx1-ind.ocp.osl.basefarm.net
    
Use your BF username/password

    $ oc project intyg


##### VPN URL

    vpn.osl.basefarm.net
    
##### GUI

    https://portal-ocpsbx1-ind.ocp.osl.basefarm.net/console/project/intyg/overview
