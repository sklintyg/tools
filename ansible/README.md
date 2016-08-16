## Grundprovisionering med Ansible

    ansible-playbook -i [inventory-fil] provision.yml
    
Exempelvis:

    ansible-playbook -i hosts_lab provision.yml
    
Kom ihåg att git-crypt behöver installeras separat:

    ansible-playbook -i hosts_lab provision_git-crypt.yml
    
    
### Lab-miljön och delade resurser

Se hosts_lab

På lab-servern har vi både Webcert och Intygstjänsten. Ett litet problem med vår grundprovisionering är att den inte är anpassad för att köra flera applikationer på samma host. Kör man exempelvis Apache-provisionering per applikation så kommer skriva över "den andres" virtualhost.conf och ports.conf. Därför provisionerar vi apache _endast_ för Webcert så får man sedan göra manuell handpåläggning om man vill. Detsamma gäller för MySQL och ActiveMQ.

Det enda som måste installeras för båda applikationerna är Tomcat. Detta fungerar fint.