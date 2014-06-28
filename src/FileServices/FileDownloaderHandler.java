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
 * Thread το οποίο αναλαμβάνει να κατεβάσει μια ομάδα από τον αντίστοιχο client.
 */
public class FileDownloaderHandler extends Thread {

    private final int DEAFAULT_FILE_SIZE = 1024;
    private final String LINE_SEPERATOR = "\n";
    private String fileName;
    private Node node;

    public FileDownloaderHandler(String fn, Node n) {
        this.fileName = fn;
        this.node = n;
    }



    public void run() {

        String fileTitle;

        byte[] chunk = new byte[DEAFAULT_FILE_SIZE];

        try {
            NodeProperties np = node.findSuccessor(KeyHash.stringToBigIntegerConverter(fileName));

            Socket socket = new Socket(np.getLocalIp(), np.getPort());

            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            out.writeUTF("DOWNLOAD" + LINE_SEPERATOR);
            out.flush();


            out.writeUTF(fileName);
            out.flush();

            if(in.readUTF().equals("NOFILEFOUND")) {
                System.out.println("The file you requested does not exists!");
                out.close();
                in.close();
                socket.close();
                return;
            }
            File folder = new File("output/");
            if (!folder.exists()) {
                folder.mkdir();
            }

            File file = new File("output/" + fileName);

            FileOutputStream fos = new FileOutputStream(file);


            int bytesRead = 0;

            while((bytesRead = in.read(chunk)) > 0) {

                fos.write(chunk, 0, bytesRead);
                fos.flush();
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
