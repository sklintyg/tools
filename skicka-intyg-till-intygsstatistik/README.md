Applikationen gör det möjligt att kunna skicka intyg av en viss typ från Intygstjänsten (IT) till Intygsstatistik (ST) genom att  IT skickar intyg av önskade typer till ST via den kö som intyg vanligtvis skickas. På så vis kan kod för att ta emot intyg återanvändas i ST.

### Distribution
I root-katalogen av projektet finns ett bash-skript (**distribution.sh**) vars uppgift är att skapa en zip-fil med de filer som behövs för att kunna använda applikationen.
> $ ./distribution.sh

### Exekvering
Applikationen startas genom att användaren exekverar bash-skriptet **run.sh**
> $ ./run.sh

### Konfigurering:
Applikationen har en default-konfigurering som är satt i filen **app.properties**. Den konfigureringen är uppsatt för att köra mot Intygstjänstens in-memory databas och mot virtuella köer.

För att kunnan använda applikationen mot andra miljöer finns ett flertal parametrar som kan sättas i **run.sh** för att  få till den konfiguration som passar den miljö som används.

##### Parametrar som måste ha värden
		DB_URL=
		DB_DRIVER=
		DB_USERNAME=
		DB_PASSWORD=
		BROKER_URL=
		BROKER_QUEUE=

##### Parametrar som inte behöver ha värden
		INTYGSTYPER=
		DATE_FROM=
		DATE_TO=
		DEBUG=

Nedan är ett exempel på konfiguration som använder MySQL som datakälla och där alla intyg med intygstyperna luse och fk7263 som kommit in till IT mellan datumen 2016-12-01 och 2017-08-31 ska skickas till ST.

		DB_URL="jdbc:mysql://localhost:3306/intyg?useCompression=true"
		DB_DRIVER="com.mysql.jdbc.Driver"
		DB_USERNAME="abcde"
		DB_PASSWORD="12345"
		BROKER_URL="tcp://localhost:61616"
		BROKER_QUEUE="intyg.statistik.test"
		INTYGSTYPER="'luse','fk7263'"
		DATE_FROM="2016-12-01"
		DATE_TO="2017-08-31"
		DEBUG=false

Vill man skicka alla intyg som finns i IT till ST så lämnas INTYGSTYPER, DATE_FROM och DATE_TO tomma:

		INTYGSTYPER=
		DATE_FROM=
		DATE_TO=
