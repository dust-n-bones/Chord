package Chord;

import java.io.Serializable;
import java.math.BigInteger;


/**
 * Created by John on 2/6/2014.
 * Thread στο οποι υάρχουν properties για τον κάθε κόμβο
 */
public class NodeProperties implements Serializable {


    private int port;
    private BigInteger nodeId;
    private String localIp;

    public NodeProperties(int port, BigInteger nodeId, String ip) {

        this.port = port;
        this.nodeId = nodeId;
        this.localIp = ip;

    }

    public int getPort() {
        return this.port;
    }

    public BigInteger getNodeId() {
        return this.nodeId;
    }

    public String getLocalIp() {
        return this.localIp;
    }
}
