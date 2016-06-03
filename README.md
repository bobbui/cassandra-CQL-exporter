# Summary
A highly configurable utility to export whole Apache Cassandra keyspace or table structure and data to CQL scripts.
Support Cassandra > 3.0.
Required Java 8 in order to work.

# Usage
usage: cql-export [--drop] [-f <file name>] [-fo] [-h <host>] [--help] [-k <password>] [-m | --truncate] [--noddl | --nodml]  [-p <password>] [-po
       <port>] [-s] [-t <table>]  [-u <username>] [-v]
    --drop                  add DROP KEYSPACE statement. BE CAREFUL! THIS WILL WIPED OUT ENTIRE KEYSPACE
 -f,--file <file name>      exported file path. default to "<keyspace>.CQL" or "<keyspace>.<table>.CQL"
 -fo,--force                force overwrite of existing file
 -h,--host <host>           server host name or IP of database server, default is "localhost"
    --help                  print this help message
 -k,--keyspace <password>   database keyspace to be exported
 -m,--merge                 merge table data, insert will be generated with "IF NOT EXISTS"
    --noddl                 don't generate DDL statements (CREATE TABLE, INDEX, MATERIALIZED VIEW, TYPE, TRIGGER, AGGREGATE), mutual exclusive with
                            "nodml"
    --nodml                 don't generate DML statements (INSERT), mutual exclusive with "noddl"
 -p,--pass <password>       database password
 -po,--port <port>          database server port, default is 9042
 -s,--separate              seperated export by tables
 -t,--table <table>         keyspace table to be exported
    --truncate              add TRUNCATE TABLE statement. BE CAREFUL!
 -u,--user <username>       database username
 -v,--verbose               print verbose message
