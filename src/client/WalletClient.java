package client;

import java.util.Scanner;

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
	
	public void obtainCoin(String who, double amount) {
		
		WebTarget target = restClient.target(CoinServer.ServerURI).path(WalletService.PATH);

		short retries = 0;

		while (retries < MAX_RETRIES) {
			try {

				Response r = target.path("/obtain/" + who).request().accept(MediaType.APPLICATION_JSON).post(Entity.entity(amount, MediaType.APPLICATION_JSON));

				if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
					System.out.println(who + " withdraw " + r.readEntity(Double.class));
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

				Response r = target.path("/transfer/" + from).queryParam("to", to).request().accept(MediaType.APPLICATION_JSON).post(Entity.entity(amount, MediaType.APPLICATION_JSON));
				
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
	
	public static void main(String[] args) {
		WalletClient w = new WalletClient();
		Scanner s = new Scanner(System.in);
		String input;
		do {
			input = s.nextLine();
			String[] inputs = input.split(" ");
			switch (inputs[0]) {
			case "obtainCoins":
				double amount = Double.parseDouble(inputs[2]);  
				w.obtainCoin(inputs[1], amount);
				break;
			case "transferMoney":
				double amount1 = Double.parseDouble(inputs[3]);
				w.transferMoney(inputs[1], inputs[2], amount1);
				break;
			case "currentAmount":
				w.currentAmount(inputs[1]);
				break;
			case "ledgerOfGlobalTransactions":
				w.ledgerTransactions("");
				break;
			case "ledgerOfClientTransactions":
				w.ledgerTransactions(inputs[1]);
				break;

			default:
				break;
			}
		} while(!input.equalsIgnoreCase("exit"));
	}
}
