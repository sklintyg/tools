# Intyg OpenShift directory structure

### CentOS7 / Tomcat

##### Runtime container directory tree

- /tomcat (CATALINA_HOME, base dir of tomcat)
- /opt/inera (Base directory for application-specific configuration)
- /opt/inera/konfiguration (.properties, logback, SAML metadata) CONFIG MAP
- /opt/inera/certifikat (.jks, .p12) SECRET
- /opt/inera/credentials (credentials.properties) SECRET - REPLACE WITH ENVS?
- /opt/inera/resources (diagnoskoder, features.yaml, authorities.yaml) Copied from source at build-time. Mount from persistent volume.

### Configuration 2.0
How can we use the exact same .properties files in all installs (from minishift through test,demo,qa to prod) where _all_ dynamic values are injected from environment variables?

##### Managing server.xml
We have some DB and AMQ usernames, passwords, URLs etc. in server.xml which needs to be injected from secrets or env vars at container startup.

We're doing it like this:

1. Ansible updates the server.xml with values from _/ansible-openshift/group_vars/all_ using the _/ansible-openshift/roles/webcert-resurser_

An example resulting server.xml snippet (cleaned up for brevity) looks like this:

    <Resource name='sharedWebcert' global='sharedWebcert' auth='Container' type='javax.sql.DataSource' 
    driverClassName='com.mysql.jdbc.Driver' 
    username='${db_username}' 
    password='${db_password}' 
    url='jdbc:mysql://${db_server}:${db_port}/${db_name}?useCompression=true' 
    />
    
Those ${db_username} etc. needs to be passed to Tomcat using -Ddb_username=[SOME VALUE] at startup.

2. Add -D to the APP_JVM_ENV environment variable declared in the deployment configuration:

     
     - env:
       - name: APP_JVM_ENV
         value: -Ddb_server=$DB_SERVER -Ddb_port=$DB_PORT -Ddb_name=$DB_NAME -Ddb_username=$DB_USERNAME -Ddb_password=$DB_PASSWORD -Dactivemq_username=$ACTIVEMQ_USERNAME -Dactivemq_password=$ACTIVEMQ_PASSWORD ...
          
3. Set up separate env vars for non-secret values
The DB_SERVER, DB_PORT and DB_NAME (for example) doesn't have to be handled as secrets. Add separate entries for those env vars to the deployment-config:


    - name: DB_SERVER
      value: mysql
    - name: DB_PORT
      value: '3306'
    - name: DB_NAME
      value: webcert
      
4. Create secret key-value pairs for secret stuff such as usernames and passwords.

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
      
Install the above key-value pairs into openshift with automatic encryption using either the GUI (Add to project / Import YAML/JSON) or oc create -f [filename].

This should create a secrets entry we now can reference from our deployment config when declaring the remaining env vars:

5. Add env-vars for secret values
Example:


    - name: DB_USERNAME
      valueFrom:
        secretKeyRef:
          name: webcert-db-credentials
          key: db_username

The env entry above declares an environment variable "DB_USERNAME" whose value will be resolved at runtime from the "webcert-db-credentials" secret and the value from its "db_username" key.

6. Done!
The pattern used above will store the value of the db_username encrypted in OpenShift, transparently decrypting it and injecting it into an environemnt variable at container startup.
