public class Message implements java.io.Serializable{
	public String sender;
	public final String recipient;
	public final String type;
	public final Object message;
	private boolean system;

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
	public void setSender(String sender){
		this.sender = sender;
	}

	public Object getMsg(){
		return this.message;
	}
	public String getType(){
		return this.type;
	}
	public boolean getSystem(){
		return this.system;
	}
	public void setSystem(boolean system){
		this.system = system;
	}


}