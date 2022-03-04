# System for migrating social network data from heterogeneous sources to a graph database

BSc thesis. Authors: Gabriel Kępka, Piotr Makarewicz

## Configuration (Ubuntu)

### PostgreSQL database

PostgreSQL is added to Ubuntu by default, so all we need to do is import the data from an SQL file. `[filename]` stands for the name of .sql file with database dump.

```shell
sudo -u postgres psql -c 'CREATE ROLE sna_user; GRANT sna_user TO postgres; CREATE DATABASE socialdata;'
sudo -u postgres psql socialdata < [filename]
```
