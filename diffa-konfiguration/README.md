# Diffa konfiguration mellan STAGE och PROD

I denna katalog ligger några shell-script för att enkelt producera diffar mellan config maps i STAGE respektive PROD.

Output från respektive script är dels faktisk konfig i form av filer, samt diff för respektive output genererad med "diff".

Båda scripten _kräver_:
  
* VPN anslutet mot Basefarm
* Programmet "diff" på PATH. (Standard i Linux och OS X)
* oc-klient i den folder man står i*
* Inloggad på prod-klustret med oc login <url>

(*) Modifiera annars scripten så det kommer åt "oc" utan prefix.

### diffconfigs.sh

Detta script exporterar respektive applikations "envvar" i YAML-format för "sintyg" och "pintyg". Filerna lagras i _/exports_ och diffarna lagras i _/result_

### diffmaps.sh

Detta script exporterar respektive applikations "config map" med _filer_, dvs det som vi i normala fall har i /devops/openshift/[env]/config. De exporterade filerna hamnar i _/filesexport_ och diffarna mellan sintyg och pintyg hamnar i _/filesexportresult_.