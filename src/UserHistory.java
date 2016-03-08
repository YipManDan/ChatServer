import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Daniel on 3/7/2016.
 */
public class UserHistory implements Serializable{
    String username;
    ArrayList<Chat> chatrooms;

    UserHistory(String username) {
        this.username = username;
        chatrooms = new ArrayList<>();
    }

    public class Chat implements Serializable{
        ArrayList<UserId> recipients;
        ArrayList<Message> messages;

        Chat(ArrayList<UserId> recipients, ArrayList<Message> messages) {
            this.recipients = recipients;
            this.messages = messages;
        }
    }

    public void newChatroom(ArrayList<UserId> recipients, ArrayList<Message> messages) {
        Chat room = new Chat(recipients, messages);
        chatrooms.add(room);
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<Chat> getChatrooms() {
        return chatrooms;
    }
}
