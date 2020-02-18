import java.io.*;

public class Message implements java.io.Serializable{
	public String type;
	public String message;

	public Message (String type, String message){
		this.type=type;
		this.message=message;
	}
}