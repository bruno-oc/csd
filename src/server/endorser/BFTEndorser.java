package server.endorser;

import api.SmartContract;
import api.Transaction;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import bftsmart.tom.util.TOMUtil;
import crypto.CryptoStuff;
import server.replica.ReplicaReply;
import server.replica.RequestType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class BFTEndorser extends DefaultSingleRecoverable {

    private static final String FILE_PATH = "src/server/endorser/contracts_%s.json";

    private final int id;

    public BFTEndorser(int id) {
        this.id = id;
        new ServiceReplica(id, "config/endorser", this, this, null, null, null);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: EndorserServer <server id>");
            System.exit(-1);
        }
        new BFTEndorser(Integer.parseInt(args[0]));
    }

    private static Object deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] serialize(List<SmartContract> obj) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(obj);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    private String addSmartContract(SmartContract sc) {
        Path path = Paths.get(String.format(FILE_PATH, id));

        File f = new File(String.format(FILE_PATH, id));
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] fileContent = null;
        try {
            fileContent = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SmartContract> list;
        if (fileContent == null || fileContent.length == 0)
            list = new LinkedList<>();
        else
            list = (List<SmartContract>) deserialize(fileContent);

        list.add(sc);

        System.out.println(list.size());
        byte[] bytes = serialize(list);
        try {
            Files.write(path, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "" + (list.size() - 1);
    }

    private boolean installSmartContract(ObjectInput objIn, ObjectOutput objOut) {
        try {
            SmartContract sc = (SmartContract) objIn.readObject();
            Transaction t = sc.getTrans();

            CryptoStuff.verifySignature(CryptoStuff.getPublicKey(t.getPublicKey()), t.getOperation().getBytes(), t.getSig());

            String ref = addSmartContract(sc);

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