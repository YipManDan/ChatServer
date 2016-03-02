import java.util.ArrayList;

/**
 * Created by Daniel on 2/29/2016.
 */
public class FileTransferHandler {

    private ArrayList<UserId> recipients;
    private int transferId;

    FileTransferHandler(ArrayList<UserId> recipients, int transferId) {
        this.recipients = recipients;
        this.transferId = transferId;
    }

    int getTransferId(){
        return transferId;
    }

    ArrayList<UserId> getRecipients(){
        return recipients;
    }

    void removeRecipient(UserId user){
        recipients.remove(user);
    }
}
