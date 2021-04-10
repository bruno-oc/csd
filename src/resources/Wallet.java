package resources;
import api.Transaction;
import api.rest.WalletService;
import java.util.List;

public class Wallet implements WalletService {

    @Override
    public double obtainCoins(String who, double amount) {
        return 0;
    }

    @Override
    public double transferMoney(String from, String to, double amount) {
        return 0;
    }

    @Override
    public double currentAmount(String who) {
        return 0;
    }

    @Override
    public List<Transaction> ledgerOfGlobalTransactions() {
        return null;
    }

    @Override
    public List<Transaction> ledgerOfClientTransactions(String who) {
        return null;
    }
}
