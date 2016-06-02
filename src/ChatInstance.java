import java.util.ArrayList;


public class ChatInstance {
	ArrayList<String> users = new ArrayList<String>();
	
	ArrayList<String> newMessage = new ArrayList<String>();
	
	String messages = new String();
	
	ChatInstance(){
		
	}
	ChatInstance(String username, String friend ){
		users.add(username);
		users.add(friend);
	}
	
	public void addUser(String user){
		if(!user.contains(user)){
			users.add(user);
		}
	}
	
	public boolean containsUser(String user,String friend){
		System.out.println(users.toString()+",  user:"+user+",  friend:"+friend);
		return users.contains(user)&& users.contains(friend);
	}
	
	public void addMessage(String sender,String receiver, String message){
		messages=messages+sender+": "+message+"\n\n";
		boolean exists=false;
		for(int i=0;i<newMessage.size();i++){
			if(newMessage.get(i).equalsIgnoreCase(receiver)){
				exists=true;
			}
		}
		
		if(!exists){
			System.out.println("addeing new messsage "+receiver);
			newMessage.add(receiver);
		}
	}
	
	public String getMessage(){
		return messages;
	}
	
	public boolean containsNewMessage(String user){
		if( newMessage.contains(user)){
			newMessage.remove(newMessage.indexOf(user));
			return true;
		}
		return false;
	}
	
	
	

}
