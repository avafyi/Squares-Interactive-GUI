import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * 
 * @author Caleb Piekstra
 *
 */

public class TextureGroup {
	public HashMap<String, Texture> textures = null;
	public String groupDir = null;
	
	public TextureGroup(ArrayList<File> files, String dir) {
		if (files == null) {
			return;
		}
		textures = new HashMap<String, Texture>();
		groupDir = dir;
		for (int fileIdx = 0; fileIdx < files.size(); fileIdx++) {
			File file = files.get(fileIdx);
			textures.put(file.getName(), new Texture(file, file.getName(), groupDir));
		}
	}//end constructor
	
	public Texture getTextureExact(String textureName) {
		return textures.get(textureName);
	}
	
	public ArrayList<Texture> getTexturesLike(String genericName) {
		// Holds the similarly named textures
		ArrayList<Texture> likeTextures = new ArrayList<Texture>();
		// Go through all textures in the group
		for (String name : textures.keySet()) {
			// Check if the texture's name is similar to the generic name
			if (name.contains(genericName)) {
				// Add the like texture to the list of like textures
				likeTextures.add(textures.get(name));
			}
		}
		// Return the like textures list, empty if none found
		return likeTextures;
	}
}
