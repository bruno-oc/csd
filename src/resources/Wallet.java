package resources;

import api.Transaction;
import api.User;
import api.rest.WalletService;
import db.DataBase;

import java.util.List;
import java.util.Map;

public class Wallet implements WalletService {
	
	private DataBase db;

    public Wallet() {
        db = new DataBase();
    }

    @Override
    public double obtainCoins(String who, double amount) {
        System.out.println("obtaincoin");
        Map<String, User> users = db.getUsers();
        User u = users.get(who);
        double total = u.drawMoney(amount);
        db.saveUsers(users);
        
        return total;
    }

    @Override
    public double transferMoney(String from, String to, double amount) {
    	System.out.println("transferMoney");
    	Map<String, User> users = db.getUsers();
    	User u1 = users.get(from);
    	User u2 = users.get(to);
    	u1.drawMoney(amount);
    	u2.deposit(amount);
    	u1.addTransaction(from, to, amount);
    	u2.addTransaction(from, to, amount);
    	db.saveUsers(users);
    	db.addTransaction(from, to, amount);
    	
        return 0; //idk what is supposed to be returned
    }

    @Override
    public double currentAmount(String who) {
    	System.out.println("currentAmount");
    	Map<String, User> users = db.getUsers();
    	User u = users.get(who);
        return u.getAmount();
    }

    @Override
    public List<Transaction> ledgerOfGlobalTransactions() {
    	System.out.println("ledgerOfGlobalTransactions");
        return db.getTransactions();
    }

    @Override
    public List<Transaction> ledgerOfClientTransactions(String who) {
    	System.out.println("ledgerOfClientTransactions");
    	Map<String, User> users = db.getUsers();
    	User u = users.get(who);
        return u.getTransactions();
    }
    
    @Override
    public double minerateMoney(String who) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void installSmartContract(String who, String smart_contract) {
        // TODO Auto-generated method stub

    }

    @Override
    public void transferMoneyWithSmartContract(String from, String to, double amount, String smart_contract_ref) {
        // TODO Auto-generated method stub

    }
}
