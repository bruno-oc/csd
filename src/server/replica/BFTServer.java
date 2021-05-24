package server.replica;

import api.Block;
import api.Transaction;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import bftsmart.tom.util.TOMUtil;
import crypto.CryptoStuff;
import db.DataBase;

import java.io.*;
import java.security.KeyPair;
import java.util.*;

import com.google.gson.Gson;

public class BFTServer extends DefaultSingleRecoverable {

    private final DataBase db, blocksDB;
    private final int id;
    private Gson gson;

    public BFTServer(int id) {
    	String filePath = "src/server/replica/bft_log_transactions_" + id + ".json";
        db = new DataBase(filePath);
        filePath = "src/server/replica/bft_log_blocks_" + id + ".json";
        blocksDB = new DataBase(filePath);
        this.id = id;
        gson = new Gson();
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
        List<Block> minedBlocks = blocksDB.getLogsBlocks();

        for(Block b : minedBlocks) {
            List<Transaction> transactions = b.getTransactions();
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
        }
        return total;
    }

    private boolean writeTransaction(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();
            String client = (String) objIn.readObject();

            CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

            db.addLog(t);
            double val = clientAmount(client);
            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), ""+val,
                    TOMUtil.computeHash(t.getOperation().getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), t.getOperation().getBytes()));
            objOut.writeObject(reply);
            return true;
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return false;
    }
    
    private boolean writeBlock(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Block b = (Block) objIn.readObject();
            
            CryptoStuff.verifySignature(CryptoStuff.getPublicKey(b.getPub()), b.getProof(), b.getSig());

            blocksDB.addLog(b);
            List<Transaction> closedTransactions = b.getTransactions();
            System.out.println("ID: " + b.getId());
            boolean removed = db.remove(closedTransactions);
            System.out.println("Removed: " + removed);
            if(!b.getId().equals("SYSTEM_INIT") && !removed)
                return false;
            
            double val = b.getTransactions().size() * 5;
            String op = String.format(Transaction.OBTAIN_COIN, b.getId(), val);
            
            KeyPair kp = CryptoStuff.getKeyPair();
            byte[] sig = CryptoStuff.sign(kp.getPrivate(), op.getBytes());
            Transaction t = new Transaction(b.getId(), op, sig, kp.getPublic().getEncoded());
            db.addLog(t);
            
            ReplicaReply reply = new ReplicaReply(id, op, ""+val, 
            		TOMUtil.computeHash(op.getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), op.getBytes()));
            System.out.println("return writeBlock");
            objOut.writeObject(reply);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean getUserTransactions(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();
            String client = (String) objIn.readObject();
            int lastN = (int) objIn.readObject();
            
            CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

            List<Transaction> logs = db.getLogsTransactions(), clientLogs = new LinkedList<>();
            Collections.reverse(logs);
            int n = 0;
            for (Transaction log : logs) {
                if (Arrays.stream(log.getOperation().split(" ")).anyMatch(client::equals)) {
                    clientLogs.add(log);
                    n++;
                    if(n >= lastN)
                    	break;
                }
            }
            Collections.reverse(clientLogs);

            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), gson.toJson(clientLogs),
                    TOMUtil.computeHash(t.getOperation().getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), t.getOperation().getBytes()));

            objOut.writeObject(reply);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean getAllTransactions(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();
            int lastN = (int) objIn.readObject();
            
            CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

            List<Transaction> logs = db.getLogsTransactions();
            List<Transaction> temp = logs;
            if(lastN < logs.size())
            	temp = logs.subList(logs.size()-lastN, logs.size());
            
            List<Transaction> jsonElement = new ArrayList<Transaction>(temp);
            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), gson.toJson(jsonElement),
                    TOMUtil.computeHash(t.getOperation().getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), t.getOperation().getBytes()));

            objOut.writeObject(reply);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean getLastBlock(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();
            
            CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

            List<Block> blocks = blocksDB.getLogsBlocks();
            Block b = blocks.get(blocks.size()-1);
            System.out.println(b.getId());
            System.out.println(b.getTransactions().get(0).getOperation());

            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), gson.toJson(b),
                    TOMUtil.computeHash(t.getOperation().getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), t.getOperation().getBytes()));

            objOut.writeObject(reply);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean getClientAmount(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();
            String client = (String) objIn.readObject();
            
            CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());
            
            double val = clientAmount(client);
            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), ""+val,
                    TOMUtil.computeHash(t.getOperation().getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), t.getOperation().getBytes()));
            objOut.writeObject(reply);
            return true;
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return false;
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
                    hasReply = writeTransaction(objIn, objOut);
                    break;
                case MINE:
                    hasReply = writeBlock(objIn, objOut);
                    break;
                case CLIENT_AMOUNT:
                    hasReply = getClientAmount(objIn, objOut);
                    break;
                case GET:
                    hasReply = getUserTransactions(objIn, objOut);
                    break;
                case GET_ALL:
                    hasReply = getAllTransactions(objIn, objOut);
                    break;
            }
            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } else {
                System.out.println("Sending empty reply");
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
                    hasReply = getClientAmount(objIn, objOut);
                    break;
                case GET:
                    hasReply = getUserTransactions(objIn, objOut);
                    break;
                case GET_ALL:
                    hasReply = getAllTransactions(objIn, objOut);
                    break;
                case GET_LAST_BLOCK:
                    hasReply = getLastBlock(objIn, objOut);
                    break;
            }
            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } else {
                System.out.println("Sending empty reply");
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