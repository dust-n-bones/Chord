package Chord;

import DHash.KeyHash;
import FileServices.FileUploaderHandler;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 2/6/2014.
 */
public class NodeImpl extends UnicastRemoteObject implements Node{

    private static final String DEFAULT_HASH_ALGORITHM = "SHA1";
    private static final int REGISRTY_PORT = 1099;
    private static final int DEFAULT_RANGE = 160;
    private int port;
    private String localIp;
    private BigInteger nodeId;
    private BigInteger successorId;
    private String successorIp;
    private int successorPort;
    private int predecessorPort;
    private BigInteger predecessorId;
    private String predecessorIp;
    private boolean connected;
    private static Registry registry;

    NodeProperties myNodeProperties;

    private List<NodeProperties> fingersList;


    public NodeImpl(int port) throws UnknownHostException, NoSuchAlgorithmException, RemoteException {

        this.port = port;
        this.localIp = InetAddress.getLocalHost().getHostAddress();
        this.nodeId = calculateNodeId(localIp, port);
        this.successorId = null;
        this.successorIp = null;
        this.predecessorId = null;
        this.predecessorPort = 0;
        this.predecessorIp = null;
        this.connected = false;
        myNodeProperties = new NodeProperties(port, nodeId, localIp);
        this.fingersList = new ArrayList<NodeProperties>();

        rmiBind();

    }

    public void rmiBind() throws RemoteException {

        registry = LocateRegistry.createRegistry(REGISRTY_PORT);

        registry.rebind(String.valueOf(this.nodeId), this);


        System.out.println("Node bound");

    }



    public NodeProperties findSuccessor(BigInteger newNodeId) throws RemoteException, NotBoundException {

        //System.out.println("RMI CALL NODE ID : " + newNodeId);

        if((this.getSuccessorId().compareTo(this.getNodeId()) == -1
                || this.getSuccessorId().compareTo(this.getNodeId()) == 0)
                && (newNodeId.compareTo(this.getSuccessorId()) == -1
                ||  newNodeId.compareTo(this.getNodeId()) == 1))
        { //telos daktyliou

            return new NodeProperties(this.getSuccessorPort(), this.getSuccessorId(), this.getSuccessorIp());

        }
        else if(newNodeId.compareTo(this.getNodeId()) == 1 && newNodeId.compareTo(this.getSuccessorId()) == -1) { // einai anamesa se emas kai to successor



            return new NodeProperties(this.getSuccessorPort(), this.getSuccessorId(), this.getSuccessorIp());
        }
        else  { // einai megalitero apo to successor mas

                NodeProperties nextNodeProp = closestPrecedingNode(newNodeId);

                Registry registry = LocateRegistry.getRegistry(String.valueOf(nextNodeProp.getLocalIp()));
                Node nextNode = (Node)  registry.lookup(String.valueOf(nextNodeProp.getNodeId()));

            return nextNode.findSuccessor(newNodeId);
        }

    }


    public synchronized void stabilize() {

        Registry registry = null;
        try {


            //this.printNodeStatus();

            registry = LocateRegistry.getRegistry(this.getSuccessorIp());
            Node sucNode = (Node)  registry.lookup(String.valueOf(this.getSuccessorId()));

            BigInteger sucPredId = sucNode.getPredecessorId();
            String sucPredIp = sucNode.getPredecessorIp();
            int sucPredPort = sucNode.getPredecessorPort();


            if(this.getSuccessorId().compareTo(this.getNodeId()) == 0 && sucPredId == null && sucPredIp == null) { // o successor mas einai o monos komvos ston daktylio
                //System.out.println("Stabilize Case 1");
                return;
            }
            else if(this.getSuccessorId().compareTo(this.getNodeId()) == 0 && sucPredId != null && sucPredIp != null) {
                //System.out.println("Stabilize Case 2");
                this.setSuccessorId(sucPredId);
                this.setSuccessorIp(sucPredIp);
                this.setSuccessorPort(sucPredPort);

                registry = LocateRegistry.getRegistry(sucPredIp);
                Node newSucNode = (Node)  registry.lookup(String.valueOf(sucPredId));

                newSucNode.notify(myNodeProperties);
                return;

            }
            else if(sucPredId == null && sucPredIp == null && sucNode.getNodeId().compareTo(this.getNodeId()) != 0 ) { //arxiki periptosi
                //System.out.println("Stabilize Case 3");
                sucNode.notify(myNodeProperties);
                return;

            }
            else {

                if(sucPredId.compareTo(this.getNodeId()) == 1 &&  sucPredId.compareTo(sucNode.getNodeId()) == -1) { //klasiki periptosi
                    //System.out.println("Stabilize Case 4");
                    this.setSuccessorId(sucPredId);
                    this.setSuccessorIp(sucPredIp);
                    this.setSuccessorPort(sucPredPort);

                    registry = LocateRegistry.getRegistry(sucPredIp);
                    Node newSucNode = (Node)  registry.lookup(String.valueOf(sucPredId));

                    newSucNode.notify(myNodeProperties);
                    return;
                }
                else if(sucNode.getNodeId().compareTo(this.getNodeId()) == -1
                        && sucPredId.compareTo(sucNode.getNodeId()) == -1
                        && sucPredId.compareTo(this.getNodeId()) != 0) { //telos daktiliou me mikroteri timi apo tin arxiki
                    //System.out.println("Stabilize Case 5");
                    this.setSuccessorId(sucPredId);
                    this.setSuccessorIp(sucPredIp);
                    this.setSuccessorPort(sucPredPort);

                    registry = LocateRegistry.getRegistry(sucPredIp);
                    Node newSucNode = (Node)  registry.lookup(String.valueOf(sucPredId));

                    newSucNode.notify(myNodeProperties);
                    return;
                }
                else if(sucNode.getNodeId().compareTo(this.getNodeId()) == -1
                        && sucPredId.compareTo(sucNode.getNodeId()) == 1
                        && sucPredId.compareTo(this.getNodeId()) != 0) { //telos daktiliou me megaliteri timi apo tin arxiki
                    //System.out.println("Stabilize Case 6");
                    this.setSuccessorId(sucPredId);
                    this.setSuccessorIp(sucPredIp);
                    this.setSuccessorPort(sucPredPort);

                    registry = LocateRegistry.getRegistry(sucPredIp);
                    Node newSucNode = (Node)  registry.lookup(String.valueOf(sucPredId));

                    newSucNode.notify(myNodeProperties);
                    return;
                }

            }

            sucNode.notify(myNodeProperties);


        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        //System.out.printf("Default Stabilize");

    }

    public void notify(NodeProperties np) throws RemoteException, NotBoundException {

        if(this.getPredecessorId() == null) {
            //System.out.println("___IP : "+ np.getLocalIp() +" Notify Case 1");
            this.setPredecessorId(np.getNodeId());
            this.setPredecessorIp(np.getLocalIp());
            this.setPredecessorPort(np.getPort());
            registry = LocateRegistry.getRegistry(this.getSuccessorIp());
            Node succNode = (Node)  registry.lookup(String.valueOf(this.getSuccessorId()));
            succNode.redistributeKeys();
        }
        else if(np.getNodeId().compareTo(this.getPredecessorId()) == 1
                && np.getNodeId().compareTo(this.getNodeId()) == -1) {
            //System.out.println("___IP : "+ np.getLocalIp() +" Notify Case 2");
            this.setPredecessorId(np.getNodeId());
            this.setPredecessorIp(np.getLocalIp());
            this.setPredecessorPort(np.getPort());
//            registry = LocateRegistry.getRegistry(this.getSuccessorIp());
//            Node succNode = (Node)  registry.lookup(String.valueOf(this.getSuccessorId()));
//            succNode.redistributeKeys();

        }
        else if(this.getPredecessorId().compareTo(this.getNodeId()) == 1
                && np.getNodeId().compareTo(this.getNodeId()) == -1) {
            //System.out.println("___IP : "+ np.getLocalIp() +" Notify Case 3");
            this.setPredecessorId(np.getNodeId());
            this.setPredecessorIp(np.getLocalIp());
            this.setPredecessorPort(np.getPort());
//            registry = LocateRegistry.getRegistry(this.getSuccessorIp());
//            Node succNode = (Node)  registry.lookup(String.valueOf(this.getSuccessorId()));
//            succNode.redistributeKeys();
        }
        else if(this.getPredecessorId().compareTo(this.getNodeId()) == 1
                && np.getNodeId().compareTo(this.getPredecessorId()) == 1) {
            //System.out.println("___IP : "+ np.getLocalIp() +" Notify Case 4");
            this.setPredecessorId(np.getNodeId());
            this.setPredecessorIp(np.getLocalIp());
            this.setPredecessorPort(np.getPort());
//            registry = LocateRegistry.getRegistry(this.getSuccessorIp());
//            Node succNode = (Node)  registry.lookup(String.valueOf(this.getSuccessorId()));
//            succNode.redistributeKeys();

        }


    }

    public synchronized void fixFingers() throws RemoteException, NotBoundException {

        fingersList.clear();

        BigInteger temp;
        BigInteger finger;

        for(int next = 1; next <= DEFAULT_RANGE; next++) {

            temp = BigInteger.valueOf(2);

            finger = this.getNodeId().add(temp.pow(next - 1));
            NodeProperties succ = this.findSuccessor(finger.mod(temp.pow(DEFAULT_RANGE)));
          //System.out.println(next+" I found:   "+succ.getNodeId()+"  with    "+ finger.mod(temp.pow(DEFAULT_RANGE)) + "    ");
            this.addFinger(succ);

        }
    }


    public NodeProperties closestPrecedingNode(BigInteger newNodeId){

        for(int i=fingersList.size()-1;i>=0;i--) {

            if(fingersList.get(i).getNodeId().compareTo(this.getNodeId()) == 1
                    && newNodeId.compareTo(fingersList.get(i).getNodeId()) == -1
                    && newNodeId.compareTo(this.getNodeId()) == -1) {

                return fingersList.get(i);

            } else if(fingersList.get(i).getNodeId().compareTo(this.getNodeId()) == -1
                    && newNodeId.compareTo(fingersList.get(i).getNodeId()) == 1
                    && newNodeId.compareTo(this.getNodeId()) == -1) {

                return fingersList.get(i);

            }
            else if(fingersList.get(i).getNodeId().compareTo(newNodeId) == -1
                    && fingersList.get(i).getNodeId().compareTo(this.getNodeId()) == 1) {

                return fingersList.get(i);
            }
        }

        return new NodeProperties(this.getSuccessorPort(), this.getSuccessorId(), getSuccessorIp());

    }



    public void addFinger(NodeProperties np) {

        if(fingersList.isEmpty())
            fingersList.add(np);
        else {

            if(np.getNodeId().compareTo(this.getNodeId()) == 0)
                return;

            for(NodeProperties nodeProp : fingersList) {
                if (nodeProp.getNodeId().compareTo(np.getNodeId()) == 0) {
                    return;
                }
            }
            fingersList.add(np);
        }

    }



    public void printFingerList() {

        System.out.println("\n_________FINGER LIST_________");

        int i = 1;

        for(NodeProperties nodeProp : fingersList) {

            System.out.println("POSITION: "+ i +"  Node ID : " + nodeProp.getNodeId()+
                    ":  Node IP : " + nodeProp.getLocalIp()+
                    ":  Node Port : " + nodeProp.getPort());

            i++;
        }
        System.out.println("\n_____________________________");
    }

    public void checkPredecessor() {

        if(this.getPredecessorId() == null)
            return;

        try {
            registry = LocateRegistry.getRegistry(this.getPredecessorIp());
        } catch (RemoteException e) {
            this.setPredecessorIp(null);
            this.setPredecessorId(null);
            System.out.println("\nPredecessor Not Found\n");
            return;
        }
        try {
            Node pre = (Node)  registry.lookup(String.valueOf(this.getPredecessorId()));
            if(pre != null && pre.isConnected() == true) {
                //System.out.println("\nPredecessor ok\n");
                return;
            }
            else {
                this.setPredecessorIp(null);
                this.setPredecessorId(null);
                //System.out.println("\nPredecessor Not Found\n");
                return;
            }

        } catch (RemoteException e) {
            this.setPredecessorIp(null);
            this.setPredecessorId(null);
            //System.out.println("\nPredecessor Not Found\n");
            return;
        } catch (NotBoundException e) {
            this.setPredecessorIp(null);
            this.setPredecessorId(null);
            //System.out.println("\nPredecessor Not Found\n");
            return;
        } catch (Exception e) {
            this.setPredecessorIp(null);
            this.setPredecessorId(null);
            //System.out.println("\nPredecessor Not Found\n");
            return;
        }


    }

    public synchronized void exit() throws RemoteException {


        Node pred = null;
        Node suc = null;
        try {

            registry = LocateRegistry.getRegistry(this.getPredecessorIp());
            pred = (Node)  registry.lookup(String.valueOf(this.getPredecessorId()));
            pred.setSuccessorId(this.getSuccessorId());
            pred.setSuccessorIp(this.getSuccessorIp());
            pred.setSuccessorPort(this.getSuccessorPort());
            registry = LocateRegistry.getRegistry(this.getSuccessorIp());
            suc = (Node)  registry.lookup(String.valueOf(this.getSuccessorId()));
            suc.setPredecessorIp(null);
            suc.setPredecessorId(null);
            suc.setPredecessorPort(0);
            this.redistributeKeys();


        } catch (NotBoundException e) {
            e.printStackTrace();
        }




        this.setPredecessorId(null);
        this.setPredecessorIp(null);
        this.setPredecessorPort(0);
        this.setSuccessorId(null);
        this.setSuccessorIp(null);
        this.setSuccessorPort(0);
        this.setConnected(false);
        System.out.println("\nNode with ID : " + this.getNodeId() + "Exiting\n");
        System.exit(120);

    }

    private BigInteger calculateNodeId(String iNet, int port) throws NoSuchAlgorithmException {

        String input = iNet + String.valueOf(port);
        System.out.println("String: " + input);

        MessageDigest mDigest = MessageDigest.getInstance(DEFAULT_HASH_ALGORITHM);
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        //System.out.println("IP: " + iNet + "\n" + "port: " + port + "\n" + "String: " + sb.toString() + " Int: " + new BigInteger(sb.toString(), 16));

        return new BigInteger(sb.toString(), 16);

    }



    public void redistributeKeys() throws RemoteException, NotBoundException {
        NodeProperties fileholder;
        File directory = new File("chordData/");
        byte[] buffer= new byte[1024];
        int bytesRead=0;

        if(directory.exists()) {
            System.out.println("Redistributing files...");

            File[] contents = directory.listFiles();
            long start = System.nanoTime();

            if (contents.length == 0)
                return;
            else {
                for (File file : contents) {
                    try {

                        InputStream is = new FileInputStream(file);

                        bytesRead = is.read(buffer, 0, (int) file.length());
                        String fileName = file.getName();
                        file.delete();
                        Thread th;

                        th = new Thread(new FileUploaderHandler(this, fileName, buffer, bytesRead));
                        th.start();
                        th.join();


                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }

            }
            long elapsedTime = System.nanoTime() - start;
            System.out.println("It took me "+ elapsedTime/1000000000.0+" seconds to redistribute my Keys");
        }
    }





    public NodeProperties broadcast() throws RemoteException, NotBoundException, MalformedURLException {


        NodeProperties np = new NodeProperties(port, nodeId, localIp);

        Broadcast bc = new Broadcast(np);

        return  bc.sendBroadcast();


    }

    public void reciever() {

        (new Thread(new BroadcastReceiver(myNodeProperties))).start();

    }

    public String getLocalIp() {
        return this.localIp;
    }

    public BigInteger getSuccessorId() {
        return this.successorId;
    }

    public String getSuccessorIp() {
        return this.successorIp;
    }

    public int getSuccessorPort() {
        return this.successorPort;
    }

    public BigInteger getPredecessorId() {
        return this.predecessorId;
    }

    public String getPredecessorIp() {
        return this.predecessorIp;
    }

    public int getPredecessorPort() {
        return this.predecessorPort;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public BigInteger getNodeId() {
        return this.nodeId;
    }

    public void setSuccessorId(BigInteger sucId) {
        this.successorId = sucId;
    }

    public void setSuccessorIp(String sucIp) { this.successorIp = sucIp; }

    public void setSuccessorPort(int port) {
        this.successorPort = port;
    }

    public void setPredecessorId(BigInteger preId) {
        this.predecessorId = preId;
    }

    public void setPredecessorIp(String preIp) {
        this.predecessorIp = preIp;
    }

    public void setPredecessorPort(int port) {
        this.predecessorPort = port;
    }

    public void setConnected(boolean con) {
        this.connected = con;
    }

    public int getPort() { return this.port; }

    public void printNodeStatus() {
        System.out.println("\n_________NODE STATUS_________");
        System.out.println("Node Ip : "+ this.localIp);
        System.out.println("Node Port : "+ port);
        System.out.println("Node ID : "+ this.nodeId);
        System.out.println("Connected : "+ this.connected);
        System.out.println("Node Successor ID : "+ this.successorId);
        System.out.println("Node Successor IP : "+ this.successorIp);
        System.out.println("Node Successor Port : "+ this.successorPort);
        System.out.println("Node Predecessor ID : "+ this.predecessorId);
        System.out.println("Node Predecessor IP : "+ this.predecessorIp);
        System.out.println("Node Predecessor Port : "+ this.predecessorPort);
        System.out.println("_____________________________\n");

    }
}
