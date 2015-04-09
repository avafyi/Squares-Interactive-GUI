import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

/**
 * 
 * 
 * @author Caleb Piekstra
 *
 */

public class Map {
	
	public static final String IMG_EXT = ".png";	// The default image file extension
	
	// Map Constants
	
	// Map Attributes
	public int mapId;
	public int mapWidth;		// In textures squares
	public int mapHeight;		// In textures squares
	public int mapLayers;		// Number of layers of the textures (Image layers)
	public int mapSquareDim;	// Size of a map square (in pixels)
	public int mapRows;			// The number of logical rows in the map (mapHeight / mapSquareDim)
	public int mapCols;			// The number of logical columns in the map (mapWidth / mapSquareDim)
		
	// Wall 
	public static class Wall { 
		public static final String LEFT = "left";
		public static final String RIGHT = "right";
		public static final String TOP = "top";
		public static final String BOTTOM = "bottom";
		public String wall;
		public Wall(String wall) {
			this.wall = wall;
		}
	}
	public static class WallShadow extends Wall { 
		public String wallShadow;
		public WallShadow(String wallShadow) {
			super(wallShadow);
			this.wallShadow = super.wall;
		}
	}
	public static class Corner { 
		public static final String BOT_LEFT = "q1";
		public static final String BOT_RIGHT = "q2";
		public static final String TOP_RIGHT = "q3";
		public static final String TOP_LEFT = "q4";
		public String corner;
		public Corner(String corner) {
			this.corner = corner;
		}
	}
	public static class CornerShadow extends Corner{ 
		public String cornerShadow;
		public CornerShadow(String cornerShadow) {
			super(cornerShadow);
			this.cornerShadow = super.corner;
		}
	}
	
	public static class CornerSize { 
		public static final String LARGE = "large";
		public static final String SMALL = "small";
		public String size;
		public CornerSize(String size) {
			this.size = size;
		}
	}
	
	// Map Layer Variables
	// These layers are not strict, so a wall could be added to a shadow layer for example
	public final class MapLayer { 
		public static final int TRANSPARENT = 0;
		public static final int TERRAIN = 1;
		public static final int SHADOW = 2;
		public static final int OBJECT = 3;
		public static final int WALL = 3;	// Same layer as object
		public int layer;
		public MapLayer(int layer) {
			this.layer = layer;
		}
	} 
	// Used to store a ratio of normal to unique (the first half of a texture group is considered the normal half)
	public final class Seed {
		public double normPercent;
		public double specPercent;
		public Seed(double normal, double special) {
			double total = normal+special;
			normPercent = normal/total;
			specPercent = special/total;
		}
	}	
	// Map Textures
	public HashMap<String, TextureGroup> textures = null;	
	// Resources
	public ResourceLoader resources = null;
	// A map array (just a 3d string)
	public final class MapArray {
		public final Texture[][][] textures;	// Holds textures for all layers and squares of the map
		public final Point[][] coords;			// Holds the pixel coordinates for all map squares
		public final MapSquare[][] squares;		// Holds the characteristics for all map squares
		
		public MapArray(Texture[][][] map, Point[][] p, MapSquare[][] ms) {
			this.textures = map;
			this.coords = p;
			this.squares = ms;
		}
	}
	// The map
	public MapArray map = null;
	
	/**
	 * Maps must be rectangular
	 * 
	 * @param map_id
	 * @param map_width		number of textures squares across
	 * @param map_height	number of textures squares down
	 * @param num_layers	how many texture layers in the map
	 */
	public Map(ResourceLoader resLoader, int map_id, int map_width, int map_height, int num_layers, int squareDim) {
		resources = resLoader;
		// Set values that will act like constants for the textures
		mapId = map_id;
		mapWidth = map_width;
		mapHeight = map_height;
		mapLayers = num_layers;
		mapSquareDim = squareDim;
		mapRows = mapHeight / mapSquareDim;
		mapCols = mapWidth / mapSquareDim;
		
		// Load all files with a '.png' extension that aren't in the 'avatars' group
		loadFiles(".png", new String[] {"avatars"});	
		// Load the file groups from our file loader into our hashtable of texture groups
		// exclude groups we do not want
		map = generateBlankMap(mapLayers, mapRows, mapCols);	
		addTransparentLayer(map, MapLayer.TRANSPARENT);
	}

	private void loadFiles(String fileType, String[] excludedGroups) {
		// Initialize the textures map
		textures = new HashMap<String, TextureGroup>();
		// Get all the directories that are not in an excluded group
		ArrayList<File> dirFiles = resources.getAllDirsExcluding(excludedGroups);
		// Create a list to hold the paths for the directories
		ArrayList<String> dirPaths = new ArrayList<String>();
		for (File file : dirFiles) {
			dirPaths.add(file.getAbsolutePath());
		}
		
		int textureCount = 0;	// Count how many textures were loaded
		// Go through every directory and create a texture group for it
		for(String dir : dirPaths) {
			// If the directory is an excluded group or a sub-directory of 
			// an excluded group, don't create a group for it
			for (String excludedGroup : excludedGroups) {
				if (dir.contains(excludedGroup)) {
					continue;
				}
			}
			String group = GlobalHelper.getLastBitFromUrl(dir);
			TextureGroup tg = new TextureGroup(resources.getAllFilesInDirWithExten(group, fileType), group);
			if (tg.textures != null) {
				textures.put(group, tg);
				textureCount++;
			}			
		}
		System.out.println("Map loaded with " + textureCount + " texture groups.");
	}
	
	private MapArray generateBlankMap(int layers, int rows, int cols) {		
		Point[][] coords = new Point[rows][cols];
		MapSquare[][] squares = new MapSquare[rows][cols];
		for (int row = 0, rowPoint = 0; row < rows; row++, rowPoint += mapSquareDim) {
			for (int col = 0, colPoint = 0; col < cols; col++, colPoint += mapSquareDim ) {
				coords[row][col] = new Point(colPoint, rowPoint);				
				squares[row][col] = new MapSquare(MapSquare.SquareType.UNDEF, false, -1);
				
			}
		}
		return new MapArray(new Texture[layers][rows][cols], coords, squares);
	}
	
	private void addTransparentLayer(MapArray map, int layer) {
		Texture transparentTexture = null;
		for (Texture t : textures.get("misc").textures.values()) {
			if (t.textureFile.getName().contains("transparent")) {
				transparentTexture = t;
				break;
			}
		}
		if (transparentTexture == null) {
			System.out.println("ERROR: No transparent file found for transparent map layer");
			return;
		}
		for (int row = 0; row < mapRows; row++) {
			for (int col = 0; col < mapCols; col++) {
				map.textures[layer][row][col] = transparentTexture;
			}
		}
	}
	
	// Sets the type of each square in the map to SOLID or EMPTY
	public void setMapSquareTypes(MapArray map, String[] solids, String[] exceptions) {
		// Used to determine if a map square should be set as solid
		boolean isSolid = false;
		// If a square only has a texture on the transparent layer, then it is also a solid
		for (int row = 0, numRows = map.squares.length; row < numRows; row++) {
			for (int col = 0, numCols = map.squares[0].length; col < numCols; col++) {
				// Check to see if there is an exception for this map square, if none then keep performing checks
				if (!checkIfException(map, row, col, exceptions)) {
					// Perform checks to see if the map square is SOLID (order matters)
					if (checkIfOnlyTransparentTexture(map, row, col) ||
							checkSquareAgainstSolids(map, row, col, solids)) {
						// Transparent-only square, unreachable
						isSolid = true;
					}
				}
				
				// Set the map square type to SOLID or EMPTY (not solid)
				map.squares[row][col].sqType = isSolid ? MapSquare.SquareType.SOLID : MapSquare.SquareType.EMPTY;
				// Reset to the default (not solid)
				isSolid = false;
			}
		}
	}
	
	/**
	 * Determine if the map square only has a transparent image among all image layers
	 * 
	 * @param map	The map (contains the map squares)
	 * @param row	The row of the map square
	 * @param col	The column of the map square
	 * @return	Whether or not the map square has just a transparent image (if yes, SOLID)
	 */
	private boolean checkIfOnlyTransparentTexture(MapArray map, int row, int col) {
		for (int layer = 0, numLayers = map.textures.length; layer < numLayers; layer++) {
			// Ignore the transparent layer
			if (layer == MapLayer.TRANSPARENT) {
				continue;
			}
			// Check to see if the layer has a texture
			if (map.textures[layer][row][col] != null) {
				// If we found a texture, then there isn't only a transparent texture so return false
				return false;
			}
		}
		// There were no other textures besides the transparent texture
		return true;
	}
	
	/**
	 * Determine if a map square contains a SOLID texture at any layer (except transparent)
	 * 
	 * @param map		The map (contains the map squares)
	 * @param row		The row of the map square
	 * @param col		The column of the map square
	 * @param solids	The list of directories that contain solid textures
	 * @return			Whether or not the map square has a SOLID texture (if yes, SOLID)
	 */
	private boolean checkSquareAgainstSolids(MapArray map, int row, int col, String[] solids) {
		for (int layer = 0, numLayers = map.textures.length; layer < numLayers; layer++) {
			// Ignore the transparent layer
			if (layer == MapLayer.TRANSPARENT) {
				continue;
			}
			// Check to see if the layer has a texture that is in the list of solids
			for (String solid : solids) {
				// Solids are determined by texture group, which is labeled by the enclosing directory's name
				if (map.textures[layer][row][col] != null && map.textures[layer][row][col].textureDir.equals(solid)) {
					// Texture is a solid
					return true;
				}
			}
		}
		// The texture at the map square is not a solid
		return false;
	}
	
	/**
	 * Determine if a map square contains a texture that would normally 
	 * be considered a SOLID but we want to make an exception
	 * 
	 * @param map			The map (contains the map squares)
	 * @param row			The row of the map square
	 * @param col			The column of the map square
	 * @param exceptions	The list of exact texture names that are exceptions (would normally be SOLID)
	 * @return				Whether or not the map square has a texture exception (if yes, ignore SOLID)
	 */
	private boolean checkIfException(MapArray map, int row, int col, String[] exceptions) {
		for (int layer = 0, numLayers = map.textures.length; layer < numLayers; layer++) {
			// Ignore the transparent layer
			if (layer == MapLayer.TRANSPARENT) {
				continue;
			}
			// Check to see if the layer has a texture that is in the list of exceptions
			for (String exception : exceptions) {
				// Solids are determined by texture group, which is labeled by the enclosing directory's name
				if (map.textures[layer][row][col] != null && map.textures[layer][row][col].textureName.equals(exception)) {
					// Texture is an exception
					return true;
				}
			}
		}
		// The texture at the map square is not an exception
		return false;		
	}
	
		
	// This can apply for both wood flooring or pavement or grass as supported by available textures
	// Return a generated array? Or merge with a parameter-provided array?
	// 4 1 5 5
	// if randSeed == 0 			all normal
	// if randSeed >= 1 && <= 10 	normal with unique
	public void addLevelTerrain(MapArray map, MapLayer mapLayer, int startRow, int startCol, int endRow, int endCol, String terrainType, Seed terrainSeed) {
		// Get the textures
		TextureGroup tg = textures.get(terrainType);
		for (int row = startRow; row <= endRow; row++) {
			for (int col = startCol; col <= endCol; col++) {
				map.textures[mapLayer.layer][row][col] = getTextureUsingSeed(terrainSeed, GlobalHelper.textureGroupToArrayList(tg));
			}
		}
	}
	
	private Texture getRandomNamedTexture(TextureGroup tg, String genericName) {
		ArrayList<Texture> textures = tg.getTexturesLike(genericName);		
		return (textures.get(new Random().nextInt(textures.size())));
	}
	
	private Texture getTextureUsingSeed(Seed seed, ArrayList<Texture> textures) {
		double random = Math.random();
		Random r = new Random();
		int textureIdx = -1;
		// Grab just the textures and sort them
		ArrayList<Texture> texturesCopy = new ArrayList<Texture>(textures);
		Collections.sort(texturesCopy, new GlobalHelper.TextureComparator());
		// Get the number of textures
		int numTextues = texturesCopy.size();
		// get a random texture index
		// TODO have it look for textures named normal and special and instead of doing a hard cutoff at numTextures/2, use
		// the counts of each type normal and special as cutoffs and make a list of their indexes or something, choose from those
		if (random <= seed.normPercent) {
			textureIdx = (int)(r.nextInt(numTextues/2));
		} else {
			textureIdx = (int)(r.nextInt(numTextues - numTextues/2) + numTextues/2);
		}
		return texturesCopy.get(textureIdx); 
	}
	
	public MapSquare getMapSquare(Point p) {
		if (p.x < 0 || p.y < 0 || p.x >= mapCols || p.y >= mapRows) {
			return null;
		}
		return map.squares[p.y][p.x];
	}
	
	/**
	 * 
	 */
	public void generateWall(MapArray map, MapLayer mapLayer, Wall wallType, String wallGroup, Seed wallSeed, int row, int col, int end) {
		// Get the textures
		TextureGroup tg = textures.get(wallGroup);
		// Get textures of the same type
		ArrayList<Texture> similarTextures = tg.getTexturesLike(wallType.wall + "-");
		
		// Check if the wall goes across a row or down a column
		if (wallType.wall == Wall.TOP || wallType.wall == Wall.BOTTOM) {
			for (int c = col; c <= end; c++) {
				// Add the wall texture
				map.textures[mapLayer.layer][row][c] = getTextureUsingSeed(wallSeed, similarTextures);
			}
		} else {
			for (int r = row; r <= end; r++) {
				// Add the wall texture
				map.textures[mapLayer.layer][r][col] = getTextureUsingSeed(wallSeed, similarTextures);
			}
		}
	}
	
	public void generateCorner(MapArray map, MapLayer mapLayer, Corner cornerType, CornerSize cornerSize, String cornerGroup, int row, int col) {
		// Get the textures
		TextureGroup tg = textures.get(cornerGroup);
		// Add the corner to the map
		map.textures[mapLayer.layer][row][col] = getCornerTexture(tg, cornerType.corner, cornerSize.size);
	}
	
	private Texture getCornerTexture(TextureGroup tg, String corner, String cornerType) {
		if (tg == null || corner == null || cornerType == null) {
			return null;
		}
		return tg.getTextureExact("corner-" + corner + "-" + cornerType + ".png");
	}
	
	private Texture getCornerShadowTexture(TextureGroup tg, String cornerType) {
		if (tg == null || cornerType == null) {
			return null;
		}
		return tg.getTextureExact("corner-" + cornerType + ".png");
	}
	
	public void generateObject() {
		
	}
	
	public void generateWallShading(MapArray map, MapLayer mapLayer, WallShadow wallShadowType, String shadowGroup, int row, int col, int end) {
		// Get the textures
		TextureGroup tg = textures.get(shadowGroup);
		
		// Check if the wall goes across a row or down a column
		if (wallShadowType.wallShadow == WallShadow.TOP || wallShadowType.wallShadow == WallShadow.BOTTOM) {
			for (int c = col; c <= end; c++) {
				// Add the wall texture
				map.textures[mapLayer.layer][row][c] = tg.getTextureExact(wallShadowType.wallShadow + ".png");
			}
		} else {
			for (int r = row; r <= end; r++) {
				// Add the wall texture
				map.textures[mapLayer.layer][r][col] = tg.getTextureExact(wallShadowType.wallShadow + ".png");
			}
		}
	}
	
	public void generateCornerShading(MapArray map, MapLayer mapLayer, CornerShadow cornerType, String shadowGroup, int row, int col) {
		// Get the textures
		TextureGroup tg = textures.get(shadowGroup);
		// Add the corner to the map
		map.textures[mapLayer.layer][row][col] = getCornerShadowTexture(tg, cornerType.cornerShadow);
	}
}
