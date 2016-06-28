/*
 * Copyright (c) 2016, Bui Nguyen Thang, thang.buinguyen@gmail.com, thangbui.net. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package net.thangbui.cql_exporter;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Bui Nguyen Thang on 6/3/2016.
 */
public class SchemaExporter {
    public static int FETCH_SIZE;
    public static final int NO_OF_ENTRY_BOUND = 10000;

    private String host;
    private int    port;
    private String username;
    private String password;
    private String keyspaceName;
    private String filePath;
    private String tableName;

    private boolean noddl;
    private boolean nodml;
    private boolean export2separateFiles;
    boolean truncate;
    private boolean dropKeyspace;
    private boolean force;
    private boolean secure;

    boolean merge;
    Session session;

    private SchemaExporter() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public void run() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();

        KeyspaceMetadata keyspace = validate();

        System.out.println("All good!");
        System.out.println("Start exporting...");

        long freeMemory = Runtime.getRuntime().freeMemory();
        FETCH_SIZE = Math.min(NO_OF_ENTRY_BOUND, (int) (freeMemory / 1000));
        if (Main.VERBOSE) {
            System.out.println("Free memory: " + freeMemory / 1024 / 1024 + " mb");
            System.out.println("Fetch size is set to: " + FETCH_SIZE);
        }

        if (Strings.isNullOrEmpty(tableName)) {
            extractKeyspace(keyspace);
        } else {
            extractOnlyOneTable(keyspace);
        }

        stopwatch.stop();
        System.out.printf("Export completed after %s s!" + Main.LINE_SEPARATOR, (float) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000);
        System.out.println("Exited.");
    }

    private void extractOnlyOneTable(KeyspaceMetadata keyspace) throws Exception {
        File file = FileUtils.verify(filePath, force);

        if (!noddl) {
            System.out.println("Write \"" + tableName + "\" DDL to " + file.getCanonicalPath());
            Files.asCharSink(file, Charset.defaultCharset(), FileWriteMode.APPEND)
                 .write(TableExporter.genDDL(keyspace.getTable(tableName)));
        }
        if (!nodml) {
            TableExporter.genDML(this, keyspace.getTable(tableName), file);
        }
    }

    private void extractKeyspace(KeyspaceMetadata keyspace) throws Exception {
        String nameWithoutExtension = Files.getNameWithoutExtension(filePath);

        File sharedFile = null;
        if (!export2separateFiles) {
            sharedFile = FileUtils.verify(filePath, force);
        }

        if (!noddl) {
            List<String> strings = genDDL(keyspace);
            File         file;
            if (export2separateFiles) {
                file = FileUtils.verify(nameWithoutExtension + "-DDL.CQL", force);
            } else {
                file = sharedFile;
            }
            System.out.println("Write DDL to " + file.getCanonicalPath());
            Files.asCharSink(file, Charset.defaultCharset()).writeLines(strings);
        }

        List<TableMetadata> tables = Lists.newArrayList(keyspace.getTables());

        if (!nodml) {
            for (int i = 0; i < tables.size(); i++) {
                TableMetadata table = tables.get(i);
                File          file;
                if (export2separateFiles) {
                    file = FileUtils.verify(
                            nameWithoutExtension + "-" + tables.get(i).getName() + "-DML.CQL", force);
                } else {
                    file = sharedFile;
                }
                TableExporter.genDML(this, table, file);
            }
        }
    }

    private KeyspaceMetadata validate() {
        Cluster.Builder builder = Cluster.builder();

        System.out.printf("Trying connect to host \"%s\"" + Main.LINE_SEPARATOR, host);
        DatabaseValidator.validateHost(host, builder);
        System.out.println("Success!");

        //FIXME: check username and password
        builder.withPort(port)
               .withoutJMXReporting()
               .withoutMetrics()
               .withCredentials(username, password)
               .withReconnectionPolicy(new ConstantReconnectionPolicy(2000));

        if (secure)
            builder.withSSL();

        System.out.printf("Trying connect to port \"%s\" " + Main.LINE_SEPARATOR, port);
        Cluster cluster = DatabaseValidator.validateDatabasePort(host, port, username, password, builder);
        System.out.println("Success!");

        if (Main.VERBOSE) {
            QueryLogger queryLogger = QueryLogger.builder()
                                                 .withConstantThreshold(1)
                                                 .withMaxQueryStringLength(QueryLogger.DEFAULT_MAX_QUERY_STRING_LENGTH)
                                                 .build();
            cluster.register(queryLogger);
        }

        SocketOptions socketOptions = cluster.getConfiguration().getSocketOptions();
        socketOptions.setConnectTimeoutMillis(10000);
        socketOptions.setReadTimeoutMillis(15000);
        socketOptions.setKeepAlive(true);

        System.out.printf("Trying connect to keyspace \"%s\"" + Main.LINE_SEPARATOR, keyspaceName);
        session = DatabaseValidator.validateKeyspace(cluster, keyspaceName);
        System.out.println("Success!");

        KeyspaceMetadata keyspace = cluster.getMetadata().getKeyspace(keyspaceName);

        if (!Strings.isNullOrEmpty(tableName)) {
            System.out.printf("Trying validate table \"%s\"" + Main.LINE_SEPARATOR, tableName);
            DatabaseValidator.validateTableName(tableName, keyspace);
            System.out.println("Success!");
        }
        return keyspace;
    }

    private List<String> genDDL(KeyspaceMetadata keyspace) {
        List<String> statements = new ArrayList();
        if (dropKeyspace) {
            statements.add("DROP KEYSPACE IF EXISTS " + keyspaceName);
        }
        statements.add(keyspace.exportAsString());
        return statements;
    }

    public boolean isForce() {
        return force;
    }

    public String getFilePath() {
        return filePath;
    }

    public static class Builder {
        private String host = "localhost";
        private String port;
        private String username = "";
        private String password = "";
        private String  keyspaceName;
        private String  tableName;
        private String  filePath;
        private boolean noddl;
        private boolean nodml;
        private boolean separate;
        private boolean truncate;
        private boolean drop;
        private boolean force;
        private boolean merge;
        private boolean secure;

        private Builder() {
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(String port) {
            this.port = port;
            return this;
        }

        public Builder file(String fileName) {
            this.filePath = fileName;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder keyspace(String keyspaceName) {
            this.keyspaceName = keyspaceName;
            return this;
        }

        public Builder table(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder noddl(boolean nodml) {
            this.noddl = nodml;
            return this;
        }

        public Builder nodml(boolean nodml) {
            this.nodml = nodml;
            return this;
        }

        public Builder separate(boolean separate) {
            this.separate = separate;
            return this;
        }

        public Builder truncate(boolean truncate) {
            this.truncate = truncate;
            return this;
        }

        public Builder drop(boolean drop) {
            this.drop = drop;
            return this;
        }

        public Builder force(boolean force) {
            this.force = force;
            return this;
        }

        public Builder merge(boolean merge) {
            this.merge = merge;
            return this;
        }

        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        public SchemaExporter build() {
            int portNo = 9042;
            if (!Strings.isNullOrEmpty(port)) {
                try {
                    portNo = Integer.parseInt(port);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("port must be an integer");
                }
                if (portNo <= 0)
                    throw new IllegalArgumentException("port can not be zero or negative");
            }

            if (Strings.isNullOrEmpty(keyspaceName))
                throw new IllegalArgumentException("keyspace name can not be empty");

            if (Strings.isNullOrEmpty(filePath)) filePath = keyspaceName + ".CQL";

            SchemaExporter schemaExporter = new SchemaExporter();
            schemaExporter.host = host;
            schemaExporter.port = portNo;
            schemaExporter.username = username;
            schemaExporter.password = password;
            schemaExporter.keyspaceName = keyspaceName;
            schemaExporter.filePath = filePath;
            schemaExporter.tableName = tableName;

            schemaExporter.noddl = noddl;
            schemaExporter.nodml = nodml;
            schemaExporter.export2separateFiles = separate;
            schemaExporter.truncate = truncate;
            schemaExporter.dropKeyspace = drop;
            schemaExporter.force = force;
            schemaExporter.merge = merge;
            schemaExporter.secure = secure;

            return schemaExporter;
        }
    }
}
