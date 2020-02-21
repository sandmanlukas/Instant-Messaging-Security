import java.io.*;

/**
 * NewMessage
 */
public class NewMessage implements java.io.Serializable {

    public UserThread recipient;

    public String message;

    public Message ( UserThread recipient, String message){
		this.recipient=recipient;
		this.message=message;
	}

    public String getRec() {
        return this.recipient;
    }

    public String getMsg() {
        return this.message;
    }

}