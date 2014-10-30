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

## Licens

Copyright (C) 2014 Inera AB (http://www.inera.se)

Intyg Common is free software: you can redistribute it and/or modify it under the terms of the GNU LESSER GENERAL PUBLIC LICENSE as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Intyg Common is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU LESSER GENERAL PUBLIC LICENSE for more details.

Se även [LICENSE.txt](https://github.com/sklintyg/common/blob/master/LICENSE.txt). 
