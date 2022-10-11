Testy manualne
==============

Testy, które sprawdzają, czy działa ładowanie mapowań z pliku i czy na podstawie tych mapowań działa import danych do
bazy Neo4j.

Przed każdym z testów wyczyść bazę neo4j operacją:
::
	MATCH (n) DETACH DELETE n

TEST: Import danych z bazy Salon24 (PostgreSQL)
-------------------------------------------------
::
	java -jar Application config_examples/salon24.properties config_examples/salon24_mapping.json


TEST: Import danych z pliku CSV z nagłówkami
---------------------------------
::
	java -jar Application --csv config_examples/csv.properties src/test/java/resources/test.csv
src/test/resources/csv_with_headers.json

TEST: Import danych z pliku CSV bez nagłówków
---------------------------------
::
	java -jar Application --csv config_examples/csv.properties src/test/java/resources/test.csv
src/test/resources/csv_no_headers.json --no-headers


TODO import danych z Huffington Post, XML w dwóch wariantach

