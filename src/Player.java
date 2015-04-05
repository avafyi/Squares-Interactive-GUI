import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Player {

	public static final int DOWN = 0;
	public static final int LEFT = 1;
	public static final int UP = 2;
	public static final int RIGHT = 3;
	public static final int SPIN_CC = 4;	// spin counter clockwise
	public static final int SPIN_C = 5;		// spin clockwise
	
	public static final int MOVE_DELAY = 250; // in milliseconds
	public static final int NUM_PHASES = 5;
	
	public static final int WALKING = 2;
	public static final int RUNNING = 1;
	
	public static final String DEFAULT_AVATAR = "green";

	// Player attributes
	public int x; 						// the x location of the player using tile coordinates
	public int y;						// the y location of the player using tile coordinates
	public int direction;
	public boolean allowedToMove;
	public int animatePhase;
	private Timer moveTimer = null;
	public int playerIdx;
	public int speed;
	
	// Holds the textures for the avatar (more than one texture because of animations)
	public Texture[] avatarTextures = null;
	
	public Player(MapSquare[][] sq, int direction, String avatar, boolean canIMove, int idx) {
		
		// Load avatar files
		String[] excludedGroups = new String[] {"avatars"};
		textures = new HashMap<String, TextureGroup>();
		loadFiles(".png", excludedGroups);	
		
		
		// Pick a pseudorandom location to place the player based on the given map
		Integer[] numRows = new Integer[sq.length];
		for (int i = 0; i < numRows.length; i++) {
			numRows[i] = i;
		}
		Collections.shuffle(Arrays.asList(numRows));	// Get a random ordering of valid rows
		boolean foundSpot = false;
		// Go through each row until we find a row with an open spot for a player
		findSpotLoop:
		for (int row : numRows) {
			Integer[] numCols = new Integer[sq[row].length];
			for (int i = 0; i < numCols.length; i++) {
				numCols[i] = i;
			}
			Collections.shuffle(Arrays.asList(numCols));	// Get a random ordering of valid rows
			for (int col : numCols) {
				if (sq[row][col].playerIdx != -1 && sq[row][col].isOccupied == false) {
					foundSpot = true;
					x = col;
					y = row;
					break findSpotLoop;
				}
			}
		}
		if (!foundSpot) {
			System.out.println("No room for the player");
			return;
		}

		if (direction < RIGHT || direction > DOWN) {
			this.direction = DOWN;
		} else {
			this.direction = direction;			
		}
		if (avatars.contains(avatar)) {
			this.avatar = avatar;
		} else {
			this.avatar = DEFAULT_AVATAR;
		}
		animatePhase = 0;
		speed = WALKING;
		playerIdx = idx;		
		allowedToMove = canIMove;
	}
	
	private void loadFiles(String fileType, String[] excludedGroups) {
		FileLoader fileLoader = new FileLoader("res/xml/Textures.xml");
		// Get all directories in the filesystem specified by the .xml file above
		ArrayList<String> textureDirs = fileLoader.getFileDirectories();
		
		int textureCount = 0;	// Count how many textures were loaded
		// Go through every directory and create a texture group for it
		for(String dir : textureDirs) {
			// If the directory is an excluded group or a sub-directory of 
			// an excluded group, don't create a group for it
			for (String excludedGroup : excludedGroups) {
				if (dir.contains(excludedGroup));
			}
			String group = getLastBitFromUrl(dir);
			TextureGroup tg = new TextureGroup(fileLoader.createFileGroup(group, fileType), group);
			if (tg.textures != null) {
				textures.put(group, tg);
				textureCount++;
			}			
		}
		System.out.println(textureCount + " texture groups loaded.");
	}
	
	public void startMoveTimer() {		
		allowedToMove = false;
		moveTimer = new Timer();
		moveTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				allowedToMove = true;
				moveTimer.cancel();
			}		
		}, MOVE_DELAY);
	}
	
}
