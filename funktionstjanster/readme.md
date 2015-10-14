
mallar mappen
-----------
Innehåller alla mallar, css och bilder för eleginloggningen. Utgår från grpx2.zip som laddats ner från 
utvecklingsportalen för funktionstjanster.

dev mappen
-----------
Innehåller ett snabbt ihopslängt php-skript som parsar de speciella PARM{} taggar som mallarna använder. För att
köra detta lokalt lägg alla mallar tillsammans med .htacess och process.php filerna i en mapp och peka ut den för apache
med mod_rewrite och php aktiverat. Rewrite reglerna gör att du sedan kan gå till t.ex. localhost/index.html för att se 
den sidan sammansatt.

Installation
-----------
För att installera dessa i CGIs miljö mailade jag en zippad fil av innehållet i mallar mappen till
funktionstjanster@cgi.com tillsammans med SP-metadatafilen för tjänsten den skulle kopplas till.
