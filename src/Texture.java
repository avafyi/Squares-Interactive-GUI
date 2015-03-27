
public final class Texture {
	
	// Default Image Extension
	public static final String IMG_EXT = ".png";
	public static final String HOUSE = "house/";
	public static final String SHADOW = "shadows/";
	public static final String FLOOR = "floor/";
	public static final String WALL = "walls/";
	public static final String WALL_DMG = "walls_dmg/";
	public static final String O_OUTSIDE_HOUSE = "outside_house/";
	public static final String O_DOOR = "door/";
	public static final String O_GRASS = "grass/";
	public static final String O_HOUSE = "house/";
	public static final String O_PAVEMENT = "pavement/";
	public static final String O_TREE = "tree/";
	public static final String O_WINDOW = "window/";
	public static final String TRANSPARENT = "transparent.png";

	// Main Texture Paths
	public static final String IMAGE_PATH = "res/images/";
	
	static public class Images 
	{
		public String path = IMAGE_PATH;
		public String transparent = this.path + TRANSPARENT;
		@Override
	    public String toString() { return path; }
		
		static public class House extends Images 
		{
			String path = super.path + HOUSE;
			@Override
		    public String toString() { return path; }
			
			static public class Floor extends House 
			{
				String path = super.path + FLOOR;
				@Override
			    public String toString() { return path; }

			}
			static public class Shadow extends House 
			{
				String path = super.path + SHADOW;	
				@Override
			    public String toString() { return path; }
			}
			static public class Wall extends House {
				String path = super.path + WALL;	
				@Override
			    public String toString() { return path; }	
			}
			static public class WallDmg extends House {
				String path = super.path + WALL_DMG;
				@Override
			    public String toString() { return path; }
			}
		}	
		static public class OutsideHouse extends Images 
		{
			String path = super.path + O_OUTSIDE_HOUSE;
			@Override
		    public String toString() { return path; }
			
			static public class Door extends House
			{
				String path = super.path + O_DOOR;
				@Override
			    public String toString() { return path; }
			}
			static public class Grass extends House 
			{
				String path = super.path + O_GRASS;
				@Override
			    public String toString() { return path; }	
			}
			static public class Building extends House
			{
				String path = super.path + O_HOUSE;
				@Override
			    public String toString() { return path; }		
			}
			static public class Pavement extends House 
			{
				String path = super.path + O_PAVEMENT;
				@Override
			    public String toString() { return path; }
			}
			static public class Tree extends House 
			{
				String path = super.path + O_TREE;
				@Override
			    public String toString() { return path; }
			}
			static public class Window extends House 
			{
				String path = super.path + O_WINDOW;
				@Override
			    public String toString() { return path; }
			}
		}
	}
	/*
	 * 
			
		
	 */
//	private static final String HOUSE_PATH = IMAGE_PATH + "house/";
//	private static final String OUTSIDE_HOUSE_PATH = IMAGE_PATH + "outside_house/";
//	
//	// Sub Texture Paths - House Map
//	public static final String H_FLOOR = HOUSE_PATH + "floor/";
//	public static final String H_SHADOWS = HOUSE_PATH + "shadows/";
//	public static final String H_WALL = HOUSE_PATH + "walls/";
//	public static final String H_WALLS_DMG = HOUSE_PATH + "walls_dmg/";
//	
//	// Sub Texture Paths - Outside House Map
//	public static final String OH_DOOR = OUTSIDE_HOUSE_PATH + "door/";
//	public static final String OH_GRASS = HOUSE_PATH + "grass/";
//	public static final String OH_HOUSE = OUTSIDE_HOUSE_PATH + "house/";
//	public static final String OH_PAVEMENT = OUTSIDE_HOUSE_PATH + "pavement/";
//	public static final String OH_TREE = OUTSIDE_HOUSE_PATH + "tree/";
//	public static final String OH_WINDOW = OUTSIDE_HOUSE_PATH + "window/";
//	
//	// Individual Texture Paths
//	public static final String LEFT_WALL = H_WALL + 		"12" 	+ IMG_EXT;
//	public static final String RIGHT_WALL = H_WALL +		"13" 	+ IMG_EXT;
//	public static final String TOP_WALL = H_WALL +			"1" 	+ IMG_EXT;
//	public static final String BOTTOM_WALL =  H_WALL +		"18" 	+ IMG_EXT;
//	public static final String FLOOR_1 = H_FLOOR + 			"1"		+ IMG_EXT;
//	public static final String FLOOR_2 = "in_floor/2.png";
//	public static final String FLOOR_3 = "in_floor/3.png";
//	public static final String FLOOR_4 = "in_floor/4.png";
//	public static final String FLOOR_5 = "in_floor/5.png";
	
	public Texture() {
		
	}

}
