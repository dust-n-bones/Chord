package DHash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by stathis on 6/5/14.
 * Συναρτήσεις κατακερματισμού
 */
public class KeyHash {

    private static final String DEFAULT_HASH_ALGORITHM = "SHA1";

    public static String calculateKey(String filename, int id)  {

        String input = filename + String.valueOf(id);

        MessageDigest mDigest = null;
        try {
            mDigest = MessageDigest.getInstance(DEFAULT_HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }


        return sb.toString();

    }


    public static BigInteger stringToBigIntegerConverter(String key){

        return new BigInteger(key, 16);
    }


}
