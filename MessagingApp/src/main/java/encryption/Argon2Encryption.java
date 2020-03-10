import java.time.Duration;
import java.time.Instant;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
//import de.mkammerer.argon2.Argon2Types;


public class Argon2Encryption {
    private static Argon2 argon2;

    public static String getArgon(String password){
        argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        // 4 iterations, 1GB Memory, 8 degrees of parallelism

        return argon2.hash(4,1024*1024,8,password);
    }

    public static Boolean verifyArgon (String hash, String password){
        return argon2.verify(hash, password);
    }

    public static void main(String[] args) {
        String password = "Hello World!";
        Instant beginHash = Instant.now();

        System.out.println(String.format("Creating hash for password '%s'.", password));
        String hash = getArgon(password);

        System.out.println(String.format("Encoded hash is '%s'.",hash));

        Instant endHash = Instant.now();
        System.out.println(String.format("Process took %f s", Duration.between(beginHash, endHash).toMillis()/1024.0));

        Instant beginVerify = Instant.now();
        System.out.println("Verifying hash...");
        boolean success = verifyArgon(hash, password);
        System.out.println(success ? "Success!" : "Failure!");

        Instant endVerify = Instant.now();

        System.out.println(String.format("Process took %f s", Duration.between(beginVerify, endVerify).toMillis()/1024.0));

    }

}
