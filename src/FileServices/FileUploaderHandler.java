package FileServices;

import Chord.Node;
import Chord.NodeProperties;
import DHash.KeyHash;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * Created by John on 10/6/2014.
 */
public class FileUploaderHandler extends Thread {

    private final int DEAFAULT_FILE_SIZE = 1024;
    private final String LINE_SEPERATOR = "\n";

    private String fileName;
    private Node node;
    private byte[] part;
    private int length;

    public FileUploaderHandler(Node n, String fn, byte[] ba, int len) {

        this.node = n;
        this.fileName = fn;
        this.part = new byte[DEAFAULT_FILE_SIZE];
        this.part = ba;
        this.length = len;

    }

    @Override
    public void run() {                                                     //Thead το οποίο θα ανεβάσει ένα αρχείο

        try {
            NodeProperties np = node.findSuccessor(KeyHash.stringToBigIntegerConverter(fileName));

            //System.out.println("SEND TO : IP: " + np.getLocalIp() + " PORT : " + np.getPort());

            //System.out.println("\nREAD : " + Arrays.toString(part));

            Socket socket = new Socket(np.getLocalIp(), np.getPort());


            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            String message = "UPLOAD" + LINE_SEPERATOR;
            out.flush();
            out.writeUTF(message);
            out.flush();

            message = fileName;

            out.writeUTF(message);
            //out.write(message);
            out.flush();


            out.write(part, 0, length);
            out.flush();


            out.close();
            in.close();
            socket.close();



        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
