
/**
 * 
 * 
 * @author Caleb Piekstra
 *
 */

public class MapSquare {
	
	
	public static enum SquareType {
		SOLID, 	// The square cannot be occupied by a player as it is a solid object
		EMPTY,	// The square can be occupied by a player, it is not solid
		UNDEF,	// The square does not have a type yet
		};
	
	SquareType sqType;	// What type of square it is - determines whether it can be occupied by a player
	boolean isOccupied;	// Whether the square is occupied or not ( if it is occupied, no player can move into it )	
	boolean isAnimated;	// Whether or not the square has some form of texture animation	
	int playerIdx;		// The index of the player in this square
	int row;			// What row of the map the square is on
	int col;			// What col of the map the square is on
	
	public MapSquare(SquareType squareType, boolean occupied, int playerIdx, int row, int col) {
		sqType = squareType;
		isOccupied = occupied;
		this.playerIdx = playerIdx;
		isAnimated = false;
		this.row = row;
		this.col = col;
	}
}
