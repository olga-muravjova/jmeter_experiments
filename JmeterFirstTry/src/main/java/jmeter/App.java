package jmeter;

import mock.servers.Server;

public class App
{
    public static void main( String[] args ) throws Exception {
//        Server.runServer();
        JMeterRunner.runJmeter();
//        Server.stopServer();
    }
}
