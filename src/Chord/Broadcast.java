package Chord;


import java.io.*;
import java.net.*;


/**
 * Created by John on 2/6/2014.
 */

public class Broadcast {

    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    private static final int BROADCAST_PORT = 7000;
    private static final int BYTE_ARRAY_SIZE = 5000;
    private static final int TIME_OUT = 5000;

    NodeProperties myNodeProp;


    public Broadcast(NodeProperties mynp) {

        this.myNodeProp = mynp;
    }


    public NodeProperties sendBroadcast() {


        try {

            DatagramSocket dataSocket = new DatagramSocket();
            dataSocket.setBroadcast(true);
            dataSocket.setReuseAddress(true);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(BYTE_ARRAY_SIZE);

            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(byteStream));
            oos.flush();
            oos.writeObject(myNodeProp);
            oos.flush();


            byte[] buffer = byteStream.toByteArray();

            InetAddress destAdress = InetAddress.getByName(BROADCAST_ADDRESS);

            DatagramPacket dataPack = new DatagramPacket(buffer, buffer.length, destAdress, BROADCAST_PORT);

            dataSocket.send(dataPack);

            oos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        ServerSocket responseSocket = null;

        try {

            responseSocket = new ServerSocket(myNodeProp.getPort());
            responseSocket.setSoTimeout(TIME_OUT);
            Socket socket = responseSocket.accept();

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            NodeProperties responseNodeProp = (NodeProperties) ois.readObject();

            System.out.println("NodeId : " + responseNodeProp.getNodeId() + "\n" + " Port : " + responseNodeProp.getPort());

            responseSocket.close();
            return responseNodeProp;

        } catch (SocketTimeoutException ste) {
            try {
                responseSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("\nSocket Time out!");
            return null;
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        return null;


    }

}