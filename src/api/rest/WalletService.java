package api.rest;
import api.Transaction;

import java.util.List;

@Path(WalletService.PATH)
public interface WalletService {

    String PATH = "/wallet";
    String HEADER_VERSION = "CoinServer";

    double obtainCoins (String who, double amount);

    double transferMoney (String from, String to, double amount);

    double currentAmount (String who);

    List<Transaction> ledgerOfGlobalTransactions ();

    List<Transaction> ledgerOfClientTransactions(String who);
}
