
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
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Joiner;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Bui Nguyen Thang on 6/4/2016.
 */
public class TableExporter {

    private static final CodecRegistry CODEC_REGISTRY = CodecRegistry.DEFAULT_INSTANCE;

    static String genDDL(TableMetadata table) {
        return table.exportAsString();
    }

    static void genDML(SchemaExporter generator, TableMetadata table, File file) throws IOException, ExecutionException, InterruptedException {
        String   tableName    = Utils.escapeReservedWord(table.getName());
        String   keyspaceName = table.getKeyspace().getName();
        CharSink charSink     = Files.asCharSink(file, Charset.defaultCharset(), FileWriteMode.APPEND);

        System.out.printf("-----------------------------------------------" + Main.LINE_SEPARATOR);
        System.out.printf("Extract from %s.%s" + Main.LINE_SEPARATOR, keyspaceName, tableName);

        if (generator.truncate) {
            charSink.write(QueryBuilder.truncate(keyspaceName, tableName).getQueryString());
        }

        Row firstRow = getFirstRow(generator.session, tableName, keyspaceName);

        if (firstRow == null) {
            return;
        }

        final List<String>                 colNames    = new ArrayList();
        final List<TypeCodec>              typeCodecs  = new ArrayList();
        List<ColumnDefinitions.Definition> definitions = firstRow.getColumnDefinitions().asList();

        for (ColumnDefinitions.Definition definition : definitions) {
            String   colName = definition.getName();
            DataType type    = definition.getType();
            colNames.add(Utils.escapeReservedWord(colName));
            Object object = firstRow.getObject(colName);
            typeCodecs.add(object != null ? CODEC_REGISTRY.codecFor(type, object) : CODEC_REGISTRY
                    .codecFor(type));
        }

        String prefix = "INSERT INTO " + keyspaceName + "." + tableName + " (" + Joiner.on(',')
                                                                                       .join(colNames) + ") VALUES (";
        String postfix = generator.merge ? ") IF NOT EXISTS;" : ");";

        long totalNoOfRows = getTotalNoOfRows(generator.session, tableName, keyspaceName);
        System.out.printf("Total number of record: %s" + Main.LINE_SEPARATOR, totalNoOfRows);

        int count = 0;
        Select select = QueryBuilder.select()
                                    .all()
                                    .from(keyspaceName, tableName).allowFiltering();
        select.setFetchSize(SchemaExporter.FETCH_SIZE);
        ResultSet     resultSet = generator.session.execute(select);
        Iterator<Row> iterator  = resultSet.iterator();

        System.out.printf("Start write \"%s\" data DML to %s" + Main.LINE_SEPARATOR, tableName, file.getCanonicalPath());

        int  noOfStep  = getNoOfStep(totalNoOfRows);
        long step      = totalNoOfRows / noOfStep;
        int  stepCount = 0;

        while (iterator.hasNext()) {
            Row    next      = iterator.next();
            String statement = generateInsertFromRow(typeCodecs, prefix, postfix, next) + Main.LINE_SEPARATOR;
            charSink.write(statement);
            count++;
            if (totalNoOfRows > SchemaExporter.FETCH_SIZE && count > stepCount * step) {
                float v = (float) count / (float) totalNoOfRows * 100;
                System.out.printf("Done %.2f%%" + Main.LINE_SEPARATOR, v);
                stepCount++;
            }
        }
        System.out.printf("Done exporting \"%s\", total number of records exported: %s" + Main.LINE_SEPARATOR, tableName, count);
    }

    private static String generateInsertFromRow(List<TypeCodec> typeCodecs, String prefix, String postfix, Row row) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);

        for (int colIdx = 0; colIdx < typeCodecs.size(); colIdx++) {
            TypeCodec typeCodec = typeCodecs.get(colIdx);
            if (colIdx != 0 && colIdx != typeCodecs.size()) {
                builder.append(',');
            }
            try {
                builder.append(typeCodec.format(row.get(colIdx, typeCodec)));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        builder.append(postfix);
        String statement = builder.toString();
        if (Main.VERBOSE) {
            System.out.println(statement);
        }
        return statement;
    }

    private static long getTotalNoOfRows(Session session, String tableName, String keyspaceName) {
        Select    select  = QueryBuilder.select().countAll().from(keyspaceName, tableName).allowFiltering();
        ResultSet execute = session.execute(select);
        Row       one     = execute.one();
        return Long.valueOf(one.getObject(0).toString());
    }

    private static Row getFirstRow(Session session, String tableName, String keyspaceName) {
        return session.execute(QueryBuilder.select()
                                           .all()
                                           .from(keyspaceName, tableName)
                                           .limit(1))
                      .one();
    }

    private static int getNoOfStep(long totalNoOfRows) {
        int noOfStep = 1;
        if (totalNoOfRows > 50000)
            noOfStep = 2;
        if (totalNoOfRows > 10000)
            noOfStep = 5;
        if (totalNoOfRows > 200000)
            noOfStep = 10;
        if (totalNoOfRows > 300000)
            noOfStep = 20;
        else if (totalNoOfRows > 500000)
            noOfStep = 30;
        else if (totalNoOfRows > 1000000)
            noOfStep = 50;
        return noOfStep;
    }
}
