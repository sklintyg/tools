# Intyg Tools
Intyg Tools tillhandahåller verktyg för att migrera data, uppdatera och anonymisera databaser och liknande uppgifter.

## Utvecklingssetup
Intyg Tools innehåller flera olika underprojekt som byggs separat beroende på vilket verktyg man är intresserad av. Alla projekt byggs med hjälp av Maven enligt följande:

```
$ git clone https://github.com/sklintyg/tools.git

$ cd tools/anonymisering
$ mvn install

$ cd ../liquibase-runner
$ mvn install
```

## Byggverktyg
Förutom ovanstående verktyg innehåller projektet även en mapp som heter `build`. Om man clonar alla projekt under [SKL Intyg](http://github.com/sklintyg) under en gemensam katalog så kan man använda `tools/buildAll.sh` för att bygga samtliga projekt i korrekt ordning.

## Licens
Copyright (C) 2014 Inera AB (http://www.inera.se)

Intyg Tools is free software: you can redistribute it and/or modify it under the terms of the GNU LESSER GENERAL PUBLIC LICENSE as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Intyg Tools is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU LESSER GENERAL PUBLIC LICENSE for more details.

Se även [LICENSE.md](https://github.com/sklintyg/common/blob/master/LICENSE.md). 
