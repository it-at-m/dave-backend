# Backend (Datenportal):

## About the project

Dieses Repository ist eine der 5 Komponenten der Anwendung "DAVe" (Datenbank und Auswertung für Verkehrszählungen).

Es müssen nicht zwingend alle 5 Komponenten gleichzeitig verwendet werden. Allerdings bildet dieses Repository den Kern der Anwendung und ist Voraussetzung für alle anderen Bestandteile.

DAVe besteht aus folgenden Repositories:

* **Backend (Datenportal)**: Beinhaltet die Business Logik für Frontend, Adminportal, Selfserviceportal und EAI. Bildet den Kern der Anwendung. 
* **Frontend (Datenportal)**: Das Datenportal bietet einen lesenden Zugriff auf die Zählungen. Es kann nach Zählungen gesucht werden (auch auf einer Karte). Hat ein Nutzer eine Zählung, bzw. eine Zählstelle gefunden, so kann in dieser eine umfangreiche Datenanalyse betrieben werden. [Repository](https://github.com/it-at-m/dave-frontend)
* **Adminportal**: Das Adminportal ist den Administratoren der Anwendung vorbehalten. Hier ist der komplette Workflow um eine Zählstelle, oder eine Zählung anzulegen abgebildet. Auch die Kommunikation mit dem Zähldienstleister wird über dieses Portal abgewickelt. [Repository](https://github.com/it-at-m/dave-admin-portal)
* **Selfserviceportal**: Das Selfserviceportal ist dem Zähldienstleister vorbehalten. Dort sieht der Dienstleister Aufträge für neue Zählungen, kann Metadaten zu einer Zählung pflegen und die Zähldaten hochladen. Das Selfserviceportal kann auch mit mehreren verschiedene Zähldienstleister betrieben werden. [Repository](https://github.com/it-at-m/dave-selfservice-portal)
* **EAI**: Um Schnittstellen zu anderen Systemen innerhalb der LHM zur Verfügung zu stellen, gibt es die Möglichkeit direkt Daten als CSV-Datei zu bekommen. Folgende Funktionen werden angeboten:
	* Ausgabe aller Zählstellen mit Koordinaten als CSV-Datei
	* Ausgabe der Spitzenstunde einer bestimmten Zählung als CSV-Datei
	* Daten aller Zählstellen und Zählungen des angegebenen Monats werden im JSON-Format zurückgegeben

[EAI-Repository](https://github.com/it-at-m/dave-eai)

Besonders ist die Aufteilung der Daten. Alle Daten, die relevant für die Suche sind (Stammdaten), werden in Elasticsearch gespeichert. Die Bewegungsdaten - im Fall von DAVe die Zähldaten - werden in einer relationalen Datenbank (bei der LHM: Oracle) vorgehalten. Um die Ladegeschwindigkeit zu erhöhen, werden bereits beim Speichern der Zähldaten diverse Berechnungen durchgeführt und die vorberechneten Ergebnisse zum direkten Abruf in der Datenbank hinterlegt. Hier kommt auch eine KI-Komponente zum Einsatz, die die Hochrechnung von Kurzzeitzählungen auf den ganzen Tag übernimmt (bisher nur bei Radzählungen).
Die Frontends sind jeweils Vue Single Page Applications, die über ein Service Gateway mit dem Backend kommunizieren. DAVe besteht nur aus einem einzigen Spring Service.


## Built with
    Java 11

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are greatly appreciated.

If you have a suggestion that would make this better, please open an issue with the tag "enhancement", fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement". Don't forget to give the project a star! Thanks again!

    Open an issue with the tag "enhancement"
    Fork the Project
    Create your Feature Branch (git checkout -b feature/AmazingFeature)
    Commit your Changes (git commit -m 'Add some AmazingFeature')
    Push to the Branch (git push origin feature/AmazingFeature)
    Open a Pull Request

## License

Distributed under the MIT License. See LICENSE for more information.
## Contact

it@m - opensource@muenchen.de
