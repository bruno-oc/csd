package resources;

import api.Transaction;
import api.rest.WalletService;
import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;
import crypto.CryptoStuff;
import db.DataBase;
import server.ReplyListenerImp;
import server.SystemReply;
import server.replica.RequestType;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Wallet implements WalletService {

    private final DataBase db;
    private final ServiceProxy serviceProxy;
    private final AsynchServiceProxy asynchServiceProxy;
    private final ReplyListener replyListener;
    private final BlockingQueue<SystemReply> replyChain;

    public Wallet(String filePath, int id) {
        db = new DataBase(filePath);
        serviceProxy = new ServiceProxy(id);
        asynchServiceProxy = new AsynchServiceProxy(id);
        replyChain = new LinkedBlockingDeque<>();
        replyListener = new ReplyListenerImp(replyChain, asynchServiceProxy);
    }

    public void getHash(byte[] data) {
        // get a new replyListener ready
        replyListener.reset();

        try {
            System.out.println("Getting hashes");

            Transaction t = (Transaction) Transaction.deserialize(data);
            CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());

            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                objOut.writeObject(RequestType.GET_HASHED);
                objOut.writeObject(t);

                objOut.flush();
                byteOut.flush();

                asynchServiceProxy.invokeAsynchRequest(byteOut.toByteArray(), replyListener,
                        TOMMessageType.UNORDERED_HASHED_REQUEST);

                /*byte[] reply = serviceProxy.invokeUnorderedHashed(byteOut.toByteArray());
                if (reply.length == 0)
                    return;
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {
                    db.addLog(t);
                    objIn.readObject();
                    return;
                }*/

            } catch (IOException /*| ClassNotFoundException */ e) {
                System.out.println("Exception: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;

    }

    @Override
    public double obtainCoins(String who, byte[] data) {
        getHash(data);
        System.out.println("Done with hashes");
        try {
            System.out.println("obtainCoins");

            Transaction t = (Transaction) Transaction.deserialize(data);
            CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());

            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                objOut.writeObject(RequestType.OBTAIN_COINS);
                objOut.writeObject(t);

                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
                if (reply.length == 0)
                    return -1;
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {
                    db.addLog(t);
                    objIn.readObject();
                    return (double) objIn.readObject();
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Exception: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public double transferMoney(String from, String to, byte[] data) {
        System.out.println("transferMoney");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            objOut.writeObject(RequestType.TRANSFER);
            objOut.writeObject(t);

            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            if (reply.length == 0)
                return -1;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                //db.addLog("transferMoney from " + from + " to " + to + " " + amount);
                return (double) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public double currentAmount(String who) {

        System.out.println("currentAmount");

        Transaction t = new Transaction(who, String.format(Transaction.CURRENT_AMOUNT, who), null);

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            objOut.writeObject(RequestType.CLIENT_AMOUNT);
            objOut.writeObject(t);

            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply.length == 0)
                return -1;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                //db.addLog("currentAmount " + who);
                return (double) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Transaction> ledgerOfGlobalTransactions() {
        System.out.println("ledgerOfGlobalTransactions");

        Transaction t = new Transaction(null, Transaction.GET_ALL_TRANSCATIONS, null);

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            objOut.writeObject(RequestType.GET_ALL);
            objOut.writeObject(t);

            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply.length == 0)
                return new LinkedList<>();
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                //db.addLog("ledgerOfGlobalTransactions");
                return (List<Transaction>) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return new LinkedList<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Transaction> ledgerOfClientTransactions(String who) {
        System.out.println("ledgerOfClientTransactions");

        Transaction t = new Transaction(who, String.format(Transaction.GET_USER_TRANSCATIONS, who), null);

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            objOut.writeObject(RequestType.GET);
            objOut.writeObject(t);

            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply.length == 0)
                return new LinkedList<>();
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                //db.addLog("ledgerOfClientTransactions " + who);
                return (List<Transaction>) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return new LinkedList<>();
    }

    @Override
    public double minerateMoney(String who) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void installSmartContract(String who, String smart_contract) {
        // TODO Auto-generated method stub

    }

    @Override
    public void transferMoneyWithSmartContract(String from, String to, double amount, String smart_contract_ref) {
        // TODO Auto-generated method stub

    }
}