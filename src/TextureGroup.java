import java.io.File;
import java.util.ArrayList;


public class TextureGroup {
	public Texture[] textures = null;
	public String groupDir = null;
	
	public TextureGroup(ArrayList<File> files, String dir) {
		if (files == null) {
			return;
		}
		textures = new Texture[files.size()];
		groupDir = dir;
		for (int fileIdx = 0; fileIdx < textures.length; fileIdx++) {
			File file = files.get(fileIdx);
			textures[fileIdx] = new Texture(file, file.getName(), groupDir);
		}
	}//end constructor
}
