package server.replica;

import api.Block;
import api.SmartContract;
import api.Transaction;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import bftsmart.tom.util.TOMUtil;
import com.google.gson.Gson;
import crypto.CryptoStuff;
import db.DataBase;

import java.io.*;
import java.security.KeyPair;
import java.util.*;

public class BFTServer extends DefaultSingleRecoverable {

    private final DataBase db;
    private final DataBase blocksDB;
    private final int id;
    private final Gson gson;

    public BFTServer(int id) {
        String filePath = "src/server/replica/bft_log_transactions_" + id + ".json";
        db = new DataBase(filePath);
        filePath = "src/server/replica/bft_log_blocks_" + id + ".json";
        blocksDB = new DataBase(filePath);
        this.id = id;
        gson = new Gson();

        new ServiceReplica(id, "config/server", this, this, null, null, null);
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

        for (Block b : minedBlocks) {
            List<Transaction> transactions = b.getTransactions();
            String log;
            for (Transaction t : transactions) {
                if (t.getType() != Transaction.NORMAL)
                    continue;
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

            String[] str = t.getOperation().split(" ");
            String val = str[str.length - 1];
            System.out.println("cipher = " + val);
            if (t.getType() == Transaction.NORMAL)
                val = "" + clientAmount(client);

            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), val,
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

            CryptoStuff.verifySignature(CryptoStuff.getPublicKey(b.getPub()), Transaction.serialize(b.getTransactions()), b.getSig());

            List<Transaction> closedTransactions = b.getTransactions();
            if (closedTransactions.size() < Block.MINIMUM_TRANSACTIONS) {
                System.out.println("Block does not contain the minimum transactions!");
                return false;
            }

            if (!Block.proofOfWork(b)) {
                System.out.println("Block does not prove the work!");
                return false;
            }

            System.out.println("ID: " + b.getId());
            boolean removed = db.remove(closedTransactions);
            System.out.println("Removed: " + removed);
            if (!b.getId().equals("SYSTEM_INIT") && !removed)
                return false;

            blocksDB.addLog(b);

            double val = b.getTransactions().size() * 5;
            String op = String.format(Transaction.OBTAIN_COIN, b.getId(), val);

            KeyPair kp = CryptoStuff.getKeyPair();
            byte[] sig = CryptoStuff.sign(kp.getPrivate(), op.getBytes());
            Transaction t = new Transaction(b.getId(), op, sig, kp.getPublic().getEncoded());
            db.addLog(t);

            ReplicaReply reply = new ReplicaReply(id, op, "" + val,
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

    private boolean getPrivateTransactions(ObjectInput objIn, ObjectOutput objOut) {

        try {
            Transaction t = (Transaction) objIn.readObject();
            String client = (String) objIn.readObject();
            System.out.println("Searching for private transactions of : " + client);

            CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

            List<Block> minedBlocks = blocksDB.getLogsBlocks();
            List<Transaction> privateTransactions = new LinkedList<>(), temp = new LinkedList<>();
            
            for (Block b : minedBlocks) {
            	List<Transaction> logs = b.getTransactions();
            	for (Transaction log : logs)
                    if (log.getType() == Transaction.HOMOMORPHIC &&
                            Arrays.stream(log.getOperation().split(" ")).anyMatch(client::equals)) {
                    	temp.add(log.getTransTypeOne());
                    	privateTransactions.add(log);
                    }
            }
            
            for (Block b : minedBlocks) {
            	List<Transaction> logs = b.getTransactions();
	            for (Transaction log : logs)
	                if (log.getType() == Transaction.SYMMETRIC &&
	                        Arrays.stream(log.getOperation().split(" ")).anyMatch(client::equals) &&
	                        !temp.contains(log)) {
	                	privateTransactions.add(log);
	                }
            }
                	

            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), gson.toJson(privateTransactions),
                    TOMUtil.computeHash(t.getOperation().getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), t.getOperation().getBytes()));

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
                    if (n >= lastN)
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
            if (lastN < logs.size())
                temp = logs.subList(logs.size() - lastN, logs.size());

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
            Block b = blocks.get(blocks.size() - 1);
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
            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), "" + val,
                    TOMUtil.computeHash(t.getOperation().getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), t.getOperation().getBytes()));
            objOut.writeObject(reply);
            return true;
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return false;
    }

    private boolean getNotMinedTransactions(ObjectInput objIn, ObjectOutput objOut) {
        try {
            Transaction t = (Transaction) objIn.readObject();
            int n = (int) objIn.readObject();

            CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

            List<Transaction> logs = db.getLogsTransactions();

            if (n > logs.size())
                n = logs.size();

            List<Transaction> temp = logs.subList(0, n);
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

    private boolean writeTransactionWithSmartContract(ObjectInput objIn, ObjectOutput objOut) {
        try {
            int serverId = (Integer) objIn.readObject();

            String ref = (String) objIn.readObject();
            int index = Integer.parseInt(ref);

            Transaction t = (Transaction) objIn.readObject();
            String client = (String) objIn.readObject();

            CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

            DataBase smartContractsDB = new DataBase("src/server/verified_contracts_" + serverId + ".json");
            List<SmartContract> smartContracts = smartContractsDB.getSmartContracts();

            SmartContract sc = smartContracts.get(index);
            if (!sc.run(t) && index < smartContracts.size()) {
                System.out.println("Smart Contract Unauthorized");
                return false;
            }

            db.addLog(t);
            double val = clientAmount(client);
            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), "" + val,
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
                case TRANSFER_PRIVATE:
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
                case GET_NOT_MINED:
                    hasReply = getNotMinedTransactions(objIn, objOut);
                    break;
                case SMART_CONTRACT:
                    hasReply = writeTransactionWithSmartContract(objIn, objOut);
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
                case GET_NOT_MINED:
                    hasReply = getNotMinedTransactions(objIn, objOut);
                    break;
                case GET_PRIVATE:
                    hasReply = getPrivateTransactions(objIn, objOut);
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