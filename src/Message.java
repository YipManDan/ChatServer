import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Daniel on 3/7/2016.
 */
public class Message implements Comparator<Message>, Comparable<Message>, Serializable{
    private UserId sender;
    private Date timestamp;
    private String message;

    //Constructor
    Message(UserId sender, Date timestamp, String message) {
        this.sender = sender;
        this.timestamp = timestamp;
        this.message = message;
    }

    public void newMessage(ChatMessage cMsg) {

    }

    public UserId getSender() {
        return sender;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int compareTo(Message m) {
        return timestamp.compareTo(m.getTimestamp());
    }

    @Override
    public int compare(Message m1, Message m2) {
        if(m1.getTimestamp().after(m2.getTimestamp()))
            return 1;
        else if(m1.getTimestamp().equals(m2.getTimestamp()))
            return 0;
        else
            return -1;

    }
}
