package resources;

import api.Block;
import api.SmartContract;
import api.Transaction;
import api.rest.WalletService;
import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;
import crypto.CryptoStuff;
import db.DataBase;
import server.ReplyListenerImp;
import server.SystemReply;
import server.replica.RequestType;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Wallet implements WalletService {
    private final DataBase db, smartContractsDB;
    private final AsynchServiceProxy serverAsynchSP, endorserAsynchSP;
    private final int id;

    public Wallet(int id) {
        this.id = id;

        String filePath = "src/server/server_log" + id + ".json";
        db = new DataBase(filePath);
        filePath = "src/server/verified_contracts_" + id + ".json";
        smartContractsDB = new DataBase(filePath);

        serverAsynchSP = new AsynchServiceProxy(id, "config/server");
        endorserAsynchSP = new AsynchServiceProxy(id, "config/endorser");
    }

    private SystemReply asyncReply(AsynchServiceProxy asynchSP, ByteArrayOutputStream byteOut, TOMMessageType type) throws InterruptedException {
        BlockingQueue<SystemReply> replyChain = new LinkedBlockingDeque<>();
        ReplyListener replyListener = new ReplyListenerImp(replyChain, asynchSP);
        asynchSP.invokeAsynchRequest(byteOut.toByteArray(), replyListener, type);

        SystemReply reply = replyChain.take();
        if (reply.getReplies().isEmpty())
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        return reply;
    }

    @Override
    public SystemReply obtainCoins(String who, byte[] data) {
        System.out.println("obtainCoins");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.OBTAIN_COINS);
            objOut.writeObject(t);
            objOut.writeObject(who);

            objOut.flush();
            byteOut.flush();

            SystemReply reply = asyncReply(serverAsynchSP, byteOut, TOMMessageType.ORDERED_REQUEST);
            db.addLog(t);
            return reply;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SystemReply transferMoney(String from, String to, byte[] data) {
        System.out.println("transferMoney");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
        	
        	if(t.getEnvelope() == null)
        		objOut.writeObject(RequestType.TRANSFER);
        	else {
        		objOut.writeObject(RequestType.TRANSFER_PRIVATE);
        		System.out.println("entrei no private");
        	}
            
            objOut.writeObject(t);
            objOut.writeObject(from);

            objOut.flush();
            byteOut.flush();

            SystemReply reply = asyncReply(serverAsynchSP, byteOut, TOMMessageType.ORDERED_REQUEST);
            db.addLog(t);
            return reply;

        } catch (IOException | InterruptedException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public SystemReply currentAmount(String me, byte[] data) {
        System.out.println("currentAmount");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.CLIENT_AMOUNT);
            objOut.writeObject(t);
            objOut.writeObject(me);

            objOut.flush();
            byteOut.flush();

            SystemReply reply = asyncReply(serverAsynchSP, byteOut, TOMMessageType.ORDERED_REQUEST);
            db.addLog(t);
            return reply;
        } catch (IOException | InterruptedException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public SystemReply ledgerOfGlobalTransactions(int lastN, byte[] data) {
        System.out.println("ledgerOfGlobalTransactions");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());


        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.GET_ALL);
            objOut.writeObject(t);
            objOut.writeObject(lastN);

            objOut.flush();
            byteOut.flush();

            SystemReply reply = asyncReply(serverAsynchSP, byteOut, TOMMessageType.ORDERED_REQUEST);
            db.addLog(t);
            return reply;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SystemReply ledgerOfClientTransactions(String who, int lastN, byte[] data) {
        System.out.println("ledgerOfClientTransactions");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.GET);
            objOut.writeObject(t);
            objOut.writeObject(who);
            objOut.writeObject(lastN);

            objOut.flush();
            byteOut.flush();

            SystemReply reply = asyncReply(serverAsynchSP, byteOut, TOMMessageType.ORDERED_REQUEST);
            db.addLog(t);
            return reply;
        } catch (IOException | InterruptedException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public SystemReply obtainLastMinedBlock(byte[] data) {
        System.out.println("obtainLastMinedBlock");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.GET_LAST_BLOCK);
            objOut.writeObject(t);

            objOut.flush();
            byteOut.flush();

            return asyncReply(serverAsynchSP, byteOut, TOMMessageType.UNORDERED_REQUEST);
        } catch (IOException | InterruptedException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public SystemReply pickNotMineratedTransaction(int n, byte[] data) {
        System.out.println("pickNotMineratedTransaction");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());


        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.GET_NOT_MINED);
            objOut.writeObject(t);
            objOut.writeObject(n);

            objOut.flush();
            byteOut.flush();

            SystemReply reply = asyncReply(serverAsynchSP, byteOut, TOMMessageType.ORDERED_REQUEST);
            db.addLog(t);
            return reply;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SystemReply sendMinedBlock(byte[] data) {
        System.out.println("sendMinedBlock");

        Block b = (Block) Block.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getPublicKey(b.getPub()), Transaction.serialize(b.getTransactions()), b.getSig());

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.MINE);
            objOut.writeObject(b);

            objOut.flush();
            byteOut.flush();

            return asyncReply(serverAsynchSP, byteOut, TOMMessageType.ORDERED_REQUEST);
        } catch (IOException | InterruptedException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public SystemReply installSmartContract(String who, byte[] data) {
        System.out.println("installSmartContract");

        SmartContract sc = (SmartContract) SmartContract.deserialize(data);
        Transaction t = sc.getTrans();
        CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.INSTALL_SMART_CONTRACT);
            objOut.writeObject(sc);

            objOut.flush();
            byteOut.flush();

            SystemReply reply = asyncReply(endorserAsynchSP, byteOut, TOMMessageType.ORDERED_REQUEST);
            smartContractsDB.addSmartContract(sc);
            return reply;
        } catch (IOException | InterruptedException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public SystemReply transferMoneyWithSmartContractRef(String from, String scontract_ref, String to, byte[] data) {
        System.out.println("transferMoneyWithSmartContractRef");

        Transaction t = (Transaction) Transaction.deserialize(data);
        CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.SMART_CONTRACT);
            objOut.writeObject(id);
            objOut.writeObject(scontract_ref);
            objOut.writeObject(t);
            objOut.writeObject(from);

            objOut.flush();
            byteOut.flush();

            return asyncReply(serverAsynchSP, byteOut, TOMMessageType.ORDERED_REQUEST);
        } catch (IOException | InterruptedException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }
}