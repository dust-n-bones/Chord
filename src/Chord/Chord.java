package Chord;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by John on 3/6/2014.
 */
public class Chord {


    public static void create(Node node) throws RemoteException { //create ring

        //System.out.println("Im here");

        node.setConnected(true);
        node.setSuccessorId(node.getNodeId());
        node.setSuccessorIp(node.getLocalIp());
        node.setSuccessorPort(node.getPort());
        node.setPredecessorId(null);
        node.setPredecessorIp(null);
        node.setPredecessorPort(0);
        node.reciever();

        //node.printNodeStatus();

    }

    public static void join(Node node, NodeProperties np) throws RemoteException, NotBoundException {

        Registry registry = LocateRegistry.getRegistry(String.valueOf(np.getLocalIp()));
        Node nextNode = (Node) registry.lookup(String.valueOf(np.getNodeId()));


        System.out.println("NEXT NODE ID : " + nextNode.getNodeId());
        long start = System.nanoTime();
        NodeProperties succ = nextNode.findSuccessor(node.getNodeId());
        long elapsedTime = System.nanoTime() - start;
        System.out.println("It took me "+ elapsedTime/1000000000.0+" seconds to join Chord");
        node.setSuccessorIp(succ.getLocalIp());
        node.setSuccessorPort(succ.getPort());
        node.setPredecessorIp(null);
        node.setPredecessorId(null);
        node.setConnected(true);
        node.setSuccessorId(succ.getNodeId());
        node.reciever();

    }

    public static void stabilize(Node node) { //periodically stabilize node and properly notify others

        Thread th = new Thread(new StabilizeThread(node));
        th.start();

    }

    public static void fixFingers(Node node) { //periodically stabilize node and properly notify others

        Thread th = new Thread(new FixFingersThread(node));
        th.start();

    }

    public static void fixFinger(Node node) { //periodically stabilize node and properly notify others

        Thread th = new Thread(new FixFingersThread(node));
        th.start();

    }

    public static void checkPredecessor(Node node) {

        Thread th = new Thread(new PredecessorHeartbeatThread(node));
        th.start();

    }


    public static void nodeExit(Node node) throws RemoteException {
        node.exit();
    }


    public static void redistributeKeys(Node node) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(String.valueOf(node.getSuccessorIp()));
        Node nextNode = (Node) registry.lookup(String.valueOf(node.getSuccessorId()));
        nextNode.redistributeKeys();

    }



}
