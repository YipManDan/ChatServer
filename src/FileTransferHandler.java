import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Daniel on 2/29/2016.
 * Reference: http://www.rgagnon.com/javadetails/java-0542.html
 */
public class FileTransferHandler {

    private ArrayList<UserId> recipients;
    private ChatMessage cMsg;
    private int transferId;
    ChatServer.ClientThread ct;


    FileTransferHandler(ChatMessage cMsg, int transferId, ChatServer.ClientThread ct) {
        this.cMsg = cMsg;
        this.transferId = transferId;
        this.recipients = cMsg.getRecipients();
        this.ct = ct;
        long length = cMsg.getFileSize();
        UserId sender = cMsg.getSender();

        int bytesRead;
        int current = 0;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        Socket socket = ct.socket;

        //This should accept the file into a temporary file
        try {
            byte [] mybytearray = new byte [((int) length)];
            InputStream is = socket.getInputStream();
            File tempFile = File.createTempFile("tmp", null, null);
            fos = new FileOutputStream(tempFile);
            bos = new BufferedOutputStream(fos);
            bytesRead = is.read(mybytearray, 0, mybytearray.length);
            current = bytesRead;
            do {
                bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
                if(bytesRead >= 0)
                    current += bytesRead;
            } while (bytesRead > -1);

            bos.write(mybytearray, 0, current);
            bos.flush();
            //Print out success alert

        }
        catch (IOException e){
            //QQ
        }
        finally {
            try {
                if (fos != null)
                    fos.close();
                if(bos != null)
                    bos.close();
            }
            catch (IOException e){
                //QQ
            }
        }
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
