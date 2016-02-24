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
    static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;
    
    private int type;
    private String message;
    private int userID;
    ArrayList<UserId> recipients;
    UserId sender;

    // constructor
    ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }
    ChatMessage(int type, String message, ArrayList<UserId> recipients, UserId sender) {
        this.type = type;
        this.message = message;
        this.recipients = recipients;
        this.sender = sender;
    }

    // getters
    int getType() {
        return type;
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
