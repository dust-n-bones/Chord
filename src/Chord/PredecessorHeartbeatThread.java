package Chord;

import java.rmi.RemoteException;

/**
 * Created by John on 5/6/2014.
 * Thead που ελέγχει τον predecessor
 */
public class PredecessorHeartbeatThread implements Runnable{

    private static final int SLEEP_TIME = 1000;
    private Node node;

    public PredecessorHeartbeatThread(Node node) {

        this.node = node;

    }

    @Override
    public void run() {

        while(true) {

            try {
                node.checkPredecessor();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }
}
