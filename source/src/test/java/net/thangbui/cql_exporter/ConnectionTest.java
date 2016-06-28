/*
 * Copyright (c) 2016, Bui Nguyen Thang, thang.buinguyen@gmail.com, thangbui.net. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package net.thangbui.cql_exporter;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Created by Bui Nguyen Thang on 6/2/2016.
 */
public class ConnectionTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test()
    public void invalidHost() throws Exception {
        String   hostName = Utils.randomChar();
        String[] args     = new String[]{"-h", hostName, "-k", "keyspaceName", "--test", "--force"};
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(Matchers.equalToIgnoringCase("Unknown host \"" + hostName + "\", please check your host server name or IP address."));
        Main.main(args);
        Assert.fail("Fail without any errors.");
    }

    @Test()
    public void invalidPort() throws Exception {
        int      port     = 9999;
        String   hostName = "localhost";
        String[] args     = new String[]{"-po", String.valueOf(port), "-h", hostName, "-k", "keyspaceName", "--test", "--force"};
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(Matchers.equalToIgnoringCase("Can not contact port \"" + port + "\" on host \"" + hostName + "\", is your server started or port opened?"));
        Main.main(args);
        Assert.fail("Fail without any errors.");
    }

    @Test()
    public void invalidSSLsetting() throws Exception {
        int      port     = 9042;
        String   hostName = "localhost";
//        -k iot_new -v --test --secure
        String[] args     = new String[]{"-po", String.valueOf(port), "-h", hostName, "-k", "keyspaceName", "--secure", "-v","--test", "--force"};
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(Matchers.equalToIgnoringCase("Can not contact port \"" + port + "\" on host \"" + hostName + "\", is your port correct or SSL setting correct?"));
        Main.main(args);
        Assert.fail("Fail without any errors.");
    }



    @Test()
    public void invalidUsernamePassword() throws Exception {
        int      port     = 9042;
        String   hostName = "localhost";
        String   username = Utils.randomChar();
        String   password = Utils.randomChar();
        String[] args     = new String[]{"-po", String.valueOf(port), "-h", hostName, "-u", username, "-p", password, "-k", "keyspaceName", "--test", "--force"};
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(Matchers.equalToIgnoringCase("Authentication error \"" + hostName + "\" on host \"" + port + "\" with username \"" + username + "\" and password  \"" + password + "\", is your username and password correct?"));
        Main.main(args);
        Assert.fail("Fail without any errors.");
    }

    @Test()
    public void invalidKeyspace() throws Exception {
        String   keyspace = Utils.randomChar();
        String[] args     = new String[]{"-k", keyspace, "--test", "--force"};
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(Matchers.startsWith("Cannot connect to \"" + keyspace + "\" keyspace : "));
        Main.main(args);
        Assert.fail("Fail without any errors.");
    }

    @Test()
    public void invalidTableName() throws Exception {
        String   keyspace  = "cycling";
        String   tableName = Utils.randomChar();
        String[] args      = new String[]{"-t", tableName, "-k", keyspace, "--test", "--force"};
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(Matchers.equalTo("table \"" + tableName + "\" does not existed!"));
        Main.main(args);
        Assert.fail("Fail without any errors.");
    }
}
