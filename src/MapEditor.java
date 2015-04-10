import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * 
 * 
 * @author Caleb Piekstra
 *
 */

public class MapEditor extends Map {	

	public MapEditor(ResourceLoader resLoader, int map_id, int map_width, int map_height, int num_layers, int squareDim) {
		// Create the map
		super(resLoader, map_id, map_width, map_height, num_layers, squareDim);		
		
		// TEMPORARY - these should be called by the maker of the map editor, not by the map editor
		// BETTER - these should be read in from a file, like an xml with attributes defining the creation
		// 		of each part of the map
//		makeRoom(3,5,14,16,"wood_floor","walls", "shadows");
//		makeOutside(0,0,19,19, "grass","","", new SquintMainWindow.TerrainAnimator());
//		makeRoom(3,5,14,16,"wood_floor","walls_dmg", "shadows");		
	} 
	
	/**
	 * MAP EDITING
	 */
	
	public void makeRoom(int sr, int sc, int er, int ec, String floorGroup, String wallGroup, String shadowGroup) {
		
		// Create our rectangular-room dimension calculator
		MapTangle mt = new MapTangle(sr, sc, er, ec);
		// Add the floor
		addTerrain(mt.startRow, mt.startCol, mt.endRow, mt.endCol, floorGroup, new Seed(15,4));
		// Add the walls around the room
		addWall(mt.walls.top.row, mt.walls.top.col, mt.walls.top.end, new Wall(Wall.TOP), wallGroup, new Ratio(5, 2));
		addWall(mt.walls.bottom.row, mt.walls.bottom.col, mt.walls.bottom.end, new Wall(Wall.BOTTOM), wallGroup, new Ratio(5, 2));
		addWall(mt.walls.right.row, mt.walls.right.col, mt.walls.right.end, new Wall(Wall.RIGHT), wallGroup, new Ratio(5, 2));
		addWall(mt.walls.left.row, mt.walls.left.col, mt.walls.left.end, new Wall(Wall.LEFT), wallGroup, new Ratio(5, 2));
		// Add corners to the room
		addCorner(mt.corners.topRight.row, mt.corners.topRight.col, new Corner(Corner.TOP_RIGHT), new CornerSize(CornerSize.LARGE), wallGroup);
		addCorner(mt.corners.topLeft.row, mt.corners.topLeft.col, new Corner(Corner.TOP_LEFT), new CornerSize(CornerSize.LARGE), wallGroup);
		addCorner(mt.corners.botLeft.row, mt.corners.botLeft.col, new Corner(Corner.BOT_LEFT), new CornerSize(CornerSize.SMALL), wallGroup);
		addCorner(mt.corners.botRight.row, mt.corners.botRight.col, new Corner(Corner.BOT_RIGHT), new CornerSize(CornerSize.SMALL), wallGroup);
		// Add shadows lines to the room
		addWallShadow(mt.wallShadows.top.row, mt.wallShadows.top.col, mt.wallShadows.top.end, new WallShadow(WallShadow.TOP), shadowGroup);
		addWallShadow(mt.wallShadows.left.row, mt.wallShadows.left.col, mt.wallShadows.left.end, new WallShadow(WallShadow.LEFT), shadowGroup);
		// Add shadow corners to the room
		addCornerShadow(mt.cornerShadows.topLeft.row, mt.cornerShadows.topLeft.col, new CornerShadow(CornerShadow.TOP_LEFT), shadowGroup);
		// Set which textures are considered to be SOLID map squares
		setSolids(new String[]{"walls"}, new String[]{"corner-q2-small.png"});		
	}
	
	public void makeOutside(int sr, int sc, int er, int ec, String terrainGroup, String animatedTerrainGroup, int terrainAnimationDelay, String houseGroup, String fenceGroup, Callable<?> callableAnimator) {
		// Add the floor
		addTerrain(sr, sc, er, ec, terrainGroup, new Seed(1,0));	
		addAnimatedTerrain(10, 10, 15, 15, animatedTerrainGroup, terrainAnimationDelay, callableAnimator);
		// This must be done AFTER adding ALL animated textures
		setAnimatedSquares();
		// Set which textures are considered to be SOLID map squares
		setSolids(new String[]{"water"}, new String[]{""});	
	}
	
	public void setAnimatedSquares() {
		ArrayList<MapSquare> animatedSquares = new ArrayList<MapSquare>();
		for (int row = 0, numRows = map.squares.length; row < numRows; ++row) {
			for (int col = 0, numCols = map.squares[0].length; col < numCols; ++col) {					
				if (map.squares[row][col].isAnimated) {
					animatedSquares.add(map.squares[row][col]);
				}
			}
		}
		map.animatedSquares = new MapSquare[animatedSquares.size()];
		map.animatedSquares = animatedSquares.toArray(map.animatedSquares);				
	}
	
	public ArrayList<String> getAvailableTextureGroups() {
		return new ArrayList<String>(super.textures.keySet());	
	}
	
	public void addTerrain(int startRow, int startCol, int endRow, int endCol, String terrainType, Seed seed) {
		generateTerrain(this.map, new MapLayer(MapLayer.TERRAIN), startRow, startCol, endRow, endCol, terrainType, seed);
	}
	
	public void addAnimatedTerrain(int startRow, int startCol, int endRow, int endCol, String terrainType, int animationDelay, Callable<?> callableAnimator) {
		generateAnimatedTerrain(this.map, new MapLayer(MapLayer.TERRAIN), startRow, startCol, endRow, endCol, terrainType, animationDelay, callableAnimator);
	}
	
	public void addWall(int row, int col, int end, Wall wallType, String wallGroup, Ratio wallRatio) {
		generateWall(this.map, new MapLayer(MapLayer.WALL), wallType, wallGroup, wallRatio, row, col, end);
	}
	
	public void addCorner(int row, int col, Corner cornerType, CornerSize cornerSize, String cornerGroup) {
		generateCorner(this.map, new MapLayer(MapLayer.WALL), cornerType, cornerSize, cornerGroup, row, col);		
	}
	
	public void addObject() {
		generateObject();
	}
	
	public void addWallShadow(int row, int col, int end, WallShadow wallShadowType, String shadowGroup) {
		generateWallShading(this.map, new MapLayer(MapLayer.SHADOW), wallShadowType, shadowGroup, row, col, end);
	}
	
	public void addCornerShadow(int row, int col, CornerShadow cornerShadowType, String shadowGroup) {
		generateCornerShading(this.map, new MapLayer(MapLayer.SHADOW), cornerShadowType, shadowGroup, row, col);
	}
	
	public void setSolids(String[] solids, String[] exceptions) {
		setMapSquareTypes(this.map, solids, exceptions);		
	}	
}
