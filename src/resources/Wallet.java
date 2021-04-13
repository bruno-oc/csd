package resources;

import api.Transaction;
import api.User;
import api.rest.WalletService;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

import java.sql.*;
import java.util.Map;

public class Wallet implements WalletService {
    private Gson gson;
    private Map<String, User> users;
    private List<Transaction> transactions;
    private Map<String, List<Transaction>> clientTranscations;
    private BufferedReader reader;

    public Wallet() throws SQLException, ClassNotFoundException, FileNotFoundException {
        gson = new Gson();
        reader = new BufferedReader(new FileReader("/users.json"));
        users = gson.fromJson(reader, HashMap.class);
    }

    @Override
    public double obtainCoins(String who, double amount) {
        System.out.println("=================== ENTREIIIIIIIIIIIIII ===================");
        double total = users.get(who).deposit(amount);
        // TODO: write new amount to file
        return total;
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
