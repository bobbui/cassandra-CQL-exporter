/*
 * Copyright (c) 2016, Bui Nguyen Thang, thang.bn@live.com, thangbui.net. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package net.thangbui.cql_exporter;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.google.common.base.Strings;

/**
 * Created by Bui Nguyen Thang on 6/2/2016.
 */
public class DatabaseValidator {

    static boolean validateHost(String host, Cluster.Builder builder) {
        try {
            builder.addContactPoint(host);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown host \"" + host + "\", please check your host server name or IP address.");
        }
        return false;
    }

    static Cluster validateDatabasePort(String host, int port, String username, String password, Cluster.Builder builder) {
        Cluster cluster = builder.build();
        try {
            cluster.connect();
        } catch (NoHostAvailableException e) {
            String message = e.getMessage();
            if (message.endsWith("Channel has been closed))")) {
                throw new RuntimeException("Can not contact port \"" + port + "\" on host \"" + host + "\", is your port correct or SSL setting correct?");
            } else {
                throw new RuntimeException("Can not contact port \"" + port + "\" on host \"" + host + "\", is your server started or port opened?");
            }
        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication error \"" + host + "\" on host \"" + port + "\" with username \"" + username + "\" and password  \"" + password + "\", is your username and password correct?");
        }
        return cluster;
    }

    static Session validateKeyspace(Cluster cluster, String keyspaceName) {
        try {
            return cluster.connect(keyspaceName);
        } catch (Exception e) {
            throw new RuntimeException("Cannot connect to \"" + keyspaceName + "\" keyspace : " + e.getMessage());
        }
    }

    static void validateTableName(String tableName, KeyspaceMetadata keyspace) {
        if (!Strings.isNullOrEmpty(tableName)) {
            TableMetadata table = keyspace.getTable(tableName);
            if (table == null) {
                throw new IllegalArgumentException("table \"" + tableName + "\" does not existed!");
            }
        }
    }

}
