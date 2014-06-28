package FileServices;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by John on 6/6/2014.
 * Το thead αυτό περιμένει για αιτήσεις από τους υπόλοιπους χρήστες και για κάθε μια ανοίγει ένα νέο thread προκειμένου να εξυπηρετήσει τον client.
 */
public class FileServer implements Runnable {

    static int port; //Arbitrary port number

    public FileServer(int port) {

        this.port = port;
    }


    public void run() {

        ServerSocket serverSocket = null;
        boolean listening = true;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.exit(-1);
        }

        while(!Thread.interrupted()) {
            handleClientRequest(serverSocket);

        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleClientRequest(ServerSocket serverSocket) {
        try {
            new Thread (new ConnectionRequestHandler(serverSocket.accept())).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}