## Grundprovisionering med Ansible

    ansible-playbook -i [inventory-fil] provision.yml
    
Exempelvis:

    ansible-playbook -i hosts_lab provision.yml
    
#### Hantera problem med MySQL
I något fall har MySQL inte kunnat installeras pga en påstådd yum-konflikt med MariaDB. Detta går att lösa genom att på målmiljön lägga till följande rad i _/etc/yum.conf_:

    exclude=mariadb*
    
Skulle detta bli en återkommande problem gör vi förslagsvis ett ansible-steg som modifierar filen före MySQL installeras.    
    
### DB tool

Specifikt för LAB-miljön (intygspoc) så kan man installera ett verktyg för att enkelt ta och återställa databassnapshots. Se /tools/dbtool

Vid behov kan man omprovisionera "dbtool" genom att köra följande playbook:

    ansible-playbook -i hosts_lab provision_dbtool.yml
    
Notera att vi har en förbyggd zip med amd64/linux binär samt GUI-applikationen (dbtool-1.0.zip) i /roles/dbtool/templates. Om man gör några ändringar i dbtool så får man bygga om den och paketera ihop en ny zip för hand som skall innehålla:

    /dbtool
    /static
    
Konfigurationen hanteras i roles/dbtool/templates/preferences.yml som kopieras ut separat.
