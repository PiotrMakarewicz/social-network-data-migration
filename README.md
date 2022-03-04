# System for migrating social network data from heterogeneous sources to a graph database

BSc thesis. Authors: Gabriel Kępka, Piotr Makarewicz

## Configuration (Ubuntu)

### PostgreSQL database

PostgreSQL is added to Ubuntu by default, so all we need to do is import the data from an SQL file. `[filename]` stands for the name of .sql file with database dump.

```shell
sudo -u postgres psql -c "CREATE ROLE sna_user; GRANT sna_user TO postgres;"
sudo -u postgres psql -c "CREATE DATABASE socialdata;"

sudo -u postgres psql socialdata < [filename]
```
This will create a new database `socialdata` and populate it with data. Every user using `socialdata` database must be granted the `sna_user` role.

It might be useful to set a password to `postgres` user. Replace `[password]` in the command below with a password of your choice.
```
sudo -u postgres psql -c "ALTER USER postgres PASSWORD '[password]';"
```

Now you can use database management software (for example DataGrip) to connect to the database using user-password authentication and execute queries.
