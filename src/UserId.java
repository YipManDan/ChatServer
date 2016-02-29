import java.io.Serializable;

/**
 * Created by Daniel on 2/24/2016.
 */
public class UserId implements Serializable{
    private int userID;
    private String userName;

    UserId(int userID, String userName)
    {
        this.userID = userID;
        this.userName = userName;
    }

    int getId(){return userID;}
    String getName(){return userName;}
    @Override
    public boolean equals(Object o) {
        UserId user = (UserId)o;
        if(user.getId() == userID && user.getName().equals(userName))
            return true;
        else
            return false;
    }
}
