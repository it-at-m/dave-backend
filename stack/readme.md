Anleitung zum Einrichten von Dave mittels docker-compose

# Erklärungen
## .env-File
Im File .env sind drei Variablen für die docker-compose.yml hinterlegt:
1. ELASTIC_PASSWORD definiert das Passwort für ElasticSearch. Dies wird bei der Erstellung der Zertifikate benötigt.
2. KIBANA_PASSWORD definiert das Passwort für Kibana. Dies wird bei der Erstellung der Zertifikate benötigt.
3. STACK_VERSION gibt die Version vom ELK-Stack vor.

## pgadmin
Enthält die Konfiguration für pgAdmin zum automatischen Verbinden mit der Datenbank.

# Allgemein
1. Podman starten
2. docker-compose.yml starten (docker compose up)

Beim ersten Start der docker-compose.yml wird je ein persistentes Volume für folgende Bereiche angelegt. Diese überstehen den Neustart des Rechners und des Containers.
* pg_data: Enthält die Daten der Postgre-Datenbank
* elastic-certs: Enthält die generierten Zertifikate zur Kommunikation mit Elasticsearch
* elastic-data: Enthält die Daten der Elasticsearch-Indices
* elastic-backup: Enthält die Daten zum Restore der Elasticsearch-Indices
* kibana-data: Enthält die Daten von Kibana

# Backup in Postgre einspielen
1. Im Browser http://localhost:5050/ aufrufen (PG4Admin)
2. Mit der Datenbank dave-db verbinden
3. Unter Schemas die beiden Schemata **dave** und **dave_ng** anlegen
4. Rechte Maustaste auf eines der neuen Schemata und **Restore** auswählen
5. Unter Filename das Icon rechts anklicken, dann auf die 3 Pünktchen gehen und Upload auswählen. 
6. Hier die beiden Backup-Files **dave-schema und **dave-ng-schema** hochladen. 
7. Zuletzt nun im jeweiligen Schema das passende Backup auswählen uns einspielen.

# Backup mittels Kibana einspielen
## Backup vorberieten (WSL)
1. WSL als podman-machine-default starten
2. Verzeichnis wechseln ins Volume vom Backup: cd /home/user/.local/share/containers/storage/volumes/dave-stack_elastic-backup
3. Rechte des Ordners _data anpassen: sudo chmod 777 _data
4. Alle Files des Backups nach _data kopieren: cp -r /mnt/c/path/to/backup/backup_es/* _data

## Snapshot mittels Kibana einspielen
1. Im Browser http://localhost:5601 aufrufen
2. Login mit Nutzer **elastic**. Passwort **changeme**
3. Management -> Stack Management -> Snapshot and Restore wechseln (http://localhost:5601/app/management/data/snapshot_restore/snapshots)
4. Einmalig das Repository einrichten (register repository)
   1. Beliebigen Namen vergeben und Shared file system auswählen. Weiter mit next 
   2. Unter Location folgendes eintragen: /usr/share/elasticsearch/backup , Readonly repository aktivieren und weiter mit register.
   3. Im Reiter Snapshots auf den Name des Backup klicke und restore auswählen
   4. Unter Data Streams and indices alle abwählen und nur die Indices von Dave aktivieren.
   5. Zweimal weiter mit next, dann auf restore snapshot klicken
   6. Wenn der Restore fertig ist, dann erscheinen unter Data -> Index Management die importierten Indices
      (http://localhost:5601/app/management/data/index_management/indices)

# Erzeugen des Fingerprints
Damit das Backend mit dem Elasticsearch-Server kommunizieren kann, wird der Fingerprint des Zertifikats benötigt.
1. Podman -> Containers -> elastic-1 -> terminal
2. cd config/certs/elastic
3. openssl x509 -fingerprint -sha256 -in elastic.crt
4. Fingerprint kopieren und in application-local.yml einfügen.

# Anpassungen application-local.yml
Bevor das Backend gestartet werde kann muss die application-local agepasst werden.
* db.schema:dave_ng
* datasource:
* * url:jdbc:postgresql://localhost:5432/dave-db
* * username:dave
* * password:1234
* elasticsearch:
* * password:changeme
* * http-ca-certificate: <generate>