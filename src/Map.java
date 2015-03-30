import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Map {
	
	public static final String IMG_EXT = ".png";	// The default image file extension
	
	// Map Constants
	public static final int IN = 0;		// a level
	public static final int OUT = 1;	// a level
	public static final int MAP_LAYERS = 4;	// max number of images needed in a layer
	
	// Map Attributes
	public static int mapId;
	public static int mapWidth;		// In m squares
	public static int mapHeight;	// In m squares
	public static int mapLayers;	// Number of layers of the m (Image layers)

	// Map 
	private BufferedImage mapStaticBackground = null;	// The static background for the m (so the entire level isn't constantly repainted)
	private String[][][] mapTextureURLs = null;			// TODO - once the background and squares are initialized, this is useless
	private Point[][] roomSquaresCoords = null;			// TODO - once the background and squares are initialized, this is useless
	private MapSquare[][] mapSquares = null;	// The logical m (used to track players, objects, and collisions)
		
	// Wall Orientations
	public static enum WallOrientation { LEFT, RIGHT, TOP, BOTTOM };
	public static enum Corner { Q1, Q2, Q3, Q4 };	// The quadrant (2d plane) of the "empty" portion of the wall
//	public static String 
	
	// Floor Variables
	public static enum FloorType { wood, pave, grass};
	public static String[] floorPaths = new String[] {"in_floor/", "out_pave/", "out_grass/"};
	public static Hashtable<FloorType, String> floorHash = new Hashtable<FloorType, String>();
	
	// Map Layer Variables
	public static final int TRANSPARENT_LAYER = 0;
	
	// Map Textures
	public static ArrayList<String> floorTextures = new ArrayList<String>();

	// A map array (just a 3d string)
	public final class MapArr {
		private final String[][][] m;
		
		public MapArr(String[][][] map) {
			this.m = map;
		}
	}
	
	// temp TODO
	public static void main(String[] args) 
	{		
		new Map(0, 400, 400, 4);
	}
	
	
	/**
	 * Maps must be rectangular
	 * 
	 * @param map_id
	 * @param map_width		number of m squares across
	 * @param map_height	number of m squares down
	 */
	public Map(int map_id, int map_width, int map_height, int num_layers) {
		// Set values that will act like constants for the m
		mapId = map_id;
		mapWidth = map_width;
		mapHeight = map_height;
		mapLayers = num_layers;
		
		// Generate the m grid
//		mapTextureURLs = new String[mapLayers][mapHeight][mapWidth];	// Create a m with multiple layers and a [row][col] orientation
		loadTextures();
		MapArr map = generateBlankMap(mapLayers, mapWidth, mapHeight);		
		addTransparentLayer(map, TRANSPARENT_LAYER);
		initFlooring();
//		generateFloor();
	}
	
	private void loadTextures() {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = docBuilder.parse(new File("res\\xml\\Textures.xml"));
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}

		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = null;
		try {
			expr = xPath.compile("//file");
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NodeList nl = null;
		try {
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getParentNode().getAttributes().item(0).getNodeValue().contains("floor")) {
				System.out.println(nl.item(i).getTextContent());
			}
		}
		
		// Gets tha path to all files
		NodeList nodes = doc.getElementsByTagName("file");
		for(int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			ArrayList<String> path = new ArrayList<String>();
			while (n.getParentNode() != null) {
				if (n.hasAttributes()) {
					path.add(n.getAttributes().item(0).getNodeValue());					
				}
				n = n.getParentNode();
			}
			Collections.reverse(path);
			System.out.println(path);
		}
		
	}
	
	private MapArr generateBlankMap(int layers, int w, int h) {		
		return new MapArr(new String[layers][w][h]);
	}
	
	private void addTransparentLayer(MapArr map, int level) {
		int numRows = map.m[level].length;
		int numCols = map.m[level][0].length;
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				map.m[level][row][col] = new TextureGroup.Images().transparent;
			}
		}
	}
	
	private void initFlooring() {
		int idx = 0;
		// Populate the hashtable pairing floortypes to their paths
		for(FloorType ft : FloorType.values()) {
			if (floorPaths[idx] != null) {
				floorHash.put(ft, floorPaths[idx++]);				
			} else {
				// Undefined floor type
				return;
			}
		}
	}
	
	// This can apply for both wood flooring or pavement or grass as supported by available textures
	// Return a generated array? Or merge with a parameter-provided array?
	// 4 1 5 5
	// if randSeed == 0 			all clean
	// if randSeed >= 1 && <= 3 	clean variations
	// if randSeed >= 4 && <= 10	clean mixed with clean variations mixed with unique boards (higher the value, the more unique) 
	private void generateFloor(String[][] map, int sRow, int sCol, int eRow, int eCol, FloorType type, int randSeed) {
		// add in some code so that we get certain floor images based on the randSeed variable
		// use Math.random with some form of scaling so that if randSeed is say, 10 then we have a huge chance
		// of getting unique boards and a much lower change of the normal, clean variations
		
		
		String floorDir = floorHash.get(type);
		while(sRow <= eRow) {
			while(sCol <= eCol) {				
				map[sRow][sCol++] = floorDir + IMG_EXT;	// 
			}
			sRow++;
		}
	}
	
	/**
	 * 
	 * @param row			The logical starting row coordinate on the level (m)
	 * @param col			The logical starting col coordinate on the level (m)
	 * @param orientation	Which wall type, also used to check start, end coordinates
	 * @param len			The length of the wall, if the Corners are not null, they are included in the total len
	 * @param layer			The m layer to place the wall
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
//				// If we run across a m square that has already been initialized (like in the case of a wall texture that takes up
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
//									// Because of how the bottom left corner wall, wall image 14, is textured, the top right m square is not solid
//									// the same goes for the bottom right corner wall, wall image 15
//									if ((c == 1 && r == 0 && layer[row][col].contains("in_walls/14.png")) 
//											|| (c == 0 && r == 0 && layer[row][col].contains("in_walls/15.png"))) 
//									{
//										continue;	// Skip this m square
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
