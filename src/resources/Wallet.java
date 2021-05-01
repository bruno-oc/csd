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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadLocalRandom;

public class Wallet implements WalletService {

    // TODO: add logs to proxy
    private final DataBase db;
    private final AsynchServiceProxy asynchSP;

    public Wallet(int id) {
    	String filePath = "src/server/server_log" + id + ".json";
        db = new DataBase(filePath);
        asynchSP = new AsynchServiceProxy(id);
    }

    private SystemReply asyncReply(ByteArrayOutputStream byteOut, TOMMessageType type) throws Exception {
        BlockingQueue<SystemReply> replyChain = new LinkedBlockingDeque<>();
        ReplyListener replyListener = new ReplyListenerImp(replyChain, asynchSP);
        asynchSP.invokeAsynchRequest(byteOut.toByteArray(), replyListener, type);

        return replyChain.take();
    }

    @Override
    public SystemReply obtainCoins(String who, byte[] data) {
        System.out.println("obtainCoins");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.OBTAIN_COINS);
            objOut.writeObject(t);
            objOut.writeObject(who);

            objOut.flush();
            byteOut.flush();

            return asyncReply(byteOut, TOMMessageType.ORDERED_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SystemReply transferMoney(String from, String to, byte[] data) {
        System.out.println("transferMoney");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.TRANSFER);
            objOut.writeObject(t);
            objOut.writeObject(from);
            
            objOut.flush();
            byteOut.flush();

            return asyncReply(byteOut, TOMMessageType.ORDERED_REQUEST);

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public SystemReply currentAmount(String who, byte[] data) {
        System.out.println("currentAmount");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.CLIENT_AMOUNT);
            objOut.writeObject(t);
            objOut.writeObject(who);

            objOut.flush();
            byteOut.flush();

            return asyncReply(byteOut, TOMMessageType.UNORDERED_REQUEST);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SystemReply ledgerOfGlobalTransactions(byte[] data) {
        System.out.println("ledgerOfGlobalTransactions");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());


        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.GET_ALL);
            objOut.writeObject(t);

            objOut.flush();
            byteOut.flush();

            return asyncReply(byteOut, TOMMessageType.UNORDERED_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SystemReply ledgerOfClientTransactions(String who, byte[] data) {
        System.out.println("ledgerOfClientTransactions");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getKeyPair().getPublic(), t.getOperation().getBytes(), t.getSig());
        
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.GET);
            objOut.writeObject(t);
            objOut.writeObject(who);

            objOut.flush();
            byteOut.flush();


            return asyncReply(byteOut, TOMMessageType.UNORDERED_REQUEST);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
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