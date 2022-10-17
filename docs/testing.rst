Testy manualne
==============

Testy, które sprawdzają, czy działa ładowanie mapowań z pliku i czy na podstawie tych mapowań działa import danych do
bazy Neo4j.

Generacja pliku JAR:
::
	./gradlew social-network-data-migration-cli:jar && \
	cp -v ./social-network-data-migration-cli/build/libs/SocialNetworkDataMigrationCLI.jar .

Przed każdym z testów wyczyść bazę neo4j operacją:
::
	MATCH (n) DETACH DELETE n

TEST: Import danych z bazy Salon24 (PostgreSQL)
::
	java -jar SocialNetworkDataMigrationCLI config_examples/salon24.properties config_examples/salon24_mapping.json

TEST: Import danych z pliku CSV z nagłówkami
::
	java -jar SocialNetworkDataMigrationCLI --csv config_examples/csv.properties \
	social-network-data-migration-core/src/test/resources/test.csv \
	social-network-data-migration-core/src/test/resources/csv_with_headers.json

TEST: Import danych z pliku CSV bez nagłówków
::
	java -jar SocialNetworkDataMigrationCLI --csv config_examples/csv.properties \
	social-network-data-migration-core/src/test/resources/test.csv \
	social-network-data-migration-core/src/test/resources/csv_no_headers.json --no-headers

TEST: Tryb interaktywny SQL
::
	java -jar SocialNetworkDataMigrationCLI --i config_examples/salon24.properties

TEST: Tryb interaktywny CSV z nagłówkami
::
	java -jar SocialNetworkDataMigrationCLI --csv --i config_examples/csv.properties \
	social-network-data-migration-core/src/test/resources/test.csv

TEST: Import danych z pliku CSV bez nagłówków
::
	java -jar SocialNetworkDataMigrationCLI --csv --i config_examples/csv.properties \
	social-network-data-migration-core/src/test/resources/test.csv --no-headers

TODO import danych z Huffington Post, XML w dwóch wariantach
