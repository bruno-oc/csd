package resources;

import api.rest.WalletService;
import bftsmart.tom.ServiceProxy;
import db.DataBase;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

public class Wallet implements WalletService {
	
	private DataBase db;
	private ServiceProxy proxy;

    public Wallet() {
        db = new DataBase();
        proxy = new ServiceProxy(0);
    }

    @Override
    public double obtainCoins(String who, double amount) {
        System.out.println("obtainCoins");
        String log = "obtainCoins: who = " + who + " amount = " + amount;  
        db.addLog(log);

        try {
            byte[] reply = proxy.invokeOrdered(log.getBytes());
            if (reply != null) {
                int newValue = new DataInputStream(new ByteArrayInputStream(reply)).readInt();
                System.out.println(", returned value: " + newValue);
            } else {
                System.out.println(", ERROR! Exiting.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getCurrentAmount(who);
    }

    @Override
    public double transferMoney(String from, String to, double amount) {
    	System.out.println("transferMoney");
    	String log = "transferMoney: from = " + from + " to = " + to + " amount = " + amount;  
        db.addLog(log);

        return amount;
    }
    
    private double getCurrentAmount(String who) {
    	double amount = 0;
    	List<String> logs = db.getLogs();
    	for(String log : logs) {
    		System.out.println(log);
    		if(log.contains(who) && log.contains("amount")) {
    			String str = log.replaceAll("\\D+",".");
				str = str.substring(1, str.length()-1);
				double temp = Double.parseDouble(str);
    			if(log.contains("obtainCoins")) {
    				amount += temp;
    			}
    			if(log.contains("from = " + who)) {
    				amount -= temp;
    			}
    			if(log.contains("to = " + who)) {
    				amount += temp;
    			}
    		}
    	}
    	return amount;
    }

    @Override
    public double currentAmount(String who) {
    	System.out.println("currentAmount");
        return getCurrentAmount(who);
    }

    @Override
    public List<String> ledgerOfGlobalTransactions() {
    	System.out.println("ledgerOfGlobalTransactions");
        return db.getLogs();
    }

    @Override
    public List<String> ledgerOfClientTransactions(String who) {
    	List<String> temp = new ArrayList<String>();
    	List<String> transactions = db.getLogs();;
    	for(String log : transactions) {
    		if(log.contains(who)) {
    			temp.add(log);
    		}
    	}
    	return temp;
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
