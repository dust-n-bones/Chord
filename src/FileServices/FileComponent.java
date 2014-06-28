package FileServices;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by John on 7/6/2014.
 *
 * -1 team
 */
public class FileComponent implements Serializable{


    private String checkSum;
    private int noOfChunks;


    public FileComponent (String cs, int noc) {

        this.checkSum = cs;
        this.noOfChunks = noc;

    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }


    public int getNoOfChunks() {
        return noOfChunks;
    }

    public void setNoOfChunks(int noOfChunks) {
        this.noOfChunks = noOfChunks;
    }

}
