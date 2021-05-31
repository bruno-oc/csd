package client;

import api.Block;
import api.Transaction;
import api.rest.WalletService;
import bftsmart.tom.util.TOMUtil;
import crypto.CryptoStuff;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import server.InsecureHostnameVerifier;
import server.SystemReply;

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
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class GenesisClient {

    public final static int MAX_RETRIES = 3;
    public final static long RETRY_PERIOD = 10000;
    public final static int CONNECTION_TIMEOUT = 10000;
    public final static int REPLY_TIMEOUT = 6000;

    private static String serverURI;
    private static String clientId;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: WalletClient <ip> <port>");
            System.exit(-1);
        }
        serverURI = String.format("https://%s:%s/", args[0], args[1]);
        clientId = "SYSTEM_INIT";
        minerate();
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

        String protocol = "TLSv1.2";
        SSLContext sslContext = SSLContext.getInstance(protocol);

        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }

    private static Client startClient() {
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

    private static Transaction getSignedTranscation(String clientId, String m, PrivateKey privateKey, PublicKey publicKey) {
        // ID||Op||sign(op)
        byte[] op = m.getBytes();
        byte[] signature;

        try {
            signature = CryptoStuff.sign(privateKey, op);
            return new Transaction(clientId, m, signature, privateKey.getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void minerate() {
        KeyPair kp = CryptoStuff.getKeyPair();
        List<Transaction> list = new ArrayList<>(2);
        String bruno = String.format(Transaction.OBTAIN_COIN, "bruno", "500000");
        String chula = String.format(Transaction.OBTAIN_COIN, "chula", "500000");
        Transaction t1 = getSignedTranscation("bruno", bruno, kp.getPrivate(), kp.getPublic());
        Transaction t2 = getSignedTranscation("chula", chula, kp.getPrivate(), kp.getPublic());
        list.add(t1);
        list.add(t2);

        Block block = new Block(list, null);
        block.setId(clientId);
        block.setPub(kp.getPublic().getEncoded());

        byte[] sig = new byte[0];
        try {
            sig = CryptoStuff.sign(kp.getPrivate(), Transaction.serialize(block.getTransactions()));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        block.setSig(sig);

        try {
            boolean found = false;
            byte[] nonce = new byte[8];
            while (!found) {
                SecureRandom.getInstanceStrong().nextBytes(nonce);
                block.setProof(nonce);

                if (Block.proofOfWork(block))
                    found = true;
            }
            sendMinedBlock(block);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendMinedBlock(Block mined) {
        Client restClient = startClient();
        WebTarget target = restClient.target(serverURI).path(WalletService.PATH);

        short retries = 0;
        while (retries < MAX_RETRIES) {
            try {

                Response r = target.path("/mine/block").request().accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(Block.serialize(mined), MediaType.APPLICATION_JSON));

                if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
                    SystemReply reply = r.readEntity(SystemReply.class);
                    System.out.println("Block genesis mined!");
                    return;
                } else {
                    System.out.println("Error, HTTP error status: " + r.getStatus());
                    retries++;
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
