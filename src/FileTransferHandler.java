import java.io.*;
import java.util.ArrayList;

/**
 * Created by Daniel on 2/29/2016.
 * Reference: http://www.rgagnon.com/javadetails/java-0542.html
 */
public class FileTransferHandler {

    private ArrayList<UserId> recipients;
    private ChatMessage cMsg;
    private int transferId;
    ChatServer server;

    long length;
    UserId sender;
    int bytesRead;
    int current = 0;
    FileOutputStream fos = null;
    FileInputStream fis = null;
    BufferedOutputStream bos = null;
    BufferedInputStream bis = null;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    File tempFile;


    FileTransferHandler(ChatMessage cMsg, int transferId, ObjectInputStream ois, ChatServer server) {
        this.cMsg = cMsg;
        this.transferId = transferId;
        this.recipients = cMsg.getRecipients();
        this.ois = ois;
        this.server = server;
        length = cMsg.getFileSize();
        sender = cMsg.getSender();

        getFile();
        sendRequest();

    }

    void getFile(){
        //This should accept the file into a temporary file
        server.event("Starting file transfer to server");
        try {
            byte [] mybytearray = new byte [((int) length)];
            tempFile = File.createTempFile("tmp", null, null);
            tempFile.deleteOnExit();
            fos = new FileOutputStream(tempFile);
            bos = new BufferedOutputStream(fos);
            try{
                mybytearray = (byte []) ois.readObject();
            }
            catch (ClassNotFoundException e){
                server.event("In reading file object:" + e.getMessage());
            }
            current = bytesRead;
            current = (int)length;

            bos.write(mybytearray, 0, current);
            bos.flush();

        }
        catch (IOException e){
            server.event("Error in receiving file" + e.getMessage());
        }
        finally {
            try {
                if (fos != null)
                    fos.close();
                if(bos != null)
                    bos.close();
            }
            catch (IOException e){
                server.event("Error closing file streams" + e.getMessage());
            }
        }
        server.event("Obtained temporary file: " + tempFile.getAbsolutePath());

    }


    private void writeFile(){
        //parent.fileNotification(fc.getSelectedFile().length(), fc.getSelectedFile().getName());
        if(tempFile == null)
            return;
        byte [] mybytearray = new byte[(int)tempFile.length()];
        server.event("Sending File: " + tempFile.getName() + " from transaction: " + transferId);
        try {
            fis = new FileInputStream(tempFile);
            bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);
            oos.writeObject(mybytearray);
            server.event("File Sent");
        }
        catch (IOException e1) {
            server.event("File Send Error: " + e1.getMessage());
        }
        finally {
            if (bis != null) {
                try{
                    bis.close();
                }
                catch (IOException e2){
                }
            }
        }

    }


    void sendRequest(){
        server.sendFileTransfer(recipients, new ChatMessage(ChatMessage.FILE, ChatMessage.FILESEND, transferId, length, cMsg.getMessage(), sender));
        return;
    }

    int getTransferId(){
        return transferId;
    }

    ArrayList<UserId> getRecipients(){
        return recipients;
    }

    UserId getSender(){
        return sender;
    }


    void removeRecipient(UserId user){
        String username = user.getName();
        if(recipients.remove(user))
            server.event("User: " + username + " is removed from transfer: " + transferId);
        else
            server.event("Failure to remove: " + username + " from transfer: " + transferId);
    }
    void sendFile(int transferId, UserId user, ObjectOutputStream oos){
        this.oos = oos;
        if(this.transferId != transferId){
            server.event("File transferID incorrect");
            return;
        }
        ArrayList<UserId> recipient = new ArrayList<>();
        recipient.add(user);
        server.sendFileTransfer(recipient, new ChatMessage(ChatMessage.FILE, ChatMessage.FILEACCEPT, transferId, 0, "", new UserId(0, "Server")));
        writeFile();
        server.sendNull(user);
    }
    int getRecipientSize(){
        return recipients.size();
    }
}
