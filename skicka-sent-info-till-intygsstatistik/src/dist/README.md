Applikationen gör det möjligt att kunna skicka om info ett intyg är skickat till en mottagare mottagaren från Intygstjänsten (IT) till Intygsstatistik (ST) genom att IT skickar meddelanden till ST på kö som informationen vanligtvis skickas. På så vis kan kod för att ta emot informationen återanvändas i ST.

### Exekvering
Applikationen startas genom att användaren exekverar bash-skriptet **run.sh**
> $ ./run.sh

### Konfigurering:
För att kunnan använda applikationen mot tänkt miljö finns ett flertal parametrar som kan sättas i **run.sh**. Några parameterar är obligatoriska och andra kan lämnas tomma.

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

Observera att användarnamn och lösenord vanligtvis måste sättas för databas och köhanterare.

##### Exempel på konfiguration

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

