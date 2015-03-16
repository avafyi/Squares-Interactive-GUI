import java.awt.Dimension;
import java.awt.Point;


public class MapSquare {
	public static final int SOLID = -1;		// A playerIdx meaning it cannot be occupied by a player as it is a solid object
	public static final int EMPTY = 0;	// The default playerIdx which indicates that it can be occupied by a player
	
	boolean isOccupied;	// Whether the square is occupied or not ( if it is occupied, no player can move into it )
	int playerIdx;		// The index of the player in this square (if the index is -1 then it is a solid object and can never hold a player)
	Point point;		// A point representing the top left of the grid square
	Dimension dim;		// The dimension of the grid square
	
	public MapSquare(boolean occupied, int idx, Point p, Dimension d) {
		isOccupied = occupied;
		playerIdx = idx;
		point = p;
		dim = d;
	}
}
