package db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import api.Transaction;
import api.User;

public class DataBase {
	
    private Map<String, User> users;
    private List<Transaction> transactions;
    private Map<String, List<Transaction>> clientTranscations;
    private BufferedReader reader;

	public DataBase() {
		users = new HashMap<String, User>();
		transactions = new ArrayList<Transaction>();
		clientTranscations = new LinkedHashMap<String, List<Transaction>>();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public void addUser(String name, String password, double amount) {
		User user = new User(name, password, amount);
		users.put(name, user);
		
		Gson gson = new Gson();
		String jsonString = gson.toJson(users);
		System.out.println(jsonString);
		
		writeData("src/resources/users.json", jsonString);
	}
	
	public Map<String, User> getUsers() {
		String jsonString = readData("src/resources/users.json");;
		
		Gson gson = new Gson();
		Type type = new TypeToken<HashMap<String, User>>(){}.getType();
        HashMap<String, User> clonedMap = gson.fromJson(jsonString, type);
                
		return clonedMap;
	}
	
	public void addTransaction(String from, String to, double amount) {
		Transaction t = new Transaction(from, to, amount);
		transactions.add(t);
		
		Gson gson = new Gson();
		String jsonString = gson.toJson(transactions);
		System.out.println(jsonString);
		
		writeData("src/resources/transactions.json", jsonString);
	}
	
	public List<Transaction> getTransactions() {
		String jsonString = readData("src/resources/transactions.json");;
		
		Gson gson = new Gson();
		Type type = new TypeToken<List<Transaction>>(){}.getType();
		List<Transaction> list = gson.fromJson(jsonString, type);
		
		System.out.println(list.get(0).getFrom());
        
		return list;
	}
	
	public static void main(String[] args) {
		DataBase db = new DataBase();
		//db.addUser("Luis", "pwd", 69.0);
		//db.getUsers();
		
		//db.addTransaction("luis", "bruno", 10.0);
		db.getTransactions();
	}
	
}
