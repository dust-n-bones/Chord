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
 * Created by John on 10/6/2014.
 */
public class ChunkHandler extends Thread {

    private final int DEAFAULT_FILE_SIZE = 1024;
    private final String LINE_SEPERATOR = "\n";
    private String fileName;
    private Node node;

    public ChunkHandler(String fn, Node n) {
        this.fileName = fn;
        this.node = n;
    }



    public void run() {

        String fileTitle;

        byte[] chunk = new byte[DEAFAULT_FILE_SIZE];

        try {
            NodeProperties np = node.findSuccessor(KeyHash.stringToBigIntegerConverter(fileName));

            Socket socket = new Socket(np.getLocalIp(), np.getPort());

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.write("DOWNLOAD" + LINE_SEPERATOR);
            out.flush();

            out.write(fileName);
            out.flush();

            File file = new File("output/" +fileName);

            FileOutputStream fos = new FileOutputStream(file);

            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());

            int bytesRead = 0;

            while((bytesRead = bis.read(chunk)) > 0) {

                fos.write(chunk, 0, bytesRead);
            }




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
