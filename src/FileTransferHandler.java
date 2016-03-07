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
    FileInputStream fis = null;
    BufferedOutputStream bos = null;
    BufferedInputStream bis = null;
    //InputStream is;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    Socket socket;
    File tempFile;


    FileTransferHandler(ChatMessage cMsg, int transferId, ObjectInputStream ois, ChatServer server) {
        this.cMsg = cMsg;
        this.transferId = transferId;
        this.recipients = cMsg.getRecipients();
        //this.ct = ct;
        //socket = ct.socket;
        //this.socket = socket;
        //this.is = is;
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
            //is = socket.getInputStream();
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
            //bytesRead = is.read(mybytearray, 0, mybytearray.length);
            //current = 0;
            current = bytesRead;
            current = (int)length;
            /*
            do {
                bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
                if(bytesRead >= 0)
                    current += bytesRead;
                server.event("File progress: " + current + " of " + length);
            } while (current < length);
            //} while (is.available() > 0);
            */


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
            /*
            os = socket.getOutputStream();
            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
            */
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
            /*
            if(os != null) {
                try {
                    os.close();
                }
                catch (IOException e3){
                }
            }
            */
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
        //server.sendFileTransfer(recipient, new ChatMessage(ChatMessage.FILE, ChatMessage.FILEACCEPT, length, "", recipient, new UserId(0, "Server")));
        server.sendFileInit(recipient, new ChatMessage(ChatMessage.FILE, ChatMessage.FILEACCEPT, transferId, 0, "", new UserId(0, "Server")));
        writeFile();
        server.sendNull(user);
    }
    int getRecipientSize(){
        return recipients.size();
    }

    /*
    InputStream getIs(){
        return is;
    }
    */
}
