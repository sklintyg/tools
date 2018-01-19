# Intyg OpenShift directory structure

### CentOS7 / Tomcat

##### Runtime container directory tree

- /tomcat (CATALINA_HOME, base dir of tomcat)
- /opt/inera (Base directory for application-specific configuration and files)
- /opt/inera/konfiguration (.properties, logback.xml, SAML metadata) CONFIG MAP, no secret stuff here.
- /opt/inera/certifikat (.jks, .p12) SECRET, mounted from here. Bundle credentials.properties with certs?
- /opt/inera/credentials (credentials.properties) SECRET - REPLACE WITH ENVS?
- /opt/inera/resources (diagnoskoder, features.yaml, authorities.yaml) Copied from source at build-time OR mount from persistent volume.

### Configuration 2.0
How can we use the exact same .properties files in all installs (from minishift through test,demo,qa to prod) where _all_ dynamic values are injected from environment variables?

##### Managing server.xml
We have some DB and AMQ usernames, passwords, URLs etc. in server.xml which needs to be injected from secrets or env vars at container startup.

We're doing it like this (example from Webcert):

##### 1. Ansible updates the server.xml with values from _/ansible-openshift/group_vars/all_ using the _/ansible-openshift/roles/webcert-resurser_

An example resulting server.xml snippet (cleaned up for brevity) looks like this:

    <Resource name='sharedWebcert' global='sharedWebcert' auth='Container' type='javax.sql.DataSource' 
    driverClassName='com.mysql.jdbc.Driver' 
    username='${db_username}'   <!-- SEE HERE -->
    password='${db_password}'   <!-- SEE HERE -->
    url='jdbc:mysql://${db_server}:${db_port}/${db_name}?useCompression=true' 
    />
    
Those ${db_username} etc. placeholders needs to be passed to Tomcat using -Ddb_username=[SOME VALUE] at startup.

##### 2. Add -Dkey=value entries to the APP_JVM_ENV environment variable declared in the deployment configuration:

     
     - env:
       - name: APP_JVM_ENV
         value: -Ddb_server=$DB_SERVER -Ddb_port=$DB_PORT -Ddb_name=$DB_NAME -Ddb_username=$DB_USERNAME -Ddb_password=$DB_PASSWORD -Dactivemq_username=$ACTIVEMQ_USERNAME -Dactivemq_password=$ACTIVEMQ_PASSWORD ...
 
The APP_JVM_ENV becomes VERY long but this is the least bad way we've managed to do this yet for plain old Tomcat.
         
##### 3. Set up separate env vars for non-secret values
The DB_SERVER, DB_PORT and DB_NAME (for example) doesn't have to be handled as secrets. Add separate entries for those env vars to the deployment-config:


    - name: DB_SERVER
      value: mysql
    - name: DB_PORT
      value: '3306'
    - name: DB_NAME
      value: webcert

Note that numeric values has to be placed inside quotes, at least according to an error message...
      
##### 4. Create secret key-value pairs for secret stuff such as usernames and passwords.

Create a .yaml file containing usernames and passwords in clear text.

    apiVersion: v1
    kind: Secret
    metadata:
      name: webcert-db-credentials
    stringData:
      db_username: webcert
      db_password: webcert
      activemq_username: ""
      activemq_password: ""
      
_Note that encrypted values can be specified too, in that case change "stringData:" to "data:"_

Install the above key-value pairs into openshift with automatic encryption using either the GUI (Add to project / Import YAML/JSON) or oc create -f [filename].

This should create a secrets entry _"webcert-db-credentials"_ we can reference from our deployment config when declaring the remaining env vars, see below.

##### 5. Add env-vars for secret values
We're still in the env: section of the deployment configuration. Example:


    - name: DB_USERNAME
      valueFrom:
        secretKeyRef:
          name: webcert-db-credentials
          key: db_username

The env entry above declares an environment variable "DB_USERNAME" whose value will be resolved at runtime from the value of the "webcert-db-credentials" secret's "db_username" key.

6. Done!
The pattern used above will store the value of the db_username encrypted in OpenShift, transparently decrypting it and injecting it into an environment variable at container startup.

### Ansible in S2I

For now, we're only using Ansible for:

- Copying .jar files into /tomcat/lib
- Modifying server.xml and context.xml with JNDI resources such as DB connection and JMS broker configuration.
- Copying _/src/main/resources_ into _/opt/inera/resources_, retaining the directory structure.

The S2I "assemble" script will look for _/ansible-openshift/provision.yaml_ in the cloned source and if present, will execute that playbook onto localhost.
