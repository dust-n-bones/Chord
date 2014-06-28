import Chord.Chord;
import Chord.Node;
import Chord.NodeImpl;
import Chord.NodeProperties;
import FileServices.FileDownloader;
import FileServices.FileServer;
import FileServices.FileUploader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException, UnknownHostException, RemoteException, MalformedURLException, NotBoundException {

        Node node = new NodeImpl(6000);
        NodeProperties nodeProp = node.broadcast();

        if(nodeProp == null) { //no node in ring, create ring
            //System.out.println("YO");
            Chord.create(node);
        } else {
            System.out.println("NodeId : " + nodeProp.getNodeId() + "\n" + " Port : " + nodeProp.getPort());

            Chord.join(node, nodeProp);
        }

        Chord.stabilize(node);

        Chord.checkPredecessor(node);

        Chord.fixFinger(node);


        new Thread(new FileServer(node.getPort())).start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //new Thread(new FileUploader("tetFile.txt" , node)).start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       //node.redistributeKeys();


       new Thread(new FileDownloader("tetFile.txt" , node)).start();
    }
}
