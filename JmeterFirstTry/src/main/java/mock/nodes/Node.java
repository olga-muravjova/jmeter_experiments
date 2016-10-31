package mock.nodes;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Created by phd on 31.10.16.
 */
public class Node {
    private static final String host = "localhost";

    private WireMockServer nodeImpl;
    private int port;
    private ExecutorService executorService;
    private NodesManager nodesManager;
    private List<Integer> transactionIDs;

    public Node(NodesManager nodesManager, int port) {
        transactionIDs = new ArrayList<>();
        this.nodesManager = nodesManager;
        executorService = Executors.newFixedThreadPool(4);
        this.port = port;
        GenerateTransformer generateTransformer = new GenerateTransformer(getSendTransactionCommand(), executorService);
        AddTransformer addTransformer = new AddTransformer(this::addTransaction);
        CheckTransformer checkTransformer = new CheckTransformer(this::checkTransaction);
        nodeImpl = new WireMockServer(wireMockConfig().extensions(generateTransformer, addTransformer, checkTransformer).port(port));
    }

    private synchronized void addTransaction(int id) {
        transactionIDs.add(id);
    }

    private synchronized boolean checkTransaction(int id) {
        return transactionIDs.contains(id);
    }

    private Consumer<Integer> getSendTransactionCommand() {
        return id -> {
            addTransaction(id);
            nodesManager.getOtherNodePorts(port).forEach(port -> {
                URL url = Utils.getUrlSafe("http", "localhost", port, "/add?" + "id=" + id);
                Utils.sendHttpGetSafe(url);
            });
        };
    }

    private void initGenerateTransactionMock() {
        WireMock mock = new WireMock(host, port);
        mock.register(get(urlMatching("/generate?.*"))
                .willReturn(aResponse().withTransformers("GenerateTransformer")));
    }

    private void initAddTransactionMock() {
        WireMock mock = new WireMock(host, port);
        mock.register(get(urlMatching("/add?.*"))
                .willReturn(aResponse().withTransformers("AddTransformer")));
    }

    private void initCheckTransactionMock() {
        WireMock mock = new WireMock(host, port);
        mock.register(get(urlMatching("/check?.*"))
            .willReturn(aResponse().withTransformers("CheckTransformer")));
    }

    public void start() {
        nodeImpl.start();
        initGenerateTransactionMock();
        initAddTransactionMock();
        initCheckTransactionMock();
    }

    public void stop() {
        nodeImpl.stop();
        executorService.shutdownNow();
    }

    public static class GenerateTransformer extends ResponseDefinitionTransformer {

        private final Consumer<Integer> sendTransactionCommand;
        private final ExecutorService executorService;

        public GenerateTransformer(Consumer<Integer> sendTransactionCommand, ExecutorService executorService) {
            this.sendTransactionCommand = sendTransactionCommand;
            this.executorService = executorService;
        }

        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
            int transactionId = Integer.valueOf(Utils.getQueryParams(request.getUrl()).get("id").get(0));
            executorService.submit(() -> sendTransactionCommand.accept(transactionId));
            return new ResponseDefinitionBuilder().withStatus(200).build();
        }


        @Override
        public boolean applyGlobally() {
            return false;
        }


        @Override
        public String getName() {
            return "GenerateTransformer";
        }
    }

    public static class AddTransformer extends ResponseDefinitionTransformer {
        private final Consumer<Integer> addTransactionCommand;

        public AddTransformer(Consumer<Integer> addTransactionCommand) {
            this.addTransactionCommand = addTransactionCommand;
        }

        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
            int transactionId = Integer.valueOf(Utils.getQueryParams(request.getUrl()).get("id").get(0));
            addTransactionCommand.accept(transactionId);
            return new ResponseDefinitionBuilder().withStatus(200).build();
        }

        @Override
        public boolean applyGlobally() {
            return false;
        }

        @Override
        public String getName() {
            return "AddTransformer";
        }
    }

    public static class CheckTransformer extends ResponseDefinitionTransformer {
        private final Predicate<Integer> checkTransactionCommand;

        public CheckTransformer(Predicate<Integer> checkTransactionCommand) {
            this.checkTransactionCommand = checkTransactionCommand;
        }

        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
            int transactionId = Integer.valueOf(Utils.getQueryParams(request.getUrl()).get("id").get(0));

            return new ResponseDefinitionBuilder().withStatus(checkTransactionCommand.test(transactionId) ? 200 : 400).build();
        }

        @Override
        public boolean applyGlobally() {
            return false;
        }

        @Override
        public String getName() {
            return "CheckTransformer";
        }
    }
}
