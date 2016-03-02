/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server. 
 * When talking from a Java Client to a Java Server a lot easier to pass Java objects, no 
 * need to count bytes or to wait for a line feed at the end of the frame
 */

import java.io.*;
import java.util.ArrayList;

/**
 * Source: http://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optional/
 */

public class ChatMessage implements Serializable {

    protected static final long serialVersionUID = 1112122200L;

    // The different types of message sent by the Client
    // WHOISIN to receive the list of the users connected
    // MESSAGE an ordinary message
    // LOGOUT to disconnect from the Server
    static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2, FILE = 3;
    static final int FILESEND = 0, FILEACCEPT = 1, FILEDENY = 2;
    
    private int type;
    private int fileStatus;
    private int transferId;
    private long fileSize;
    private String message;
    private int userID;
    private ArrayList<UserId> recipients;
    private UserId sender;
    private Boolean isYou;  //Allows server to tell client it's own UserId

    // constructor for a simple message to server
    ChatMessage(int type, String message, UserId sender) {
        this.type = type;
        this.message = message;
        recipients = new ArrayList<>();
        this.sender = sender;
        this.isYou = false;
    }
    //constructor for file messaging
    ChatMessage(int type, int fileStatus, int transferId, long fileSize, String message, UserId sender){
        this.type = type;
        this.fileStatus = fileStatus;
        this.transferId = transferId;
        this.fileSize = fileSize;
        this.message = message;
        recipients = new ArrayList<>();
        this.sender = sender;
        this.isYou = false;
    }
    //constructor for file transfer initializing
    ChatMessage(int type, int fileStatus, long fileSize, String message, ArrayList<UserId> recipients, UserId sender) {
        this.type = type;
        this.fileStatus = fileStatus;
        this.fileSize = fileSize;
        this.message = message;
        this.recipients = recipients;
        this.sender = sender;
    }
    //constructor for sending user information
    ChatMessage(int type, String message, UserId sender, Boolean isYou) {
        this.type = type;
        this.message = message;
        recipients = new ArrayList<>();
        this.sender = sender;
        this.isYou = isYou;
    }
    //constructor for sending directed messages
    ChatMessage(int type, String message, ArrayList<UserId> recipients, UserId sender) {
        this.type = type;
        this.message = message;
        this.recipients = recipients;
        this.sender = sender;
        this.isYou = false;
    }

    // getters
    int getType() {
        return type;
    }

    int getFileStatus(){
        return fileStatus;
    }

    int getTransferId(){
        return transferId;
    }

    long getFileSize(){
        return fileSize;
    }

    Boolean getIsYou(){
        return isYou;
    }
    
    String getMessage() {
        return message;
    }

    void setUserID(int userID)
    {
        this.userID = userID;
    }

    int getUserID()
    {
        return userID;
    }

    ArrayList<UserId> getRecipients() {
        return recipients;
    }

    UserId getSender() {
        return sender;
    }
}
