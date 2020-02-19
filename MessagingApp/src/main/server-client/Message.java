import java.io.*;

public class Message implements java.io.Serializable{
	public String sender;
	public String recipient;
	public String type;
	public String message;

	public Message (String sender, String recipient, String type, String message){
		this.sender=sender;
		this.recipient=recipient;
		this.type=type;
		this.message=message;
	}
	public String getSnd(){
		return this.sender;
	}
	public String getRec(){
		return this.recipient;
	}

	public String getMsg(){
		return this.message;
	}
	public String getType(){
		return this.type;
	}

}