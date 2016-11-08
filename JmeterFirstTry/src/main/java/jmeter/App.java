package jmeter;

import mock.nodes.NodesManager;

public class App
{
    public static void main( String[] args ) throws Exception {
        NodesManager nodesManager = new NodesManager();
        nodesManager.startNodes();
//        for(int i =0; i<10; i++) {
            JMeterRunner.run();
//            Thread.sleep(1000);
//        }
        nodesManager.stopNodes();
    }
}
