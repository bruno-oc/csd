package db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import api.Transaction;
import api.User;
import resources.Wallet;

public class DataBase {
	
    private Map<String, User> users;
    private List<Transaction> transactions;

	public DataBase() {
		users = getUsers();
		transactions = getTransactions();
		//clientTranscations = new LinkedHashMap<String, List<Transaction>>();
	}
	
	private void writeData(String path, String data) {
		//Write JSON file
        try (FileWriter file = new FileWriter(path)) {
            file.write(data); 
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private String readData(String path) {
		try {
			Scanner scanner = new Scanner(new File(path));
			return scanner.useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public void addUser(String name, String password, double amount) {
		User user = new User(name, password, amount);
		users.put(name, user);
		saveUsers(users);
	}
	
	public Map<String, User> getUsers() {
		String jsonString = readData("src/resources/users.json");;
		
		Gson gson = new Gson();
		Type type = new TypeToken<HashMap<String, User>>(){}.getType();
        HashMap<String, User> clonedMap = gson.fromJson(jsonString, type);
                
		return clonedMap;
	}
	
	public void saveUsers(Map<String, User> users) {
		Gson gson = new Gson();
		String jsonString = gson.toJson(users);
		
		writeData("src/resources/users.json", jsonString);
	}
	
	public void addTransaction(String from, String to, double amount) {
		Transaction t = new Transaction(from, to, amount);
		transactions.add(t);
		
		Gson gson = new Gson();
		String jsonString = gson.toJson(transactions);
		
		writeData("src/resources/transactions.json", jsonString);
	}
	
	public List<Transaction> getTransactions() {
		String jsonString = readData("src/resources/transactions.json");;
		
		Gson gson = new Gson();
		Type type = new TypeToken<List<Transaction>>(){}.getType();
		List<Transaction> list = gson.fromJson(jsonString, type);
		        
		return list;
	}
	
	public static void main(String[] args) {
		DataBase db = new DataBase();
		db.addUser("Luis", "pwd", 69.0);
		db.addUser("Bruno", "pwd", 69.0);
		//db.getUsers();
		
		//db.addTransaction("luis", "bruno", 10.0);
		//db.getTransactions();
		
		Wallet w = new Wallet();
		w.obtainCoins("Luis", 9);
		w.transferMoney("Luis", "Bruno", 10);
		System.out.println(w.currentAmount("Luis"));
	}
	
}
