import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Map {
	
	public static final String IMG_EXT = ".png";	// The default image file extension
	
	// Map Constants
	public static final int IN = 0;		// a level
	public static final int OUT = 1;	// a level
	public static final int MAP_LAYERS = 4;	// max number of images needed in a layer
	
	// Map Attributes
	public static int mapId;
	public static int mapWidth;		// In textures squares
	public static int mapHeight;	// In textures squares
	public static int mapLayers;	// Number of layers of the textures (Image layers)
	public static int mapSquareDim;	// Size of a map square (in pixels)
	public static int mapRows;		// The number of logical rows in the map (mapHeight / mapSquareDim)
	public static int mapCols;		// The number of logical columns in the map (mapWidth / mapSquareDim)

	// Map 
	private BufferedImage mapStaticBackground = null;	// The static background for the textures (so the entire level isn't constantly repainted)
	private String[][][] mapTextureURLs = null;			// TODO - once the background and squares are initialized, this is useless
	private Point[][] roomSquaresCoords = null;			// TODO - once the background and squares are initialized, this is useless
	private MapSquare[][] mapSquares = null;	// The logical textures (used to track players, objects, and collisions)
		
	// Wall Orientations
	public static enum WallOrientation { LEFT, RIGHT, TOP, BOTTOM };
	public static enum Corner { Q1, Q2, Q3, Q4 };	// The quadrant (2d plane) of the "empty" portion of the wall
//	public static String 
	
	// Floor Variables
//	public static enum FloorType { wood, pave, grass};
	public static String[] floorPaths = new String[] {"in_floor/", "out_pave/", "out_grass/"};
//	public static Hashtable<FloorType, String> floorHash = new Hashtable<FloorType, String>();
	
	// Map Layer Variables
	// These layers are not strict, so a wall could be added to a shadow layer for example
	private static class MAP_LAYER { 
		public static final int TRANSPARENT = 0;
		public static final int FLOOR = 1;
		public static final int SHADOW = 2;
		public static final int WALL = 3;
	} 
	// Used to store a ratio of normal to unique (the first half of a texture group is considered the normal half)
	public final class Seed {
		public double normalPercentage;
		public double uniquePercentage;
		public Seed(double normal, double unique) {
			double total = normal+unique;
			normalPercentage = normal/total;
			uniquePercentage = unique/total;
		}
	}
	
	
	// Map Textures
	public static HashMap<String, TextureGroup> textures = null;
	
	// Resources
	public ResourceLoader resources = null;
	

	// A map array (just a 3d string)
	public final class MapArray {
		public final Texture[][][] textures;	// Holds textures for all layers and squares of the map
		public final Point[][] coords;	// Holds the pixel coordinates for all map squares
		
		public MapArray(Texture[][][] map, Point[][] p) {
			this.textures = map;
			this.coords = p;
		}
	}
	
	// temp TODO
	public static void main(String[] args) 
	{		
		ResourceLoader resLoad = new ResourceLoader();
		new Map(resLoad, 0, 640, 640, 4, 40);
	}	
	
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
		
		
		// Generate the textures grid
//		mapTextureURLs = new String[mapLayers][mapHeight][mapWidth];	// Create a textures with multiple layers and a [row][col] orientation
		// Load all files with a '.png' extension that aren't in the 'avatars' group
		textures = new HashMap<String, TextureGroup>();
		loadFiles(".png", new String[] {"avatars"});	
		// Load the file groups from our file loader into our hashtable of texture groups
		// exclude groups we do not want
		map = generateBlankMap(mapLayers, mapCols, mapRows);		
		addTransparentLayer(map, MAP_LAYER.TRANSPARENT);
		addLevelTerrain(map, MAP_LAYER.FLOOR, 3, 3, 12, 12, "wood_floor", new Seed(10,6));
//		addWalls();
//		addObjects();
//		addShadows();	
		
	}
	
	private void loadFiles(String fileType, String[] excludedGroups) {
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
				if (dir.contains(excludedGroup));
			}
			String group = GlobalHelper.getLastBitFromUrl(dir);
			TextureGroup tg = new TextureGroup(resources.getAllFilesInDirWithExten(group, fileType), group);
			if (tg.textures != null) {
				textures.put(group, tg);
				textureCount++;
			}			
		}
		System.out.println(textureCount + " texture groups loaded.");
	}
	
	private MapArray generateBlankMap(int layers, int rows, int cols) {		
		Point[][] coords = new Point[rows][cols];
		for (int row = 0, rowPoint = 0; row < rows; row++, rowPoint += mapSquareDim) {
			for (int col = 0, colPoint = 0; col < cols; cols++, colPoint += mapSquareDim ) {
				coords[row][col] = new Point(colPoint, rowPoint);
			}
		}
		return new MapArray(new Texture[layers][rows][cols], coords);
	}
	
	private void addTransparentLayer(MapArray map, int layer) {
		Texture transparentTexture = null;
		for (Texture t : textures.get("misc").textures) {
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
	
	// This can apply for both wood flooring or pavement or grass as supported by available textures
	// Return a generated array? Or merge with a parameter-provided array?
	// 4 1 5 5
	// if randSeed == 0 			all normal
	// if randSeed >= 1 && <= 10 	normal with unique
	private void addLevelTerrain(MapArray map, int mapLayer, int startRow, int startCol, int endRow, int endCol, String terrainType, Seed terrainSeed) {
		// add in some code so that we get certain floor images based on the randSeed variable
		// use Math.random with some form of scaling so that if randSeed is say, 10 then we have a huge chance
		// of getting unique boards and a much lower change of the normal, clean variations
		Texture[][] layerTextures = map.textures[mapLayer];
//		ArrayList<File> flooring = textures.get(terrainType);
//		String floorDir = floorHash.get(terrainType);
		TextureGroup tg = textures.get(terrainType);
		for (int row = startRow; row < endRow; row++) {
			for (int col = startCol; col < endCol; col++) {
				double random = Math.random();
				Random r = new Random();
				int textureIdx = -1;
				// get a random texture index
				if (random <= terrainSeed.normalPercentage) {
					textureIdx = (int)(r.nextInt(tg.textures.length/2));
				} else {
					textureIdx = (int)(r.nextInt(tg.textures.length - tg.textures.length/2) + tg.textures.length/2);
				}
				layerTextures[row][col] = tg.textures[textureIdx]; 
			}
		}
	}
	
	/**
	 * 
	 * @param row			The logical starting row coordinate on the level (textures)
	 * @param col			The logical starting col coordinate on the level (textures)
	 * @param orientation	Which wall type, also used to check start, end coordinates
	 * @param len			The length of the wall, if the Corners are not null, they are included in the total len
	 * @param layer			The textures layer to place the wall
	 * @param lCorner		The corner on the left/top part of the wall (If null, no corner added)
	 * @param rCorner		The corner on the right/bottom part of the wall (If null, no corner added)
	 */
	private void generateWall(int row, int col, WallOrientation orientation, int len, int layer, Corner lCorner, Corner rCorner) {
		switch (orientation) {
			case LEFT:				
				break;
			case RIGHT:
				break;
			case TOP:
				break;
			case BOTTOM:
				break;
			default:
				// Not a valid orientation
				break;
		}
	}
	
	// generateLevel(MAP_LAYERS, NUM_SQUARES_DOWN, NUM_SQUARES_ACROSS);
	private void generateLevel(int layers, int down, int across) {		
//		String[][][] mapImages = new String[layers][down][across];
//		String dir = null;
//		if (LEVEL == IN) {
//			dir = "in/";
//		} else if (LEVEL == OUT) {
//			dir = "out/";
//		}
//		
//		// Need to make sure that SOLIDS includes "trans"
//		// Also need to modify the solids checking mapSquare generator
//		//	Have to ignore a SOLID layer if there is a NON-SOLID layer on top of it 
//		//	Like if mapImages[1][0][0] is "in_floor/4.png" but mapImages[0][0][0] will have "transparent.png"
//		// Also need to modify image resource file system to have two seperate folders, in and out with subdivisions like wall, floor, fence, pave
//		// Create a transparent layer so that any area not textured will block movement
//		for (int row = 0; row < down; row++) {
//			for (int col = 0; col < across; col++) {	
//				mapImages[0][row][col] = "transparent.png";
//			}
//		}	
	}

	
//	private void initRoom() {
//		roomSquaresCoords = new Point[NUM_SQUARES_DOWN][NUM_SQUARES_ACROSS]; 	
//		for (int row = 0; row < NUM_SQUARES_DOWN; row++) {
//			for (int col = 0; col < NUM_SQUARES_ACROSS; col++) {	
//				roomSquaresCoords[row][col] = new Point(col * MAP_DIM, row * MAP_DIM);
//			}
//		}		
//		
//		// Standard wall, floor, wall row
//		final String[] wfw_row = new String[] {
//			"transparent.png", "transparent.png", "in_walls/12.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_walls/13.png", "transparent.png", "transparent.png"
//		};
//		// Wall, special floor, wall row
//		final String[] wsfw1_row = new String[] {
//			"transparent.png", "transparent.png", "in_walls/12.png", "in_floor/5.png", "in_floor/1.png", "in_floor/5.png", "in_floor/0.png", "in_floor/4.png", "in_floor/5.png", "in_floor/2.png", "in_floor/5.png", "in_floor/3.png", "in_floor/5.png", "in_walls/13.png", "transparent.png", "transparent.png"
//		};
//		// Wall, special floor, wall row
//		final String[] wsfw2_row = new String[] {
//			"transparent.png", "transparent.png", "in_walls/12.png", "in_floor/1.png", "in_floor/4.png", "in_floor/1.png", "in_floor/3.png", "in_floor/2.png", "in_floor/4.png", "in_floor/2.png", "in_floor/3.png", "in_floor/5.png", "in_floor/2.png", "in_walls/13.png", "transparent.png", "transparent.png"
//		};
//		// Wall, special floor, wall row
//		final String[] wsfw3_row = new String[] {
//			"transparent.png", "transparent.png", "in_walls/12.png", "in_floor/1.png", "in_floor/4.png", "in_floor/4.png", "in_floor/3.png", "in_floor/2.png", "in_floor/4.png", "in_floor/5.png", "in_floor/3.png", "in_floor/5.png", "in_floor/2.png", "in_walls/13.png", "transparent.png", "transparent.png"
//		};
//		final String[] left_shade = new String[] {
//			"transparent.png", "transparent.png", "", "in_shadows/6.png", "", "", "", "", "", "", "", "", "", "", "", "transparent.png", "transparent.png"
//		};
//		final String[] empty_row = new String[] {
//			"transparent.png", "transparent.png", "", "", "", "", "", "", "", "", "", "", "", "", "transparent.png", "transparent.png"	
//		};
//		final String[] top_row = new String[] {
//			"transparent.png", "transparent.png", "in_walls/0.png", "", "in_walls/3.png", "in_walls/1.png", "in_walls/1.png", "in_walls/1.png", "in_walls/1.png", "in_walls/1.png", "in_walls/1.png", "in_walls/1.png", "in_walls/4.png", "", "transparent.png", "transparent.png"	
//		};
//		final String[] transparent_row = new String[] {
//				"transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png", "transparent.png"	
//		};
//		roomSquaresImageURLs = new String[][][] {
//			{
//				empty_row,	// top row
//				empty_row,	// top row
//				empty_row,	// top row
//				wsfw2_row,	// middle row
//				wfw_row,
//				wsfw1_row,
//				wfw_row,
//				wsfw3_row,
//				wsfw1_row,
//				wsfw2_row,
//				wfw_row,
//				wsfw1_row,
//				{"transparent.png", "transparent.png", "", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/2.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "", "transparent.png", "transparent.png"}, 	// bottom row
//				{"transparent.png", "transparent.png", "", "", "", "", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "", "", "", "", "", "transparent.png", "transparent.png"},	// bottom row (doorway flooring)
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//			},
//			{
//				top_row,	// top row
//				empty_row,	// top row
//				empty_row,	// top row
//				{"transparent.png", "transparent.png", "", "in_shadows/0.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "", "transparent.png", "transparent.png"},
//				left_shade,	// middle row
//				left_shade,	// middle row
//				left_shade,	// middle row
//				left_shade,	// middle row
//				left_shade,	// middle row
//				left_shade,	// middle row
//				left_shade,	// middle row
//				left_shade,	// middle row
//				{"transparent.png", "transparent.png", "in_walls/14.png", "", "", "", "", "", "", "", "", "", "in_walls/15.png", "", "transparent.png", "transparent.png"},	// bottom row
//				{"transparent.png", "transparent.png", "", "", "in_walls/17.png", "in_walls/18.png", "in_walls/11.png", "in_shadows/3.png", "", "in_walls/10.png", "in_walls/18.png", "in_walls/17.png", "", "", "transparent.png", "transparent.png"},	// bottom row
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//			},
//			{
//				empty_row,	// top row
//				empty_row,	// top row
//				empty_row,	// top row
//				empty_row,	// middle row
//				empty_row,	// middle row
//				empty_row,	// middle row
//				empty_row,	// middle row
//				empty_row,	// middle row
//				empty_row,	// middle row
//				empty_row,	// middle row
//				empty_row,	// middle row
//				empty_row,	// middle row
//				{"transparent.png", "transparent.png", "", "in_shadows/8.png", "", "", "", "", "", "", "", "", "", "", "", "transparent.png", "transparent.png"},	// bottom row
//				empty_row,	// bottom row
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//				transparent_row,
//			}
//		};
//		mapSquares = new MapSquare[NUM_SQUARES_DOWN][NUM_SQUARES_ACROSS];
//		for (int row = 0; row < NUM_SQUARES_DOWN; row++) {
//			for (int col = 0; col < NUM_SQUARES_ACROSS; col++) {	
//				// Check if any of the layers contains a wall, if so then create a MapSquare that cannot be occupied by a player
//				// If any square in the first layer has an imageURL of "" then that means that it is a part of a larger object
//				
//				// If we run across a textures square that has already been initialized (like in the case of a wall texture that takes up
//				// more than one room square) then ignore this square and continue on to the next column
//				if (mapSquares[row][col] != null) {
//					continue;
//				}
//				findSolidsLoop:
//				for (String[][] layer : roomSquaresImageURLs) {
//					boolean isSolid = false;
//					for(String solid : SOLIDS) {
//						if (layer[row][col].contains(solid)) {	
//							isSolid = true;
//						}
//					}
//					if (isSolid) {		
//						File imageSrc = new File(IMAGES_DIR + layer[row][col]);					
//						BufferedImage bimg = null;
//						try {
//							bimg = ImageIO.read(imageSrc);
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//						if (bimg != null) {
//							int numSquaresWide = bimg.getWidth() / MAP_DIM;
//							int numSquaresTall = bimg.getHeight() / MAP_DIM;
//							for (int c = 0; c < numSquaresWide; c++) {
//								for (int r = 0; r < numSquaresTall; r++) {
//									// Because of how the bottom left corner wall, wall image 14, is textured, the top right textures square is not solid
//									// the same goes for the bottom right corner wall, wall image 15
//									if ((c == 1 && r == 0 && layer[row][col].contains("in_walls/14.png")) 
//											|| (c == 0 && r == 0 && layer[row][col].contains("in_walls/15.png"))) 
//									{
//										continue;	// Skip this textures square
//									}
//									mapSquares[row+r][col+c] = new MapSquare(true, MapSquare.SOLID, roomSquaresCoords[row][col], new Dimension(MAP_DIM, MAP_DIM));
//								}	
//							}
//						}
//						break findSolidsLoop;
//					}
//				}
//				if (mapSquares[row][col] == null) {
//					mapSquares[row][col] = new MapSquare(false, MapSquare.EMPTY, roomSquaresCoords[row][col], new Dimension(MAP_DIM, MAP_DIM));
//				}
//			}
//		}	
//		roomBackgroundImage = makeImage(roomSquaresImageURLs, roomSquaresCoords);
//	}
//	
//	public BufferedImage makeImage(String[][][] imageURLs, Point[][] coords) {
//		BufferedImage bImg = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, 
//				BufferedImage.TYPE_INT_ARGB);
//		Graphics2D g2 = bImg.createGraphics();
//
//		// background drawing here, the display that doesn't change
//		drawGrid(g2, imageURLs, coords);
//
//		g2.dispose();
//		return bImg;
//	}

}
