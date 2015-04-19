import java.awt.Point;
import java.util.HashMap;


public class PretendServer {
	
	private MapSquare[][] map = null;
	private HashMap<Integer, Player> players = null;
	
	public PretendServer(MapSquare[][] map, Player[] players) {
		this.map = map;
		// init the mapping of player id's to players
		this.players = new HashMap<Integer, Player>();
		for (Player player : players) {	
			this.players.put(player.id, player);
		}
	}
	
	public boolean lookIPressedSomethingCanIMove(int moveDirection, int playerId) {
		return isValidMove(moveDirection, playerId);
	}
	
	public boolean isValidMove(int moveDirection, int playerId) {
		// Check if the player is simply rotating in place
		if (moveDirection != players.get(playerId).direction) {
			// Player is rotating in place, they can do that all they want
			return true;
		}
		// Get the coordinates of the destination square based on the move direction
		Point destSquarePoint = getNewPlayerPosition(players.get(playerId), moveDirection);
		// Get the destination square based on the move direction
		MapSquare destSquare = map[destSquarePoint.y][destSquarePoint.x];
		// Check if the destination square is occupied or SOLID
		if (!destSquare.isOccupied && !destSquare.sqType.equals(MapSquare.SquareType.SOLID)) {
			// Update the map to indicate the new location
			updateMap(destSquare, playerId);
			// Square is available to be moved into, let the player know they can move
			return true;
		}
		return false;
	}
	
	private void updateMap(MapSquare destSquare, int playerId) {
		// First get the old map square and clear it
		Player player = players.get(playerId);
		MapSquare oldSquare = map[player.y][player.x];
		oldSquare.isOccupied = false;
		oldSquare.playerId = -1;
		destSquare.playerId = playerId;
		destSquare.isOccupied = true;
	}
	
	/**
	 * Figure out where the player would end up if they moved in
	 * a direction
	 * 
	 * @param player
	 * @param direction
	 * @return
	 */
	private Point getNewPlayerPosition(Player player, int direction){
		Point newPoint = new Point(player.x, player.y);
		switch(direction) {
			case Player.Move.RIGHT: newPoint.x++;	break;
			case Player.Move.UP:	newPoint.y--;	break;
			case Player.Move.LEFT:	newPoint.x--;	break;
			case Player.Move.DOWN:	newPoint.y++;	break;
		}
		return newPoint;
	}
}
