package jmeter;

import mock.nodes.NodesManager;

public class App
{
    public static void main( String[] args ) throws Exception {
        NodesManager nodesManager = new NodesManager();
        nodesManager.startNodes();
        JMeterRunner.run();
        nodesManager.stopNodes();
    }
}
