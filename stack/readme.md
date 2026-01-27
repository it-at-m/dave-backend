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
2. Mit der Datenbank dave-db verbinden, Login mit Nutzer **dave**. Passwort **1234**
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

# DAVe sample stack

DAVe __sollte__ in Produktionsumgebungen via [helm chart](https://artifacthub.io/packages/helm/it-at-m/dave?modal=install) installiert und betrieben werden.

Aber für einen ersten Einblick in das, was DAVe zu bieten hat oder für Entwicklungsumgebungen stellen wir eine 
[docker-compose](https://github.com/it-at-m/dave-backend/blob/sprint/stack/docker-compose.yml)-Datei zur Verfügung, 
die neben der benötigten Infrastruktur auch Frontend und Backend mit einem Beispieldatensatz einer Zählstelle startet.

Folgende Schritte sind hierfür nötig:

1. Docker und Docker Compose installieren: Sie müssen Docker und Docker Compose auf Ihrem System installiert haben.
   Wenn Sie diese noch nicht installiert haben, können Sie die offizielle Dokumentation befolgen, um sie zu installieren.
   Mit den folgenden Befehlen können Sie überprüfen, ob sie installiert sind:
```
docker --version
docker compose --version
```
Alternativ zu Docker kann auch [Podman](https://podman.io/) verwendet werden.

2. Infrastruktur starten: Die Infrastruktur-Container können mit folgenden Befehlen gestartet werden:
```
cd stack
source .env
docker compose up
```

3. Elasticsearch Zertifikat einbinden: Folgen Sie den Anweisungen unter [Erzeugen des Fingerprints](#erzeugen-des-fingerprints),
   um das von Elasticsearch verwendete Zertifikat für die verschlüsselte Verbindung zu erhalten.
   Dann fügen Sie den Fingerprint als Umgebungsvariable ELASTICSEARCH_CERT_FINGERPRINT im [.env-File](https://github.com/it-at-m/dave-frontend/blob/sprint/stack/.env) ein
   und lesen die Datei nochmal ein:
```
source .env
```

4. Backend und Frontend hochfahren: Fahren Sie die beiden Container wie folgt hoch:
```
docker compose --profile sample-stack up
```

5. Auf die Anwendung zugreifen: Sobald alle Container betriebsbereit sind, können Sie die DAVe UI verwenden,
   indem Sie im Browser zu http://localhost:8082 navigieren.

Das war's! Sie haben den Anwendungsstack für DAVe erfolgreich installiert und gestartet.

# Autorisierung via Keycloak

Anwendungen, die auf der LHM-eigenen [Referenzarchitektur](https://refarch.oss.muenchen.de) aufgebaut sind (und dazu gehört DAVe), 
nutzen für Authentifizierung und Autorisierung OAuth 2.0 und OpenID Connect (siehe https://refarch.oss.muenchen.de/cross-cutting-concepts/security.html#security).
Intern verwenden wir für DAVe die Open-Source Identitäts- und Zugriffsmanagement-Lösung Keycloak.

Wie Sie eine eigene Keycloak-Instanz neben dem DAVe-Stack zur Verfügung stellen können, entnehmen Sie bitte den Anweisungen 
in der RefArch-Dokumentation zur [Container-Engine](https://refarch.oss.muenchen.de/templates/develop.html#container-engine).

Um die Instanz so zu konfigurieren, dass sie für DAVe als IAM-Schicht arbeiten kann, können Sie folgende Schritte durchführen: 

1) Legen Sie ein Keycloak-Realm mit Namen "Dave" in Keycloak an
2) Importieren Sie den DAVe-Client aus der Datei "sso-config/sso-client.json" in das angelegte Realm
3) Importieren Sie die grundlegenden Authorizations aus der Datei "sso-config/sso-authorisation.json" in den Client
4) Bei Bedarf fügen Sie dem neuen Client neue User hinzu
5) Erstellen Sie die Client Roles, die hier definiert sind: https://github.com/it-at-m/dave/blob/sprint/docs/src/de/SysSpec-arc42.md#security
6) Weisen Sie Ihren Usern die entsprechende Rolle zu
7) Konfigurieren Sie folgende SSO-Properties mit der Adresse Ihrer Keycloak-Instanz:
- Für Backend und Integrationsanwendungen: 
  - ${keycloak.auth-server-url} bzw. 
  - ${keycloak.auth-server-url.dave}
- Für die Frontends: 
  - ${spring.cloud.gateway.routes.0.uri}
  - ${spring.security.oauth2.client.provider.keycloak.issueruri}
  - ${spring.security.oauth2.resource.userinfouri}
  - ${spring.security.oauth2.resourceserver.jwt.issueruri}
  - ${spring.security.oauth2.resourceserver.jwt.jwkseturi}
  - ${spring.security.oauth2.client.provider.keycloak.token.uri}


