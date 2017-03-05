## Summary
Alternative utility to **nodetool snapshot**  to export whole [Apache Cassandra](http://cassandra.apache.org/) keyspace/table structure and data to Cassandra Query Language (CQL) scripts. CQL scripts is a lightweight,simple way to restore and backupAn .

Features:
-  simple and configurable, see usage for detail.
-  fast and highly scalable: Data is exported gradually, so memory usage is very low, e.g: for my keyspace with 1,5 million record took ~3m to generated.
-  export process is tracked with detail information.
-  CQL scripts is ready to import using [SOURCE command](http://docs.datastax.com/en/cql/3.3/cql/cql_reference/source_r.html).
-  tested with Cassandra > 2.1, 2.2, 3.0 and tick-tock releases.
-  require Java > 6., make sure Java is available in PATH variable.

Overcome nodetool snapshot caveats:
-  snapshot can only be restored when table schema is there --> **cassandra-CQL-exporter** support both DDL and DML backup.
-  snapshot can only run on a node, multiple node require parallel ssh to be setup -->  **cassandra-CQL-exporter** dont need to care there is how many node.
-  snapshot is stored the node itself, **cassandra-CQL-exporter** back up is stored on the backup client itself --> more isolated backup environment.

Generated script contains 2 component:
- DDL: include keyspace CREATE statement, all tables, indexs, materialized views, function, aggregate function, user defined type.
- DML: INSERT statement for tables data.

Be careful that script will be forward-compatible but not guarantee to be backward-compatible especially DDL statements. It's better that export and import using same Cassandra version.
I'm using this on a daily basis. But anyways, use this at **YOUR OWN RISK**!

## Usage
```
usage: cql-export [--drop] [-f <file name>] [-fo] [-h <host>] [--help] [-k <keyspace>] [-l] [-m]
       [--noddl] [--nodml] [-p <password>] [-po <port>] [-s] [-t <table>] [--test] [--truncate] [-u
       <username>] [-v]
    --drop                  add DROP KEYSPACE statement. BE CAREFUL! THIS WILL WIPED OUT ENTIRE
                            KEYSPACE
 -f,--file <file name>      exported file path. default to "<keyspace>.CQL" or
                            "<keyspace>.<table>.CQL"
 -fo,--force                force overwrite of existing file
 -h,--host <host>           server host name or IP of database server, default is "localhost"
    --help                  print this help message
 -k,--keyspace <keyspace>   database keyspace to be exported
 -l,--license               Print this software license
 -m,--merge                 merge table data, insert will be generated with "IF NOT EXISTS"
    --noddl                 don't generate DDL statements (CREATE TABLE, INDEX, MATERIALIZED VIEW,
                            TYPE, TRIGGER, AGGREGATE), mutual exclusive with "nodml" option
    --nodml                 don't generate DML statements (INSERT), mutual exclusive with "noddl"
                            option
 -p,--pass <password>       database password
 -po,--port <port>          database server port, default is 9042
 -s,--separate              seperated export by tables
 -t,--table <table>         keyspace table to be exported
    --test                  Enable test mode. for development testing only
    --truncate              add TRUNCATE TABLE statement. BE CAREFUL!
 -u,--user <username>       database username
 -v,--verbose               print verbose message
```

##Sample usage

1. Simplest usages; only keyspace needed with **localhost** server and default port

        $cql-export -k cycling
        Trying connect to host "localhost"
        Success!
        Trying connect to port "9042" 
        Success!
        Trying connect to keyspace "cycling"
        Success!
        All good!
        Start exporting...
        Write DDL to C:\cql-generator\cycling.CQL
        Extract from cycling.cyclist_races
        Total number of record: 117920
        Start write "cyclist_teams" data DML to C:\cql-generator\cycling.CQL
        Done 5.00%
        Done 30.00%
        Done 90.00%
        Done exporting "cyclist_teams", total number of records exported: 117920
        Export completed after 21.179 s!
        Exited.

2. Simple usage:
```
$cql-export -h lcoalhost-po 9043 -k cycling
```
3. Generate only DDL statement 
```
$cql-export -h lcoalhost-po 9043  -k keyspace_name -noddl
```
## TODO
TODO: optimized jar size.
##License
 Apache 2.0 License
