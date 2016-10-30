package jmeter;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

/**
 * Created by Ольга on 30.10.2016.
 */
public class JMeterRunner {

    public static void runJmeter() throws Exception {

        StandardJMeterEngine jm = new StandardJMeterEngine();

        JMeterUtils.loadJMeterProperties("C:/apache-jmeter-3.0/bin/jmeter.properties");
        JMeterUtils.initLogging();
        JMeterUtils.initLocale();

        // my custom sampler
        TransactionSampler transactionSampler = new TransactionSampler();

        // http sampler
        HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
        httpSampler.setDomain("localhost");
        httpSampler.setPath("/generate");
        httpSampler.setPort(8090);
        httpSampler.setMethod("GET");

        // Loop Controller
        LoopController loopCtrl = new LoopController();
        loopCtrl.setLoops(1);
        loopCtrl.setFirst(true);
//        loopCtrl.addTestElement(transactionSampler);
        loopCtrl.addTestElement(httpSampler);
        loopCtrl.initialize();

        // Thread Group
        SetupThreadGroup threadGroup = new SetupThreadGroup();
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController(loopCtrl);

        // Test plan
        TestPlan testPlan = new TestPlan();

        HashTree hashTree = new HashTree();
        hashTree.add(testPlan);
        hashTree.add(threadGroup);
        hashTree.add(loopCtrl, transactionSampler);

        jm.configure(hashTree);
        jm.run();
    }
}