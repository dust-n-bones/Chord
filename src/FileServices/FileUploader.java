package FileServices;

import Chord.Node;
import DHash.KeyHash;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by John on 10/6/2014.
 */
public class FileUploader implements Runnable {

    private static final String DEFAULT_HASH_ALGORITHM = "SHA1";
    private final int DEFAULT_TEAM = -1;
    private final int DEAFAULT_FILE_SIZE = 1024;
    private final String LINE_SEPERATOR = "\n";

    private String fileName;

    private Node node;

    public FileUploader(String fileN, Node n) {

        this.fileName = fileN;
        this.node = n;
    }


    public void run() {                     //Το thread το οποίο κόβει το αρχείο προς ανέβασμα σε κομμάτια και για κάθε κομμάτι ανοίγει ένα Thread το οποίο στέλνει το αρχείο.
                                            //Το thread αυτό τελικά δημιουργεί την ομάδα -1
        File file = new File(fileName);

        String fName = file.getName();

        System.out.println("Hello from File Uploader");

        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

            MessageDigest md = MessageDigest.getInstance(DEFAULT_HASH_ALGORITHM);

            byte[] buffer = new byte[DEAFAULT_FILE_SIZE];

            int byteCounter = 0;
            int partCounter = 1;

            while ((byteCounter = bis.read(buffer)) > 0) {

                //System.out.println("\nREAD : " + Arrays.toString(buffer) +"    " + byteCounter);


                String filePartName = KeyHash.calculateKey(fName, partCounter);

                Thread th = new FileUploaderHandler(node, filePartName, buffer, byteCounter);
                th.start();
                th.join();

                md.update(buffer, 0, byteCounter);

                partCounter++;

            }

            byte[] mdbytes = md.digest();

            StringBuffer sb = new StringBuffer("");
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            FileComponent fc = new FileComponent(sb.toString(), partCounter-1);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutput obOut = new ObjectOutputStream(baos);

            obOut.writeObject(fc);

            byte[] obBytes = baos.toByteArray();

            Thread th = new FileUploaderHandler(node, KeyHash.calculateKey(fName, DEFAULT_TEAM), obBytes, obBytes.length);
            th.start();
            th.join();

            baos.close();
            bis.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}
