package csd.coin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import csd.coin.client.WalletClient;
import csd.coin.replicas.BFTServer;

@SpringBootApplication
public class CoinApplication {

	public static void main(String[] args) {
		String cmd = args[0];
		switch (cmd) {
			case "server":
				SpringApplication.run(CoinApplication.class, args);
				break;
			case "client":
				WalletClient.main(args);
				break;
			case "replica":
				System.out.println(args[1]);
				String[] str = {args[1]};
				BFTServer.main(str);
				break;
		}
	}

}
