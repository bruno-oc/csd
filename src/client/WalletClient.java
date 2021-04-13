package client;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import api.rest.WalletService;
import server.CoinServer;
import server.InsecureHostnameVerifier;

public class WalletClient {
	
	public final static int MAX_RETRIES = 3;
	public final static long RETRY_PERIOD = 10000;
	public final static int CONNECTION_TIMEOUT = 10000;
	public final static int REPLY_TIMEOUT = 6000;

	private static Client restClient;

	public WalletClient() {
		restClient = this.startClient();
	}
	
	private Client startClient() {
		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
		ClientConfig config = new ClientConfig();
		// How much time until timeout on opening the TCP connection to the server
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT);
		// How much time to wait for the reply of the server after sending the request
		config.property(ClientProperties.READ_TIMEOUT, REPLY_TIMEOUT);

		return ClientBuilder.newClient(config);
	}
	
	public double obtainCoin(String who, double amount) {
		
		WebTarget target = restClient.target(CoinServer.ServerURI).path(WalletService.PATH);

		short retries = 0;

		while (retries < MAX_RETRIES) {
			try {

				Response r = target.path("/obtain/" + who).request().accept(MediaType.APPLICATION_JSON).post(Entity.entity(amount, MediaType.APPLICATION_JSON));

				if (r.getStatus() == Status.NO_CONTENT.getStatusCode() && r.hasEntity())
					System.out.println("UserInBox Success, message posted with id: " + r.readEntity(Long.class));
				else {
					System.out.println("Error, HTTP error status: " + r.getStatus());
				}

				return r.readEntity(Double.class);
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
		return -1;
	}
	
	public static void main(String[] args) {
		WalletClient c = new WalletClient();
		c.obtainCoin("bruno", 10);
	}
}
