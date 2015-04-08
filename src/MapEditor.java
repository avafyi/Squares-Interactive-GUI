import java.awt.Point;
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
		// srow, scol, erow, ecol
	} 
	
	/**
	 * MAP EDITING
	 */
	
	public void makeRoom(int sr, int sc, int er, int ec, String floorGroup, String wallGroup) {
		
		// Create our rectangular-room dimension calculator
		MapTangle mt = new MapTangle(sr, sc, er, ec);
		// Add the floor
		addTerrain(mt.startRow, mt.startCol, mt.endRow, mt.endCol, "wood_floor", new Seed(15,6));
		// Add the walls around the room
		addWall(mt.topWall.row, mt.topWall.col, mt.topWall.end, new Wall(Wall.TOP), "walls", new Seed(5, 2));
		addWall(mt.botWall.row, mt.botWall.col, mt.botWall.end, new Wall(Wall.BOTTOM), "walls", new Seed(5, 2));
		addWall(mt.rightWall.row, mt.rightWall.col, mt.rightWall.end, new Wall(Wall.RIGHT), "walls", new Seed(5, 2));
		addWall(mt.leftWall.row, mt.leftWall.col, mt.leftWall.end, new Wall(Wall.LEFT), "walls", new Seed(5, 2));
		// Add corners to the room
		addCorner(mt.topRightcorner.row, mt.topRightcorner.col, new Corner(Corner.TOP_RIGHT), new CornerSize(CornerSize.LARGE), "walls");
		addCorner(mt.topLeftcorner.row, mt.topLeftcorner.col, new Corner(Corner.TOP_LEFT), new CornerSize(CornerSize.LARGE), "walls");
		addCorner(mt.botLeftcorner.row, mt.botLeftcorner.col, new Corner(Corner.BOT_LEFT), new CornerSize(CornerSize.SMALL), "walls");
		addCorner(mt.botRightcorner.row, mt.botRightcorner.col, new Corner(Corner.BOT_RIGHT), new CornerSize(CornerSize.SMALL), "walls");
		// Add shadows lines to the room
		//addShadow()
		// Add shadow corners to the room
		//addShadowCorner()
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
	
	public void addShadow() {
		// TODO needs to be implemented
		generateShading();
	}
	
	public void addShadowCorner() {
		// TODO needs to be implemented
		generateShadingCorner();
	}
	
	public void setMapSquareTypes(String[] solids) {
		super.setMapSquareTypes(solids);		
	}
	
	public static class MapTangle {
		public final int startRow;
		public final int startCol;
		public final int endRow;
		public final int endCol;
		
		// TODO modify these so we can do mapTangle.corner.topLeft and mapTangle.corner.botRight to access the row and col values
		public final Corner topLeftcorner;
		public final Corner topRightcorner;
		public final Corner botLeftcorner;
		public final Corner botRightcorner;
		
		// TODO same modification needed as with corner
		public final ShadowCorner topLeftShadowCorner;

		// TODO same modification needed as with corner
		public final Wall topWall;
		public final Wall botWall;
		public final Wall rightWall;
		public final Wall leftWall;

		// TODO same modification needed as with corner
		public final Shadow topShadow;
		public final Shadow leftShadow;
				
		public class Corner {
			public final int row;
			public final int col;
			public Corner(int row, int col) {
				this.row = row;
				this.col = col;
			}
		}
		public class Wall {
			public final int row;
			public final int col;
			public final int end;
			
			public Wall(int row, int col, int length) {
				this.row = row;
				this.col = col;
				this.end = length;
			}
		}
		
		public final class Shadow extends Wall{

			public Shadow(int row, int col, int length) {
				super(row, col, length);				
			}			
		}
		
		public final class ShadowCorner extends Corner{

			public ShadowCorner(int row, int col) {
				super(row, col);
			}			
		}
		
		public MapTangle(int startRow, int startCol, int endRow, int endCol) {
			// Set rectangular dimensions (for the floor)
			this.startRow = startRow;
			this.startCol = startCol;
			this.endRow = endRow;
			this.endCol = endCol;
			// Set corner dimensions
			topLeftcorner = new Corner(startRow-3, startCol-1);
			topRightcorner = new Corner(startRow-3, endCol);
			botLeftcorner = new Corner(endRow, startCol-1);
			botRightcorner = new Corner(endRow, endCol);
			// Set wall dimensions			
			topWall = new Wall(startRow-3, startCol+1, endCol-1);
			botWall = new Wall(endRow+1, startCol+1, endCol-1);
			rightWall = new Wall(startRow, endCol+1, endRow-1);
			leftWall = new Wall(startRow, startCol-1, endRow-1);
			// Set shadow lines
			topShadow = new Shadow(startRow, startCol+1, endCol);
			leftShadow = new Shadow(startRow+1, startCol, endRow);
			
			// Set shadow corners
			topLeftShadowCorner = new ShadowCorner(startCol, startRow);
		}
	}
}
