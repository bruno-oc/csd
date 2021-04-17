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
	
    private List<String> logs;

	public DataBase() {
		//logs = new ArrayList<String>();
		logs = getLogs();
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
	
	public void addLog(String log) {
		logs.add(log);
		Gson gson = new Gson();
		String jsonString = gson.toJson(logs);
		writeData("src/resources/logs.json", jsonString);
	}
	
	public List<String> getLogs() {
		String jsonString = readData("src/resources/logs.json");;
		Gson gson = new Gson();
		Type type = new TypeToken<List<String>>(){}.getType();
		List<String> list = gson.fromJson(jsonString, type);   
		return list;
	}
	
	public static void main(String[] args) {
		DataBase db = new DataBase();
		db.addLog("bla bla bla");
		db.addLog("log2");
		db.addLog("teste = sdcsf");
		
		List<String> logs = db.getLogs();
		System.out.println(logs.get(2));
		
		String str = "bla bla 4.56 bla bla";
		str = str.replaceAll("\\D+",".");
		str = str.substring(1, str.length()-1);
		System.out.println(str);
	}
	
}
