package jmeter;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.File;

/**
 * Created by Ольга on 30.10.2016.
 */
public class JMeterRunner {
    private final static String JMeterPath = "/home/phd/Development/Projects/JMeter/apache-jmeter-3.0";

    public static void runJmeter() throws Exception {

        StandardJMeterEngine jm = new StandardJMeterEngine();


        File jmeterHome = new File(JMeterPath);
        String slash = System.getProperty("file.separator");

        if (!jmeterHome.exists()) {
            System.err.println("jmeter location is incorrect");
            System.exit(1);
        }

        File jmeterProperties = new File(jmeterHome.getPath() + slash + "bin" + slash + "jmeter.properties");
        if (!jmeterProperties.exists()) {
            System.err.println("jmeter.properties incorrect location");
            System.exit(1);
        }

        JMeterUtils.setJMeterHome(jmeterHome.getPath());
        JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
        JMeterUtils.initLogging();
        JMeterUtils.initLocale();

        // my custom sampler
        TransactionSampler transactionSampler = new TransactionSampler();

        // Loop Controller
        LoopController loopCtrl = new LoopController();
        loopCtrl.setLoops(1000);
        loopCtrl.setFirst(true);
        loopCtrl.addTestElement(transactionSampler);
        loopCtrl.initialize();

        // Thread Group
        SetupThreadGroup threadGroup = new SetupThreadGroup();
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController(loopCtrl);

        // GenerateTransformer plan
        TestPlan testPlan = new TestPlan();

        HashTree testPlanTree = new HashTree();
        testPlanTree.add(testPlan);
        testPlanTree.add(testPlan, threadGroup).add(transactionSampler);

        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        // Store execution results into a .jtl file
        String logFile = System.getProperty("user.dir") + slash + "result.jtl";
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(logFile);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        jm.configure(testPlanTree);
        jm.run();
    }
}