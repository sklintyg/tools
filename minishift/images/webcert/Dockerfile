FROM anapsix/alpine-java

ADD /liquibase /liquibase

WORKDIR "/liquibase"
ENTRYPOINT ["/liquibase/bin/webcert-liquibase-runner","--url=jdbc:mysql://mysql:3306/webcert?useCompression=true", "--username=webcert", "--password=webcert", "update"]
CMD[]
