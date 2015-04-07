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
		addTerrain(3,3,13,13,"wood_floor", new Seed(15,6));
	} 
	
	/**
	 * MAP EDITING
	 */
	
	public ArrayList<String> getAvailableTextureGroups() {
		return new ArrayList<String>(super.textures.keySet());	
	}
	
	public void addTerrain(int startRow, int startCol, int endRow, int endCol, String terrainType, Seed seed) {
		addLevelTerrain(this.map, MAP_LAYER.TERRAIN, startRow, startCol, endRow, endCol, terrainType, seed);
	}
	
	public void addWall() {
//		generateWall();
	}
	
	public void addObject() {
		generateObject();
	}
	
	public void addShadow() {
		generateShading();
	}
	
	public void setMapSquareTypes(String[] solids) {
		super.setMapSquareTypes(solids);		
	}
}
