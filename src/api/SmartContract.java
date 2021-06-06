package api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

public class SmartContract implements Serializable{
	
	public static final String INSTALL = "installSmartContract %s";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4L;
	
	private Transaction trans;
    	
	public SmartContract(Transaction trans) {
        this.setTrans(trans);
    }
	
	public Transaction getTrans() {
		return trans;
	}

	public void setTrans(Transaction trans) {
		this.trans = trans;
	}
	
	public boolean run(Transaction trans) {
		String[] str = trans.getOperation().split(" ");
		double temp = Double.parseDouble(str[str.length - 1]);
		System.out.println(temp + " < 100");
		if(temp < 100)
			return true;
		return false;		
	}
	
	public static Object deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] serialize(SmartContract obj) {
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
}
