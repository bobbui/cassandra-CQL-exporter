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
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Created by Bui Nguyen Thang on 6/2/2016.
 */
public class ExportTest {

    @Test
    public void exportAll() throws Exception {
        String[] args = new String[]{"-k", "cycling",
                "--force", "--test"};
        Main.main(args);
        String s  = Files.toString(new File("cycling.CQL"), Charset.defaultCharset());
        String s1 = Files.toString(new File("src/test/resources/cycling.expected.CQL"), Charset.defaultCharset());
        compareContent(s, s1);
    }

    @Test
    public void exportOneTable() throws Exception {
        String[] args = new String[]{"-k", "cycling", "-t", "cyclist_races",
                "--force", "--test"};
        Main.main(args);
        String s  = Files.toString(new File("cycling.CQL"), Charset.defaultCharset());
        String s1 = Files.toString(new File("src/test/resources/cycling.table.cyclist_races.expected.CQL"), Charset.defaultCharset());
        compareContent(s, s1);
    }

    @Test
    public void seperateTable() throws Exception {
        String[] args = new String[]{"-k", "iot_new", "-s",
               /* "--force",*/ "--test"};
        Main.main(args);
        String s  = Files.toString(new File("cycling.CQL"), Charset.defaultCharset());
        String s1 = Files.toString(new File("src/test/resources/cycling.table.cyclist_races.expected.CQL"), Charset.defaultCharset());
        compareContent(s, s1);
    }

    private void compareContent(String content1, String content2) {
        content1 = content1.trim().replaceAll("  ", " ").replaceAll("\t", "").replaceAll("\r", "").replaceAll("\n", "");
        content2 = content2.trim().replaceAll("\t", "").replaceAll("  ", " ").replaceAll("\r", "").replaceAll("\n", "");
        Assert.assertEquals("Content is not the same! Failed!", content1, content2);
    }
}