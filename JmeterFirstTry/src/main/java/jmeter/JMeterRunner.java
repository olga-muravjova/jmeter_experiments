package jmeter;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.report.dashboard.ReportGenerator;
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
    private final static String JMeterPath = "C:/apache-jmeter-3.0";
    private final static String slash = System.getProperty("file.separator");
    private final static String logFile = System.getProperty("user.dir") + slash + "result.jtl";
    private final static String reportResultDir = System.getProperty("user.dir") + slash + "result";
    private final static int numberOfNodes = 4;
    private final static String host = "localhost";//"46.101.106.86";
    private final static int numberOfThreads = 10;
    private final static int numberOfLoops = 250;

    public static void run() throws Exception {
        setJMeterSettings();
        //creating or cleaning result dir
        File file = new File(reportResultDir);
        if (!file.exists()) {
            new File(reportResultDir).mkdir();
        }
        FileUtils.cleanDirectory(file);
        JMeterUtils.setProperty("jmeter.reportgenerator.exporter.html.property.output_dir", reportResultDir);
        JMeterUtils.setProperty("jmeter.reportgenerator.overall_granularity", "600");

        //cleaning logfile
        File logfile = new File(logFile);
        if (logfile.exists()) {
            logfile.delete();
        }
        configureRunJmeter();
        ReportGenerator reportGenerator = new ReportGenerator(logFile, null);
        reportGenerator.generate();
    }

    private static void setJMeterSettings() {
        File jmeterHome = new File(JMeterPath);

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
    }


    private static void generateTransactionThreadGroup(int port, String threadGroupName, HashTree testPlanTree) {
        // my custom sampler
        TransactionSampler transactionSampler = new TransactionSampler();
        transactionSampler.setSenderPort(port);
        transactionSampler.setHost(host);
        transactionSampler.setNumberOfNodes(numberOfNodes);

        // Loop Controller
        LoopController loopCtrl = new LoopController();
        loopCtrl.setLoops(numberOfLoops);
        loopCtrl.setFirst(true);
        loopCtrl.addTestElement(transactionSampler);
        loopCtrl.initialize();

        // Thread Group
        SetupThreadGroup threadGroup = new SetupThreadGroup();
        threadGroup.setNumThreads(numberOfThreads);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController(loopCtrl);
        threadGroup.setName(threadGroupName);

        testPlanTree.add(testPlanTree.getArray()[0], threadGroup).add(transactionSampler);
    }

    private static void configureRunJmeter() throws Exception {

        StandardJMeterEngine jm = new StandardJMeterEngine();

        // GenerateTransformer plan
        TestPlan testPlan = new TestPlan();
        HashTree testPlanTree = new HashTree();
        testPlanTree.add(testPlan);

        for (Integer nodePort : new Integer[]{8090, 8091, 8092, 8093}) {
            generateTransactionThreadGroup(nodePort,  "th" + nodePort.toString(), testPlanTree);
        }

        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        // Store execution results into a .jtl file

        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(logFile);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        jm.configure(testPlanTree);
        jm.run();
    }
}