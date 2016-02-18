/*
 * COEN 317 Project
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;
/**
 * Source: http://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optional/
 */

public class ChatServer {
    private static int uniqueID;
    private static ServerSocket serverSocket;
    private static int port = 8080;
    boolean loggedIn = true;

    private ServerGUI sg;
   
    private static ArrayList<ClientThread> list; //Keep track of clients
    private SimpleDateFormat sdf;
    
    public ChatServer(int port) {
        this(port, null);
    }

    public ChatServer(int port, ServerGUI sg) {
        //GUI or not
        this.sg = sg;
        this.port = port;
        sdf = new SimpleDateFormat("HH:mm:ss");
        list = new ArrayList<ClientThread>();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port); //Start listening on port
            
            System.out.println("Web Server running on Inet Address " + serverSocket.getInetAddress()
                    + " port " + serverSocket.getLocalPort());
            
            //System.out.println("Working Directory: \"" + System.getProperty("user.dir").replace('\\', '/') + "\"");

            //Server infinite loop and wait for clients to connect
            while (true) {
                
                Socket socket = serverSocket.accept(); //accept client connection
                System.out.println("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
                
                //Create a new thread and handle the connection
                ClientThread ct = new ClientThread(socket);
                list.add(ct); //Save client to ArrayList
                ct.start();
            }
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void stop()
    {
        loggedIn = false;
        try
        {
            new Socket("localhost", port);
        }
        catch (Exception e)
        {
            //cry
        }
    }
    
    //Broadcast a message to all Clients
    private synchronized void broadcast(String message) {
       String time = sdf.format(new Date());
       
       message = time + ": " + message + "\n";
       System.out.print(message);

       // we loop in reverse order in case we would have to remove a Client
       for(int i = list.size(); i >= 0; --i) {
            ClientThread ct = list.get(i);
            // try to write to the Client if it fails remove it from the list
            if(!ct.writeMsg(message)) {
                list.remove(i);
                System.out.print("Disconnected Client: " + ct.username + " removed from list");
            }
       }
    }

    // for a client who logoff using the LOGOUT message
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for(int i = 0; i < list.size(); ++i) {
            if(list.get(i).id == id) {
                list.remove(i);
                return;
            }
        }
    }
        
    public static void main(String[] args) {
        //Read in port from command line
        //default to port 8080 if there's a parsing error
        try {
            port = Integer.parseInt(args[0]);
            System.out.println("Port number set to " + port + "\n");
        } catch (Exception e) {
            System.out.println("Error parsing port number; " +
                    "using port number 8080\n");
        }
         
        ChatServer cs = new ChatServer(port);
        cs.start();
    }
 
    
    //Class to handle the clients
    class ClientThread extends Thread {
        Socket socket;
        ObjectInputStream in;
        ObjectOutputStream out;
        
        int id; //Unique ID (easier for deconnection)
        String username; //Client username
        
        //Chat Message
        ChatMessage cm;
        String date;

        // Constructore
        ClientThread(Socket socket) {
            // a unique id
            id = ++uniqueID;
            this.socket = socket;

            //Create both Data Stream
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in  = new ObjectInputStream(socket.getInputStream());
                
                // read the username
                username = (String) in.readObject();
                System.out.println(username + " has connected");
                
            } catch (IOException e) {
                System.out.println("Exception creating new Input/output Streams: " + e);
                return;
            } catch (ClassNotFoundException e) {}

            date = new Date().toString() + "\n";
        }

        @Override
        public void run() {
            loggedIn = true;
            //Keep running until LOGOUT
            while(loggedIn) {
                // read a String (which is an object)
                try {
                    cm = (ChatMessage) in.readObject();
                } catch (IOException e) {
                    System.out.println(username + " Exception reading Streams: " + e);
                    break;			
                } catch(ClassNotFoundException e2) {
                    break;
                }
                // the messaage part of the ChatMessage
                String message = cm.getMessage();

                // Switch on the type of message receive
                switch(cm.getType()) {

                case ChatMessage.MESSAGE:
                    broadcast(username + ": " + message);
                    break;
                case ChatMessage.LOGOUT:
                    System.out.println(username + " disconnected with a LOGOUT message.");
                    loggedIn = false;
                    break;
                case ChatMessage.WHOISIN:
                    writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
                    // scan al the users connected
                    for(int i = 0; i < list.size(); ++i) {
                        ClientThread ct = list.get(i);
                        writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
                    }
                    break;
                }
            }
            
            //Remove self from the arrayList containing the list of connected Clients
            remove(id);
            close();
        }

        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if(out != null) out.close();
            } catch(Exception e) {}
            
            try {
                if(in != null) in.close();
            } catch(Exception e) {}
            
            try {
                if(socket != null) socket.close();
            } catch (Exception e) {}
        }

        //Write message to the Client output stream
        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }
            
            // write the message to the stream
            try {
                out.writeObject(msg);
            } catch(IOException e) {
                // if an error occurs, do not abort just inform the user
                System.out.println("Error sending message to " + username);
                System.out.println(e.toString());
            }
            
            return true;
        }
    }
}
