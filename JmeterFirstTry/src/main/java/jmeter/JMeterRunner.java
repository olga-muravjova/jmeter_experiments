package jmeter;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
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

    public static void runScenario() throws Exception {
        setJMeterSettings();
        for (int i = 70; i <= 250; i += 10) {
            runJmeter(i);
        }
        File file = new File(reportResultDir);
        if (!file.exists()) {
            new File(reportResultDir).mkdir();
        }
        FileUtils.cleanDirectory(file);

        JMeterUtils.setProperty("jmeter.reportgenerator.exporter.html.property.output_dir", reportResultDir);

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


    private static void runJmeter(int numberOfThreads) throws Exception {

        StandardJMeterEngine jm = new StandardJMeterEngine();

        // my custom sampler
        TransactionSampler transactionSampler = new TransactionSampler();

        // Loop Controller
        LoopController loopCtrl = new LoopController();
        loopCtrl.setLoops(1);
        loopCtrl.setFirst(true);
        loopCtrl.addTestElement(transactionSampler);
        loopCtrl.initialize();

        // Thread Group
        SetupThreadGroup threadGroup = new SetupThreadGroup();
        threadGroup.setNumThreads(numberOfThreads);
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

        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(logFile);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        jm.configure(testPlanTree);
        jm.run();
    }
}