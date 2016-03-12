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
    //Attaches a unique ID to all users
    private static int uniqueID;
    //Attaches a unique transferID to all file transfers
    private static int transferID;

    private static ServerSocket serverSocket;
    private static int port = 8080; //default port number is 8080
    boolean continueServer = true;

    //GUI of server
    private ServerGUI sg;
   
    private static ArrayList<ClientThread> list; //Keep track of clients
    private static ArrayList<FileTransferHandler> fileTransfers; //Keep track of file transfers
    private static ArrayList<UserHistory> userHistories; //Maintain a chat history for each user

    private SimpleDateFormat sdf;

    //Locks
    private Object lock1 = new Object();
    private Object lock2 = new Object();

    //Constructor with no GUI
    public ChatServer(int port) {
        //open server with no GUI
        this(port, null);
    }

    //Constructor with GUI
    public ChatServer(int port, ServerGUI sg) {
        this.sg = sg;
        this.port = port;

        sdf = new SimpleDateFormat("HH:mm:ss");

        list = new ArrayList<>();
        fileTransfers = new ArrayList<>();
        userHistories = new ArrayList<>();

        //Set initial values of the ID's
        uniqueID=1;
        transferID=1;
    }
    
    public void start() {
        try {
            //Start listening on port
            serverSocket = new ServerSocket(port);

            //Inform user that Server is starting
            event("Web Server running on Inet Address " + serverSocket.getInetAddress()
                    + " port " + serverSocket.getLocalPort());
            

            //Server infinite loop and wait for clients to connect
            while (continueServer) {

                //accept client connection
                Socket socket = serverSocket.accept();

                //Inform user when a connection is accepted
                event("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
                
                //Create a new thread and handle the connection
                ClientThread ct = new ClientThread(socket);

                //Checks to see if a user is already logged in
                if(ct.checkUsername()) {
                    //Lock list of clients before adding to list
                    synchronized (lock1) {
                        list.add(ct); //Save client to ArrayList
                    }
                    ct.start();
                }
            }
            //when server stops
            try
            {
                serverSocket.close();
                //close all threads
                for(int i=0; i<list.size(); i++)
                {
                    ClientThread tempThread = list.get(i);
                    try{
                        tempThread.socket.close();
                        tempThread.in.close();
                        tempThread.out.close();
                    }
                    catch (IOException ioE){

                    }
                }
            }
            catch (Exception e)
            {
                event("Exception in closing server and clients: " + e);
            }
            
        } catch (Exception e) {
            event(sdf.format(new Date()) + " Exception in starting server: " + e);
        }
    }

    public void stop()
    {
        continueServer = false;
        try
        {
            new Socket("localhost", port);
        }
        catch (Exception e)
        {
            //cry
        }
    }

    //If GUI available, append message to textfield, else System.out
    public void event(String msg)
    {
        String time = sdf.format(new Date())+ " " + msg;
        if(sg == null)
            System.out.println(time);
        else
            sg.appendEvent(time + "\n");
    }
    
    //Broadcast a message to all Clients
    private synchronized void broadcast(String message) {
       String time = sdf.format(new Date());
       
       message = time + ": " + message;
        if(sg == null)
            System.out.print(message);
        else
            sg.appendRoom(message);

       // we loop in reverse order in case we would have to remove a Client
       for(int i = list.size()-1; i >= 0; --i) {
            ClientThread ct = list.get(i);
           System.out.println("Sending a message to client" + i);
            // try to write to the Client if it fails remove it from the list
            if(!ct.writeMsg(new ChatMessage(ChatMessage.MESSAGE, message, null, new UserId(0, "Server"), new Date()))) {
                removeThread(i);
                event("Disconnected Client" + i + " : " + ct.username + " removed from list");
            }
       }
    }
    //Send a message to all clients in recipients list
    private synchronized void multicast(ChatMessage cm, String username, int id) {
        ArrayList<UserId> recipients = cm.getRecipients();
        String time = sdf.format(new Date());
        Date date = new Date();

        System.out.println("A message was received from: " + cm.getSender().getName() + " " + cm.getSender().getId());

        //Display message in Server chatroom
        String message = time + ": " + username + ": " + cm.getMessage();
        if(sg == null)
            System.out.print(message);
        else
            sg.appendRoom(message);

        // we loop in reverse order in case we would have to remove a Client
        for(int i = list.size()-1; i >= 0; --i) {
            ClientThread ct = list.get(i);
            //Only send message if ct is in the recipients list
            if(recipients.contains(new UserId(ct.id, ct.username))) {

                //Arraylist of UserId to inform client who is in chatroom
                ArrayList<UserId> recipients2 = new ArrayList<>();
                recipients2.addAll(cm.getRecipients());
                recipients2.remove(new UserId(ct.id, ct.username));
                recipients2.add(cm.getSender());

                // try to write to the Client, if it fails remove it from the list
                if (!ct.writeMsg(new ChatMessage(ChatMessage.MESSAGE, cm.getMessage(), recipients2, cm.getSender(), date))) {
                    removeThread(i);
                    event("Disconnected Client" + i + " : " + ct.username + " removed from list");
                }
            }
            //Return message to sender (This ensures that ordering is consistent)
            else if(ct.id == id) {
                if (!ct.writeMsg(new ChatMessage(ChatMessage.MESSAGE, cm.getMessage(), recipients, cm.getSender(), date))) {
                    removeThread(i);
                    event("Disconnected Client" + i + " : " + ct.username + " removed from list");
                }
            }
        }
    }

    //Send multicasted file information
    public synchronized void sendFileTransfer(ArrayList<UserId> recipients, ChatMessage cm){
        event("Sending file transfer message: " + cm.getTransferId());

        // we loop in reverse order in case we would have to remove a Client
        for(int i = list.size()-1; i >= 0; --i) {
            ClientThread ct = list.get(i);

            //Only send message if ct is in the recipients list
            if(recipients.contains(new UserId(ct.id, ct.username))) {
                System.out.println("A user was found");

                // try to write to the Client, if it fails remove it from the list
                if (!ct.writeMsg(cm)){
                    removeThread(i);
                    event("Disconnected Client" + i + " : " + ct.username + " removed from list");
                }
            }
        }

    }

    //Send a null message to a user, this allows the user to clear out the buffer stream
    public synchronized void sendNull(UserId user) {
        ChatMessage cMsg = new ChatMessage(10, "", new UserId(0, "Server"), new Date());

        // we loop in reverse order in case we would have to remove a Client
        for(int i = list.size()-1; i >= 0; --i) {
            ClientThread ct = list.get(i);

            //Only send message if ct is in the recipients list
            if(user.equals(new UserId(ct.id, ct.username))) {

                // try to write to the Client, if it fails remove it from the list
                if (!ct.writeMsg(cMsg)){
                    removeThread(i);
                    event("Disconnected Client" + i + " : " + ct.username + " removed from list");
                }
            }
        }
    }


    //Function removes thread from ClientThread list and then updates all client's user list
    private synchronized void removeThread(int index) {
        //lock list before modifying
        synchronized (lock1) {
            list.remove(index);
        }
        for(int i = list.size()-1; i >= 0; --i) {
            ClientThread ct = list.get(i);
            whoIsIn(ct);
        }
    }

    //Updates a client's userlist
    private synchronized void whoIsIn(ClientThread thread) {
        thread.writeMsg(new ChatMessage(ChatMessage.MESSAGE, "List of the users connected at " + sdf.format(new Date()), null, new UserId(0, "Server"), new Date()));

        // scan all the users connected
        for(int i = 0; i < list.size(); ++i) {
            ClientThread ct = list.get(i);

            if(ct.id == thread.id)
                thread.writeUser(ct.username, ct.id, true);
            else
                thread.writeUser(ct.username, ct.id, false);
        }
        //send an empty message to end
        thread.writeMsg(new ChatMessage(ChatMessage.MESSAGE, "", null, new UserId(0, "Server"), new Date()));


    }


    //removes a client from ClientThread
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for(int i = 0; i < list.size(); ++i) {
            ClientThread ct = list.get(i);

            if(ct.id == id) {
                removeThread(i);
                event("Disconnected Client" + i + " : " + ct.username + " removed from list");
                return;
            }
        }
    }

    //Saves a user's chat history
    void saveHistory(UserHistory history) {
        event("Saving history: " + history.getUsername() + " of size: " + history.getChatrooms().size());
        String username = history.getUsername();

        //lock the history list before saving to it
        synchronized (lock2) {

            //Erases a user's previously saved history if exists
            for (UserHistory t : userHistories) {
                if (t.getUsername().equals(username)) {
                    userHistories.remove(t);
                    break;
                }
            }
            userHistories.add(history);
        }
    }

    //Check if a user has a chathistory and sends history to user
    Boolean findHistory(String username, ObjectOutputStream out) {
        //Lock history list before accessing
        synchronized (lock2) {

            for (UserHistory t : userHistories) {
                if (t.getUsername().equals(username)) {
                    //write userHistory to output stream
                    try {
                        out.writeObject(t);
                        event("Found history of size: " + t.getChatrooms().size());
                    }
                    catch (IOException e) {
                        event("Error writing userHistory" + e.getMessage());
                    }
                    return true;
                }
            }
        }

        //If history not found, an empty history is sent to client
        UserHistory history = new UserHistory(username);
        try {
            out.writeObject(history);
        }
        catch (IOException e) {
            event("Error writing userHistory" + e.getMessage());
        }
        return false;
    }

    ChatServer getServer(){
        return this;
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

            } catch (IOException e) {
                event("Exception creating new Input/output Streams: " + e);
                return;
            } catch (ClassNotFoundException e) {}

            date = new Date().toString() + "\n";

        }

        //checks to see if user is already logged in
        public boolean checkUsername() throws ClassNotFoundException, IOException {
            for(ClientThread cthread : list) {

                if(cthread.username.equals(username)) {
                    UserHistory history = new UserHistory(username);

                    try {
                        out.writeObject(history);
                    }
                    catch (IOException e) {
                        event("Error writing userHistory" + e.getMessage());
                    }
                    //if user is logged in, prevent user from logging in again
                    out.writeObject(new ChatMessage(ChatMessage.LOGOUT, "Username is already in use, please select another username", new UserId(0, "Server"), new Date()));
                    close();
                    return false;
                }
            }

            event(username + " has connected");
            return true;
        }

        @Override
        public void run() {
            //Send chatHistory
            if(findHistory(username, out)) {
                event("History of " + username + " found!");
            }

            //Update user list of all users
            for(int i = list.size()-1; i >= 0; --i) {
                ClientThread ct = list.get(i);
                whoIsIn(ct);
            }

            boolean loggedIn = true;
            //Keep running until LOGOUT
            while(loggedIn) {
                // read a ChatMessage (which is an object)
                try {
                    cm = (ChatMessage) in.readObject();
                } catch (IOException e) {
                    event(username + " Exception reading Streams: " + e);
                    break;			
                } catch(ClassNotFoundException e2) {
                    break;
                }

                // the message part of the ChatMessage
                String message = cm.getMessage();

                // Switch on the type of message receive
                switch(cm.getType()) {

                    case ChatMessage.MESSAGE:
                        //Check to see if message is a broadcast or multicast/singlecast
                        if(cm.getRecipients().size()==0)
                            broadcast(username + ": " + message);
                        else
                            multicast(cm, username, id);
                        break;

                    case ChatMessage.LOGOUT:
                        event(username + " disconnected with a LOGOUT message.");

                        //get user's history
                        UserHistory history;
                        try {
                            history = (UserHistory) in.readObject();
                        } catch (IOException e) {
                            event(username + " Exception reading Streams: " + e);
                            break;
                        } catch(ClassNotFoundException e2) {
                            break;
                        }
                        if(history != null)
                            saveHistory(history);

                        loggedIn = false;
                        break;

                    case ChatMessage.WHOISIN:
                        System.out.println("WHOISIN received from " + username);
                        whoIsIn(this);
                        break;

                    case ChatMessage.FILE:
                        if (cm.getFileStatus() == ChatMessage.FILESEND) {
                            FileTransferHandler newFTH = new FileTransferHandler(cm, transferID, in, getServer());

                            //store filetransfer in arraylist
                            fileTransfers.add(newFTH);
                            //increment transferID so it remains unique
                            //TODO: eventually transferID may loop back, handle this (Check uniqueness?)
                            transferID++;

                            try{
                                in.readObject();
                            }
                            catch (ClassNotFoundException e){
                            }
                            catch (IOException e){

                            }
                        }
                        else if(cm.getFileStatus() == ChatMessage.FILEDENY) {

                            //Remove this user from recipient list
                            for(int i = 0; i < fileTransfers.size(); i++) {
                                if(cm.getTransferId() == fileTransfers.get(i).getTransferId()) {
                                    fileTransfers.get(i).removeRecipient(cm.getSender());

                                    //Inform file sender that file denied
                                    ArrayList<UserId> recipient = new ArrayList<>();
                                    recipient.add(fileTransfers.get(i).getSender());
                                    multicast(new ChatMessage(ChatMessage.MESSAGE, "User: " + cm.getSender().getId() + " " + cm.getSender().getName() + " has denied the file transfer request", recipient, new UserId(0, "Server"), new Date())
                                            , "Server", 0);

                                    //Close filetransfer if user count drops to 0
                                    if(fileTransfers.get(i).getRecipientSize() <= 0){
                                        event("Closing filetransfer: " + fileTransfers.get(i).getTransferId());
                                        fileTransfers.remove(i);
                                    }
                                    break;
                                }
                            }

                        }
                        else if(cm.getFileStatus() == ChatMessage.FILEACCEPT){
                            for(int i = 0; i < fileTransfers.size(); i++) {
                                if (cm.getTransferId() == fileTransfers.get(i).getTransferId()) {
                                    fileTransfers.get(i).sendFile(cm.getTransferId(), cm.getSender(), out);

                                    ArrayList<UserId> recipient = new ArrayList<>();
                                    recipient.add(fileTransfers.get(i).getSender());
                                    multicast(new ChatMessage(ChatMessage.MESSAGE, "User: " + cm.getSender().getId() + " " + cm.getSender().getName() + " has received the file", recipient, new UserId(0, "Server"), new Date())
                                            , "Server", 0);

                                    //Remove user from filetransfer recipient list
                                    fileTransfers.get(i).removeRecipient(cm.getSender());

                                    //Close filetransfer if user count drops to 0
                                    if(fileTransfers.get(i).getRecipientSize() <= 0){
                                        event("Closing filetransfer: " + fileTransfers.get(i).getTransferId());
                                        fileTransfers.remove(i);
                                    }
                                    break;
                                }
                            }
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
            for(int i = fileTransfers.size()-1; i >= 0; i--) {
                fileTransfers.remove(i);
            }
        }

        //Write message to the Client output stream
        private boolean writeMsg(ChatMessage cMsg) {
            // if Client is still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }

            // write the message to the stream
            try {
                out.writeObject(cMsg);
            } catch(IOException e) {
                // if an error occurs, do not abort just inform the user
                event("Error sending message to " + username);
                event(e.toString());
            }
            
            return true;
        }
        //Send user info to a client
        private boolean writeUser(String msg, int userID, boolean isReceiver) {
            // if Client is still connected send the message to it
            if (!socket.isConnected()) {
                close();
                return false;
            }
            ChatMessage cMsg = new ChatMessage(ChatMessage.WHOISIN, msg, new UserId(0, "Server"), isReceiver);
            cMsg.setUserID(userID);

            // write the message to the stream
            try {
                out.writeObject(cMsg);
            } catch (IOException e) {
                // if an error occurs, do not abort just inform the user
                event("Error sending message to " + username);
                event(e.toString());
            }

            return true;
        }
    }
}
