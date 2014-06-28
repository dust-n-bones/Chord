package FileServices;

import DHash.KeyHash;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by stathis on 6/5/14.
 * Στην Κλάση αυτή υπάρχουν οι συναρτήσεις διάσπασης και επανένωνσης ενός αρχείου
 */
public  class FileSpliter {


    public static void FileHasher(String filename) {

        File f = new File(filename);

        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(
                    new FileInputStream(f));
            MessageDigest md = MessageDigest.getInstance("SHA1");
            FileOutputStream out;
            String name = f.getName();
            int partCounter = 0;
            int sizeOfFiles = 1024;// 1KB
            byte[] buffer = new byte[sizeOfFiles];
            int tmp = 0;
            File folder = new File("chordData/");
            if (!folder.exists()) {
                folder.mkdir();
            }
            try {
                while ((tmp = bis.read(buffer)) > 0) {
                    File newFile = new File("chordData/" + KeyHash.calculateKey(name, partCounter));
                    newFile.createNewFile();
                    out = new FileOutputStream(newFile);
                    out.write(buffer, 0, tmp);
                    md.update(buffer, 0, tmp);
                    out.close();
                    partCounter++;

                }

                byte[] mdbytes = md.digest();

                //convert the byte to hex format
                StringBuffer sb = new StringBuffer("");
                for (int i = 0; i < mdbytes.length; i++) {
                    sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
                }

                System.out.println("Digest(in hex format):: " + sb.toString());




            } catch (IOException e) {


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }


    public static void FileReconstuctor(String filename, int noOfChunks, String validation) {



        File outputFolder= new File("output/");
        if(!outputFolder.exists()){
            outputFolder.mkdir();
        }
        File ofile = new File("output/"+filename);
        if(ofile.exists())
            ofile.delete();
        try {
        ofile.createNewFile();
        FileOutputStream fos = null;
        FileInputStream fis;
        byte[] fileBytes;
        int bytesRead = 0;


            fos = new FileOutputStream(ofile, true);
            File file;
            for (int i = 1; i < noOfChunks + 1; i++) {

                file = new File("output/" + KeyHash.calculateKey(filename, i));
                fis = new FileInputStream(file);
                fileBytes = new byte[(int) file.length()];
                bytesRead = fis.read(fileBytes, 0, (int) file.length());
                assert (bytesRead == fileBytes.length);
                assert (bytesRead == (int) file.length());
                fos.write(fileBytes);
                fos.flush();

                fis.close();
                file.delete();

            }
            fos.close();
 ofile= new File("output/"+filename);
            BufferedInputStream bis = null;
            bis = new BufferedInputStream(
                    new FileInputStream(ofile));


            byte[] buffer = new byte[1024];
            int tmp = 0;


            MessageDigest md = MessageDigest.getInstance("SHA1");

            while ((tmp = bis.read(buffer)) > 0) {

                md.update(buffer,0,tmp);


            }



            byte[] mdbytes = md.digest();

            //convert the byte to hex format
            StringBuffer sb = new StringBuffer("");
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            if(!sb.toString().equals(validation))
                System.out.println("File Downloaded but SHA-11 hash is diferent than the original file\n"+"Original SHA1: "+validation+"\n New file SHA1= " + sb.toString());
            else
                System.out.println("File Downloaded and SHA-1 Matches!");

            bis.close();



        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


    }



}