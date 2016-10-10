# Webcert DB tool

### Vad är detta?
Webcert DB tool (wdt) är ett enkelt verktyg för att mha _mysqldump_ och _mysql_ ta och återställa "ögonblicksbilder" (aka snapshots) av en given databas.

### Bygga
wdt är byggt i golang(https://golang.org/) och dess källkod återfinns under /tools/dbtool.

Vi bygger wdt mha gradle:

    export GOPATH=[din sökväg till /tools/dbtool]
    cd src/github.com/sklintyg/dbtool
    ./gradlew build
    
Ovanstående rader kommer i en helt ren miljö hämta Go SDK för din miljö, hämta dependencies samt bygga go-binärer för Linux, Windows och OS X till katalogen /src/github.com/sklintyg/dbtool/build/bin

Det finns även ett bash-script som bygger och paketerar en release-zip för linux/amd64 och kopierar den till /tools/ansible/roles/dbtool/templates

    ./buildanddeploy.sh
       
Vill man köra lokalt kan man göra följande:

    export GOPATH=[din sökväg till /tools/dbtool]
    cd src/github.com/sklintyg/dbtool
    go get
    go run *.go

### Konfiguration
På målmiljön, öppna _preferences.yml_ som skall ligga bredvid binären (eller i rotbiblioteket för dbtool om man kör lokalt):

    ---
    port: 7171
    snapshots_dir: /opt/inera/dbtool/dumps
    version_file: /opt/inera/tomcat7/version.txt
    username: username        # change!
    password: password        # change!
    db_username: webcert-db-username
    db_password: webcert-db-password
    db2_username: intyg-db-username
    db2_password: intyg-db-password
      
- Port för tjänsten
- Korrigera om nödvändigt sökvägarna till /dumps-mappen samt version.txt.
- Username / password är för HTTP Basic skydd av verktygets GUI samt API:er.
- db_username / db_password är användarnamn och lösenord till Webcert-databasen

### Apache-konfiguration
För att köra på en typisk testmiljö för Webcert behöver man korrigera /etc/httpd/conf.d/webcert.conf

    <VirtualHost *:80>
        ServerAdmin webmaster@intygspoc.inera.nordicmedtest.se
        ServerName intygspoc.inera.nordicmedtest.se
        ProxyPreserveHost On
        ProxyPass /dbtool/ http://127.0.0.1:7171/         # Lägg till!
        ProxyPassReverse /dbtool/ http://127.0.0.1:7171/  # Lägg till!
        ProxyPass / http://127.0.0.1:8181/
        ProxyPassReverse / http://127.0.0.1:8181/ 
        ProxyRequests Off
        ProxyVia Off
    </VirtualHost>

Starta om apache:

    sudo apachectl restart

### GUI-användning

wdt kör genom ett minimalistiskt GUI lokalt på default: http://localhost:7171/static/index.html
Labbmiljön (intygspoc): https://intygspoc.inera.nordicmedtest.se/dbtool/static/

![alt text](dbtool.png))
