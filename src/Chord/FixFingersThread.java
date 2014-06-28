package Chord;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by John on 6/6/2014.
 * Thread που τρέχει την fixfingers κάθε 10 δευτερόλεπτα
 */
public class FixFingersThread implements Runnable{

    private static final int SLEEP_TIME = 10000;
    private Node node;

    public FixFingersThread(Node node) {
        this.node = node;
    }


    public void run() {

        while (true) {

            try {
                node.fixFingers();
                node.printFingerList();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
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
