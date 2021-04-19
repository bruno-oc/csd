package server.replica;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class BFTServer extends DefaultSingleRecoverable {
    private List<String> logs;

    public BFTServer(int id) {
        logs = new LinkedList<>();
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
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            RequestType reqType = (RequestType) objIn.readObject();
            switch (reqType) {
                case OBTAIN_COINS:
                    client = (String) objIn.readObject();
                    amount = (double) objIn.readObject();
                    logs.add("obtainCoins " + client + " " + amount);
                    objOut.writeObject(clientAmount(client));
                    hasReply = true;
                    break;
                case TRANSFER:
                    client = (String) objIn.readObject();
                    String to = (String) objIn.readObject();
                    amount = (double) objIn.readObject();
                    logs.add("transferMoney from " + client + " to " + to + " " + amount);
                    objOut.writeObject(amount);
                    hasReply = true;
                    break;
                case CLIENT_AMOUNT:
                    client = (String) objIn.readObject();
                    logs.add("currentAmount " + client);
                    objOut.writeObject(clientAmount(client));
                    hasReply = true;
                    break;
                case GET:
                    client = (String) objIn.readObject();
                    logs.add("ledgerOfClientTransactions " + client);
                    List<String> clientLogs = new LinkedList<>();
                    for (String log : logs)
                        if (log.contains(client))
                            clientLogs.add(log);

                    objOut.writeObject(clientLogs);
                    hasReply = true;
                    break;
                case GET_ALL:
                    logs.add("ledgerOfGlobalTransactions");
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
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            RequestType reqType = (RequestType) objIn.readObject();
            switch (reqType) {
                case CLIENT_AMOUNT:
                    client = (String) objIn.readObject();
                    logs.add("currentAmount " + client);
                    objOut.writeObject(clientAmount(client));
                    hasReply = true;
                    break;
                case GET:
                    client = (String) objIn.readObject();
                    logs.add("ledgerOfClientTransactions " + client);
                    List<String> clientLogs = new LinkedList<>();
                    for (String log : logs)
                        if (log.contains(client))
                            clientLogs.add(log);

                    objOut.writeObject(clientLogs);
                    hasReply = true;
                    break;
                case GET_ALL:
                    logs.add("ledgerOfGlobalTransactions");
                    objOut.writeObject(logs);
                    hasReply = true;
                    break;
                default:
                    System.out.println("In appExecuteUnordered only read operations are supported");
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
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(state);
             ObjectInput objIn = new ObjectInputStream(byteIn)) {
            logs = (List<String>) objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error while installing snapshot:\n" + e);
        }
    }

    @Override
    public byte[] getSnapshot() {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            objOut.writeObject(logs);
            return byteOut.toByteArray();
        } catch (IOException e) {
            System.out.println("Error while taking snapshot:\n" + e);
        }
        return new byte[0];
    }
}
