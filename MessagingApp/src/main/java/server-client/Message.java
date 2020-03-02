public class Message implements java.io.Serializable{
	public final String sender;
	public final String recipient;
	public final String type;
	public final Object message;

	public Message (String sender, String recipient, String type, Object message){
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

	public Object getMsg(){
		return this.message;
	}
	public String getType(){
		return this.type;
	}

}