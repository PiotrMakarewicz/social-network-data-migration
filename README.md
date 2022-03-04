# System for migrating social network data from heterogeneous sources to a graph database

BSc thesis. Authors: Gabriel Kępka, Piotr Makarewicz

## Configuration (Ubuntu)

### PostgreSQL database

PostgreSQL is added to Ubuntu by default, so all we need to do is import the data from an SQL file.

```shell
sudo -u postgres psql -f <filename>
```


