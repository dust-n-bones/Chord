package FileServices;


import Chord.Node;
import Chord.NodeProperties;
import DHash.KeyHash;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by John on 7/6/2014.
 * Το thead αυτό κατεβάζει την ομάδα -1.Αν το καταφέρει τότε υπολογίζει τις υπόλοιπες ομάδες και ανοίγει τα αντίστοιχα threads προκειμένου να κατέβουν οι ομάδες του αρχείου.
 */
public class FileDownloader implements Runnable{

    private final int DEFAULT_TEAM = -1;
    private final int DEAFAULT_FILE_SIZE = 1024;
    private final String LINE_SEPERATOR = "\n";

    private String fileName;

    private Node node;

    public FileDownloader(String fileN, Node n) {

        this.fileName = fileN;
        this.node = n;
    }


    @Override
    public void run() {

        String defaultTeamKeyString = KeyHash.calculateKey(fileName, DEFAULT_TEAM);

        byte[] chunk = new byte[DEAFAULT_FILE_SIZE];

        try {
            long start = System.nanoTime();

            NodeProperties defaultNodeProp = node.findSuccessor(KeyHash.stringToBigIntegerConverter(defaultTeamKeyString));

            Socket socket = new Socket(defaultNodeProp.getLocalIp(), defaultNodeProp.getPort());

            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            out.writeUTF("DOWNLOAD" + LINE_SEPERATOR);
            out.flush();

            out.writeUTF(defaultTeamKeyString);
            out.flush();
            String answer;

           // answer=in.readUTF();

            if(in.readUTF().equals("NOFILEFOUND")) {
                System.out.println("The file you requested does not exists!");
                out.close();
                in.close();
                socket.close();
                return;
            }


            int bytesRead = in.read(chunk);

            ByteArrayInputStream bais = new ByteArrayInputStream(chunk, 0, bytesRead);
            ObjectInputStream ois = new ObjectInputStream(bais);


            FileComponent fc = (FileComponent) ois.readObject();

            //System.out.println("_______________________" + fc.getNoOfChunks());


            if(fc.getNoOfChunks() == 0) { //file is not found

            } else {

                for(int i=1; i<fc.getNoOfChunks()+1; i++) {

                    Thread th = new FileDownloaderHandler(KeyHash.calculateKey(fileName, i), node);
                    th.start();
                    th.join();

                }


            }

            FileSpliter.FileReconstuctor(fileName, fc.getNoOfChunks(),fc.getCheckSum());
            long elapsedTime = System.nanoTime() - start;
            System.out.println("It took me "+ elapsedTime/1000000000.0+" seconds to Download this file");


        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
