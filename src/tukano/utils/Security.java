package tukano.utils;

import java.security.MessageDigest;

public class Security {
    
    public Security(){}

    public boolean validateHash(String hash, String[] args) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String toHash = "";
            for (String arg : args) {
                toHash += arg;
            }
            byte[] hashBytes = digest.digest(toHash.getBytes());

            if (!hash.equals(new String(hashBytes)))
                return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean validateToken(String token, String secret) {
        if(!token.equals(secret))
            return false;

        return true;
    }
}
