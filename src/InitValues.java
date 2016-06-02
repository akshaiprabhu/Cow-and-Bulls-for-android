public class InitValues {

	private String player;
	public boolean win;
	public boolean lost;
	public StringBuffer stats = new StringBuffer();
	long lastUpdate;
	private String player_init;
	public int count;

	public InitValues() {
		win = false;
		lost = false;
		count = 0;
		player = "null";
		player_init = null;
		lastUpdate = System.currentTimeMillis();
	}

	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public String getPlayer_init() {
		return player_init;
	}

	public void setPlayer_init(String player_init) {
		this.player_init = player_init;
	}
}
