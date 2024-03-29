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

	// Cereal Identification (not frosted flakes)
	private static final long serialVersionUID = -8781116976800446850L;

	// Window title
	public static final String TITLE = "Squares Interactive - SQUINT";
			
	// Size of the desktop client window
	public static final int CANVAS_WIDTH = 800;		// the pixel width of the application window
	public static final int CANVAS_HEIGHT = 800;	// the pixel height of the application window
	
	// Constants that define the level's characteristics
	public static final int MAP_LAYERS = 4;	// The number of texture layers in the map
	public static final int MAP_LEVEL = 0;	// Currently unimplemented
	
	// Toggle between AI mode and client mode
	private final boolean AI_MODE = false;					
	// The list of ai players for triggering movements
	private Player[] ai_players;					
	// The number of AI requested
	private static final int NUM_AI_PLAYERS = 1972;	
	// The delay in ms between the AI movements
	public static final int AI_MOVE_DELAY = 100; 		
	// The delay in ms between terrain updates
	public static final int TERRAIN_ANIMATION_DELAY = 200;	
	
	// The player
	public Player player = null;
	// The number of clients connected to the host
	public int num_players = 0;		
	// Used to keep track of keys that have not been released
	public List<Integer> heldKeys = new ArrayList<Integer>();	
	
	// the number of pixels per grid square
	public static final int MAP_DIM = 40;				
	
	// The "background" image for the level - used so we only redraw moving parts
	private BufferedImage roomBackgroundImage = null;
	
	// The entire level
	Map level = null;
	
	// These are pointers to data in the 'level' variable
	private MapSquare[][] mapSquares = null;
	private MapSquare[] animatedSquares = null;
	
	// Resource loader
	private ResourceLoader resLoad = null;
	
	// Texture Groups for the avatars
	AvatarGroup avatars = null;

	/** Constructor to setup the GUI components */
	public SquintMainWindow() 
	{		
		// Create a resource loader so we can get textures
		resLoad = new ResourceLoader();		
		// Create the level's map editor
		MapEditor me = new MapEditor(resLoad, MAP_LEVEL, CANVAS_WIDTH, CANVAS_HEIGHT, MAP_LAYERS, MAP_DIM);
		// Edit the level using the map editor
		editLevel(me);
		// Save the level's map editor as a map
		level = (Map)me;
		// Allow for easy access to the map squares
		mapSquares = level.map.squares;
		// Allow for easy access to the map's animated squares
		animatedSquares = level.map.animatedSquares;
		// Create a static background image for the level
		roomBackgroundImage = makeImage(level.map.textures, level.map.coords);
		// Create an avatar group for the players
		avatars = new AvatarGroup(resLoad, "re");
		
		// Set up the client window
		setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));		
		addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // Determine what mode to start the client in
		if (AI_MODE) {
			// Set up the AI and lock out user input
			initAI();
		} else {
			// Create a new player and update the map with it's location
			player = new Player(avatars.getAvatar("glasses"), mapSquares, Player.Move.DOWN, true, ++num_players);			
			changeMapOccupation(player.x, player.y, player.idx, true);			
		}		
	}
	
	private void editLevel(MapEditor level) {
		// This is the prototype ROOM level
//		level.makeRoom(3,5,14,16,"wood_floor","walls", "shadows");
		// This is the prototype OUTSIDE level
		level.makeOutside(0,0,19,19, "grass","water",TERRAIN_ANIMATION_DELAY, "", "", new TerrainAnimator());
	}
	
	private void initAI() {
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
		Timer autoMoveTimer = new Timer();
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
	}
	
	/**
	 * This creates a static "background" image so we don't have to redraw the
	 * entire map every time a GUI update is triggered
	 * 
	 * @param textures
	 * @param coords
	 * @return
	 */
	public BufferedImage makeImage(Texture[][][] textures, Point[][] coords) {
		BufferedImage bImg = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, 
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bImg.createGraphics();

		// background drawing here, the display that doesn't change
		drawGrid(g2, textures, coords);

		g2.dispose();
		return bImg;
	}

	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  
		// paint background
		drawMap(g);
		// Update the animated textures
		drawAnimatedTerrain(g);
		// Update the avatar textures
		drawAvatars((Graphics2D) g, AI_MODE);
	}
	
	/**
	 * Updates the application window to display the active map static background
	 * 
	 * @param g		the graphics object
	 * @param level	the map to be drawn
	 */
	private void drawMap(Graphics g) {
		// Ensure that a static background image has been generated for the map
		if (roomBackgroundImage != null) {
			g.drawImage(roomBackgroundImage, 0, 0, this);
		}
	}
	
	private void drawAnimatedTerrain(Graphics g) {
		for (MapSquare animatedSquare : animatedSquares) {
			drawImageToGrid(level.map.animator.getCurrentPhaseTexture().textureFile, animatedSquare.col * MAP_DIM, animatedSquare.row * MAP_DIM, g, false, false);
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
	 * Draws the map like a grid
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
			Dimension scaledSize = ImageEditor.getScaledDimension(new Dimension(bimg.getWidth(), bimg.getHeight()), new Dimension(MAP_DIM, MAP_DIM), true);
			img = resizeToBig(img, scaledSize.width, scaledSize.height);
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(img, x_coord, y_coord, null);
	}	
	
	/**
	 * Make a larger copy of the original image
	 * 
	 * @param originalImage
	 * @param biggerWidth
	 * @param biggerHeight
	 * @return
	 */
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

	/**
	 * Draw the player onto the map
	 * 
	 * @param player
	 * @param g
	 */
	private void drawPlayer(Player player, Graphics g) {	
		if (player == null) {
			return;
		}
		int player_x = player.x * MAP_DIM;// + LEFT_WALL_OFFSET;
		int player_y = player.y * MAP_DIM;// + TOP_WALL_OFFSET;	
		String animationSuffix = "";

		if (player.animatePhase > 0) {
			if (player.isJumping) {
				Texture t = level.textures.get("misc").getTextureExact("jump_circle.png");
				// Draw a circle "beneath" the player so it is obvious they are jumping and not floating upwards
				drawImageToGrid(t.textureFile, player_x, player_y, g, false, true);				
				// Calculate the halfway point in pixels
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

	/**
	 * Set up the client window
	 * 
	 * @param args
	 */
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
	
	/**
	 * Handles player movement upon keyboard input
	 * 
	 * @param direction
	 * @param player
	 */
	private void movePlayer(int direction, Player player) {
		// A callable method so we can repaint during animation
		PlayerAnimator aniUp = null;
		
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
			aniUp = new PlayerAnimator();
			aniUp.setPlayer(player);
		}
		MovePlayer.movePlayer(direction, player, aniUp);
	}
	
	/**
	 * Update a map square to indicate whether it contains a player and if so
	 * what is the player's ID
	 * 
	 * @param playerX
	 * @param playerY
	 * @param playerID
	 * @param occupied
	 */
	public void changeMapOccupation(int playerX, int playerY, int playerID, Boolean occupied) {
		mapSquares[playerY][playerX].isOccupied = occupied;
		mapSquares[playerY][playerX].playerIdx = occupied ? playerID : -1;
	}
	
	/**
	 * A callable that is used to update and redraw the level
	 * during player animation
	 *
	 */
	public class PlayerAnimator implements Callable<Void> {
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
	
	/**
	 * A callable used to redraw animated textures during animation
	 *
	 */
	public class TerrainAnimator implements Callable<Void> {
		
		@Override
		public Void call() throws Exception {
			// REPAINT! and stand still no more.
			repaint();
			return null;
		}		
	}
	
	/**
	 * Update the location of the player - called after completion
	 * of the movement animation
	 *  
	 * @param player
	 */
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
	
	/**
	 * Figure out where the player would end up if they moved in
	 * a direction
	 * 
	 * @param player
	 * @param direction
	 * @return
	 */
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

	@Override
	public void keyReleased(KeyEvent e) {
		heldKeys.remove(new Integer(e.getKeyCode()));
    	if (e.getKeyCode() == KeyEvent.VK_SHIFT ) {
    		player.speed = Player.WALKING;
    	}
	}

	@Override
	public void keyTyped(KeyEvent e) { }	
}
