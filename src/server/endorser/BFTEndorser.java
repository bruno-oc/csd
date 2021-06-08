package server.endorser;

import api.SmartContract;
import api.Transaction;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import bftsmart.tom.util.TOMUtil;
import com.google.gson.Gson;
import crypto.CryptoStuff;
import db.DataBase;
import server.replica.ReplicaReply;
import server.replica.RequestType;

import java.io.*;

public class BFTEndorser extends DefaultSingleRecoverable {

    private final DataBase smartContractsDB;
    private final int id;
    private Gson gson;

    public BFTEndorser(int id) {
        String filePath = "src/server/endorser/contracts_" + id + ".json";
        smartContractsDB = new DataBase(filePath);
        this.id = id;
        gson = new Gson();

        new ServiceReplica(id, "config/endorser", this, this, null, null, null);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: EndorserServer <server id>");
            System.exit(-1);
        }
        new BFTEndorser(Integer.parseInt(args[0]));
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

            if (reqType == RequestType.INSTALL_SMART_CONTRACT) {
                hasReply = installSmartContract(objIn, objOut);
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

    private boolean installSmartContract(ObjectInput objIn, ObjectOutput objOut) {
        try {
            SmartContract sc = (SmartContract) objIn.readObject();
            Transaction t = sc.getTrans();

            CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

            String ref = smartContractsDB.addSmartContract(sc);

            ReplicaReply reply = new ReplicaReply(id, t.getOperation(), ref,
                    TOMUtil.computeHash(t.getOperation().getBytes()),
                    TOMUtil.signMessage(CryptoStuff.getKeyPair().getPrivate(), t.getOperation().getBytes()));

            objOut.writeObject(reply);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        return new byte[0];
    }

    @Override
    public void installSnapshot(byte[] state) {

    }

    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }
}
