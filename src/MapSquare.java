import java.awt.Dimension;
import java.awt.Point;
import java.io.File;


public class MapSquare {
	
	
	public static enum SquareType {
		SOLID, 	// The square cannot be occupied by a player as it is a solid object
		EMPTY,	// The square can be occupied by a player, it is not solid
		};
	
	SquareType sqType;	// What type of square it is - determines whether it can be occupied by a player
	Point point;		// A point representing the top left of the grid square
	Dimension dim;		// The dimension of the grid square
	boolean isOccupied;	// Whether the square is occupied or not ( if it is occupied, no player can move into it )	
	int playerIdx;		// The index of the player in this square
	File textureFile;	// The texture for the square, null if no texture
	
	public MapSquare(SquareType st, Point p, Dimension d, boolean occupied, int idx, File f) {
		isOccupied = occupied;
		sqType = st;
		playerIdx = idx;
		point = p;
		dim = d;
		textureFile = f;
	}
}
