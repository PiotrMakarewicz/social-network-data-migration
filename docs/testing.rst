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
	java Application 'config_examples/salon24.properties' 'salon24_mapping.json'


TEST: Import danych z pliku CSV
---------------------------------
::
	java Application 'config_examples/csv.properties' 'csv_mapping.json'


TODO import danych z Huffington Post, XML w dwóch wariantach

