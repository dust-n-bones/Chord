package Chord;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by John on 2/6/2014.
 */
public class BroadcastReceiver implements Runnable {


    private static final int BROADCAST_PORT = 7000;
    private static final int BYTE_ARRAY_SIZE = 5000;
    private NodeProperties ns;
    ObjectInputStream ois;

    public BroadcastReceiver(NodeProperties ns) {
    this.ns= ns;
    }

    @Override
    public void run() {


        byte[] buffer = new byte[BYTE_ARRAY_SIZE];

        boolean listening = true;

        try {




            DatagramSocket dataSocket = new DatagramSocket(BROADCAST_PORT);
            dataSocket.setBroadcast(true);
            dataSocket.setReuseAddress(true);
            DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length);



            while (listening) {




                dataSocket.receive(dataPacket);

                ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer);
                ois = new ObjectInputStream(new BufferedInputStream(byteStream));

                NodeProperties np = (NodeProperties) ois.readObject();

                System.out.println("NodeId : " + np.getNodeId() + "\n" + " Port : " + np.getPort() + "   Port    "+ dataPacket.getPort()+  "   Address    "+ dataPacket.getAddress());


                byteStream.close();

                Socket socket= new Socket(dataPacket.getAddress(),np.getPort());


                ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
                objectOutput.flush();
                objectOutput.writeObject(ns);
                objectOutput.flush();
                socket.close();


            }

            dataSocket.close();
            ois.close();


        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }
}
