
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Helper;

import java.time.Duration;
import java.time.Instant;
//import de.mkammerer.argon2.Argon2Types;


public class Argon2Encryption {
    // Creates an instance of Argon2 with the Argon2id version selected.
    static final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    /*
     The parameters here are placeholder values used for debugging, and should be changed to other values
     depending on what computer the server is hosted on.
     */

    // Returns the hash of the password
    public static String getArgon(String password){
        return argon2.hash(9,1024*256,4, password);
    }

    //Checks the hash and returns a boolean.
    public static boolean verifyArgon(String hash, String password){
        return argon2.verify(hash, password);
    }

}
