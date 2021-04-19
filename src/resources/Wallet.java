package resources;

import api.rest.WalletService;
import bftsmart.tom.ServiceProxy;
import db.DataBase;
import server.replica.RequestType;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Wallet implements WalletService {

    private final DataBase db;
    private final ServiceProxy serviceProxy;

    public Wallet(String filePath, int id) {
        db = new DataBase(filePath);
        serviceProxy = new ServiceProxy(id);
    }

    @Override
    public double obtainCoins(String who, double amount) {
        System.out.println("obtainCoins");

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.OBTAIN_COINS);
            objOut.writeObject(who);
            objOut.writeObject(amount);

            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            if (reply.length == 0)
                return -1;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
            	db.addLog("obtainCoins " + who + " " + amount);
                return (double) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public double transferMoney(String from, String to, double amount) {
        System.out.println("transferMoney");

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.TRANSFER);
            objOut.writeObject(from);
            objOut.writeObject(to);
            objOut.writeObject(amount);

            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            if (reply.length == 0)
                return -1;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
            	db.addLog("transferMoney from " + from + " to " + to + " " + amount);
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

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.CLIENT_AMOUNT);
            objOut.writeObject(who);

            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply.length == 0)
                return -1;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
            	db.addLog("currentAmount " + who);
                return (double) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> ledgerOfGlobalTransactions() {
        System.out.println("ledgerOfGlobalTransactions");

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.GET_ALL);

            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply.length == 0)
                return new LinkedList<>();
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
            	db.addLog("ledgerOfGlobalTransactions");
                return (List<String>) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return new LinkedList<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> ledgerOfClientTransactions(String who) {
        System.out.println("ledgerOfClientTransactions");

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(RequestType.GET);
            objOut.writeObject(who);

            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply.length == 0)
                return new LinkedList<>();
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
            	db.addLog("ledgerOfClientTransactions " + who);
                return (List<String>) objIn.readObject();
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