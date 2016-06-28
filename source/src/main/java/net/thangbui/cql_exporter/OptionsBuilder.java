
/*
 * Copyright (c) 2016, Bui Nguyen Thang, thang.buinguyen@gmail.com, thangbui.net. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package net.thangbui.cql_exporter;

import com.google.common.io.Files;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by Bui Nguyen Thang on 6/3/2016.
 */
public class OptionsBuilder {

    static Options build() {
        Options options = new Options();
        Option helpOption = Option.builder()
                                  .longOpt("help")
                                  .desc("print this help message")
                                  .build();
        options.addOption(helpOption);

        Option verboseOption = Option.builder("v")
                                     .longOpt("verbose")
                                     .desc("print verbose message")
                                     .build();
        options.addOption(verboseOption);

        /*
        * database options
        * */
        Option hostOption = Option.builder("h")
                                  .longOpt("host")
                                  .argName("host")
                                  .hasArg()
                                  .desc("server host name or IP of database server, default is \"localhost\"")
                                  .build();
        options.addOption(hostOption);

        Option portOption = Option.builder("po")
                                  .longOpt("port")
                                  .argName("port")
                                  .hasArg()
                                  .type(Integer.class)
                                  .desc("database server port, default is 9042")
                                  .build();
        options.addOption(portOption);

        Option userNameOption = Option.builder("u")
                                      .longOpt("user")
                                      .argName("username")
                                      .hasArg()
                                      .desc("database username")
                                      .build();
        options.addOption(userNameOption);

        Option passwordOption = Option.builder("p")
                                      .longOpt("pass")
                                      .argName("password")
                                      .hasArg()
                                      .desc("database password")
                                      .build();
        options.addOption(passwordOption);

        Option keyspaceOption = Option.builder("k")
                                      .argName("keyspace")
                                      .longOpt("keyspace")
                                      .hasArg()
                                      .desc("database keyspace to be exported")
                                      .build();
        options.addOption(keyspaceOption);

        Option tableOption = Option.builder("t")
                                   .longOpt("table")
                                   .argName("table")
                                   .hasArg()
                                   .desc("keyspace table to be exported")
                                   .build();
        options.addOption(tableOption);

        Option secureOption = Option.builder()
                                    .longOpt("secure")
                                    .desc("connect to database using SSL")
                                    .build();
        options.addOption(secureOption);

        /*
        * export options
        * */
        Option noDDL = Option.builder()
                             .longOpt("noddl")
                             .desc("don't generate DDL statements (CREATE TABLE, INDEX, MATERIALIZED VIEW, TYPE, TRIGGER, AGGREGATE), mutual exclusive with \"nodml\" option")
                             .build();
        Option noDML = Option.builder()
                             .longOpt("nodml")
                             .desc("don't generate DML statements (INSERT), mutual exclusive with \"noddl\" option")
                             .build();

        options.addOption(noDDL);
        options.addOption(noDML);

        Option truncateOption = Option.builder()
                                      .longOpt("truncate")
                                      .desc("add TRUNCATE TABLE statement. BE CAREFUL!")
                                      .build();
        Option dropOption = Option.builder()
                                  .longOpt("drop")
                                  .desc("add DROP KEYSPACE statement. BE CAREFUL! THIS WILL WIPED OUT ENTIRE KEYSPACE")
                                  .build();
        options.addOption(dropOption);

        //mutual exclusive with drop schema
        Option mergeDataOption = Option.builder("m")
                                       .longOpt("merge")
                                       .desc("merge table data, insert will be generated with \"IF NOT EXISTS\"")
                                       .build();

        options.addOption(truncateOption);
        options.addOption(mergeDataOption);

        /*
        * export file options
        * */

        Option fileNameOption = Option.builder("f")
                                      .argName("file name")
                                      .longOpt("file")
                                      .hasArg()
                                      .desc("exported file path. default to \"<keyspace>.CQL\" or \"<keyspace>.<table>.CQL\"")
                                      .build();
        options.addOption(fileNameOption);

        Option seperateFileOption = Option.builder("s")
                                          .longOpt("separate")
                                          .desc("seperated export by tables")
                                          .build();
        options.addOption(seperateFileOption);

        Option forceOverWrite = Option.builder("fo")
                                      .longOpt("force")
                                      .desc("force overwrite of existing file")
                                      .build();
        options.addOption(forceOverWrite);

        Option testOption = Option.builder()
                                  .longOpt("test")
                                  .desc("Enable test mode. for development testing only")
                                  .build();
        options.addOption(testOption);
        Option licenseOption = Option.builder("l")
                                     .longOpt("license")
                                     .desc("Print this software license")
                                     .build();
        options.addOption(licenseOption);

        return options;
    }

    static void printHelp(Options options) {
        System.out.println("Cassandra CQL exporter");
        System.out.println("Copyright (c) 2016 Thang Bui, thang.buinguyen@gmail.com, thangbui.net, Type 'cql-exporter \"-L\"' for this software license");
        System.out.println("Version 1.0  07 June 2016");
        new HelpFormatter().printHelp(100, "cql-export", "", options, "", true);
    }

    static void printLicense() {
        try {
            List<String> strings = Files.readLines(new File(OptionsBuilder.class.getClassLoader()
                                                                                .getResource("LICENSE.txt")

                                                                                .getFile()), Charset.defaultCharset());
            for (String string : strings) {
                System.out.println(string);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
