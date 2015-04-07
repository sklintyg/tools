De anonyiserade dumpar av produktionsdata vi har tillgång till ger adekvat
testdata med avseende på volymer och spridning. Ett problem vi har är dock
att de riktiga vårdeneheter och vårdgivare som intygen är utfärdade på inte
finns i test-hsa. Därmed är de inte utan vidare användbara för tester.

Denna applikation "kapar" alla intyg utfärdade på en viss ursprungs-enhet,
genom att ändra utfärdande enhet och vårdgivare till angivna värden. Därmed
kan man får en delmängd intyg som överensstämmer med en enhet som finns i
test-hsa. 

Konfiguration av ursprungs-enhet och önskad enhet och vårdgivare görs i
hsa.properties. Val av ursprungs-enhet att kapa kan göras godtyckligt.