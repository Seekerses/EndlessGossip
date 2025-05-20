package io.seekerses.endlessgossip.gossip.misc;

import net.minecraft.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class ObjectUtil {

	public static final byte[] NULL_BYTES = "null".getBytes(StandardCharsets.UTF_8);

	public static String serialize(Serializable object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream ous = new ObjectOutputStream(baos);
			ous.writeObject(object);
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		} catch (IOException ex) {
			return "null";
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T deserialize(String serializedObject) {
		if (StringUtil.isNullOrEmpty(serializedObject)) {
			return null;
		}
		byte[] decoded = Base64.getDecoder().decode(serializedObject);
		if (!Arrays.equals(NULL_BYTES, decoded)) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(decoded);
				ObjectInputStream ois = new ObjectInputStream(bais);
				return (T) ois.readObject();
			} catch (IOException | ClassNotFoundException ex) {
				return null;
			}
		} else {
			return null;
		}
	}
}
