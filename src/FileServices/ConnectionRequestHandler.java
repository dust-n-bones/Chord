package FileServices;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by John on 6/6/2014.
 * To thread αυτό αναλαβμάνει να ανεβάσει μία ομάδα στον ίδιο τον client ή να εξυπηρετήσει κάποιον άλλο client που θέλει ένα αρχείο που έχουμε.
 */
public class ConnectionRequestHandler implements Runnable {

    private Socket _socket = null;
    private DataOutputStream _out = null;
    private DataInputStream _in = null;
    private final int DEAFAULT_FILE_SIZE = 1024;
    private final String LINE_SEPERATOR = "\n";


    public ConnectionRequestHandler(Socket socket) {

        _socket = socket;


    }

    public void run() {



        String requestedFile;
        String fileTitle;

        byte[] chunk = new byte[DEAFAULT_FILE_SIZE];

        try {
            _out = new DataOutputStream(new BufferedOutputStream(_socket.getOutputStream()));
            _in = new DataInputStream(new BufferedInputStream(_socket.getInputStream()));



        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            String request = _in.readUTF();
           // = new String(chunk, StandardCharsets.UTF_8);

            //System.out.println(request);

            if(request.equals("UPLOAD" + LINE_SEPERATOR)) {

                fileTitle = _in.readUTF();

                File folder = new File("chordData/");
                if (!folder.exists()) {
                    folder.mkdir();
                }

                File file = new File("chordData/" +fileTitle);

                if(file.exists()){
                    file.delete();
                }
                file.createNewFile();

                FileOutputStream fos = new FileOutputStream(file);

                //BufferedInputStream bis = new BufferedInputStream(_socket.getInputStream());



                int bytesRead = 0;

                while((bytesRead = _in.read(chunk)) > 0) {

                    //System.out.println("\nREAD : " + Arrays.toString(chunk) +"    " + bytesRead);

                    fos.write(chunk, 0, bytesRead);
                }

                _out.close();
                fos.close();
                //bis.close();
                _in.close();
                _socket.close();


            }else if(request.equals("DOWNLOAD" + LINE_SEPERATOR)) {

                requestedFile = _in.readUTF();

                File folder = new File("chordData/");
                if (!folder.exists()) {
                    folder.mkdir();
                }

                File file = new File("chordData/" + requestedFile);

                if(!file.exists()) {
                    _out.writeUTF("NOFILEFOUND");
                    _out.flush();
                    _out.close();
                    _in.close();
                    _socket.close();
                    return;
                }
                _out.writeUTF("FILEEXISTS");
               _out.flush();

                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

                int bytesRead = 0;


                while((bytesRead = bis.read(chunk)) > 0) {

                    _out.write(chunk, 0, bytesRead);
                }

                _out.close();
                bis.close();
                _in.close();
                _socket.close();

//            } else if(request.equals("DOWNLOADFILEINFO")) {
//
//                requestedFile = _in.readLine();
//
//                File file = new File("chordData/" + requestedFile);
//
//                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
//
//                ObjectOutputStream oos = new ObjectOutputStream(_socket.getOutputStream());
//
//                if(!file.exists()) {
//
//                    oos.writeObject(new FileComponent(null, 0));
//                    oos.close();
//                    ois.close();
//                    _out.close();
//                    _in.close();
//                    _socket.close();
//                    return;
//                }
//
//
//                FileComponent fc = (FileComponent) ois.readObject();
//
//                oos.writeObject(fc);
//
//                _out.close();
//                ois.close();
//                oos.close();
//                _in.close();
//                _socket.close();
//
             }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
