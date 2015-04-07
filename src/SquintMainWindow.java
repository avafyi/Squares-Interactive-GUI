import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This code is the GUI portion of a larger project being built for a networking class
 * at the University of Portland. The project is meant to demonstrate certain aspects
 * and concepts of how networks communicate data.
 * 
 * Team members: Jordan White, Kai Jorgensen, Bryce Matsuda, Caleb Piekstra
 * 
 * @author Caleb Piekstra
 *
 */
public class SquintMainWindow extends JPanel implements KeyListener {

	// Serial ID
	private static final long serialVersionUID = -8781116976800446850L;

	// Window title
	public static final String TITLE = "...Squares Interactive GUI...";

	// File directory constants (used in map generation)
	public static final String AVATAR_SUBDIR = "avatars/re/";	// The file path to the avatars used in this program
	public static final String IMAGES_DIR = "res/images/";		// The file path of images used in this program
	
	// A list of substrings that indicate certain map "objects" that are solid and therefore should block avatars from entering their pixel space
	public static final List<String> SOLIDS = Arrays.asList("wall", "trans");
	
	// Window constants
	public static final int CANVAS_WIDTH = 640;			// the pixel width of the application window
	public static final int CANVAS_HEIGHT = 640;		// the pixel height of the application window
	
	// AI - Disabled by default, set AI_MODE = true; if you want to run in AI mode (note that keyboard input is ignored during AI mode)
	private final boolean AI_MODE = false;			// Set this = true to make some dummies (prevents user control)
	private Player[] ai_players;					// The list of dummies (it will be resized if there are too many requested players that can fit in the map)
	private Timer autoMoveTimer;					// Handles the automated movement of the AIs
	private static final int NUM_AI_PLAYERS = 100;	// The number of dummies you want
	public static final int AI_MOVE_DELAY = 1000; 	// in milliseconds (used to slow down or speed up the dummies' movements)
	
	// PLAYER
	public Player player = null;
	public int num_players = 0;
	public List<Integer> heldKeys = new ArrayList<Integer>();	// Used to make sure that other keypresses do not interrupt an action that should be repeated by holding a key down
	
	// ANIMATION 
//	public static final int ANIMATION_DELAY = 250;										// in milliseconds, represents the total theoretical time of the moving animation between two squares
//	public static final int ANIMATION_DELAY_STEP = ANIMATION_DELAY / Player.NUM_PHASES; // in milliseconds, how much time each phase of the moving animation should take	
//	private boolean phaseComplete = false;	// whether the animation phase has completed	
//	Hashtable<Integer, ScheduledFuture<?>> animationHandlers = new Hashtable<Integer, ScheduledFuture<?>>();	// Pair each animation executor with a playerId to make sure that each player gets their own executor	

	// General Level variables
//	private int currentMap = IN;
	public static final int MAP_DIM = 40;				// the number of pixels per grid square
//	public static final int NUM_SQUARES_ACROSS = CANVAS_WIDTH / MAP_DIM;	// The logical width of the map
//	public static final int NUM_SQUARES_DOWN = CANVAS_HEIGHT / MAP_DIM;		// The logical height of the map
	private static enum Level { interior, exterior };
	
	// Inside Level
//	private String[][][] roomSquaresImageURLs = null;
//	private Point[][] roomSquaresCoords = null;
	private BufferedImage roomBackgroundImage = null;
	private MapSquare[][] mapSquares = null;
	
//	private ThreadListener moveListener = null;
//	private Thread moveThread = null;
	
//	public static final int IN = 0;	// Level ID number
	
	// Outside Level
//	private String[][][] outsideSquaresImageURLs = null;
//	private Point[][] outsideSquaresCoords = null;
//	private BufferedImage outsideBackgroundImage = null;
//	private MapSquare[][] outsideSquares = null;
//	public static final int OUT_SQUARES_ACROSS = 20;	// The logical width of the map
//	public static final int OUT_SQUARES_DOWN = 15;		// The logical height of the map
//	public static final int OUT = 1;	// Level number
	
	//
	Map level = null;
	public static final int MAP_LAYERS = 4;
	AvatarGroup avatars = null;
	public static final int MAP_LEVEL = 0;
	// Resource loader
	private ResourceLoader resLoad = null;

	/** Constructor to setup the GUI components */
	public SquintMainWindow() 
	{		
		// Create a resource loader so we can get textures
		resLoad = new ResourceLoader();		
		// Create the level
		level = new MapEditor(resLoad, MAP_LEVEL, CANVAS_WIDTH, CANVAS_HEIGHT, MAP_LAYERS, MAP_DIM);
		// Allow for easy access to the map squares
		mapSquares = level.map.squares;
		// Create a static background image for the level
		roomBackgroundImage = makeImage(level.map.textures, level.map.coords);
		// Create an avatar group for the players
		avatars = new AvatarGroup(resLoad, "re");
		
//		initRoom();
//		initOutside();
		setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		
		addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

		if (AI_MODE) 
		{
			// Initialize the array of players so they can all be drawn
			ai_players = new Player[NUM_AI_PLAYERS];			
			// Go through and create the specified number of players
			for (int ai = 0; ai < NUM_AI_PLAYERS; ai++) 
			{				
				// Each AI is created in a random available location with a random avatar, facing a random direction
				ai_players[ai] = new Player(avatars.getRandomAvatar(), mapSquares, (int)(Math.random()*4), true, num_players++);
				
				// in the case that there was no more room for players, the player would have a null avatar
				// if the player was not successfully created, then too many AI players were requested and
				// the array of ai_players should be resized, and the loop exited
				if (ai_players[ai].avatar == null) 
				{
					Player[] newAI_players = new Player[ai];
					System.arraycopy(ai_players, 0, newAI_players, 0, ai);
					ai_players = newAI_players;
					break;
				}
				Player currAI = ai_players[ai];
				changeMapOccupation(currAI.x, currAI.y, currAI.idx, true);					
			}
			// Configure a timer to automatically move the AI players
			autoMoveTimer = new Timer();
			autoMoveTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					// Go through all ai players and make them move in a random direction
					for(Player player : ai_players) {
						if (player.allowedToMove) {
							movePlayer((int)(Math.random()*4), player);	
						}
						repaint();		
					}				
				}		
			}, 2000, AI_MOVE_DELAY);
		} else {
			// Create a new player and update the map to show it
//			player = new Player(avatars.getRandomAvatar(), mapSquares, Player.DOWN, true, ++num_players);
			player = new Player(avatars.getAvatar("glasses"), mapSquares, Player.Move.DOWN, true, ++num_players);			
			changeMapOccupation(player.x, player.y, player.idx, true);			
		}		
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
//				// If we run across a map square that has already been initialized (like in the case of a wall texture that takes up
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
//									// Because of how the bottom left corner wall, wall image 14, is textured, the top right map square is not solid
//									// the same goes for the bottom right corner wall, wall image 15
//									if ((c == 1 && r == 0 && layer[row][col].contains("in_walls/14.png")) 
//											|| (c == 0 && r == 0 && layer[row][col].contains("in_walls/15.png"))) 
//									{
//										continue;	// Skip this map square
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
	
	public BufferedImage makeImage(Texture[][][] textures, Point[][] coords) {
		BufferedImage bImg = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, 
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bImg.createGraphics();

		// background drawing here, the display that doesn't change
		drawGrid(g2, textures, coords);

		g2.dispose();
		return bImg;
	}

	/** Custom painting codes on this JPanel */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  // paint background
		drawMap(g, Level.interior);
//		Graphics2D g2d = (Graphics2D) g;
		drawAvatars((Graphics2D) g, AI_MODE);
	}
	
	/**
	 * Updates the application window to display the active map static background
	 * 
	 * @param g		the graphics object
	 * @param level	the map to be drawn
	 */
	private void drawMap(Graphics g, Level level) {
		switch (level) {
			case interior:
				// Ensure that a static background image has been generated for the map
				if (roomBackgroundImage != null) {
					g.drawImage(roomBackgroundImage, 0, 0, this);
				}
				break;
			case exterior:
				// Not yet implemented
				break;
			default:
				// Not a valid map
				break;				
		}
	}
	
	/**
	 * Draws the avatar(s) onto the map
	 * 
	 * @param g2d		the 2d graphics object
	 * @param modeIsAI	whether or not the program is running in ai mode or user-controlled mode
	 */
	private void drawAvatars(Graphics2D g2d, boolean modeIsAI) {
		// If the program is running in AI mode, draw each of the players in the list of AIs
		// Otherwise just draw the single, user-controlled player
		if (modeIsAI) {
			for(Player player : ai_players) {
				drawPlayer(player, g2d);
			}
		} else {
			drawPlayer(player, g2d);	// Draw the player
		}
	}

	/**
	 * Draws the gridlines to define tiles in the roomSquaresImageURLs
	 * 
	 * @param numTilesPerRowAndCol		the number of tiles per row and per column
	 * @param g							the graphics object
	 */
	private void drawGrid(Graphics g, Texture[][][] textures, Point[][] coords) {	    				
		for (int layer = 0, layerCap = textures.length; layer < layerCap; layer++) {
			for (int row = 0, rowCap = textures[layer].length; row < rowCap; row++) {
				for (int col = 0, colCap = textures[layer][row].length; col < colCap; col++) {
					// Allow for empty grid spots in case of larger images
					if ( textures[layer][row][col] != null && textures[layer][row][col].textureFile != null ) {
						// If the image to be drawn to the grid is for shading we want to handle it differently
						if (textures[layer][row][col].textureDir.contains("shad")) {
							drawImageToGrid(textures[layer][row][col].textureFile, coords[row][col].x, coords[row][col].y, g, true, false);							
						} else {
							drawImageToGrid(textures[layer][row][col].textureFile, coords[row][col].x, coords[row][col].y, g, false, false);
						}
					}
				}
			}	
		}
	}
	
	/**
	 * Draw an image onto the room
	 * 
	 * @param imageFile		The path to the image file
	 * @param x_coord		the x coordinate of the top left corner where the image should be drawn (it is drawn from there to the bottom right)
	 * @param y_coord		the y coordinate of the top left corner where the image should be drawn
	 * @param g				the graphics object that we are drawing the image onto
	 * @param isShading		whether or not the image is being used to add shading
	 * @param scaleImage	whether or not to scale the image to fit inside a tile dimension (used to make scale player avatars)
	 */
	private void drawImageToGrid(File imageFile, int x_coord, int y_coord, Graphics g, boolean isShading, boolean scaleImage) {
		Image img = null;
		BufferedImage bimg = null;	// Used if the image needs to be scaled
		try {
			img = ImageIO.read(imageFile);
			bimg = ImageIO.read(imageFile);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to read image file");
		}
		if (img == null) {
			return;
		}
		if (isShading) {			
			// Go through each shade level, represented by a unique color, and
			// replace the color with a specified opacity level
			Color shadeLevelOneColor = new Color(0x000000);
			Color shadeLevelTwoColor = new Color(0x7F007F);
			Color shadeLevelThreeColor = new Color(0xff00ff);
			img = Transparency.makeColorTransparent(img, shadeLevelOneColor, 0x65);
			img = Transparency.makeColorTransparent(img, shadeLevelTwoColor, 0x40);
			img = Transparency.makeColorTransparent(img, shadeLevelThreeColor, 0x20);
			// Once all colors have had their opacity modified, convert the image to a black grayscale
			ImageFilter filter = new GrayFilter(true, 0);  
			ImageProducer producer = new FilteredImageSource(img.getSource(), filter);  
			img = Toolkit.getDefaultToolkit().createImage(producer);  
		}
		if (scaleImage) {
			Dimension scaledSize = ImageEditor.getScaledDimension(new Dimension(bimg.getWidth(), bimg.getWidth()), new Dimension(MAP_DIM, MAP_DIM), true);
			img = resizeToBig(img, scaledSize.width, scaledSize.height);
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(img, x_coord, y_coord, null);
	}	
	
	private Image resizeToBig(Image originalImage, int biggerWidth, int biggerHeight) {
	    int type = BufferedImage.TYPE_INT_ARGB;


	    BufferedImage resizedImage = new BufferedImage(biggerWidth, biggerHeight, type);
	    Graphics2D g = resizedImage.createGraphics();

	    g.setComposite(AlphaComposite.Src);
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	    g.drawImage(originalImage, 0, 0, biggerWidth, biggerHeight, this);
	    g.dispose();


	    return resizedImage;
	}

	private void drawPlayer(Player player, Graphics g) {	
		if (player == null) {
			return;
		}
		int player_x = player.x * MAP_DIM;// + LEFT_WALL_OFFSET;
		int player_y = player.y * MAP_DIM;// + TOP_WALL_OFFSET;	
		String animationSuffix = "";

		if (player.animatePhase > 0) {
			if (player.isJumping) {
				// Calculate the halfway poit in pixels
				int halfwayDisplacement = (MAP_DIM / Player.JUMP_PHASES) * Player.JUMP_PHASES/2;
				// Calculate the amount of pixels away from standing we are at this phase of the animation
				int displacement = (MAP_DIM / Player.JUMP_PHASES) * player.animatePhase;
				if (displacement > halfwayDisplacement) {
					displacement = halfwayDisplacement-(displacement-halfwayDisplacement);
					// we are jumping, not falling
					if (displacement < 0) {
						displacement = 0;
					}
				}
				// Jumping up
				player_y -= displacement;	
			} else {
				int animationStep = (MAP_DIM / Player.MOVE_PHASES) * player.animatePhase;
				if (player.direction == Player.Move.RIGHT) {
					player_x += animationStep;		
				} else if (player.direction == Player.Move.UP) {
					player_y -= animationStep;								
				} else if (player.direction == Player.Move.LEFT) {
					player_x -= animationStep;	
				} else if (player.direction == Player.Move.DOWN) {	
					player_y += animationStep;	
				}				
				animationSuffix = "-" + ((player.animatePhase % 2) + 1);
			}
			// We have drawn the latest phase of the animation, update the player
			player.inAnimationPhase = false;
			player.animatePhase++;
		}
		String textureName = player.direction + animationSuffix + ".png";
		Texture t = player.avatar.getTextureWithName(textureName);
		if (t != null) {
			drawImageToGrid(t.textureFile, player_x, player_y - MAP_DIM/2, g, false, true);
		}
	}

//	class Animation extends TimerTask{
//		private Player p = null;
//		private int speedToggle;
//		
//		public Animation(Player p) {
//			this.p = p;
//			speedToggle = p.speed;
//		}
//		public void run() {
//			// If the player is walking, the animation will occur every other run()
//			if (--speedToggle <= 0) { 
//				speedToggle = p.speed;	// Update the speed of the player in case they started sprinting during the animation
//				animatePlayer(p);
//			}
//		}
//	}
	
	// This sequence will have to be modified so that both the source and destination tiles
	// are considered to be occupied during the animation in order to reserve the destination
	// so that no other player can try to occupy it during the animation sequence
//	public void animatePlayer(Player p) {	
//		if (p == null) {
//			return;
//		}
//		boolean endPhases = false;
//		if (p.animatePhase >= Player.NUM_PHASES) {
//			endPhases = true;
//		}		
//		if (phaseComplete) {
//			phaseComplete = false;
//			p.animatePhase++;
//			repaint();
//		}	
//		if (endPhases) {
//			
//			// Update the map to indicate the player has moved
//			updatePlayerLocation(mapSquares, p, false);
//			
//			// Now that the animation has completed, we can consider the player to be at the new tile
//			if 		(p.direction == Player.RIGHT)	p.x++;		
//			else if (p.direction == Player.UP)		p.y--;								
//			else if (p.direction == Player.LEFT)	p.x--;
//			else if (p.direction == Player.DOWN)	p.y++;	
//			
//			// Update the map to indicate the player's new position
//			updatePlayerLocation(mapSquares, p, true);
//			
//			p.animatePhase = 0;	// Reset to default of no animation			
//			p.allowedToMove = true; // Let the player move again now that the animation is complete
//			animationHandlers.get(p.idx).cancel(true);
//			repaint();			
//		}
//	}
//	
//	/**
//	 * Updates the provided map to indicate whether a player has left or entered a map square
//	 * 
//	 * @param map		The map to update
//	 * @param p			The player being moved
//	 * @param occupied	Whether or not the player's position is being occupied or reset to not occupied
//	 */
//	private void updateMapSquareOccupation(Player p, boolean moving) {
//		if (moving) {
//			mapSquares[p.y][p.x].isOccupied = true;
//			mapSquares[p.y][p.x].playerIdx = p.idx;
//		} else {
//			
//		}
//	}

//	private void movePlayer(int direction, Player p) {
//		if (p == null || !p.allowedToMove) {
//			return;		// Make sure we have a player to move and that they are allowed to move
//		}
//		boolean animate = false;
//		// If the player is not current facing the direction specified via
//		// keyboard input, then turn them to face that direction
//		if ( p.direction != direction ) {
//			p.direction = direction;
//		} else {
//			if ( p.y < 0 || p.x < 0 || p.y >= mapSquares.length || p.x >= mapSquares[p.y].length ) {
//				return;
//			}
//			// for each direction of movement, check to see that the destination square is not a wall and is not occupied by another player			
//			switch (direction) {
//			case Player.RIGHT:
//				// Check array bounds
//				if ( p.x + 1 >= mapSquares[p.y].length ) { 
//					break;
//				} else if ( mapSquares[p.y][p.x + 1].sqType != MapSquare.SquareType.SOLID && mapSquares[p.y][p.x + 1].isOccupied == false ) {
//					animate = true;	
//				}
//				break;
//			case Player.UP:
//				// Check array bounds
//				if ( p.y <= 0 || p.x < 0) { 
//					break;
//				} else if ( mapSquares[p.y - 1][p.x].sqType != MapSquare.SquareType.SOLID && mapSquares[p.y - 1][p.x].isOccupied == false ) {
//					animate = true;	
//				}
//				break;
//			case Player.LEFT:
//				// Check array bounds
//				if ( p.x <= 0  || p.y < 0) { 
//					break;
//				}else if ( mapSquares[p.y][p.x - 1].sqType != MapSquare.SquareType.SOLID && mapSquares[p.y][p.x - 1].isOccupied == false ) {
//					animate = true;	
//				}	
//				break;
//			case Player.DOWN:
//				// Check array bounds
//				if ( p.y + 1 >= mapSquares.length) { 
//					break;
//				}else if ( mapSquares[p.y + 1][p.x].sqType != MapSquare.SquareType.SOLID && mapSquares[p.y + 1][p.x].isOccupied == false ) {
//					animate = true;	
//				}
//				break;
//			}		
//		}
//		if (animate) {
//			p.allowedToMove = false;
//			p.animatePhase = 1;	// Start the animation
//			ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
//			ScheduledFuture<?> taskHandler = scheduledExecutor.scheduleAtFixedRate(new Animation(p), 0, ANIMATION_DELAY_STEP, TimeUnit.MILLISECONDS);
//		    animationHandlers.put(p.idx, taskHandler);
//		    Thread futureCheck = new Thread(new AnimationCompletionChecker(taskHandler));
//		    futureCheck.start();	
//		} else {
//			p.allowedToMove = true;
//		}
//	}

	/** The entry main() method */
	public static void main(String[] args) {
		// Run GUI codes in the Event-Dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame(TITLE);
				frame.setContentPane(new SquintMainWindow());
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setResizable(false);
				frame.pack();             // "this" JFrame packs its components
				frame.setLocationRelativeTo(null); // center the application window
				frame.setVisible(true);            // show it
			}
		});
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (AI_MODE) return;
		if (!heldKeys.contains(e.getKeyCode())) {
			heldKeys.add(new Integer(e.getKeyCode()));		
		}
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
    		player.speed = Player.RUNNING;			
		}
    	if (player.allowedToMove) {
	        if(heldKeys.contains(KeyEvent.VK_D) || heldKeys.contains(KeyEvent.VK_RIGHT)) {
	        	movePlayer(Player.Move.RIGHT, player);
	        } else if(heldKeys.contains(KeyEvent.VK_W) || heldKeys.contains(KeyEvent.VK_UP)) {
	        	movePlayer(Player.Move.UP, player);
	        } else if(heldKeys.contains(KeyEvent.VK_A) || heldKeys.contains(KeyEvent.VK_LEFT)) {
	        	movePlayer(Player.Move.LEFT, player);
	        } else if(heldKeys.contains(KeyEvent.VK_S) || heldKeys.contains(KeyEvent.VK_DOWN)) {
	        	movePlayer(Player.Move.DOWN, player);
	        } else if(heldKeys.contains(KeyEvent.VK_SPACE)) {
	        	player.isJumping = true;
	        	movePlayer(player.direction, player);
	        } else if(heldKeys.contains(KeyEvent.VK_Q)) {
	        	int modVal = Player.Move.RIGHT + 1;
	        	player.direction = ((((player.direction-1) % modVal) + modVal) % modVal);
	        } else if(heldKeys.contains(KeyEvent.VK_E)) {
	        	player.direction = (player.direction + 1) % (Player.Move.RIGHT + 1);
	        }
	        repaint();
		}
	}
	
	private void movePlayer(int direction, Player player) {
		// A callable method so we can repaint during animation
		AnimationUpdater aniUp = null;
		
		// Check if we are simply changing direction or animating
		if (player.direction == direction) {
			// See if we are moving instead of jumping
			if (!player.isJumping) {
				// Holds the destination location of the player
				Point newSquareLoc = getNewPlayerPosition(player, direction);		
				// Holds the map square at the destination point
				MapSquare destinationSquare = level.getMapSquare(newSquareLoc);
				// Make sure there is a square at the destination
				if (destinationSquare == null) {
					return;
				}
				// Ask for permission to move - host must claim the destination map square if client allowed to move
				if(!MoveRequest.canIMoveHere(destinationSquare, newSquareLoc, level.mapCols, level.mapRows)) {
					return;
				}
				// Get the location where the player will be located but don't actually 
				// update the player's location - the old one needs to be maintained for
				// the duration of the movement animation
				// Update the map to show that the destination square has been claimed
				changeMapOccupation(newSquareLoc.x, newSquareLoc.y, player.idx, true);
			}			
			// Move the player
			aniUp = new AnimationUpdater();
			aniUp.setPlayer(player);
		}
		MovePlayer.movePlayer(direction, player, aniUp);
	}
	
	public void changeMapOccupation(int playerX, int playerY, int playerIdx, Boolean occupied) {
		mapSquares[playerY][playerX].isOccupied = occupied;
		mapSquares[playerY][playerX].playerIdx = occupied ? playerIdx : -1;
	}
	
	public class AnimationUpdater implements Callable<Void> {
		private Player player = null;
		
		// Call this before using .call()
		public void setPlayer(Player player) {
			this.player = player;			
		}
		
		@Override
		public Void call() throws Exception {
			if (player != null) {
				// If the player has finished it's animation, it is allowed to move again
				if (player.allowedToMove) {					
					// update the player's location
					updatePlayerLocation(player);
				}
			}
			// Update the display
			repaint();
			return null;
		}		
	}
	
	private void updatePlayerLocation(Player player) {
		if (player.isJumping) {
			// If we were jumping, we are done now
			player.isJumping = false;
			return;
		}
		// Update the map to indicate that the player is no longer at it's old location
		changeMapOccupation(player.x, player.y, player.idx, false);
		// Animation has been completed at this point, update the player's location
		Point newLocation = getNewPlayerPosition(player, player.direction);
		player.x = newLocation.x;
		player.y = newLocation.y;
	}
	
	private Point getNewPlayerPosition(Player player, int direction){
		Point newPoint = new Point(player.x, player.y);
		switch(direction) {
			case Player.Move.RIGHT: newPoint.x++;	break;
			case Player.Move.UP:	newPoint.y--;	break;
			case Player.Move.LEFT:	newPoint.x--;	break;
			case Player.Move.DOWN:	newPoint.y++;	break;
		}
		return newPoint;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		heldKeys.remove(new Integer(e.getKeyCode()));
    	if (e.getKeyCode() == KeyEvent.VK_SHIFT ) {
    		player.speed = Player.WALKING;
    	}
	}

	@Override
	public void keyTyped(KeyEvent e) {
//        System.out.println("keyTyped");
	}
	

	
//	private void initOutside() {
//		outsideSquaresCoords = new Point[OUT_SQUARES_DOWN][OUT_SQUARES_ACROSS]; 	
//		for (int row = 0; row < OUT_SQUARES_DOWN; row++) {
//			for (int col = 0; col < OUT_SQUARES_ACROSS; col++) {	
//				outsideSquaresCoords[row][col] = new Point(col * MAP_DIM, row * MAP_DIM);
//			}
//		}		
//		
//		// Standard wall, floor, wall row
//		final String[] wfw_row = new String[] {
//			"in_walls/12.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_walls/13.png"
//		};
//		// Wall, special floor, wall row
//		final String[] wsfw1_row = new String[] {
//			"in_walls/12.png", "in_floor/5.png", "in_floor/1.png", "in_floor/5.png", "in_floor/0.png", "in_floor/4.png", "in_floor/5.png", "in_floor/2.png", "in_floor/5.png", "in_floor/3.png", "in_floor/5.png", "in_walls/13.png"
//		};
//		// Wall, special floor, wall row
//		final String[] wsfw2_row = new String[] {
//			"in_walls/12.png", "in_floor/1.png", "in_floor/4.png", "in_floor/1.png", "in_floor/3.png", "in_floor/2.png", "in_floor/4.png", "in_floor/2.png", "in_floor/3.png", "in_floor/5.png", "in_floor/2.png", "in_walls/13.png"
//		};
//		// Wall, special floor, wall row
//		final String[] wsfw3_row = new String[] {
//			"in_walls/12.png", "in_floor/1.png", "in_floor/4.png", "in_floor/4.png", "in_floor/3.png", "in_floor/2.png", "in_floor/4.png", "in_floor/5.png", "in_floor/3.png", "in_floor/5.png", "in_floor/2.png", "in_walls/13.png"
//		};
//		final String[] left_shade = new String[] {
//			"", "in_shadows/6.png", "", "", "", "", "", "", "", "", "", "", ""
//		};
//		final String[] empty_row = new String[] {
//			"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""	
//		};
//		final String[] top_row = new String[] {
//			"out_house/3.png", "", "", "", "out_house/0.png", "", "", "", "out_house/1.png", "", "", "", "out_house/4.png", "out_grass/3.png", "out_grass/3.png", "out_grass/3.png", "out_grass/3.png", "out_grass/3.png", "out_grass/3.png", "out_grass/3.png"		
//		};
//		outsideSquaresImageURLs = new String[][][] {
//			{
//				empty_row,	// top row
//				empty_row,	// top row
//				empty_row,	// top row
//				empty_row,	// middle row
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//			},
//			{
//				top_row,	// top row
//				{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""	},
//				empty_row,	// top row
//				empty_row,	// middle row
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row
//			},
//			{
//				empty_row,	// top row
//				empty_row,	// top row
//				empty_row,	// top row
//				empty_row,	// middle row
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row,
//				empty_row
//			}
//		};
//		outsideSquares = new MapSquare[OUT_SQUARES_DOWN][OUT_SQUARES_ACROSS];
//		for (int row = 0; row < OUT_SQUARES_DOWN; row++) {
//			for (int col = 0; col < OUT_SQUARES_ACROSS; col++) {	
//				// Check if any of the layers contains a wall, if so then create a MapSquare that cannot be occupied by a player
//				// If any square in the first layer has an imageURL of "" then that means that it is a part of a larger object
//				
//				// If we run across a map square that has already been initialized (like in the case of a wall texture that takes up
//				// more than one room square) then ignore this square and continue on to the next column
//				if (outsideSquares[row][col] != null) {
//					continue;
//				}
//				findSolidsLoop:
//				for (String[][] layer : outsideSquaresImageURLs) {
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
//									// Because of how the bottom left corner wall, wall image 14, is textured, the top right map square is not solid
//									// the same goes for the bottom right corner wall, wall image 15
//									if ((c == 1 && r == 0 && layer[row][col].contains("in_walls/14.png")) 
//											|| (c == 0 && r == 0 && layer[row][col].contains("in_walls/15.png"))) 
//									{
//										continue;	// Skip this map square
//									}
//									outsideSquares[row+r][col+c] = new MapSquare(true, MapSquare.SOLID, outsideSquaresCoords[row][col], new Dimension(MAP_DIM, MAP_DIM));
//								}	
//							}
//						}
//						break findSolidsLoop;
//					}
//				}
//				if (outsideSquares[row][col] == null) {
//					outsideSquares[row][col] = new MapSquare(false, MapSquare.EMPTY, outsideSquaresCoords[row][col], new Dimension(MAP_DIM, MAP_DIM));
//				}
//			}
//		}	
//		outsideBackgroundImage = makeImage(outsideSquaresImageURLs, outsideSquaresCoords);
//	}
	
}
