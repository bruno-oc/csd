package db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Scanner;

public class DataBase {

    private final String filePath;

    public DataBase(String filePath) {
        this.filePath = filePath;
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
        System.out.println("WRITING::: " + data);
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
        List<String> logs = getLogs();
        logs.add(log);
        Gson gson = new Gson();
        String jsonString = gson.toJson(logs);
        writeData(filePath, jsonString);
    }

    public List<String> getLogs() {
        String jsonString = readData(filePath);

        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {
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
