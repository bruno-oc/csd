package crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Message implements Serializable{
	
	public static final String OBTAIN_COIN = "obtainCoins %s %s";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<byte[]> m;
	
	public Message() {
		m = new LinkedList<byte[]>();
	}
	
	public void add(byte[] bytes) {
		m.add(bytes);
	}
	
	public List<byte[]> getMessage() {
		return m;
	}
	
	public void add(int index, byte[] b) {
		m.add(index, b);
	}
	
	public static byte[] serialize(Object obj) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
		    ObjectOutputStream os = new ObjectOutputStream(out);
		    os.writeObject(obj);
		    return out.toByteArray();
		} catch(Exception e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	public static Object deserialize(byte[] data) {
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(data);
		    ObjectInputStream is = new ObjectInputStream(in);
		    return is.readObject();
		} catch(Exception e) {
			e.printStackTrace();
		}
	    return null;
	}

}