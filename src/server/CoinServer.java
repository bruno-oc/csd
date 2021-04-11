package server;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import api.rest.WalletService;
import resources.Wallet;

public class CoinServer {
	
	private static Logger Log = Logger.getLogger(CoinServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}
	
	public static final int PORT = 8080;
	public static final String ServerURI = "https://127.0.1.1:8080/coin";
	
	public static void main(String[] args) throws UnknownHostException {
		
		String ip = InetAddress.getLocalHost().getHostAddress();

		//String serverURI = String.format("https://%s:%s", ip, PORT);
        
        WalletService wallet = new Wallet();

        HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
        
        ResourceConfig config = new ResourceConfig();
        config.register(wallet);

        try {
			JdkHttpServerFactory.createHttpServer( URI.create(ServerURI), config, SSLContext.getDefault());
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Invalid SSL/TLS configuration");
			e.printStackTrace();
		}
      
        Log.info(String.format("%s Server ready @ %s\n",  InetAddress.getLocalHost().getCanonicalHostName(), ServerURI));     

	}
	
}
