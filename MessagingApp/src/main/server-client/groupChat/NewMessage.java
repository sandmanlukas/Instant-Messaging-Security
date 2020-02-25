
/**
 * NewMessage
 */
public class NewMessage implements java.io.Serializable {

    public UserThread recipient;

    public String message;

    public NewMessage(UserThread recipient, String message){
		this.recipient=recipient;
		this.message=message;
	}

    public UserThread getRec() {
        return this.recipient;
    }

    public String getMsg() {
        return this.message;
    }

}