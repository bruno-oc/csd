package server.replica;

import api.Transaction;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import bftsmart.tom.util.TOMUtil;
import crypto.CryptoStuff;
import db.DataBase;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BFTServer extends DefaultSingleRecoverable {

    private final DataBase db;
    private final int id;

    public BFTServer(int id) {
    	String filePath = "src/server/replica/bft_log" + id + ".json";
        db = new DataBase(filePath);
        this.id = id;
        new ServiceReplica(id, this, this);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: BFTServer <server id>");
            System.exit(-1);
        }
        new BFTServer(Integer.parseInt(args[0]));
    }

    private double clientAmount(String client) {
        double total = 0;
        List<Transaction> transactions = db.getLogsTransactions();
        String log;
        for (Transaction t : transactions) {
            log = t.getOperation();
            if (Arrays.stream(log.split(" ")).anyMatch(client::equals)
                    && (log.contains("obtainCoins") || log.contains("transferMoney"))) {
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

    private void writeTransaction(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();
            String client = (String) objIn.readObject();

            CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());

            db.addLog(t);
            double val = clientAmount(client);
            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), val,
                    TOMUtil.computeHash(t.getOperation().getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), t.getOperation().getBytes()));
            objOut.writeObject(reply);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getUserTransactions(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();
            String client = (String) objIn.readObject();
            
            CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());

            List<Transaction> logs = db.getLogsTransactions(), clientLogs = new LinkedList<>();
            for (Transaction log : logs)
                if (Arrays.stream(log.getOperation().split(" ")).anyMatch(client::equals))
                    clientLogs.add(log);
            db.addLog(t);

            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), clientLogs,
                    TOMUtil.computeHash(t.getOperation().getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), t.getOperation().getBytes()));

            objOut.writeObject(reply);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAllTransactions(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();
            
            CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());

            List<Transaction> logs = db.getLogsTransactions();
            db.addLog(t);

            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), logs,
                    TOMUtil.computeHash(t.getOperation().getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), t.getOperation().getBytes()));

            objOut.writeObject(reply);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getClientAmount(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();
            String client = (String) objIn.readObject();
            
            CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());
            
            db.addLog(t);
            double val = clientAmount(client);
            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), val,
                    TOMUtil.computeHash(t.getOperation().getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), t.getOperation().getBytes()));
            objOut.writeObject(reply);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        boolean hasReply = false;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            RequestType reqType = (RequestType) objIn.readObject();
            switch (reqType) {
                case OBTAIN_COINS:
                case TRANSFER:
                    writeTransaction(objIn, objOut);
                    hasReply = true;
                    break;
                case CLIENT_AMOUNT:
                    getClientAmount(objIn, objOut);
                    hasReply = true;
                    break;
                case GET:
                    getUserTransactions(objIn, objOut);
                    hasReply = true;
                    break;
                case GET_ALL:
                    getAllTransactions(objIn, objOut);
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
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            RequestType reqType = (RequestType) objIn.readObject();
            switch (reqType) {
                case CLIENT_AMOUNT:
                    getClientAmount(objIn, objOut);
                    hasReply = true;
                    break;
                case GET:
                    getUserTransactions(objIn, objOut);
                    hasReply = true;
                    break;
                case GET_ALL:
                    getAllTransactions(objIn, objOut);
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
        System.out.println("-----------------------------ERROR------------------------------");
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(state);
             ObjectInput objIn = new ObjectInputStream(byteIn)) {
            db.clear();
            List<Transaction> logs = (List<Transaction>) objIn.readObject();
            for (Transaction log : logs)
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
            objOut.writeObject(db.getLogsTransactions());
            return byteOut.toByteArray();
        } catch (IOException e) {
            System.out.println("Error while taking snapshot:\n" + e);
        }
        return new byte[0];
    }
}