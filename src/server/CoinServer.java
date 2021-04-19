package server;

import api.rest.WalletService;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import resources.Wallet;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.URI;
import java.security.KeyStore;
import java.util.logging.Logger;

public class CoinServer {

    // public static final int PORT = 8080;
    public static final String ServerURI = "https://127.0.1.1:8443/";
    private static final Logger Log = Logger.getLogger(CoinServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
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

    public static void main(String[] args) throws Exception {

        /*
        String ip = InetAddress.getLocalHost().getHostAddress();

        String serverURI = String.format("https://%s:%s", ip, PORT)
        */
        HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());

        WalletService wallet = new Wallet(Integer.parseInt(args[0]));

        ResourceConfig config = new ResourceConfig();
        config.register(wallet);

        SSLContext context = getContext();
        JdkHttpServerFactory.createHttpServer(URI.create(ServerURI), config, SSLContext.getDefault());

        Log.info(String.format("%s Server ready @ %s\n", InetAddress.getLocalHost().getCanonicalHostName(), ServerURI));
    }

}
