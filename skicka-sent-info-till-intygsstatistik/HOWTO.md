Applikationen gör det möjligt att kunna skicka om info ett intyg är skickat till en mottagare mottagaren från Intygstjänsten (IT) till Intygsstatistik (ST) genom att IT skickar meddelanden till ST på kö som informationen vanligtvis skickas. På så vis kan kod för att ta emot informationen återanvändas i ST.

### Distribution
I root-katalogen av projektet finns ett bash-skript (**distribution.sh**) vars uppgift är att skapa en distribution (zip-fil) med de filer som behövs för att kunna använda applikationen. För att skapa en distribution körs:
> $ ./distribution.sh

Den skapade zip-filen hittas sedan under _'build/distributions'_.

### Vid utveckling
Applikationen byggs genom att köra:
> $ ./gradlew build

Applikationen startas genom att köra:
> $ ./gradlew

> Den konfiguration som då används är den som är applikationens default-konfigurering  och som är definierad i filen _'app.properties'_. Den konfigureringen är uppsatt för att köra mot Intygstjänstens in-memory databas och mot virtuella köer och används bara vid utveckling.

Det finns ett shell-skript som kan också kan användas för att starta applikationen.
> $ ./test.sh

> Den konfiguration som då används är den som sätts inuti själva shell-skriptet, dvs **test.sh**. All variabler som är definierade i 'test.sh' kommer att skriva över applikationens default-variabler. Skriptet finns bara där som bekvämlighet och användes först för att simulera en tänkt målmiljö.

För att publicera till Nexus körs:
> $ ./gradlew uploadArchives

> Observera att miljö-variablerna *ineraNexusUsername* och *ineraNexusPassword* måste sättas för autentisering mot Nexus.

### Konfigurering:
Applikationen har en default-konfigurering som är satt i filen _'app.properties'_. Den konfigureringen är uppsatt för att köra mot Intygstjänstens in-memory databas och mot virtuella köer och används bara vid utveckling.

För att kunnan använda applikationen med andra utvecklingsmiljöer finns ett flertal parametrar som kan sättas i **test.sh** vilket ger möjlighet till en konfiguration som passar den utvecklingsmiljö som används.

##### Parametrar som måste ha värden
		DB_URL=
		DB_DRIVER=
		BROKER_URL=
		BROKER_QUEUE=

##### Parametrar som inte behöver ha värden
		DB_USERNAME=
		DB_PASSWORD=
		BROKER_USERNAME=
		BROKER_PASSWORD=
		DATE_FROM=
		DATE_TO=
		DEBUG=

Nedan är ett exempel på konfiguration som använder MySQL som datakälla och där alla skickat inforamtion som kommit in till IT mellan datumen 2016-12-01 och 2017-08-31 ska skickas till ST.

		DB_URL="jdbc:mysql://localhost:3306/intyg?useCompression=true"
		DB_DRIVER="com.mysql.jdbc.Driver"
		DB_USERNAME="abcde"
		DB_PASSWORD="12345"
		BROKER_URL="tcp://localhost:61616"
		BROKER_QUEUE="intyg.statistik.test"
		BROKER_USERNAME="fghij"
		BROKER_PASSWORD="67890"
		DATE_FROM="2016-12-01"
		DATE_TO="2017-08-31"
		DEBUG=false

Önskar man att skicka alla information som finns i IT till ST så lämnas DATE_FROM och DATE_TO tomma:

		DATE_FROM=
		DATE_TO=
