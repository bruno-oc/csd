package server;

import api.rest.WalletService;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import resources.Wallet;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

public class CoinServer {

    // public static final int PORT = 8080;
    //public static final String ServerURI = "https://127.0.1.1:8443/";
    private static final Logger Log = Logger.getLogger(CoinServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 4) {
            System.out.println("Usage: CoinServer <filePath> <id> <ip> <port>");
            System.exit(-1);
        }

        /*
        String ip = InetAddress.getLocalHost().getHostAddress();
		*/
        String serverURI = String.format("https://%s:%s/", args[2], args[3]);

        HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());

        WalletService wallet = new Wallet(args[0], Integer.parseInt(args[1]));

        ResourceConfig config = new ResourceConfig();
        config.register(wallet);

        JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

        Log.info(String.format("%s Server ready @ %s\n", InetAddress.getLocalHost().getCanonicalHostName(), serverURI));
    }

}
