import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Created by Daniel on 2/18/2016.
 * Source: http://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optional/
 */
public class ServerGUI extends JFrame implements ActionListener, WindowListener{
    private static final long serialVersionUID = 1L;
    //the stop and start buttons
    private JButton stopStart;
    //JTextArea for the chatroom and the events
    private JTextArea chat, event;
    //The port number
    private JTextField tPortNumber;
    //my server
    private ChatServer server;


    //server constructor that receive the port to listen to for connection as paramter
    ServerGUI(int port)
    {
        super("Chat Server");
        server = null;
        //in the Northpanel the PortNumber the Start and Stop buttons
        JPanel north = new JPanel();
        north.add(new JLabel("Port Number: "));
        tPortNumber = new JTextField("  " + port);
        north.add(tPortNumber);
        // to stop or start the server, we start with "Start"
        stopStart = new JButton("Start");
        stopStart.addActionListener(this);
        north.add(stopStart);
        add(north, BorderLayout.NORTH);

        //the event and chat room
        JPanel center = new JPanel(new GridLayout(2,1));
        chat = new JTextArea(80, 80);
        chat.setEditable(false);
        appendRoom("Chat room.\n");
        center.add(new JScrollPane(chat));
        event = new JTextArea(80, 80);
        event.setEditable(false);
        appendEvent("Events log.\n");
        center.add(new JScrollPane(event));
        add(center);

        //need to be informed when the user click the close button on the frame
        addWindowListener(this);
        setSize(400, 600);
        setVisible(true);
    }

    //append message to the two JTextAreas
    //position at end
    void appendRoom(String str)
    {
        chat.append(str);
        chat.setCaretPosition(chat.getText().length() - 1);
    }
    void appendEvent(String str) {
        event.append(str);
        event.setCaretPosition(chat.getText().length() - 1);
    }

    //start or stopped when clicked
    public void actionPerformed(ActionEvent e) {
        //if running we have to stop
        if (server != null) {
            server.stop();
            server = null;
            tPortNumber.setEditable(true);
            stopStart.setText("Start");
            return;
        }
        //OK start the server
        int port;
        try {
            port = Integer.parseInt(tPortNumber.getText().trim());
        } catch (Exception er) {
            appendEvent("Invalid port number");
            return;
        }
        //create a new server
        server = new ChatServer(port, this);
        new ServerRunning().start();
        stopStart.setText("Stop");
        tPortNumber.setEditable(false);
    }

    //starting the server
    public static void main(String[] arg)
    {
        //default port 8080
        new ServerGUI(8080);
    }

    /**
     * If user clicks X button while connection active, the connection needs to be closed to free the port
     */
    public void windowClosing(WindowEvent e)
    {
        //check to see if server exists
        if(server != null) {
            try {
                server.stop();
            } catch (Exception eClose) {
            }
            server = null;
        }
        dispose();
        System.exit(0);
    }

    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e){}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    class ServerRunning extends Thread
    {
        public void run()
        {
            server.start();
            //start should continue until server fails
            stopStart.setText("Start");
            tPortNumber.setEditable(true);
            appendEvent("Server Crashed\n");
            server = null;
        }
    }

}
