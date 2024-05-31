package tukano.servers.rest;


import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Users;
import tukano.clients.ClientFactory;

public class PreConditionsShorts {

    public static Result<Void> createShort(String userId, String password) {
        Users client = ClientFactory.getUsersClient();
        Result<User> uCheck = client.getUser(userId, password);

        // Check if the user exists and if the password is correct
        if (!uCheck.isOK())
            return Result.error(uCheck.error());

        return Result.ok();
    }

}
