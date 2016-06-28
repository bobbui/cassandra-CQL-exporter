/*
 * Copyright (c) 2016, Bui Nguyen Thang, thang.buinguyen@gmail.com, thangbui.net. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package net.thangbui.cql_exporter;

import com.google.common.io.BaseEncoding;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Bui Nguyen Thang on 6/3/2016.
 */
public class Utils {
    private final static List<String> RESERVED_KEYWORDS = Arrays.asList("ADD", "ALLOW", "ALTER", "AND", "ANY", "APPLY", "ASC", "AUTHORIZE", "BATCH", "BEGIN", "BY", "COLUMNFAMILY", "CREATE", "DELETE", "DESC", "DROP", "EACH_QUORUM", "FROM", "GRANT", "IN", "INDEX", "INET", "INSERT", "INTO", "KEYSPACE", "KEYSPACES", "LIMIT", "LOCAL_ONE", "LOCAL_QUORUM", "MODIFY", "NORECURSIVE", "OF", "ON", "ONE", "ORDER", "PASSWORD", "PRIMARY", "QUORUM", "RENAME", "REVOKE", "SCHEMA", "SELECT", "SET", "TABLE", "TO", "TOKEN", "THREE", "TRUNCATE", "TWO", "UNLOGGED", "UPDATE", "USE", "USING", "WHERE", "WITH");

    static String escapeReservedWord(String input) {
        return RESERVED_KEYWORDS.contains(input) ? "\"" + input + "\"" : input;
    }

    private static final Random random = new Random(); // or SecureRandom

    public static String randomChar() {
        final byte[] buffer = new byte[5];
        random.nextBytes(buffer);
        return BaseEncoding.base64Url().omitPadding().encode(buffer); // or base32()
    }
}
