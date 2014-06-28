package Chord;

import sun.misc.REException;

import java.awt.image.renderable.RenderableImage;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by John on 3/6/2014.
 */
public interface Node extends Remote {


    public NodeProperties findSuccessor(BigInteger newNodeId) throws RemoteException, NotBoundException;

    public NodeProperties closestPrecedingNode(BigInteger id) throws RemoteException;

    public void stabilize() throws RemoteException;

    public void notify(NodeProperties np) throws RemoteException, NotBoundException;

    public void fixFingers() throws RemoteException, NotBoundException;

    public void addFinger(NodeProperties np) throws RemoteException;

    public void printFingerList() throws RemoteException;

    public void checkPredecessor() throws RemoteException;

    public void exit() throws RemoteException;

    public NodeProperties broadcast() throws RemoteException, NotBoundException, MalformedURLException;

    public void reciever() throws RemoteException;

    public BigInteger getSuccessorId() throws RemoteException;

    public String getSuccessorIp() throws RemoteException;

    public int getSuccessorPort() throws RemoteException;

    public int getPredecessorPort() throws RemoteException;

    public void setSuccessorId(BigInteger suc) throws RemoteException;

    public void setSuccessorIp(String sucIp) throws RemoteException;

    public void setSuccessorPort(int port) throws RemoteException;

    public void setPredecessorPort(int port) throws RemoteException;

    public String getLocalIp() throws RemoteException;

    public BigInteger getPredecessorId() throws RemoteException;

    public String getPredecessorIp() throws RemoteException;

    public void setPredecessorId(BigInteger preId) throws RemoteException;

    public void setPredecessorIp(String preIp) throws RemoteException;

    public boolean isConnected() throws RemoteException;

    public void setConnected(boolean con) throws RemoteException;

    public BigInteger getNodeId() throws RemoteException;

    public int getPort() throws RemoteException;

    public void printNodeStatus() throws RemoteException;

    public void redistributeKeys() throws RemoteException, NotBoundException;

}