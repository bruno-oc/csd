package client;

import api.Block;
import api.Transaction;
import api.rest.WalletService;
import bftsmart.tom.util.TOMUtil;
import crypto.CryptoStuff;
import db.DataBase;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Scanner;

public class WalletClient {

    public final static int MAX_RETRIES = 3;
    public final static long RETRY_PERIOD = 10000;
    public final static int CONNECTION_TIMEOUT = 10000;
    public final static int REPLY_TIMEOUT = 6000;

    private static Client restClient;
    private static String serverURI;
    private static String clientId;
    private static int num_op;
    private static double time;
    private final KeyPair clientKey;
    private DataBase db;
    
    private Gson gson = new Gson();

    public WalletClient(String ip, String port) {
        serverURI = String.format("https://%s:%s/", ip, port);
        restClient = this.startClient();
        clientKey = CryptoStuff.getKeyPair();

        String filePath = "src/client/client_log.json";
        db = new DataBase(filePath);
        time = 0;
        num_op = 0;
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

    private static void help() {
        System.out.println("1: obtainCoins");
        System.out.println("2: transferMoney");
        System.out.println("3: currentAmount");
        System.out.println("4: ledgerOfGlobalTransactions");
        System.out.println("5: ledgerOfClientTransactions");
        System.out.println("6: changeServer");
        System.out.println("7: minerateBlock");
        System.out.println("h: help");
        System.out.println("q: quit");
    }

    private static void metrics(long start, long end) {
        time += (double) (end - start) / 1_000_000_000.0;
        num_op++;
        System.out.println();
        System.out.println("========= Metrics =========");
        System.out.println("Latency = " + (double) (end - start) / 1_000_000_000.0 + " secs");
        System.out.println("Throughput = " + num_op / time);
        System.out.println("===========================");
        System.out.println();
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: WalletClient <ip> <port> <clientId>");
            System.exit(-1);
        }
        WalletClient w = new WalletClient(args[0], args[1]);
        clientId = args[2];
        Scanner s = new Scanner(System.in);
        String input, who, to, ip;
        int port, lastN;
        double amount;
        long start, end;
        help();
        do {
            System.out.print("> ");
            input = s.nextLine();
            String[] inputs = input.split(" ");
            switch (inputs[0]) {
                case "1":
                    System.out.print("who: ");
                    who = s.nextLine();
                    System.out.print("amount: ");
                    amount = Double.parseDouble(s.nextLine());

                    start = System.nanoTime();
                    w.obtainCoin(who, amount);
                    end = System.nanoTime();
                    metrics(start, end);
                    break;
                case "2":
                    System.out.print("from: ");
                    who = s.nextLine();
                    System.out.print("to: ");
                    to = s.nextLine();
                    System.out.print("amount: ");
                    amount = Double.parseDouble(s.nextLine());

                    start = System.nanoTime();
                    w.transferMoney(who, to, amount);
                    end = System.nanoTime();
                    metrics(start, end);
                    break;
                case "3":
                    start = System.nanoTime();
                    w.currentAmount(clientId);
                    end = System.nanoTime();
                    metrics(start, end);
                    break;
                case "4":
                    System.out.print("lastN: ");
                    lastN = Integer.parseInt(s.nextLine());

                    start = System.nanoTime();
                    w.ledgerTransactions("", lastN);
                    end = System.nanoTime();
                    metrics(start, end);
                    break;
                case "5":
                    System.out.print("who: ");
                    who = s.nextLine();
                    System.out.print("lastN: ");
                    lastN = Integer.parseInt(s.nextLine());

                    start = System.nanoTime();
                    w.ledgerTransactions(who, lastN);
                    end = System.nanoTime();
                    metrics(start, end);
                    break;
                case "6":
                    System.out.print("ip: ");
                    ip = s.nextLine();
                    System.out.print("port: ");
                    port = Integer.parseInt(s.nextLine());
                    changeServer(ip, port);
                case "7":
                    start = System.nanoTime();
                    System.out.print("N transactions: ");
                    lastN = Integer.parseInt(s.nextLine());
                    if(lastN < Block.MINIMUM_TRANSACTIONS) {
                        System.out.println("Minimum transactions required: " + Block.MINIMUM_TRANSACTIONS);
                        break;
                    }
                    w.minerate(lastN);
                    end = System.nanoTime();
                    metrics(start, end);
                    break;
                case "h":
                    help();
                    break;
                default:
                    break;
            }
        } while (!input.equals("q"));
    }

    private static void changeServer(String ip, int port) {
        serverURI = String.format("https://%s:%s/", ip, port);
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

    private Transaction getSignedTranscation(String m) {
        // ID||Op||sign(op)
        byte[] op = m.getBytes();
        byte[] signature;

        try {
            signature = CryptoStuff.sign(clientKey.getPrivate(), op);
            return new Transaction(clientId, m, signature, clientKey.getPublic().getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void obtainCoin(String who, double amount) {

        String m = String.format(Transaction.OBTAIN_COIN, who, amount);
        Transaction output = getSignedTranscation(m);

        WebTarget target = restClient.target(serverURI).path(WalletService.PATH);

        short retries = 0;

        while (retries < MAX_RETRIES) {
            try {

                Response r = target.path("/obtain/" + who).request().accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(Transaction.serialize(output), MediaType.APPLICATION_JSON_TYPE));

                if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
                    SystemReply reply = r.readEntity(SystemReply.class);
                    db.addLog(reply);
                    System.out.println(who + " balance: " + reply.getReplies().get(0).getValue());
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

    public void transferMoney(String from, String to, double amount) {
        WebTarget target = restClient.target(serverURI).path(WalletService.PATH);

        String m = String.format(Transaction.TRANSFER, from, to, amount);
        Transaction output = getSignedTranscation(m);


        short retries = 0;

        while (retries < MAX_RETRIES) {
            try {

                Response r = target.path("/transfer/" + from).queryParam("to", to).request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(Transaction.serialize(output), MediaType.APPLICATION_JSON));

                if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
                    SystemReply reply = r.readEntity(SystemReply.class);
                    db.addLog(reply);
                    System.out.println("from: " + from + " to: " + to + " amount: " + amount);
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

    public void currentAmount(String me) {

        String m = String.format(Transaction.CURRENT_AMOUNT, me);
        Transaction output = getSignedTranscation(m);

        WebTarget target = restClient.target(serverURI).path(WalletService.PATH);

        short retries = 0;

        while (retries < MAX_RETRIES) {
            try {

                Response r = target.path(me).request().accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(Transaction.serialize(output), MediaType.APPLICATION_JSON));

                if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
                    SystemReply reply = r.readEntity(SystemReply.class);
                    db.addLog(reply);
                    System.out.println("balance: " + reply.getReplies().get(0).getValue());
                    return;
                } else {
                    retries++;
                    System.out.println("Error, HTTP error status: " + r.getStatus());
                }
            } catch (ProcessingException pe) { // Error in communication with server
                System.out.println("Timeout occurred.");
                //System.out.println(pe.getMessage()); // Could be removed
                pe.printStackTrace();
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

    @SuppressWarnings("unchecked")
    public List<Transaction> ledgerTransactions(String who, int lastN) {

        String m;
        if (who.equals(""))
            m = String.format(Transaction.GET_ALL_TRANSCATIONS);
        else
            m = String.format(Transaction.GET_USER_TRANSCATIONS, who);
        Transaction output = getSignedTranscation(m);

        WebTarget target = restClient.target(serverURI).path(WalletService.PATH);

        short retries = 0;

        while (retries < MAX_RETRIES) {
            try {
                Response r = target.path("/transactions/" + who).queryParam("lastN", lastN).request().accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(Transaction.serialize(output), MediaType.APPLICATION_JSON_TYPE));

                if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
                    SystemReply reply = r.readEntity(SystemReply.class);
                    db.addLog(reply);
                    String json = reply.getReplies().get(0).getValue();
                    System.out.println(json);
                    
                    Type type = new TypeToken<List<Transaction>>() {
                    }.getType();
                    
                    return gson.fromJson(json, type);
                } else {
                    retries++;
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
        return null;
    }

    private void minerate(int lastN) {
        Block lastMined = obtainLastMinedBlock();
        List<Transaction> transactionList = pickTransactions(lastN);

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(lastMined);
            byte[] lastMinedBytes = bos.toByteArray();

            Block minedBlock = new Block(transactionList, TOMUtil.computeHash(lastMinedBytes));
            boolean found = false;
            byte[] nonce = new byte[8];

            while (!found) {
                SecureRandom.getInstanceStrong().nextBytes(nonce);
                minedBlock.setProof(nonce);

                oos.writeObject(lastMined);
                if (proofOfWork(bos.toByteArray())) {
                    found = true;
                }
            }
            sendMinedBlock(minedBlock);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Transaction> pickTransactions(int n) {
        String m = String.format(Transaction.GET_NOT_MINED_TRANSACTIONS, n);
        Transaction output = getSignedTranscation(m);

        WebTarget target = restClient.target(serverURI).path(WalletService.PATH);

        short retries = 0;

        while (retries < MAX_RETRIES) {
            try {
                Response r = target.path("/mine/transactions").queryParam("n", n).request().accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(Transaction.serialize(output), MediaType.APPLICATION_JSON_TYPE));

                if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
                    SystemReply reply = r.readEntity(SystemReply.class);
                    db.addLog(reply);
                    String json = reply.getReplies().get(0).getValue();
                    System.out.println(json);

                    Type type = new TypeToken<List<Transaction>>() {
                    }.getType();

                    return gson.fromJson(json, type);
                } else {
                    retries++;
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
        return null;
    }

    private boolean proofOfWork(byte[] block) {
        byte[] blockHash = TOMUtil.computeHash(block);
        for (Byte b: blockHash) {
            System.out.print(b + " ");
        }
        System.out.println();
        int count = 0;
        for(byte b : blockHash) {
            if(b == 0) {
                System.out.println("first byte: " + b);
                count++;
                if (count == 2)
                    return true;
            } else
                return false;
        }
        return false;
    }

    public Block obtainLastMinedBlock() {
    	
    	String m = Transaction.GET_LAST_MINED_BLOCK;
        Transaction output = getSignedTranscation(m);

        WebTarget target = restClient.target(serverURI).path(WalletService.PATH);

        short retries = 0;

        while (retries < MAX_RETRIES) {
            try {
                Response r = target.path("/mine/lastBlock").request().accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(Transaction.serialize(output), MediaType.APPLICATION_JSON));

                if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
                    SystemReply reply = r.readEntity(SystemReply.class);
                    String json = reply.getReplies().get(0).getValue();
                    
                    Type type = new TypeToken<Block>() {
                    }.getType();
                    Block b = gson.fromJson(json, type);
                    System.out.println(b.getId());
                    System.out.println(b.getTransactions().get(0).getOperation());
                    return b;
                } else {
                    retries++;
                    System.out.println("Error, HTTP error status: " + r.getStatus());
                }
            } catch (ProcessingException pe) { // Error in communication with server
                System.out.println("Timeout occurred.");
                pe.printStackTrace(); // Could be removed
                retries++;
                try {
                    Thread.sleep(RETRY_PERIOD); // wait until attempting again.
                } catch (InterruptedException e) {
                    // Nothing to be done here, if this happens we will just retry sooner.
                }
                System.out.println("Retrying to execute request.");
            }
        }
        return null;
    }

    private void sendMinedBlock(Block mined) {
        Client restClient = startClient();
        WebTarget target = restClient.target(serverURI).path(WalletService.PATH);

        byte[] sig = new byte[0];
        KeyPair kp = CryptoStuff.getKeyPair();
        try {
            sig = CryptoStuff.sign(kp.getPrivate(), mined.getProof());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        mined.setId(clientId);
        mined.setSig(sig);
        mined.setPub(kp.getPublic().getEncoded());

        short retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                Response r = target.path("/mine/block").request().accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(Block.serialize(mined), MediaType.APPLICATION_JSON));

                if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
                    SystemReply reply = r.readEntity(SystemReply.class);
                    System.out.println("Block mined!");
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
