## Grundprovisionering med Ansible

    ansible-playbook -i [inventory-fil] provision.yml
    
Exempelvis:

    ansible-playbook -i hosts_lab provision.yml
    
### DB tool

Specifikt för LAB-miljön (intygspoc) så kan man installera ett verktyg för att enkelt ta och återställa databassnapshots. Se /tools/dbtool

Vid behov kan man omprovisionera "dbtool" genom att köra följande playbook:

    ansible-playbook -i hosts_lab provision_dbtool.yml
    
Notera att vi har en förbyggd zip med amd64/linux binär samt GUI-applikationen (dbtool-1.0.zip) i /roles/dbtool/templates. Om man gör några ändringar i dbtool så får man bygga om den och paketera ihop en ny zip för hand som skall innehålla:

    /dbtool
    /static
    
Konfigurationen hanteras i roles/dbtool/templates/preferences.yml som kopieras ut separat.
