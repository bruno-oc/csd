package server.replica;

import api.Transaction;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ReplicaContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import crypto.CryptoStuff;
import db.DataBase;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class BFTServer extends DefaultSingleRecoverable {

    private final DataBase db;
    private ServiceReplica replica;

    public BFTServer(String filePath, int id) {
        db = new DataBase(filePath);
        replica = new ServiceReplica(id, this, this);
        ReplicaContext context = replica.getReplicaContext();

    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: BFTServer <filePath> <server id>");
            System.exit(-1);
        }
        new BFTServer(args[0], Integer.parseInt(args[1]));
    }

    private double clientAmount(String client) {
        double total = 0;
        List<Transaction> transactions = db.getLogs();
        String log;
        for (Transaction t : transactions) {
            log = t.getOperation();
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

    private void writeTransaction(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();

            CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());

            db.addLog(t);
            objOut.writeObject(clientAmount(t.getID()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getUserTransactions(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();
            List<Transaction> logs = db.getLogs();
            List<Transaction> clientLogs = new LinkedList<>();
            for (Transaction log : logs)
                if (log.contains(t.getID()))
                    clientLogs.add(log);
            db.addLog(t);
            objOut.writeObject(clientLogs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAllTransactions(ObjectInput objIn, ObjectOutput objOut) {
        try {
            List<Transaction> logs = db.getLogs();
            db.addLog((Transaction) objIn.readObject());
            objOut.writeObject(logs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getClientAmount(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();
            db.addLog(t);
            objOut.writeObject(clientAmount(t.getID()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // isto envia os logs com os hashes
    private void getHashedTransactions(ObjectInput objIn, ObjectOutput objOut) {
        System.out.println("Getting hashes");
        try {
            byte[] hash;
            List<Transaction> logs = db.getLogs();
            for (Transaction t : logs) {
                String id = t.getID();

                // TODO: Create hash of operation
                hash = "Operation hash".getBytes();
                t.setHash(hash);
                System.out.println(t);
            }

            db.addLog((Transaction) objIn.readObject());
            objOut.writeObject(logs);
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
                case GET_HASHED:
                    getHashedTransactions(objIn, objOut);
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
                case GET_HASHED:
                    getHashedTransactions(objIn, objOut);
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
            objOut.writeObject(db.getLogs());
            return byteOut.toByteArray();
        } catch (IOException e) {
            System.out.println("Error while taking snapshot:\n" + e);
        }
        return new byte[0];
    }
}
