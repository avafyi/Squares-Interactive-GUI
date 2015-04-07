import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
		
	// Wall Orientations
	public static enum WallOrientation { LEFT, RIGHT, TOP, BOTTOM };
	public static enum Corner { Q1, Q2, Q3, Q4 };	// The quadrant (2d plane) of the "empty" portion of the wall
	
	// Map Layer Variables
	// These layers are not strict, so a wall could be added to a shadow layer for example
	public static class MAP_LAYER { 
		public static final int TRANSPARENT = 0;
		public static final int TERRAIN = 1;
		public static final int SHADOW = 2;
		public static final int OBJECT = 3;
		public static final int WALL = 4;
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
		map = generateBlankMap(mapLayers, mapCols, mapRows);	
		addTransparentLayer(map, MAP_LAYER.TRANSPARENT);
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
	public void setMapSquareTypes(String[] solids) {
		// If a square only has a texture on the transparent layer, then it is also a solid
	}
	
		
	// This can apply for both wood flooring or pavement or grass as supported by available textures
	// Return a generated array? Or merge with a parameter-provided array?
	// 4 1 5 5
	// if randSeed == 0 			all normal
	// if randSeed >= 1 && <= 10 	normal with unique
	public void addLevelTerrain(MapArray map, int mapLayer, int startRow, int startCol, int endRow, int endCol, String terrainType, Seed terrainSeed) {
		// Get the texture layer for modification
		Texture[][] layerTextures = map.textures[mapLayer];
		// Get the textures
		TextureGroup tg = textures.get(terrainType);
		for (int row = startRow; row < endRow; row++) {
			for (int col = startCol; col < endCol; col++) {
				layerTextures[row][col] = getTextureUsingSeed(terrainSeed, tg);
			}
		}
	}
	
	private Texture getRandomNamedTexture(TextureGroup tg, String genericName) {
		ArrayList<Texture> textures = tg.getTexturesLike(genericName);		
		return (textures.get(new Random().nextInt(textures.size())));
	}
	
	private Texture getTextureUsingSeed(Seed seed, TextureGroup tg) {
		double random = Math.random();
		Random r = new Random();
		int textureIdx = -1;
		// Grab just the textures and sort them
		ArrayList<Texture> textures = new ArrayList<Texture>(tg.textures.values());
		Collections.sort(textures, new GlobalHelper.TextureComparator());
		// Get the number of textures
		int numTextues = textures.size();
		// get a random texture index
		// TODO have it look for textures named normal and special and instead of doing a hard cutoff at numTextures/2, use
		// the counts of each type normal and special as cutoffs and make a list of their indexes or something, choose from those
		if (random <= seed.normPercent) {
			textureIdx = (int)(r.nextInt(numTextues/2));
		} else {
			textureIdx = (int)(r.nextInt(numTextues - numTextues/2) + numTextues/2);
		}
		return textures.get(textureIdx); 
	}
	
	public MapSquare getMapSquare(Point p) {
		if (p.x < 0 || p.y < 0 || p.x >= mapCols || p.y >= mapRows) {
			return null;
		}
		return map.squares[p.y][p.x];
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
	public void generateWall(MapArray map, int mapLayer, WallOrientation orientation, String wallType, Seed wallSeed, int row, int col, int length, Corner lCorner, Corner rCorner) {
		// Get the texture layer for modification
		Texture[][] layerTextures = map.textures[mapLayer];
		// Get the textures
		TextureGroup tg = textures.get(wallType);
		// Individual Textures
		Texture leftCorner;
		Texture rightCorner;
//		Texture 
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
		if (lCorner != null) {
			
		}
		if (rCorner != null) {
			
		}
	}
	
	public void generateObject() {
		
	}
	
	public void generateShading() {
		
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
