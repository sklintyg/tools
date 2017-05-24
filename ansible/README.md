## Grundprovisionering med Ansible

    ansible-playbook -i [inventory-fil] provision.yml
    
Exempelvis:

    ansible-playbook -i hosts_lab provision.yml
    
    
# Provisionera lokalt med Vagrant

### Krav:
- Virtualbox installerat
- Vagrant installerat

#### Kommandon
(Instruktioner för OS X)

##### 1. Generera upp en Vagrantfile
_(Byt ut "myuser" mot ditt användarnamn.)_

    mkdir -p /Users/myuser/vagrant/centos68
    cd /Users/myuser/vagrant/centos68
    vagrant init bento/centos-6.8
    
##### 2. Öppna Vagrantfile och kommentera in:
 
     _config.vm.network "public_network"   
   
Justera ev. också minnesinställningarna till 2048 genom att kommentera in och modifiera:

    config.vm.provider "virtualbox" do |vb|
    #   # Display the VirtualBox GUI when booting the machine
    #   vb.gui = true
    #
    #   # Customize the amount of memory on the VM:
       vb.memory = "2048"
    end
    
##### 3. Skapa virtuella maskinen
   
    vagrant up --provider virtualbox
    
Efter en stund får man manuellt ange vilket nätverksinterface man vill binda mot. 

Svara motsv. 1) så bör burken få en IP-adress på samma LAN som hostdatorn. 
    
    ==> default: Available bridged network interfaces:
    1) en0: Wi-Fi (AirPort)
    2) en1: Thunderbolt 1
    3) en2: Thunderbolt 2
    4) p2p0
    5) awdl0
    6) bridge0
    
Vänta tills allt är klart.    
    
##### 4. ssh:a in och anteckna ip-numret   
    
    vagrant ssh
    ifconfig
    
Anteckna IP-numret för eth1, borde vara något i stil med 192.168.0.189

    exit

##### 5. Börja grundprovisionera med ansible

Byt ut IP-numret i exemplet nedan mot det IP-nummer du skrev ned ovan.
    
    cd $INTYG_HOME/tools/ansible
    ansible-playbook -i inventory/webcert/dev provision.yml --extra-vars "vagrantIp=192.168.0.189 vagrantPort=22 keyFile=~/.ssh/vagrant"

Detta tar en stund...

##### 6. Provisionera webcert
Byt mapp till /webcert/ansible

Om du _inte_ har git-crypt nyckeln, be en snäll kollega om hjälp. Den behöver placeras i samma mapp du befinner dig i. Får absolut _inte_ commitas till något repo!!!

Samma sak igen med IP-numret.

    ansible-playbook -i inventory/webcert/dev provision.yml --extra-vars "vagrantIp=192.168.1.22 vagrantPort=22 keyFile=~/.ssh/vagrant"

##### 7. Deploya webcert från lokal burk
Notera att vi specificerar version=0-SNAPSHOT som parameter, du måste alltså ha byggt webcert lokalt innan!
    
    ansible-playbook -i inventory/webcert/dev deploy.yml --extra-vars "vagrantIp=192.168.1.22 vagrantPort=22 keyFile=~/.ssh/vagrant version=0-SNAPSHOT"

    
## 8. Klart! 
  
Nu borde du kunna köra Webcert mot t.ex. https://192.168.0.189/welcome.html
    
# Övrigt
    
## Hantera problem med MySQL
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
