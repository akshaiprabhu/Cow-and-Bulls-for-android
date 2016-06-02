import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BullsAndCowsServer extends Thread {
	private ServerSocket serverSocket;
	private Socket server;
	static private List<String> clientList = new ArrayList<String>();;
	static private List<InitValues> initValuesList = new ArrayList<InitValues>();
	static private ArrayList<ChatInstance> chatInstanceList = new ArrayList<ChatInstance>();

	public BullsAndCowsServer(ServerSocket serverSocket, Socket server) {
		super();
		this.serverSocket = serverSocket;
		this.server = server;
	}

	public void run() {
		DataInputStream in;
		try {
			in = new DataInputStream(server.getInputStream());

			Map<String, String> map = new HashMap<>();
			String request = in.readUTF();
			System.out.println(request);
			String obj[] = request.split(",");
			// System.out.println(obj[0]);
			for (int i = 0; i < obj.length; i++) {
				obj[i] = obj[i].replaceAll("\\{", "");
				obj[i] = obj[i].replaceAll("\\}", "");
				obj[i] = obj[i].replaceAll("\"", "");
				map.put(obj[i].trim().substring(0, obj[i].trim().indexOf(":")),
						obj[i].trim().substring(obj[i].trim().indexOf(":") + 1));
			}
			DataOutputStream out = new DataOutputStream(
					server.getOutputStream());
			System.out.println(map.toString());
			System.out.println(map.get("type"));
			if (map.get("type").equals("login")) {
				String username = map.get("username");
				if (clientList.contains(username)) {
					out.writeUTF("Already exists");
				} else {
					clientList.add(username);
					out.writeUTF("Login successful");
				}
			} else if (map.get("type").equals("logout")) {
				clientList.remove(map.get("username"));
				out.writeUTF("Logout successful");
			} else if (map.get("type").equals("init")) {
				String friend = map.get("friend");
				String username = map.get("username");
				System.out.println("will check for friend" + friend);
				boolean friendExists = false;
				for (String client : clientList) {
					System.out.println("list" + client);
					for (int i = 0; i < initValuesList.size(); i++) {
						if(!initValuesList.isEmpty()){
							if(initValuesList.get(i).getPlayer().equals(username)){
								out.writeUTF("In a game");
								return;
							}
						}
					}
					if (client.equalsIgnoreCase(friend)) {
						System.out.println(" friend found init");
						InitValues init = new InitValues();
						init.setPlayer(map.get("username"));
						init.setPlayer_init((map.get("initValue")));
						initValuesList.add(init);
						ChatInstance chat = new ChatInstance(username, friend);
						chatInstanceList.add(chat);
						out.writeUTF("Init successful");
						friendExists = true;
						break;
					}
				}
				if (friendExists == false) {
					out.writeUTF("Init failed");
				}

			} else if (map.get("type").equals("quitGame")) {
				String friend = map.get("friend");
				// String guessnumber = map.get("number");
				String username = map.get("username");
				// int count = 0;
				removeInitValue(username);
				// update won in own list for friend if won
				updateFriendLost(friend);
			} else if (map.get("type").equals("update")) {
				String friend = map.get("friend");
				String guessnumber = map.get("number");
				String username = map.get("username");
				int count = 0;
				if (checkExceeded(username, friend)) {
					// removeFromChat(username);
					// removeFromChat(friend);
					out.writeUTF("Round Exceeded");
					return;
				}
				// check if opposite player won and get player count in own list
				for (InitValues game : initValuesList) {
					if (game.getPlayer().equalsIgnoreCase(username)) {
						count = game.count;
						if (game.win) {
							removeInitValue(username);
							// removeFromChat(username);
							out.writeUTF("Opposite player won");
							return;
						}
					}
				}

				// check if opposite player played(if count <mycount) update
				// list
				for (InitValues game : initValuesList) {
					if (game.getPlayer().equalsIgnoreCase(friend)) {
						String friend_num = game.getPlayer_init();
						if (game.count > count) {
							out.writeUTF("Wait for other player to play");
							return;
						}
						game.lastUpdate = System.currentTimeMillis();
						game.count++;
						String response = fourBulls(guessnumber, friend_num);
						game.stats.append(guessnumber + "\n");
						game.stats.append(response + "\n");
						if (game.lost || response.equalsIgnoreCase("WIN")) {
							// remove own in friends list if won
							removeInitValue(username);
							// removeFromChat(username);
							// update won in own list for friend if won
							updateFriendWON(friend);
						}

						out.writeUTF(response);

						break;
					}
				}
			} else if (map.get("type").equals("oppositeplayed")) {
				String friend = map.get("friend");
				String username = map.get("username");
				int userCount = 0;
				long friendTimer = 0;
				boolean exists = false;
				InitValues usernameInit = null;
				for (InitValues game : initValuesList) {
					if (game.getPlayer().equalsIgnoreCase(username)) {
						usernameInit = game;
						userCount = game.count;
						friendTimer = game.lastUpdate;
						if (game.win) {
							exists = true;
							removeInitValue(username);
							// removeFromChat(username);
							out.writeUTF("Opposite player won");
							return;
						}
						if (game.lost) {
							exists = true;
							removeInitValue(username);
							// removeFromChat(username);
							out.writeUTF("WIN by disconnect");
							return;
						}
					}
				}
				// if(exists==false) {
				// out.writeUTF("Game ended abruptly");
				// return;
				// }
				exists = false;
				for (InitValues game : initValuesList) {
					if (game.getPlayer().equalsIgnoreCase(friend)) {
						exists = true;
						int friendCount = game.count;
						if (friendCount > userCount) {
							game.lastUpdate = System.currentTimeMillis();
							if (Math.abs(friendTimer - game.lastUpdate) > 120000) {
								removeInitValue(friend);
								removeInitValue(username);
								out.writeUTF("Player Quit");
								return;
							}
							out.writeUTF("Wait for other player to play");
							return;
						} else {
							out.writeUTF(usernameInit.stats.toString());
							return;
						}
					}
				}
				// if (exists == false) {
				// out.writeUTF("Game ended abruptly");
				// return;
				// }
				out.writeUTF("not played");
			} else if (map.get("type").equals("addNewMessage")) {
				String friend = map.get("friend");
				String username = map.get("username");
				String message = map.get("message");
				boolean exists = false;
				for (ChatInstance chat : chatInstanceList) {
					if (chat.containsUser(username, friend)) {
						System.out.println("true");
						chat.addMessage(username, friend, message);
						out.writeUTF("ok");
						exists = true;
						break;
					}

				}
				if (!exists) {
					System.out.println("false");
					out.writeUTF("usernamelistnotfound");
				}
			} else if (map.get("type").equals("getMessage")) {
				String friend = map.get("friend");
				String username = map.get("username");
				// String message = map.get("message");
				boolean exists = false;
				for (ChatInstance chat : chatInstanceList) {
					if (chat.containsUser(username, friend)) {
						out.writeUTF(chat.getMessage());
						exists = true;
						break;
					}
				}
				if (!exists) {
					out.writeUTF("usernamelistnotfound");
				}
			} else if (map.get("type").equals("checkNewMessage")) {
				String friend = map.get("friend");
				String username = map.get("username");
				boolean exists = false;
				for (ChatInstance chat : chatInstanceList) {
					if (chat.containsUser(username, friend)) {
						if (chat.containsNewMessage(username)) {
							out.writeUTF("true");
						} else {
							out.writeUTF("false");
						}
						exists = true;
						return;
					}

				}
				if (!exists) {
					out.writeUTF("usernamelistnotfound");
				}
			} else {
				out.writeUTF("error in query no response found");
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void removeFromChat(String username) {
		if (chatInstanceList.isEmpty() || chatInstanceList.size() == 0) {
			return;
		}
		for (int i = 0; i < chatInstanceList.size(); i++) {
			if (chatInstanceList.get(i).users.contains(username)) {
				chatInstanceList.remove(i);
			}
			if (chatInstanceList.get(i).newMessage.isEmpty()
					|| chatInstanceList.get(i).newMessage.size() == 0) {
				return;
			}
			for (int j = 0; j < chatInstanceList.get(i).newMessage.size(); i++) {
				if (chatInstanceList.get(i).newMessage.get(j)
						.contains(username)) {
					chatInstanceList.get(i).newMessage.remove(j);
				}
			}
		}
	}

	private void updateFriendLost(String friend) {
		for (int i = 0; i < initValuesList.size(); i++) {
			if (initValuesList.get(i).getPlayer().equalsIgnoreCase(friend)) {
				initValuesList.get(i).lost = true;
			}
		}
	}

	private boolean checkExceeded(String username, String friend) {
		int count1 = 0;
		int count2 = 0;
		for (int i = 0; i < initValuesList.size(); i++) {
			if (initValuesList.get(i).getPlayer().equalsIgnoreCase(friend)) {
				count1 = initValuesList.get(i).count;
			}
			if (initValuesList.get(i).getPlayer().equalsIgnoreCase(username)) {
				count2 = initValuesList.get(i).count;
			}
		}
		if (count1 == count2) {
			if (count1 == 14) {
				return true;
			}
		}
		return false;
	}

	public void updateFriendWON(String friend) {
		for (int i = 0; i < initValuesList.size(); i++) {
			if (initValuesList.get(i).getPlayer().equalsIgnoreCase(friend)) {
				initValuesList.get(i).win = true;
			}
		}
	}

	public void removeInitValue(String usernmame) {
		for (int i = 0; i < initValuesList.size(); i++) {
			if (initValuesList.get(i).getPlayer().equalsIgnoreCase(usernmame)) {
				initValuesList.remove(i);
			}
		}

	}

	private String fourBulls(String my_guess_temp, String guess_temp) {
		int cows = 0, bulls = 0;
		for (int i = 0; i < 4; i++) {
			if (my_guess_temp.contains("" + guess_temp.charAt(i))
					&& my_guess_temp.indexOf("" + guess_temp.charAt(i)) == guess_temp
							.indexOf("" + guess_temp.charAt(i))) {
				++bulls;
			} else if (my_guess_temp.contains("" + guess_temp.charAt(i))
					&& my_guess_temp.indexOf("" + guess_temp.charAt(i)) != guess_temp
							.indexOf("" + guess_temp.charAt(i))) {
				++cows;
			}
		}
		if (bulls == 4) {
			return "WIN";
		}

		return (bulls + " Bulls and " + cows + " Cows...");

	}

	public static void main(String args[]) {
		try {
			ServerSocket serverSocket = new ServerSocket(9998);
			System.out.println("Server started");
			while (true) {
				Socket server = serverSocket.accept();
				System.out.println("new client request");
				BullsAndCowsServer bcs = new BullsAndCowsServer(serverSocket,
						server);
				bcs.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
