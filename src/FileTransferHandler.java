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
    ChatServer server;

    long length;
    UserId sender;
    int bytesRead;
    int current = 0;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    Socket socket = ct.socket;
    File tempFile;


    FileTransferHandler(ChatMessage cMsg, int transferId, ChatServer.ClientThread ct, ChatServer server) {
        this.cMsg = cMsg;
        this.transferId = transferId;
        this.recipients = cMsg.getRecipients();
        this.ct = ct;
        this.server = server;
        length = cMsg.getFileSize();
        sender = cMsg.getSender();

        getFile();
        //sendRequest();

    }

    void getFile(){
        //This should accept the file into a temporary file
        try {
            byte [] mybytearray = new byte [((int) length)];
            InputStream is = socket.getInputStream();
            tempFile = File.createTempFile("tmp", null, null);
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
            server.event("Obtained temporary file: " + tempFile.getAbsolutePath());

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
    void sendRequest(){
        server.sendFileTransfer(recipients, new ChatMessage(ChatMessage.FILE, ChatMessage.FILESEND, transferId, length, "", sender));
        return;
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
    void sendFile(int transferId){
        if(this.transferId != transferId){
            server.event("File transferID incorrect");
            return;

        }
    }
}
