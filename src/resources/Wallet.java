package resources;

import api.MySQLAccess;
import api.Transaction;
import api.rest.WalletService;

import java.util.List;

import java.sql.*;

public class Wallet implements WalletService {
    private MySQLAccess db;

    public Wallet() throws SQLException, ClassNotFoundException {
        db = new MySQLAccess();
    }

    @Override
    public double obtainCoins(String who, double amount) {
        System.out.println("=================== ENTREIIIIIIIIIIIIII ===================");
        db.addUser("bruno", "pass", 100);
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
