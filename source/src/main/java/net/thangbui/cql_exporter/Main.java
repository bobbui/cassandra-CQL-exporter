/*
 * Copyright (c) 2016, Bui Nguyen Thang, thang.buinguyen@gmail.com, thangbui.net. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package net.thangbui.cql_exporter;

import org.apache.commons.cli.*;

/**
 * Created by Bui Nguyen Thang on 5/27/2016.
 */
public class Main {
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static boolean VERBOSE;

    public static void main(String[] args) throws Exception {
        CommandLineParser parser  = new DefaultParser();
        Options           options = OptionsBuilder.build();
        CommandLine       cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            OptionsBuilder.printHelp(options);
            return;
        }

        if (cmd.hasOption("help")) {
            OptionsBuilder.printHelp(options);
            return;
        }
        if (cmd.hasOption("license")) {
            OptionsBuilder.printLicense();
            return;
        }

        boolean noddl = cmd.hasOption("noddl");
        boolean nodml = cmd.hasOption("nodml");
        if (noddl && nodml) {
            System.err.println("\"nodll\" and \"nodml\" can not be set at the same time!");
            return;
        }
        boolean drop     = cmd.hasOption("drop");
        boolean merge    = cmd.hasOption("merge");
        boolean truncate = cmd.hasOption("truncate");
        boolean test     = cmd.hasOption("test");

        if (drop && truncate) {
            System.err.println("\"drop\" and \"truncate\" can not be set at the same time!");
            return;
        }
        if (drop && merge) {
            System.err.println("\"drop\" and \"merge\" can not be set at the same time!");
            return;
        }
        if (truncate && merge) {
            System.err.println("\"truncate\" and \"merge\" can not be set at the same time!");
            return;
        }

        VERBOSE = cmd.hasOption("verbose");

        SchemaExporter.Builder builder = SchemaExporter.builder();
        if (cmd.hasOption("host")) {
            builder.host(cmd.getOptionValue("host"));
        }
        if (cmd.hasOption("port")) {
            builder.port(cmd.getOptionValue("port"));
        }
        if (cmd.hasOption("user")) {
            builder.username(cmd.getOptionValue("user"));
        }
        if (cmd.hasOption("pass")) {
            builder.password(cmd.getOptionValue("pass"));
        }
        if (cmd.hasOption("keyspace")) {
            builder.keyspace(cmd.getOptionValue("keyspace"));
        }
        if (cmd.hasOption("file")) {
            builder.file(cmd.getOptionValue("file"));
        }
        if (cmd.hasOption("table")) {
            builder.table(cmd.getOptionValue("table"));
        }

        builder.drop(drop)
               .force(cmd.hasOption("force"))
               .merge(merge)
               .noddl(noddl)
               .nodml(nodml)
               .secure(cmd.hasOption("secure"))
               .separate(cmd.hasOption("separate"))
               .truncate(truncate);

        try {
            SchemaExporter schemaExporter = builder.build();
            schemaExporter.run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            if (test) {
                e.printStackTrace();
                throw e;
            }
        }

        if (!test)
            System.exit(0);
        return;
    }

}
