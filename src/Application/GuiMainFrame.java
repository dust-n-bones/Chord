package Application;

import Chord.Chord;
import Chord.Node;
import Chord.NodeImpl;
import Chord.NodeProperties;
import FileServices.FileDownloader;
import FileServices.FileServer;
import FileServices.FileUploader;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

/*
 * FrameDemo2.java shows off the window decoration features added in
 * 1.4, plus some window positioning code and (optionally)
 * setIconImage. It uses the file images/FD.jpg.
 */
public class GuiMainFrame extends  JPanel implements ActionListener {
    private Point lastLocation = null;
    private int maxX = 500;
    private int maxY = 500;

    protected final static String START_NODE= "start_node";
    protected final static String CANCEL = "cancel";

    static private final String newline = "\n";

    int portNo;

    Node node;

    public JTextArea txt;
    public JTextArea txtDown;
    public JTextArea log;
    public JFileChooser fc;
    public JButton startButton;
    public JButton exitButton;
    public JButton openButton;
    public JButton uploadButton;
    public JButton downloadButton;
    public File file;

    public static OutputScreen out;

    //Perform some initialization.
    public GuiMainFrame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        maxX = screenSize.width - 50;
        maxY = screenSize.height - 50;
    }


    // Create the window-creation controls that go in the main window.
    protected JComponent createOptionControls() {
        log = new JTextArea(20,70);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        log.setLineWrap(true);
        log.setBackground(new Color(225,225,225));

        JScrollPane logScrollPane = new JScrollPane(log);

        //Create a file chooser
        fc = new JFileChooser();

        //Uncomment one of the following lines to try a different
        //file selection mode.  The first allows just directories
        //to be selected (and, at least in the Java look and feel,
        //shown).  The second allows both files and directories
        //to be selected.  If you leave these lines commented out,
        //then the default mode (FILES_ONLY) will be used.
        //
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.addActionListener(this);

        //Create the open button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        openButton = new JButton("Select File");
        openButton.addActionListener(this);
        openButton.setVisible(false);

        //Create the save button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        uploadButton = new JButton("Upload File");
        uploadButton.addActionListener(this);
        uploadButton.setVisible(false);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        buttonPanel.add(uploadButton);

        //Add everything to a container.
        Box box = Box.createVerticalBox();
        box.add(logScrollPane);
        box.add(Box.createVerticalStrut(5)); //spacer
        box.add(buttonPanel);


        box.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        return box;
    }

    //Create the button that goes in the main window.
    protected JComponent createCancelButton() {
        JButton button = new JButton("Cancel");
        button.setActionCommand(CANCEL);
        button.addActionListener(this);


        //Center the button in a panel with some space around it.
        JPanel pane = new JPanel(); //use default FlowLayout
        pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        pane.add(button);

        return pane;
    }

    protected JComponent createStartNodeButton() {
        JLabel label = new JLabel("Port: ");
        //label.setHorizontalAlignment(SwingConstants.WEST);
        txt = new JTextArea(1, 10);
        txt.setEditable(true);
        txt.setBackground(new Color(200,200,200));
        startButton = new JButton("Start Node");
        startButton.setActionCommand(START_NODE);
        startButton.addActionListener(this);

        exitButton = new JButton("Exit");
        exitButton.setActionCommand(CANCEL);
        exitButton.addActionListener(this);
        exitButton.setVisible(false);


        //Center the button in a panel with some space around it.
        JPanel pane = new JPanel(); //use default FlowLayout
        Border bord = BorderFactory.createTitledBorder("Please firstly enter port and then start");
        pane.setBorder(bord);
        pane.add(label);
        pane.add(txt);
        pane.add(startButton);
        pane.add(exitButton);


        return pane;
    }


    protected JComponent createDownloadField() {
        JLabel label = new JLabel("File to Download: ");
        //label.setHorizontalAlignment(SwingConstants.WEST);
        txtDown = new JTextArea(1, 40);
        txtDown.setEditable(true);
        txtDown.setBackground(new Color(200,200,200));
        downloadButton = new JButton("Download");
        //downloadButton.setActionCommand(START_NODE);
        downloadButton.addActionListener(this);


        //Center the button in a panel with some space around it.
        JPanel pane = new JPanel(); //use default FlowLayout
        Border bord = BorderFactory.createEmptyBorder(20, 20, 20, 20);
        pane.setBorder(bord);
        pane.add(label);
        pane.add(txtDown);
        pane.add(downloadButton);


        return pane;
    }


    //Handle action events from all the buttons.
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();


        if(e.getSource() == startButton) {

            String port = txt.getText();

            try {
                portNo = Integer.parseInt(port);
            } catch(NumberFormatException num) {
                txt.setText("");
                log.append("Port should be 6100 - 6900\n");
                return;
            }


            if(portNo > 6900 || portNo < 6100) {
                txt.setText("");
                log.append("Port should be 6100 - 6900" + newline);

            } else {
                log.append("Choosen Port:  " +port + newline);
                log.append("Starting Node ...." + newline);

                openButton.setVisible(true);
                exitButton.setVisible(true);


                node = null;
                try {
                    node = new NodeImpl(portNo);

                    NodeProperties nodeProp = node.broadcast();

                    if(nodeProp == null) { //no node in ring, create ring
                        //System.out.println("YO");
                        Chord.create(node);
                    } else {
                        System.out.println("NodeId : " + nodeProp.getNodeId() + newline + " Port : " + nodeProp.getPort());

                        Chord.join(node, nodeProp);
                    }

                    Chord.stabilize(node);

                    Chord.checkPredecessor(node);

                    Chord.fixFinger(node);


                    new Thread(new FileServer(node.getPort())).start();

                    log.append("Node Successfully Started" + newline);

                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                } catch (NotBoundException e1) {
                    e1.printStackTrace();
                }


            }



        } else if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(GuiMainFrame.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();

                //This is where a real application would open the file.
                log.append("Opening File: " + file.getAbsolutePath() + "." + newline);
                uploadButton.setVisible(true);

            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());

            //Handle the first group of radio buttons.
        } else if (e.getSource() == uploadButton) {

            log.append("Uploading File: " + file.getAbsolutePath() + " to Cloud" + newline);

            log.setCaretPosition(log.getDocument().getLength());

            Thread th = new Thread(new FileUploader(file.getAbsolutePath() , node));
            th.start();
            try {
                th.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            log.append("Uploading File Completed " + newline);


            //new Thread(new FileDownloader("Dataflow.pdf" , node)).start();

        }
        else if (e.getSource() == downloadButton) {

            log.append("File To Downlaod: " + txtDown.getText() + newline);

            log.setCaretPosition(log.getDocument().getLength());

            log.append("Downloading... Please wait " + newline);



            Thread th = new Thread(new FileDownloader(txtDown.getText() , node));
            th.start();
            try {
                th.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            txtDown.setText("");
            txtDown.setText("");

            log.append("Downloading File Completed " + newline);


            //new Thread(new FileDownloader("Dataflow.pdf" , node)).start();

        }
        else if (e.getSource() == exitButton) {

            log.append("Exiting... Please wait: " +  newline);

            log.setCaretPosition(log.getDocument().getLength());



            try {
                Chord.nodeExit(node);
            } catch (RemoteException e1) {
                log.append("Error while exiting, please try again " +  newline);
                e1.printStackTrace();
            }

            //System.exit(200);

        }





    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Use the Java look and feel.
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(false);
        JDialog.setDefaultLookAndFeelDecorated(false);

        //Instantiate the controlling class.
        JFrame frame = new JFrame("Chord Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea textArea = new JTextArea (25, 80);

        textArea.setEditable (false);

        JFrame frameC = new JFrame ("Chord Console");
        frame.setDefaultCloseOperation (frameC.DO_NOTHING_ON_CLOSE);
        Container contentPanel = frameC.getContentPane ();
        contentPanel.setLayout (new BorderLayout ());
        contentPanel.add (
                new JScrollPane (
                        textArea,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
                BorderLayout.CENTER);
        frameC.pack ();
        frameC.setVisible (true);

        out = new OutputScreen(textArea);
        System.setOut (new PrintStream (out));

        //Create and set up the content pane.
        GuiMainFrame demo = new GuiMainFrame();

        //Add components to it.
        Container contentPane = frame.getContentPane();
        contentPane.add(demo.createStartNodeButton(),
                BorderLayout.PAGE_START);
        contentPane.add(demo.createOptionControls(),
                BorderLayout.CENTER);
        contentPane.add(demo.createDownloadField(),
                BorderLayout.PAGE_END);



        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null); //center it
        frame.setVisible(true);
    }

    //Start the demo.
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

}