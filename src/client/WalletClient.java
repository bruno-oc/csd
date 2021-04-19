package client;

import api.rest.WalletService;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import server.CoinServer;
import server.InsecureHostnameVerifier;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Scanner;

public class WalletClient {

    public final static int MAX_RETRIES = 3;
    public final static long RETRY_PERIOD = 10000;
    public final static int CONNECTION_TIMEOUT = 10000;
    public final static int REPLY_TIMEOUT = 6000;

    private static Client restClient;

    public WalletClient() {
        restClient = this.startClient();
    }

    private static SSLContext getContext() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");

        try (FileInputStream fis = new FileInputStream("security/server.ks")) {
            ks.load(fis, "password".toCharArray());
        }

        try (FileInputStream fis = new FileInputStream("security/truststore.ks")) {
            ts.load(fis, "changeit".toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "password".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);

        String protocol = "TLSv1.3";
        SSLContext sslContext = SSLContext.getInstance(protocol);

        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }

    private static void help() {
        System.out.println("1: obtainCoins");
        System.out.println("2: transferMoney");
        System.out.println("3: currentAmount");
        System.out.println("4: ledgerOfGlobalTransactions");
        System.out.println("5: ledgerOfClientTransactions");
        System.out.println("9: help");
        System.out.println("0: quit");
    }

    public static void main(String[] args) {
        WalletClient w = new WalletClient();
        Scanner s = new Scanner(System.in);
        String input;
        help();
        do {
            System.out.print("> ");
            input = s.nextLine();
            String[] inputs = input.split(" ");
            switch (inputs[0]) {
                case "1":
                    double amount = Double.parseDouble(inputs[2]);
                    w.obtainCoin(inputs[1], amount);
                    break;
                case "2":
                    double amount1 = Double.parseDouble(inputs[3]);
                    w.transferMoney(inputs[1], inputs[2], amount1);
                    break;
                case "3":
                    w.currentAmount(inputs[1]);
                    break;
                case "4":
                    w.ledgerTransactions("");
                    break;
                case "5":
                    w.ledgerTransactions(inputs[1]);
                    break;
                case "9":
                    help();
                    break;
                default:
                    break;
            }
        } while (!input.equals("0"));
    }

    private Client startClient() {
        HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
        SSLContext context = null;
        try {
            context = getContext();
            HttpsURLConnection.setDefaultSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ClientConfig config = new ClientConfig();

        // How much time until timeout on opening the TCP connection to the server
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT);
        // How much time to wait for the reply of the server after sending the request
        config.property(ClientProperties.READ_TIMEOUT, REPLY_TIMEOUT);

        return ClientBuilder.newBuilder().sslContext(context)
                .hostnameVerifier(new InsecureHostnameVerifier())
                .withConfig(config).build();
    }

    public void obtainCoin(String who, double amount) {

        WebTarget target = restClient.target(CoinServer.ServerURI).path(WalletService.PATH);

        short retries = 0;

        while (retries < MAX_RETRIES) {
            try {

                Response r = target.path("/obtain/" + who).request().accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(amount, MediaType.APPLICATION_JSON));

                if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
                    System.out.println(who + " balance: " + r.readEntity(Double.class));
                    return;
                } else {
                    System.out.println("Error, HTTP error status: " + r.getStatus());
                }
            } catch (ProcessingException pe) { // Error in communication with server
                System.out.println("Timeout occurred.");
                System.out.println(pe.getMessage()); // Could be removed
                retries++;
                try {
                    Thread.sleep(RETRY_PERIOD); // wait until attempting again.
                } catch (InterruptedException e) {
                    // Nothing to be done here, if this happens we will just retry sooner.
                }
                System.out.println("Retrying to execute request.");
            }
        }
    }

    public void transferMoney(String from, String to, double amount) {
        WebTarget target = restClient.target(CoinServer.ServerURI).path(WalletService.PATH);

        short retries = 0;

        while (retries < MAX_RETRIES) {
            try {

                Response r = target.path("/transfer/" + from).queryParam("to", to).request()
                        .accept(MediaType.APPLICATION_JSON).post(Entity.entity(amount, MediaType.APPLICATION_JSON));

                if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
                    System.out.println("from: " + from + " to: " + to + " amount: " + r.readEntity(Double.class));
                    return;
                } else {
                    System.out.println("Error, HTTP error status: " + r.getStatus());
                }
            } catch (ProcessingException pe) { // Error in communication with server
                System.out.println("Timeout occurred.");
                System.out.println(pe.getMessage()); // Could be removed
                retries++;
                try {
                    Thread.sleep(RETRY_PERIOD); // wait until attempting again.
                } catch (InterruptedException e) {
                    // Nothing to be done here, if this happens we will just retry sooner.
                }
                System.out.println("Retrying to execute request.");
            }
        }
    }

    public void currentAmount(String who) {
        WebTarget target = restClient.target(CoinServer.ServerURI).path(WalletService.PATH);

        short retries = 0;

        while (retries < MAX_RETRIES) {
            try {

                Response r = target.path("/" + who + "/").request().accept(MediaType.APPLICATION_JSON).get();

                if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
                    System.out.println("balance: " + r.readEntity(Double.class));
                    return;
                } else {
                    System.out.println("Error, HTTP error status: " + r.getStatus());
                }
            } catch (ProcessingException pe) { // Error in communication with server
                System.out.println("Timeout occurred.");
                System.out.println(pe.getMessage()); // Could be removed
                retries++;
                try {
                    Thread.sleep(RETRY_PERIOD); // wait until attempting again.
                } catch (InterruptedException e) {
                    // Nothing to be done here, if this happens we will just retry sooner.
                }
                System.out.println("Retrying to execute request.");
            }
        }
    }

    public void ledgerTransactions(String who) {
        WebTarget target = restClient.target(CoinServer.ServerURI).path(WalletService.PATH);

        short retries = 0;

        while (retries < MAX_RETRIES) {
            try {

                Response r = target.path("/transactions/" + who).request().accept(MediaType.APPLICATION_JSON).get();

                if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
                    System.out.println(r.readEntity(String.class));
                    return;
                } else {
                    System.out.println("Error, HTTP error status: " + r.getStatus());
                }
            } catch (ProcessingException pe) { // Error in communication with server
                System.out.println("Timeout occurred.");
                System.out.println(pe.getMessage()); // Could be removed
                retries++;
                try {
                    Thread.sleep(RETRY_PERIOD); // wait until attempting again.
                } catch (InterruptedException e) {
                    // Nothing to be done here, if this happens we will just retry sooner.
                }
                System.out.println("Retrying to execute request.");
            }
        }
    }
}
