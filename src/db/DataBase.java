package db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import api.Block;
import api.Transaction;
import server.SystemReply;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Scanner;

public class DataBase {

    private final String filePath;
    private Gson gson;

    public DataBase(String filePath) {
        this.filePath = filePath;
        gson = new Gson();
        File f = new File(filePath);
        if(!f.exists()) {
        	try {
				f.createNewFile();
				FileWriter w = new FileWriter(f);
	            w.write("[]");
	            w.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
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

    public void addLog(Transaction log) {
        List<Transaction> logs = getLogsTransactions();
        logs.add(log);
        String jsonString = gson.toJson(logs);
        writeData(filePath, jsonString);
    }
    
    public void addLog(Block log) {
        List<Block> logs = getLogsBlocks();
        logs.add(log);
        String jsonString = gson.toJson(logs);
        writeData(filePath, jsonString);
    }

    public List<Transaction> getLogsTransactions() {
        String jsonString = readData(filePath);
        
        Type type = new TypeToken<List<Transaction>>() {
        }.getType();
        return gson.fromJson(jsonString, type);
    }
    
    public List<Block> getLogsBlocks() {
        String jsonString = readData(filePath);
        
        Type type = new TypeToken<List<Block>>() {
        }.getType();
        return gson.fromJson(jsonString, type);
    }
    
    public void addLog(SystemReply log) {
        List<SystemReply> logs = getLogsSystemReply();
        logs.add(log);
        String jsonString = gson.toJson(logs);
        writeData(filePath, jsonString);
    }
    
    public List<SystemReply> getLogsSystemReply() {
        String jsonString = readData(filePath);
        
        Type type = new TypeToken<List<Transaction>>() {
        }.getType();
        return gson.fromJson(jsonString, type);
    }

    public void clear() {
        try {
            FileWriter w = new FileWriter(filePath);
            w.write("[]");
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
