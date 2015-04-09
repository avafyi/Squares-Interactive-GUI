import java.util.ArrayList;

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
		makeRoom(3,5,14,16,"wood_floor","walls");
		
	} 
	
	/**
	 * MAP EDITING
	 */
	
	public void makeRoom(int sr, int sc, int er, int ec, String floorGroup, String wallGroup) {
		
		// Create our rectangular-room dimension calculator
		MapTangle mt = new MapTangle(sr, sc, er, ec);
		// Add the floor
		addTerrain(mt.startRow, mt.startCol, mt.endRow, mt.endCol, "wood_floor", new Seed(15,4));
		// Add the walls around the room
		addWall(mt.walls.top.row, mt.walls.top.col, mt.walls.top.end, new Wall(Wall.TOP), "walls", new Seed(5, 2));
		addWall(mt.walls.bottom.row, mt.walls.bottom.col, mt.walls.bottom.end, new Wall(Wall.BOTTOM), "walls", new Seed(5, 2));
		addWall(mt.walls.right.row, mt.walls.right.col, mt.walls.right.end, new Wall(Wall.RIGHT), "walls", new Seed(5, 2));
		addWall(mt.walls.left.row, mt.walls.left.col, mt.walls.left.end, new Wall(Wall.LEFT), "walls", new Seed(5, 2));
		// Add corners to the room
		addCorner(mt.corners.topRight.row, mt.corners.topRight.col, new Corner(Corner.TOP_RIGHT), new CornerSize(CornerSize.LARGE), "walls");
		addCorner(mt.corners.topLeft.row, mt.corners.topLeft.col, new Corner(Corner.TOP_LEFT), new CornerSize(CornerSize.LARGE), "walls");
		addCorner(mt.corners.botLeft.row, mt.corners.botLeft.col, new Corner(Corner.BOT_LEFT), new CornerSize(CornerSize.SMALL), "walls");
		addCorner(mt.corners.botRight.row, mt.corners.botRight.col, new Corner(Corner.BOT_RIGHT), new CornerSize(CornerSize.SMALL), "walls");
		// Add shadows lines to the room
		addWallShadow(mt.wallShadows.top.row, mt.wallShadows.top.col, mt.wallShadows.top.end, new WallShadow(WallShadow.TOP), "shadows");
		addWallShadow(mt.wallShadows.left.row, mt.wallShadows.left.col, mt.wallShadows.left.end, new WallShadow(WallShadow.LEFT), "shadows");
		// Add shadow corners to the room
		addCornerShadow(mt.cornerShadows.topLeft.row, mt.cornerShadows.topLeft.col, new CornerShadow(CornerShadow.TOP_LEFT), "shadows");
		// Set which textures are considered to be SOLID map squares
		setSolids(new String[]{"walls"}, new String[]{"corner-q2-small.png"});
	}
	
	public ArrayList<String> getAvailableTextureGroups() {
		return new ArrayList<String>(super.textures.keySet());	
	}
	
	public void addTerrain(int startRow, int startCol, int endRow, int endCol, String terrainType, Seed seed) {
		addLevelTerrain(this.map, new MapLayer(MapLayer.TERRAIN), startRow, startCol, endRow, endCol, terrainType, seed);
	}
	
	public void addWall(int row, int col, int end, Wall wallType, String wallGroup, Seed wallSeed) {
		generateWall(this.map, new MapLayer(MapLayer.WALL), wallType, wallGroup, wallSeed, row, col, end);
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
	
	public static class MapTangle {
		// Dimensions of the rectangle floor
		public final int startRow;
		public final int startCol;
		public final int endRow;
		public final int endCol;
		// Stores different wall type dimensions
		public final Wall walls;		
		// Stores different wall shadow type dimensions
		public final WallShadow wallShadows;
		// Stores different corner type dimensions
		public final Corner corners;		
		// Stores different corner shadow type dimensions
		public final CornerShadow cornerShadows;
				
		/**
		 * Structs
		 *
		 */
		public class WallStruct {
			public final int row;
			public final int col;
			public final int end;
			
			public WallStruct(int row, int col, int length) {
				this.row = row;
				this.col = col;
				this.end = length;
			}
		}
		
		public final class ShadowWallStruct extends WallStruct {
			public ShadowWallStruct(int row, int col, int length) {
				super(row, col, length);				
			}			
		}
		
		public class CornerStruct {					
			public final int row;
			public final int col;
			public CornerStruct(int row, int col) {
				this.row = row;
				this.col = col;
			}
		}
		
		public final class ShadowCornerStruct extends CornerStruct {
			public ShadowCornerStruct(int row, int col) {
				super(row, col);
			}			
		}
		
		/**
		 * Map-Feature Group Classes
		 *
		 */
		public class Corner {
			public final CornerStruct topLeft;
			public final CornerStruct topRight;
			public final CornerStruct botLeft;
			public final CornerStruct botRight;
			
			public Corner() {
				// Set corner dimensions
				topLeft = new CornerStruct(startRow-3, startCol-1);
				topRight = new CornerStruct(startRow-3, endCol);
				botLeft = new CornerStruct(endRow, startCol-1);
				botRight = new CornerStruct(endRow, endCol);
			}
		}
		public class Wall {	
			public final WallStruct left;
			public final WallStruct right;
			public final WallStruct top;
			public final WallStruct bottom;
			
			public Wall() {
				// Set wall dimensions			
				top = new WallStruct(startRow-3, startCol+1, endCol-1);
				bottom = new WallStruct(endRow+1, startCol+1, endCol-1);
				right = new WallStruct(startRow, endCol+1, endRow-1);
				left = new WallStruct(startRow, startCol-1, endRow-1);
			}
		}
		
		public final class WallShadow {
			
			public final ShadowWallStruct left;
//			public final ShadowWallStruct right;
			public final ShadowWallStruct top;
//			public final ShadowWallStruct bottom;
			
			public WallShadow() {
				// Set wall shadow dimensions		
				top = new ShadowWallStruct(startRow, startCol+1, endCol);
				left = new ShadowWallStruct(startRow+1, startCol, endRow);		
			}			
		}
		
		public final class CornerShadow {
			public final ShadowCornerStruct topLeft;
//			public final ShadowCornerStruct topRight;
//			public final ShadowCornerStruct botLeft;
//			public final ShadowCornerStruct botRight;
			
			public CornerShadow() {
				// Set corner shadow dimensions		
				topLeft = new ShadowCornerStruct(startRow, startCol);
			}			
		}
		
		public MapTangle(int startRow, int startCol, int endRow, int endCol) {
			// Set rectangular dimensions (for the floor)
			this.startRow = startRow;
			this.startCol = startCol;
			this.endRow = endRow;
			this.endCol = endCol;			
			// Set corner dimensions
			corners = new Corner();
			// Set wall dimensions			
			walls = new Wall();
			// Set wall shadows		
			wallShadows = new WallShadow();
			// Set corner shadows
			cornerShadows = new CornerShadow();
		}
	}
}
