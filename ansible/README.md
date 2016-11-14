## Grundprovisionering med Ansible

    ansible-playbook -i [inventory-fil] provision.yml
    
Exempelvis:

    ansible-playbook -i hosts_lab provision.yml
    
### Lab-miljön och delade resurser

Se hosts_lab

På lab-servern har vi både Webcert och Intygstjänsten. Ett litet problem med vår grundprovisionering är att den inte är anpassad för att köra flera applikationer på samma host. Kör man exempelvis Apache-provisionering per applikation så kommer skriva över "den andres" virtualhost.conf och ports.conf. Därför provisionerar vi apache _endast_ för Webcert så får man sedan göra manuell handpåläggning om man vill. Detsamma gäller för MySQL och ActiveMQ.

Det enda som måste installeras för båda applikationerna är Tomcat. Detta fungerar fint.

### DB tool

Specifikt för LAB-miljön (intygspoc) så kan man installera ett verktyg för att enkelt ta och återställa databassnapshots. Se /tools/dbtool

Vid behov kan man omprovisionera "dbtool" genom att köra följande playbook:

    ansible-playbook -i hosts_lab provision_dbtool.yml
    
Notera att vi har en förbyggd zip med amd64/linux binär samt GUI-applikationen (dbtool-1.0.zip) i /roles/dbtool/templates. Om man gör några ändringar i dbtool så får man bygga om den och paketera ihop en ny zip för hand som skall innehålla:

    /dbtool
    /static
    
Konfigurationen hanteras i roles/dbtool/templates/preferences.yml som kopieras ut separat.
