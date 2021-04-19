package api;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private String password;
    private double amount;
    private List<Transaction> transactions;

    public User(String name, String password, double amount) {
        this.name = name;
        this.password = password;
        this.amount = amount;
        this.transactions = new ArrayList<Transaction>();
    }

    public String getName() {
        return name;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public double getAmount() {
        return amount;
    }

    public double deposit(double amount) {
        this.amount += amount;
        return this.amount;
    }
    
    public double drawMoney(double amount) {
    	this.amount -= amount;
        return this.amount;
    }
    
    public List<Transaction> getTransactions(){
    	return transactions;
    }
    
    public void addTransaction(String from, String to, double amount) {
    	Transaction t = new Transaction(from, to, amount);
    	transactions.add(t);
    }
}
