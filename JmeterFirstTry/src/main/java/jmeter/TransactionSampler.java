package jmeter;

import org.apache.commons.lang.NullArgumentException;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gkatzioura on 30/1/2016.
 */
public class TransactionSampler extends AbstractSampler implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionSampler.class);
    private String senderNodeHost = "localhost";
    private String senderNodePort = ":8090";
    private String recieverNodeHost = "localhost";
    private String recieverNodePort = ":8091";

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult sampleResult = new SampleResult();
//        System.out.println("ma");
        sampleResult.sampleStart();
        try {
            String message = transactionTest();
            sampleResult.sampleEnd();
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
        String urlString = "http://" + senderNodeHost + "/generate/transaction" + senderNodePort;
//        urlString = "http://google.com";
        if (sendHTTPGet(urlString) >= 400) {
            return "Sender node exception";
        }
        urlString = "http://" + recieverNodeHost + "/has/transaction" + recieverNodePort;
//        urlString = "http://google.com";

        int responseCode;
        do {
            responseCode = sendHTTPGet(urlString);
        } while (responseCode >= 400);
        return "Success";
    }

    private int sendHTTPGet(String urlString) throws IOException {
        if (urlString == null) {
            throw new NullArgumentException("");
        }
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        connection.disconnect();
        return responseCode;
    }
}