package csd.coin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import csd.coin.client.WalletClient;
import csd.coin.replicas.BFTServer;

@SpringBootApplication
public class CoinApplication {

	public static void main(String[] args) {
		String cmd = args[0];
		if(cmd.equals("server")) {
			SpringApplication.run(CoinApplication.class, args);
		} else if(cmd.equals("client")) {
			WalletClient.main(args);
		} else if(cmd.equals("replica")) {
			System.out.println(args[1]);
			String[] str = {args[1]};
			BFTServer.main(str);
		}
	}

}
