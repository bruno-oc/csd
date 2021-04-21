package server.replica;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import crypto.Message;
import db.DataBase;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;

import api.Transaction;

public class BFTServer extends DefaultSingleRecoverable {

    private final DataBase db;

    public BFTServer(String filePath, int id) {
        db = new DataBase(filePath);
        new ServiceReplica(id, this, this);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: BFTServer <filePath> <server id>");
            System.exit(-1);
        }
        new BFTServer(args[0], Integer.parseInt(args[1]));
    }

    private double clientAmount(String client) {
    	//TODO: converter o file para List<Transaction> e iterar pelas op
        double total = 0;
        List<String> logs = db.getLogs();
        for (String log : logs) {
            if (log.contains(client) && (log.contains("obtainCoins") || log.contains("transferMoney"))) {
                System.out.println("log=" + log);
                String[] str = log.split(" ");
                double temp = Double.parseDouble(str[str.length - 1]);

                if (log.contains("obtainCoins"))
                    total += temp;
                if (log.contains("from " + client))
                    total -= temp;
                if (log.contains("to " + client))
                    total += temp;
            }
        }
        return total;
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        boolean hasReply = false;
        String client;
        double amount;
        Message m;
        List<String> logs;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            RequestType reqType = (RequestType) objIn.readObject();
            switch (reqType) {
                case OBTAIN_COINS:
                    client = (String) objIn.readObject();
                    m = (Message) objIn.readObject();
                    List<byte[]> l = m.getMessage();
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(new Transaction(new String(l.get(0)), new String(l.get(1)), l.get(2)));
                    db.addLog(jsonString);
                    objOut.writeObject(clientAmount(client));
                    hasReply = true;
                    break;
                case TRANSFER:
                    client = (String) objIn.readObject();
                    String to = (String) objIn.readObject();
                    amount = (double) objIn.readObject();
                    db.addLog("transferMoney from " + client + " to " + to + " " + amount);
                    objOut.writeObject(amount);
                    hasReply = true;
                    break;
                case CLIENT_AMOUNT:
                    client = (String) objIn.readObject();
                    db.addLog("currentAmount " + client);
                    objOut.writeObject(clientAmount(client));
                    hasReply = true;
                    break;
                case GET:
                    client = (String) objIn.readObject();
                    logs = db.getLogs();
                    List<String> clientLogs = new LinkedList<>();
                    for (String log : logs)
                        if (log.contains(client))
                            clientLogs.add(log);
                    db.addLog("ledgerOfClientTransactions " + client);
                    objOut.writeObject(clientLogs);
                    hasReply = true;
                    break;
                case GET_ALL:
                    logs = db.getLogs();
                    db.addLog("ledgerOfGlobalTransactions");
                    objOut.writeObject(logs);
                    hasReply = true;
                    break;
            }
            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } else {
                reply = new byte[0];
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error curred during operation execution:\n" + e);
        }
        return reply;
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        boolean hasReply = false;
        String client;
        List<String> logs;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            RequestType reqType = (RequestType) objIn.readObject();
            switch (reqType) {
                case CLIENT_AMOUNT:
                    client = (String) objIn.readObject();
                    db.addLog("currentAmount " + client);
                    objOut.writeObject(clientAmount(client));
                    hasReply = true;
                    break;
                case GET:
                    client = (String) objIn.readObject();
                    logs = db.getLogs();
                    List<String> clientLogs = new LinkedList<>();
                    for (String log : logs)
                        if (log.contains(client))
                            clientLogs.add(log);
                    db.addLog("ledgerOfClientTransactions " + client);
                    objOut.writeObject(clientLogs);
                    hasReply = true;
                    break;
                case GET_ALL:
                    logs = db.getLogs();
                    db.addLog("ledgerOfGlobalTransactions");
                    objOut.writeObject(logs);
                    hasReply = true;
                    break;
            }
            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } else {
                reply = new byte[0];
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error curred during operation execution:\n" + e);
        }
        return reply;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void installSnapshot(byte[] state) {
        System.out.println("-----------------------------ERRO------------------------------");
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(state);
             ObjectInput objIn = new ObjectInputStream(byteIn)) {
            db.clear();
            List<String> logs = (List<String>) objIn.readObject();
            for(String log : logs)
                db.addLog(log);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error while installing snapshot:\n" + e);
        }
    }

    @Override
    public byte[] getSnapshot() {
        System.out.println("-----------------------------GET------------------------------");
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            objOut.writeObject(db.getLogs());
            return byteOut.toByteArray();
        } catch (IOException e) {
            System.out.println("Error while taking snapshot:\n" + e);
        }
        return new byte[0];
    }
}
