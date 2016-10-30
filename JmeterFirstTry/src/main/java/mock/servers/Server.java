package mock.servers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


/**
 * Created by Ольга on 30.10.2016.
 */
public class Server {

    private static WireMockServer wireMockServer;
    private static WireMockServer wireMockServer2;

    public static void runServer() {
        wireMockServer = new WireMockServer(8090);
        wireMockServer.start();
        WireMock wireMock = new WireMock("localhost", 8090);
        wireMock.register(get(urlEqualTo("/generate"))
                .willReturn(aResponse()
                        .withStatus(200)));

        wireMockServer2 = new WireMockServer(8091);
        wireMockServer2.start();
        WireMock wireMock2 = new WireMock("localhost", 8091);
        wireMock2.register(get(urlEqualTo("/check"))
                .willReturn(aResponse()
                        .withStatus(302)));
    }

    public static void stopServer() {
        wireMockServer.stop();
        wireMockServer2.stop();
    }

//    public static void main( String[] args ) throws IOException {
//        runServer();
//        String urlString = "http://localhost:8089";
//        URL url = new URL(urlString);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // have to cast connection
//        connection.setRequestMethod("GET");
//        connection.connect();
//        connection.getResponseCode();
//        System.out.println(connection.getResponseCode());
//        BufferedReader br;
//        if (200 <= connection.getResponseCode() && connection.getResponseCode() <= 299) {
//            br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
//        } else {
//            br = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
//        }
//        System.out.println(br.readLine());
//        connection.disconnect();
//
//        String urlString2 = "http://localhost:8088";
//        URL url2 = new URL(urlString2);
//        HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection(); // have to cast connection
//        connection2.setRequestMethod("GET");
//        connection2.connect();
//        connection2.getResponseCode();
//        System.out.println(connection2.getResponseCode());
//        connection2.disconnect();
//        stopServer();
//    }
}
