Configuration (Ubuntu)
======================

PostgreSQL database - Salon24 data import
----------------

PostgreSQL is added to Ubuntu by default, so all we need to do is import the data from an SQL file. First we need to create new `sna_user` role with `LOGIN` attribute, which will be an owner of database.

::

  sudo -u postgres psql -c "CREATE ROLE sna_user LOGIN; ALTER ROLE sna_user PASSWORD 'password';"

By default PostgreSQL uses `peer` authentication method for local connections, i.e. comparing operating system user name with database user name and allowing to log in only if they match. To enable login and password authentication we need to modify `/etc/postgresql/14/main/pg_hba.conf` configuration file, changing `METHOD` field value from `peer` to `md5`:

::

  # TYPE  DATABASE        USER            ADDRESS                 METHOD
  
  # "local" is for Unix domain socket connections only
  local   all             all                                     md5

PostgreSQL service has to be restarted to initialize new configuration:

::

  systemctl restart postgresql


Then we create a new database `salon24` and populate it with data.  `[filename]` stands for the name of .sql file with database dump.

::

  sudo -u postgres createdb -O sna_user salon24
  psql -U sna_user salon24 < [filename]


Now you can use database management software (for example DataGrip) to connect to the database using user-password authentication and execute queries.

PostgreSQL data import - Huffington Post
----------------
`[filename]` stands for the name of a .backup file with database backup

::

  sudo -u postgres pg_restore -d huffington < [filename]


Neo4j database - local installation 
----------------
Add repository:
::

  wget -O - https://debian.neo4j.com/neotechnology.gpg.key | sudo apt-key add -
  echo 'deb https://debian.neo4j.com stable latest' | sudo tee /etc/apt/sources.list.d/neo4j.list
  sudo apt-get update


Install Neo4j Community Edition:
::

  sudo apt-get install neo4j


Start systemd service:
::
  systemctl start neo4j

Neo4j authentication setup
---------------------------

When first starting a Neo4j DBMS, there is always a single default user neo4j with administrative privileges. To set an initial password for default user execute the following command:
::
  sudo neo4j-admin set-initial-password [password]


After completing steps above you will be able to connect to database using Neo4j Browser available at address http://localhost:7474 or using `cypher-shell`:
::

  cypher-shell -u neo4j -p


Loading data from PostgreSQL to Neo4j using APOC library and JDBC
----------------
Install APOC plugin:
::

  wget https://github.com/neo4j-contrib/neo4j-apoc procedures/releases/download/4.4.0.3/apoc-4.4.0.3-all.jar
  sudo mv apoc-4.4.0.3-all.jar /var/lib/neo4j/plugins/


Install PostgreSQL JDBC connector:
::

  wget https://jdbc.postgresql.org/download/postgresql-42.3.3.jar
  sudo mv postgresql-42.3.3.jar /var/lib/neo4j/plugins/
 
 
Tutorial on how to use APOC library is available [here](https://www.youtube.com/watch?v=e8UfOHJngQA&list=PL9Hl4pk2FsvXEww23lDX_owoKoqqBQpdq&index=5).


In case of problems running Neo4j service on a Linux machine, you can check the service logs using:
::

  sudo journalctl -u neo4j.service
