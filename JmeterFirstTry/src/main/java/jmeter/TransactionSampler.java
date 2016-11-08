package jmeter;

import mock.nodes.Utils;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;

public class TransactionSampler extends AbstractSampler implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionSampler.class);
    private final static String SENDER_PORT = "TransactionSampler.senderPort";
    private final static String HOST = "TransactionSampler.host";
    private final static String NUMBER_OF_NODES = "TransactionSampler.numberOfNodes";
    private Random random = new Random();

    public void setNumberOfNodes(int value) {
        this.setProperty(NUMBER_OF_NODES, value);
    }

    public int getNumberOfNodes() {
        return this.getPropertyAsInt(NUMBER_OF_NODES);
    }

    public void setSenderPort(int value) {
        this.setProperty(SENDER_PORT, value);
    }

    public void setHost(String host) {
        this.setProperty(HOST, host);
    }

    public String getHost() {
        return this.getPropertyAsString(HOST);
    }

    public int getSenderPort() {
        return this.getPropertyAsInt(SENDER_PORT);
    }

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult sampleResult = new SampleResult();
        sampleResult.sampleStart();
        try {
            String message = transactionTest();
            sampleResult.sampleEnd();
            sampleResult.setSampleLabel("Transaction sampler");
            sampleResult.setSuccessful(Boolean.TRUE);
            sampleResult.setResponseCodeOK();
            sampleResult.setResponseMessage(message);
        } catch (Exception e) {
            LOGGER.error("Request was not successfully processed", e);
            sampleResult.sampleEnd();
            sampleResult.setResponseMessage(e.getMessage());
            sampleResult.setSuccessful(Boolean.FALSE);
        }
        return sampleResult;
    }

    private String transactionTest() throws IOException {
        int senderNodePort = this.getSenderPort();
        String host = this.getHost();
        int numberOfNodes = this.getNumberOfNodes();
        int transactionId = abs(random.nextInt());

        if (Utils.sendHTTPGet(new URL("http", host, senderNodePort, "/generate" + "?" + "id=" + transactionId)) >= 400) {
            return "Sender node exception";
        }
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfNodes - 1);
        getAllOtherPorts(senderNodePort).forEach(p -> executorService.submit(() -> {
            try {
                URL url = new URL("http", host, p, "/check" + "?" + "id=" + transactionId);
                int responseCode;
                do {
                    responseCode = Utils.sendHTTPGet(url);
                    if (responseCode >= 400) {
                        System.out.println("On Node " + p + " transaction absent, repeat");
                    }
                    Thread.sleep(10);
                } while (responseCode >= 400);
                System.out.println("On Node " + p + " transaction added");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }));
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return "Success";
    }

    private List<Integer> getAllOtherPorts(int port) {
        List<Integer> ports = Arrays.asList(8090, 8091, 8092, 8093);
        List<Integer> otherPorts = new ArrayList<>();
        for (Integer port1 : ports) {
            if (port1.equals(port)) {
                continue;
            }
            otherPorts.add(port1);
        }
        return otherPorts;
    }
}