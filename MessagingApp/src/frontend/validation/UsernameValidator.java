import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class UsernameValidator {
    // Allowed parameters are: a-z, A-Z, 0-9,_,.
    // Min length: 3 characters
    // Max length: 15 characters.
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,15}$";

    private final Pattern pattern;

    public UsernameValidator(){
        pattern = Pattern.compile(USERNAME_PATTERN);
    }
    /**
     * Validate username with regular expression
     * @param username username for validation
     * @return true valid username, false invalid username
     */
    public boolean validate(final String username){
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }


}
